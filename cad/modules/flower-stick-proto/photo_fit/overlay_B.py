import numpy as np, cv2, sys
sys.argv=['x']; exec(open('pnp/solve_B.py').read().split("O=np.vstack")[0])
import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle
im=cv2.cvtColor(cv2.imread('/root/.claude/uploads/ab6cd868-9c1c-57db-97a7-bfb9362d4a20/fdf9fe1b-image.jpg'),cv2.COLOR_BGR2RGB)
O=np.vstack([obj,extra_obj]); I=np.vstack([img,extra_img])
f=1786; WEB=166.0
K,rv,tv,err=solve(f,O,I)
def rectify(z, xmin=-70, xmax=200, ymin=-125, ymax=125, s=3.0):
    xs=np.arange(xmax,xmin,-1/s); ys=np.arange(ymin,ymax,1/s)     # ahead on the left, robot-left (+y) down
    X,Y=np.meshgrid(xs,ys); P=np.stack([X,Y,np.full_like(X,z)],-1).reshape(-1,3)
    pr,_=cv2.projectPoints(P.astype(np.float64),rv,tv,K,None); pr=pr.reshape(X.shape+(2,)).astype(np.float32)
    return cv2.remap(im,pr[...,0],pr[...,1],cv2.INTER_LINEAR,borderMode=cv2.BORDER_CONSTANT), (xmax,xmin,ymax,ymin)
fig,axs=plt.subplots(1,2,figsize=(17,6.4))
LIP_MODEL=3.81*25.4; REACH=33.0
# tail end and lip measured in this photo (blade top about 13 mm above the tile)
ztile=-WEB+13.0
Pt,_,_=back(K,rv,tv,(757,897),ztile); Pt2,_,_=back(K,rv,tv,(757,1537),ztile); xt=(Pt[0]+Pt2[0])/2; lip_meas=xt+50.8
for ax,(z,title) in zip(axs,((4.0,"A. The photo as a true top view at the poker's roof height: the poker and the channel are undistorted"),(ztile,"B. The same photo as a true top view at the blade's height: the blade is undistorted"))):
    r,ext=rectify(z); ax.imshow(r,extent=(ext[0],ext[1],ext[2],ext[3]),aspect='equal')
    ax.add_patch(Rectangle((-38,-40),42.3,80,fill=False,ec='#00e5ff',lw=1.5)); ax.add_patch(Rectangle((4.3,-9.5),28.7,19,fill=False,ec='#00e5ff',lw=1.5,ls='--'))
    ax.axvline(0,color='w',lw=0.8,ls=':'); ax.text(1,-118,"channel's front lip face (x = 0)",color='w',fontsize=7,ha='right')
    ax.set_xlim(ext[0],ext[1]); ax.set_ylim(ext[2],ext[3]); ax.set_xlabel("mm ahead of the channel's lip face (the FLOWER is to the left)"); ax.set_ylabel("mm to the robot's left"); ax.set_title(title,fontsize=9)
ax=axs[1]
ax.add_patch(Rectangle((lip_meas-50.8,-100.35),50.8,200.7,fill=False,ec='#ffd400',lw=2)); ax.text(lip_meas-25,-104,f"the blade as measured: lip {lip_meas/25.4:.2f} in ahead",color='#ffd400',fontsize=8,ha='center')
ax.add_patch(Rectangle((LIP_MODEL-50.8,-100.35),50.8,200.7,fill=False,ec='#ff4d4d',lw=2,ls='--')); ax.text(LIP_MODEL-25,112,"the model's blade: lip 3.81 in ahead",color='#ff4d4d',fontsize=8,ha='center')
ax.axvline(REACH,color='#00e5ff',lw=1); ax.text(REACH-1,100,"poke tip\n(1.3 in)",color='#00e5ff',fontsize=8,ha='right')
fig.suptitle(f"Rectified with a camera pose solved from the poker's roof (corners, holes, screws; rms 1.2 px). The poke tip checks to {back(K,rv,tv,(743.3,1241.7),24.1)[0][0]:.1f} mm against a design 33.",fontsize=10)
fig.tight_layout(); fig.savefig('pnp/overlay_B.png',dpi=110); print("lip measured",lip_meas/25.4,"in; tail end x",xt)
