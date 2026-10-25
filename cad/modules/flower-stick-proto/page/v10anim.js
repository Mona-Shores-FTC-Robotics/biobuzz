<script>
// A close-up of v10 doing its trick, driven by the simulator's own run: the lip meets the post, the body goes on, the nose
// piece tips up about its pin, the ball rolls over the peak. Then the robot backs out and the nose falls flat, and it loops.
(function () {
  const S = window.FlowerSim, svg = document.getElementById("v10-svg"); if (!S || !svg) return;
  const NS = "http://www.w3.org/2000/svg", PX = 112, X0 = -3.1, ZTOP = 4.5, CAPY = (ZTOP + 0.4) * PX;
  const sx = x => (x - X0) * PX, sy = z => (ZTOP - z) * PX;
  const el = (tag, attrs, parent) => { const e = document.createElementNS(NS, tag); for (const k in attrs) e.setAttribute(k, attrs[k]); (parent || svg).appendChild(e); return e; };
  const poly = (pts, attrs) => { const e = el("polygon", attrs); e.setAttribute("points", pts.map(([x, z]) => sx(x).toFixed(1) + "," + sy(z).toFixed(1)).join(" ")); return e; };
  // the field: tile, ring, the post (its section at the corner), the collar's ledge
  el("rect", { x: 0, y: sy(0), width: 700, height: 0.4 * PX, fill: "var(--steel)" });
  el("rect", { x: 0, y: CAPY, width: 700, height: 62, fill: "var(--panel)" });
  el("rect", { x: sx(-2.4), y: sy(0.35), width: 0.88 * PX, height: 0.35 * PX, fill: "none", stroke: "var(--fg)", "stroke-width": 1.2 });
  el("rect", { x: sx(1.52), y: sy(0.35), width: 0.94 * PX, height: 0.35 * PX, fill: "none", stroke: "var(--fg)", "stroke-width": 1.2 });
  el("rect", { x: sx(1.18), y: sy(4.1), width: 1.28 * PX, height: 3.75 * PX, fill: "var(--muted)", "fill-opacity": 0.3, stroke: "var(--fg)", "stroke-width": 1, "stroke-dasharray": "4 3" });
  el("text", { x: sx(1.82), y: sy(3.6), class: "lbl", "font-size": 11, "text-anchor": "middle" }).textContent = "post";
  el("text", { x: sx(1.82), y: sy(3.35), class: "lbl", "font-size": 10, "text-anchor": "middle" }).textContent = "(beside the ball)";
  for (const sg of [1, -1]) poly([[sg * -2.41, 3.90], [sg * -1.93, 3.90], [sg * -1.93, 4.10], [sg * -1.71, 4.10], [sg * -1.71, 4.25], [sg * -2.41, 4.25]], { fill: "var(--steel)", "fill-opacity": 0.5, stroke: "var(--fg)", "stroke-width": 1 });
  el("text", { x: sx(-2.06), y: sy(4.33), class: "lbl", "font-size": 10, "text-anchor": "middle" }).textContent = "collar";
  const balls = [0, 1, 2, 3].map(() => el("circle", { r: 1.4 * PX, fill: "var(--pollen)", "fill-opacity": 0.75, stroke: "var(--fg)", "stroke-width": 1.2 }));
  const body = el("polygon", { fill: "var(--petg)" }), nose = el("polygon", { fill: "#e0552b", stroke: "var(--fg)", "stroke-width": 0.8 });
  const pin = el("circle", { r: 0.08 * PX, fill: "var(--steel)", stroke: "var(--fg)" });
  const face = el("line", { stroke: "var(--muted)", "stroke-width": 1.5 }), gecko = el("circle", { fill: "var(--steel)", "fill-opacity": 0.35, stroke: "var(--fg)", "stroke-width": 1.2, r: 0.94 * PX });
  const pinLbl = el("text", { "font-size": 11, "text-anchor": "middle", fill: "var(--fg)" }); pinLbl.textContent = "pin";
  const arrow = el("path", { fill: "none", stroke: "#e0552b", "stroke-width": 2.5, "marker-end": "url(#v10arrow)" });
  const defs = el("defs", {}); const mk = el("marker", { id: "v10arrow", viewBox: "0 0 10 10", refX: 9, refY: 5, markerWidth: 7, markerHeight: 7, orient: "auto" }, defs); el("path", { d: "M0,0 L10,5 L0,10 z", fill: "#e0552b" }, mk);
  const cap = el("text", { x: 12, y: CAPY + 26, "font-size": 16, "font-weight": 600, fill: "var(--fg)" }), cap2 = el("text", { x: 12, y: CAPY + 48, "font-size": 13, fill: "var(--muted)" }), clock = el("text", { x: 688, y: CAPY + 26, "font-size": 12, "text-anchor": "end", fill: "var(--muted)", "font-family": "var(--mono)" });
  const profile = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), PIV = [0.32, 0.96], TRIP = S.STOP - 0.35;
  const NOSE = [[0, 0.52], [0, 0.6], [0.29, 1.1], [0.32, 1.11], [0.45, 1.0], [0.45, 0.52]];
  const zAt = x => { for (let i = 0; i + 1 < profile.length; i++) { const [x0, z0] = profile[i], [x1, z1] = profile[i + 1]; if (x >= x0 && x <= x1) return z0 + (z1 - z0) * (x - x0) / (x1 - x0); } return profile[0][1]; };
  const BODY = [[PIV[0], 0.52], [PIV[0], zAt(PIV[0])], ...profile.filter(p => p[0] > PIV[0])];
  const r = S.run(profile, { onRobot: true, tmax: 3.5, drive: 4, mu: 0.4, stopErr: -0.35, muBall: 0.4, gutter: { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6, oval: 0 }, extraDepth: 0.22, shaftBack: 0.53, width: 5.4, mouth: 7.5, pattern: [-0.29, 0.35, 0.35], finger: { len: 0, deg: 54, advance: 0.22, pivot: PIV } });
  const F = r.frames, last = F[F.length - 1], lastFed = Math.max(...r.fed.map(v => v === null ? 0 : v));
  // the loop: the run's frames, a hold, then the robot backing out over a second with the nose falling flat, a hold
  const SLOW0 = 1.28, SLOW1 = 1.46, SLOWK = 20, BACK = 1.0, HOLD = 0.8, total = last[0] + (SLOW1 - SLOW0) * (SLOWK - 1) + HOLD + BACK + HOLD, NEXT = [[0, 1.4], [-0.29, 4.13], [0.86, 6.7], [-0.86, 9.4]];
  function show(lip, bs, deg, caption, sub, t) {
    const a = deg * Math.PI / 180, c = Math.cos(a), s = Math.sin(a);
    body.setAttribute("points", BODY.map(([x, z]) => sx(lip - x).toFixed(1) + "," + sy(z).toFixed(1)).join(" "));
    nose.setAttribute("points", NOSE.map(([x, z]) => { const dx = x - PIV[0], dz = z - PIV[1]; return [PIV[0] + dx * c + dz * s, PIV[1] - dx * s + dz * c]; }).map(([x, z]) => sx(lip - x).toFixed(1) + "," + sy(z).toFixed(1)).join(" "));
    pin.setAttribute("cx", sx(lip - PIV[0])); pin.setAttribute("cy", sy(PIV[1]));
    const fx = lip - S.FACE_BEHIND_LIP; face.setAttribute("x1", sx(fx)); face.setAttribute("x2", sx(fx)); face.setAttribute("y1", sy(0.3)); face.setAttribute("y2", sy(4.5));
    gecko.setAttribute("cx", sx(fx - 0.94)); gecko.setAttribute("cy", sy(2.89));
    bs.forEach(([x, z], k) => { balls[k].setAttribute("cx", sx(x)); balls[k].setAttribute("cy", sy(z)); balls[k].style.display = x < -2.6 ? "none" : ""; });
    // the push arrow at the lip, while the post is holding it
    if (deg > 0 && deg < 54) { arrow.setAttribute("d", `M${sx(2.05)},${sy(0.62)} L${sx(1.32)},${sy(0.62)}`); arrow.style.display = ""; pinLbl.style.display = ""; pinLbl.setAttribute("x", sx(lip - PIV[0])); pinLbl.setAttribute("y", sy(PIV[1] + 0.28)); } else { arrow.style.display = "none"; pinLbl.style.display = "none"; }
    cap.textContent = caption; cap2.textContent = sub; clock.textContent = t.toFixed(2) + " s";
  }
  let t0 = null, paused = false;
  function frameAt(t) {
    if (t <= last[0]) {
      let lo = 0, hi = F.length - 1; while (lo < hi) { const m = (lo + hi) >> 1; if (F[m][0] < t) lo = m + 1; else hi = m; }
      const f = F[lo], lip = f[1], deg = f[3] || 0, b1 = f[2][0];
      let caption, sub;
      if (lip < TRIP - 0.02 && deg === 0) { caption = "Driving in"; sub = "v7's shape, split at a pin just behind the peak"; }
      else if (deg < 1) { caption = "The lip meets the post"; sub = "the nose piece stops; the robot keeps pushing"; }
      else if (deg < 53) { caption = "The nose tips up: " + deg.toFixed(0) + "° (slow motion)"; sub = "the push lands at the lip, below the pin, so it turns the nose; the body goes on 0.22 in"; }
      else if (f[0] < lastFed) { caption = f[2].some(b => b[0] > -0.9 && b[1] < 3) ? "Tipped to 54°: the ball rolls over" : "The next one follows"; sub = "the tilted face lifts the back of the ball while the peak moves under it"; }
      else { caption = "All four out"; sub = "one after another, into the intake"; }
      show(lip, f[2], deg, caption, sub, f[0]);
    } else if (t <= last[0] + HOLD) show(last[1], last[2], last[3] || 0, "Out, one after another", "all four into the intake", last[0]);
    else if (t <= last[0] + HOLD + BACK) {
      const u = (t - last[0] - HOLD) / BACK, lip = last[1] - 3.0 * u, deg = (last[3] || 0) * Math.max(0, 1 - u * 2.2);
      show(lip, NEXT, deg, "The robot backs out", "the nose falls flat on its heel, ready for the next FLOWER", last[0]);
    } else show(last[1] - 3.0, NEXT, 0, "Ready again", "no spring, no slide, no lever", last[0]);
  }
  const simTime = w => w <= SLOW0 ? w : w <= SLOW0 + (SLOW1 - SLOW0) * SLOWK ? SLOW0 + (w - SLOW0) / SLOWK : w - (SLOW1 - SLOW0) * (SLOWK - 1);
  function tick(now) { if (!paused) { if (t0 === null) t0 = now; const w = ((now - t0) / 1000) % total; frameAt(simTime(w)); } requestAnimationFrame(tick); }
  svg.addEventListener("click", () => { paused = !paused; if (!paused) t0 = null; });
  window.v10Seek = w => { paused = true; frameAt(simTime(w)); };
  frameAt(0); requestAnimationFrame(tick);
})();
</script>
