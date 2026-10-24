const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const CASES = []; for (const pattern of [[0, 0, 0], [-0.29, 0.35, 0.35]]) for (const mu of [0.3, 0.45]) for (const drive of [3, 6]) for (const stopErr of [0, -0.35]) CASES.push({ mu, drive, stopErr, pattern });
function test(label, prof, extra, sb, more) {
  let ok = 0;
  for (const c of CASES) { const r = S.run(prof, Object.assign({ onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: extra, shaftBack: sb }, c, more || {})); if (r.fed.every(v => v !== null)) ok++; }
  console.log(`${label.padEnd(50)} ${ok}/${CASES.length}`);
}
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
test("v7, toe hinged at the lip, 1.3 in to 75 deg", v7, 0, 0.53, { toe: { len: 1.3, deg: 75, time: 0.3 } });
for (const deg of [20, 30, 40, 50]) test(`v7, finger about the shaft, no toe, ${deg} deg`, v7, 0, 0.53, { finger: { len: 0, deg, time: 0.3 } });
for (const len of [0.8, 1.3]) for (const deg of [20, 30, 40, 50]) test(`v7, finger about the shaft, toe ${len} in, ${deg} deg`, v7, 0, 0.53, { finger: { len, deg, time: 0.3 } });
test("v7, finger, toe 1.3, 40 deg, slow 0.6 s", v7, 0, 0.53, { finger: { len: 1.3, deg: 40, time: 0.6 } });
test("v7, finger, toe 1.3, 40 deg, after 0.3 s delay", v7, 0, 0.53, { finger: { len: 1.3, deg: 40, time: 0.3, delay: 0.3 } });
