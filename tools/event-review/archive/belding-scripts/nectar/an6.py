import pickle
S='/tmp/claude-0/-home-user-biobuzz/fc16faa2-d150-54ec-97b5-3071dce4f144/scratchpad'
c=pickle.load(open(f'{S}/nectar/cache.pkl','rb'))
d=[v for (t,k,f),v in c.items() if t=='20245' and k=='matches' and f.startswith('2026-11-07_11-55')][0]
for k in ['/Lane/Slot1','/Lane/Slot2','/Lane/Slot3','/Lane/Slot4','/Lane/Count','/Launcher/Shots']:
    prev=None; out=[]
    for t,v in d[k]:
        if v!=prev and 211<t<232: out.append((round(t,2),v))
        prev=v
    print(k,out)
print([(round(t,2),str(e)[:80]) for t,e in d['/Events'] if 211<t<232 and 'ane' in str(e)])
# Q31 rejecting / intake state context
d=[v for (t,k,f),v in c.items() if t=='20245' and k=='matches' and f.startswith('2026-11-07_15-04')][0]
print([(round(t,2),str(e)[:100]) for t,e in d['/Events'] if 154.5<t<156.5])
print([k for k in d if 'Intake' in k])
