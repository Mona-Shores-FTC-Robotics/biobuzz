import pickle, re, bisect
S='/tmp/claude-0/-home-user-biobuzz/fc16faa2-d150-54ec-97b5-3071dce4f144/scratchpad'
c=pickle.load(open(f'{S}/nectar/cache.pkl','rb'))
def get(team,prefix): 
    f=[f for (t,k,f) in c if t==team and k=='matches' and f.startswith(prefix)][0]; return c[(team,'matches',f)]
def window(d,t0,t1,keys=('/Lane/Slot1','/Lane/Slot2','/Lane/Slot3','/Lane/Slot4','/Launcher/Shots','/Lane/Rejecting','/Lane/Count')):
    out=[]
    for k in keys:
        last=None
        for t,v in d[k]:
            if t0<=t<=t1 and v!=last: out.append((round(t,2),k.split('/')[-1],v))
            if t<=t1: last=v if t0<=t else last
    for t,e in d['/Events']:
        if t0<=t<=t1: out.append((round(t,2),'EV',str(e)[:110]))
    for (t,h),(_,a) in zip(d['/ColorSensor/HueDeg'],d['/ColorSensor/Alpha']):
        if t0<=t<=t1 and a>0.05: out.append((round(t,2),'col',f'{h:.0f} {a:.2f}'))
    return sorted(out)
print('--- 20245 Q1 UNKNOWN in'); [print(x) for x in window(get('20245','2026-11-07_11-01'),251.0,252.5) if x[1] in('col','EV')]
print('--- 20245 hue 300 samples')
for (t,k,f),d in c.items():
    if k!='matches': continue
    for (tt,h),(_,a) in zip(d['/ColorSensor/HueDeg'],d['/ColorSensor/Alpha']):
        if 270<h<330 and a>0.05: print(t,f[:19],round(tt,2),round(h),round(a,2))
print('--- 19429 M4 RED in'); [print(x) for x in window(get('19429','2026-11-07_16-39'),198.5,215)]
