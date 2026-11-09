import sys, json, glob, statistics as st
sys.path.insert(0,'/home/user/biobuzz/tools/pull-logs'); import read_logs as R
J=json.load(open('ctl/cycles.json')); vs=J['volleys']
NAMES = {'19429': ['Q4','Q13','Q19','Q26','Q32','Playoff 1','Upper final','Lower final','Final'],'20245': ['Q1','Q7','Q14','Q27','Q31','Playoff 1','Upper final','Final']}
D={}
for team in NAMES:
    for i,p in enumerate(sorted(glob.glob(f'bel/{team}/matches/*.gz'))):
        d=R.read(p); te=next(t for t,v in d['/DriverStation/RobotMode'] if v=='teleop')
        sp=[(t-te,v) for t,v in d['/Drive/OdometrySpeedInPerSec']]
        ins=[t-te for t,e in d['/Events'] if e.startswith('lane: ') and ' in, ' in e]
        full=[t-te for t,e in d['/Events'] if e.startswith('Lane: FULL')]
        D[(team,NAMES[team][i])]=(sp,ins,full)
def moving_again(sp,t0):
    run=0; start=None
    for t,v in sp:
        if t<t0: continue
        if v>8:
            if start is None: start=t
            if t-start>=0.3: return start
        else: start=None
    return None
prev={}
for v in vs:
    k=(v['robot'],v['match']); sp,ins,full=D[k]
    if 'feed' not in v: v['launch_span']=None; continue
    press=v['first']-v['feed']-v['settle']-v['drive']
    mv=moving_again(sp,v['first'])
    v['press']=press; v['moving_again']=mv
    v['launch_span']=(mv-press) if mv else None
    if v['first_volley']: prev[k]=None
    start=prev.get(k)
    if start is not None and v.get('away') is not None:
        fi=[x for x in ins if start<x<=press]; fl=[x for x in full if start<x<=press]
        v['tfp2']=(fi[0]-start) if fi else None
        v['cycle2']=press-start
    else: v['tfp2']=None; v['cycle2']=None
    prev[k]=mv
json.dump(J,open('cycles_span.json','w'))
md=lambda xs: round(st.median([x for x in xs if x is not None]),2) if [x for x in xs if x is not None] else None
for t in ('20245','19429'):
    allv=[v for v in vs if v['robot']==t and v['launch_span'] is not None]
    print(t,'ALL volleys',len(allv),'launch span med',md([v['launch_span'] for v in allv]),
          'Square',md([v['launch_span'] for v in allv if v['kind']=='Square']),'FIRE',md([v['launch_span'] for v in allv if v['kind']=='FIRE']),
          'old press->last',md([v['drive']+v['settle']+v['feed']+v['launch'] for v in allv]))
    c=[v for v in vs if v['robot']==t and v.get('away') is not None and not v['first_volley'] and v['cycle2'] is not None]
    for g,sub in (('Filled up',[v for v in c if v.get('full_before_press')]),('Shot before full',[v for v in c if not v.get('full_before_press')])):
        print('  ',g,len(sub),'to first (from moving again)',md([v['tfp2'] for v in sub]),'launch span',md([v['launch_span'] for v in sub]),'cycle',md([v['cycle2']+ (v['launch_span'] or 0) for v in sub]))
