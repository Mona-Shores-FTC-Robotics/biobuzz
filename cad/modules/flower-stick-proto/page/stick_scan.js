const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const CASES = []; for (const pattern of [[0, 0, 0], [-0.29, 0.35, 0.35]]) for (const mu of [0.3, 0.45]) for (const drive of [3, 6]) for (const stopErr of [0, -0.35]) CASES.push({ mu, drive, stopErr, pattern });
function test(label, more) {
  let ok = 0; const fails = [];
  for (const c of CASES) { const r = S.run(v7, Object.assign({ onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, shaftBack: 0.53 }, c, more)); if (r.fed.every(v => v !== null)) ok++; else fails.push(`${c.pattern[0] ? "fb" : "st"}/${c.mu}/${c.drive}${c.stopErr ? "s" : ""}`); }
  console.log(`${label.padEnd(44)} ${ok}/${CASES.length}  ${fails.join(" ")}`);
}
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
for (const len of [0.4, 0.6, 0.8, 1.0, 1.3]) test(`self-tripping, 54 deg, toe ${len} in`, { finger: { len, deg: 54, advance: 0.22, pivot: [0.32, 0.96] }, extraDepth: 0.22 });
for (const len of [0.6, 0.8]) test(`same, toe ${len} in bent up 0.15`, { finger: { len, deg: 54, advance: 0.22, pivot: [0.32, 0.96], rise: 0.15 }, extraDepth: 0.22 });
test(`no toe at all: just the nose piece turning`, { finger: { len: 0, deg: 54, advance: 0.22, pivot: [0.32, 0.96] }, extraDepth: 0.22 });
