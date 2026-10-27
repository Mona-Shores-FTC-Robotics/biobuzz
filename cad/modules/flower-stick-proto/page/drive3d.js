<script>
// The 3D drive-in view: the same run as the 2-D simulator, drawn with three.js. Inches, the FLOWER's frame (x toward the
// wall, z up) mapped to three.js y-up. The FLOWER is tools/robot-cad/flower.py's model from the field-CAD numbers; the
// robot's front is the 16 Oct CAD's intake as simple solids (seven 48 mm Gecko wheels on their shaft, the uprights, the
// cross channel, the 96 mm second stage); the blade is the current profile extruded to its width.
window.Drive3D = (function () {
  const FLOWER_B64 = "__FLOWER3D__";
  const css = n => getComputedStyle(document.documentElement).getPropertyValue(n).trim();
  const canvas = document.getElementById("drive3d");
  const renderer = new THREE.WebGLRenderer({ canvas, antialias: true });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
  const scene = new THREE.Scene();
  const camera = new THREE.PerspectiveCamera(32, 16 / 8, 0.5, 400);
  camera.position.set(-7, 8, -19);
  const controls = new THREE.OrbitControls(camera, canvas);
  controls.enableDamping = true; controls.target.set(-1.5, 2.2, 0);
  scene.add(new THREE.HemisphereLight(0xffffff, 0x445566, 0.9));
  const sun = new THREE.DirectionalLight(0xffffff, 0.7); sun.position.set(-10, 20, 12); scene.add(sun);
  const tiles = new THREE.Mesh(new THREE.PlaneGeometry(40, 30), new THREE.MeshStandardMaterial({ color: 0x8b96a1, roughness: 0.95 }));
  tiles.rotation.x = -Math.PI / 2; tiles.position.set(-8, -0.005, 0); scene.add(tiles);
  const grid = new THREE.GridHelper(40, 20, 0x667, 0x667); grid.position.set(-8, 0.002, 0); grid.material.opacity = 0.25; grid.material.transparent = true; scene.add(grid);
  // the FLOWER
  const loader = new THREE.STLLoader();
  const bin = atob(FLOWER_B64); const buf = new Uint8Array(bin.length); for (let i = 0; i < bin.length; i++) buf[i] = bin.charCodeAt(i);
  const fg = loader.parse(buf.buffer); fg.rotateX(-Math.PI / 2); fg.computeVertexNormals();
  const flowerMat = new THREE.MeshStandardMaterial({ color: 0x3f8f3a, roughness: 0.6, transparent: true, opacity: 0.45, depthWrite: false });
  scene.add(new THREE.Mesh(fg, flowerMat));
  // the robot's front, in a group whose x is the face
  const robot = new THREE.Group(); scene.add(robot);
  const steel = new THREE.MeshStandardMaterial({ color: 0x9aa4ae, roughness: 0.4, metalness: 0.5 });
  const dark = new THREE.MeshStandardMaterial({ color: 0x3b4148, roughness: 0.6, transparent: true, opacity: 0.55, depthWrite: false });
  const soft = new THREE.MeshStandardMaterial({ color: 0xd8dde2, roughness: 0.8, transparent: true, opacity: 0.9 });
  const body = new THREE.MeshStandardMaterial({ color: 0xcdd5dc, roughness: 0.9, transparent: true, opacity: 0.15, depthWrite: false });
  const GECKO_Y = [-3.67, -2.52, -1.38, -0.25, 0.90, 2.04, 3.18];   // the seven wheels' centres, left is +y in the CAD, -z here
  const geckoGeo = new THREE.CylinderGeometry(0.94, 0.94, 0.63, 28); geckoGeo.rotateX(Math.PI / 2);
  const geckos = GECKO_Y.map(y => { const m = new THREE.Mesh(geckoGeo, soft); m.position.set(-0.94, 2.89, -y); robot.add(m); return m; });
  const shaft = new THREE.Mesh(new THREE.CylinderGeometry(0.16, 0.16, 9.4, 6), steel); shaft.geometry.rotateX(Math.PI / 2); shaft.position.set(-0.94, 2.89, 0); robot.add(shaft);
  for (const y of [-5.1, 5.1]) { const u = new THREE.Mesh(new THREE.BoxGeometry(1.9, 5.7, 0.5), dark); u.position.set(-0.95, 6.0, -y); robot.add(u); }
  const cross = new THREE.Mesh(new THREE.BoxGeometry(1.9, 0.5, 9.4), dark); cross.position.set(-0.95, 5.5, 0); robot.add(cross);
  const stage2 = new THREE.CylinderGeometry(1.89, 1.89, 1.9, 28); stage2.rotateX(Math.PI / 2);
  for (const y of [-1.0, 1.0]) { const m = new THREE.Mesh(stage2, soft); m.position.set(-8.2, 2.6, -y); robot.add(m); }
  const chassis = new THREE.Mesh(new THREE.BoxGeometry(10, 7, 16), body); chassis.position.set(-7, 4.6, 0); robot.add(chassis);
  // the corner wheel at the front right (4.5 in, servo driven), for POLLEN in the wall corner by the garden
  // a flat disc facing forward (its axle runs fore-aft): its face sweeps a ball sideways out of the corner
  const cornerGeo = new THREE.CylinderGeometry(2.25, 2.25, 0.5, 32); cornerGeo.rotateZ(Math.PI / 2);
  const corner = new THREE.Mesh(cornerGeo, soft); corner.position.set(0.97, 4.7, 6.2); robot.add(corner);
  const cornerHub = new THREE.Mesh(new THREE.CylinderGeometry(0.3, 0.3, 2.0, 12), steel); cornerHub.geometry.rotateZ(Math.PI / 2); cornerHub.position.set(0.3, 4.7, 6.2); robot.add(cornerHub);
  // the drive wheels' front pair (96 mm omni), beside the intake
  const omniGeo = new THREE.CylinderGeometry(1.89, 1.89, 1.9, 24); omniGeo.rotateX(Math.PI / 2);
  for (const y of [-6.2, 6.2]) { const w = new THREE.Mesh(omniGeo, dark); w.position.set(-1.6, 1.89, -y); robot.add(w); }
  // the rest of the robot from the Oct 25 export, as translucent boxes (x ahead of the face, y left -> -z here, z up -> y)
  function setBoxes(list) {
    const m = new THREE.MeshStandardMaterial({ color: 0x8a949e, roughness: 0.9, transparent: true, opacity: 0.18, depthWrite: false });
    for (const q of list) { const [x0, x1, y0, y1, z0, z1] = q.box; const bx = new THREE.Mesh(new THREE.BoxGeometry(Math.max(0.05, x1 - x0), Math.max(0.05, z1 - z0), Math.max(0.05, y1 - y0)), m); bx.position.set((x0 + x1) / 2, (z0 + z1) / 2, -(y0 + y1) / 2); robot.add(bx); }
  }
  // the Limelight 3A at its planned pose: lens 3.56 in behind the face, 14.0 up, pitched 45 deg up; 54.5 x 42 deg field of view
  let camMeshes = [];
  { const cam = new THREE.Mesh(new THREE.BoxGeometry(1.75, 1.7, 2.3), new THREE.MeshStandardMaterial({ color: 0x2bb5e0, roughness: 0.5 })); cam.rotation.z = Math.PI / 4; robot.add(cam);
    const R = 12, hw = R * Math.tan(54.5 / 2 * Math.PI / 180), hh = R * Math.tan(42 / 2 * Math.PI / 180);
    const v = new Float32Array([0, 0, 0, R, hh, -hw, R, hh, hw, 0, 0, 0, R, -hh, hw, R, -hh, -hw, 0, 0, 0, R, hh, hw, R, -hh, hw, 0, 0, 0, R, -hh, -hw, R, hh, -hw, R, hh, -hw, R, -hh, -hw, R, -hh, hw, R, hh, -hw, R, -hh, hw, R, hh, hw]);
    const g = new THREE.BufferGeometry(); g.setAttribute("position", new THREE.BufferAttribute(v, 3)); g.computeVertexNormals();
    const fov = new THREE.Mesh(g, new THREE.MeshBasicMaterial({ color: 0x2bb5e0, transparent: true, opacity: 0.12, side: THREE.DoubleSide, depthWrite: false })); fov.rotation.z = Math.PI / 4; robot.add(fov);
    const edges = new THREE.LineSegments(new THREE.EdgesGeometry(g), new THREE.LineBasicMaterial({ color: 0x2bb5e0, transparent: true, opacity: 0.5 })); edges.rotation.z = Math.PI / 4; robot.add(edges);
    camMeshes = [cam, fov, edges]; }
  function setCamera(x, z) { const y = z < 8 ? -7.0 : 0; camMeshes[0].position.set(x - 0.5, z, y); camMeshes[1].position.set(x, z, y); camMeshes[2].position.set(x, z, y); }   // the low corner spot sits 7 in off centre, outboard of the arm
  setCamera(-3.56, 14.0);
  // the blade and its arms, in a group whose x is the lip
  const bladeG = new THREE.Group(); scene.add(bladeG);
  const petg = new THREE.MeshStandardMaterial({ color: 0xd9711c, roughness: 0.55 });
  let bladeMesh = null;
  const bladeShaft = new THREE.Mesh(new THREE.CylinderGeometry(0.16, 0.16, 17.0, 6), steel); bladeShaft.geometry.rotateX(Math.PI / 2); bladeG.add(bladeShaft);
  let shaftBackNow = 0.77;
  // the arms from the October 25 CAD: 11-hole U-channels, 11.3 in long and 1.26 wide, pivoting 2.37 in behind the face and
  // 7.89 in up, the shaft 9.09 in from the pivot; the left arm at y 5.65, the right at -6.07
  const arms = [0, 1].map(() => { const a = new THREE.Mesh(new THREE.BoxGeometry(1, 1.26, 0.48), steel); scene.add(a); return a; });
  const stubs = [0, 1].map(() => { const st = new THREE.Mesh(new THREE.CylinderGeometry(0.2, 0.2, 1.2, 8), steel); st.geometry.rotateX(Math.PI / 2); robot.add(st); return st; });
  // where the arms pivot: left is +y in the CAD, -z here; the corner disc is on the right (+z)
  const ARMS = { uprights: [6.07, -5.65], left: [-5.65], both: [6.07, -5.65] };
  // the two mounts (simlib MOUNTS): the coach's tall arms, or the low arm with 39 mm wheels on the shaft outboard of the arms
  const MOUNTS = { tall: { pivot: [-2.37, 7.89], L: 9.09, back: 1.5, fwd: 9.09, w: 1.26, t: 0.48, ys: [6.07, -5.65], wheels: { r: 0.77, ys: [4.7, -4.7], w: 0.5 } },
                   low: { pivot: [0.8, 2.2], L: 3 * 24 / 25.4, back: 24 / 25.4, fwd: 2 * 24 / 25.4 + 0.47, w: 32 / 25.4, t: 0.5, ys: [4.2, -4.2], wheels: { r: 0.77, ys: [4.7, -4.7], w: 0.5 } } };
  let mountNow = "tall";
  // rounded ends on the arms: a cap cylinder at each end, placed with the arm every frame
  const armCaps = [0, 1].map(() => [0, 1].map(() => { const c = new THREE.Mesh(new THREE.CylinderGeometry(0.63, 0.63, 0.48, 16), steel); c.geometry.rotateX(Math.PI / 2); scene.add(c); return c; }));
  const wheelMeshes = [0, 1].map(() => { const m = new THREE.Mesh(new THREE.CylinderGeometry(0.77, 0.77, 0.5, 24), dark); m.geometry.rotateX(Math.PI / 2); m.visible = false; bladeG.add(m); return m; });
  function setMount(mode) {
    mountNow = mode; const M = MOUNTS[mode]; ARMS.both = M.ys; armMode = "both";
    arms.forEach(a => { a.geometry.dispose(); a.geometry = new THREE.BoxGeometry(1, M.w, M.t); });
    armCaps.forEach(pair => pair.forEach(c => { c.geometry.dispose(); c.geometry = new THREE.CylinderGeometry(M.w / 2, M.w / 2, M.t, 16); c.geometry.rotateX(Math.PI / 2); }));
    wheelMeshes.forEach((m, k) => { m.visible = !!M.wheels; if (M.wheels) m.position.set(-shaftBackNow, 0.77, -M.wheels.ys[k]); });
    setArms("both");
  }
  let armMode = "both";
  function setArms(mode) {
    armMode = mode;
    const ys = ARMS[mode];
    arms.forEach((a, k) => { a.visible = k < ys.length; if (k < ys.length) a.position.z = ys[k] - offsetNow; });
    stubs.forEach((st, k) => { st.visible = k < ys.length; if (k < ys.length) st.position.set(MOUNTS[mountNow].pivot[0], MOUNTS[mountNow].pivot[1], ys[k]); });
    const zmin = Math.min(...ys, -2.9), zmax = Math.max(...ys, 2.9);
    bladeShaft.geometry.dispose(); bladeShaft.geometry = new THREE.CylinderGeometry(0.16, 0.16, zmax - zmin + 0.6, 6); bladeShaft.geometry.rotateX(Math.PI / 2);
    bladeShaft.position.z = (zmin + zmax) / 2;
  }
  const sled = new THREE.Group(); scene.add(sled);
  const sledWheelGeo = new THREE.CylinderGeometry(0.77, 0.77, 0.4, 24); sledWheelGeo.rotateX(Math.PI / 2);
  for (const y of [-3.2, 3.2]) { const w = new THREE.Mesh(sledWheelGeo, dark); w.position.set(0, 0.77, -y); sled.add(w); const bar = new THREE.Mesh(new THREE.BoxGeometry(8, 0.3, 0.15), steel); bar.position.set(-4, 0.77, -y); sled.add(bar); }
  const ballGeo = new THREE.SphereGeometry(1.4, 24, 16);
  const ballMat = new THREE.MeshStandardMaterial({ color: 0xd8c21a, roughness: 0.5 });
  const balls = [0, 1, 2, 3].map(() => { const m = new THREE.Mesh(ballGeo, ballMat); scene.add(m); return m; });
  // The blade as a height field over its plan (x behind the lip, y across): top z = profile(x) + the lane's
  // cross-section (Carter's lane: shoulders raised `rise` above the tail from `start` behind the lip, reaching full
  // height over `lead`; between them a circular trough of radius R tangent to the tail at the centreline), bottom at the
  // profile's foot. Local x runs toward the robot (-x is behind the lip).
  function heightAt(profile, xb) {               // the centreline's top surface height at xb inches behind the lip
    for (let i = 1; i + 1 < profile.length; i++) {
      const [x0, z0] = profile[i], [x1, z1] = profile[i + 1];
      if (xb >= x0 && xb <= x1) return z0 + (z1 - z0) * (xb - x0) / (x1 - x0);
    }
    return xb < profile[1][0] ? profile[1][1] : profile[profile.length - 1][1];
  }
  function laneRise(g, xb, y, hw) {
    if (!g || g.rise <= 0) return 0;
    const gs = g.start + (g.oval || 0) * (1 - Math.sqrt(Math.max(0, 1 - (y / hw) ** 2)));
    if (xb <= gs) return 0;
    const lead = g.lead || 0.4, tp = xb >= gs + lead ? 1 : (xb - gs) / lead;
    const a = Math.abs(y); let h = a >= g.R ? g.rise : g.R - Math.sqrt(g.R * g.R - a * a); if (h > g.rise) h = g.rise;
    return h * tp;
  }
  // vee: the V nose. xb is measured behind the centre's lip (as the simulator's lip is); the local lip sits cut(y) behind it:
  // 0 over the middle (|y| <= 0.855), rising to vee at |y| >= 1.42 (the posts' points), and vee outside. The profile is
  // measured from the local lip, the back edge stays straight, so the sides are vee shorter than the middle: the printed v8.
  function bladeGeometry(profile, width, g, bosses, vee) {
    const bottom = profile[0][1], edge = profile[1][1], len = profile[profile.length - 1][0], hw = width / 2;
    const V = vee || 0, VI = 0.855, VO = 1.42;
    const cut = y => { const a = Math.abs(y); return !V ? 0 : a <= VI ? 0 : a >= VO ? V : V * (a - VI) / (VO - VI); };
    const bossZ = (xb, y) => bosses && Math.abs(y) >= bosses.inner && Math.abs(xb - bosses.x) <= bosses.len / 2 ? bosses.top : -1;
    const pos = [];
    const tri = (a, b, c) => pos.push(...a, ...b, ...c);
    const quad = (a, b, c, d) => { tri(a, b, c); tri(a, c, d); };
    // u: distance behind the local lip (the profile's own x); xb = u + cut(y) behind the centre's lip
    const top = (u, y) => { const xb = u + cut(y); return [-xb, Math.max(heightAt(profile, u) + laneRise(g, xb, y, hw), bossZ(xb, y)), -y]; }, bot = (u, y) => [-(u + cut(y)), bottom, -y];
    const nx = Math.max(6, Math.round(len / 0.06)), ny = Math.max(6, Math.round(width / 0.06));
    const xs = []; for (let i = 0; i <= nx; i++) xs.push(len * i / nx);
    if (g && g.rise > 0) xs.push(g.start, g.start + (g.lead || 0.4));
    if (bosses) xs.push(bosses.x - bosses.len / 2, bosses.x - bosses.len / 2 + 1e-3, bosses.x + bosses.len / 2 - 1e-3, bosses.x + bosses.len / 2);
    xs.sort((a, b) => a - b);
    const ysL = []; for (let j = 0; j <= ny; j++) ysL.push(-hw + width * j / ny);
    if (bosses) ysL.push(-bosses.inner, -bosses.inner + 1e-3, bosses.inner - 1e-3, bosses.inner); ysL.sort((a, b) => a - b);
    if (V) ysL.push(-VO, -VI, VI, VO), ysL.sort((a, b) => a - b);
    // u at the back edge for this y: the back stays straight at xb = len, so the local profile is cut off at len - cut(y)
    const uBack = y => len - cut(y);
    for (let i = 0; i + 1 < xs.length; i++) for (let j = 0; j + 1 < ysL.length; j++) {
      const ya = ysL[j], yb = ysL[j + 1];
      const ua0 = Math.min(xs[i], uBack(ya)), ua1 = Math.min(xs[i + 1], uBack(ya)), ub0 = Math.min(xs[i], uBack(yb)), ub1 = Math.min(xs[i + 1], uBack(yb));
      if (ua1 - ua0 < 1e-6 && ub1 - ub0 < 1e-6) continue;
      quad(top(ua0, ya), top(ub0, yb), top(ub1, yb), top(ua1, ya));
      quad(bot(ua0, ya), bot(ua1, ya), bot(ub1, yb), bot(ub0, yb));
    }
    // the nose's vertical face (the lip, following the V), the back face (straight), and the two sides
    for (let j = 0; j + 1 < ysL.length; j++) { const ya = ysL[j], yb = ysL[j + 1]; quad(bot(0, yb), bot(0, ya), top(0, ya).map((v, k) => k === 1 ? edge : v), top(0, yb).map((v, k) => k === 1 ? edge : v)); }
    for (let j = 0; j + 1 < ysL.length; j++) { const ya = ysL[j], yb = ysL[j + 1]; quad(bot(uBack(ya), ya), bot(uBack(yb), yb), top(uBack(yb), yb), top(uBack(ya), ya)); }
    if (bosses) for (const y0 of [-hw, hw]) { /* the sides already follow top(); the bosses' front and back faces: */ }
    for (const sgn of [-1, 1]) {
      const y = sgn * hw;
      for (let i = 0; i + 1 < xs.length; i++) { const u0 = Math.min(xs[i], uBack(y)), u1 = Math.min(xs[i + 1], uBack(y)); if (u1 - u0 < 1e-6) continue; const q = [bot(u0, y), top(u0, y), top(u1, y), bot(u1, y)]; sgn > 0 ? quad(q[0], q[1], q[2], q[3]) : quad(q[3], q[2], q[1], q[0]); }
    }
    const geo = new THREE.BufferGeometry(); geo.setAttribute("position", new THREE.Float32BufferAttribute(pos, 3)); geo.computeVertexNormals();
    return geo;
  }
  let offsetNow = 0, yawNow = 0;
  let bladeBottom = 0.52, bladeWidthNow = 5.4;
  // a blade given as a closed outline (x behind the lip, z), extruded across its width: the coach's plate
  function outlineGeometry(poly, width) {
    const shape = new THREE.Shape(); poly.forEach(([x, z], i) => i ? shape.lineTo(-x, z) : shape.moveTo(-x, z)); shape.closePath();
    const g = new THREE.ExtrudeGeometry(shape, { depth: width, bevelEnabled: false }); g.translate(0, 0, -width / 2); return g;
  }
  function setBlade(profile, width, shaftBack, lane, offset, yaw, bosses, vee, cutAt, outline) {
    bladeBottom = profile[0][1]; bladeWidthNow = width;
    if (bladeMesh) { bladeG.remove(bladeMesh); bladeMesh.geometry.dispose(); }
    let prof = profile;
    if (cutAt) {                                                         // the body only: cut off ahead of the pin
      const zAt = x => { for (let i = 0; i + 1 < profile.length; i++) { const [x0, z0] = profile[i], [x1, z1] = profile[i + 1]; if (x >= x0 && x <= x1) return z0 + (z1 - z0) * (x - x0) / (x1 - x0); } return profile[0][1]; };
      prof = [[cutAt, profile[0][1]], [cutAt, zAt(cutAt)], ...profile.filter(p => p[0] > cutAt)];
    }
    bladeMesh = new THREE.Mesh(outline ? outlineGeometry(outline, width) : bladeGeometry(prof, width, lane, bosses, vee), petg); bladeMesh.material.side = THREE.DoubleSide; bladeG.add(bladeMesh);
    shaftBackNow = shaftBack; bladeShaft.position.x = -shaftBack; bladeShaft.position.y = 0.77;
    offsetNow = offset || 0; yawNow = (yaw || 0) * Math.PI / 180;
    // the robot and the blade sit `offset` to the FLOWER's +y (three.js -z) and are turned by `yaw` about the vertical
    for (const grp of [bladeG, robot, sled]) { grp.position.z = -offsetNow; grp.rotation.y = -yawNow; }
    setArms(armMode);
  }
  // the nose piece (v10): its outline extruded across the blade, the toe 1.2 in wide, turning about the pin
  let noseG = null;
  function nosePiece(poly, pivot, width) {
    const g = new THREE.Group();
    const shape = new THREE.Shape(); poly.forEach(([x, z], i) => { const X = -(x - pivot[0]), Y = z - pivot[1]; i ? shape.lineTo(X, Y) : shape.moveTo(X, Y); }); shape.closePath();
    const wedge = poly.filter(([x]) => x >= 0).concat();                 // behind the lip: full width; the toe ahead of it: 1.2 in
    const full = new THREE.Shape(); let first = true;
    for (const [x, z] of poly) { if (x < 0) continue; const X = -(x - pivot[0]), Y = z - pivot[1]; first ? full.moveTo(X, Y) : full.lineTo(X, Y); first = false; } full.closePath();
    const m1 = new THREE.Mesh(new THREE.ExtrudeGeometry(full, { depth: width, bevelEnabled: false }), petg); m1.position.z = -width / 2;
    const toeShape = new THREE.Shape(); first = true;
    for (const [x, z] of [[-1.3, 0.42], [-1.3, 0.52], [0.05, 0.52], [0.05, 0.42]]) { const X = -(x - pivot[0]), Y = z - pivot[1]; first ? toeShape.moveTo(X, Y) : toeShape.lineTo(X, Y); first = false; } toeShape.closePath();
    g.add(m1);
    if (poly.some(([x]) => x < 0)) { const m2 = new THREE.Mesh(new THREE.ExtrudeGeometry(toeShape, { depth: 1.2, bevelEnabled: false }), petg); m2.position.z = -0.6; g.add(m2); }
    g.position.set(-pivot[0], pivot[1], 0); return g;
  }
  function frame(lip, ballPos, onRobot, shaftBack, t, nose, phi) {
    phi = phi || 0; bladeG.position.set(lip, 0, 0); bladeG.rotation.z = 0;
    if (nose) {
      if (!noseG) { noseG = nosePiece(nose[2], nose[1], bladeWidthNow); bladeG.add(noseG); }
      noseG.visible = true; noseG.rotation.z = nose[0] * Math.PI / 180;
    } else if (noseG) noseG.visible = false;
    const shx = lip - shaftBack;
    if (onRobot) {
      robot.visible = true; sled.visible = false;
      const M = MOUNTS[mountNow], sa = M.pivot[0] + Math.sqrt(M.L * M.L - (M.pivot[1] - 0.77) ** 2);
      const face = lip - shaftBack - sa, px = face + M.pivot[0], pz = M.pivot[1];
      robot.position.x = face;
      const a = t * 40 / 0.94; for (const g of geckos) g.rotation.z = -a;
      if (phi) { const c = Math.cos(phi), s = Math.sin(phi); bladeG.position.set(px + (lip - px) * c - (0 - pz) * s, pz + (lip - px) * s + (0 - pz) * c, 0); bladeG.rotation.z = phi; }
      const dx0 = shx - px, dz0 = 0.77 - pz, c0 = Math.cos(phi), s0 = Math.sin(phi), dx = dx0 * c0 - dz0 * s0, dz = dx0 * s0 + dz0 * c0, L = Math.hypot(dx, dz), ang = Math.atan2(dz, dx), ux = dx / L, uz = dz / L, mid = (M.fwd - M.back) / 2;
      arms.forEach((arm, k) => { arm.visible = k < ARMS[armMode].length; arm.scale.x = M.back + M.fwd; arm.position.set(px + ux * mid, pz + uz * mid, ARMS[armMode][k] - offsetNow); arm.rotation.z = ang;
        armCaps[k][0].visible = armCaps[k][1].visible = arm.visible; armCaps[k][0].position.set(px - ux * M.back, pz - uz * M.back, ARMS[armMode][k] - offsetNow); armCaps[k][1].position.set(px + ux * M.fwd, pz + uz * M.fwd, ARMS[armMode][k] - offsetNow); });
      wheelMeshes.forEach((m, k) => { if (M.wheels) m.position.set(-shaftBack, 0.77, -M.wheels.ys[k] - 0); });
      stubs.forEach(st => st.visible = st.visible);
    } else {
      robot.visible = false; sled.visible = true; sled.position.x = shx;
      for (const arm of arms) arm.visible = false; armCaps.forEach(pair => pair.forEach(c => c.visible = false));
    }
    ballPos.forEach(([x, z, y], k) => balls[k].position.set(x, z, -(y || 0)));
  }
  // the poker on the robot (poker_u.py): an L over the intake's front channel: a wall down the front lip, the roof flat on the
  // web with relief holes over the screws, the poke ahead of the wall and a buttress from its top back along the roof
  let pokerMesh = null;
  function setPoker(pk) {
    if (pokerMesh) { robot.remove(pokerMesh); pokerMesh.traverse(o => { if (o.geometry) o.geometry.dispose(); }); pokerMesh = null; }
    if (!pk) return;
    const mat = new THREE.MeshStandardMaterial({ color: 0xe0552b, roughness: 0.6 }), g = new THREE.Group(), web = pk.web || pk.z0 - 0.09, U = 4.1, w = pk.hw ? 2 * pk.hw : 0.75, T = 0.16, D = 1.9, R = 0.2;
    const add = (x0, x1, z0, z1, wd, y) => { const m = new THREE.Mesh(new THREE.BoxGeometry(x1 - x0, z1 - z0, wd), mat); m.position.set((x0 + x1) / 2, (z0 + z1) / 2, -(y || 0)); g.add(m); };
    add(-D, T, web, web + T, U);                     // the roof, flat on the web
    add(0.01, T, web - 0.47, web + T, U);            // the front wall down the lip
    add(T, pk.reach, pk.z0, pk.z1, w);               // the poke
    const sh = new THREE.Shape(); sh.moveTo(-1.42, web + T); sh.lineTo(T, web + T); sh.lineTo(T, pk.z1); sh.closePath();   // the buttress
    const but = new THREE.Mesh(new THREE.ExtrudeGeometry(sh, { depth: w, bevelEnabled: false }), mat); but.position.z = -w / 2; g.add(but);
    pokerMesh = g; robot.add(g);
  }
  function theme() { scene.background = new THREE.Color(css("--panel")); }
  theme();
  new MutationObserver(theme).observe(document.documentElement, { attributes: true, attributeFilter: ["data-theme"] });
  try { matchMedia("(prefers-color-scheme: dark)").addEventListener("change", theme); } catch (e) {}
  function resize() {
    const w = canvas.clientWidth, h = canvas.clientHeight, r = renderer.getPixelRatio();
    if (w && h && (canvas.width !== Math.round(w * r) || canvas.height !== Math.round(h * r))) { renderer.setSize(w, h, false); camera.aspect = w / h; camera.updateProjectionMatrix(); }
  }
  (function loop() { resize(); controls.update(); renderer.render(scene, camera); requestAnimationFrame(loop); })();
  return { setBlade, setArms, frame, setPoker, setMount, setBoxes, setCamera };
})();
</script>
