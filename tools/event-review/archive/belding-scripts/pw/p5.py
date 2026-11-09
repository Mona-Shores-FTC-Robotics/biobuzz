import sys, glob, os, re, statistics as st
sys.path.insert(0, sys.argv[1]); import read_logs as R
root = sys.argv[2]
def at(series, t):
    v = None
    for ts, x in series:
        if ts > t: break
        v = x
    return v
steady=[]; off=[]; inamps_on=[]; inamps_off=[]; fd_on=[]; fd_off=[]; spin=[]; vsteady=[]; voff=[]
tot_ah=[]
for team in ('19429','20245'):
    for p in sorted(glob.glob(f'{root}/{team}/matches/*.gz')):
        d=R.read(p); m=d['/DriverStation/RobotMode']; a=next(t for t,v in m if v=='autonomous'); end=d['/Robot/BatteryVolts'][-1][0]
        tgt=d['/Launcher/TargetTicksPerSecond']; ats=d['/Launcher/AtSpeed']; dip=d['/Launcher/InDip']
        L=d['/Launcher/LeftCurrentAmps']; Rr=d['/Launcher/RightCurrentAmps']
        for (t,v) in L:
            if not a<=t<=end: continue
            r=at(Rr,t) or 0; tg=at(tgt,t) or 0
            if tg>0 and at(ats,t) and not at(dip,t): steady.append(abs(v)+abs(r))
            elif tg==0: off.append(abs(v)+abs(r))
        for (t,v) in d['/Robot/BatteryVolts']:
            if a<=t<=end and v>1:
                tg=at(tgt,t) or 0
                (vsteady if tg>0 and at(ats,t) else voff if tg==0 else []).append(v)
        for (t,v) in d['/Intake/CurrentAmps']:
            if a<=t<=end: (inamps_on if abs(v)>0.3 else inamps_off).append(abs(v))
        for (t,v) in d['/Feeder/CurrentAmps']:
            if a<=t<=end: (fd_on if abs(v)>0.3 else fd_off).append(abs(v))
        for t,e in d['/Events']:
            mm=re.search(r'spin-up from 0 to (\d+) ticks/s: in tolerance (\d+) ms.*at speed (\d+) ms',e)
            if mm and a<=t<=end: spin.append((int(mm.group(2)),int(mm.group(3))))
        # amp-hours of logged mechanisms over the match
        ah=0
        for k in ('/Launcher/LeftCurrentAmps','/Launcher/RightCurrentAmps','/Intake/CurrentAmps','/Feeder/CurrentAmps'):
            s=[(t,abs(v)) for t,v in d[k] if a<=t<=end]
            ah+=sum((s[i+1][0]-s[i][0])*s[i][1] for i in range(len(s)-1))/3600
        tot_ah.append(ah)
q=lambda xs:'median %.2f A (n=%d)'%(st.median(xs),len(xs)) if xs else '-'
print('launcher both wheels, holding speed:',q(steady),' off:',q(off))
print('intake when drawing:',q(inamps_on),' feeder when drawing:',q(fd_on))
print('battery: launcher at speed median %.2f V, launcher off median %.2f V'%(st.median(vsteady),st.median(voff)))
print('spin-up from 0 in-tolerance ms median',st.median([x for x,_ in spin]),' at-speed(held) ms median',st.median([y for _,y in spin]),'n',len(spin))
print('logged mechanisms Ah per match: median %.3f max %.3f'%(st.median(tot_ah),max(tot_ah)))
