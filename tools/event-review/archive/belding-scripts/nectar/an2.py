import pickle, re, bisect, collections
S='/tmp/claude-0/-home-user-biobuzz/fc16faa2-d150-54ec-97b5-3071dce4f144/scratchpad'
c=pickle.load(open(f'{S}/nectar/cache.pkl','rb'))
def files(team): return sorted(f for (t,k,f) in c if t==team and k=='matches')
for team in ('19429','20245'):
    hist=collections.Counter(); dup=0; tot=0
    for f in files(team):
        d=c[(team,'matches',f)]
        auto0=[t for t,v in d['/DriverStation/RobotMode'] if v=='autonomous'][0]
        H=d['/ColorSensor/HueDeg']; A=d['/ColorSensor/Alpha']; Sa=d['/ColorSensor/Saturation']
        prev=None
        for (t,h),(_,a),(_,s) in zip(H,A,Sa):
            if t<auto0: continue
            if a>0.10: hist[int(h//5)*5]+=1
            cur=(h,a,s)
            if prev is not None and a>0.10:
                tot+=1; dup+= cur==prev
            prev=cur
    print(team,'identical-to-previous samples (alpha>0.10):',dup,'/',tot)
    print('  hue histogram (alpha>0.10), 5° bins:', ' '.join(f'{k}:{v}' for k,v in sorted(hist.items())))
