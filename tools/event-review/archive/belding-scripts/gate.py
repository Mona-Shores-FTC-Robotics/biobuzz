import sys,glob,json,statistics as st
sys.path.insert(0,'/home/user/biobuzz/tools/pull-logs')
import read_logs as R
def edges(s):
    out=[];prev=0
    for t,v in s:
        if v and not prev: out.append(t)
        prev=v
    return out
rows=[]
for team in ('19429','20245'):
  for f in sorted(glob.glob(f'bel/{team}/matches/*.gz')):
    d=R.read(f); key=f.split('/')[-1][:19]
    bd=[t for t,v in d['/Events'] if str(v).startswith('OpMode init: Basic Drive')]
    tele=bd[0] if bd else 1e9
    shots=d['/Launcher/Shots']; feed=d['/FeedGate/Feeding']
    inc=[t for (t0,a),(t,b) in zip(shots,shots[1:]) if b>a]
    for g in edges(feed):
        # end of this feeding period
        end=next((t for t,v in feed if t>g and not v),g+5)
        sh=[t for t in inc if g<=t<=end+0.3]
        if not sh: rows.append(dict(team=team,match=key,phase='auto' if g<tele else 'tele',open=g,first=None,n=0)); continue
        gaps=[b-a for a,b in zip(sh,sh[1:])]
        rows.append(dict(team=team,match=key,phase='auto' if g<tele else 'tele',open=round(g,3),first=round(sh[0]-g,3),n=len(sh),gaps=[round(x,3) for x in gaps],
           lane=R.at(d['/Lane/Count'],g-0.02),feeder_pw=R.at(d['/Feeder/Power'],g-0.02),feeder_pw_after=R.at(d['/Feeder/Power'],g+0.05),
           atspeed=R.at(d['/Launcher/AtSpeed'],g),gatepos_before=R.at(d['/FeedGate/Position'],g-0.05),gatepos_after=R.at(d['/FeedGate/Position'],g+0.2),
           slot1=R.at(d['/Lane/Slot1'],g-0.02),present=R.at(d['/Lane/Present'],g-0.02)))
json.dump(rows,open('gate_rows.json','w'),indent=0)
for team in ('19429','20245'):
  r=[x for x in rows if x['team']==team and x['first'] is not None]
  f=sorted(x['first'] for x in r); g=sorted(y for x in r for y in x['gaps'])
  print(team,'volleys',len(r),'no shot',sum(1 for x in rows if x['team']==team and x['first'] is None))
  q=lambda a,p:a[min(len(a)-1,int(p*len(a)))]
  print('  gate->1st: median %.2f p10 %.2f p90 %.2f max %.2f'%(st.median(f),q(f,.1),q(f,.9),f[-1]))
  print('  ball-to-ball: median %.2f p10 %.2f p90 %.2f'%(st.median(g),q(g,.1),q(g,.9)))
