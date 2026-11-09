import sys, glob, json, bisect, statistics as st
sys.path.insert(0, sys.argv[1]); import read_logs as R
root = sys.argv[2]
NAMES = {'19429': ['Q4','Q13','Q19','Q26','Q32','Playoff 1','Upper final','Lower final','Final'],
         '20245': ['Q1','Q7','Q14','Q27','Q31','Playoff 1','Upper final','Final']}
STOP_IN_S, AIM_DEG, TURN_DPS = 3.0, 5.0, 10.0
class Ser:
    def __init__(s, xs): s.t = [t for t, _ in xs]; s.v = [v for _, v in xs]
    def at(s, t):
        i = bisect.bisect_right(s.t, t) - 1
        return s.v[i] if i >= 0 else None
rows = []
for team in NAMES:
    for i, p in enumerate(sorted(glob.glob(f'{root}/{team}/matches/*.gz'))):
        d = R.read(p); te = next(t for t, v in d['/DriverStation/RobotMode'] if v == 'teleop'); end = d['/Robot/BatteryVolts'][-1][0]
        sp = Ser(d['/Drive/OdometrySpeedInPerSec']); aim = Ser(d['/Controls/AimErrorDeg']); ats = Ser(d['/Launcher/AtSpeed'])
        hd = Ser(d['/Odometry/PedroInches/HeadingDeg']); ref = Ser(d['/Odometry/Referenced'])
        sh = d['/Launcher/Shots']; g2 = d['/DriverStation/Joystick1/ButtonValues']; g1 = d['/DriverStation/Joystick0/ButtonValues']
        sqdown = []; prev = 0
        for t, v in g1:
            b = (int(v) >> 2) & 1
            if b and not prev: sqdown.append(t)
            prev = b
        sqheld = lambda t: ((int(Ser(g1).at(t) or 0) >> 2) & 1) == 1
        prev = 0; presses = []
        for t, v in g2:
            b = (int(v) >> 1) & 1
            if b and not prev and te <= t <= end: presses.append(t)
            prev = b
        for t in presses:
            def turn(x):
                a, b = hd.at(x - 0.1), hd.at(x)
                if a is None or b is None: return 0
                dd = (b - a + 180) % 360 - 180
                return abs(dd) / 0.1
            def stopped(x): return (sp.at(x) or 0) < STOP_IN_S and turn(x) < TURN_DPS
            def aimed(x):
                a = aim.at(x); return a is not None and abs(a) <= AIM_DEG
            def ready(x): return stopped(x) and aimed(x) and bool(ats.at(x))
            def since(cond, maxback=15.0, step=0.02):
                if not cond(t - 0.02): return 0.0
                x = t - 0.02
                while x > t - maxback and cond(x): x -= step
                return round(t - x, 2)
            s0 = R.at(sh, t) or 0; nb = next((tt for tt, v in sh if tt > t and v > s0), None)
            rows.append({'robot': team, 'match': NAMES[team][i], 't': round(t - te, 1),
                         'speed': round(sp.at(t) or 0, 1), 'aim': None if aim.at(t) is None else round(aim.at(t), 1), 'at_speed': bool(ats.at(t)),
                         'referenced': bool(ref.at(t)), 'square_held': sqheld(t),
                         'stopped_for': since(stopped), 'aimed_for': since(aimed), 'ready_for': since(ready),
                         'first_ball_s': None if nb is None or nb - t > 5 else round(nb - t, 2)})
json.dump(rows, open(sys.argv[3], 'w'), indent=1)
for team in NAMES:
    rs = [r for r in rows if r['robot'] == team and not r['square_held']]
    print('====', team, 'FIRE presses (not during Square)', len(rs))
    st_ = [r for r in rs if r['stopped_for'] > 0]
    print(' stopped at press', len(st_), ' moving at press', len(rs) - len(st_))
    print(' aimed within 5 deg at press', sum(r['aimed_for'] > 0 for r in rs), ' aim unknown (pose not referenced)', sum(r['aim'] is None for r in rs))
    print(' launcher at speed at press', sum(r['at_speed'] for r in rs))
    rd = [r['ready_for'] for r in rs if r['ready_for'] > 0]
    print(' fully ready (stopped+aimed+at speed) at press', len(rd), ' wait median %.2f s' % st.median(rd) if rd else '', 'list', sorted(rd))
    sf = [r['stopped_for'] for r in st_]
    print(' stopped-for median %.2f s' % st.median(sf) if sf else '', sorted(sf))
    fb = [r['first_ball_s'] for r in rs if r['first_ball_s'] is not None]
    print(' first ball after press median %.2f s, n %d, no ball within 5 s %d' % (st.median(fb), len(fb), sum(r['first_ball_s'] is None for r in rs)))
    print(' aim at press values', sorted(r['aim'] for r in rs if r['aim'] is not None))
