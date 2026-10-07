#!/usr/bin/env python3
"""Per clip: START from the clock; every score change in AUTO; for each, the swing measured on both HIVEs in the
6 s before it, and a 20 fps strip of both HIVEs over that window for a visual check."""
import sys, subprocess, numpy as np
from tipmeasure import start_from_clock, measure_swing, FPS
HIVES = (470, 30, 950, 260)

def ticks(sig, thresh, min_gap=30):
    idx = np.nonzero(sig > thresh)[0]; out = []
    for k in idx:
        if not out or k - out[-1] > min_gap: out.append(int(k))
    return out

for path in sys.argv[1:]:
    clip = path.replace('.mp4', ''); d = np.load(clip + '.npz')
    start, tk = start_from_clock(d)
    print(f'== {clip}: START {start:.3f}')
    changes = []
    for name, side in (('rscore', 'red'), ('bscore', 'blue')):
        for k in ticks(d[name], 8):
            t = k / FPS
            if start + 2 < t < start + 40: changes.append((t, side))
    changes.sort()
    for n, (t, side) in enumerate(changes):
        t0, t1 = t - 6.5, t + 0.3
        res = {}
        for s in ('red', 'blue'):
            m = measure_swing(d, s, max(0, t0), t1)
            res[s] = m
        best = max(res, key=lambda s: abs(res[s]['final'] - res[s]['plateau']))
        m = res[best]
        on, st = m['onset'], m['stop']
        line = f"  {side} score change at {t:.2f} (+{t - start:.2f}): biggest HIVE motion on {best} (cy {m['plateau']:.0f}->{m['final']:.0f})"
        if on: line += f"; onset {on:.2f} (+{on - start:.2f})"
        if st: line += f"; stop {st:.2f} (+{st - start:.2f})"
        if on and st: line += f"; swing {st - on:.2f} s; score {t - st:.2f} s after stop"
        print(line)
        out = f'sheets/{clip}_chg{n}_{side}.png'
        w, h = HIVES[2] - HIVES[0], HIVES[3] - HIVES[1]
        vf = (f"fps=20,crop={w}:{h}:{HIVES[0]}:{HIVES[1]},scale=240:-1,drawtext=text='%{{pts\\:flt}}':x=2:y=2:fontsize=14:"
              f"fontcolor=yellow:box=1:boxcolor=black@0.6,tile=13x10:padding=2:margin=2")
        subprocess.run(['ffmpeg', '-hide_banner', '-loglevel', 'error', '-ss', f'{max(0, t0):.3f}', '-t', '6.8', '-i', path,
                        '-vf', vf, '-frames:v', '1', '-y', out], check=False)
        print(f'    strip {out} (starts {max(0, t0):.3f})')
