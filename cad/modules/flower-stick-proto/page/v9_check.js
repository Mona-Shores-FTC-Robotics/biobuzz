const S = require("./simlib.js");
for (const [name, d, sz] of [["v8: peak 1.10, tail 12", { edge: 0.6, nose: 60, peak: 1.1, tail: 12 }, 0.77], ["v9: peak 1.00, tail 10", { edge: 0.6, nose: 60, peak: 1.0, tail: 10 }, 0.74], ["peak 0.95, tail 10", { edge: 0.6, nose: 60, peak: 0.95, tail: 10 }, 0.72]]) {
  const prof = S.mountain(d); let ok = 0, worst = 0;
  for (const mu of [0.3, 0.45]) for (const drive of [3, 12]) for (const stopErr of [0, -0.2, -0.35, -0.5]) {
    const r = S.run(prof, { onRobot: true, tmax: 3.5, mu, drive, stopErr, extraDepth: 0.3, gutter: { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 } });
    if (r.fed.every(v => v !== null)) { ok++; worst = Math.max(worst, Math.max(...r.fed)); }
  }
  console.log(`${name.padEnd(24)} with the V: fed ${ok}/16, worst ${worst.toFixed(2)} s; ball top on the peak ${(d.peak + 2.8).toFixed(2)} in; tail ends ${(S.STOP + 0.3 - prof[3][0]).toFixed(2)} from the FLOWER's centre`);
}
