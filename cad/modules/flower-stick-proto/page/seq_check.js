const S = require("./simlib.js");
const LANE = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6, oval: 0 }, M = p => S.mountain(p);
const D = {
  "1 first block (Oct 15)": { profile: [[0, 0.52], [0, 0.64], [0.2, 1.30], [0.7, 1.30], [1.4, 0.52]], extraDepth: 0.57, shaftBack: 0.93, lane: null },
  "2 wedge with a low lane, v2 (Oct 16)": { profile: [[0, 0.52], [0, 0.60], [0.45, 1.05], [0.90, 0.99], [1.50, 0.52]], shaftBack: 0.69, lane: null },
  "3 first mountain, tall tail, v3 (Oct 16)": { profile: M({ edge: 0.53, nose: 55, peak: 1.28, tail: 45 }), shaftBack: 0.77, lane: null },
  "4 mountain that carries, v4 (Oct 16)": { profile: M({ edge: 0.53, nose: 55, peak: 1.28, tail: 14.4 }), shaftBack: 0.77, lane: null },
  "5 mountain + Carter's lane, v5/v7 (Oct 19)": { profile: M({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), shaftBack: 0.53 },
  "6 the 50 deg nose, v6 (Oct 22)": { profile: M({ edge: 0.6, nose: 50, peak: 1.1, tail: 12 }), shaftBack: 0.58 },
  "7 V nose, v8 (Oct 24)": { profile: M({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), shaftBack: 0.75, extraDepth: 0.3, vee: 0.3 },
  "8 low peak, v9 (Oct 24)": { profile: M({ edge: 0.6, nose: 60, peak: 1.0, tail: 10 }), shaftBack: 0.39 },
  "9 mountain + poker (Oct 25)": { profile: M({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), shaftBack: 0.53, pusher: { reach: 1.2, z0: 5.3, z1: 7.5, hw: 0.375 } },
};
for (const [name, P] of Object.entries(D)) {
  const out = [];
  for (const [sn, st] of [["front-back", { pattern: [-0.29, 0.35, 0.35] }], ["side-side", { lean: 0.6 }]]) {
    const cfg = Object.assign({ drive: 4, mu: 0.4, stopErr: 0, extraDepth: P.extraDepth || 0, onRobot: true, width: 5.4, gutter: P.lane === null ? null : LANE, offset: 0, yaw: 0, mouth: 7.5, muBall: 0.4, tmax: 3.5, shaftBack: P.shaftBack }, st);
    if (P.pusher) cfg.pusher = P.pusher;
    const r = S.run(P.profile, cfg);
    const fed = r.fed.filter(v => v !== null).length, end = r.frames[r.frames.length - 1][2];
    const onPeak = end.some(b => b[0] > 0.1 && b[0] < 0.7 && b[1] > 2.1 && b[1] < 2.8);
    out.push(`${sn}: ${r.stalled ? "STALL" : fed === 4 ? "feeds " + Math.max(...r.fed).toFixed(2) + "s" : (4 - fed) + " left" + (onPeak ? " (on the peak)" : "") + (r.missed.some(v => v !== null) ? " missed" : "")}`);
  }
  console.log(name.padEnd(44), out.join(" | "));
}
