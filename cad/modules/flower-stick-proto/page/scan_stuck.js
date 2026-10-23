const S = require("./simlib.js");
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const fmt = b => `(${b[0].toFixed(2)},${b[1].toFixed(2)})`;
const pats = { straight: [0, 0, 0] };
for (const a of [0.3, 0.6, 0.9]) { pats[`A${a}`] = [-a, a, -a]; pats[`B${a}`] = [a, -a, a]; pats[`leanR${a}`] = [-a, -a, -a]; pats[`leanW${a}`] = [a, a, a]; pats[`topR${a}`] = [0, -a, -a]; pats[`topW${a}`] = [0, a, a]; }
for (const [name, pattern] of Object.entries(pats)) {
  let ok = 0, n = 0; const notes = [];
  for (const mu of [0.3, 0.45]) for (const drive of [3, 6, 12]) for (const stopErr of [0, -0.35]) {
    n++;
    const r = S.run(v7, { onRobot: true, tmax: 3.5, mu, drive, stopErr, muBall: 0.4, gutter: lane, shaftBack: 0.53, pattern });
    if (r.fed.every(v => v !== null)) { ok++; continue; }
    const last = r.frames[r.frames.length - 1][2];
    const left = last.filter(b => b[0] > -2.4);
    notes.push(`mu${mu} d${drive} s${stopErr}: lip ${r.lip.toFixed(2)} left ${left.map(fmt).join(" ")}`);
  }
  console.log(`${name.padEnd(9)} ${ok}/${n}`); for (const s of notes) console.log("   " + s);
}
