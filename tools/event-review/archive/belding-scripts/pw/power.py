import sys, os, glob, statistics as st
sys.path.insert(0, sys.argv[1]); import read_logs as R
root = sys.argv[2]
def phases(d):
    m = d.get('/DriverStation/RobotMode', [])
    a = [t for t, v in m if v == 'autonomous']; te = [t for t, v in m if v == 'teleop']
    end = max(t for k in ('/Robot/BatteryVolts',) for t, _ in d[k])
    ae = next((t for t, v in m if a and t > a[0] and v != 'autonomous'), None)
    return (a[0] if a else None), ae, (te[0] if te else None), end
def vs(d, a, b): return [v for t, v in d['/Robot/BatteryVolts'] if a <= t <= b]
def mean(d, k, a, b):
    xs = [abs(v) for t, v in d.get(k, []) if a <= t <= b]; return st.mean(xs) if xs else 0
def peak(d, k, a, b):
    xs = [abs(v) for t, v in d.get(k, []) if a <= t <= b]; return max(xs) if xs else 0
f = lambda x: f'{x:5.2f}'
for team in ('19429', '20245'):
    print(f'\n==== {team} matches')
    print('file                 INIT  autoMin  teleStart teleMed teleMin last30med  end | amps tele mean: launch intake feeder | launch peak')
    for p in sorted(glob.glob(f'{root}/{team}/matches/*.gz')):
        d = R.read(p); a, ae, te, end = phases(d)
        init = d['/Robot/BatteryVolts'][0][1]
        av = vs(d, a, ae) if a else []
        tv = vs(d, te, end) if te else []
        l30 = vs(d, end - 30, end)
        la = mean(d, '/Launcher/LeftCurrentAmps', te, end) + mean(d, '/Launcher/RightCurrentAmps', te, end)
        lp = peak(d, '/Launcher/LeftCurrentAmps', te, end) + peak(d, '/Launcher/RightCurrentAmps', te, end)
        print(os.path.basename(p)[11:19], ' ', f(init), f(min(av)) if av else '  -  ', ' ', f(tv[0]) if tv else '-', '  ', f(st.median(tv)), ' ', f(min(tv)), '  ', f(st.median(l30)), ' ', f(tv[-1]),
              '|', f(la), f(mean(d, '/Intake/CurrentAmps', te, end)), f(mean(d, '/Feeder/CurrentAmps', te, end)), '|', f(lp), f'tele {end-te:.0f}s' if te else '')
    print(f'---- {team} every log, INIT volts in time order (battery swaps show as jumps up)')
    rows = []
    for p in glob.glob(f'{root}/{team}/*/*.gz'):
        try: d = R.read(p)
        except Exception as e: continue
        b = d.get('/Robot/BatteryVolts', [])
        if not b: continue
        rows.append((os.path.basename(p)[:19], b[0][1], min(v for _, v in b), b[-1][1], b[-1][0], 'M' if '/matches/' in p else ' '))
    for r in sorted(rows):
        print(f'{r[0]} {r[5]} init {r[1]:5.2f} min {r[2]:5.2f} end {r[3]:5.2f} dur {r[4]:5.0f}s')
