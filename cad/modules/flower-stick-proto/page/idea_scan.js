const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const CASES = []; for (const pattern of [[0, 0, 0], [-0.29, 0.35, 0.35]]) for (const mu of [0.3, 0.45]) for (const drive of [3, 6]) for (const stopErr of [0, -0.35]) CASES.push({ mu, drive, stopErr, pattern });
function test(label, extra, sb, more, tmax) {
  let ok = 0; const fails = [];
  for (const c of CASES) { const r = S.run(v7, Object.assign({ onRobot: true, tmax: tmax || 3.5, muBall: 0.4, gutter: lane, extraDepth: extra, shaftBack: sb }, c, more)); if (r.fed.every(v => v !== null)) ok++; else fails.push(`${c.pattern[0] ? "fb" : "st"}/${c.mu}/${c.drive}${c.stopErr ? "s" : ""}`); }
  console.log(`${label.padEnd(58)} ${ok}/${CASES.length}  ${fails.join(" ")}`);
}
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
test("v7", 0, 0.53, {}); test("v8", 0.3, 0.75, {});
for (const reach of [0.6, 0.9, 1.2]) for (const [z0, z1] of [[4.4, 5.6], [4.4, 6.5], [5.0, 6.5]]) test(`v7 + pusher ${reach} in ahead of the face, z ${z0}-${z1}`, 0, 0.53, { pusher: { reach, z0, z1 } });
test(`v8 + pusher 0.9 in, z 4.4-5.6`, 0.3, 0.75, { pusher: { reach: 0.9, z0: 4.4, z1: 5.6 } });
for (const retreat of [1.0, 2.0]) for (const speed of [8, 12]) test(`v7 + re-ram: back ${retreat} in, in again at ${speed} in/s`, 0, 0.53, { ram: { dwell: 0.3, retreat, speed } }, 5.0);
test(`v7 + re-ram twice? (back 1.5, 12 in/s), longer`, 0, 0.53, { ram: { dwell: 0.3, retreat: 1.5, speed: 12 } }, 5.0);
