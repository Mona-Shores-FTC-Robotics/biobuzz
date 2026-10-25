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
  const pokerEl = el("rect", { fill: "#e0552b", stroke: "var(--fg)", "stroke-width": 1 }, robotG);
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
  // the three blades: the same mountain (60 deg nose from a 0.60 in edge, peak 1.10 in, 12 deg tail) with Carter's lane
  const SHAPE = { edge: 0.6, nose: 60, peak: 1.1, tail: 12 }, LANE = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6, oval: 0 };
  const PRESETS = {
    v7: { name: "v7", shaftBack: 0.53 },
    v8: { name: "v8, the V nose", shaftBack: 0.75, extraDepth: 0.3, vee: 0.3 },
    v10: { name: "v10, the nose that trips itself", shaftBack: 0.53, extraDepth: 0.22, finger: { len: 0, deg: 54, advance: 0.22, pivot: [0.32, 0.96] } },
  };
  // how the stack sits before the blade arrives: front to back (the second ball against the collar, the way our photos
  // show it), straight, or leaning side to side into the gaps between the pipes
  const STACKS = { fb: { pattern: [-0.29, 0.35, 0.35] }, straight: { pattern: [0, 0, 0] }, ss: { lean: 0.6 } };
  const NOSE_POLY = [[0, 0.52], [0, 0.6], [0.29, 1.1], [0.32, 1.11], [0.45, 1.0], [0.45, 0.52]];   // v10's nose piece: lip, face, hub, heel
  // the poker: a fixed plate on the robot's frame, 1.2 in ahead of the intake face, 5.3 to 7.5 in up, 1.4 in wide, that
  // meets the second ball as the lip meets the posts and pushes it back off the collar's ridge
  const POKER = { reach: 1.2, z0: 5.3, z1: 7.5, hw: 0.375 };   // 0.75 in wide: clears the pipes to 0.8 in off-centre
  let preset = "v7", stack = "fb", push = 4, stopErr = -0.35, poker = false;
  let result = null, i = 0, playing = false, slow = false, acc = 0, last = null, profile = null, cfg = null;
  function compute() {
    const P = PRESETS[preset];
    profile = S.mountain(SHAPE);
    cfg = { drive: push, mu: 0.4, stopErr, extraDepth: P.extraDepth || 0, onRobot: true, width: 5.4, gutter: LANE, offset: 0, yaw: 0, mouth: 7.5, muBall: 0.4 };
    Object.assign(cfg, STACKS[stack]); if (P.finger) cfg.finger = P.finger; if (poker) cfg.pusher = POKER;
    const t0 = performance.now(); result = S.run(profile, cfg); const ms = Math.round(performance.now() - t0);
    const r = result; let s;
    if (r.stalled) s = "Stalled at " + r.stalled[0].toFixed(2) + " s: a steady push cannot lift the ball.";
    else if (r.fed.every(v => v !== null)) s = "All four balls into the intake, the last at " + Math.max(...r.fed).toFixed(2) + " s.";
    else if (r.missed.some(v => v !== null)) s = r.missed.filter(v => v !== null).length + " of 4 rolled past the wheels, off to the side.";
    else {
      const end = r.frames[r.frames.length - 1][2], left = 4 - r.fed.filter(v => v !== null).length;
      const onPeak = end.some(b => b[0] > 0.1 && b[0] < 0.7 && b[1] > 2.1 && b[1] < 2.8), onBar = end.some(b => b[0] < -0.3 && b[0] > -1.0 && b[1] > 4.6 && b[1] < 5.5);
      s = left + " of 4 never reached the intake" + (onPeak && onBar ? ": the stack locked itself. The bottom ball sits on the peak against the posts, and the ball above leans out over the collar's ledge and holds it there, as in our October 23 photos." : onPeak ? ": the bottom ball sits on the peak against the posts." : ": stuck on the blade.");
    }
    const stackName = { fb: "stack leaning front to back", straight: "straight stack", ss: "stack leaning side to side" }[stack];
    $("sim-result").textContent = P.name + (poker ? " with the poker on the robot" : "") + ", " + stackName + ", pushed at " + push + " in/s" + (stopErr ? ", sled stopping " + (-stopErr).toFixed(2) + " in short" : ", sled at the posts") + ". " + s;
    i = 0; acc = 0; $("sim-scrub").max = r.frames.length - 1; $("sim-scrub").value = 0;
    playing = true; $("sim-play").textContent = "Pause";
    if (window.Drive3D) { window.Drive3D.setBlade(profile, 5.4, P.shaftBack, LANE, 0, 0, null, P.vee || 0, P.finger ? P.finger.pivot[0] : 0); if (window.Drive3D.setPoker) window.Drive3D.setPoker(poker ? POKER : null); }
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
    if (true) {
      const face = lip - S.FACE_BEHIND_LIP, ax = face - S.GECKO_R, az = S.GECKO_Z;
      robotG.setAttribute("transform", `translate(${(face * PX).toFixed(1)} 0)`);
      gecko.setAttribute("cx", sx(ax)); gecko.setAttribute("cy", sy(az)); gecko.setAttribute("r", S.GECKO_R * PX);
      const a = t * 40 / S.GECKO_R;
      spoke.setAttribute("x1", sx(ax)); spoke.setAttribute("y1", sy(az)); spoke.setAttribute("x2", sx(ax + S.GECKO_R * Math.cos(a))); spoke.setAttribute("y2", sy(az + S.GECKO_R * Math.sin(a)));
      arm.setAttribute("x1", sx(ax)); arm.setAttribute("y1", sy(az)); arm.setAttribute("x2", sx(shx)); arm.setAttribute("y2", sy(0.77));
      robotG.style.display = gecko.style.display = spoke.style.display = ""; sledWheel.style.display = "none";
      if (cfg.pusher) { pokerEl.setAttribute("x", sx(0)); pokerEl.setAttribute("y", sy(cfg.pusher.z1)); pokerEl.setAttribute("width", cfg.pusher.reach * PX); pokerEl.setAttribute("height", (cfg.pusher.z1 - cfg.pusher.z0) * PX); pokerEl.style.display = ""; } else pokerEl.style.display = "none";
    } else {
      robotG.style.display = gecko.style.display = spoke.style.display = "none";
      arm.setAttribute("x1", sx(X0)); arm.setAttribute("y1", sy(0.77)); arm.setAttribute("x2", sx(shx)); arm.setAttribute("y2", sy(0.77));
      sledWheel.setAttribute("cx", sx(shx)); sledWheel.setAttribute("cy", sy(0.77)); sledWheel.setAttribute("r", 0.77 * PX); sledWheel.style.display = "";
    }
    f[2].forEach(([x, z, y], k) => { balls[k].setAttribute("cx", sx(x)); balls[k].setAttribute("cy", sy(z)); planBalls[k].setAttribute("cx", px2(x)); planBalls[k].setAttribute("cy", py2(y || 0)); });
    // the plan: the blade's outline and lane, the robot's face and wheel row, all in the blade's frame (offset, turned)
    const W = 5.4, len = profile[profile.length - 1][0], g = cfg.gutter, c = g ? S.catchHalf(g) : 0;
    planG.setAttribute("transform", `translate(${px2(lip).toFixed(1)} ${py2(cfg.offset || 0).toFixed(1)}) rotate(${(-(cfg.yaw || 0)).toFixed(1)})`);
    const pl = (xb, y) => (-xb * P2).toFixed(1) + "," + (-y * P2).toFixed(1);
    const V = P.vee || 0;                                              // the V nose from above: the sides sit V behind the middle's lip
    planBlade.setAttribute("points", (V ? [pl(V, -W / 2), pl(V, -1.42), pl(0, -0.855), pl(0, 0.855), pl(V, 1.42), pl(V, W / 2)] : [pl(0, -W / 2), pl(0, W / 2)]).concat([pl(len, W / 2), pl(len, -W / 2)]).join(" "));
    planLane.setAttribute("points", g ? [pl(g.start, -0.05), pl(g.start, 0.05), pl(g.start + 1.6, c), pl(len, c), pl(len, -c), pl(g.start + 1.6, -c)].join(" ") : "");
    if (true) {
      const fb = S.FACE_BEHIND_LIP, m = cfg.mouth || 7.5;
      planFace.setAttribute("x1", -fb * P2); planFace.setAttribute("x2", -fb * P2); planFace.setAttribute("y1", -8 * P2); planFace.setAttribute("y2", 8 * P2);
      planMouth.setAttribute("x", -(fb + 2 * S.GECKO_R) * P2); planMouth.setAttribute("y", -m / 2 * P2); planMouth.setAttribute("width", 2 * S.GECKO_R * P2); planMouth.setAttribute("height", m * P2);
      planFace.style.display = planMouth.style.display = "";
    } else planFace.style.display = planMouth.style.display = "none";
    if (window.Drive3D) window.Drive3D.frame(lip, f[2], true, P.shaftBack, t, cfg.finger ? [f[3] || 0, cfg.finger.pivot, NOSE_POLY] : null);
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
  function pressed(attr, val) { for (const b of document.querySelectorAll("[" + attr + "]")) b.setAttribute("aria-pressed", b.getAttribute(attr) === String(val)); }
  for (const b of document.querySelectorAll("[data-preset]")) b.onclick = () => { preset = b.dataset.preset; pressed("data-preset", preset); compute(); };
  for (const b of document.querySelectorAll("[data-stack]")) b.onclick = () => { stack = b.dataset.stack; pressed("data-stack", stack); compute(); };
  for (const b of document.querySelectorAll("[data-push]")) b.onclick = () => { push = +b.dataset.push; pressed("data-push", push); compute(); };
  for (const b of document.querySelectorAll("[data-poker]")) b.onclick = () => { poker = b.dataset.poker === "on"; pressed("data-poker", poker ? "on" : "off"); compute(); };
  for (const b of document.querySelectorAll("[data-stop]")) b.onclick = () => { stopErr = +b.dataset.stop; pressed("data-stop", stopErr); compute(); };
  $("sim-play").onclick = function () { if (!result) return; if (i >= result.frames.length - 1) { i = 0; acc = 0; } playing = !playing; this.textContent = playing ? "Pause" : "Play"; };
  $("sim-slow").onclick = function () { slow = !slow; this.setAttribute("aria-pressed", slow); };
  $("sim-scrub").oninput = function () { if (!result) return; playing = false; $("sim-play").textContent = "Play"; i = +this.value; draw(); };
  compute();
  requestAnimationFrame(tick);
})();
</script>
