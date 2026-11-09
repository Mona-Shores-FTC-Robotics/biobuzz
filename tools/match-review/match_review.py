#!/usr/bin/env python3
"""One match's log and video, lined up, for reading together. Python 3.9+ stdlib, ffmpeg/ffprobe;
Pillow optional (captioned contact sheets).

  match_review.py fetch  EVENT MATCH TEAM        log + clean video into the match's folder
  match_review.py sync   EVENT MATCH TEAM [--play-at S]
                                                 where Auto PLAY is in the video, with its evidence
  match_review.py frames EVENT MATCH TEAM (--auto S... | --teleop S...) [--span 1] [--fps 10]
                         [--crop X,Y,W,H] [--scale 2] [--sheet]
  match_review.py clip   EVENT MATCH TEAM (--auto S | --teleop S) [--len 6] [--crop X,Y,W,H] [--scale 2]

EVENT is the event code (USMIBEQ), MATCH as matches.json writes it (Q-1, M-7), TEAM 19429 or 20245.
Times are seconds after that period's PLAY in the log (Auto: "PLAY: <colour> alliance"; TeleOp:
"PLAY: field forward = driver forward"). Crops are in the video's 1280x720 pixels.
Everything lands in TeamCode/build/match-review/<EVENT>_<MATCH>_<TEAM>/; each step runs the ones
before it if their output is missing. The procedure is .claude/skills/match-review/SKILL.md.
"""
import argparse
import html
import json
import math
import os
import re
import shutil
import subprocess
import sys
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, '..', '..'))
MATCHES_JSON = os.path.join(REPO, 'tools', 'advantagescope', 'replay', 'matches.json')
VIDEOS_JSON = os.path.join(HERE, 'videos.json')
HAND = os.path.join(REPO, 'tools', 'advantagescope', 'replay', 'marker', 'scoreboard-hand.gray')
sys.path.insert(0, os.path.join(REPO, 'tools', 'pull-logs'))
import read_logs  # noqa: E402

# The stream scoreboard's transition hand (same crop and template as the replay tool's Replay.ps1).
HAND_CROP = 'crop=iw*0.036458:ih*0.052778:iw*0.481771:ih*0.834259,scale=28:24:flags=area,format=gray'
# Qualifiers at Belding: log Auto PLAY = scoreboard switch - this. Playoffs: our PLAY was 1-4 s later.
SWITCH_AFTER_PLAY_S = 30.45


def die(msg):
    sys.exit('match_review: ' + msg)


def run(cmd, **kw):
    r = subprocess.run(cmd, capture_output=True, **kw)
    if r.returncode != 0:
        die('%s failed: %s' % (cmd[0], (r.stderr or b'').decode(errors='replace')[-600:]))
    return r.stdout


# --- Which match ---------------------------------------------------------------------------------

def find_match(event, match, team):
    doc = json.load(open(MATCHES_JSON))
    ev = next((e for e in doc['events'] if e['event'] == event), None)
    if not ev:
        die('%s is not in %s (events: %s)' % (event, MATCHES_JSON, ', '.join(e['event'] for e in doc['events'])))
    m = next((m for m in ev['matches'] if m['match'] == match and str(m['team']) == str(team)), None)
    if not m:
        have = ', '.join('%s/%s' % (m['match'], m['team']) for m in ev['matches'])
        die('no %s for %s at %s in matches.json (have: %s)' % (match, team, event, have))
    return ev, m


def folder(event, match, team):
    d = os.path.join(REPO, 'TeamCode', 'build', 'match-review', '%s_%s_%s' % (event, match, team))
    os.makedirs(d, exist_ok=True)
    return d


# --- The log -------------------------------------------------------------------------------------

def git(*args):
    return subprocess.run(['git', '-C', REPO] + list(args), capture_output=True)


def remote_branches(prefix):
    out = git('for-each-ref', '--format=%(refname:short)', 'refs/remotes/origin/' + prefix).stdout.decode()
    return [b for b in out.split() if b]


def find_log_blob(team, log):
    """matches.json's log is '<logs branch>/<file>'. The file is on an events/* branch as
    <team>/matches/<file>, or on that logs/* branch as <file>. Returns 'ref:path' or None."""
    branch, name = log.split('/', 1)
    for ref in remote_branches('events/'):
        spec = '%s:%s/matches/%s' % (ref, team, name)
        if git('cat-file', '-e', spec).returncode == 0:
            return spec
    spec = 'origin/logs/%s:%s' % (branch, name)
    if git('cat-file', '-e', spec).returncode == 0:
        return spec
    return None


def fetch_log(out, team, m):
    dst = os.path.join(out, 'log.wpilog.gz')
    if os.path.exists(dst):
        return dst
    spec = find_log_blob(team, m['log'])
    if not spec:
        print('  fetching events/* and logs/* branches ...')
        git('fetch', '-q', 'origin', 'refs/heads/events/*:refs/remotes/origin/events/*',
            'refs/heads/logs/*:refs/remotes/origin/logs/*')
        spec = find_log_blob(team, m['log'])
    if not spec:
        die('log %s is on no events/* or logs/* branch' % m['log'])
    data = git('show', spec).stdout
    with open(dst + '.part', 'wb') as f:
        f.write(data)
    os.replace(dst + '.part', dst)
    print('  log    %s' % spec)
    return dst


def log_facts(path):
    """Auto/TeleOp PLAY, alliance, lane-full edges and counted shots, all in log seconds."""
    d = read_logs.read(path)
    ev = d.get('/Events', [])
    auto = next(((t, e) for t, e in ev if re.match(r'PLAY: (RED|BLUE) alliance', str(e))), None)
    if not auto:
        die('the log has no "PLAY: <colour> alliance" event: not a match log?')
    tele = next((t for t, e in ev if 'PLAY: field forward = driver forward' in str(e) and t > auto[0]), None)
    full, last, on = [], False, None
    for t, v in d.get('/Lane/Full', []):
        v = bool(v)
        if v and not last:
            on = t
        if not v and last and on is not None:
            full.append((on, t))
            on = None
        last = v
    shots, prev = [], None
    for t, v in d.get('/Launcher/Shots', []):
        if prev is not None and v > prev:
            shots.append(t)
        prev = v
    return {'auto_play': auto[0], 'blue': 'BLUE' in str(auto[1]), 'teleop_play': tele,
            'full': full, 'shots': shots}


# --- The video -----------------------------------------------------------------------------------

def drive_folder_files(folder_id):
    """{name: file id} of a link-shared Drive folder, from its public embedded view (no login)."""
    url = 'https://drive.google.com/embeddedfolderview?id=' + folder_id
    page = urllib.request.urlopen(url, timeout=60).read().decode('utf-8', 'replace')
    found = re.findall(r'id="entry-([\w-]+)".*?flip-entry-title">([^<]+)<', page, re.S)
    return {html.unescape(n): i for i, n in found}


def drive_download(file_id, dst):
    url = 'https://drive.usercontent.google.com/download?id=%s&export=download&confirm=t' % file_id
    run(['curl', '-sS', '-L', '--fail', '-o', dst + '.part', url])
    with open(dst + '.part', 'rb') as f:
        head = f.read(512)
    if b'<html' in head.lower() or b'<!doctype' in head.lower():
        die('Drive sent a web page, not the file (not link-shared?): %s' % url)
    os.replace(dst + '.part', dst)


def fetch_video(out, event, match, team):
    dst = os.path.join(out, 'video.mp4')
    side = os.path.join(out, 'video.json')
    if os.path.exists(dst) and os.path.exists(side):
        return dst, json.load(open(side))
    videos = json.load(open(VIDEOS_JSON))
    entry = videos.get('events', {}).get(event)
    if not entry or not entry.get('folder'):
        die('%s has no link-shared folder in %s: a mentor adds it (see tools/match-review/README.md)'
            % (event, VIDEOS_JSON))
    name = '%s_%s_%s.mp4' % (event, re.sub(r'[^A-Za-z0-9-]', '', match), team)
    files = drive_folder_files(entry['folder'])
    if name not in files:
        die('%s is not in the folder %s (it has: %s)' % (name, entry['folder'], ', '.join(sorted(files)) or 'nothing'))
    if name + '.json' in files:
        drive_download(files[name + '.json'], side)
    else:
        with open(side, 'w') as f:
            json.dump({}, f)
    drive_download(files[name], dst)
    print('  video  %s (%.0f MB)' % (name, os.path.getsize(dst) / 1e6))
    return dst, json.load(open(side))


def probe(video):
    out = run(['ffprobe', '-v', 'error', '-select_streams', 'v:0', '-show_entries',
               'stream=width,height:format=duration', '-of', 'json', video])
    j = json.loads(out)
    return j['streams'][0]['width'], j['streams'][0]['height'], float(j['format']['duration'])


def raw_frames(video, t0, dur, vf, pix='rgb24'):
    return run(['ffmpeg', '-hide_banner', '-loglevel', 'error', '-ss', '%.3f' % max(0.0, t0), '-t', '%.3f' % dur,
                '-i', video, '-vf', vf, '-f', 'rawvideo', '-pix_fmt', pix, '-'])


# --- Sync ----------------------------------------------------------------------------------------

def ncc(a, b):
    n = len(b)
    ma, mb = sum(a) / n, sum(b) / n
    ab = aa = bb = 0.0
    for x, y in zip(a, b):
        x -= ma
        y -= mb
        ab += x * y
        aa += x * x
        bb += y * y
    return 0.0 if aa < 1e-6 or bb < 1e-6 else ab / math.sqrt(aa * bb)


def find_switch(video, duration):
    """First frame the scoreboard's transition hand appears and stays (the field's end of Auto):
    (seconds into the file, score) or (None, best score)."""
    tpl = open(HAND, 'rb').read()
    n, fps = len(tpl), 30
    raw = raw_frames(video, 0, duration, 'fps=%d,%s' % (fps, HAND_CROP), 'gray')
    score = [ncc(raw[i:i + n], tpl) for i in range(0, len(raw) - n + 1, n)]
    before, stay, thr = 60, 225, 0.75
    for f in range(before, len(score) - stay + 1):
        if score[f] < thr or any(s >= thr for s in score[f - before:f]):
            continue
        if sum(1 for s in score[f:f + stay] if s >= thr) >= stay * 0.8:
            return f / fps, score[f]
    return None, max(score or [0])


def find_light(video, t0, t1, blue, on_s, off_s):
    """Our indicator light: steady in the alliance colour, drops at PLAY, green from on_s to off_s
    after it (the log's first lane-full). The replay tool's BiobuzzLight.Find, in Python.
    Returns dict(at, x, y, score, green, drop) in video seconds and 1280x720 pixels, or None."""
    w, h, fps = 320, 180, 60
    dur = t1 + off_s + 1.0 - t0
    raw = raw_frames(video, t0, dur, 'fps=%d,scale=%d:%d:flags=area' % (fps, w, h))
    n = w * h
    frames = len(raw) // (n * 3)
    sgn = (lambda r, g, b: b - max(r, g)) if blue else (lambda r, g, b: r - max(g, b))
    pre = min(frames, fps)
    # Candidates: alliance colour >= 60 and steady (sd < 12) through the window's first second, above the overlay.
    s1 = [0.0] * n
    s2 = [0.0] * n
    mn = [1e9] * n
    top = int(h * 0.83) * w
    for f in range(pre):
        fr = raw[f * n * 3:(f + 1) * n * 3]
        rs, gs, bs = fr[0:top * 3:3], fr[1:top * 3:3], fr[2:top * 3:3]
        for p in range(top):
            v = sgn(rs[p], gs[p], bs[p])
            s1[p] += v
            s2[p] += v * v
            if v < mn[p]:
                mn[p] = v
    cand = [p for p in range(top) if mn[p] >= 60 and math.sqrt(max(0.0, s2[p] / pre - (s1[p] / pre) ** 2)) < 12]
    if not cand:
        return None, 'no steady %s light in the first second of the window' % ('blue' if blue else 'red')

    def series(p):
        col, grn = [0.0], [0.0]
        for f in range(frames):
            o = f * n * 3 + p * 3
            r, g, b = raw[o], raw[o + 1], raw[o + 2]
            col.append(col[-1] + sgn(r, g, b))
            grn.append(grn[-1] + (g - max(r, b)))
        return col, grn

    def mean(a, i, j):
        i, j = max(0, i), min(frames, j)
        return (a[j] - a[i]) / max(1, j - i)

    F = lambda s: int(round(s * fps))  # noqa: E731
    best = (-1e9, None, None, 0, 0)
    for p in cand:
        col, grn = series(p)
        for i in range(max(F(0.6), F(0.0)), F(t1 - t0) + 1):
            a0, b0 = i + F(on_s), i + F(off_s)
            if b0 + F(0.3) > frames:
                break
            g = mean(grn, a0 + 2, b0 - 2) - max(mean(grn, a0 - F(0.3), a0 - 2), mean(grn, b0 + 2, b0 + F(0.3)))
            d = mean(col, i - F(0.4), i - 2) - mean(col, i + 3, i + F(0.5))
            s = min(2 * g, d)
            if s > best[0]:
                best = (s, p, i, g, d)
    score, p, i, g, d = best
    if p is None or score < 40:
        return None, 'best light scored %.0f (40 needed): our robot out of shot or hidden' % score
    # The exact frame: brightest of the 3x3 cells around it, where its colour crosses halfway down.
    px, py = p % w, p // w

    def light(f):
        v = -1e9
        for dy in (-1, 0, 1):
            for dx in (-1, 0, 1):
                x, y = px + dx, py + dy
                if 0 <= x < w and 0 <= y < h:
                    o = f * n * 3 + (y * w + x) * 3
                    v = max(v, sgn(raw[o], raw[o + 1], raw[o + 2]))
        return v
    bef = [light(f) for f in range(i - F(0.5), i - F(0.15)) if f >= 0]
    aft = [light(f) for f in range(i + F(0.15), i + F(0.5)) if f < frames]
    half = (sum(bef) / max(1, len(bef)) + sum(aft) / max(1, len(aft))) / 2
    edge = next((f for f in range(i - F(0.25), i + F(0.25) + 1) if 0 < f < frames and light(f) < half), i)
    # Green at the light, measured: when it rises and falls through halfway, for the evidence line.
    col, grn = series(p)
    gv = [grn[f + 1] - grn[f] for f in range(frames)]
    dark = gv[edge + F(0.15):edge + F(0.45)]  # just after PLAY: off, before the lane fills
    lo = sum(dark) / max(1, len(dark))
    hi = max(gv[edge + F(0.15):] or [lo])
    gh = (lo + hi) / 2
    g_on = next((f for f in range(edge + F(0.15), frames) if gv[f] > gh), None)
    g_off = next((f for f in range(g_on or frames, frames) if gv[f] < gh), None) if g_on is not None else None
    sc = 1280 / w
    return {'at': t0 + edge / fps, 'x': int(px * sc), 'y': int(py * sc), 'score': score, 'green': g, 'drop': d,
            'green_on': None if g_on is None else (g_on - edge) / fps,
            'green_off': None if g_off is None else (g_off - edge) / fps}, ''


def do_sync(a, quiet=False):
    out = folder(a.event, a.match, a.team)
    path = os.path.join(out, 'sync.json')
    if quiet and os.path.exists(path) and a.play_at is None:
        return json.load(open(path))
    ev, m = find_match(a.event, a.match, a.team)
    log = fetch_log(out, a.team, m)
    video, side = fetch_video(out, a.event, a.match, a.team)
    lf = log_facts(log)
    W, H, dur = probe(video)
    print('%s %s %s (%s): %s' % (a.event, a.match, a.team, m['alliance'], os.path.relpath(out, REPO)))
    if side.get('marked'):
        print('  WARNING: this video is an old-style clip (countdown and black boxes drawn on it); '
              'the light and scoreboard checks may fail in its first ~40 s')
    print('  video  %dx%d, %.1f s; starts at stream %s s' % (W, H, dur, side.get('from', '?')))
    tele_off = None if lf['teleop_play'] is None else lf['teleop_play'] - lf['auto_play']
    print('  log    Auto PLAY %.3f s (%s), TeleOp PLAY %s' % (
        lf['auto_play'], 'BLUE' if lf['blue'] else 'RED',
        '%.3f s (Auto PLAY + %.3f)' % (lf['teleop_play'], tele_off) if tele_off else 'none'))
    fulls = [(on - lf['auto_play'], off - lf['auto_play']) for on, off in lf['full']]
    first = next(((on, off) for on, off in fulls if on > 0), None)
    if first:
        print('  log    first Lane: FULL after PLAY: green %.2f to %.2f s after it' % first)
    switch, hscore = find_switch(video, dur)
    if switch is not None:
        print('  scoreboard  2:01 -> 0:08 switch at %.2f s in the file (match %.2f); minus %.2f = %.2f'
              % (switch, hscore, SWITCH_AFTER_PLAY_S, switch - SWITCH_AFTER_PLAY_S))
    else:
        print('  scoreboard  transition hand not found (best match %.2f)' % hscore)
    how, play, lt = None, None, None
    if a.play_at is not None:
        play, how = a.play_at, 'given (--play-at)'
    elif switch is not None and first and first[0] < 3:
        est = switch - SWITCH_AFTER_PLAY_S
        lt, why = find_light(video, est - 2.1, est + 5.5, lf['blue'], first[0], first[1])
        if lt:
            play, how = lt['at'], 'our light'
            print('  light  at (%d,%d) in 1280x720, score %.0f (colour drop %.0f, green bump %.0f): '
                  'left the alliance colour at %.2f s in the file' % (lt['x'], lt['y'], lt['score'], lt['drop'],
                                                                       lt['green'], lt['at']))
            if lt['green_on'] is not None:
                print('  light  green measured %.2f to %s s after it; log says %.2f to %.2f'
                      % (lt['green_on'], '%.2f' % lt['green_off'] if lt['green_off'] is not None else '?', *first))
            print('  check  switch - light PLAY = %.2f s (qualifiers ~%.2f; a playoff can be 1-4 s less)'
                  % (switch - lt['at'], SWITCH_AFTER_PLAY_S))
        else:
            print('  light  not found: %s' % why)
    elif not first or first[0] >= 3:
        print('  light  no lane-full green in the first 3 s of Auto: nothing to find it by')
    if play is None and switch is not None:
        play, how = switch - SWITCH_AFTER_PLAY_S, 'scoreboard only (qualifier offset; WRONG by 1-4 s in a playoff)'
    if play is None:
        die('no sync: pass --play-at <seconds into video.mp4 of Auto PLAY>, found by eye with frames --raw')
    print('  SYNC   Auto PLAY = %.2f s in the file, %s' % (play, how))
    if tele_off:
        print('         TeleOp PLAY = %.2f s in the file (log offset; check it on a TeleOp green)' % (play + tele_off))
    end = tele_off or 30.0
    later = [on for on, off in fulls if (first[1] if first else 0) < on < end][:3]
    shots = [t - lf['auto_play'] for t in lf['shots']]
    tgreen = [on - tele_off for on, off in fulls if tele_off and on > tele_off][:3]
    fmt = lambda xs: ', '.join('%.2f' % t for t in xs) or '-'  # noqa: E731
    print('  check by eye (frames ... --span 1.2 --sheet), at least one of:')
    print('         Auto greens (Lane: FULL) at %s; Auto counted shots at %s' % (fmt(later), fmt([t for t in shots if 0 < t < end][:6])))
    print('         TeleOp greens at %s' % fmt(tgreen))
    s = {'play_video_s': play, 'how': how, 'teleop_offset_s': tele_off, 'auto_play_log_s': lf['auto_play'],
         'switch_video_s': switch, 'light': lt, 'later_greens_auto_s': later, 'shots_auto_s': shots}
    with open(path, 'w') as f:
        json.dump(s, f, indent=1)
    return s


# --- Frames and clips ----------------------------------------------------------------------------

def to_video(s, period, t):
    if period == 'teleop':
        if s['teleop_offset_s'] is None:
            die('the log has no TeleOp PLAY')
        return s['play_video_s'] + s['teleop_offset_s'] + t
    return s['play_video_s'] + t


def crop_filter(a):
    vf = []
    if a.crop:
        x, y, w, h = [int(v) for v in a.crop.split(',')]
        vf.append('crop=%d:%d:%d:%d' % (w, h, x, y))
    if a.scale and a.scale != 1:
        vf.append('scale=iw*%g:ih*%g:flags=lanczos' % (a.scale, a.scale))
    return vf


def do_frames(a):
    s = do_sync(a, quiet=True)
    out = folder(a.event, a.match, a.team)
    video = os.path.join(out, 'video.mp4')
    times = [('auto', t) for t in a.auto or []] + [('teleop', t) for t in a.teleop or []]
    if not times:
        die('give --auto and/or --teleop times')
    fdir = os.path.join(out, 'frames')
    os.makedirs(fdir, exist_ok=True)
    made = []
    for period, t in times:
        v0 = to_video(s, period, t) - a.span / 2
        k = max(1, int(round(a.span * a.fps)) + 1)
        for j in range(k):
            lt = t - a.span / 2 + j / a.fps
            name = '%s_%07.2f.png' % ('A' if period == 'auto' else 'T', lt)
            dst = os.path.join(fdir, name)
            vf = ','.join(crop_filter(a)) or 'null'
            run(['ffmpeg', '-hide_banner', '-loglevel', 'error', '-y', '-ss', '%.3f' % (v0 + j / a.fps),
                 '-i', video, '-frames:v', '1', '-vf', vf, dst])
            made.append((period, lt, dst))
    print('%d frames in %s (named A_/T_ + seconds after that PLAY)' % (len(made), os.path.relpath(fdir, REPO)))
    if a.sheet:
        sheet(made, os.path.join(out, 'sheet_%s.png' % '_'.join('%s%g' % (p[0], t) for p, t in times)),
              '%s %s %s' % (a.event, a.match, a.team))


def sheet(made, dst, title):
    try:
        from PIL import Image, ImageDraw
    except ImportError:
        print('(no Pillow: no sheet; the frames are there)')
        return
    ims = [Image.open(p).convert('RGB') for _, _, p in made]
    w, h = ims[0].size
    cols = max(1, min(len(ims), 5 if w <= 800 else 3))
    rows = (len(ims) + cols - 1) // cols
    pad, cap = 8, 22
    S = Image.new('RGB', (cols * (w + pad) + pad, rows * (h + cap + pad) + pad + cap), (245, 245, 240))
    d = ImageDraw.Draw(S)
    d.text((pad, 4), title, fill=(20, 20, 20))
    for k, (im, (period, t, _)) in enumerate(zip(ims, made)):
        x, y = pad + (k % cols) * (w + pad), cap + pad + (k // cols) * (h + cap + pad)
        d.text((x, y), '%s %.2f s' % ('Auto' if period == 'auto' else 'TeleOp', t), fill=(20, 20, 20))
        S.paste(im, (x, y + cap - 4))
    S.save(dst)
    print('sheet  %s' % os.path.relpath(dst, REPO))


def do_clip(a):
    s = do_sync(a, quiet=True)
    out = folder(a.event, a.match, a.team)
    period, t = ('teleop', a.teleop) if a.teleop is not None else ('auto', a.auto)
    if t is None:
        die('give --auto or --teleop')
    v0 = to_video(s, period, t)
    dst = os.path.join(out, 'clip_%s%g_%gs.mp4' % (period[0].upper(), t, a.len))
    vf = crop_filter(a)
    if a.fps:
        vf.append('fps=%g' % a.fps)
    run(['ffmpeg', '-hide_banner', '-loglevel', 'error', '-y', '-ss', '%.3f' % v0, '-t', '%.3f' % a.len,
         '-i', video_path(out), '-an'] + (['-vf', ','.join(vf)] if vf else []) +
        ['-c:v', 'libx264', '-preset', 'veryfast', '-crf', '20', dst])
    print('clip   %s (%s %.2f s + %g s)' % (os.path.relpath(dst, REPO), period, t, a.len))


def video_path(out):
    return os.path.join(out, 'video.mp4')


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = p.add_subparsers(dest='cmd', required=True)

    def common(sp):
        sp.add_argument('event')
        sp.add_argument('match')
        sp.add_argument('team')
    common(sub.add_parser('fetch'))
    sp = sub.add_parser('sync')
    common(sp)
    sp.add_argument('--play-at', type=float, help='Auto PLAY in video.mp4 seconds, found by eye')
    for name in ('frames', 'clip'):
        sp = sub.add_parser(name)
        common(sp)
        multi = name == 'frames'
        sp.add_argument('--auto', type=float, nargs='+' if multi else None)
        sp.add_argument('--teleop', type=float, nargs='+' if multi else None)
        sp.add_argument('--crop', help='X,Y,W,H in 1280x720 pixels')
        sp.add_argument('--scale', type=float, default=2 if multi else 1, help='lanczos scale after the crop')
        sp.add_argument('--fps', type=float, default=10 if multi else None)
        if multi:
            sp.add_argument('--span', type=float, default=1.0, help='seconds of frames around each time')
            sp.add_argument('--sheet', action='store_true', help='also a captioned contact sheet (Pillow)')
        else:
            sp.add_argument('--len', type=float, default=6.0)
        sp.set_defaults(play_at=None)
    a = p.parse_args()
    for tool in ('ffmpeg', 'ffprobe', 'curl'):
        if not shutil.which(tool):
            die(tool + ' is not installed')
    if a.cmd == 'fetch':
        ev, m = find_match(a.event, a.match, a.team)
        out = folder(a.event, a.match, a.team)
        fetch_log(out, a.team, m)
        fetch_video(out, a.event, a.match, a.team)
        print(os.path.relpath(out, REPO))
    elif a.cmd == 'sync':
        do_sync(a)
    elif a.cmd == 'frames':
        do_frames(a)
    else:
        do_clip(a)


if __name__ == '__main__':
    main()
