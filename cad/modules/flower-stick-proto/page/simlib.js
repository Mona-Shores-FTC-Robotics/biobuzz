const window = {};
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
  const BACK = 0.33 + R, STOP = 1.18, GECKO_R = 0.94, GECKO_Z = 2.89;
  // the arms from the October 25 CAD: two 11-hole mini channels pivoting 2.37 in behind the intake face and 7.89 in up, 9.09 in
  // from pivot to the shaft across their tips. With the shaft on 0.77 in sled wheels the arm stops 52 deg below horizontal and
  // the shaft sits SHAFT_AHEAD of the face; the lip is shaftBack further on, so the face is SHAFT_AHEAD + shaftBack behind it.
  // Two mounts. "tall": the coach's arms, drawn ending at the shaft with a rounded end: his channel runs 1 in past the shaft and
  // its corner would be 0.4 in below the tile with the shaft on the wheels, so the channel is cut back to the shaft hole. "low": the low arm (low_arm.py): a 5-hole lowside U-channel pivoting 0.5 in ahead
  // of the face and 2.2 in up on a bracket off the chassis side rails, the shaft 72 mm (3 holes) down the arm on 39 mm wheels,
  // the arm 30 deg below horizontal when down and stowed 8 deg short of vertical; the blade folds up in front of the mouth.
  const HOLE = 24 / 25.4;
  const MOUNTS = {
    tall: { pivot: [-2.37, 7.89], L: 9.09, shaftZ: 0.77, stowedDir: [0.177, 0.984], back: 1.5, fwd: 9.09, w: 1.26, armY: [[5.41, 5.89], [-6.30, -5.83]], wheels: null },
    low: { pivot: [0.8, 2.2], L: 3 * HOLE, shaftZ: 0.77, stowedDir: [Math.cos(82 * Math.PI / 180), Math.sin(82 * Math.PI / 180)], back: HOLE, fwd: 2 * HOLE + 0.47, w: 32 / 25.4, armY: [[3.95, 4.45], [-4.45, -3.95]], wheels: { r: 0.77, y: [[4.45, 4.95], [-4.95, -4.45]] } },
  };
  const shaftAhead = m => m.pivot[0] + Math.sqrt(m.L * m.L - (m.pivot[1] - m.shaftZ) ** 2);
  const ARM = MOUNTS.tall, SHAFT_AHEAD = shaftAhead(ARM);
  const FACE_BEHIND_LIP = SHAFT_AHEAD + 0.53;
  const PIPE = 1.72, PIPE_R = 0.525, PIPE_Z = 4.25, HOLE_PLAY = 0.14;
  // the uprights' vee (the back wall for every ball), the ring and its hole, the tiles, and the collar's front bar as the
  // field CAD's centreline section has it: underside 3.90, top 4.25 where the pipes begin, inner edge at -1.69 (the opening
  // is 3.6 in across). Above 4.25 only the four pipes hold the stack (post(), in 3-D). COLLAR_Z lets a lower collar be tried.
  // Layer B measured from the field CAD: a plate from 3.90 up whose opening is 1.93 in in radius, with a ridge 1.71 in in
  // radius from 4.10 to 4.25 (the pipes' sockets sit on it). The corner a leaning ball rests on is the ridge's, at 4.25.
  const COLLAR_Z = 3.90, COLLAR_IN = -1.71, COLLAR_T = 0.35, COLLAR_LOW = -1.93, COLLAR_STEP = 4.10;
  const collarSegs = (cz, sg) => [[[sg * -2.41, cz], [sg * COLLAR_LOW, cz]], [[sg * COLLAR_LOW, cz], [sg * COLLAR_LOW, cz + 0.20]], [[sg * COLLAR_LOW, cz + 0.20], [sg * COLLAR_IN, cz + 0.20]], [[sg * COLLAR_IN, cz + 0.20], [sg * COLLAR_IN, cz + COLLAR_T]], [[sg * -2.41, cz + COLLAR_T], [sg * COLLAR_IN, cz + COLLAR_T]]];
  const FIXED = [
    [[BACK, 0], [BACK, PIPE_Z]], [[-1.54, 0], [BACK, 0]],
    [[-1.54, 0], [-1.54, 0.35]], [[-1.54, 0.35], [-2.36, 0.35]], [[-2.36, 0.35], [-2.36, 0]], [[-60, 0], [-2.36, 0]],
    ...collarSegs(COLLAR_Z, 1), ...collarSegs(COLLAR_Z, -1),
  ];
  const N_COLLAR = 10;
  // the two posts (the uprights' lobes) seen from above, from the field CAD: the point at (1.18, +-1.42), the slanted face
  // running inward to the inner corner (1.61, +-0.61), and the flat front face out to +-1.9. They stand from the ring's top to
  // the collar, and hold a ball sideways in a funnel: a ball at x 0.35 is pinned, one at x 0 can move +-0.65 before it meets a face.
  const LOBES = [];
  for (const sg of [1, -1]) { const c = [[1.184, 1.417], [1.66, 0.614], [2.463, 1.09], [1.987, 1.893]].map(([x, y]) => [x, y * sg]); for (let i = 0; i < 4; i++) LOBES.push([c[i], c[(i + 1) % 4]]); }
  // the collar's round opening holds a ball passing through it: where the ball's cross-section at the plate's height
  // reaches the opening's edge, the ball is pushed back toward the centre (the lower plate at 1.93 in, the ridge at 1.71)
  function ringHold(b, cz) {
    for (const [z0, z1, ro] of [[cz, cz + 0.20, -COLLAR_LOW], [cz + 0.20, cz + COLLAR_T, -COLLAR_IN]]) {
      if (b.z + R < z0 || b.z - R > z1) continue;
      const zc = b.z < z0 ? z0 : b.z > z1 ? z1 : b.z, dz = b.z - zc, rc = Math.sqrt(Math.max(0, R * R - dz * dz));
      const d = Math.hypot(b.x, b.y); if (d >= ro) continue;                  // the ball's centre is over the plate, not in the opening
      const room = ro - rc; if (d <= room || d < 1e-9) continue;
      const nx = -b.x / d, ny = -b.y / d; b.x = -nx * room; b.y = -ny * room;
      const vn = b.vx * nx + b.vy * ny; if (vn < 0) { b.vx -= vn * nx; b.vy -= vn * ny; }
    }
  }
  // a vertical wall segment in plan (x-y) between z0 and z1: push the ball out, with friction
  function wall(b, seg, z0, z1, e, mu) {
    if (b.z + R < z0 || b.z - R > z1) return;
    const ax = seg[0][0], ay = seg[0][1], ex = seg[1][0] - ax, ey = seg[1][1] - ay, L2 = ex * ex + ey * ey;
    let t = ((b.x - ax) * ex + (b.y - ay) * ey) / L2; t = t < 0 ? 0 : t > 1 ? 1 : t;
    const cx = ax + t * ex, cy = ay + t * ey, dx = b.x - cx, dy = b.y - cy, d = Math.hypot(dx, dy);
    if (d >= R || d < 1e-9) return;
    const nx = dx / d, ny = dy / d; b.x = cx + nx * R; b.y = cy + ny * R;
    const vn = b.vx * nx + b.vy * ny; if (vn >= 0) return;
    const jn = -(1 + (-vn > REST ? e : 0)) * vn; b.vx += jn * nx; b.vy += jn * ny;
    const tx = -ny, ty = nx, vt = b.vx * tx + b.vy * ty; let jt = -vt; const l = mu * jn; if (jt > l) jt = l; if (jt < -l) jt = -l;
    b.vx += jt * tx; b.vy += jt * ty;
  }
  // returns the normal impulse (0 if no contact), so the caller can tilt it sideways
  function contact(b, seg, sv, e, mu) {
    const ax = seg[0][0], az = seg[0][1], ex = seg[1][0] - ax, ez = seg[1][1] - az, L2 = ex * ex + ez * ez;
    let t = L2 < 1e-12 ? 0 : ((b.x - ax) * ex + (b.z - az) * ez) / L2; t = t < 0 ? 0 : t > 1 ? 1 : t;
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
  // the push needed to lift the stack with the bottom ball pinched between a face at `deg` from horizontal (the ball
  // rolling on it) and the uprights (sliding, mu): a multiple of the stack's weight; null when a steady push cannot do it
  function pushForce(deg, mu) { const a = deg * Math.PI / 180, d = Math.cos(a) - mu * Math.sin(a); return d <= 0 ? null : Math.sin(a) / d; }
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
    // cfg.stagger: the stack zigzagged front to back, balls 2 and 4 this much toward the robot (negative: toward the wall)
    const stagger = cfg.stagger || 0;
    // the stagger pattern: cfg.pattern as [dx2, dx3, dx4] in inches (toward the robot negative), or cfg.stagger for 2 and 4
    const pat = cfg.pattern || [k => (k % 2 ? -stagger : 0)][0];
    const balls = STAGED.map((z, k) => ({ x: Array.isArray(pat) ? (k === 0 ? 0 : pat[k - 1]) : pat(k), y: lean * k / 3, z, vx: 0, vy: 0, vz: 0, w: 0, u: 0 }));
    { const cz = cfg.collar || COLLAR_Z, segs = [...collarSegs(cz, 1), ...collarSegs(cz, -1)]; for (let i = 0; i < N_COLLAR; i++) FIXED[6 + i] = segs[i]; }
    let lip = -4.4, t = 0, step = 0, stalled = null;
    const out = [null, null, null, null], fed = [null, null, null, null], missed = [null, null, null, null], offBlade = [null, null, null, null], frames = [];
    const yAtWheels = [null, null, null, null];
    // the blade's frame: xb behind the lip along the blade's axis, yb across it, for a blade centred `off` and turned `yaw`
    const toBlade = b => { const dx = lip - b.x, dy = b.y - off; return [dx * cy + dy * sy, dy * cy - dx * sy]; };
    const xs = profile.map(p => p[0]);
    const tiltDeg = cfg.tilt || 0, shaftBack = cfg.shaftBack || 0.53, shaftZ = 0.77;
    let tStop = null, ramLip = null;
    // cfg.flap: a lifter hinged at the peak, lying along the tail, that swings up to `deg` over `time` s starting `delay` s
    // after the blade stops (a mechanism tripped by the posts): the tail's first `len` inches rise behind the ball
    const flapped = (prof, fl, f) => {
      const [xp, zp] = prof[2], tailA = Math.atan2(prof[3][1] - zp, prof[3][0] - xp), a = tailA + (fl.deg * Math.PI / 180 - tailA) * f;
      const L = fl.len, tip = [xp + L * Math.cos(a), zp + L * Math.sin(a)], foot = [xp + L * Math.cos(tailA), zp + L * Math.sin(tailA)];
      return [prof[0], prof[1], prof[2], tip, foot, ...prof.slice(3)];
    };
    // cfg.toe: a plate ahead of the lip, under the ball (the blade's thickness, lying on the posts' side), hinged at the
    // lip's foot, that swings up to `deg` over `time` s starting `delay` s after the stop: it lifts the ball's back and
    // pushes it toward the robot (a lifter tripped by the posts, on the right side of the ball)
    const toed = (prof, tp, f) => {
      const a = tp.deg * Math.PI / 180 * f, L = tp.len, [x0, z0] = prof[0];
      return [[x0 - L * Math.cos(a), z0 + L * Math.sin(a)], ...prof];
    };
    // cfg.finger: the blade's centre ahead of the shaft is a separate finger keyed to the shaft, with a toe `len` beyond the
    // lip; after the stop the shaft turns it nose-up by `deg` over `time` s (the body behind the shaft stays put)
    const fingered = (prof, fg, f) => {
      const a = fg.deg * Math.PI / 180 * f, c = Math.cos(a), sn = Math.sin(a);
      const pts = fg.len ? [[prof[0][0] - fg.len, prof[0][1] + (fg.rise || 0)], ...prof] : prof.slice();   // fg.rise: the toe's tip bent up
      // split the profile at the shaft's x so the part ahead of it rotates about the shaft
      const out = [];
      for (let i = 0; i < pts.length; i++) {
        const [x, z] = pts[i];
        if (i > 0) { const [x0, z0] = pts[i - 1]; if (x0 < shaftBack && x > shaftBack) out.push([shaftBack, z0 + (z - z0) * (shaftBack - x0) / (x - x0), true]); }
        out.push([x, z]);
      }
      const [px, pz] = fg.pivot || [shaftBack, shaftZ];                  // the hinge: the shaft, or any point of the profile
      return out.map(([x, z, split]) => { if (x > shaftBack + 1e-9) return [x, z]; const dx = x - px, dz = z - pz; return [px + dx * c + dz * sn, pz - dx * sn + dz * c]; });
    };
    const tilted = (prof, deg) => { const a = deg * Math.PI / 180, c = Math.cos(a), sn = Math.sin(a); return prof.map(([x, z]) => { const dx = x - shaftBack, dz = z - shaftZ; return [shaftBack + dx * c + dz * sn, shaftZ - dx * sn + dz * c]; }); };
    let toeNow = 0;                                                      // the toe's angle this step, degrees (0: lying flat)
    const snap = () => [+t.toFixed(3), +lip.toFixed(3), balls.map(b => [+b.x.toFixed(3), +b.z.toFixed(3), +b.y.toFixed(3)]), +toeNow.toFixed(1)];
    for (; t < tmax; t += DT, step++) {
      let driving = lip < stop && stalled === null;
      if (!driving && tStop === null) tStop = t;
      let bvSign = 1, speedNow = drive;
      if (cfg.ram && tStop !== null) {                                   // the re-ram: dwell, back off, then in again, faster
        const rm = cfg.ram, t1 = tStop + (rm.dwell || 0.3), t2 = t1 + rm.retreat / drive;
        if (t >= t1 && t < t2) { driving = true; bvSign = -1; }
        else if (t >= t2 && ramLip === null) ramLip = lip;
        if (ramLip !== null && lip < stop) { driving = true; speedNow = rm.speed || 12; }
      }
      let curProfile = tiltDeg && tStop !== null ? tilted(profile, tiltDeg * Math.min(1, (t - tStop) / 0.15)) : profile;
      if (cfg.finger && cfg.finger.advance) {                              // self-tripping: the posts stop the nose piece at the trip
        // point and the body drives `advance` further, turning the piece as it goes (f: the fraction of that advance done)
        const trip = STOP + (cfg.stopErr || 0), f = Math.max(0, Math.min(1, (lip - trip) / cfg.finger.advance));
        if (f > 0) { toeNow = cfg.finger.deg * f; curProfile = fingered(curProfile, cfg.finger, f); }
      } else if (cfg.finger && tStop !== null && t > tStop + (cfg.finger.delay || 0)) { const f = Math.min(1, (t - tStop - (cfg.finger.delay || 0)) / (cfg.finger.time || 0.3)); toeNow = cfg.finger.deg * f; curProfile = fingered(curProfile, cfg.finger, f); }
      if (cfg.toe && tStop !== null && t > tStop + (cfg.toe.delay || 0)) { const f = Math.min(1, (t - tStop - (cfg.toe.delay || 0)) / (cfg.toe.time || 0.3)); toeNow = cfg.toe.deg * f; curProfile = toed(curProfile, cfg.toe, f); }
      if (cfg.flap && tStop !== null && t > tStop + (cfg.flap.delay || 0)) curProfile = flapped(curProfile, cfg.flap, Math.min(1, (t - tStop - (cfg.flap.delay || 0)) / (cfg.flap.time || 0.3)));
      const bv = driving ? bvSign * speedNow : 0;
      if (driving) lip = bvSign > 0 ? Math.min(stop, lip + speedNow * DT) : lip - speedNow * DT;
      let axleX = 0, axleZ = 0;
      const fbl = cfg.faceBehindLip || (shaftAhead(MOUNTS[cfg.mount || "tall"]) + shaftBack);
      if (cfg.onRobot) { axleX = lip - fbl - GECKO_R; axleZ = GECKO_Z; }
      const touch = balls.map(() => false);
      for (const b of balls) {
        b.vz -= G * DT; b.x += b.vx * DT; b.y += b.vy * DT; b.z += b.vz * DT;
        for (const s of FIXED) contact(b, s, [0, 0], e, mu);
        // the blade under this ball: the profile raised by the lane's cross-section at the ball's y across the blade
        const [xb, yb] = toBlade(b);
        if (Math.abs(yb) <= hw) {
          const [h, slope] = cross(g, yb);
          const seg = [];
          const lead = g ? (g.lead || 0.4) : 0;
          const gs = g ? g.start + (g.oval || 0) * (1 - Math.sqrt(Math.max(0, 1 - (yb / hw) ** 2))) : 0;
          const taper = x => { if (!g) return 0; return x <= gs ? 0 : x >= gs + lead ? 1 : (x - gs) / lead; };
          const pts = [];
          for (let i = 0; i < curProfile.length; i++) {
            const [x0, z0] = curProfile[i];
            pts.push([x0, z0 + (i === 0 ? 0 : h * taper(x0))]);
            if (g && i + 1 < curProfile.length) {                        // insert the lane's start and full-depth points on this segment
              const [x1, z1] = curProfile[i + 1];
              for (const xq of [gs, gs + lead]) if (xq > x0 && xq < x1) pts.push([xq, z0 + (z1 - z0) * (xq - x0) / (x1 - x0) + h * taper(xq)]);
            }
          }
          for (let i = 0; i + 1 < pts.length; i++) seg.push([[lip - pts[i][0], pts[i][1]], [lip - pts[i + 1][0], pts[i + 1][1]]]);
          let jsum = 0, tp = 0;
          for (const s of seg) { const j = contact(b, s, [bv, 0], e, mu); if (j > 0) { jsum += j; tp = Math.max(tp, taper(xb)); } }
          if (jsum > 0) { touch[balls.indexOf(b)] = true; if (slope !== 0 && tp > 0) b.vy -= jsum * slope * tp / (1 + K); }
          if (jsum > 0 && yaw !== 0) { /* a turned blade carries the ball along its own axis: nothing more to do, the lane's push above already follows it */ }
        }
        if (cfg.pusher) {
          const pk = cfg.pusher, px = lip - fbl + pk.reach;
          if (pk.hw) {                                                     // a plate of finite width: a box, hit in 3-D
            const yc = off, x0 = px - 2, x1 = px, y0 = yc - pk.hw, y1 = yc + pk.hw;
            const cx = Math.min(Math.max(b.x, x0), x1), cy = Math.min(Math.max(b.y, y0), y1), cz = Math.min(Math.max(b.z, pk.z0), pk.z1);
            const dx = b.x - cx, dy = b.y - cy, dz = b.z - cz, d = Math.hypot(dx, dy, dz);
            if (d < R && d > 1e-9) {
              const nx = dx / d, ny = dy / d, nz = dz / d; b.x = cx + nx * R; b.y = cy + ny * R; b.z = cz + nz * R;
              const vn = (b.vx - bv) * nx + b.vy * ny + b.vz * nz;
              if (vn < 0) { const jn = -(1 + (-vn > REST ? e : 0)) * vn; b.vx += jn * nx; b.vy += jn * ny; b.vz += jn * nz;
                const tvx = (b.vx - bv) - ((b.vx - bv) * nx + b.vy * ny + b.vz * nz) * nx, tvy = b.vy - ((b.vx - bv) * nx + b.vy * ny + b.vz * nz) * ny, tvz = b.vz - ((b.vx - bv) * nx + b.vy * ny + b.vz * nz) * nz;
                const tm = Math.hypot(tvx, tvy, tvz); if (tm > 1e-9) { const jt = Math.min(mu * jn, tm); b.vx -= jt * tvx / tm; b.vy -= jt * tvy / tm; b.vz -= jt * tvz / tm; } }
            }
          } else { contact(b, [[px, pk.z0], [px, pk.z1]], [bv, 0], e, mu); contact(b, [[px - 2, pk.z1], [px, pk.z1]], [bv, 0], e, mu); }
        }
        // the FLOWER holds the balls sideways: the bottom one in the ring's hole and the uprights' vee, the rest between the pipes
        // the ring's hole (radius 1.54): a ball whose bottom is below the ring's top can only move sideways as far as the
        // hole's radius minus the ball's cross-section at the ring's height allows (the hole holds a ball at its centre to
        // +-0.14 in, one 0.61 in off centre not at all); the uprights' vee holds a ball against the back the same +-0.14
        let play = Infinity;
        if (b.z - R < 0.35 && Math.abs(b.x) < 1.54) { const dz = 0.35 - b.z, rc = dz < -R || dz > R ? 0 : Math.sqrt(Math.max(0, R * R - dz * dz)); const room = 1.54 - rc; play = room > 0 ? Math.sqrt(Math.max(0, room * room - b.x * b.x)) : 0; play = Math.max(play, 0); }
        if (play < Infinity) { if (b.y > play) { b.y = play; if (b.vy > 0) b.vy = 0; } if (b.y < -play) { b.y = -play; if (b.vy < 0) b.vy = 0; } }
        for (const px of [PIPE, -PIPE]) for (const py of [PIPE, -PIPE]) post(b, px, py, PIPE_R, PIPE_Z, e, mu);
        for (const sgm of LOBES) wall(b, sgm, 0.35, 4.10, e, mu);
        ringHold(b, cfg.collar || COLLAR_Z);
        if (cfg.onRobot) { const [, yb] = toBlade(b); if (Math.abs(yb) <= mouth / 2 + 0.6) geckoDrag(b, axleX, axleZ); }
      }
      for (let i = 0; i < 4; i++) for (let j = i + 1; j < 4; j++) {
        const a = balls[i], c = balls[j], dx = c.x - a.x, dy = c.y - a.y, dz = c.z - a.z, d = Math.hypot(dx, dy, dz);
        if (d > 1e-9 && d < 2 * R) {
          const nx = dx / d, ny = dy / d, nz = dz / d, push = (2 * R - d) / 2;
          a.x -= nx * push; a.y -= ny * push; a.z -= nz * push; c.x += nx * push; c.y += ny * push; c.z += nz * push;
          const vn = (c.vx - a.vx) * nx + (c.vy - a.vy) * ny + (c.vz - a.vz) * nz;
          if (vn < 0) {
            const jj = -(1 + (-vn > REST ? e : 0)) * vn / 2; a.vx -= jj * nx; a.vy -= jj * ny; a.vz -= jj * nz; c.vx += jj * nx; c.vy += jj * ny; c.vz += jj * nz;
            // friction between the balls (cfg.muBall, default the same mu): the surfaces' relative slip in the x-z plane,
            // spins included, resisted up to mu times the normal impulse; it is what lets a ball above hold the one below
            const muB = cfg.muBall ?? mu, tx = -nz, tz = nx;
            const slip = (c.vx - a.vx) * tx + (c.vz - a.vz) * tz - (a.w + c.w) * R;
            let jt = -slip / (2 * (1 + 1 / K));
            // and across (y): the balls' sideways slip at the contact, resisted by the same friction, so a ball cannot be
            // squeezed out sideways from under a stack it is holding up
            const slipY = (c.vy - a.vy) - ((c.vx - a.vx) * nx + (c.vz - a.vz) * nz) * ny;
            let jy = -slipY / 2;
            const lim = muB * 2 * jj, mag = Math.hypot(jt, jy); if (mag > lim) { jt *= lim / mag; jy *= lim / mag; }
            a.vx -= jt * tx; a.vz -= jt * tz; c.vx += jt * tx; c.vz += jt * tz; a.w -= jt / (K * R); c.w -= jt / (K * R);
            a.vy -= jy; c.vy += jy;
          }
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
  return { run, mountain, cross, catchHalf, pushForce, STOP, R, GECKO_R, GECKO_Z, FACE_BEHIND_LIP, SHAFT_AHEAD, ARM, MOUNTS, shaftAhead, PIPE, PIPE_R };
})();
module.exports = window.FlowerSim;
