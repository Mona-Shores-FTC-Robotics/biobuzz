const S = require("./simlib.js");
const prof = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
const r = S.run(prof, { onRobot: true, tmax: 3.5, mu: 0.4, drive: 4, stopErr: -0.35, muBall: 0.4, gutter: { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 }, pattern: [-0.29, 0.35, 0.35], shaftBack: 0.53 });
const last = r.frames[r.frames.length - 1];
console.log(JSON.stringify({ profile: prof, lip: last[1], balls: last[2], fed: r.fed, t: last[0] }));
