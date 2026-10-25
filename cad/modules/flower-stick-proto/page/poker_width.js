const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const PATS = { st: [0, 0, 0], fb: [-0.29, 0.35, 0.35], fb2: [-0.29, -0.86, 0.35], fb3: [-0.29, 0.86, -0.86], wall: [0.35, -0.29, 0.35], ss: null };
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
function test(label, more, offsets) {
  const out = [];
  for (const offset of offsets) { let ok = 0, n = 0; const fails = [];
    for (const pn of Object.keys(PATS)) for (const mu of [0.3, 0.45]) for (const drive of [4, 8]) for (const stopErr of [0, -0.35]) { n++; const cfg = Object.assign({ onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: 0, shaftBack: 0.53, mu, drive, stopErr, offset }, more); if (PATS[pn]) cfg.pattern = PATS[pn]; else cfg.lean = 0.6; const r = S.run(v7, cfg); if (r.fed.every(v => v !== null)) ok++; else fails.push(pn); }
    out.push(`off ${offset}: ${ok}/${n}${fails.length ? " (" + [...new Set(fails)].join(",") + ")" : ""}`); }
  console.log(`${label.padEnd(36)} ${out.join(" | ")}`);
}
test("v7, no poker", {}, [0, 0.5, 1.0]);
for (const w of [0.5, 0.75, 1.0, 1.4]) test(`poker ${w} in wide, 1.2 ahead, 5.3-7.5`, { pusher: { reach: 1.2, z0: 5.3, z1: 7.5, hw: w / 2 } }, [0, 0.5, 1.0]);
test(`poker 0.75 wide, 1.1 ahead, 5.3-7.5`, { pusher: { reach: 1.1, z0: 5.3, z1: 7.5, hw: 0.375 } }, [0, 0.5, 1.0]);
