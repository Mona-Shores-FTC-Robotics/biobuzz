// the coach's plate in the model, as drawn and turned flat, on the two stacks at the slow push, plus our blade at 7.9 wide
const S = require("./simlib.js"); const CS = require("./coach_stick.json");
const asProfile = pts => { const lip = Math.max(...pts.map(p => p[0])), out = pts.map(([a, z]) => [lip - a, z]); out.push(out[0]); return { lip, profile: out }; };
const drawn = asProfile(CS.deployed_outline["-0.43"]);
const sh0 = CS.shaft_stowed, sh1 = CS.shaft_deployed;
const flat = asProfile(CS.stowed_outline_centre.map(([a, z]) => [a + sh1[0] - sh0[0], z + sh1[1] - sh0[1]]));
const v7 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 }), lane = { R: 12.3, rise: 0.3, start: 0.9, lead: 1.6 };
const STACKS = { fb: { pattern: [-0.29, 0.35, 0.35] }, ss: { lean: 0.6 } };
function go(name, profile, extra) {
  for (const drive of [4, 10]) for (const st of ["fb", "ss"]) {
    const cfg = Object.assign({ drive, mu: 0.4, stopErr: 0, onRobot: true, mouth: 7.5, muBall: 0.4, tmax: 3.5 }, extra, STACKS[st]);
    const r = S.run(profile, cfg); const fed = r.fed.filter(v => v !== null).length;
    const end = r.frames[r.frames.length - 1][2];
    console.log(`${name.padEnd(28)} ${drive} in/s ${st}: fed ${fed}/4${r.stalled ? " stalled " + r.stalled[0].toFixed(2) : ""}${r.missed.some(v => v !== null) ? " missed" : ""}  balls end: ${end.map(b => "(" + b[0].toFixed(1) + "," + b[1].toFixed(1) + ")").join(" ")}`);
  }
}
go("coach as drawn", drawn.profile, { width: 7.9, shaftBack: drawn.lip - S.SHAFT_AHEAD, gutter: null });
go("coach turned flat", flat.profile, { width: 7.9, shaftBack: flat.lip - S.SHAFT_AHEAD, gutter: null });
go("mountain 7.9 wide", v7, { width: 7.9, shaftBack: 0.53, gutter: lane });
go("mountain 7.9 + poker", v7, { width: 7.9, shaftBack: 0.53, gutter: lane, pusher: { reach: 1.2, z0: 5.3, z1: 7.5, hw: 0.375 } });
console.log("lips ahead of face: drawn", drawn.lip.toFixed(2), "flat", flat.lip.toFixed(2));
