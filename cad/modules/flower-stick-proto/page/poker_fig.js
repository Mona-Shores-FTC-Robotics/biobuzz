const S = require("./simlib.js");
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
const r = S.run(v7, { onRobot: true, tmax: 3.5, mu: 0.4, drive: 4, stopErr: -0.35, muBall: 0.4, gutter: { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 }, pattern: [-0.29, 0.35, 0.35], shaftBack: 0.53, pusher: { reach: 1.2, z0: 5.3, z1: 7.5 } });
const pick = [];
for (const t of [1.25, 1.34, 1.42, 1.55]) pick.push(r.frames.reduce((b, f) => Math.abs(f[0] - t) < Math.abs(b[0] - t) ? f : b));
console.log(JSON.stringify({ profile: v7, fed: r.fed, frames: pick }));
