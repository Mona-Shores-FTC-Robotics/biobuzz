import sys; sys.path.insert(0,'.')
from lib import *
for f in files():
    d=load(f); a,b=d['_tele']
    for t,e in ev(d,'push:|jolt:',a,b):
        ctx=[(round(t2-t,1),e2[:70]) for t2,e2 in d['/Events'] if t-3<=t2<=t+2 and not re.match('launcher|HIVE|shadow|Lane: piece|lane:|shot |slow loop|feeder',e2) and t2!=t]
        print(f[:22],round(t-a,1),e[:75]); 
        for c in ctx: print('      ',c)
