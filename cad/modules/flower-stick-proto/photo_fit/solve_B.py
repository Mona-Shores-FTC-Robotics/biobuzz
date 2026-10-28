import numpy as np, cv2
obj = np.array([[4.3,-40,4],[4.3,40,4],[-38,40,4],[-38,-40,4],[-24,-24,4],[-24,24,4],[-8,-32,4],[-8,32,4],[-40,32,4]], float)
img = np.array([[903.3,1094.3],[903.3,1485],[1110.7,1485],[1110.7,1094.3],[1041.7,1174],[1040.7,1407.3],[963.3,1133.3],[963.3,1445],[1118.3,1446.7]], float)
extra_obj=np.array([[33,-9.5,24.1],[33,9.5,24.1]],float); extra_img=np.array([[743.3,1241.7],[743.3,1338.3]],float)
W,H=1932,2576; cx,cy=W/2,H/2
def solve(f,O,I):
    K=np.array([[f,0,cx],[0,f,cy],[0,0,1]],float); ok,rv,tv=cv2.solvePnP(O,I,K,None,flags=cv2.SOLVEPNP_ITERATIVE)
    pr,_=cv2.projectPoints(O,rv,tv,K,None); return K,rv,tv,np.linalg.norm(pr[:,0]-I,axis=1)
def back(K,rv,tv,px,z):
    R,_=cv2.Rodrigues(rv); C=-R.T@tv.ravel(); d=R.T@np.linalg.inv(K)@np.array([px[0],px[1],1.0]); s=(z-C[2])/d[2]; return C+s*d, C, R
O=np.vstack([obj,extra_obj]); I=np.vstack([img,extra_img])
WEB_ABOVE_TILE=166.0     # 6.55 in (assumed)
for web in (150.0,166.0,180.0):
  print(f"--- channel web top {web/25.4:.2f} in above the tile")
  for f in (1400,1786,2200,2800):
    K,rv,tv,err=solve(f,O,I); R,_=cv2.Rodrigues(rv); C=-R.T@tv.ravel(); tilt=np.degrees(np.arccos(abs((R.T@np.array([0,0,1.0]))[2])))
    ztail=-web+13.0
    Pt,_,_=back(K,rv,tv,(757,897),ztail); Pt2,_,_=back(K,rv,tv,(757,1537),ztail)
    xt=(Pt[0]+Pt2[0])/2; lip=xt+50.8
    Pl,_,_=back(K,rv,tv,(595,897),ztail)
    print(f"f={f}: rms {np.sqrt((err**2).mean()):.1f}px; cam height above tile {C[2]+web:.0f} mm, tilt {tilt:.1f} deg | tail end x={xt:.0f} mm -> lip = {lip:.0f} mm = {lip/25.4:.2f} in ahead of the lip face | (direct: left edge px 595 -> {Pl[0]/25.4:.2f} in) | tip check x={back(K,rv,tv,(743.3,1241.7),24.1)[0][0]:.1f}")
