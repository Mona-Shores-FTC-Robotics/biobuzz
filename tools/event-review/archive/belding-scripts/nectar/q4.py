import pickle
S='/tmp/claude-0/-home-user-biobuzz/fc16faa2-d150-54ec-97b5-3071dce4f144/scratchpad'
c=pickle.load(open(f'{S}/nectar/cache.pkl','rb'))
d=c[('20245','matches','2026-11-07_15-04-17_R-Solo_11-05_8_09PM.wpilog.gz')]
for k in ['/ColorSensor/HueDeg','/ColorSensor/Alpha','/ColorSensor/Saturation','/Lane/Rejecting','/Launcher/Shots','/Lane/Slot1']:
    print(k, d[k][:3], len(d[k]))
h=d['/ColorSensor/HueDeg']; a=d['/ColorSensor/Alpha']
print(sum(1 for x,y in zip(h,a) if abs(x[0]-y[0])>1e-6))
