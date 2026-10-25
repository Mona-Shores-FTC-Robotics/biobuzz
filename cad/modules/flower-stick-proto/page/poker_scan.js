const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const PATS = { st: [0, 0, 0], fb: [-0.29, 0.35, 0.35], fb2: [-0.29, -0.86, 0.35], fb3: [-0.29, 0.86, -0.86], wall: [0.35, -0.29, 0.35], ss: null };
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
function test(label, extra, sb, more, drives) {
  const out = [];
  for (const drive of drives) { let ok = 0, n = 0; const fails = [];
    for (const pn of Object.keys(PATS)) for (const mu of [0.3, 0.45]) for (const stopErr of [0, -0.35]) { n++; const cfg = Object.assign({ onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: extra, shaftBack: sb, mu, drive, stopErr }, more); if (PATS[pn]) cfg.pattern = PATS[pn]; else cfg.lean = 0.6; const r = S.run(v7, cfg); if (r.fed.every(v => v !== null)) ok++; else fails.push(pn); }
    out.push(`${drive} in/s: ${ok}/${n}${fails.length ? " (" + [...new Set(fails)].join(",") + ")" : ""}`); }
  console.log(`${label.padEnd(34)} ${out.join(" | ")}`);
}
test("v7, no poker", 0, 0.53, {}, [3, 6, 10]);
for (const reach of [1.1, 1.2, 1.3]) test(`v7 + poker ${reach} in, z 5.3-6.5`, 0, 0.53, { pusher: { reach, z0: 5.3, z1: 6.5 } }, [3, 6, 8, 10]);
test(`v7 + poker 1.2 in, z 5.3-7.5 (taller)`, 0, 0.53, { pusher: { reach: 1.2, z0: 5.3, z1: 7.5 } }, [3, 6, 8, 10]);
test(`v8 + poker 1.0 in, z 5.3-6.5`, 0.3, 0.75, { pusher: { reach: 1.0, z0: 5.3, z1: 6.5 } }, [3, 6, 8, 10]);
