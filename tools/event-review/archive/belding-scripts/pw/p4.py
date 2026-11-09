import sys, glob, os, statistics as st
sys.path.insert(0, sys.argv[1]); import read_logs as R
root = sys.argv[2]
rows=[]
for team in ('19429', '20245'):
    for p in sorted(glob.glob(f'{root}/{team}/matches/*.gz')):
        d = R.read(p); m = d['/DriverStation/RobotMode']; te = next(t for t, v in m if v == 'teleop')
        end = d['/Robot/BatteryVolts'][-1][0]
        sp = d['/Drive/OdometrySpeedInPerSec']; cf = d['/Drive/CommandForward']; cs = d['/Drive/CommandStrafe']
        bv = d['/Robot/BatteryVolts']
        def win(a, b):
            s = sorted(v for t, v in sp if a <= t <= b)
            v = [x for t, x in bv if a <= t <= b and x > 1]
            return s[int(.98*len(s))] if s else 0, st.median(v)
        e = win(te, te + 40); l = win(end - 45, end - 5)
        rows.append((team, os.path.basename(p)[11:19], e, l))
        print(team, os.path.basename(p)[11:19], 'first 40s tele: top speed %.0f in/s at %.2f V | last 40s: %.0f in/s at %.2f V' % (e[0], e[1], l[0], l[1]))
