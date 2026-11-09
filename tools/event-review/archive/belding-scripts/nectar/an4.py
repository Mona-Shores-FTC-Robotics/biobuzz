import pickle
S='/tmp/claude-0/-home-user-biobuzz/fc16faa2-d150-54ec-97b5-3071dce4f144/scratchpad'
c=pickle.load(open(f'{S}/nectar/cache.pkl','rb'))
def get(team,prefix):
    f=[f for (t,k,f) in c if t==team and k=='matches' and f.startswith(prefix)][0]; return c[(team,'matches',f)]
d=get('20245','2026-11-07_11-01')
for k in ['Red','Green','Blue','Alpha','HueDeg','Saturation']:
    print(k,[ (round(t,2),round(v,3)) for t,v in d['/ColorSensor/'+k] if 251.5<t<251.9])
# shots and slot1 near the UNK
sh=d['/Launcher/Shots']
prev=None
for t,v in sh:
    if v!=prev and 245<t<300: print('shot',round(t,2),v)
    prev=v
for k in ['/Lane/Slot1','/Lane/Slot2','/Lane/Slot3']:
    prev=None
    for t,v in d[k]:
        if v!=prev and 251<t<300: print(k,round(t,2),v)
        prev=v
print([ (round(t,2),e) for t,e in d['/Events'] if 251<t<300 and ('lane' in str(e).lower() or 'fire' in str(e))][:40])
d=get('19429','2026-11-07_16-39')
prev=None
for t,v in d['/Launcher/Shots']:
    if v!=prev and t>195: print('M4 shot',round(t,2),v)
    prev=v
print('M4 modes', d['/DriverStation/RobotMode'], 'last t', d['/Lane/Slot1'][-1])
print([ (round(t,2),str(e)[:90]) for t,e in d['/Events'] if t>199.5 and ('lane' in str(e).lower() or 'Lane' in str(e))])
