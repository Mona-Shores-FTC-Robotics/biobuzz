// Design search for the mountain blade: a grid over edge height, nose angle, peak height and tail angle, each design
// scored over a robustness set (friction, drive speed, stop error). Prints one JSON line per design.
// usage: node opt.js <shard> <nshards>
const S = require("./simlib.js");
const [shard, nshards] = process.argv.slice(2).map(Number);
const EDGES = [0.52, 0.56, 0.60], NOSES = [45, 50, 55, 60, 65, 70], PEAKS = [0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5], TAILS = [10, 12, 14, 17, 20, 25, 30];
const SCEN = [];
for (const mu of [0.3, 0.45]) for (const drive of [3, 12]) for (const stopErr of [0, -0.2, -0.35]) SCEN.push({ mu, drive, stopErr });
const designs = [];
for (const edge of EDGES) for (const nose of NOSES) for (const peak of PEAKS) for (const tail of TAILS) designs.push({ edge, nose, peak, tail });
for (let k = shard; k < designs.length; k += nshards) {
  const d = designs[k], prof = S.mountain(d);
  const xp = prof[2][0], xe = prof[3][0];
  // printability: the 8 mm shaft at 0.77 behind the lip, 0.77 in high, needs the surface 0.99 in or more above the tiles there
  const surf = 0.77 < xp ? d.edge + 0.77 * Math.tan(d.nose * Math.PI / 180) : d.peak - (0.77 - xp) * Math.tan(d.tail * Math.PI / 180);
  const margin = S.STOP - xp - 0.35, tailEnd = S.STOP - xe;
  let ok = 0, tsum = 0, worst = 0, stalls = 0;
  for (const sc of SCEN) {
    const r = S.run(prof, Object.assign({ onRobot: true, tmax: 3.5 }, sc));
    const fed = r.fed.every(v => v !== null);
    if (r.stalled) stalls++;
    if (fed) { ok++; const t = Math.max(...r.fed) - (S.STOP - sc.stopErr + 4.4) / sc.drive * 0 ; tsum += Math.max(...r.fed); worst = Math.max(worst, Math.max(...r.fed)); }
  }
  console.log(JSON.stringify({ d, xp: +xp.toFixed(3), xe: +xe.toFixed(3), surf: +surf.toFixed(3), margin: +margin.toFixed(3), tailEnd: +tailEnd.toFixed(3), ok, n: SCEN.length, stalls, meanT: ok ? +(tsum / ok).toFixed(3) : null, worstT: ok ? +worst.toFixed(3) : null }));
}
