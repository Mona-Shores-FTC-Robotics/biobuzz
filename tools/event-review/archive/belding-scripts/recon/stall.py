import sys,json,glob,statistics as st
sys.path.insert(0,'/home/user/biobuzz/tools/pull-logs'); import read_logs as R
rows=json.load(open('recon/full.json'))
out={'true':[], 'false':[]}
cache={}
for r in rows:
    if r['auto'] and r['t']<1.5: continue  # preload sitting at sensor
    cls='true' if (r['volley'] or 0)>=3 else ('false' if r['volley'] is None and r['dur']<0.8 else None)
    if not cls: continue
    p=glob.glob(f"bel/{r['team']}/matches/*{r['log']}*.gz")[0]
    if p not in cache:
        d=R.read(p); play=next(t for t,e in d['/Events'] if str(e).startswith('PLAY')); cache[p]=(d,play)
    d,play=cache[p]; on=play+r['t']
    tps=d['/Intake/TicksPerSecond']; amps=d.get('/Intake/CurrentAmps',[])
    pres=d['/Lane/Present']
    # presence start = last rising edge before on
    ps=max([t for (t,x),(t0,x0) in zip(pres[1:],pres) if x and not x0 and t<=on] or [on-1])
    def win(s,a,b): 
        v=[x for t,x in s if a<=t<=b and isinstance(x,(int,float))]; return st.median(v) if v else None
    out[cls].append(dict(team=r['team'], tps_0_3=win(tps,ps+0.0,ps+0.3), tps_3_6=win(tps,ps+0.3,ps+0.6), tps_6_10=win(tps,ps+0.6,ps+1.0), amps_3_6=win(amps,ps+0.3,ps+0.6), amps_6_10=win(amps,ps+0.6,ps+1.0)))
for k,v in out.items():
    print(k,len(v))
    for f in ['tps_0_3','tps_3_6','tps_6_10','amps_3_6','amps_6_10']:
        xs=sorted(x[f] for x in v if x[f] is not None)
        if xs: print(f"  {f}: median {st.median(xs):.0f}  p10 {xs[len(xs)//10]:.0f}  p90 {xs[(9*len(xs))//10]:.0f}")
    for team in ['20245','19429']:
        xs=sorted(x['tps_3_6'] for x in v if x['team']==team and x['tps_3_6'] is not None)
        print('   ',team,'tps 0.3-0.6s', [round(a) for a in xs])
