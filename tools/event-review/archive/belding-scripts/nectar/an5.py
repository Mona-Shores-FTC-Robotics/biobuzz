import pickle
S='/tmp/claude-0/-home-user-biobuzz/fc16faa2-d150-54ec-97b5-3071dce4f144/scratchpad'
c=pickle.load(open(f'{S}/nectar/cache.pkl','rb'))
d=[v for (t,k,f),v in c.items() if t=='19429' and f.startswith('2026-11-07_14-41')][0]
print([(round(t,2),str(e)[:100]) for t,e in d['/Events'] if 125<t<134])
print([(round(t,2),round(h),round(a,2)) for (t,h),(_,a) in zip(d['/ColorSensor/HueDeg'],d['/ColorSensor/Alpha']) if 126<t<133.3 and a>0.05])
print('Rejecting', sorted(set(v for t,v in d['/Lane/Rejecting'])))
for (t,k,f),v in sorted(c.items()):
    if k!='practice': continue
    al=[str(e) for _,e in v.get('/Events',[]) if 'PLAY:' in str(e) and 'alliance' in str(e)]
    ev=[(round(tt,1),str(e)[:95]) for tt,e in v.get('/Events',[]) if 'NECTAR' in str(e) or 'REJECT' in str(e)]
    if ev: print(t,f[:30],al[-1:] ,ev)
