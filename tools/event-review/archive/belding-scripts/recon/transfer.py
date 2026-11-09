import sys,glob,statistics as st,collections
sys.path.insert(0,'/home/user/biobuzz/tools/pull-logs'); import read_logs as R
for team in ['20245','19429']:
    by=collections.defaultdict(list); gaps=[]; allp=[]
    for p in sorted(glob.glob(f'bel/{team}/matches/*.gz')):
        d=R.read(p); pres=d['/Lane/Present']; cnt=d['/Lane/Count']; full=d['/Lane/Full']; tps=d['/Intake/TicksPerSecond']
        ev=[(t,str(e)) for t,e in d['/Events']]
        shots=[t for t,e in ev if 'lane: shot' in e]
        on=None; prev=False; segs=[]
        for t,x in pres:
            if x and not prev: on=t
            if not x and prev and on is not None: segs.append((on,t))
            prev=x
        for i,(a,b) in enumerate(segs):
            dw=b-a
            if dw>2.5: continue  # parked (full lane) - not a transfer
            if any(a<=s<=b+0.3 for s in shots): continue  # cleared by launching
            c=R.at(cnt,a-0.05) or 0
            by[int(c)].append(dw); allp.append(dw)
            if i+1<len(segs): gaps.append(segs[i+1][0]-b)
    print(team,'passing pieces',len(allp),'median dwell %.2f s'%st.median(allp),'share over 0.6 s %.0f%%'%(100*sum(x>0.6 for x in allp)/len(allp)),'over 1.0 s %.0f%%'%(100*sum(x>1.0 for x in allp)/len(allp)))
    for c in sorted(by): print('   lane already had',c,'n',len(by[c]),'median %.2f'%st.median(by[c]),'p75 %.2f'%sorted(by[c])[3*len(by[c])//4])
    q=[g for g in gaps if g<0.3]; print('   next piece arrived <0.3 s after the last cleared:',len(q),'of',len(gaps))
