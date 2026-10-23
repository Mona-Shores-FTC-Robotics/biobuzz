const S = require("./simlib.js");
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const PATTERNS = { "straight": [0, 0, 0], "2,3,4 toward the robot 0.6": [-0.6, -0.6, -0.6], "2 fwd 3 back 4 fwd 0.6": [-0.6, 0.6, -0.6], "2 back 3 fwd 4 back 0.6": [0.6, -0.6, 0.6], "2,3,4 toward the robot 0.85": [-0.85, -0.85, -0.85], "2 fwd 3 back 4 fwd 0.85": [-0.85, 0.85, -0.85], "left-right zigzag 0.6 (lean)": null };
for (const collar of [3.9, 3.7]) for (const [name, pattern] of Object.entries(PATTERNS)) {
  let ok = 0; const fails = []; let stuckEx = null;
  for (const mu of [0.3, 0.45]) for (const drive of [3, 12]) for (const stopErr of [0, -0.2, -0.35]) {
    const cfg = { onRobot: true, tmax: 3.5, mu, drive, stopErr, muBall: 0.4, gutter: lane, collar };
    if (pattern) cfg.pattern = pattern; else cfg.lean = 0.6;
    const r = S.run(v7, cfg);
    if (r.fed.every(v => v !== null)) ok++; else { fails.push(`mu${mu}/${drive}/stop${stopErr}`); if (!stuckEx) stuckEx = r.frames[r.frames.length - 1]; }
  }
  console.log(`collar ${collar}: ${name.padEnd(30)} fed ${ok}/12${fails.length ? "; fails " + fails.join(" ") : ""}${stuckEx ? "; e.g. ends with balls at " + JSON.stringify(stuckEx[2].map(b => [b[0], b[1]])) : ""}`);
}
