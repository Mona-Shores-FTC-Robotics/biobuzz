const S = require("./simlib.js");
for (const tail of [8, 10]) for (const edge of [0.56, 0.6]) {
  const prof = S.mountain({ edge, nose: 45, peak: 0.9, tail }); let ok = 0, worst = 0;
  for (const mu of [0.3, 0.45]) for (const drive of [3, 12]) for (const stopErr of [0, -0.2, -0.35]) {
    const r = S.run(prof, { onRobot: true, tmax: 3.5, mu, drive, stopErr, gutter: { R: 12.3, rise: 0.3, start: 0.39, lead: 1.2, oval: 1.2 } });
    if (r.fed.every(v => v !== null)) { ok++; worst = Math.max(worst, Math.max(...r.fed)); }
  }
  console.log("nose 45 edge", edge, "peak 0.9 tail", tail, "+ oval lane:", ok + "/12", "worst", worst.toFixed(2), "tail end", (S.STOP - prof[3][0]).toFixed(2));
}
