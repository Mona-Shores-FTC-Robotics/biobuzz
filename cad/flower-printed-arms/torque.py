"""What the FLOWER stick weighs, and what holding it against gravity asks of its servo, through the 0..162 deg swing:
today's aluminium arms against the printed ones.

    python3 cad/flower-printed-arms/torque.py    # prints the table and writes torque.json

Masses (grams) and where each one sits (u along the arm from the pivot, v across it, mm):
- **Printed arm:** PrusaSlicer's filament weight for arm.stl (PETG, 4 perimeters, 40 % gyroid; slice.ini), at the
  STL's centroid.
- **Today's arm:** an 11 Hole Mini U-Channel, ESTIMATED at 75 g from its section (2.5 mm 6061, 32 x 12 mm, about a
  quarter of its face holes), at u 120. Weigh one and change `ALU_ARM_G`.
- **At the tip (u 240), the same today and printed:** the 312 mm 8 mm REX steel shaft (0.395 g/mm round 8 mm steel),
  the mountain (the 31 Oct CAD's Flower Ramp, 96.5 cm^3 solid, printed: ESTIMATED 75 g, at its own centroid), the
  hubs, collars and bearings there (ESTIMATED 30 g). Today's two tip rollers (8.6 g each, the CAD's aluminium
  3400-0014-0032 volume) and two tip plates (ESTIMATED 20 g each) go; the printed version has two skids instead.
- The pivot axle and hub sit on the axis and cost no holding torque.

The torque is the weight's moment about the pivot axis: g x sum(m x horizontal distance), in kg.cm, the unit servo
makers quote. Against: goBILDA Torque 25.0 and Speed 9.3 kg.cm (the bounds the Hub gave), and SWYFT's Balance 27.3,
Torque 33.5 and Speed 19 (its table gives no unit; read as kg.cm). A servo's stall torque is the most it can give;
hold below about a third of it for a stick that must not droop when bumped.
"""
import json
import os
import sys

import numpy as np
import trimesh

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from geometry import SWING_DEG, TIP_U, arm_to_robot, PIVOT_XZ  # noqa: E402

ALU_ARM_G = 75.0            # ESTIMATE: weigh one
SHAFT_G = 0.395 * 312       # 8 mm round steel
MOUNTAIN_G = 75.0           # ESTIMATE: weigh it
MOUNTAIN_UV = (234.0, -6.0)  # the CAD ramp's centroid, near the tip axis
TIP_HARDWARE_G = 30.0       # ESTIMATE: hubs, collars, bearings at the tip
ROLLER_G = 8.6
TIP_PLATE_G = 20.0          # ESTIMATE
SERVOS = {"goBILDA Torque": 25.0, "goBILDA Speed": 9.3, "SWYFT Balance": 27.3, "SWYFT Torque": 33.5,
          "SWYFT Speed": 19.0}


def sliced_grams(name):
    path = os.path.join(HERE, "print-estimates.json")
    with open(path) as f:
        return json.load(f)[name]["grams_petg"]


def build(printed):
    """[(label, grams, u, v)] for both sides together."""
    items = []
    if printed:
        m = trimesh.load(os.path.join(HERE, "arm.stl"))
        cu, cv = m.centroid[0], m.centroid[1]
        items.append(("2 printed arms", 2 * sliced_grams("arm"), cu, cv))
        items.append(("2 skids", 2 * sliced_grams("foot_skid"), TIP_U, 0.0))
    else:
        items.append(("2 aluminium arms (estimate)", 2 * ALU_ARM_G, 120.0, 0.0))
        items.append(("2 tip rollers", 2 * ROLLER_G, TIP_U, 0.0))
        items.append(("2 tip plates (estimate)", 2 * TIP_PLATE_G, TIP_U - 12.0, 0.0))
    items.append(("8 mm REX shaft, 312 mm", SHAFT_G, TIP_U, 0.0))
    items.append(("mountain (estimate)", MOUNTAIN_G, *MOUNTAIN_UV))
    items.append(("tip hubs, collars, bearings (estimate)", TIP_HARDWARE_G, TIP_U, 0.0))
    return items


def torque_kgcm(items, theta):
    t = 0.0
    for _, g, u, v in items:
        x = arm_to_robot([u], [v], [0.0], theta)[0, 0]
        t += g / 1000.0 * (x - PIVOT_XZ[0]) / 10.0
    return t


def main():
    out = {}
    for label, printed in (("today", False), ("printed", True)):
        items = build(printed)
        angles = np.arange(0, SWING_DEG + 0.01, 1.0)
        tq = [torque_kgcm(items, a) for a in angles]
        k = int(np.argmax(np.abs(tq)))
        out[label] = {
            "mass_g": round(sum(i[1] for i in items), 0),
            "items": [{"what": i[0], "g": round(i[1], 1)} for i in items],
            "torque_kgcm_stowed_0deg": round(tq[0], 2),
            "torque_kgcm_pick_162deg": round(tq[-1], 2),
            "torque_kgcm_worst": round(tq[k], 2), "worst_at_deg": float(angles[k]),
        }
    worst = abs(out["printed"]["torque_kgcm_worst"])
    out["margin_printed"] = {s: f"{v / worst:.1f}x stall ({v / 3 / worst:.1f}x a third of stall)" for s, v in SERVOS.items()}
    worst0 = abs(out["today"]["torque_kgcm_worst"])
    out["margin_today"] = {s: f"{v / worst0:.1f}x stall" for s, v in SERVOS.items()}
    with open(os.path.join(HERE, "torque.json"), "w") as f:
        json.dump(out, f, indent=1)
    print(json.dumps(out, indent=1))


if __name__ == "__main__":
    main()
