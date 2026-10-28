import numpy as np, cv2
W,H=1512,2016; cx,cy=W/2,H/2
# the blade, seen whole: X_b = mm behind the lip, Y_b = mm along its length (top end 0); corners in the photo
bobj=np.array([[0,0,0],[50.8,0,0],[0,200.7,0],[50.8,200.7,0]],float)
bimg=np.array([[597,800],[706,800],[617,1225],[729,1225]],float)
# the poker: (x ahead of the lip face, y, z above the web top) and the photo's pixels
pk=np.array([[4.3,-40,4],[4.3,40,4],[-24,-24,4],[-24,24,4],[-8,-32,4],[-8,32,4]],float)
pi=np.array([[872,945],[885.8,1166.7],[950,984],[961,1119],[905.5,966],[917.5,1142.5]],float)
tip=np.array([[33,-9.5,24.1],[33,9.5,24.1]]); tipi=np.array([[798,1034],[801,1084]],float)
def pose_blade(f):
    K=np.array([[f,0,cx],[0,f,cy],[0,0,1]],float)
    ok,rv,tv=cv2.solvePnP(bobj,bimg,K,None,flags=cv2.SOLVEPNP_IPPE); R,_=cv2.Rodrigues(rv); C=-R.T@tv.ravel()
    return K,R,C
def bp(K,R,C,px,z):
    d=R.T@np.linalg.inv(K)@np.array([px[0],px[1],1.0]); s=(z-C[2])/d[2]; return C+s*d
def kabsch(A,B):   # rigid 2D fit B ~ R A + t
    ca,cb=A.mean(0),B.mean(0); U,S,Vt=np.linalg.svd((A-ca).T@(B-cb)); d=np.sign(np.linalg.det(Vt.T@U.T)); D=np.diag([1,d]); Rm=Vt.T@D@U.T; t=cb-Rm@ca; return Rm,t
for H_r_above_tile in (150.0,170.0,190.0):          # the roof top's height above the tile (web 5.9 to 7.5 in plus 4 mm)
  print(f"--- roof top {H_r_above_tile/25.4:.2f} in above the tile")
  zr=H_r_above_tile-14.0                              # above the blade's top plane
  for f in (900,1100,1300,1500,1800,2200,2800):
    K,R,C=pose_blade(f)
    if C[2]*np.sign(R.T[2,2])==0: pass
    # the blade-plane frame has z toward the camera if C[2]>0
    sgn=1 if C[2]>0 else -1
    P=np.array([bp(K,R,C,px,sgn*zr)[:2] for px in pi])          # the roof points, back-projected into the blade's plane coords
    A=pk[:,:2]*np.array([-1,1]); Rm,t=kabsch(A,P); fit=(Rm@A.T).T+t; rms=np.sqrt(((fit-P)**2).sum(1).mean())
    # the poker frame -> blade frame; the lip is the line X_b=0 (blade coords: lip at X_b=0 and tail at 50.8). Express the lip's points in the poker frame
    # blade coords (X_b, Y_b) are the plane coords themselves; the poker origin (the lip face x=0,y=0) maps to Rm@(0,0)+t
    Rinv=Rm.T
    lip_pts=np.array([[0,100.35],[0,0],[0,200.7]]); lip_in_poker=(Rinv@(lip_pts-t).T).T
    tipP=np.array([bp(K,R,C,px,sgn*(24.1+H_r_above_tile-4-14))[:2] for px in tipi]); tip_in_poker=(Rinv@(tipP-t).T).T
    print(f"f={f}: camera {C[2]*sgn:.0f} mm above the blade plane | poker fit rms {rms:.1f} mm | lip at x = {-lip_in_poker[0][0]/25.4:+.2f} in (blade centre) | tip check x={-tip_in_poker[:,0].mean():.0f} mm (33) | detected rotation of poker vs blade {np.degrees(np.arctan2(Rm[1,0],Rm[0,0])):.1f} deg")
