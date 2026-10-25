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
  // the blade and its arms, in a group whose x is the lip
  const bladeG = new THREE.Group(); scene.add(bladeG);
  const petg = new THREE.MeshStandardMaterial({ color: 0xd9711c, roughness: 0.55 });
  let bladeMesh = null;
  const bladeShaft = new THREE.Mesh(new THREE.CylinderGeometry(0.16, 0.16, 17.0, 6), steel); bladeShaft.geometry.rotateX(Math.PI / 2); bladeG.add(bladeShaft);
  let shaftBackNow = 0.77;
  const arms = [0, 1].map(() => { const a = new THREE.Mesh(new THREE.BoxGeometry(1, 0.35, 0.18), steel); scene.add(a); return a; });
  const stubs = [0, 1].map(() => { const st = new THREE.Mesh(new THREE.CylinderGeometry(0.2, 0.2, 1.2, 8), steel); st.geometry.rotateX(Math.PI / 2); robot.add(st); return st; });
  // where the arms pivot: left is +y in the CAD, -z here; the corner disc is on the right (+z)
  const ARMS = { uprights: [4.9, -4.9], left: [4.9 * 0 - 8.3], both: [-8.3, 8.3] };
  let armMode = "both";
  function setArms(mode) {
    armMode = mode;
    const ys = ARMS[mode];
    arms.forEach((a, k) => { a.visible = k < ys.length; if (k < ys.length) a.position.z = ys[k] - offsetNow; });
    stubs.forEach((st, k) => { st.visible = k < ys.length; if (k < ys.length) st.position.set(-0.94, 2.89, ys[k]); });
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
  function setBlade(profile, width, shaftBack, lane, offset, yaw, bosses, vee, cutAt) {
    bladeBottom = profile[0][1]; bladeWidthNow = width;
    if (bladeMesh) { bladeG.remove(bladeMesh); bladeMesh.geometry.dispose(); }
    let prof = profile;
    if (cutAt) {                                                         // the body only: cut off ahead of the pin
      const zAt = x => { for (let i = 0; i + 1 < profile.length; i++) { const [x0, z0] = profile[i], [x1, z1] = profile[i + 1]; if (x >= x0 && x <= x1) return z0 + (z1 - z0) * (x - x0) / (x1 - x0); } return profile[0][1]; };
      prof = [[cutAt, profile[0][1]], [cutAt, zAt(cutAt)], ...profile.filter(p => p[0] > cutAt)];
    }
    bladeMesh = new THREE.Mesh(bladeGeometry(prof, width, lane, bosses, vee), petg); bladeMesh.material.side = THREE.DoubleSide; bladeG.add(bladeMesh);
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
    const m2 = new THREE.Mesh(new THREE.ExtrudeGeometry(toeShape, { depth: 1.2, bevelEnabled: false }), petg); m2.position.z = -0.6;
    g.add(m1, m2); g.position.set(-pivot[0], pivot[1], 0); return g;
  }
  function frame(lip, ballPos, onRobot, shaftBack, t, nose) {
    bladeG.position.x = lip;
    if (nose) {
      if (!noseG) { noseG = nosePiece(nose[2], nose[1], bladeWidthNow); bladeG.add(noseG); }
      noseG.visible = true; noseG.rotation.z = nose[0] * Math.PI / 180;
    } else if (noseG) noseG.visible = false;
    const shx = lip - shaftBack;
    if (onRobot) {
      robot.visible = true; sled.visible = false;
      const face = lip - 3.68, ax = face - 0.94;
      robot.position.x = face;
      const a = t * 40 / 0.94; for (const g of geckos) g.rotation.z = -a;
      const dx = shx - ax, dz = 0.77 - 2.89, L = Math.hypot(dx, dz), ang = Math.atan2(dz, dx);
      arms.forEach((arm, k) => { arm.visible = k < ARMS[armMode].length; arm.scale.x = L; arm.position.set((shx + ax) / 2, (0.77 + 2.89) / 2, ARMS[armMode][k] - offsetNow); arm.rotation.z = ang; });
      stubs.forEach(st => st.visible = st.visible);
    } else {
      robot.visible = false; sled.visible = true; sled.position.x = shx;
      for (const arm of arms) arm.visible = false;
    }
    ballPos.forEach(([x, z, y], k) => balls[k].position.set(x, z, -(y || 0)));
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
  return { setBlade, setArms, frame };
})();
</script>
