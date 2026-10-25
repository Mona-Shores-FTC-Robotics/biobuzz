// the low arm with the pivot 0.8 in ahead: the face 3.78 behind the lip; the poker's reach, and the short tail (2.4) with it
const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const PATS = { st: [0, 0, 0], fb: [-0.29, 0.35, 0.35], fb2: [-0.29, -0.86, 0.35], fb3: [-0.29, 0.86, -0.86], wall: [0.35, -0.29, 0.35], ss: null };
const full = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
function truncate(prof, cut) { const out = prof.filter(([x]) => x <= cut); for (let i = 0; i + 1 < prof.length; i++) { const [x0, z0] = prof[i], [x1, z1] = prof[i + 1]; if (x0 <= cut && cut <= x1) { out.push([cut, z0 + (z1 - z0) * (cut - x0) / (x1 - x0)]); break; } } out.push([cut + 0.001, 0.52]); return out; }
const prof = truncate(full, 2.4);
for (const reach of [1.0, 1.1, 1.2, 1.3, 1.4]) { const out = [];
  for (const drive of [3, 4, 6, 10]) { let ok = 0, n = 0; for (const pn of Object.keys(PATS)) for (const mu of [0.3, 0.45]) for (const stopErr of [0, -0.35]) { n++; const cfg = { onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: 0, shaftBack: 0.53, faceBehindLip: 3.78, mu, drive, stopErr, width: 7.9, pusher: { reach, z0: 5.3, z1: 7.5, hw: 0.375 } }; if (PATS[pn]) cfg.pattern = PATS[pn]; else cfg.lean = 0.6; if (S.run(prof, cfg).fed.every(v => v !== null)) ok++; } out.push(`${drive} in/s ${ok}/${n}`); }
  console.log(`tail 2.4, face 3.78 behind the lip, poker ${reach} in: ${out.join(" | ")}`); }
