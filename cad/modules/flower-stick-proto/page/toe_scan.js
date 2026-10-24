const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const CASES = []; for (const pattern of [[0, 0, 0], [-0.29, 0.35, 0.35]]) for (const mu of [0.3, 0.45]) for (const drive of [3, 6]) for (const stopErr of [0, -0.35]) CASES.push({ mu, drive, stopErr, pattern });
function test(label, prof, extra, sb, more) {
  let ok = 0; const ends = [];
  for (const c of CASES) { const r = S.run(prof, Object.assign({ onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: extra, shaftBack: sb }, c, more || {})); if (r.fed.every(v => v !== null)) ok++; else { const l = r.frames[r.frames.length - 1][2]; ends.push(l.filter(b => b[0] > -2.4).map(b => `(${b[0].toFixed(2)},${b[1].toFixed(2)})`).join("")); } }
  console.log(`${label.padEnd(40)} ${ok}/${CASES.length}  ${ends.slice(0, 2).join(" | ")}`);
}
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
for (const [len, deg] of [[0.8, 60], [1.0, 60], [1.0, 75], [1.0, 90], [1.3, 60], [1.3, 75], [1.3, 90], [1.6, 75]]) test(`v7, toe ${len} in to ${deg} deg`, v7, 0, 0.53, { toe: { len, deg, time: 0.3 } });
test("v7, toe 1.3 in to 75 deg, slow (0.8 s)", v7, 0, 0.53, { toe: { len: 1.3, deg: 75, time: 0.8 } });
test("v8, toe 1.0 in to 75 deg", v7, 0.3, 0.75, { toe: { len: 1.0, deg: 75, time: 0.3 } });
