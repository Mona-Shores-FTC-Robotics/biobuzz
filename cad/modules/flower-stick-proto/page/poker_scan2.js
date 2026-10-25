// the poker's reach with the face where the October 25 arms put it (3.81 in behind the mountain blade's lip, not 3.68)
const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const PATS = { st: [0, 0, 0], fb: [-0.29, 0.35, 0.35], fb2: [-0.29, -0.86, 0.35], fb3: [-0.29, 0.86, -0.86], wall: [0.35, -0.29, 0.35], ss: null };
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
function test(label, more, drives, width) {
  const out = [];
  for (const drive of drives) { let ok = 0, n = 0; const fails = [];
    for (const pn of Object.keys(PATS)) for (const mu of [0.3, 0.45]) for (const stopErr of [0, -0.35]) { n++; const cfg = Object.assign({ onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: 0, shaftBack: 0.53, mu, drive, stopErr, width }, more); if (PATS[pn]) cfg.pattern = PATS[pn]; else cfg.lean = 0.6; const r = S.run(v7, cfg); if (r.fed.every(v => v !== null)) ok++; else fails.push(pn); }
    out.push(`${drive} in/s: ${ok}/${n}${fails.length ? " (" + [...new Set(fails)].join(",") + ")" : ""}`); }
  console.log(`${label.padEnd(44)} ${out.join(" | ")}`);
}
for (const width of [5.4, 7.9]) {
  console.log(`blade ${width} in wide`);
  test("no poker", {}, [3, 6, 10], width);
  for (const reach of [1.1, 1.2, 1.3, 1.4, 1.5]) test(`poker ${reach} in ahead of face, 5.3-7.5 up, 0.75 wide`, { pusher: { reach, z0: 5.3, z1: 7.5, hw: 0.375 } }, [3, 6, 8, 10], width);
}
