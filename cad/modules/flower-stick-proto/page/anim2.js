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
  const shaft = el("circle", { r: 0.16 * PX, fill: "var(--steel)", stroke: "var(--fg)" });
  const balls = [0, 1, 2, 3].map(() => el("circle", { r: S.R * PX, fill: "var(--pollen)", "fill-opacity": 0.75, stroke: "var(--fg)", "stroke-width": 1.2 }));
  const $ = id => document.getElementById(id);
  const PRESETS = {
    blockA: { name: "Block A, hand sled", profile: [[0, 0.52], [0, 0.64], [0.2, 1.30], [0.7, 1.30], [1.4, 0.52]], extraDepth: 0.57, shaftBack: 0.93, onRobot: false, face: 17 },
    v2: { name: "v2 lane", profile: [[0, 0.52], [0, 0.60], [0.45, 1.05], [0.90, 0.99], [1.50, 0.52]], shaftBack: 0.69, onRobot: true },
    v3: { name: "v3 mountain", shape: { edge: 0.53, nose: 55, peak: 1.28, tail: 45 }, shaftBack: 0.77, onRobot: true },
    v4: { name: "v4 carry", shape: { edge: 0.53, nose: 55, peak: 1.28, tail: 14.4 }, shaftBack: 0.77, onRobot: true },
  };
  const sliders = ["edge", "nose", "peak", "tail", "drive", "mu", "stopErr"];
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
    for (const k of sliders) { const o = $("v-" + k); if (o) o.textContent = $("s-" + k).value; }
    return prof;
  }
  function compute() {
    const P = PRESETS[preset];
    profile = readouts();
    cfg = { drive: +$("s-drive").value, mu: +$("s-mu").value, stopErr: +$("s-stopErr").value, extraDepth: P.extraDepth || 0, onRobot: P.onRobot };
    const t0 = performance.now();
    result = S.run(profile, cfg);
    const ms = Math.round(performance.now() - t0);
    const r = result, name = custom ? "custom mountain" : P.name;
    let s;
    if (r.stalled) s = "Stalled at " + r.stalled[0].toFixed(2) + " s: the push at the blade contact is tilted up only " + r.stalled[1].toFixed(0) + "°, under the friction angle " + (Math.atan(cfg.mu) * 180 / Math.PI).toFixed(0) + "°. A steady push cannot lift the ball.";
    else if (cfg.onRobot && r.fed.every(v => v !== null)) s = "All four past the Gecko at " + Math.max(...r.fed).toFixed(2) + " s (first out of the FLOWER at " + r.out[0].toFixed(2) + " s).";
    else if (r.out.every(v => v !== null)) s = "All four out of the FLOWER, the last at " + Math.max(...r.out).toFixed(2) + " s.";
    else s = (4 - r.out.filter(v => v !== null).length) + " of 4 still in the FLOWER after " + r.frames[r.frames.length - 1][0].toFixed(1) + " s.";
    $("sim-result").textContent = name + ", " + cfg.drive + " in/s, friction " + cfg.mu + (cfg.stopErr ? ", stop " + (cfg.stopErr > 0 ? "+" : "") + cfg.stopErr + " in" : "") + ". " + s + " (" + ms + " ms)";
    i = 0; acc = 0; $("sim-scrub").max = r.frames.length - 1; $("sim-scrub").value = 0;
    playing = true; $("sim-play").textContent = "Pause";
    draw();
  }
  function draw() {
    if (!result) return;
    const f = result.frames[i], t = f[0], lip = f[1], P = PRESETS[preset];
    blade.setAttribute("points", profile.map(([x, z]) => sx(lip - x) + "," + sy(z)).join(" "));
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
    f[2].forEach(([x, z], k) => { balls[k].setAttribute("cx", sx(x)); balls[k].setAttribute("cy", sy(z)); });
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
    for (const s of ["edge", "nose", "peak", "tail"]) $("s-" + s).disabled = !P.shape;
    compute();
  }
  for (const b of document.querySelectorAll("[data-preset]")) b.onclick = () => choose(b.dataset.preset);
  for (const s of ["edge", "nose", "peak", "tail"]) $("s-" + s).oninput = () => { custom = true; for (const b of document.querySelectorAll("[data-preset]")) b.setAttribute("aria-pressed", "false"); readouts(); };
  for (const s of ["drive", "mu", "stopErr"]) $("s-" + s).oninput = readouts;
  $("sim-run").onclick = compute;
  $("sim-play").onclick = function () { if (!result) return; if (i >= result.frames.length - 1) { i = 0; acc = 0; } playing = !playing; this.textContent = playing ? "Pause" : "Play"; };
  $("sim-slow").onclick = function () { slow = !slow; this.setAttribute("aria-pressed", slow); };
  $("sim-scrub").oninput = function () { if (!result) return; playing = false; $("sim-play").textContent = "Play"; i = +this.value; draw(); };
  choose("v4");
  requestAnimationFrame(tick);
})();
</script>
