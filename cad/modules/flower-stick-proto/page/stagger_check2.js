const S = require("./simlib.js");
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), v9 = S.mountain({ edge: 0.6, nose: 60, peak: 1.0, tail: 10 });
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
for (const muBall of [0, 0.4, 0.6]) for (const stagger of [0, 0.5, -0.5]) {
  const line = [];
  for (const [label, prof, extra] of [["v7", v7, 0], ["v8", v7, 0.3], ["v9", v9, 0]]) {
    let ok = 0;
    for (const mu of [0.3, 0.45]) for (const drive of [3, 12]) for (const stopErr of [0, -0.2, -0.35]) {
      const r = S.run(prof, { onRobot: true, tmax: 3.5, mu, drive, stopErr, stagger, extraDepth: extra, muBall, gutter: lane });
      if (r.fed.every(v => v !== null)) ok++;
    }
    line.push(`${label} ${ok}/12`);
  }
  console.log(`ball-on-ball friction ${muBall}, stagger ${stagger === 0 ? "none" : (stagger > 0 ? "balls 2,4 toward the robot" : "balls 2,4 toward the wall") + " " + Math.abs(stagger) + " in"}: ${line.join(" | ")}`);
}
