const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const PATS = { st: [0, 0, 0], fb: [-0.29, 0.35, 0.35], fb2: [-0.29, -0.86, 0.35], fb3: [-0.29, 0.86, -0.86], wall: [0.35, -0.29, 0.35], ss: null };
const CASES = []; for (const pn of Object.keys(PATS)) for (const mu of [0.3, 0.45]) for (const drive of [3, 6]) for (const stopErr of [0, -0.35]) CASES.push({ pn, mu, drive, stopErr });
function test(label, extra, sb, more) {
  let ok = 0; const fails = [];
  for (const c of CASES) { const cfg = Object.assign({ onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: extra, shaftBack: sb, mu: c.mu, drive: c.drive, stopErr: c.stopErr }, more); if (PATS[c.pn]) cfg.pattern = PATS[c.pn]; else cfg.lean = 0.6;
    const r = S.run(v7, cfg); if (r.fed.every(v => v !== null)) ok++; else fails.push(`${c.pn}/${c.mu}/${c.drive}${c.stopErr ? "s" : ""}`); }
  console.log(`${label.padEnd(52)} ${ok}/${CASES.length}  ${fails.join(" ")}`);
}
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
test("v7", 0, 0.53, {}); test("v8", 0.3, 0.75, {});
for (const reach of [1.0, 1.2, 1.4]) test(`v7 + pusher ${reach} in ahead, z 5.3-6.5 (above the sockets)`, 0, 0.53, { pusher: { reach, z0: 5.3, z1: 6.5 } });
test(`v8 + pusher 1.0 in ahead, z 5.3-6.5`, 0.3, 0.75, { pusher: { reach: 1.0, z0: 5.3, z1: 6.5 } });
test(`v7 + pusher 1.2, z 5.3-6.5, pushed at 12 in/s`, 0, 0.53, { pusher: { reach: 1.2, z0: 5.3, z1: 6.5 }, drive: 12 });
