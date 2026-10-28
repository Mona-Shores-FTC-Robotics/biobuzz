// How much reach the poke needs for a given blade-lip distance (28 Oct 2026): run with node, ~5 min. Cells: cases fed of 24 at 3 / 6 in/s.
const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const PATS = { st: [0, 0, 0], fb: [-0.29, 0.35, 0.35], fb2: [-0.29, -0.86, 0.35], fb3: [-0.29, 0.86, -0.86], wall: [0.35, -0.29, 0.35], ss: null };
const full = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
function truncate(prof, cut) { const out = prof.filter(([x]) => x <= cut); for (let i = 0; i + 1 < prof.length; i++) { const [x0, z0] = prof[i], [x1, z1] = prof[i + 1]; if (x0 <= cut && cut <= x1) { out.push([cut, z0 + (z1 - z0) * (cut - x0) / (x1 - x0)]); break; } } out.push([cut + 0.001, 0.52]); return out; }
const prof = truncate(full, 2.53);
console.log("rows: the blade lip's distance ahead of the face (in); columns: poke reach past the face (in); cells: cases fed of 24 at 3 / 6 in/s, poke 5.7 to 7.5 up");
for (const fbl of [3.5, 3.78, 4.1, 4.4, 4.67, 5.0]) { const row = [];
  for (const reach of [1.3, 1.6, 1.9, 2.2, 2.5]) { const o = [];
    for (const drive of [3, 6]) { let ok = 0, n = 0; for (const pn of Object.keys(PATS)) for (const mu of [0.3, 0.45]) for (const stopErr of [0, -0.35]) { n++; const cfg = { onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: 0, shaftBack: 0.53, faceBehindLip: fbl, mu, drive, stopErr, width: 7.9, pusher: { reach, z0: 5.7, z1: 7.5, hw: 0.375 } }; if (PATS[pn]) cfg.pattern = PATS[pn]; else cfg.lean = 0.6; if (S.run(prof, cfg).fed.every(v => v !== null)) ok++; } o.push(ok); }
    row.push(`${reach}: ${o[0]}/${o[1]}`); }
  console.log(`lip ${fbl} in ahead of the face | ${row.join("  ")}`); }
console.log("this build's lip distance:", (S.SHAFT_AHEAD + 0.53).toFixed(2), "in (shaft", S.SHAFT_AHEAD.toFixed(2), "+ 0.53)");
