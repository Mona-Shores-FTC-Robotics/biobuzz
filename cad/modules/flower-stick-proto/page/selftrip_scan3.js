const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const CASES = []; for (const pattern of [[0, 0, 0], [-0.29, 0.35, 0.35]]) for (const mu of [0.3, 0.45]) for (const drive of [3, 6]) for (const stopErr of [0, -0.35]) CASES.push({ mu, drive, stopErr, pattern });
function test(label, prof, more) {
  let ok = 0; const fails = [];
  for (const c of CASES) { const r = S.run(prof, Object.assign({ onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, shaftBack: 0.53 }, c, more || {})); if (r.fed.every(v => v !== null)) ok++; else fails.push(`${c.pattern[0] ? "fb" : "st"}/${c.mu}/${c.drive}${c.stopErr ? "s" : ""}`); }
  console.log(`${label.padEnd(60)} ${ok}/${CASES.length}  ${fails.join(" ")}`);
}
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
// the pin a 4 mm steel rod at (0.32, 0.96): the lip's bottom corner reaches its height at 54 deg, the body advancing 0.22
test(`pin (0.32, 0.96): 54 deg over 0.22 in, toe 1.3`, v7, { finger: { len: 1.3, deg: 54, advance: 0.22, pivot: [0.32, 0.96] }, extraDepth: 0.22 });
test(`pin (0.32, 0.96): 54 deg over 0.22 in, toe 1.5`, v7, { finger: { len: 1.5, deg: 54, advance: 0.22, pivot: [0.32, 0.96] }, extraDepth: 0.22 });
test(`pin (0.32, 0.96): 54 deg over 0.22 in, toe 1.3 bent up 0.1`, v7, { finger: { len: 1.3, deg: 54, advance: 0.22, pivot: [0.32, 0.96], rise: 0.1 }, extraDepth: 0.22 });
test(`same, mu 0.3 only, 12 in/s`, v7, { finger: { len: 1.3, deg: 54, advance: 0.22, pivot: [0.32, 0.96] }, extraDepth: 0.22, drive: 12 });
