import numpy as np, cv2, sys
src=open('pnp/solve_C2.py').read().split("for H_r_above_tile")[0]; exec(src)
import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle
im=cv2.cvtColor(cv2.imread('/root/.claude/uploads/ab6cd868-9c1c-57db-97a7-bfb9362d4a20/f31946f4-image.jpg'),cv2.COLOR_BGR2RGB)
f=1500; HR=170.0; zr=HR-14.0
K,R,C=pose_blade(f); sgn=1 if C[2]>0 else -1
P=np.array([bp(K,R,C,px,sgn*zr)[:2] for px in pi]); A=pk[:,:2]*np.array([-1,1]); Rm,t=kabsch(A,P)
tvec=-R@C
def to_img(xy,z):                    # poker-frame (x ahead, y) mm at blade-plane height z -> pixels
    b=(Rm@(np.array(xy)*np.array([-1,1])).T).T+t; X=np.c_[b,np.full(len(b),sgn*z)]; pc=(R@X.T).T+tvec; uv=(K@pc.T).T; return uv[:,:2]/uv[:,2:3]
xs=np.arange(230,-80,-0.5); ys=np.arange(-130,130,0.5); X,Y=np.meshgrid(xs,ys)
uv=to_img(np.stack([X.ravel(),Y.ravel()],1),0.0).reshape(X.shape+(2,)).astype(np.float32)
rect=cv2.remap(im,uv[...,0],uv[...,1],cv2.INTER_LINEAR)
fig,ax=plt.subplots(figsize=(11,8)); ax.imshow(rect,extent=(230,-80,130,-130),aspect='equal')
LIP_MODEL=3.81*25.4
ax.add_patch(Rectangle((LIP_MODEL-50.8,-100.35),50.8,200.7,fill=False,ec='#ff4d4d',lw=2.2,ls='--')); ax.text(LIP_MODEL-25,-104,"the model's blade (lip 3.81 in)",color='#ff4d4d',fontsize=9,ha='center')
ax.add_patch(Rectangle((-38,-40),42.3,80,fill=False,ec='#00e5ff',lw=2)); ax.add_patch(Rectangle((4.3,-9.5),28.7,19,fill=False,ec='#00e5ff',lw=2,ls='--')); ax.text(-20,-46,"the poker (roof, poke)",color='#00e5ff',fontsize=9,ha='center')
ax.axvline(0,color='w',lw=0.8,ls=':'); ax.text(1,125,"channel's front lip face (x = 0)",color='w',fontsize=8,ha='right')
lipx=np.mean([-(Rm.T@(np.array([0,y])-t))[0] for y in (0,200.7)]); ax.axvline(lipx,color='#ffd400',lw=1.5); ax.text(lipx+2,-118,f"the real lip: {lipx/25.4:.2f} in ahead",color='#ffd400',fontsize=9,ha='left')
ax.set_xlabel("mm ahead of the channel's lip face (the FLOWER is to the left)"); ax.set_ylabel("mm to the robot's left")
ax.set_title("The ring photo as a true top view at the blade's height (camera solved from the blade's own ends: f 1500 px). The poker and the FLOWER stand higher, so they look stretched; the outlines are where they truly are.",fontsize=8)
fig.tight_layout(); fig.savefig('pnp/overlay_C.png',dpi=110); print('lip',lipx/25.4)
