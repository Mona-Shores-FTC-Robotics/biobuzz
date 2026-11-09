import pickle
S='/tmp/claude-0/-home-user-biobuzz/fc16faa2-d150-54ec-97b5-3071dce4f144/scratchpad'
c=pickle.load(open(f'{S}/nectar/cache.pkl','rb'))
for (t,k,f),d in sorted(c.items()):
    if k!='matches': continue
    print(t,f[:19], [v for _,v in d.get('/RealMetadata/RobotDifferences',[])][-1][:300])
    h=d['/ColorSensor/HueDeg']; print('  hue n',len(h), 'dt med', sorted(b[0]-a[0] for a,b in zip(h,h[1:]))[len(h)//2], 'rej n', len(d['/Lane/Rejecting']))
    break
