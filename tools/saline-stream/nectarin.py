#!/usr/bin/env python3
"""nectarin.py CLIP STOP MOVES SIDE CORNERS_JSON: when does a NECTAR of the tipping alliance first appear in that
alliance's LOADING ZONE after the TIP? Tracks the alliance's NECTAR at 20 fps from STOP-0.5 s to STOP+12 s in the floor
band and lists the tracks that begin inside the zone (red: x < 16 in, blue: x > 125.5 in; y 85-125 in, the far half of
the side wall by the human player) with their start time relative to the rocker starting to move (MOVES) and stopping."""
import sys, json, csv, subprocess, os
clip, stop, moves, side, cj = sys.argv[1], float(sys.argv[2]), float(sys.argv[3]), sys.argv[4], sys.argv[5]
tmp = 'nectar_tmp.csv'
subprocess.run(['python3', __file__.rsplit('/', 1)[0] + '/track.py', clip, str(stop - 0.5), str(stop + 12), cj, tmp], check=True, capture_output=True)
tr = {}
for r in csv.DictReader(open(tmp)):
    if r['kind'] != side: continue
    tr.setdefault(r['track'], []).append((float(r['t']), float(r['fx_in']), float(r['fy_in'])))
hits = []
for i, p in tr.items():
    t, x, y = p[0]
    inzone = (x < 16 if side == 'red' else x > 125.5) and 85 < y < 125
    if inzone and len(p) >= 6: hits.append((t, x, y, p[-1][1], p[-1][2], len(p)))
hits.sort()
for t, x, y, x1, y1, n in hits[:6]:
    print('  %s NECTAR track from %.2f s (%+.1f s after the rocker moved, %+.1f s after it stopped) at (%.0f,%.0f) -> (%.0f,%.0f), %d frames' % (side, t, t - moves, t - stop, x, y, x1, y1, n))
if not hits: print('  none')
