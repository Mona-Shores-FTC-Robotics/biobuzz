const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const CASES = []; for (const pattern of [[0, 0, 0], [-0.29, 0.35, 0.35]]) for (const mu of [0.3, 0.45]) for (const drive of [3, 6]) for (const stopErr of [0, -0.35]) CASES.push({ mu, drive, stopErr, pattern });
function test(label, prof, more) {
  let ok = 0; const fails = [];
  for (const c of CASES) { const r = S.run(prof, Object.assign({ onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, shaftBack: 0.53 }, c, more || {})); if (r.fed.every(v => v !== null)) ok++; else fails.push(`${c.pattern[0] ? "fb" : "st"}/${c.mu}/${c.drive}${c.stopErr ? "s" : ""}`); }
  console.log(`${label.padEnd(60)} ${ok}/${CASES.length}  ${fails.join(" ")}`);
}
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
// a real pin: 4 mm, knuckle 0.3 in across, centred 0.14 below the peak at (0.29, 0.96); the lip's bottom corner reaches its
// height at 57 deg after the body advances 0.24 in
const pin = [0.289, 0.96];
for (const len of [1.3, 1.6]) for (const rise of [0, 0.15]) test(`pin (0.29, 0.96): 57 deg over 0.24 in, toe ${len}${rise ? " bent up " + rise : ""}`, v7, { finger: { len, deg: 57, advance: 0.24, pivot: pin, rise }, extraDepth: 0.24 });
test(`pin (0.29, 0.96) but a stop lets it reach 70 deg over 0.3 in, toe 1.3`, v7, { finger: { len: 1.3, deg: 70, advance: 0.3, pivot: pin }, extraDepth: 0.3 });
