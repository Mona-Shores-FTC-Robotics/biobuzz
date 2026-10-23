const S = require("./simlib.js");
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
for (const stagger of [0, 0.3, 0.6, 0.85, -0.3, -0.6, -0.85]) {
  for (const [label, extra] of [["v7", 0], ["v8 (0.3 deeper)", 0.3]]) {
    let ok = 0, worst = 0; const fails = [];
    for (const mu of [0.3, 0.45]) for (const drive of [3, 12]) for (const stopErr of [0, -0.2, -0.35]) {
      const r = S.run(v7, { onRobot: true, tmax: 3.5, mu, drive, stopErr, stagger, extraDepth: extra, gutter: { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 } });
      if (r.fed.every(v => v !== null)) { ok++; worst = Math.max(worst, Math.max(...r.fed)); } else fails.push(`mu${mu}/${drive}/stop${stopErr}`);
    }
    console.log(`balls 2 and 4 ${stagger >= 0 ? "toward the robot" : "toward the wall"} by ${Math.abs(stagger)} in, ${label.padEnd(16)}: fed ${ok}/12, worst ${worst.toFixed(2)} s${fails.length ? "; fails " + fails.join(" ") : ""}`);
  }
}
