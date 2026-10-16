// The sideways sweep: tail gutter shapes against the ways a run can be off, for v5's profile. One JSON line per case.
const S = require("./simlib.js");
const v5 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
const GUT = [null, { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 }, { R: 7.5, rise: 0.5, start: 0.9, lead: 1.6 }, { R: 4, rise: 0.4, start: 0.9, lead: 1.6 }, { R: 12.3, rise: 0.3, start: 0.9, lead: 0.4 }, { R: 12.3, rise: 0.3, start: 0.5, lead: 1.6 }];
const [shard, n] = process.argv.slice(2).map(Number); let k = 0;
for (const gutter of GUT) for (const offset of [0, 0.8, 1.3]) for (const lean of [0, 0.3, 0.6]) for (const yaw of [0, 8]) for (const mouth of [7.5, 4, 3]) {
  if (k++ % n !== shard) continue;
  const r = S.run(v5, { drive: 4, mu: 0.4, onRobot: true, tmax: 4, gutter, offset, lean, yaw, mouth });
  console.log(JSON.stringify({ gutter, offset, lean, yaw, mouth, fed: r.fed.filter(v => v !== null).length, missed: r.missed.filter(v => v !== null).length, off: r.offBlade.filter(v => v !== null).length, stalled: !!r.stalled, t: r.fed.every(v => v !== null) ? +Math.max(...r.fed).toFixed(2) : null, y: r.yAtWheels }));
}
