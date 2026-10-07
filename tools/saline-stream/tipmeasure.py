#!/usr/bin/env python3
"""Measure a TIP from a tipscan .npz: START from the overlay clock, swing onset/stop from the rocker-frame centroid."""
import sys, numpy as np
FPS = 60.0

def smooth(a, w=7):
    k = np.ones(w) / w
    return np.convolve(np.nan_to_num(a, nan=np.nanmean(a)), k, mode='same')

def start_from_clock(d, thresh=8):
    c = d['clock']
    idx = np.nonzero(c > thresh)[0]
    ticks = []
    for k in idx:
        if not ticks or k - ticks[-1] > 30:
            ticks.append(int(k))
    # the first tick is 2:30 -> 2:29, one second after START
    if not ticks:
        return None, ticks
    # the overlay can fade in before the match: take the first tick that another tick follows about 1 s later
    first = None
    for i in range(len(ticks) - 1):
        if 50 <= ticks[i + 1] - ticks[i] <= 70:
            first = ticks[i]
            break
    if first is None:
        first = ticks[0]
    return first / FPS - 1.0, [k / FPS for k in ticks]

def measure_swing(d, side, t0, t1):
    """Within [t0, t1] s find the swing: plateau before, onset (centroid leaves plateau by >2 sigma, sustained),
    stop (centroid settles into its final band after the peak)."""
    cy = smooth(d[f'{side}_cy'])
    a, b = int(t0 * FPS), int(t1 * FPS)
    seg = cy[a:b]
    # plateau: first 1.0 s of the window
    p = seg[:60]
    pm, ps = p.mean(), max(p.std(), 0.8)
    # final plateau: last 0.8 s of the window
    q = seg[-48:]
    qm, qs = q.mean(), max(q.std(), 0.8)
    direction = np.sign(qm - pm)
    dev = (seg - pm) * direction
    # onset: first index after which dev stays > 2 sigma for 12 frames
    onset = None
    for k in range(60, len(seg) - 12):
        if (dev[k:k + 12] > 2 * ps).all():
            onset = k
            break
    # stop: after the motion peak, the first frame from which the rocker-frame change (xor) stays below
    # 1.5x its pre-TIP baseline for 10 frames -- the rocker is on its stop and nothing in the frame moves
    xor = d[f'{side}_xor'][a:b]
    base = np.median(d[f'{side}_xor'][max(0, a - 300):a]) if a > 0 else np.median(xor[:60])
    stop = None
    if onset is not None:
        peak = onset + int(np.argmax(xor[onset:]))
        for k in range(peak, len(xor) - 10):
            if (xor[k:k + 10] < max(1.5 * base, 60)).all():
                stop = k
                break
    # the final level is read after the stop, if found
    if stop is not None and stop + 30 <= len(seg):
        qm, qs = seg[stop:stop + 30].mean(), max(seg[stop:stop + 30].std(), 0.8)
    return dict(plateau=pm, plateau_sd=ps, final=qm, final_sd=qs,
                onset=(a + onset) / FPS if onset is not None else None,
                stop=(a + stop) / FPS if stop is not None else None)

if __name__ == '__main__':
    path, side, t0, t1 = sys.argv[1], sys.argv[2], float(sys.argv[3]), float(sys.argv[4])
    d = np.load(path)
    start, ticks = start_from_clock(d)
    m = measure_swing(d, side, t0, t1)
    print(f'{path}: START (clock) = {start:.3f} s  (first tick {ticks[0]:.3f}, {len(ticks)} ticks)')
    print(f"  {side} swing: plateau cy {m['plateau']:.1f}±{m['plateau_sd']:.1f} -> {m['final']:.1f}±{m['final_sd']:.1f}; onset {m['onset']}, stop {m['stop']}")
    if m['onset'] and m['stop']:
        print(f"  onset {m['onset']-start:.2f} s after START, stop {m['stop']-start:.2f} s after START, swing {m['stop']-m['onset']:.2f} s")
