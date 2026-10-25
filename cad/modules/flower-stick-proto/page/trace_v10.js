const S = require("./simlib.js");
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
const base = { onRobot: true, tmax: 3.5, mu: 0.4, drive: 4, stopErr: -0.35, muBall: 0.4, gutter: { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 }, pattern: [-0.29, 0.35, 0.35], shaftBack: 0.53 };
for (const [name, more] of [["v10 (tilt + 0.22 advance)", { finger: { len: 0, deg: 54, advance: 0.22, pivot: [0.32, 0.96] }, extraDepth: 0.22 }], ["0.22 advance only", { extraDepth: 0.22 }], ["v7", {}]]) {
  const r = S.run(v7, Object.assign({}, base, more));
  console.log("==", name, "fed", r.fed.map(v => v && v.toFixed(2)));
  for (const f of r.frames) if (f[0] >= 1.26 && f[0] <= 1.62 && Math.round(f[0] * 100) % 3 === 0) console.log(f[0].toFixed(2), "lip", f[1].toFixed(2), "nose", (f[3] || 0).toFixed(0) + "°", "b1", `(${f[2][0][0].toFixed(2)}, ${f[2][0][1].toFixed(2)})`, "b2", `(${f[2][1][0].toFixed(2)}, ${f[2][1][1].toFixed(2)})`);
}
