import sys,json,glob
sys.path.insert(0,'/home/user/biobuzz/tools/pull-logs'); import read_logs as R
rows=[]
for team in ['20245','19429']:
  vols=json.load(open(f'newcount/{team}.json'))
  for path in sorted(glob.glob(f'bel/{team}/matches/*.gz')):
    log=path.split('/')[-1][:19]; d=R.read(path)
    ev=[(t,str(e)) for t,e in d.get('/Events',[])]
    play=next((t for t,e in ev if e.startswith('PLAY')),None)
    if play is None: continue
    full=d.get('/Lane/Full',[]); tps=d.get('/Intake/TicksPerSecond',[]); amps=d.get('/Intake/CurrentAmps',[])
    picks=[]; 
    st=None
    for t,e in ev:
        if 'FLOWER pick: start' in e: st=t
        if st and ('FLOWER pick: done' in e or 'FLOWER pick: stopped' in e): picks.append((st,t)); st=None
    vs=sorted((v['open_s'],v['new']) for v in vols if v['log']==log)
    prev=0; on=None
    for t,x in full:
        if x and not prev: on=t
        if not x and prev and on is not None:
            off=t
            inpick=any(a-0.2<=on<=b+0.5 for a,b in picks)
            # volley opening while full or within 0.5 s of clearing
            v=[n for o,n in vs if on<=o<=off+0.5]
            rows.append(dict(team=team,log=log[11:],t=round(on-play,1),dur=round(off-on,2),inpick=inpick,volley=v[0] if v else None,
                             tps_on=R.at(tps,on), auto=on-play<30.5))
        prev=x
json.dump(rows,open('recon/full.json','w'))
import collections
for team in ['20245','19429']:
    rs=[r for r in rows if r['team']==team]
    print(team,'full episodes',len(rs))
    for k in [True,False]:
        g=[r for r in rs if r['inpick']==k]
        withv=[r for r in g if r['volley'] is not None]
        print('  in FLOWER pick' if k else '  not in pick', len(g), 'ended by a volley',len(withv),'shots',collections.Counter(r['volley'] for r in withv), 'cleared with no volley',len(g)-len(withv), 'durations no volley', sorted(r['dur'] for r in g if r['volley'] is None)[:30])
