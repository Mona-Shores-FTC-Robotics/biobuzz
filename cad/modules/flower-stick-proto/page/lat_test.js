const S = require("./simlib.js");
const v5 = S.mountain({ edge: 0.6, nose: 60, peak: 1.1, tail: 12 });
function go(cfg, label) {
  const r = S.run(v5, Object.assign({ drive: 4, mu: 0.4, onRobot: true, tmax: 4 }, cfg));
  const tf = r.fed.every(v => v !== null) ? Math.max(...r.fed).toFixed(2) : "no";
  console.log(label.padEnd(44), "fed", r.fed.map(v => v === null ? "-" : v.toFixed(2)).join(" "), "miss", r.missed.filter(v => v !== null).length, "off", r.offBlade.filter(v => v !== null).length, "y@wheels", JSON.stringify(r.yAtWheels), "stall", r.stalled ? "Y" : "n", "t", tf);
}
go({}, "v5 plain, centred");
go({ offset: 1.2 }, "plain, robot 1.2 off");
go({ lean: 0.8 }, "plain, stack leaning 0.8");
go({ yaw: 10 }, "plain, robot turned 10");
go({ offset: 1.2, lean: 0.8, yaw: 10 }, "plain, all three");
go({ mouth: 3 }, "plain, 3 in mouth");
go({ mouth: 3, offset: 1.2 }, "plain, 3 in mouth, 1.2 off");
const gut = { R: 3, rise: 0.3, start: 0.4 };
go({ gutter: gut }, "gutter R3 rise.3, centred");
go({ gutter: gut, offset: 1.2 }, "gutter, 1.2 off");
go({ gutter: gut, lean: 0.8 }, "gutter, lean 0.8");
go({ gutter: gut, yaw: 10 }, "gutter, turned 10");
go({ gutter: gut, offset: 1.2, lean: 0.8, yaw: 10 }, "gutter, all three");
go({ gutter: gut, mouth: 3, offset: 1.2 }, "gutter, 3 in mouth, 1.2 off");
go({ gutter: gut, mouth: 3, offset: 1.2, yaw: 10, lean: 0.8 }, "gutter, 3 in mouth, all three");
