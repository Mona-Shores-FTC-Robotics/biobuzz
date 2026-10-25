// the mountain blade 0.07 in lower (shaft at 0.70 instead of 0.77: 72 mm wheels on stub axles one hole up the arm), with the poker
const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const PATS = { st: [0, 0, 0], fb: [-0.29, 0.35, 0.35], fb2: [-0.29, -0.86, 0.35], fb3: [-0.29, 0.86, -0.86], wall: [0.35, -0.29, 0.35], ss: null };
for (const drop of [0, 0.07, 0.12]) {
  const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }).map(([x, z]) => [x, z - drop]);
  for (const poker of [false, true]) { const out = [];
    for (const drive of [3, 4, 6, 10]) { let ok = 0, n = 0; for (const pn of Object.keys(PATS)) for (const mu of [0.3, 0.45]) for (const stopErr of [0, -0.35]) { n++; const cfg = { onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: 0, shaftBack: 0.53, mu, drive, stopErr, width: 7.9 }; if (poker) cfg.pusher = { reach: 1.2, z0: 5.3, z1: 7.5, hw: 0.375 }; if (PATS[pn]) cfg.pattern = PATS[pn]; else cfg.lean = 0.6; if (S.run(v7, cfg).fed.every(v => v !== null)) ok++; } out.push(`${drive} in/s ${ok}/${n}`); }
    console.log(`blade ${drop} in lower${poker ? ", poker 1.2" : ""}: ${out.join(" | ")}`); }
}
