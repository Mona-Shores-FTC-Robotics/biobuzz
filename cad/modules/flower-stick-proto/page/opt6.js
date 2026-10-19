// v6 search: like opt.js, plus the push needed (F/W at the face, rolling, uprights mu 0.4) as a criterion. No shaft-skin
// constraint: v6 carries the shaft in end bosses. usage: node opt6.js <shard> <nshards>
const S = require("./simlib.js");
const [shard, nshards] = process.argv.slice(2).map(Number);
const EDGES = [0.52, 0.56, 0.60], NOSES = [40, 45, 50, 55], PEAKS = [0.9, 0.95, 1.0, 1.05, 1.1], TAILS = [10, 12, 14];
const SCEN = []; for (const mu of [0.3, 0.45]) for (const drive of [3, 12]) for (const stopErr of [0, -0.2, -0.35]) SCEN.push({ mu, drive, stopErr });
const designs = []; for (const edge of EDGES) for (const nose of NOSES) for (const peak of PEAKS) for (const tail of TAILS) designs.push({ edge, nose, peak, tail });
const FW = (a, mu) => { a *= Math.PI / 180; const d = Math.cos(a) - mu * Math.sin(a); return d <= 0 ? null : Math.sin(a) / d; };
for (let k = shard; k < designs.length; k += nshards) {
  const d = designs[k], prof = S.mountain(d), xp = prof[2][0], xe = prof[3][0];
  const margin = S.STOP - xp - 0.35, tailEnd = S.STOP - xe;
  const edgeA = 90 - Math.asin((1.4 - d.edge) / 1.4) * 180 / Math.PI;
  let ok = 0, worst = 0;
  for (const sc of SCEN) { const r = S.run(prof, Object.assign({ onRobot: true, tmax: 3.5 }, sc)); if (r.fed.every(v => v !== null)) { ok++; worst = Math.max(worst, Math.max(...r.fed)); } }
  console.log(JSON.stringify({ d, xp: +xp.toFixed(3), margin: +margin.toFixed(3), tailEnd: +tailEnd.toFixed(3), ok, n: SCEN.length, worstT: ok ? +worst.toFixed(3) : null, forceFace: FW(d.nose, 0.4) && +FW(d.nose, 0.4).toFixed(2), forceEdge: +FW(edgeA, 0.4).toFixed(2) }));
}
