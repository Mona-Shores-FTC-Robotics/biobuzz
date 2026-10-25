<script>
(function () {
  const S = window.FlowerSim, FS = __FLOWER__, RS = __ROBOT__;
  const PX = 52, X0 = -10.0, ZTOP = 8.4;
  const sx = x => (x - X0) * PX, sy = z => (ZTOP - z) * PX;
  const svg = document.getElementById("sim-svg"), NS = "http://www.w3.org/2000/svg";
  const el = (tag, attrs, parent) => { const e = document.createElementNS(NS, tag); for (const k in attrs) e.setAttribute(k, attrs[k]); (parent || svg).appendChild(e); return e; };
  const segPath = segs => segs.map(([a, b]) => `M${sx(a[0]).toFixed(1)},${sy(a[1]).toFixed(1)}L${sx(b[0]).toFixed(1)},${sy(b[1]).toFixed(1)}`).join("");
  // the field: tiles and the wall (not in the FLOWER's CAD), then the FLOWER's own sections from FIRST's STEP
  el("rect", { x: sx(X0), y: sy(0), width: (3.4 - X0) * PX, height: 0.3 * PX, fill: "var(--steel)" });
  el("rect", { x: sx(2.71), y: sy(ZTOP), width: 0.69 * PX, height: (ZTOP + 0.3) * PX, fill: "var(--steel)" });
  el("path", { d: segPath(FS.pipe), stroke: "var(--field)", "stroke-width": 1.2, fill: "none", opacity: 0.5 });
  el("path", { d: segPath(FS.support), stroke: "var(--muted)", "stroke-width": 1.2, fill: "none", opacity: 0.7 });
  el("path", { d: segPath(FS.ring), stroke: "var(--fg)", "stroke-width": 1.4, fill: "none" });
  el("path", { d: segPath(FS.layerB), stroke: "var(--fg)", "stroke-width": 1.4, fill: "none" });
  el("line", { x1: sx(S.STOP), x2: sx(S.STOP), y1: sy(-0.3), y2: sy(1.7), stroke: "var(--petg)", "stroke-dasharray": "3 3" });
  el("text", { x: sx(S.STOP), y: sy(-0.22), "font-size": 10, "text-anchor": "middle", fill: "#fff" }).textContent = "stop";
  el("text", { x: sx(-2.0), y: sy(5.5), class: "lbl", "font-size": 10, "text-anchor": "end" }).textContent = "bracket";
  el("text", { x: sx(1.25), y: sy(4.3), class: "lbl", "font-size": 10 }).textContent = "uprights";
  el("text", { x: sx(-1.9), y: sy(7.9), class: "lbl", "font-size": 10, "text-anchor": "end" }).textContent = "pipe (beside)";
  // the robot: its centreline section from the mentor's STEP, moved with the blade
  // the robot's section is in inches behind its face; in the FLOWER frame that is face - behind, so mirror it and
  // translate the group to the face each frame
  const robotG = el("g", {});
  const mirror = segs => segs.map(([a, b]) => [[-a[0], a[1]], [-b[0], b[1]]]);
  el("path", { d: segPath(mirror(RS.centre)), stroke: "var(--muted)", "stroke-width": 1.1, fill: "none", opacity: 0.8 }, robotG);
  el("path", { d: segPath(mirror(RS.gecko)), stroke: "var(--fg)", "stroke-width": 1.4, fill: "none", opacity: 0.9 }, robotG);
  const gecko = el("circle", { fill: "var(--steel)", "fill-opacity": 0.35, stroke: "var(--fg)", "stroke-width": 1.2 });
  const spoke = el("line", { stroke: "var(--fg)", "stroke-width": 2 });
  const arm = el("line", { stroke: "var(--muted)", "stroke-width": 5, "stroke-linecap": "round" });
  const sledWheel = el("circle", { fill: "none", stroke: "var(--fg)", "stroke-width": 2 });
  const blade = el("polygon", { fill: "var(--petg)" });
  const toeEl = el("polygon", { fill: "var(--petg)", stroke: "var(--fg)", "stroke-width": 0.8 });
  const shaft = el("circle", { r: 0.16 * PX, fill: "var(--steel)", stroke: "var(--fg)" });
  const balls = [0, 1, 2, 3].map(() => el("circle", { r: S.R * PX, fill: "var(--pollen)", "fill-opacity": 0.75, stroke: "var(--fg)", "stroke-width": 1.2 }));
  // the plan view (from above): half scale, under the side view; x as above, y across the field (+y up the screen)
  const P2 = PX / 2, Y0 = 452 + 8, YC = Y0 + 4.5 * P2;
  const px2 = x => (x - X0) * P2, py2 = y => YC - y * P2;
  el("line", { x1: 0, x2: 697, y1: Y0 - 4, y2: Y0 - 4, stroke: "var(--line)" });
  el("text", { x: 8, y: Y0 + 12, class: "lbl", "font-size": 10 }).textContent = "from above";
  el("rect", { x: px2(2.71), y: py2(4.5), width: 0.69 * P2, height: 9 * P2, fill: "var(--steel)" });
  el("circle", { cx: px2(0), cy: py2(0), r: 2.36 * P2, fill: "none", stroke: "var(--fg)", "stroke-width": 1.2 });
  el("circle", { cx: px2(0), cy: py2(0), r: 1.54 * P2, fill: "none", stroke: "var(--fg)", "stroke-width": 0.8, "stroke-dasharray": "2 2" });
  for (const sx_ of [S.PIPE, -S.PIPE]) for (const sy_ of [S.PIPE, -S.PIPE]) el("circle", { cx: px2(sx_), cy: py2(sy_), r: S.PIPE_R * P2, fill: "var(--field)", "fill-opacity": 0.5, stroke: "var(--fg)", "stroke-width": 0.8 });
  for (const sg of [1, -1]) el("polygon", { points: [[1.18, 1.42 * sg], [1.61, 0.61 * sg], [2.4, 0.61 * sg], [2.4, 1.9 * sg], [1.18, 1.9 * sg]].map(([x, y]) => px2(x) + "," + py2(y)).join(" "), fill: "var(--muted)", "fill-opacity": 0.35, stroke: "var(--fg)", "stroke-width": 0.8 });
  const planG = el("g", {});                                            // the blade and the robot's front, moved and turned with the blade
  const planBlade = el("polygon", { fill: "var(--petg)", "fill-opacity": 0.85 }, planG);
  const planLane = el("polygon", { fill: "#fff", "fill-opacity": 0.35 }, planG);
  const planFace = el("line", { stroke: "var(--muted)", "stroke-width": 1.2 }, planG);
  const planMouth = el("rect", { fill: "var(--steel)", "fill-opacity": 0.35, stroke: "var(--fg)", "stroke-width": 1 }, planG);
  const planBalls = [0, 1, 2, 3].map(k => el("circle", { r: S.R * P2, fill: "var(--pollen)", "fill-opacity": 0.35 + 0.15 * (3 - k), stroke: "var(--fg)", "stroke-width": 0.9 }));
  const $ = id => document.getElementById(id);
  const PRESETS = {
    blockA: { name: "Block A, hand sled", profile: [[0, 0.52], [0, 0.64], [0.2, 1.30], [0.7, 1.30], [1.4, 0.52]], extraDepth: 0.57, shaftBack: 0.93, onRobot: false, face: 17, width: 5.44 },
    v2: { name: "v2 lane", profile: [[0, 0.52], [0, 0.60], [0.45, 1.05], [0.90, 0.99], [1.50, 0.52]], shaftBack: 0.69, onRobot: true },
    v3: { name: "v3 mountain", shape: { edge: 0.53, nose: 55, peak: 1.28, tail: 45 }, shaftBack: 0.77, onRobot: true },
    v4: { name: "v4 carry", shape: { edge: 0.53, nose: 55, peak: 1.28, tail: 14.4 }, shaftBack: 0.77, onRobot: true },
  };
  const sliders = ["edge", "nose", "peak", "tail", "drive", "mu", "stopErr", "laneR", "laneRise", "laneStart", "laneOval", "offset", "yaw", "mouth"];
  // how the stack sits in the FLOWER before the blade arrives. Front to back: the second ball has rolled toward the robot until
  // it rests on the collar's front bar (0.29 in off centre is where it meets the bar's inner edge) and the two above sit back
  // against the uprights, the way the balls settle in our photos. Side to side: the stack leans 0.6 in across, into the gaps
  // between the pipes. Straight: all four on the centreline.
  const STACKS = { fb: { pattern: [-0.29, 0.35, 0.35] }, straight: { pattern: [0, 0, 0] }, ss: { lean: 0.6 } };
  let stack = "fb";
  // the lifter: a plate ahead of the lip under the ball, the blade's thickness, that swings up about the lip's foot when the
  // blade stops on the posts (a mechanism the posts trip), pushing the ball toward the robot
  // v10: the nose piece on a 4 mm pin 0.32 in behind the lip, 0.96 in up, with a 1.3 in toe; the posts push the lip below the
  // pin, so it turns nose-up as the body drives 0.22 in further, to 54 deg where the lip's corner reaches the pin's height
  const TOES = { none: null, lifter: { len: 1.3, deg: 54, advance: 0.22, pivot: [0.32, 0.96] } };
  // the nose piece's outline, in inches behind the lip and up: toe, lip, face, hub, heel
  const NOSE_POLY = [[-1.3, 0.42], [-1.3, 0.52], [0, 0.52], [0, 0.6], [0.29, 1.1], [0.32, 1.11], [0.45, 1.0], [0.45, 0.52], [0.05, 0.52], [0.05, 0.42]];
  let toe = "none";
  function readLane() { const rise = +$("s-laneRise").value; return rise > 0 ? { R: +$("s-laneR").value, rise, start: +$("s-laneStart").value, lead: 1.6, oval: +$("s-laneOval").value } : null; }
  function setLane(l) { $("s-laneRise").value = l ? l.rise : 0; if (l) { $("s-laneR").value = l.R; $("s-laneStart").value = l.start; $("s-laneOval").value = l.oval || 0; } }
  let bladeWidth = 5.4, armMode = "both";
  let preset = "v4", custom = false, result = null, i = 0, playing = false, slow = false, acc = 0, last = null, profile = null, cfg = null;
  function readShape() { return { edge: +$("s-edge").value, nose: +$("s-nose").value, peak: +$("s-peak").value, tail: +$("s-tail").value }; }
  function setShape(sh) { for (const k in sh) $("s-" + k).value = sh[k]; }
  function readouts() {
    const sh = readShape(), P = PRESETS[preset];
    const prof = custom || P.shape ? S.mountain(sh) : P.profile;
    const peakX = prof.reduce((m, p) => p[1] > m[1] ? p : m, prof[0]);
    const margin = (S.STOP + (P.extraDepth || 0)) - peakX[0] - 0.35;
    const edge = prof[1][1], alpha = Math.asin((S.R - edge) / S.R) * 180 / Math.PI;
    const tailEnd = (S.STOP + (P.extraDepth || 0)) - prof[prof.length - 1][0];
    if (P.face && !custom) {
      $("ro-margin").textContent = "none: its curved tip passes the uprights' points and stops 0.57 in deeper, with the ball over the ramp's foot"; $("ro-margin").style.color = "var(--petg)";
      $("ro-edge").textContent = "the face, leaning " + P.face + "°: the push is tilted up " + P.face + "° (stalls above μ " + Math.tan(P.face * Math.PI / 180).toFixed(2) + ")";
    } else {
      $("ro-margin").textContent = margin.toFixed(2) + " in" + (margin < 0.15 ? " (too little: the ball may sit on the peak)" : margin < 0.3 ? " (tight)" : "");
      $("ro-margin").style.color = margin < 0.15 ? "var(--petg)" : "";
      $("ro-edge").textContent = alpha.toFixed(0) + "° (stalls above μ " + Math.tan(alpha * Math.PI / 180).toFixed(2) + ")";
    }
    $("ro-tail").textContent = tailEnd.toFixed(2) + " in " + (tailEnd < -2.36 ? "(past the ring: no dip)" : tailEnd < -1.54 ? "(over the ring)" : "(into the hole: the ball dips)");
    $("ro-width").textContent = bladeWidth.toFixed(1) + " in: the nose meets both uprights' points up to ±" + (bladeWidth / 2 - 1.42).toFixed(1) + " in off centre" + (bladeWidth > 7.5 ? "; wider than the Gecko row (7.5)" : bladeWidth >= 7.5 ? "; matches the Gecko row" : "; the Gecko row is 7.5");
    const noseDeg = P.face && !custom ? 90 - P.face : (custom || P.shape ? sh.nose : 45);
    const muP = +$("s-mu").value, fFace = S.pushForce(noseDeg, muP), fEdge = S.pushForce(90 - alpha, muP);
    $("ro-push").textContent = (fFace === null ? "a steady push cannot lift the ball on this face (it stalls)" : fFace.toFixed(1) + " × the stack's weight on the face") + (fEdge === null ? "; stalls at first contact" : ", " + fEdge.toFixed(1) + " × at first contact") + " (friction " + muP + ", ball rolling on the blade)";
    $("ro-push").style.color = fFace === null || fFace > 3 ? "var(--petg)" : "";
    const g = readLane();
    if (!g) $("ro-lane").textContent = "none: a plain tail";
    else { const hw = (P.width || bladeWidth) / 2, c0 = S.catchHalf(g), c = Math.min(c0, hw), v = Math.sqrt(2 * 386 * (c0 >= hw ? g.R - Math.sqrt(g.R * g.R - hw * hw) : g.rise) / (1 + 2 / 3)); $("ro-lane").textContent = (c0 >= hw ? "one curve across the whole blade, " + (g.R - Math.sqrt(g.R * g.R - hw * hw)).toFixed(2) + " in up at the edges" : "curves across ±" + c.toFixed(1) + " in, flat outside that") + "; holds a sideways kick up to " + v.toFixed(0) + " in/s" + (g.start < 0.85 ? ". Starts too near the nose: the FLOWER still holds the ball on its own centreline there, so an off-centre robot wedges it on the wall" : ""); }
    $("ro-lane").style.color = g && g.start < 0.85 ? "var(--petg)" : "";
    for (const k of sliders) { const o = $("v-" + k); if (o) o.textContent = $("s-" + k).value; }
    return prof;
  }
  function compute() {
    const P = PRESETS[preset];
    profile = readouts();
    cfg = { drive: +$("s-drive").value, mu: +$("s-mu").value, stopErr: +$("s-stopErr").value, extraDepth: P.extraDepth || 0, onRobot: P.onRobot,
      width: P.width || bladeWidth, gutter: readLane(), offset: +$("s-offset").value, yaw: +$("s-yaw").value, mouth: +$("s-mouth").value, muBall: 0.4 };
    Object.assign(cfg, STACKS[stack]); if (TOES[toe]) { cfg.finger = TOES[toe]; cfg.extraDepth += TOES[toe].advance; }
    const t0 = performance.now();
    result = S.run(profile, cfg);
    const ms = Math.round(performance.now() - t0);
    const r = result, name = custom ? "custom mountain" : P.name;
    let s;
    if (r.stalled) s = "Stalled at " + r.stalled[0].toFixed(2) + " s: the push at the blade contact is tilted up only " + r.stalled[1].toFixed(0) + "°, under the friction angle " + (Math.atan(cfg.mu) * 180 / Math.PI).toFixed(0) + "°. A steady push cannot lift the ball.";
    else if (cfg.onRobot && r.fed.every(v => v !== null)) { const w = Math.max(0, ...r.yAtWheels.filter(v => v !== null).map(Math.abs)); s = "All four into the wheels at " + Math.max(...r.fed).toFixed(2) + " s (first out of the FLOWER at " + (r.out[0] === null ? "?" : r.out[0].toFixed(2)) + " s)" + (w > 0.05 ? "; the furthest reached the wheels " + w.toFixed(1) + " in off the blade's centre." : "."); }
    else if (cfg.onRobot && r.missed.some(v => v !== null)) s = r.missed.filter(v => v !== null).length + " of 4 rolled past the wheels, off to the side (" + r.yAtWheels.filter(v => v !== null).map(v => v.toFixed(1)).join(", ") + " in from the blade's centre).";
    else if (cfg.onRobot) {
      const end = r.frames[r.frames.length - 1][2], left = 4 - r.fed.filter(v => v !== null).length;
      const onPeak = end.some(b => b[0] > 0.1 && b[0] < 0.7 && b[1] > 2.1 && b[1] < 2.8), onBar = end.some(b => b[0] < -0.3 && b[0] > -1.0 && b[1] > 4.6 && b[1] < 5.5);
      s = left + " of 4 never reached the wheels" + (onPeak && onBar ? ": the stack has locked itself. The bottom ball sits on the peak, pinned against the posts, and the ball above it leans out over the collar's front bar and holds it there. This is what our October 23 photos show." : onPeak ? ": the bottom ball sits on the peak, pinned against the posts." : ": stuck on the blade after " + r.frames[r.frames.length - 1][0].toFixed(1) + " s.");
    }
    else if (r.out.every(v => v !== null)) s = "All four out of the FLOWER, the last at " + Math.max(...r.out).toFixed(2) + " s.";
    else s = (4 - r.out.filter(v => v !== null).length) + " of 4 still in the FLOWER after " + r.frames[r.frames.length - 1][0].toFixed(1) + " s.";
    const stackName = { fb: "stack leaning front to back", straight: "straight stack", ss: "stack leaning side to side" }[stack];
    $("sim-result").textContent = name + (toe === "none" ? "" : " with the self-tripping nose") + ", " + stackName + ", " + cfg.drive + " in/s, friction " + cfg.mu + (cfg.stopErr ? ", stop " + (cfg.stopErr > 0 ? "+" : "") + cfg.stopErr + " in" : "") + ". " + s + " (" + ms + " ms)";
    i = 0; acc = 0; $("sim-scrub").max = r.frames.length - 1; $("sim-scrub").value = 0;
    playing = true; $("sim-play").textContent = "Pause";
    if (window.Drive3D) window.Drive3D.setBlade(profile, P.width || bladeWidth, P.shaftBack, cfg.gutter, cfg.offset, cfg.yaw, P.bosses || null, P.vee || 0, cfg.finger ? cfg.finger.pivot[0] : 0);
    draw();
  }
  function draw() {
    if (!result) return;
    const f = result.frames[i], t = f[0], lip = f[1], P = PRESETS[preset];
    if (cfg.finger) {                                                   // the body behind the pin, and the nose piece turned about it
      const px = cfg.finger.pivot[0], pz = cfg.finger.pivot[1], zAt = x => { for (let i = 0; i + 1 < profile.length; i++) { const [x0, z0] = profile[i], [x1, z1] = profile[i + 1]; if (x >= x0 && x <= x1) return z0 + (z1 - z0) * (x - x0) / (x1 - x0); } return profile[0][1]; };
      const body = [[px, profile[0][1]], [px, zAt(px)], ...profile.filter(p => p[0] > px)];
      blade.setAttribute("points", body.map(([x, z]) => sx(lip - x) + "," + sy(z)).join(" "));
      const a = (f[3] || 0) * Math.PI / 180, c = Math.cos(a), s_ = Math.sin(a);
      const pts = NOSE_POLY.map(([x, z]) => { const dx = x - px, dz = z - pz; return [px + dx * c + dz * s_, pz - dx * s_ + dz * c]; });
      toeEl.setAttribute("points", pts.map(([x, z]) => sx(lip - x) + "," + sy(z)).join(" ")); toeEl.style.display = "";
    } else { blade.setAttribute("points", profile.map(([x, z]) => sx(lip - x) + "," + sy(z)).join(" ")); toeEl.style.display = "none"; }
    const shx = lip - P.shaftBack;
    shaft.setAttribute("cx", sx(shx)); shaft.setAttribute("cy", sy(0.77));
    if (P.onRobot) {
      const face = lip - S.FACE_BEHIND_LIP, ax = face - S.GECKO_R, az = S.GECKO_Z;
      robotG.setAttribute("transform", `translate(${(face * PX).toFixed(1)} 0)`);
      gecko.setAttribute("cx", sx(ax)); gecko.setAttribute("cy", sy(az)); gecko.setAttribute("r", S.GECKO_R * PX);
      const a = t * 40 / S.GECKO_R;
      spoke.setAttribute("x1", sx(ax)); spoke.setAttribute("y1", sy(az)); spoke.setAttribute("x2", sx(ax + S.GECKO_R * Math.cos(a))); spoke.setAttribute("y2", sy(az + S.GECKO_R * Math.sin(a)));
      arm.setAttribute("x1", sx(ax)); arm.setAttribute("y1", sy(az)); arm.setAttribute("x2", sx(shx)); arm.setAttribute("y2", sy(0.77));
      robotG.style.display = gecko.style.display = spoke.style.display = ""; sledWheel.style.display = "none";
    } else {
      robotG.style.display = gecko.style.display = spoke.style.display = "none";
      arm.setAttribute("x1", sx(X0)); arm.setAttribute("y1", sy(0.77)); arm.setAttribute("x2", sx(shx)); arm.setAttribute("y2", sy(0.77));
      sledWheel.setAttribute("cx", sx(shx)); sledWheel.setAttribute("cy", sy(0.77)); sledWheel.setAttribute("r", 0.77 * PX); sledWheel.style.display = "";
    }
    f[2].forEach(([x, z, y], k) => { balls[k].setAttribute("cx", sx(x)); balls[k].setAttribute("cy", sy(z)); planBalls[k].setAttribute("cx", px2(x)); planBalls[k].setAttribute("cy", py2(y || 0)); });
    // the plan: the blade's outline and lane, the robot's face and wheel row, all in the blade's frame (offset, turned)
    const W = cfg.width || bladeWidth, len = profile[profile.length - 1][0], g = cfg.gutter, c = g ? S.catchHalf(g) : 0;
    planG.setAttribute("transform", `translate(${px2(lip).toFixed(1)} ${py2(cfg.offset || 0).toFixed(1)}) rotate(${(-(cfg.yaw || 0)).toFixed(1)})`);
    const pl = (xb, y) => (-xb * P2).toFixed(1) + "," + (-y * P2).toFixed(1);
    const V = P.vee || 0;                                              // the V nose from above: the sides sit V behind the middle's lip
    planBlade.setAttribute("points", (V ? [pl(V, -W / 2), pl(V, -1.42), pl(0, -0.855), pl(0, 0.855), pl(V, 1.42), pl(V, W / 2)] : [pl(0, -W / 2), pl(0, W / 2)]).concat([pl(len, W / 2), pl(len, -W / 2)]).join(" "));
    planLane.setAttribute("points", g ? [pl(g.start, -0.05), pl(g.start, 0.05), pl(g.start + 1.6, c), pl(len, c), pl(len, -c), pl(g.start + 1.6, -c)].join(" ") : "");
    if (P.onRobot) {
      const fb = S.FACE_BEHIND_LIP, m = cfg.mouth || 7.5;
      planFace.setAttribute("x1", -fb * P2); planFace.setAttribute("x2", -fb * P2); planFace.setAttribute("y1", -8 * P2); planFace.setAttribute("y2", 8 * P2);
      planMouth.setAttribute("x", -(fb + 2 * S.GECKO_R) * P2); planMouth.setAttribute("y", -m / 2 * P2); planMouth.setAttribute("width", 2 * S.GECKO_R * P2); planMouth.setAttribute("height", m * P2);
      planFace.style.display = planMouth.style.display = "";
    } else planFace.style.display = planMouth.style.display = "none";
    if (window.Drive3D) window.Drive3D.frame(lip, f[2], P.onRobot, P.shaftBack, t, cfg.finger ? [f[3] || 0, cfg.finger.pivot, NOSE_POLY] : null);
    $("sim-clock").textContent = t.toFixed(2) + " s";
    $("sim-scrub").value = i;
  }
  function tick(now) {
    if (playing && result) {
      if (last !== null) acc += (now - last) * (slow ? 0.3 : 1);
      last = now;
      while (acc >= 10 && i < result.frames.length - 1) { acc -= 10; i++; }
      if (i >= result.frames.length - 1) { playing = false; $("sim-play").textContent = "Replay"; }
      draw();
    } else last = now;
    requestAnimationFrame(tick);
  }
  function choose(k) {
    preset = k; custom = false;
    for (const b of document.querySelectorAll("[data-preset]")) b.setAttribute("aria-pressed", b.dataset.preset === k);
    const P = PRESETS[k];
    if (P.shape) setShape(P.shape);
    if ("lane" in P) setLane(P.lane);
    for (const s of ["edge", "nose", "peak", "tail"]) $("s-" + s).disabled = !P.shape;
    compute();
  }
  for (const b of document.querySelectorAll("[data-preset]")) b.onclick = () => choose(b.dataset.preset);
  let timer = null;
  const later = () => { clearTimeout(timer); timer = setTimeout(compute, 120); };
  for (const s of ["edge", "nose", "peak", "tail"]) $("s-" + s).oninput = () => { custom = true; for (const b of document.querySelectorAll("[data-preset]")) b.setAttribute("aria-pressed", "false"); readouts(); later(); };
  for (const s of ["drive", "mu", "stopErr", "laneR", "laneRise", "laneStart", "laneOval", "offset", "yaw", "mouth"]) $("s-" + s).oninput = () => { readouts(); later(); };
  for (const b of document.querySelectorAll("[data-toe]")) b.onclick = () => { toe = b.dataset.toe; for (const o of document.querySelectorAll("[data-toe]")) o.setAttribute("aria-pressed", o === b); compute(); };
  for (const b of document.querySelectorAll("[data-stack]")) b.onclick = () => { stack = b.dataset.stack; for (const o of document.querySelectorAll("[data-stack]")) o.setAttribute("aria-pressed", o === b); compute(); };
  for (const b of document.querySelectorAll("[data-width]")) b.onclick = () => { bladeWidth = +b.dataset.width; for (const o of document.querySelectorAll("[data-width]")) o.setAttribute("aria-pressed", o === b); readouts(); later(); };
  for (const b of document.querySelectorAll("[data-arms]")) b.onclick = () => { armMode = b.dataset.arms; for (const o of document.querySelectorAll("[data-arms]")) o.setAttribute("aria-pressed", o === b); if (window.Drive3D) window.Drive3D.setArms(armMode); };
  $("sim-run").onclick = compute;
  $("sim-play").onclick = function () { if (!result) return; if (i >= result.frames.length - 1) { i = 0; acc = 0; } playing = !playing; this.textContent = playing ? "Pause" : "Play"; };
  $("sim-slow").onclick = function () { slow = !slow; this.setAttribute("aria-pressed", slow); };
  $("sim-scrub").oninput = function () { if (!result) return; playing = false; $("sim-play").textContent = "Play"; i = +this.value; draw(); };
  choose("v4");
  requestAnimationFrame(tick);
})();
</script>
