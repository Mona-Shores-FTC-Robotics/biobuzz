"""The pictures in doc/corner-wheels/ and the clearances in doc/corner-wheels.md, from the AdvantageScope model of the
Onshape CAD (cad/advantagescope/Robot_BIOBUZZ) and this folder's parts.

    pip install cadquery trimesh shapely matplotlib scipy
    python3 cad/corner-wheels/parts.py && python3 cad/corner-wheels/drawings.py     # a few minutes

Robot frame, inches: +X forward, +Y left, +Z up, origin on the floor under the middle of the drive axles. The FLOWER
stick is swept 0..162 deg in 1 deg steps about its axis from components.json.
"""
import os, sys
import numpy as np
import math
from matplotlib.collections import PolyCollection
HERE = os.path.dirname(os.path.abspath(__file__))
OUTDIR = os.path.join(HERE, "..", "..", "doc", "corner-wheels")
sys.path.insert(0, HERE)
import trimesh, numpy as np
IN=0.0254
R = os.path.join(HERE, "..", "advantagescope", "Robot_BIOBUZZ") + "/"
def parts(stick_deg=None):
    out=[]
    s=trimesh.load(R+"model.glb")
    for node in s.graph.nodes_geometry:
        T,g=s.graph[node]; m=s.geometry[g].copy(); m.apply_transform(T); m.apply_scale(1/IN); out.append((node,m,"fixed"))
    if stick_deg is not None:
        s=trimesh.load(R+"model_1.glb")
        ax=np.array([0.15449,0.13883,0.18462]); d=np.array([0.00107,1,0]); d/=np.linalg.norm(d)
        Rm=trimesh.transformations.rotation_matrix(np.radians(stick_deg),d,ax)
        for node in s.graph.nodes_geometry:
            T,g=s.graph[node]; m=s.geometry[g].copy(); m.apply_transform(T); m.apply_transform(Rm); m.apply_scale(1/IN); out.append((node,m,"stick"))
    s=trimesh.load(R+"model_0.glb")
    for node in s.graph.nodes_geometry:
        T,g=s.graph[node]; m=s.geometry[g].copy(); m.apply_transform(T); m.apply_scale(1/IN); out.append((node,m,"turret"))
    return out

# ---- the stick's swing
s=trimesh.load(R+"model_1.glb")
stick=np.vstack([ (lambda m:(m.apply_transform(T),m.vertices)[1])(s.geometry[g].copy()) for T,g in [s.graph[n] for n in s.graph.nodes_geometry]])
ax=np.array([0.15449,0.13883,0.18462]); d=np.array([0.00107,1,0]); d/=np.linalg.norm(d)
sw=[]
for a in range(0,163,1):
    Rm=trimesh.transformations.rotation_matrix(np.radians(a),d,ax)
    sw.append((stick@Rm[:3,:3].T+Rm[:3,3])/IN)
sw=np.vstack(sw)
fixed=np.vstack([m.vertices for n,m,k in parts(None) if k=="fixed"])


# ---- clearances
import parts as PP
from scipy.spatial import cKDTree
TS, TF = cKDTree(sw), cKDTree(fixed)
D = HERE + "/"
for side in ("left", "right"):
    c, (x0, y0, z0), s = PP.placed(side)
    t = trimesh.load(D + f"bracket_{side}.stl")
    P = np.vstack([t.vertices, t.sample(30000)]) / 25.4 + np.array([x0, y0, z0])
    print(side, "bracket: stick", TS.query(P)[0].min().round(2), "frame", TF.query(P)[0].min().round(2))
    ax = np.array(c["axle"]) * np.array([1, s]); off = (40.6 / 2 - 10) / 25.4
    cx = ax[0] - off if c["body"] == "back" else ax[0] + off
    g = np.mgrid[-0.8:0.8:17j, -0.4:0.4:9j, 2.34:3.4:9j].reshape(3, -1).T
    S = np.c_[cx + g[:, 0], ax[1] + g[:, 1], g[:, 2]]
    print(side, "servo: stick", TS.query(S)[0].min().round(2), "frame", TF.query(S)[0].min().round(2))
    W = trimesh.load(D + f"flap_wheel_{side}.stl"); Q = np.vstack([W.vertices, W.sample(20000)]) / 25.4
    Q = Q + np.array([ax[0], ax[1], 0.60])
    print(side, "wheel: stick", TS.query(Q)[0].min().round(2), "frame", TF.query(Q)[0].min().round(2))

# ---- top views
import sys, numpy as np, trimesh, matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from shapely.geometry import Polygon, Point, box
from shapely.ops import unary_union


IN=25.4
def proj(tris, z0, z1):
    zmn=tris[:,:,2].min(1); zmx=tris[:,:,2].max(1)
    t=tris[(zmx>z0)&(zmn<z1)]
    polys=[Polygon(tt[:,:2]) for tt in t]
    polys=[p.buffer(0.005) for p in polys if p.area>1e-7]
    return unary_union(polys) if polys else None
P=parts(162)
fixed_ball=unary_union([g for g in (proj(m.triangles,0.1,2.9) for n,m,k in P if k=="fixed") if g])
fixed_above=unary_union([g for g in (proj(m.triangles,2.9,10) for n,m,k in P if k=="fixed") if g])
stick_ball=unary_union([g for g in (proj(m.triangles,0.0,2.9) for n,m,k in P if k=="stick") if g])

def draw(side, fn):
    c,(x0,y0,z0),s=PP.placed(side)
    fig,ax=plt.subplots(figsize=(8,7))
    def fill(g,**kw):
        if g is None or g.is_empty: return
        for poly in getattr(g,"geoms",[g]):
            if poly.geom_type!="Polygon": continue
            x,y=poly.exterior.xy; ax.fill(x,y,**kw)
    win=box(5.5,(2.8 if s>0 else -9.5),13.6,(9.5 if s>0 else -2.8))
    fill(fixed_above.intersection(win),color="#d9d9d9",lw=0,zorder=1)
    fill(fixed_ball.intersection(win),color="#7a7a7a",lw=0,zorder=2)
    # stick sweep at ball height in the arm band (z<2.9) and above (z<4)
    for zt,col,a in ((4.0,"#f4b183",0.35),(2.9,"#e07b39",0.55)):
        q=sw[(sw[:,2]<zt)&(sw[:,0]>5.5)]
        q=q[(q[:,1]*s>4)]
        if len(q):
            from shapely.geometry import MultiPoint
            g=unary_union([Point(p).buffer(0.06) for p in q[::7,:2]])
            fill(g.intersection(win),color=col,alpha=a,lw=0,zorder=3)
    fill(stick_ball.intersection(win),color="#c0504d",lw=0,zorder=4)
    # bracket
    t=trimesh.load(D+f"bracket_{side}.stl")
    bt=t.triangles/IN+np.array([x0,y0,z0])
    fill(proj(bt,-1,10),color="#4f81bd",alpha=0.8,lw=0,zorder=6)
    axl=np.array(c["axle"])*np.array([1,s]); off=(40.6/2-10)/IN
    cx=axl[0]-off if c["body"]=="back" else axl[0]+off
    ax.add_patch(plt.Rectangle((cx-0.8,axl[1]-0.4),1.6,0.8,fill=False,ec="#1f3864",lw=1.4,ls="--",zorder=7))
    W=trimesh.load(D+f"flap_wheel_{side}.stl"); wt=W.triangles/IN+np.array([axl[0],axl[1],0.6])
    fill(proj(wt,0.5,1.7),color="#2e7d32",alpha=0.9,lw=0,zorder=8)
    ax.plot(*axl,"k+",ms=10,zorder=9)
    # POLLEN: one pinned at the corner against a wall 0.3 in ahead of the wheel's front; one in the mouth
    wall_x=axl[0]+1.25+2.8+0.0
    for (bx,by),lab in (((axl[0]+1.25+1.4, axl[1]+s*0.9),"POLLEN at a wall"),((9.95, s*3.4),"raked POLLEN,\nahead of the roller")):
        ax.add_patch(plt.Circle((bx,by),1.4,fill=False,ec="#bf9000",lw=1.6,zorder=10))
        ax.text(bx,by,lab,fontsize=6.5,ha="center",va="center",color="#7f6000",zorder=11)
    ax.axvline(wall_x,color="k",lw=2.5,zorder=5); ax.text(wall_x+0.08,s*3.0,"wall (nose-in)",rotation=90,fontsize=7,va="bottom" if s>0 else "top")
    edge=7.63 if s>0 else -7.77
    ax.axhline(edge,color="k",ls=":",lw=1); ax.text(5.6,edge+s*0.08,"frame side",fontsize=7,va="bottom" if s>0 else "top")
    ax.axvline(7.57,color="k",ls=":",lw=1); ax.text(7.6,s*9.3,"frame front (drive wheels) X 7.57",fontsize=7,va="center")
    ax.axvline(10.43,color="#7030a0",ls="--",lw=1); ax.text(10.46,s*9.3,"18 in cube front\n(from back at X -7.57)",fontsize=6.5,color="#7030a0",va="center")
    ax.axhline(s*9.0,color="#7030a0",ls="--",lw=1)
    # arrow: spin
    ang=np.linspace(0.3,1.3,20)*s
    ax.annotate("",xy=(axl[0]+1.55,axl[1]-s*0.5),xytext=(axl[0]+1.2,axl[1]+s*1.1),arrowprops=dict(arrowstyle="->",color="#2e7d32",lw=1.5,connectionstyle=f"arc3,rad={-0.4*s}"),zorder=12)
    ax.set_xlim(5.5,13.6); ax.set_ylim((2.8,9.5) if s>0 else (-9.5,-2.8)); ax.set_aspect("equal")
    ax.set_xlabel("X forward (in)"); ax.set_ylabel("Y left (in)")
    ax.grid(True,lw=0.3,alpha=0.5)
    ax.set_title(f"{side.upper()} front corner, top view, to scale (CAD of 31 Oct)\n"
                 f"2.5 in TPU flap wheel, axle ({axl[0]:.2f}, {axl[1]:+.2f}), wheel Z 0.60..1.65",fontsize=9)
    from matplotlib.patches import Patch
    hs=[Patch(color="#7a7a7a",label="robot at ball height (Z<2.9)"),Patch(color="#d9d9d9",label="robot above ball height"),
        Patch(color="#c0504d",label="FLOWER stick down (Z<2.9)"),Patch(color="#e07b39",alpha=0.55,label="stick swing, Z<2.9"),
        Patch(color="#f4b183",alpha=0.35,label="stick swing, Z<4.0"),Patch(color="#4f81bd",label="bracket (Z 2.95..3.65)"),
        Patch(fill=False,ec="#1f3864",ls="--",label="servo body (Z 2.34..3.40)"),Patch(color="#2e7d32",label="corner wheel")]
    ax.legend(handles=hs,fontsize=6.5,loc="lower right" if s>0 else "upper right",framealpha=0.9)
    fig.tight_layout(); fig.savefig(fn,dpi=110); plt.close(fig)
import os
os.makedirs(OUTDIR, exist_ok=True)
draw("left",os.path.join(OUTDIR, "left-corner-top.png"))
draw("right",os.path.join(OUTDIR, "right-corner-top.png"))

# ---- side view
fig,ax=plt.subplots(figsize=(9,7))
P0=parts(0); P1=parts(162)
def pc(tris,col,a=1,z=1):
    ax.add_collection(PolyCollection(tris[:,:,[0,2]],facecolors=col,edgecolors="none",alpha=a,zorder=z))
for n,m,k in P0:
    if k=="fixed": pc(m.triangles,"#bdbdbd",1,1)
    if k=="turret": pc(m.triangles,"#8e8e8e",1,2)
    if "Limelight" in n: pc(m.triangles,"#2f5597",1,6)
    if k=="stick": pc(m.triangles,"#c0504d",0.35,3)
for n,m,k in P1:
    if k=="stick": pc(m.triangles,"#c0504d",0.8,3)
for n,m,k in P0:
    if "Limelight" in n: pc(m.triangles,"#2f5597",1,6)
# camera view wedge: lens at (4.40, 14.84), pitch 45 up, vertical FOV 42 -> 24..66 deg
L=(4.40,14.84)
for a in (24,66):
    ax.plot([L[0],L[0]+12*math.cos(math.radians(a))],[L[1],L[1]+12*math.sin(math.radians(a))],color="#2f5597",lw=1)
ax.fill([L[0],L[0]+12*math.cos(math.radians(24)),L[0]+12*math.cos(math.radians(66))],[L[1],L[1]+12*math.sin(math.radians(24)),L[1]+12*math.sin(math.radians(66))],color="#2f5597",alpha=0.12)
ax.text(10.6,19.3,"Limelight view\n(45° up, 42° tall:\n24°..66° above level)",color="#2f5597",fontsize=8)
# centre stick from the tower
piv=(2.45,8.0); tip=(12.6,0.5)
Lr=math.hypot(tip[0]-piv[0],tip[1]-piv[1])
ax.plot(*zip(piv,tip),color="#2e7d32",lw=5,solid_capstyle="round",zorder=7)
ax.plot(*piv,"ko",zorder=8)
up=(piv[0],piv[1]+Lr)
ax.plot(*zip(piv,up),color="#2e7d32",lw=3,ls=":",zorder=7)
ang=math.radians(180-49)
back=(piv[0]+Lr*math.cos(ang),piv[1]+Lr*math.sin(ang))
ax.plot(*zip(piv,back),color="#2e7d32",lw=3,ls="--",zorder=7)
ax.text(10.9,6.6,f"centre stick down: pivot on\nthe tower (X {piv[0]}, Z {piv[1]}), {Lr:.1f} in\nto today's ramp tip. At Y 0 it\ncrosses a servo block, the intake\nmotor and the intake roller", color="#2e7d32",fontsize=7.5,bbox=dict(fc="white",ec="none",alpha=0.85))
ax.text(up[0]+0.3,up[1]-1.2,f"stowed straight up: Z {up[1]:.1f}\n(over the 18 in cube, R102)",color="#2e7d32",fontsize=8)
ax.text(-8.8,20.6,"stowed back under 18 in:\nthrough the turret",color="#2e7d32",fontsize=8)
# start cube
ax.add_patch(plt.Rectangle((-7.57,0),18,18,fill=False,ec="#7030a0",ls="--",lw=1))
ax.text(-7.4,18.25,"18 in starting cube (R102), back at X -7.57",color="#7030a0",fontsize=8)
ax.axhline(0,color="k",lw=1)
ax.add_patch(plt.Circle((14.2,1.4),1.4,fill=False,ec="#bf9000",lw=1.5)); ax.text(13.2,3.1,"POLLEN",fontsize=7,color="#7f6000")
ax.set_xlim(-9,17.5); ax.set_ylim(-0.5,23.5); ax.set_aspect("equal"); ax.grid(True,lw=0.3,alpha=0.5)
ax.set_xlabel("X forward (in)"); ax.set_ylabel("Z up (in)")
ax.set_title("Side view, to scale (CAD of 31 Oct): today's corner stick (red, faint stowed, solid down)\nand a centre stick off the Limelight tower (green)",fontsize=9)
fig.tight_layout(); fig.savefig(os.path.join(OUTDIR, "centre-stick-side.png"),dpi=110)
