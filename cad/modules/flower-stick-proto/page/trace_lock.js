const S = require("./simlib.js");
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
for (const [name, extra, sb, more] of [["v7", 0, 0.53, {}], ["v8", 0.3, 0.75, {}], ["v7 flap 1.0in 45deg", 0, 0.53, { flap: { len: 1.0, deg: 45, time: 0.3 } }]]) {
  const r = S.run(v7, Object.assign({ onRobot: true, tmax: 3.5, mu: 0.4, drive: 4, stopErr: -0.35, muBall: 0.4, gutter: { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 }, pattern: [-0.29, 0.35, 0.35], shaftBack: sb, extraDepth: extra }, more));
  const last = r.frames[r.frames.length - 1];
  console.log(name, "fed", r.fed.map(v => v && v.toFixed(2)), "end lip", last[1].toFixed(2), last[2].map(b => `(${b[0].toFixed(2)},${b[1].toFixed(2)},y${b[2].toFixed(2)})`).join(" "));
}
