// node lat_grid.js <faceBehindLip> <reach>  : poke widths x sideways offsets, cases fed of 6 (3 stack patterns x 2 friction) at 4 in/s
const S = require("./simlib.js");
const fbl = parseFloat(process.argv[2]), reach = parseFloat(process.argv[3]);
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const PATS = { fb: [-0.29, 0.35, 0.35], fb2: [-0.29, -0.86, 0.35], ss: null };
const full = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
function truncate(prof, cut) { const out = prof.filter(([x]) => x <= cut); for (let i = 0; i + 1 < prof.length; i++) { const [x0, z0] = prof[i], [x1, z1] = prof[i + 1]; if (x0 <= cut && cut <= x1) { out.push([cut, z0 + (z1 - z0) * (cut - x0) / (x1 - x0)]); break; } } out.push([cut + 0.001, 0.52]); return out; }
const prof = truncate(full, 2.53);
const offs = [-2.4, -1.6, -0.8, 0, 0.8, 1.6, 2.4]; const lines = [];
for (const w of [0.75, 1.5, 2.25, 3.0]) { const row = [];
  for (const off of offs) { let ok = 0, n = 0;
    for (const pn of Object.keys(PATS)) for (const mu of [0.3, 0.45]) { n++;
      const cfg = { onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: 0, shaftBack: 0.53, faceBehindLip: fbl, mu, drive: 4, stopErr: 0, width: 7.9, offset: off, pusher: { reach, z0: 5.7, z1: 7.5, hw: w / 2 } };
      if (PATS[pn]) cfg.pattern = PATS[pn]; else cfg.lean = 0.6;
      if (S.run(prof, cfg).fed.every(v => v !== null)) ok++; }
    row.push(`${ok}/${n}`); }
  lines.push(`  width ${w.toFixed(2)} in: ${row.join("  ")}`); }
console.log(`lip ${fbl} in, reach ${reach} in   offsets (in): ${offs.join(" ")}`); console.log(lines.join("\n"));
