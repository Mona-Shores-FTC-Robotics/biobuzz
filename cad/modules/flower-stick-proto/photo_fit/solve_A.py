import numpy as np, cv2
# the L's roof top plane (z = 4 mm above the web top): x ahead of the lip face, y to the robot's left (image down); mm
obj = np.array([
 [ 4.3,-40,4], [ 4.3, 40,4], [-38, 40,4], [-38,-40,4],      # roof corners
 [-24,-24,4], [-24, 24,4],                                   # 12 mm roof holes
 [-8,-32,4], [-8, 32,4],                                      # front relief holes
 [-40, 32,4],                                                 # rear screw head
], float)
img = np.array([
 [923,1057],[955,1462],[1182,1430],[1135,1028],
 [1071,1117],[1096,1359],
 [985,1087],[1015,1405],
 [1185,1391],
], float)
W,H=1932,2576; cx,cy=W/2,H/2
def solve(f, pts_obj=obj, pts_img=img):
    K=np.array([[f,0,cx],[0,f,cy],[0,0,1]],float)
    ok,rv,tv=cv2.solvePnP(pts_obj,pts_img,K,None,flags=cv2.SOLVEPNP_ITERATIVE)
    proj,_=cv2.projectPoints(pts_obj,rv,tv,K,None); err=np.linalg.norm(proj[:,0]-pts_img,axis=1)
    return K,rv,tv,err
def backproject(K,rv,tv,px,z):
    R,_=cv2.Rodrigues(rv); C=-R.T@tv.ravel()
    d=R.T@np.linalg.inv(K)@np.array([px[0],px[1],1.0])
    s=(z-C[2])/d[2]; P=C+s*d; return P,C
for f in (1400,1786,2200,2800):
    K,rv,tv,err=solve(f); R,_=cv2.Rodrigues(rv); C=-R.T@tv.ravel()
    zc=R.T@np.array([0,0,1.0])   # optical axis in world
    tilt=np.degrees(np.arccos(abs(zc[2])))
    print(f"f={f}: rms reproj {np.sqrt((err**2).mean()):.1f}px max {err.max():.1f}; camera at x={C[0]:.0f} y={C[1]:.0f} z={C[2]:.0f} mm above web (height above tile ~{C[2]+166:.0f}); optical axis tilted {tilt:.1f} deg from vertical")
    # validation: the poke's top corners at z = 24.1 should land at x = 33 mm
    for nm,px in (("poke top tip, upper corner",(760,1197)),("poke top tip, lower corner",(765,1283))):
        P,_=backproject(K,rv,tv,px,24.1); print(f"   {nm}: x={P[0]:.1f} y={P[1]:.1f} mm (design: x=33, y=-+9.5)")
    # the blade's front edge: left edge of the red, tile plane (blade lip top about z = web - 166 + 13)
    for tile in (-166+13,):
        for nm,px in (("blade lip upper (625,1090)",(625,1090)),("blade lip lower (645,1650)",(645,1650))):
            P,_=backproject(K,rv,tv,px,tile); print(f"   {nm}: x={P[0]:.0f} mm = {P[0]/25.4:.2f} in ahead of the lip face, y={P[1]:.0f}")

print("\n--- fitting f with the poke's own height: tip top corners (z=24.1) and the tip's lower +y corner (z=-21.6)")
extra_obj=np.array([[33,-9.5,24.1],[33,9.5,24.1],[33,9.5,-21.6]],float)
extra_img=np.array([[760,1197],[765,1283],[827,1340]],float)
O=np.vstack([obj,extra_obj]); I=np.vstack([img,extra_img])
best=None
for f in range(1000,3600,100):
    K,rv,tv,err=solve(f,O,I); rms=np.sqrt((err**2).mean())
    P,_=backproject(K,rv,tv,(625,1090),-166+13); P2,_=backproject(K,rv,tv,(645,1650),-166+13)
    print(f"f={f}: rms {rms:5.1f}px (poke pts: {err[-3:].round(1)})  blade lip {P[0]/25.4:.2f} / {P2[0]/25.4:.2f} in")
