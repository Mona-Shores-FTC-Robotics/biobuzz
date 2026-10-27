// a taller stack: does the 2.0 in blade on the coach's arms with the poker still free the bottom ball with 4..7 POLLEN in the
// FLOWER? The model pushes the blade at a set speed, so this is the geometry and the balls' dynamics, not the drive's traction;
// pushForce() gives the push a steady drive needs as a multiple of the stack's weight.
const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const full = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
function truncate(prof, cut) { if (cut >= prof[prof.length - 1][0]) return prof; const out = prof.filter(([x]) => x <= cut); for (let i = 0; i + 1 < prof.length; i++) { const [x0, z0] = prof[i], [x1, z1] = prof[i + 1]; if (x0 <= cut && cut <= x1) { out.push([cut, z0 + (z1 - z0) * (cut - x0) / (x1 - x0)]); break; } } out.push([cut + 0.001, 0.52]); return out; }
const prof = truncate(full, 2.0);
console.log("push a steady drive needs, as a multiple of the stack's weight (locks where the wedge angle and friction say no):");
for (const nose of [60, 55, 50, 45]) console.log(`  ${nose} deg nose: ` + [0.2, 0.3, 0.4, 0.45, 0.5, 0.6].map(mu => { const f = S.pushForce ? S.pushForce(nose, mu) : null; return `mu ${mu}: ${f === null || f === undefined ? "LOCKS" : f.toFixed(1) + "x"}`; }).join(" | "));
for (const stack of [4, 5, 6, 7, 8, 9, 10]) {
  const out = [];
  for (const drive of [3, 4, 6, 10]) { let ok = 0, n = 0, bottom = 0;
    for (const mu of [0.3, 0.45]) for (const stopErr of [0, -0.35]) for (const lean of [0, 0.6]) { n++;
      const cfg = { onRobot: true, tmax: 4.0, muBall: 0.4, gutter: lane, extraDepth: 0, shaftBack: 0.53, faceBehindLip: 3.81, mu, drive, stopErr, width: 7.9, lean, stack, pusher: { reach: 1.3, z0: 5.7, z1: 7.5, hw: 0.375 } };
      const r = S.run(prof, cfg); if (r.fed[0] !== null) bottom++; if (r.fed.every(v => v !== null)) ok++; }
    out.push(`${drive} in/s: bottom ball out ${bottom}/${n}, all ${ok}/${n}`); }
  console.log(`${stack} POLLEN: ${out.join(" | ")}`);
}
