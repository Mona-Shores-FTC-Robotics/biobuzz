"""Fills an event's entry in matches.json: which robot log is which match, from the event's real match times.

    python3 tools/advantagescope/replay/maintain/find_matches.py USMIBEQ --season 2026 --video https://www.youtube.com/watch?v=...

FTCScout (public API) gives every match our teams played and when it actually started. The logs are on the
`logs/<date>-<team>-RC` branches (fetched here first). A log's Auto starts at its file-name time plus its first
enable; each hub's clock is off by its own constant (seen at Belding: 20245 +11 s, 19429 +11 min 51 s), so the
offset is the median over the event and each match takes the full-match log (a 25 s+ Auto, then TeleOp) nearest
to it. A match with no log within 20 s of the fitted time is left out and printed. Writes matches.json in place,
keeping any video_start already typed for a match. Python 3 standard library only.
"""
import argparse
import datetime as dt
import gzip
import json
import os
import statistics
import subprocess
import sys
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, "..", "..", "..", ".."))
sys.path.insert(0, os.path.join(REPO, "tools", "pull-logs"))
import read_logs  # noqa: E402

TEAMS = (19429, 20245)
MATCHES_JSON = os.path.join(HERE, "..", "matches.json")


def ftcscout(code, season):
    q = ('{ eventByCode(season: %d, code: "%s") { name start end timezone matches { description actualStartTime '
         'teams { teamNumber alliance } } } }') % (season, code)
    req = urllib.request.Request("https://api.ftcscout.org/graphql", json.dumps({"query": q}).encode(),
                                 {"content-type": "application/json", "user-agent": "biobuzz-replay"})
    return json.load(urllib.request.urlopen(req, timeout=60))["data"]["eventByCode"]


def git(*args):
    return subprocess.run(["git", "-C", REPO, *args], check=True, capture_output=True).stdout


def match_logs(dates, team):
    """[(auto start by the hub's clock, auto length, '<branch>/<file>')] for every full-match log."""
    out = []
    for date in dates:
        branch = f"{date}-{team}-RC"
        try:
            git("fetch", "-q", "origin", f"refs/heads/logs/{branch}:refs/remotes/origin/logs/{branch}")
        except subprocess.CalledProcessError:
            continue
        for line in git("ls-tree", "--name-only", f"origin/logs/{branch}").decode().split():
            if not line.endswith(".wpilog.gz"):
                continue
            raw = gzip.decompress(git("show", f"origin/logs/{branch}:{line}"))
            tmp = os.path.join(REPO, "TeamCode", "build", "find-matches.wpilog")
            os.makedirs(os.path.dirname(tmp), exist_ok=True)
            with open(tmp, "wb") as f:
                f.write(raw)
            en = read_logs.read(tmp).get("/DriverStation/Enabled", [])
            on = next((t for t, v in en if v), None)
            off = next((t for t, v in en if on is not None and t > on and not v), None)
            if on is None or off is None or off - on < 25 or not any(v for t, v in en if t > off):
                continue  # not a match: no 25 s+ Auto followed by TeleOp
            base = dt.datetime.strptime(line[:19], "%Y-%m-%d_%H-%M-%S")
            out.append((base + dt.timedelta(seconds=on), round(off - on, 2), f"{branch}/{line}"))
    return out


def main():
    p = argparse.ArgumentParser()
    p.add_argument("event")
    p.add_argument("--season", type=int, default=2026)
    p.add_argument("--video", default="", help="the event's stream (one video for the whole day)")
    p.add_argument("--utc-offset", type=float, default=-5, help="the hubs' clock zone (Michigan in winter: -5)")
    a = p.parse_args()

    ev = ftcscout(a.event, a.season)
    start, end = dt.date.fromisoformat(ev["start"]), dt.date.fromisoformat(ev["end"])
    dates = [str(start + dt.timedelta(days=i)) for i in range((end - start).days + 1)]
    rows, missing = [], []
    for team in TEAMS:
        played = []
        for m in ev["matches"]:
            for t in m["teams"]:
                if t["teamNumber"] == team and m["actualStartTime"]:
                    utc = dt.datetime.fromisoformat(m["actualStartTime"].replace("Z", "+00:00"))
                    local = (utc + dt.timedelta(hours=a.utc_offset)).replace(tzinfo=None)
                    played.append((m["description"], t["alliance"], m["actualStartTime"], local))
        logs = match_logs(dates, team)
        if not played:
            continue
        if not logs:
            missing += [f"{d} {team}: no match logs on logs/<date>-{team}-RC" for d, *_ in played]
            continue
        # The hub's clock offset: the gap (log minus match) that most match/log pairs agree on within 20 s, then
        # the median of those pairs. Works however far off the clock is.
        gaps = [(l[0] - local).total_seconds() for *_, local in played for l in logs]
        mode = max(gaps, key=lambda g: sum(1 for x in gaps if abs(x - g) < 20))
        offset = statistics.median(x for x in gaps if abs(x - mode) < 20)
        print(f"{team}: hub clock {offset:+.1f} s from the field's", file=sys.stderr)
        for desc, alliance, start_utc, local in played:
            log = min(logs, key=lambda l: abs((l[0] - local).total_seconds() - offset))
            gap = (log[0] - local).total_seconds() - offset
            if abs(gap) > 20:
                missing.append(f"{desc} {team}: no log within 20 s (nearest {gap:+.0f} s)")
                continue
            rows.append({"match": desc, "team": team, "alliance": alliance, "start_utc": start_utc,
                         "video_start": "", "log": log[2]})
    rows.sort(key=lambda r: (r["start_utc"], r["team"]))

    with open(MATCHES_JSON, encoding="utf-8") as f:
        doc = json.load(f)
    old = next((e for e in doc["events"] if e["event"] == a.event), None)
    typed = {(m["match"], m["team"]): m.get("video_start", "") for m in (old or {}).get("matches", [])}
    for r in rows:
        r["video_start"] = typed.get((r["match"], r["team"]), "")
    entry = {"event": a.event, "name": ev["name"], "date": ev["end"],
             "video": a.video or (old or {}).get("video", ""), "video_shift_s": (old or {}).get("video_shift_s", 0),
             "matches": rows}
    if (old or {}).get("logs"):
        entry["logs"] = old["logs"]  # the events/* branch that keeps the logs, typed once they are moved there
    doc["events"] = [entry] + [e for e in doc["events"] if e["event"] != a.event]
    write(doc)
    print(f"Wrote {len(rows)} matches for {a.event} into matches.json", file=sys.stderr)
    for m in missing:
        print("  not found: " + m, file=sys.stderr)


def write(doc):
    """matches.json with one match per line, so it stays easy to edit by hand."""
    lines = ["{", f'  "_comment": {json.dumps(doc["_comment"])},', '  "events": [']
    for i, e in enumerate(doc["events"]):
        lines.append("    {")
        for k in ("event", "name", "date", "video", "video_shift_s"):
            lines.append(f"      {json.dumps(k)}: {json.dumps(e[k])},")
        if e.get("logs"):
            lines.append(f'      "logs": {json.dumps(e["logs"])},')
        lines.append('      "matches": [')
        for j, m in enumerate(e["matches"]):
            lines.append("        " + json.dumps(m) + ("," if j < len(e["matches"]) - 1 else ""))
        lines.append("      ]")
        lines.append("    }" + ("," if i < len(doc["events"]) - 1 else ""))
    lines += ["  ]", "}"]
    with open(MATCHES_JSON, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(lines) + "\n")


if __name__ == "__main__":
    main()
