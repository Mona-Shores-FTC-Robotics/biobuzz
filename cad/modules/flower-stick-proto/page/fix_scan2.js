const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const CASES = []; for (const pattern of [[0, 0, 0], [-0.29, 0.35, 0.35]]) for (const mu of [0.3, 0.45]) for (const drive of [3, 6]) for (const stopErr of [0, -0.35]) CASES.push({ mu, drive, stopErr, pattern });
function test(label, prof, extra, sb, more) {
  let ok = 0;
  for (const c of CASES) { const r = S.run(prof, Object.assign({ onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: extra, shaftBack: sb }, c, more || {})); if (r.fed.every(v => v !== null)) ok++; }
  console.log(`${label.padEnd(46)} ${ok}/${CASES.length}`);
}
const m = p => S.mountain(p), v7 = m({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), v9 = m({ edge: 0.6, nose: 60, peak: 1.0, tail: 10 });
test("v7", v7, 0, 0.53); test("v8 (V, 0.3 deeper)", v7, 0.3, 0.75); test("v9 (peak 1.0)", v9, 0, 0.39);
test("v7 + 0.6 deeper", v7, 0.6, 0.75); test("v7 + 0.9 deeper", v7, 0.9, 0.75); test("v7 + 1.2 deeper", v7, 1.2, 0.75);
test("peak 0.9", m({ edge: 0.6, nose: 60, peak: 0.9, tail: 10 }), 0, 0.39); test("peak 0.9 + 0.6 deeper", m({ edge: 0.6, nose: 60, peak: 0.9, tail: 10 }), 0.6, 0.75);
test("v7, nose 55", m({ edge: 0.6, nose: 55, peak: 1.1, tail: 12 }), 0, 0.53); test("v7, nose 65", m({ edge: 0.6, nose: 65, peak: 1.1, tail: 12 }), 0, 0.53);
test("v7, tilt 6 nose-up", v7, 0, 0.53, { tilt: 6 });
test("v7, flap 0.8 in to 30 deg", v7, 0, 0.53, { flap: { len: 0.8, deg: 30, time: 0.3 } });
test("v7, flap 1.0 in to 45 deg", v7, 0, 0.53, { flap: { len: 1.0, deg: 45, time: 0.3 } });
test("v7, flap 1.4 in to 45 deg", v7, 0, 0.53, { flap: { len: 1.4, deg: 45, time: 0.3 } });
test("v7, flap 1.0 in to 60 deg", v7, 0, 0.53, { flap: { len: 1.0, deg: 60, time: 0.3 } });
test("v8, flap 1.0 in to 45 deg", v7, 0.3, 0.75, { flap: { len: 1.0, deg: 45, time: 0.3 } });
test("v7, collar 0.3 higher", v7, 0, 0.53, { collar: 4.2 }); test("v7, drive 12 only (fast push)", v7, 0, 0.53, { drive: 12 });
