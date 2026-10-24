const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const CASES = []; for (const pattern of [[0, 0, 0], [-0.29, 0.35, 0.35]]) for (const mu of [0.3, 0.45]) for (const drive of [3, 6]) for (const stopErr of [0, -0.35]) CASES.push({ mu, drive, stopErr, pattern });
function test(label, prof, more) {
  let ok = 0;
  for (const c of CASES) { const r = S.run(prof, Object.assign({ onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: 0, shaftBack: 0.53 }, c, more || {})); if (r.fed.every(v => v !== null)) ok++; }
  console.log(`${label.padEnd(56)} ${ok}/${CASES.length}`);
}
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
for (const [pname, pivot] of [["the peak (0.29, 1.10)", [0.289, 1.1]], ["0.3 behind the lip, 0.6 up", [0.3, 0.6]], ["0.15 behind the lip, 0.6 up", [0.15, 0.6]], ["0.3 behind the lip, 0.9 up (axle in the nose)", [0.3, 0.9]]])
  for (const deg of [45, 60, 75]) test(`toe 1.3 in + nose, hinged at ${pname}, ${deg} deg`, v7, { finger: { len: 1.3, deg, time: 0.3, pivot } });
