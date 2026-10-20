const S = require("./simlib.js");
for (const d of [{ edge: 0.6, nose: 50, peak: 1.05, tail: 8 }, { edge: 0.6, nose: 50, peak: 1.05, tail: 10 }, { edge: 0.6, nose: 50, peak: 1.1, tail: 10 }, { edge: 0.6, nose: 55, peak: 1.1, tail: 10 }, { edge: 0.6, nose: 60, peak: 1.1, tail: 12 }]) {
  const prof = S.mountain(d), xp = prof[2][0]; let ok = 0, worst = 0; const fails = [];
  for (const mu of [0.3, 0.45]) for (const drive of [3, 12]) for (const stopErr of [0, -0.2, -0.35]) {
    const r = S.run(prof, { onRobot: true, tmax: 3.5, mu, drive, stopErr, gutter: { R: 12.3, rise: 0.3, start: xp + 0.05, lead: 1.2, oval: 1.2 } });
    if (r.fed.every(v => v !== null)) { ok++; worst = Math.max(worst, Math.max(...r.fed)); } else fails.push(`mu${mu}/${drive}in/s/stop${stopErr}`);
  }
  const tt = Math.tan(d.tail * Math.PI / 180), sx = xp + 0.186, skin = (d.peak - (sx + 0.186 - xp) * tt) - (0.77 + 0.161);
  console.log(`nose ${d.nose} peak ${d.peak} tail ${d.tail}: fed ${ok}/12 worst ${worst.toFixed(2)}; margin ${(S.STOP - xp - 0.35).toFixed(2)}; hex skin at the centre ${(skin * 25.4).toFixed(1)} mm; fails: ${fails.join(", ") || "none"}`);
}
