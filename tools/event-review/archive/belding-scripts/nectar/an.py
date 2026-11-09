import pickle, re, json, bisect
S='/tmp/claude-0/-home-user-biobuzz/fc16faa2-d150-54ec-97b5-3071dce4f144/scratchpad'
c=pickle.load(open(f'{S}/nectar/cache.pkl','rb'))
MATCH={'19429':['Q4','Q13','Q19','Q26','Q32','M4','M7','M9','M10'],'20245':['Q1','Q7','Q14','Q27','Q31','M3','M7','M10']}
def inr(h,a,b): return (a<=h<=b) if a<=b else (h>=a or h<=b)
def cls(h):
    if h!=h: return 'UNK'
    if inr(h,48,130): return 'POLLEN'
    if inr(h,330,47): return 'RED'
    if inr(h,180,270): return 'BLUE'
    return 'UNK'
def at(s,t):
    ts=[x[0] for x in s]; i=bisect.bisect_right(ts,t)-1
    return s[i][1] if i>=0 else None
SLOT={0:'empty',1:'POLLEN',2:'RED',3:'BLUE',4:'UNK'}
rows=[]; summ=[]
for team in ('19429','20245'):
  files=sorted(f for (t,k,f) in c if t==team and k=='matches')
  for mi,f in enumerate(files):
    d=c[(team,'matches',f)]; m=MATCH[team][mi]
    ev=d['/Events']
    ally=[re.search(r'PLAY: (\w+) alliance',str(e)).group(1) for _,e in ev if re.search(r'PLAY: (\w+) alliance',str(e))][-1]
    enemy='BLUE' if ally=='RED' else 'RED'
    modes=d['/DriverStation/RobotMode']
    auto0=[t for t,v in modes if v=='autonomous'][0]; tele0=[t for t,v in modes if v=='teleop'][0]
    def phase(t):
        if t>=tele0: return 'TeleOp %.1f s'%(t-tele0)
        if t>=auto0: return 'Auto %.1f s'%(t-auto0)
        return 'INIT'
    H=d['/ColorSensor/HueDeg']; A=d['/ColorSensor/Alpha']; Sa=d['/ColorSensor/Saturation']
    rd=[(t,h,a[1],s[1]) for (t,h),a,s in zip(H,A,Sa) if t>=auto0]
    # sightings: runs of consecutive readings classified NECTAR with alpha>0.05
    runs=[]; cur=None
    for t,h,a,s in rd:
        k=cls(h)
        if a>0.05 and k in('RED','BLUE'):
            if cur and cur['col']==k: cur['n']+=1; cur['t1']=t; cur['hues'].append(h); cur['alphas'].append(a); cur['sats'].append(s)
            else:
                if cur: runs.append(cur)
                cur=dict(col=k,t0=t,t1=t,n=1,hues=[h],alphas=[a],sats=[s])
        else:
            if cur: runs.append(cur); cur=None
    if cur: runs.append(cur)
    rejev=[(t,str(e)) for t,e in ev if 'REJECT' in str(e)]
    inev=[(t,str(e)) for t,e in ev if re.match(r'lane: \w+ in',str(e))]
    otherev=[(t,str(e)) for t,e in ev if 'FULL with the other' in str(e) or 'gave up' in str(e)]
    # shots
    sh=d['/Launcher/Shots']; s1=d['/Lane/Slot1']
    shots=[]
    for (t0,v0),(t1,v1) in zip(sh,sh[1:]):
        if v1>v0 and t1>=auto0:
            shots.append((t1,SLOT.get(int(at(s1,t0)),'?')))
    rej=d['/Lane/Rejecting']
    summ.append(dict(team=team,match=m,file=f,alliance=ally,
        lane_in={k:sum(1 for _,e in inev if e.startswith('lane: '+k)) for k in ['POLLEN','RED_NECTAR','BLUE_NECTAR','UNKNOWN']},
        rejects=len(rejev), shots=len(shots), shot_front={k:sum(1 for _,x in shots if x==k) for k in set(x for _,x in shots)},
        sightings=[dict(col=r['col'],own=r['col']==ally,phase=phase(r['t0']),t=round(r['t0'],2),readings=r['n'],dur=round(r['t1']-r['t0'],2),
                         hue=[round(min(r['hues'])),round(max(r['hues']))],alpha_max=round(max(r['alphas']),2)) for r in runs],
        other=otherev))
    for t,e in rejev:
        # rejecting timeline after
        seq=[(round(x-t,2),v) for x,v in rej if t-0.1<=x<=t+2.5]
        chg=[]; last=None
        for x,v in seq:
            if v!=last: chg.append((x,v)); last=v
        after=[(round(x-t,2),round(h),round(a,3),cls(h)) for x,h,a,s in rd if t-0.3<=x<=t+2.0]
        nearin=[(round(x-t,2),e2) for x,e2 in inev if t-3<=x<=t+5]
        nshots=[(round(x-t,2),fr) for x,fr in shots if t-1<=x<=t+30]
        rows.append(dict(team=team,match=m,alliance=ally,phase=phase(t),event=e,rejecting_changes=chg,readings=after,lane_in_near=nearin,shots_after=nshots))
json.dump(dict(summary=summ,rejects=rows),open(f'{S}/nectar/raw.json','w'),indent=1,default=str)
for s in summ:
    print(s['team'],s['match'],s['alliance'],'in',s['lane_in'],'rej',s['rejects'],'shots',s['shots'],s['shot_front'],s['other'])
    for r in s['sightings']: print('     ',r)
for r in rows:
    print('\nREJ',r['team'],r['match'],r['alliance'],r['phase'],r['event']); print('  rej',r['rejecting_changes']); print('  rd',r['readings']); print('  in',r['lane_in_near']); print('  shots',r['shots_after'][:6])
