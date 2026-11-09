import sys, json, glob, re
sys.path.insert(0, '/home/user/biobuzz/tools/pull-logs')
import read_logs as R
out = []
for team in ['20245', '19429']:
    vols = [v for v in json.load(open(f'newcount/{team}.json')) if v['phase'] == 'auto']
    for path in sorted(glob.glob(f'bel/{team}/matches/*.gz')):
        log = path.split('/')[-1][:19]
        d = R.read(path)
        ev = [(t, str(e)) for t, e in d.get('/Events', [])]
        play = next((t for t, e in ev if e.startswith('PLAY')), None)
        if play is None: continue
        full = d.get('/Lane/Full', []); pres = d.get('/Lane/Present', []); amps = d.get('/Intake/CurrentAmps', [])
        cnt = d.get('/Lane/Count', [])
        vs = sorted((v['open_s'], v['new'], v['lane_at_open']) for v in vols if v['log'] == log)
        # picks in Auto (first 30 s)
        starts = [t for t, e in ev if 'FLOWER pick: start' in e and t < play + 30.5]
        ends = [t for t, e in ev if 'FLOWER pick: done' in e or 'FLOWER pick: stopped' in e]
        pre_full = R.at(full, play + 0.2)
        for s in starts:
            e = min([x for x in ends if x > s] or [s + 4])
            a, b = s, e + 0.5
            sin = sum(1 for t, x in ev if a <= t <= b and re.match(r'lane: \w+ in', x))
            ovf = sum(1 for t, x in ev if a <= t <= b and 'arrived with the lane counted full' in x)
            edges, prev = 0, R.at(pres, a)
            for t, x in pres:
                if a < t <= b:
                    if x and not prev: edges += 1
                    prev = x
            spk = R.spikes(amps, a, b)
            prevv = [v for v in vs if v[0] < s]; nextv = [v for v in vs if v[0] > s]
            # full at any time between pick end and next volley open
            nv = nextv[0] if nextv else None
            full_after = any(x for t, x in full if e <= t <= (nv[0] if nv else e + 2)) or bool(R.at(full, e + 0.3))
            out.append(dict(team=team, log=log, t=round(s - play, 1), sensor_in=sin, sensor_ovf=ovf, present_edges=edges, spikes=spk,
                            full_after=full_after, count_before=R.at(cnt, a), prev_launch=prevv[-1][1] if prevv else None,
                            next_launch=nv[1] if nv else None, preload_full=bool(pre_full), n_prev_volleys=len(prevv)))
json.dump(out, open('recon/picks.json', 'w'), indent=1)
for r in out: print(r)
