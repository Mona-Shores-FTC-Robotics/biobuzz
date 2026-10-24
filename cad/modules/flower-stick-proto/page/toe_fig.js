const S = require("./simlib.js");
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
const r = S.run(v7, { onRobot: true, tmax: 3.5, mu: 0.4, drive: 4, stopErr: -0.35, muBall: 0.4, gutter: { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 }, pattern: [-0.29, 0.35, 0.35], shaftBack: 0.53, toe: { len: 1.3, deg: 75, time: 0.3 } });
console.log(JSON.stringify({ profile: v7, fed: r.fed, frames: r.frames.filter((f, i) => i % 10 === 0 && f[3] > 0).slice(0, 12) }));
