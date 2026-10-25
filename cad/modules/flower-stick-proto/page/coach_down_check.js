// the plate from the flowerextractordown export, at full depth, in the model; and the poker's reach with the face where that export's arm puts it
const S = require("./simlib.js"); const P = require("./coach_down_profile.json");
const prof = P.profile.slice(); prof.push(prof[0]);
const fbl = P.lip_ahead, sb = P.lip_ahead - P.shaft_full[0];
const STACKS = { fb: { pattern: [-0.29, 0.35, 0.35] }, ss: { lean: 0.6 } };
for (const drive of [4, 10]) for (const st of ["fb", "ss"]) { const r = S.run(prof, Object.assign({ drive, mu: 0.4, stopErr: 0, onRobot: true, mouth: 7.5, muBall: 0.4, tmax: 3.5, width: 7.9, shaftBack: sb, faceBehindLip: fbl, gutter: null }, STACKS[st])); console.log(`coach plate, full depth, ${drive} in/s ${st}: fed ${r.fed.filter(v => v !== null).length}/4${r.stalled ? " stalled" : ""}`); }
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const PATS = { st: [0, 0, 0], fb: [-0.29, 0.35, 0.35], fb2: [-0.29, -0.86, 0.35], fb3: [-0.29, 0.86, -0.86], wall: [0.35, -0.29, 0.35], ss: null };
const face = P.shaft_full[0] + 0.53;
console.log(`mountain blade on this arm: face ${face.toFixed(2)} behind the lip`);
for (const reach of [1.2, 1.6, 1.9, 2.0, 2.1, 2.2, 2.4]) { const out = [];
  for (const drive of [3, 6, 10]) { let ok = 0, n = 0; for (const pn of Object.keys(PATS)) for (const mu of [0.3, 0.45]) for (const stopErr of [0, -0.35]) { n++; const cfg = { onRobot: true, tmax: 3.5, muBall: 0.4, gutter: lane, extraDepth: 0, shaftBack: 0.53, faceBehindLip: face, mu, drive, stopErr, width: 7.9, pusher: { reach, z0: 5.3, z1: 7.5, hw: 0.375 } }; if (PATS[pn]) cfg.pattern = PATS[pn]; else cfg.lean = 0.6; if (S.run(v7, cfg).fed.every(v => v !== null)) ok++; } out.push(`${drive} in/s ${ok}/${n}`); }
  console.log(`poker ${reach} in ahead of the face: ${out.join(" | ")}`); }
