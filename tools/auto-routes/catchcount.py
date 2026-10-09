"""catchcount.py <dir> <runs>: TIP 1's spill against robot 1 standing through it (sister5.py right_catch, doc/five-tip-flow-through.md):
pieces spilled, the most robot 1 held in the 6 s after, floor pieces (z < 4 in) gained from the rocker moving to 3 s after
the TIP near a catcher's face (x 43-70.75, y 18-42), at our right end, on the other half and under the HIVE, and robot 1's G409 touches."""
import sys, statistics as st, re

import wpilog, spilltrack as s
d,runs=sys.argv[1],int(sys.argv[2])
cols={'spilled':[],'intaken':[],'ours':[],'theirs':[],'hive':[],'g409':[],'full_by':[],'near':[]}
for i in range(1,runs+1):
    f=f"{d}/{i}.wpilog"
    ev=[(t,p.decode()) for t,n,typ,p in wpilog.read(f) if n=='/Events']
    tip=next(t for t,e in ev if 'tipped: LEFT_CELL_UP' in e)
    move=max(t for t,e in ev if 'the rocker moves' in e and t<=tip)
    cols['spilled'].append(sum(1 for t,e in ev if 'spill:' in e and tip-0.2<=t<=tip+1))
    held=[(t,int.from_bytes(p,'little',signed=True)) for t,n,typ,p in wpilog.read(f) if n=='/Sim/Robot/Held']
    cols['intaken'].append(max([h for t,h in held if tip-1<=t<=tip+6] or [0]))
    cols['g409'].append(sum(1 for t,e in ev if 'G409' in e and 'robot 1' in e and move<=t<=tip+4))
    full=[t for t,h in held if h>=4 and tip-1<=t<=tip+6]
    cols['full_by'].append(full[0]-tip if full else 9)
    snaps,_=s.load(f)
    a=[x for x in snaps if x[0]<=move][-1][1]; b=[x for x in snaps if x[0]<=tip+3][-1][1]
    fl=lambda S_:[q for q in S_ if q[2]<4]
    R={'near':lambda q:43<=q[0]<=70.75 and 18<=q[1]<=42,'ours':lambda q:q[0]<=70.75 and q[1]<51,'theirs':lambda q:q[0]>70.75 and q[1]<51,'hive':lambda q:51<=q[1]<=90}
    for k,g in R.items(): cols[k].append(sum(map(g,fl(b)))-sum(map(g,fl(a))))
print(' '.join(f"{k} {st.mean(v):.1f}" for k,v in cols.items()), '| G409 runs', sum(1 for v in cols['g409'] if v), '/', runs)
