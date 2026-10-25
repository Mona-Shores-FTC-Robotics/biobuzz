const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const CASES = []; for (const pattern of [[0, 0, 0], [-0.29, 0.35, 0.35]]) for (const mu of [0.3, 0.45]) for (const drive of [3, 6]) for (const stopErr of [0, -0.35]) CASES.push({ mu, drive, stopErr, pattern });
function test(label, more) {
  let ok = 0; const fails = [];
  for (const c of CASES) { const r = S.run(v7, Object.assign({ onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, shaftBack: 0.53 }, c, more)); if (r.fed.every(v => v !== null)) ok++; else fails.push(`${c.pattern[0] ? "fb" : "st"}/${c.mu}/${c.drive}${c.stopErr ? "s" : ""}`); }
  console.log(`${label.padEnd(60)} ${ok}/${CASES.length}  ${fails.join(" ")}`);
}
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
test(`v7 reaching 0.22 deeper, nothing moving`, { extraDepth: 0.22 });
test(`nose piece turns 54 deg after the stop, body does not advance`, { finger: { len: 0, deg: 54, time: 0.15, pivot: [0.32, 0.96] } });
test(`nose piece turns 54 deg as the body advances 0.22 (v10)`, { finger: { len: 0, deg: 54, advance: 0.22, pivot: [0.32, 0.96] }, extraDepth: 0.22 });
test(`same with a 0.6 in stick`, { finger: { len: 0.6, deg: 54, advance: 0.22, pivot: [0.32, 0.96] }, extraDepth: 0.22 });
test(`v10, no toe, but only 35 deg (a stiffer hinge or a stop)`, { finger: { len: 0, deg: 35, advance: 0.14, pivot: [0.32, 0.96] }, extraDepth: 0.14 });
test(`v10, no toe, 12 in/s`, { finger: { len: 0, deg: 54, advance: 0.22, pivot: [0.32, 0.96] }, extraDepth: 0.22, drive: 12 });
