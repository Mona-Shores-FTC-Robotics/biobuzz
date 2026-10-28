const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const PATS = { st: [0, 0, 0], fb: [-0.29, 0.35, 0.35], fb2: [-0.29, -0.86, 0.35], fb3: [-0.29, 0.86, -0.86], wall: [0.35, -0.29, 0.35], ss: null };
const full = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
function truncate(prof, cut) { const out = prof.filter(([x]) => x <= cut); for (let i = 0; i + 1 < prof.length; i++) { const [x0, z0] = prof[i], [x1, z1] = prof[i + 1]; if (x0 <= cut && cut <= x1) { out.push([cut, z0 + (z1 - z0) * (cut - x0) / (x1 - x0)]); break; } } out.push([cut + 0.001, 0.52]); return out; }
const base = truncate(full, 2.53), sb = 0.53, sz = 0.77;
const tilt = (prof, deg) => { const a = deg * Math.PI / 180, c = Math.cos(a), sn = Math.sin(a); return prof.map(([x, z]) => { const dx = x - sb, dz = z - sz; return [sb + dx * c + dz * sn, sz - dx * sn + dz * c]; }); };
for (const cant of [0, 3, 5, 8]) { const prof = tilt(base, cant), lowest = Math.min(...prof.map(p => p[1])), lip = prof[0]; const o = [];
  for (const drive of [3, 6, 10]) { let ok = 0, n = 0; for (const pn of Object.keys(PATS)) for (const mu of [0.3, 0.45]) for (const stopErr of [0, -0.35]) { n++; const cfg = { onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: 0, shaftBack: sb, faceBehindLip: 3.81, mu, drive, stopErr, width: 7.9, pusher: { reach: 1.3, z0: 5.7, z1: 7.5, hw: 0.375 } }; if (PATS[pn]) cfg.pattern = PATS[pn]; else cfg.lean = 0.6; if (S.run(prof, cfg).fed.every(v => v !== null)) ok++; } o.push(`${drive} in/s ${ok}/${n}`); }
  console.log(`nose-up cant ${cant}: lip at z ${lip[1].toFixed(2)}, tail end z ${prof[prof.length-1][1].toFixed(2)} (lowest ${lowest.toFixed(2)}) -> ${o.join(" | ")}`); }
