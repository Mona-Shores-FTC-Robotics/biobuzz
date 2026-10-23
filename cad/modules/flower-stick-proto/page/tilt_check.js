const S = require("./simlib.js");
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
for (const tilt of [0, 5, 10, 15]) {
  let ok = 0, worst = 0; const fails = [];
  for (const mu of [0.3, 0.45]) for (const drive of [3, 12]) for (const stopErr of [0, -0.2, -0.35, -0.5, -0.65]) {
    const r = S.run(v7, { onRobot: true, tmax: 3.5, mu, drive, stopErr, tilt, shaftBack: 0.53, gutter: { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 } });
    if (r.fed.every(v => v !== null)) { ok++; worst = Math.max(worst, Math.max(...r.fed)); } else fails.push(`stop${stopErr}`);
  }
  console.log(`v7, nose tilted up ${tilt} deg after the stop: fed ${ok}/20 (stops 0 to -0.65 in short), worst ${worst.toFixed(2)} s; fails at ${[...new Set(fails)].join(", ") || "none"}`);
}
