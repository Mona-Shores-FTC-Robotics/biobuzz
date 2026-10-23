const S = require("./simlib.js");
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), v9 = S.mountain({ edge: 0.6, nose: 60, peak: 1.0, tail: 10 });
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const PATTERNS = { "straight": [0, 0, 0], "front-back A (2 fwd, 3 back, 4 fwd)": [-0.6, 0.6, -0.6], "front-back B (2 back, 3 fwd, 4 back)": [0.6, -0.6, 0.6], "side to side (lean 0.6)": null };
const BLADES = [["v7", v7, 0, 0.53], ["v8 (V, 0.3 deeper)", v7, 0.3, 0.45], ["v9 (peak 1.0)", v9, 0, 0.39], ["v8 + peak 1.0", v9, 0.3, 0.39]];
for (const [name, pattern] of Object.entries(PATTERNS)) {
  const line = [];
  for (const [label, prof, extra, sb] of BLADES) {
    let ok = 0; const fails = [];
    for (const mu of [0.3, 0.45]) for (const drive of [3, 6, 12]) for (const stopErr of [0, -0.2, -0.35]) {
      const cfg = { onRobot: true, tmax: 3.5, mu, drive, stopErr, muBall: 0.4, gutter: lane, extraDepth: extra, shaftBack: sb };
      if (pattern) cfg.pattern = pattern; else cfg.lean = 0.6;
      const r = S.run(prof, cfg); if (r.fed.every(v => v !== null)) ok++; else fails.push(`${drive}in/s`);
    }
    line.push(`${label} ${ok}/18${fails.length ? " (fails at " + [...new Set(fails)].join(",") + ")" : ""}`);
  }
  console.log(`${name.padEnd(38)}: ${line.join(" | ")}`);
}
