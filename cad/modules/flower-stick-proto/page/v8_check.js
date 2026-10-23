const S = require("./simlib.js");
// a V nose: the centre of the lip goes `vee` deeper than the sides (which still stop on the lobes' points)
for (const vee of [0, 0.2, 0.3, 0.43]) {
  const prof = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }); let ok = 0, worst = 0;
  for (const mu of [0.3, 0.45]) for (const drive of [3, 12]) for (const stopErr of [0, -0.2, -0.35, -0.5]) {
    const r = S.run(prof, { onRobot: true, tmax: 3.5, mu, drive, stopErr, extraDepth: vee, gutter: { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 } });
    if (r.fed.every(v => v !== null)) { ok++; worst = Math.max(worst, Math.max(...r.fed)); }
  }
  console.log(`V depth ${vee}: fed ${ok}/16 (stop errors 0, -0.2, -0.35, -0.5), worst ${worst.toFixed(2)} s, margin at the centre ${(S.STOP + vee - prof[2][0] - 0.35).toFixed(2)} in`);
}
