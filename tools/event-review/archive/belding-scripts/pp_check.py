import sys,math,glob,statistics as st
sys.path.insert(0,'/home/user/biobuzz/tools/pull-logs')
import read_logs as R
C=141.5/2; half=25.5/2; out=14.25; drop=1.52; t=math.radians(30)
upd=out*math.cos(t)+drop*math.sin(t); dnd=out*math.cos(t)-drop*math.sin(t)
def cells(al):
    x=C-half if al=='red' else C+half
    return [(x,C+s*d) for s in (-1,1) for d in (upd,dnd)]
TF,TL=-3.33,-1.87
def rng(x,y,h,al):
    c,s=math.cos(h),math.sin(h); ax=x+TF*c-TL*s; ay=y+TF*s+TL*c
    return min(math.hypot(px-ax,py-ay) for px,py in cells(al))
def gaps(d,al,lo,hi,need_ref=None):
    out=[]
    X=d['/Odometry/PedroInches/X']; Y=d['/Odometry/PedroInches/Y']; H=d['/Odometry/PedroInches/HeadingDeg']; Rf=d['/Odometry/Referenced']
    for tt,v in d['/Launch/CameraRangeIn']:
        if not(lo<tt<hi) or v!=v: continue
        x,y,h=R.at(X,tt),R.at(Y,tt),R.at(H,tt)
        if None in (x,y,h): continue
        out.append(v-rng(x,y,math.radians(h),al))
    return out
def summ(g):
    if not g: return 'no camera samples'
    a=[abs(v) for v in g]; return f'n={len(g)} median |gap| {st.median(a):.1f} in, 90% under {sorted(a)[int(.9*len(a))-1]:.1f} in'
