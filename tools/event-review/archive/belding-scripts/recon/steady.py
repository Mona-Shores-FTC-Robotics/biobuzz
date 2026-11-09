import sys,json,glob,statistics as st
sys.path.insert(0,'/home/user/biobuzz/tools/pull-logs'); import read_logs as R
rows=json.load(open('recon/full.json')); cache={}
res={'true':[], 'false':[]}
for r in rows:
    if r['auto'] and r['t']<1.5: continue
    cls='true' if (r['volley'] or 0)>=3 else ('false' if r['volley'] is None and r['dur']<0.8 else None)
    if not cls: continue
    p=glob.glob(f"bel/{r['team']}/matches/*{r['log']}*.gz")[0]
    if p not in cache:
        d=R.read(p); play=next(t for t,e in d['/Events'] if str(e).startswith('PLAY')); cache[p]=(d,play)
    d,play=cache[p]; on=play+r['t']; ps=on-1.0
    a=[x for t,x in d['/ColorSensor/Alpha'] if ps+0.2<=t<=ps+0.7]
    h=[x for t,x in d['/ColorSensor/HueDeg'] if ps+0.2<=t<=ps+0.7]
    pre=[x for t,x in d.get('/Prefeed/Power',[]) if ps<=t<=on]
    cnt=R.at(d['/Lane/Count'],ps)
    res[cls].append(dict(team=r['team'],n=len(a),a_rng=(max(a)-min(a))/max(1e-6,st.mean(a)) if len(a)>1 else None,h_rng=(max(h)-min(h)) if len(h)>1 else None,
        prefeed=st.mean(pre) if pre else None,count=cnt))
for k,v in res.items():
    print(k,len(v),'samples per 0.5s',st.median([x['n'] for x in v]))
    for f in ['a_rng','h_rng','prefeed','count']:
        xs=sorted(round(x[f],2) for x in v if x[f] is not None); print("  ",f,xs)
