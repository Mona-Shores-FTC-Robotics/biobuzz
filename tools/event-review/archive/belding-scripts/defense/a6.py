import sys,os; sys.path.insert(0,'.')
from lib import *
import collections, numpy as np
for team in ('19429','20245'):
    why=collections.Counter(); fired=[]; ref=collections.Counter(); dl=[]
    for f in files(team):
        d=load(f); a,b=d['_tele']
        for t,e in ev(d,'launch spot: gate open',a,b): why[e.split('), ')[-1]]+=1
        for t,e in ev(d,'launch spot: refused',a,b): ref[re.sub(r', from.*','',e[22:])[:60]]+=1
        for t,e in ev(d,'launch spot: (let go|sticks moved)',a,b):
            m=re.search(r'fired by (\w+).*?(gate open at [\d.]+ s for ([\d.]+) s( \(cut[^)]*\))?)?.*?(\d+) shots counted',e)
            if m and 'button' in e.split('fired by')[1][:8]:
                fired.append((float(m.group(3) or 0), bool(m.group(4)), int(m.group(5)), e.split(' after')[0][13:]))
        for t,e in ev(d,'launch spot: start',a,b):
            m=re.search(r'\(nearest, (\d+) in\)',e); 
            if m: dl.append(int(m.group(1)))
    print(team,'gate-open reasons',dict(why)); print('  refusals',dict(ref))
    print('  button volleys',len(fired),'cut by end of press',sum(c for _,c,_,_ in fired),
          ' shots counted dist',collections.Counter(s for _,_,s,_ in fired), ' by end kind',collections.Counter(k for *_,k in fired))
    print('  cut volleys feed s',sorted(round(x,2) for x,c,_,_ in fired if c))
    print('  start distance to spot median',np.median(dl),'n',len(dl))
