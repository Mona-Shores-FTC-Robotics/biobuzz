import sys, glob, statistics as st
sys.path.insert(0, sys.argv[1]); import read_logs as R
root=sys.argv[2]
def at(s,t):
    v=None
    for ts,x in s:
        if ts>t: break
        v=x
    return v
for team in ('19429','20245'):
    g1=[]; gaps=[]; per=[]; none=0
    for p in sorted(glob.glob(f'{root}/{team}/matches/*.gz')):
        d=R.read(p); sh=d['/Launcher/Shots']; m=d['/DriverStation/RobotMode']
        for t,e in d['/Events']:
            if e.startswith(('launch spot: gate open','auto: LaunchAll')) or ('fire:' in e and '→ fired' in e):
                s0=at(sh,t) or 0
                times=[tt for tt,v in sh if t<tt<t+4 and v>s0]
                # shot increments
                inc=[]; last=s0
                for tt,v in sh:
                    if t<tt<t+4 and v>last: inc.append(tt); last=v
                if inc:
                    g1.append(inc[0]-t); gaps += [b-a for a,b in zip(inc,inc[1:])]; per.append(len(inc))
                else: none+=1
    print(team,'gate/feed start → 1st ball median %.2f s (p25 %.2f, p75 %.2f) n=%d, no ball %d'%(st.median(g1),sorted(g1)[len(g1)//4],sorted(g1)[3*len(g1)//4],len(g1),none),
          '| between balls median %.2f s'%st.median(gaps),'| balls per volley median',st.median(per))
