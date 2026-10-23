const S = require("./simlib.js");
const lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const m = p => S.mountain(p);
const BL = { v7: [m({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), 0, 0.53], v8: [m({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), 0.3, 0.75], v9: [m({ edge: 0.6, nose: 60, peak: 1.0, tail: 10 }), 0, 0.39], "v7+0.6": [m({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), 0.6, 0.75], "peak0.9": [m({ edge: 0.6, nose: 60, peak: 0.9, tail: 10 }), 0, 0.39] };
const pats = { "settled FB1": [-0.29, 0.35, -0.86], "settled FB2": [-0.29, -0.86, 0.35], "settled FB3": [-0.29, 0.35, 0.35], "settled FB4": [-0.29, -0.86, -0.86], "collar only": [-0.29, 0, 0], "straight": [0, 0, 0], "side": null };
for (const [pn, pattern] of Object.entries(pats)) {
  const line = [];
  for (const [bn, [prof, extra, sb]] of Object.entries(BL)) { let ok = 0, n = 0; const f = [];
    for (const mu of [0.3, 0.4]) for (const [drive, stopErr] of [[3, 0], [4, 0], [6, 0], [4, -0.35], [8, 0]]) {
      const cfg = { onRobot: true, tmax: 3.5, mu, drive, stopErr, muBall: 0.4, gutter: lane, extraDepth: extra, shaftBack: sb };
      if (pattern) cfg.pattern = pattern; else cfg.lean = 0.6;
      const r = S.run(prof, cfg); n++;
      if (r.fed.every(v => v !== null)) ok++; else f.push(`${mu}/${drive}${stopErr ? "s" : ""}`);
    }
    line.push(`${bn} ${ok}/${n}${f.length ? "(" + f.join(",") + ")" : ""}`); }
  console.log(pn.padEnd(12), line.join(" | "));
}
