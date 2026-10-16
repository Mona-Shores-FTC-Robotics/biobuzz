<script>
// The FLOWER stick's simulator, in the browser: a port of tools/ramp-hook/ramp.py + animate.py, with a sideways
// dimension added for the tail's gutter (Carter's lane) and the ways a run can be off: the robot parked off-centre,
// the robot turned, and the stack leaning in the FLOWER.
// FLOWER frame: inches, +x toward the wall, y across (the FLOWER's centreline is y = 0), z up; origin on the tile under
// the bottom POLLEN's centre.
// Balls: hollow spheres (K = 2/3), Coulomb friction, restitution e below a rest speed; the blade is driven by position
// at the drive speed and stops on the uprights' forward points; a stall check stops it earlier when the push at the
// blade contact is tilted up less than the friction angle (the ball rolling on the blade, sliding on the uprights).
// Sideways: the blade's top is profile(x behind the lip) + gutter(x, y across the blade); the gutter is a circular
// lane (radius R) between shoulders raised `rise` above the tail, starting `start` behind the lip and reaching full height
// over `lead` inches (a gentle lead-in, so the shoulders are not a bump the ball meets head-on).
// A ball in contact with the blade feels the lane's cross-slope as a sideways push. The FLOWER holds the bottom ball
// in the ring's hole and in the uprights' vee; the upper balls are held by the four pipes. The intake's wheel row is
// `mouth` wide: a ball reaching the wheels outside it rolls past.
window.FlowerSim = (function () {
  const G = 386.1, R = 1.4, K = 2 / 3, DT = 5e-5, REST = 4.0, STAGED = [1.4, 4.3, 7.2, 10.1];
  const BACK = 0.35 + R, STOP = 1.18, FACE_BEHIND_LIP = STOP + 2.5, GECKO_R = 0.94, GECKO_Z = 2.89;
  const PIPE = 1.72, PIPE_R = 0.525, PIPE_Z = 4.25, HOLE_PLAY = 0.14;
  const FIXED = [
    [[BACK, 0], [BACK, 20]], [[-1.54, 0], [BACK, 0]], [[-1.54, 3.90], [-1.54, 20]],
    [[-1.54, 0], [-1.54, 0.35]], [[-1.54, 0.35], [-2.36, 0.35]], [[-2.36, 0.35], [-2.36, 0]], [[-60, 0], [-2.36, 0]],
    [[-2.41, 3.90], [-1.54, 3.90]],
  ];
  // returns the normal impulse (0 if no contact), so the caller can tilt it sideways
  function contact(b, seg, sv, e, mu) {
    const ax = seg[0][0], az = seg[0][1], ex = seg[1][0] - ax, ez = seg[1][1] - az, L2 = ex * ex + ez * ez;
    let t = ((b.x - ax) * ex + (b.z - az) * ez) / L2; t = t < 0 ? 0 : t > 1 ? 1 : t;
    const cx = ax + t * ex, cz = az + t * ez, dx = b.x - cx, dz = b.z - cz, d = Math.hypot(dx, dz);
    if (d >= R || d < 1e-9) return 0;
    const nx = dx / d, nz = dz / d;
    b.x = cx + nx * R; b.z = cz + nz * R;
    const rvx = b.vx - sv[0], rvz = b.vz - sv[1], vn = rvx * nx + rvz * nz;
    if (vn >= 0) return 0;
    const tx = -nz, tz = nx, slip = rvx * tx + rvz * tz + b.w * R;
    const jn = -(1 + (-vn > REST ? e : 0)) * vn;
    let jt = -slip / (1 + 1 / K); const lim = mu * jn; if (jt > lim) jt = lim; if (jt < -lim) jt = -lim;
    b.vx += jn * nx + jt * tx; b.vz += jn * nz + jt * tz; b.w += jt / (K * R);
    // sideways: the same friction law across the contact (spin about the x axis, b.u), so a kicked ball rolls, not slides
    const slipY = b.vy - (sv[2] || 0) + b.u * R;
    let jy = -slipY / (1 + 1 / K); if (jy > lim) jy = lim; if (jy < -lim) jy = -lim;
    b.vy += jy; b.u += jy / (K * R);
    return jn;
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
  // a vertical post (the pipes): push the ball out in x-y, with friction, above z0
  function post(b, px, py, pr, z0, e, mu) {
    if (b.z + R < z0) return;
    const dx = b.x - px, dy = b.y - py, d = Math.hypot(dx, dy), lim = pr + R;
    if (d >= lim || d < 1e-9) return;
    const nx = dx / d, ny = dy / d; b.x = px + nx * lim; b.y = py + ny * lim;
    const vn = b.vx * nx + b.vy * ny; if (vn >= 0) return;
    const jn = -(1 + (-vn > REST ? e : 0)) * vn; b.vx += jn * nx; b.vy += jn * ny;
    const tx = -ny, ty = nx, vt = b.vx * tx + b.vy * ty; let jt = -vt; const l = mu * jn; if (jt > l) jt = l; if (jt < -l) jt = -l;
    b.vx += jt * tx; b.vy += jt * ty;
  }
  // the lane's cross-section: height of the shoulders above the lane's centre at y across the blade, and its slope
  function cross(g, y) {
    if (!g || g.rise <= 0) return [0, 0];
    const r = g.R, a = Math.abs(y);
    if (a >= r) return [g.rise, 0];
    const h = r - Math.sqrt(r * r - a * a);
    if (h >= g.rise) return [g.rise, 0];
    return [h, y / Math.sqrt(r * r - a * a)];
  }
  // how far the lane reaches in y before the shoulders' flat cap (the catch width, each side)
  function catchHalf(g) { if (!g || g.rise <= 0) return 0; return g.rise >= g.R ? g.R : Math.sqrt(g.R * g.R - (g.R - g.rise) ** 2); }
  // profile: [[x behind the lip, z], ...] from the lip's foot up over the peak and down
  // cfg: {drive, mu, extraDepth, stopErr, onRobot, e, tmax, width, gutter: {R, rise, start, lead}, offset, yaw (deg), lean, mouth}
  function run(profile, cfg) {
    const e = cfg.e ?? 0.3, mu = cfg.mu, drive = cfg.drive, tmax = cfg.tmax ?? 4.0;
    const stop = STOP + (cfg.extraDepth || 0) + (cfg.stopErr || 0);
    const limit = Math.atan(mu);
    const width = cfg.width || 5.4, hw = width / 2, g = cfg.gutter && cfg.gutter.rise > 0 ? cfg.gutter : null;
    const off = cfg.offset || 0, yaw = (cfg.yaw || 0) * Math.PI / 180, sy = Math.sin(yaw), cy = Math.cos(yaw);
    const lean = cfg.lean || 0, mouth = cfg.mouth || 7.5;
    const balls = STAGED.map((z, k) => ({ x: 0, y: lean * k / 3, z, vx: 0, vy: 0, vz: 0, w: 0, u: 0 }));
    let lip = -4.4, t = 0, step = 0, stalled = null;
    const out = [null, null, null, null], fed = [null, null, null, null], missed = [null, null, null, null], offBlade = [null, null, null, null], frames = [];
    const yAtWheels = [null, null, null, null];
    // the blade's frame: xb behind the lip along the blade's axis, yb across it, for a blade centred `off` and turned `yaw`
    const toBlade = b => { const dx = lip - b.x, dy = b.y - off; return [dx * cy + dy * sy, dy * cy - dx * sy]; };
    const xs = profile.map(p => p[0]);
    const snap = () => [+t.toFixed(3), +lip.toFixed(3), balls.map(b => [+b.x.toFixed(3), +b.z.toFixed(3), +b.y.toFixed(3)])];
    for (; t < tmax; t += DT, step++) {
      const driving = lip < stop && stalled === null;
      const bv = driving ? drive : 0;
      if (driving) lip = Math.min(stop, lip + drive * DT);
      let axleX = 0, axleZ = 0;
      if (cfg.onRobot) { axleX = lip - FACE_BEHIND_LIP - GECKO_R; axleZ = GECKO_Z; }
      const touch = balls.map(() => false);
      for (const b of balls) {
        b.vz -= G * DT; b.x += b.vx * DT; b.y += b.vy * DT; b.z += b.vz * DT;
        for (const s of FIXED) contact(b, s, [0, 0], e, mu);
        // the blade under this ball: the profile raised by the lane's cross-section at the ball's y across the blade
        const [xb, yb] = toBlade(b);
        if (Math.abs(yb) <= hw) {
          const [h, slope] = cross(g, yb);
          const seg = [];
          let prev = null;
          const verts = [];
          for (let i = 0; i < profile.length; i++) verts.push(profile[i][0]);
          if (g) { const lead = g.lead || 0.4; if (!verts.includes(g.start)) verts.push(g.start); if (!verts.includes(g.start + lead)) verts.push(g.start + lead); }
          verts.sort((a, c) => a - c);
          const zAt = x => { for (let i = 1; i + 1 < profile.length; i++) { const [x0, z0] = profile[i], [x1, z1] = profile[i + 1]; if (x >= x0 - 1e-9 && x <= x1 + 1e-9) return z0 + (z1 - z0) * (x - x0) / (x1 - x0); } return x < profile[1][0] ? profile[1][1] : profile[profile.length - 1][1]; };
          const taper = x => { if (!g) return 0; const lead = g.lead || 0.4; return x <= g.start ? 0 : x >= g.start + lead ? 1 : (x - g.start) / lead; };
          const pts = [[profile[0][0], profile[0][1]]];
          for (const x of verts) { if (x === profile[0][0] && pts.length === 1 && profile[0][1] !== profile[1][1]) { pts.push([x, profile[1][1] + h * taper(x)]); continue; } if (x === profile[0][0]) continue; pts.push([x, zAt(x) + h * taper(x)]); }
          for (let i = 0; i + 1 < pts.length; i++) seg.push([[lip - pts[i][0], pts[i][1]], [lip - pts[i + 1][0], pts[i + 1][1]]]);
          let jsum = 0, tp = 0;
          for (const s of seg) { const j = contact(b, s, [bv, 0], e, mu); if (j > 0) { jsum += j; tp = Math.max(tp, taper(xb)); } }
          if (jsum > 0) { touch[balls.indexOf(b)] = true; if (slope !== 0 && tp > 0) b.vy -= jsum * slope * tp / (1 + K); }
          if (jsum > 0 && yaw !== 0) { /* a turned blade carries the ball along its own axis: nothing more to do, the lane's push above already follows it */ }
        }
        // the FLOWER holds the balls sideways: the bottom one in the ring's hole and the uprights' vee, the rest between the pipes
        const inHole = Math.abs(b.x) < 0.6 && b.z < 0.35 + R + 0.25, inVee = b.x > BACK - R - 0.02 && b.z < PIPE_Z;
        if (inHole || inVee) { if (b.y > HOLE_PLAY) { b.y = HOLE_PLAY; if (b.vy > 0) b.vy = 0; } if (b.y < -HOLE_PLAY) { b.y = -HOLE_PLAY; if (b.vy < 0) b.vy = 0; } }
        for (const px of [PIPE, -PIPE]) for (const py of [PIPE, -PIPE]) post(b, px, py, PIPE_R, PIPE_Z, e, mu);
        if (cfg.onRobot) { const [, yb] = toBlade(b); if (Math.abs(yb) <= mouth / 2 + 0.6) geckoDrag(b, axleX, axleZ); }
      }
      for (let i = 0; i < 4; i++) for (let j = i + 1; j < 4; j++) {
        const a = balls[i], c = balls[j], dx = c.x - a.x, dy = c.y - a.y, dz = c.z - a.z, d = Math.hypot(dx, dy, dz);
        if (d > 1e-9 && d < 2 * R) {
          const nx = dx / d, ny = dy / d, nz = dz / d, push = (2 * R - d) / 2;
          a.x -= nx * push; a.y -= ny * push; a.z -= nz * push; c.x += nx * push; c.y += ny * push; c.z += nz * push;
          const vn = (c.vx - a.vx) * nx + (c.vy - a.vy) * ny + (c.vz - a.vz) * nz;
          if (vn < 0) { const jj = -(1 + (-vn > REST ? e : 0)) * vn / 2; a.vx -= jj * nx; a.vy -= jj * ny; a.vz -= jj * nz; c.vx += jj * nx; c.vy += jj * ny; c.vz += jj * nz; }
        }
      }
      if (driving) {
        const bl = profile.map(([px, pz]) => [lip - px, pz]);
        outer: for (const b of balls) {
          if (!touching(b, FIXED[0])) continue;
          for (let i = 0; i + 1 < bl.length; i++) {
            const n = touching(b, [bl[i], bl[i + 1]]);
            if (n && n[0] > 1e-6) { const alpha = Math.atan2(n[1], n[0]); if (alpha < limit) { stalled = [t, alpha * 180 / Math.PI]; break outer; } }
          }
        }
      }
      balls.forEach((b, i) => {
        const [xb, yb] = toBlade(b);
        if (out[i] === null && b.x + R < -2.36) out[i] = t;
        if (offBlade[i] === null && xb > 0.2 && xb < profile[profile.length - 1][0] && Math.abs(yb) > hw && b.z < 2.2) offBlade[i] = t;
        if (cfg.onRobot && fed[i] === null && missed[i] === null && b.x <= axleX + R + GECKO_R) {
          if (Math.abs(yb) <= mouth / 2 + 0.6) { fed[i] = t; yAtWheels[i] = +yb.toFixed(2); } else if (b.x <= axleX) { missed[i] = t; yAtWheels[i] = +yb.toFixed(2); }
        }
      });
      if (step % 200 === 0) frames.push(snap());
      const done = cfg.onRobot ? fed.every((v, i) => v !== null || missed[i] !== null) : out.every(v => v !== null);
      if (done) { frames.push(snap()); break; }
      if (stalled && t > stalled[0] + 0.6) break;
    }
    return { frames, stalled, out, fed, missed, offBlade, yAtWheels, stop, lip, catchHalf: catchHalf(g) };
  }
  // the parametric mountain: edge height, nose angle, peak height, tail angle; bottom 0.52 (the sled's shaft at 0.77)
  function mountain(p) {
    const bottom = 0.52, tn = Math.tan(p.nose * Math.PI / 180), tt = Math.tan(p.tail * Math.PI / 180);
    const xp = (p.peak - p.edge) / tn, xe = xp + (p.peak - bottom) / tt;
    return [[0, bottom], [0, p.edge], [xp, p.peak], [xe, bottom]];
  }
  return { run, mountain, cross, catchHalf, STOP, R, GECKO_R, GECKO_Z, FACE_BEHIND_LIP, PIPE, PIPE_R };
})();
</script>
