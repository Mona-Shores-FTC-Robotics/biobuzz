import sys, glob, os, statistics as st, json, re
sys.path.insert(0, sys.argv[1]); import read_logs as R
root, sp = sys.argv[2], sys.argv[3]
for team in ('19429','20245'):
    print('====', team)
    lm=[]
    for p in sorted(glob.glob(f'{root}/{team}/matches/*.gz')):
        d=R.read(p); m=d['/DriverStation/RobotMode']
        a=next(t for t,v in m if v=='autonomous'); ae=next(t for t,v in m if t>a and v!='autonomous'); te=next(t for t,v in m if v=='teleop')
        end=d['/Robot/BatteryVolts'][-1][0]; sh=d['/Launcher/Shots']
        s=lambda t:(lambda v: v if v is not None else 0)(R.at(sh,t))
        auto_sh=s(ae)-s(a); tele_sh=s(end)-s(te)
        ev=d['/Events']
        la=sum(1 for t,e in ev if a<=t<=ae and e.startswith('auto: command LaunchAll'))
        last=[e for t,e in ev if a<=t<=ae and e.startswith('auto: ')][-1][:60]
        crash=sum(1 for t,e in ev if 'CRASH' in e)
        lm+= [v for t,v in d['/Robot/LoopMs'] if a<=t<=end]
        sc=[e for t,e in ev if 'StartCheck' in e or e.startswith('start check')][:1]
        print(os.path.basename(p)[11:19], f'auto balls {auto_sh:.0f} in {la} volleys | teleop balls {tele_sh:.0f} | crash {crash} | last auto: {last}', sc[:1])
    print('loop ms median %.1f p95 %.1f'%(st.median(lm), sorted(lm)[int(.95*len(lm))]))
m=json.load(open(f'{sp}/m.json'))
hi=max((x['scores'][c]['totalPoints'],x['tournamentLevel'],x['id'],c) for x in m for c in ('red','blue'))
q=[(x['scores'][c]['totalPoints']) for x in m if x['tournamentLevel']=='Quals' for c in ('red','blue')]
print('event high score',hi,' qual high',max(q),' qual median', st.median(q))
for x in m:
  for c in ('red','blue'):
    ts=[t['teamNumber'] for t in x['teams'] if t['alliance'].lower()==c]
    if set(ts)&{19429,20245}:
      sc=x['scores'][c]; print(x['tournamentLevel'][:2],x['id'],ts,'auto',sc['autoPoints'],'total',sc['totalPoints'],'pen by us?', x['scores']['red' if c=='blue' else 'blue'].get('penaltyPointsByOpp', sc.get('penaltyPointsCommitted')), {k:v for k,v in sc.items() if 'enalt' in k or 'ajor' in k or 'inor' in k})
