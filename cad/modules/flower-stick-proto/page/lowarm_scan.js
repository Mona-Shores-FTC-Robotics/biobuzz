// the low arm: the face 3.47 or 4.53 in behind the lip (72 or 96 mm pivot-to-shaft), blade at 0.77, poker reach scan
const S = require("./simlib.js");
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const PATS = { st: [0, 0, 0], fb: [-0.29, 0.35, 0.35], fb2: [-0.29, -0.86, 0.35], fb3: [-0.29, 0.86, -0.86], wall: [0.35, -0.29, 0.35], ss: null };
for (const [label, fbl] of [["72 mm arm, face 3.47 behind lip", 3.47], ["96 mm arm, face 4.53 behind lip", 4.53]]) {
  console.log(label);
  for (const reach of [null, 0.6, 0.8, 0.9, 1.0, 1.1, 1.2, 1.6, 1.8, 1.9, 2.0, 2.1, 2.2]) {
    if (reach !== null && ((fbl < 4 && reach > 1.3) || (fbl > 4 && reach < 1.5))) continue;
    const out = [];
    for (const drive of [3, 4, 6, 10]) { let ok = 0, n = 0; for (const pn of Object.keys(PATS)) for (const mu of [0.3, 0.45]) for (const stopErr of [0, -0.35]) { n++; const cfg = { onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: 0, shaftBack: 0.53, faceBehindLip: fbl, mu, drive, stopErr, width: 7.9 }; if (reach !== null) cfg.pusher = { reach, z0: 5.3, z1: 7.5, hw: 0.375 }; if (PATS[pn]) cfg.pattern = PATS[pn]; else cfg.lean = 0.6; if (S.run(v7, cfg).fed.every(v => v !== null)) ok++; } out.push(`${drive} in/s ${ok}/${n}`); }
    console.log(`  poker ${reach === null ? "none" : reach + " in"}: ${out.join(" | ")}`);
  }
}
