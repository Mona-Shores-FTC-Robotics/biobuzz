const S = require("./simlib.js");
const FW = (a, mu) => { a *= Math.PI / 180; const d = Math.cos(a) - mu * Math.sin(a); return d <= 0 ? null : Math.sin(a) / d; };
// candidates with the 8 mm hex (flats top and bottom) at z 0.77 placed with its front corner under the peak; the hex's
// top flat is modelled where it is proud of the ramp (a step), and ignored where it is buried
function withHex(d, sz) {
  const base = S.mountain(d), xp = base[2][0], tt = Math.tan(d.tail * Math.PI / 180), sx = xp + 0.186;
  const top = sz + 0.161, x0 = sx - 0.186, x1 = sx + 0.186, zt = x => Math.max(0.52, d.peak - (x - xp) * tt);
  const proud = top - zt(x1);
  if (proud <= 0.0005) return { prof: base, proud, sx };
  const prof = [base[0], base[1], base[2]];
  if (top > zt(x0)) prof.push([x0, zt(x0)], [x0, top]); else prof.push([xp + (d.peak - top) / tt, top]);
  prof.push([x1, top], [x1, zt(x1)], base[3]);
  return { prof, proud, sx };
}
const C = [{ edge: 0.6, nose: 45, peak: 0.95, tail: 8 }, { edge: 0.56, nose: 45, peak: 0.95, tail: 8 }, { edge: 0.6, nose: 48, peak: 1.0, tail: 8 }, { edge: 0.6, nose: 50, peak: 1.0, tail: 8 }, { edge: 0.6, nose: 45, peak: 1.0, tail: 8 }, { edge: 0.6, nose: 50, peak: 1.0, tail: 10 }];
for (const d of C) for (const sz of [0.77, 0.75]) {
  const { prof, proud, sx } = withHex(d, sz); let ok = 0, worst = 0;
  for (const mu of [0.3, 0.45]) for (const drive of [3, 12]) for (const stopErr of [0, -0.2, -0.35]) {
    const r = S.run(prof, { onRobot: true, tmax: 3.5, mu, drive, stopErr, gutter: { R: 12.3, rise: 0.3, start: S.mountain(d)[2][0] + 0.05, lead: 1.2, oval: 1.2 } });
    if (r.fed.every(v => v !== null)) { ok++; worst = Math.max(worst, Math.max(...r.fed)); }
  }
  const margin = S.STOP - S.mountain(d)[2][0] - 0.35;
  console.log(`nose ${d.nose} edge ${d.edge} peak ${d.peak} tail ${d.tail}, shaft z ${sz} at ${sx.toFixed(2)}: hex ${proud > 0.0005 ? "proud " + (proud * 25.4).toFixed(1) + " mm" : "buried " + (-proud * 25.4).toFixed(1) + " mm"}; fed ${ok}/12 worst ${worst.toFixed(2)}; push ${FW(d.nose, 0.4).toFixed(1)} W; margin ${margin.toFixed(2)}; under ${((sz - 0.161 - 0.52) * 25.4).toFixed(1)} mm`);
}
