const window = {};
// The FLOWER stick's 2-D simulator, in the browser: a port of tools/ramp-hook/ramp.py + animate.py.
// FLOWER frame: inches, +x toward the wall, origin on the tile under the bottom POLLEN's centre, z up.
// Balls: hollow spheres (K = 2/3), Coulomb friction, restitution e below a rest speed; the blade is driven by position
// at the drive speed and stops on the uprights' forward points; a stall check stops it earlier when the push at the
// blade contact is tilted up less than the friction angle (the ball rolling on the blade, sliding on the uprights).
window.FlowerSim = (function () {
  const G = 386.1, R = 1.4, K = 2 / 3, DT = 5e-5, REST = 4.0, STAGED = [1.4, 4.3, 7.2, 10.1];
  const BACK = 0.35 + R, STOP = 1.18, FACE_BEHIND_LIP = STOP + 2.5, GECKO_R = 0.94, GECKO_Z = 2.89;
  const FIXED = [
    [[BACK, 0], [BACK, 20]], [[-1.54, 0], [BACK, 0]], [[-1.54, 3.90], [-1.54, 20]],
    [[-1.54, 0], [-1.54, 0.35]], [[-1.54, 0.35], [-2.36, 0.35]], [[-2.36, 0.35], [-2.36, 0]], [[-60, 0], [-2.36, 0]],
    [[-2.41, 3.90], [-1.54, 3.90]],
  ];
  function contact(b, seg, sv, e, mu) {
    const ax = seg[0][0], az = seg[0][1], ex = seg[1][0] - ax, ez = seg[1][1] - az, L2 = ex * ex + ez * ez;
    let t = ((b.x - ax) * ex + (b.z - az) * ez) / L2; t = t < 0 ? 0 : t > 1 ? 1 : t;
    const cx = ax + t * ex, cz = az + t * ez, dx = b.x - cx, dz = b.z - cz, d = Math.hypot(dx, dz);
    if (d >= R || d < 1e-9) return;
    const nx = dx / d, nz = dz / d;
    b.x = cx + nx * R; b.z = cz + nz * R;
    const rvx = b.vx - sv[0], rvz = b.vz - sv[1], vn = rvx * nx + rvz * nz;
    if (vn >= 0) return;
    const tx = -nz, tz = nx, slip = rvx * tx + rvz * tz + b.w * R;
    const jn = -(1 + (-vn > REST ? e : 0)) * vn;
    let jt = -slip / (1 + 1 / K); const lim = mu * jn; if (jt > lim) jt = lim; if (jt < -lim) jt = -lim;
    b.vx += jn * nx + jt * tx; b.vz += jn * nz + jt * tz; b.w += jt / (K * R);
  }
  function touching(b, seg) {
    const ax = seg[0][0], az = seg[0][1], ex = seg[1][0] - ax, ez = seg[1][1] - az, L2 = ex * ex + ez * ez;
    let t = ((b.x - ax) * ex + (b.z - az) * ez) / L2; t = t < 0 ? 0 : t > 1 ? 1 : t;
    const dx = b.x - (ax + t * ex), dz = b.z - (az + t * ez), d = Math.hypot(dx, dz);
    return d <= R + 1e-3 ? [dx / d, dz / d] : null;
  }
  // The Gecko is a soft wheel: it squeezes a ball by up to 0.9 in rather than stopping it, so it is not a rigid
  // contact. Any ball within its reach (its radius plus the ball's) is dragged toward the robot at the wheel's surface
  // speed, as fast as the grip allows; nothing holds the ball down or up.
  const GECKO_SPEED = 40, GECKO_ACCEL = 4000;
  function geckoDrag(b, ax, az) {
    if (Math.hypot(b.x - ax, b.z - az) > R + GECKO_R) return;
    const want = -GECKO_SPEED, dv = want - b.vx, cap = GECKO_ACCEL * DT;
    b.vx += dv < -cap ? -cap : dv > cap ? cap : dv;
    b.w = -b.vx / R;                                        // rolling with the wheel
  }
  // profile: [[x behind the lip, z], ...] from the lip's foot up over the peak and down; cfg: {drive, mu, extraDepth, stopErr, onRobot, e, tmax}
  function run(profile, cfg) {
    const e = cfg.e ?? 0.3, mu = cfg.mu, drive = cfg.drive, tmax = cfg.tmax ?? 4.0;
    const stop = STOP + (cfg.extraDepth || 0) + (cfg.stopErr || 0);
    const limit = Math.atan(mu);
    const balls = STAGED.map(z => ({ x: 0, z, vx: 0, vz: 0, w: 0 }));
    let lip = -4.4, t = 0, step = 0, stalled = null;
    const out = [null, null, null, null], fed = [null, null, null, null], frames = [];
    for (; t < tmax; t += DT, step++) {
      const driving = lip < stop && stalled === null;
      const bv = driving ? drive : 0;
      if (driving) lip = Math.min(stop, lip + drive * DT);
      const bl = profile.map(([px, pz]) => [lip - px, pz]);
      const bladeSegs = []; for (let i = 0; i + 1 < bl.length; i++) bladeSegs.push([bl[i], bl[i + 1]]);
      let axleX = 0, axleZ = 0;
      if (cfg.onRobot) { axleX = lip - FACE_BEHIND_LIP - GECKO_R; axleZ = GECKO_Z; }
      for (const b of balls) {
        b.vz -= G * DT; b.x += b.vx * DT; b.z += b.vz * DT;
        for (const s of FIXED) contact(b, s, [0, 0], e, mu);
        for (const s of bladeSegs) contact(b, s, [bv, 0], e, mu);
        if (cfg.onRobot) geckoDrag(b, axleX, axleZ);
      }
      for (let i = 0; i < 4; i++) for (let j = i + 1; j < 4; j++) {
        const a = balls[i], c = balls[j], dx = c.x - a.x, dz = c.z - a.z, d = Math.hypot(dx, dz);
        if (d > 1e-9 && d < 2 * R) {
          const nx = dx / d, nz = dz / d, push = (2 * R - d) / 2;
          a.x -= nx * push; a.z -= nz * push; c.x += nx * push; c.z += nz * push;
          const vn = (c.vx - a.vx) * nx + (c.vz - a.vz) * nz;
          if (vn < 0) { const jj = -(1 + (-vn > REST ? e : 0)) * vn / 2; a.vx -= jj * nx; a.vz -= jj * nz; c.vx += jj * nx; c.vz += jj * nz; }
        }
      }
      if (driving) {
        outer: for (const b of balls) {
          if (!touching(b, FIXED[0])) continue;
          for (const s of bladeSegs) {
            const n = touching(b, s);
            if (n && n[0] > 1e-6) { const alpha = Math.atan2(n[1], n[0]); if (alpha < limit) { stalled = [t, alpha * 180 / Math.PI]; break outer; } }
          }
        }
      }
      balls.forEach((b, i) => {
        if (out[i] === null && b.x + R < -2.36) out[i] = t;
        if (cfg.onRobot && fed[i] === null && b.x <= axleX) fed[i] = t;
      });
      if (step % 200 === 0) frames.push([+t.toFixed(3), +lip.toFixed(3), balls.map(b => [+b.x.toFixed(3), +b.z.toFixed(3)])]);
      if ((cfg.onRobot && fed.every(v => v !== null)) || (!cfg.onRobot && out.every(v => v !== null))) { frames.push([+t.toFixed(3), +lip.toFixed(3), balls.map(b => [+b.x.toFixed(3), +b.z.toFixed(3)])]); break; }
      if (stalled && t > stalled[0] + 0.6) break;
    }
    return { frames, stalled, out, fed, stop, lip };
  }
  // the parametric mountain: edge height, nose angle, peak height, tail angle; bottom 0.52 (the sled's shaft at 0.77)
  function mountain(p) {
    const bottom = 0.52, tn = Math.tan(p.nose * Math.PI / 180), tt = Math.tan(p.tail * Math.PI / 180);
    const xp = (p.peak - p.edge) / tn, xe = xp + (p.peak - bottom) / tt;
    return [[0, bottom], [0, p.edge], [xp, p.peak], [xe, bottom]];
  }
  return { run, mountain, STOP, R, GECKO_R, GECKO_Z, FACE_BEHIND_LIP };
})();
module.exports = window.FlowerSim;
