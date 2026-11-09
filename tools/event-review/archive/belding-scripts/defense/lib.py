import pickle,glob,re,bisect,math
def load(f):
    d=pickle.load(open(f,'rb'))
    rm=d['/DriverStation/RobotMode']
    t0=[t for t,m in rm if m=='teleop']
    ts=t0[-1] if t0 else None
    a0=[t for t,m in rm if m=='autonomous']
    d['_tele']=(ts, ts+125 if ts else None)
    d['_auto']=(a0[0],a0[0]+30) if a0 else None
    return d
def files(team=None):
    return sorted(f for f in glob.glob('cache/[12]*.pkl') if team is None or f.startswith('cache/'+team))
class S:
    def __init__(s,ser): s.t=[a for a,b in ser]; s.v=[b for a,b in ser]
    def at(s,t):
        i=bisect.bisect_right(s.t,t)-1
        return s.v[i] if i>=0 else None
    def win(s,a,b):
        i=bisect.bisect_left(s.t,a); j=bisect.bisect_right(s.t,b)
        return list(zip(s.t[i:j],s.v[i:j]))
def ev(d,pat,a=None,b=None):
    return [(t,e) for t,e in d['/Events'] if re.match(pat,e) and (a is None or a<=t<=b)]
