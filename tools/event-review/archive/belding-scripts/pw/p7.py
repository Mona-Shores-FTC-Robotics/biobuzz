import sys, glob, os, re, statistics as st, collections as C
sys.path.insert(0, sys.argv[1]); import read_logs as R
root = sys.argv[2]
for team in ('19429','20245'):
    ends=C.Counter(); shots_by=C.Counter(); gate_after_press=[]; gate_after_arr=[]; why=C.Counter()
    early_held=[]; arrive_t=[]; cut=0; spot_shots=0; fire_presses=C.Counter(); fire_shots=0; tot_shots=0
    sticks_early_held=[]
    for p in sorted(glob.glob(f'{root}/{team}/matches/*.gz')):
        d=R.read(p); m=d['/DriverStation/RobotMode']; te=next(t for t,v in m if v=='teleop')
        sh=d['/Launcher/Shots']; s0=next((v for t,v in sh if t>=te),0); tot_shots+=max(v for t,v in sh)-s0
        for t,e in d['/Events']:
            if t<te: continue
            if e.startswith('launch spot: arrived'):
                arrive_t.append(float(re.search(r'in ([\d.]+) s',e).group(1)))
            if e.startswith('launch spot: gate open'):
                mm=re.search(r'open ([\d.]+) s after the press \(([\d.]+) s after arriving\), (.*)',e)
                gate_after_press.append(float(mm.group(1))); gate_after_arr.append(float(mm.group(2))); why[mm.group(3)]+=1
            mm=re.match(r'launch spot: (let go early|let go|sticks moved early|sticks moved|refused)',e)
            if mm:
                k=mm.group(1); ends[k]+=1
                n=re.search(r'(\d+) shots counted',e); n=int(n.group(1)) if n else 0
                shots_by[k]+=n; spot_shots+=n
                if 'cut by the end of the press' in e: cut+=1
                h=re.search(r'early after ([\d.]+) s',e)
                if h and k=='let go early': early_held.append(float(h.group(1)))
                if h and k=='sticks moved early': sticks_early_held.append(float(h.group(1)))
            if e.startswith('fire:'):
                r=e.split('→')[-1].strip(); fire_presses[r.split(';')[0][:40]]+=1
    print(f'\n===== {team}  teleop shots counted (all) {tot_shots:.0f}, by launch-spot button {spot_shots}')
    print('launch-spot press endings:', dict(ends))
    print('  shots counted by ending:', dict(shots_by), ' volleys cut short by releasing:', cut)
    md=lambda xs: f'median {st.median(xs):.2f}s (n={len(xs)})' if xs else '-'
    print('  press→arrive', md(arrive_t), '| press→gate', md(gate_after_press), '| arrive→gate', md(gate_after_arr))
    print('  gate opened because:', dict(why))
    print('  "let go early" held', md(early_held), '| "sticks moved early" held', md(sticks_early_held))
    print('FIRE presses outcome:', dict(fire_presses))
