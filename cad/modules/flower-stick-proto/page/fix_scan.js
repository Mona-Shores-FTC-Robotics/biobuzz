const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const fmt = b => `(${b[0].toFixed(2)},${b[1].toFixed(2)})`;
// the stuck scenario: a straight stack, pushed slowly, the sled seated or 0.35 short; mu 0.3 and 0.45
const PAT = process.argv[2] ? JSON.parse(process.argv[2]) : [0, 0, 0];
const CASES = []; for (const mu of [0.3, 0.45]) for (const drive of [3, 6]) for (const stopErr of [0, -0.35]) CASES.push({ mu, drive, stopErr });
function test(label, prof, extra, sb, more) {
  let ok = 0; const fails = [];
  for (const c of CASES) {
    const r = S.run(prof, Object.assign({ onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: extra, shaftBack: sb, pattern: PAT }, c, more || {}));
    if (r.fed.every(v => v !== null)) ok++; else fails.push(`mu${c.mu}/d${c.drive}/s${c.stopErr}`);
  }
  console.log(`${label.padEnd(44)} ${ok}/${CASES.length}  ${fails.join(" ")}`);
}
const m = p => S.mountain(p);
test("v7 (60/1.10/12)", m({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), 0, 0.53);
test("v8 = v7 + 0.3 deeper (V)", m({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), 0.3, 0.45);
test("v9 (60/1.00/10)", m({ edge: 0.6, nose: 60, peak: 1.0, tail: 10 }), 0, 0.39);
test("v7 + 0.6 deeper", m({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), 0.6, 0.45);
test("v7 + 0.9 deeper", m({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), 0.9, 0.45);
test("v9 + 0.3 deeper", m({ edge: 0.6, nose: 60, peak: 1.0, tail: 10 }), 0.3, 0.39);
test("v9 + 0.6 deeper", m({ edge: 0.6, nose: 60, peak: 1.0, tail: 10 }), 0.6, 0.39);
test("peak 0.9 (60/0.90/10)", m({ edge: 0.6, nose: 60, peak: 0.9, tail: 10 }), 0, 0.39);
test("peak 0.9 + 0.3 deeper", m({ edge: 0.6, nose: 60, peak: 0.9, tail: 10 }), 0.3, 0.39);
test("v7, nose 65", m({ edge: 0.6, nose: 65, peak: 1.1, tail: 12 }), 0, 0.53);
test("v7, nose 55", m({ edge: 0.6, nose: 55, peak: 1.1, tail: 12 }), 0, 0.53);
test("v7, edge 0.7", m({ edge: 0.7, nose: 60, peak: 1.1, tail: 12 }), 0, 0.53);
test("v7, tail 18", m({ edge: 0.6, nose: 60, peak: 1.1, tail: 18 }), 0, 0.53);
test("v7, tilt 6 deg nose-up after stop", m({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), 0, 0.53, { tilt: 6 });
test("v7, tilt -6 deg nose-down after stop", m({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), 0, 0.53, { tilt: -6 });
test("v7, lower collar (3.6)", m({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), 0, 0.53, { collar: 3.6 });
test("v7, higher collar (4.2)", m({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), 0, 0.53, { collar: 4.2 });
test("v7, ball friction 0.25", m({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), 0, 0.53, { muBall: 0.25 });
test("v7, ball friction 0.6", m({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), 0, 0.53, { muBall: 0.6 });
