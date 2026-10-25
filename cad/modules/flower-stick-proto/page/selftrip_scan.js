const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const CASES = []; for (const pattern of [[0, 0, 0], [-0.29, 0.35, 0.35]]) for (const mu of [0.3, 0.45]) for (const drive of [3, 6]) for (const stopErr of [0, -0.35]) CASES.push({ mu, drive, stopErr, pattern });
function test(label, prof, more) {
  let ok = 0; const fails = [];
  for (const c of CASES) { const r = S.run(prof, Object.assign({ onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, shaftBack: 0.53 }, c, more || {})); if (r.fed.every(v => v !== null)) ok++; else fails.push(`${c.pattern[0] ? "fb" : "st"}/${c.mu}/${c.drive}${c.stopErr ? "s" : ""}`); }
  console.log(`${label.padEnd(58)} ${ok}/${CASES.length}  ${fails.join(" ")}`);
}
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
// the pin at the peak (0.29 behind the lip, 1.10 up); the lip's bottom corner reaches the pin's height at 63 deg after the
// body has advanced 0.36 in; the body's stop is that much deeper than the posts
const peak = [0.289, 1.1];
for (const len of [1.3, 1.6, 2.0]) test(`pin at the peak, self-limits at 63 deg, toe ${len} in`, v7, { finger: { len, deg: 63, advance: 0.36, pivot: peak }, extraDepth: 0.36 });
for (const len of [1.3, 1.6]) test(`same, 75 deg if a stop let it (advance 0.45)`, v7, { finger: { len, deg: 75, advance: 0.45, pivot: peak }, extraDepth: 0.45 });
test(`pin at the peak, 63 deg, toe 1.6, time-driven (for comparison)`, v7, { finger: { len: 1.6, deg: 63, time: 0.3, pivot: peak } });
test(`v7 alone`, v7, {});
