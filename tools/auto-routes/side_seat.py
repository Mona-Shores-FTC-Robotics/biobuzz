"""R-Quals seated beside the far FLOWER (routes chat, 8 Oct 2026). qual-south-v meets a left partner still at the
standard left start (59, 132.25) at about 6.8 s: its far FLOWER seat and N_FIRE overlap that start. An extractor at
a front corner of the V, 7.3 in off the centre line (RobotDesign.extractorLateralIn), seats the robot at x 40.06
instead of 47.36; qual-south-v-side and qual-south-v-corner (alone.side_seat) stay at y <= 112 near the partner's
x 50-68 (the corner route fires TIP 2 from N_LOW, the lane's top facing south at y 115.5).

    python3 side_seat.py [runs]     exports both routes and prints the matrix against the left test partners
"""
import sys
from collections import defaultdict

import alone
import autogen

ROUTES = {"RQualsAuto": "rigid V, fixed turret", "QualSouthVSideAuto": "rigid V, fixed turret, corner extractor",
          "QualSouthVCornerAuto": "rigid V, fixed turret, corner extractor"}
# The left test partners (alone.py), and two that hold at their start until 18 s if they are late (partner_left_hold).
PARTNERS = ["PartnerLeftVAuto", "PartnerLeftSlow3000Auto", "PartnerLeftDeadAuto", "PartnerLeftSilentAuto",
            "PartnerLeftDeadWestAuto", "PartnerLeftSlow3000HoldAuto", "PartnerLeftSilentHoldAuto"]


def table(lines):
    """SEEDROW spec|design|seed|tips|points|collide|robot0|robot1 -> rows of counts."""
    rows = defaultdict(lambda: defaultdict(int))
    for l in lines:
        if not l.startswith("STUDY SEEDROW "):
            continue
        spec, design, seed, tips, pts, coll, ours, partner = l[len("STUDY SEEDROW "):].split("|")
        k = rows[(spec, design)]
        k["runs"] += 1
        k["tips%s" % tips] += 1
        k["pts"] += int(pts)
        k["park"] += ours.startswith("P")
        k["collide"] += coll != "-"
        k["ours"] += any(c in ours for c in "HFWC")
    for (spec, design), k in rows.items():
        n = k["runs"]
        print(f"| {spec} | {design} | {k['pts'] / n:.1f} | {k['tips3']} | {k['tips2']} | {k['tips1']} | {k['park']} | "
              f"{k['collide']} | {k['ours']} |")


def run(names, runs=60, design="rigid V, fixed turret, corner extractor"):
    """Exports these alone.VARIANTS and prints their rows against PARTNERS."""
    out = []
    for n in names:
        r = alone.alone(n, **alone.VARIANTS[n])
        r.folder = autogen.EXPERIMENTS
        r.write()
        cls = "".join(w.capitalize() for w in n.split("-")) + "Auto"
        out += autogen.study(";".join(f"{cls},{p}@50" for p in PARTNERS), designs=design, runs=runs,
                             extra_env={"BIOBUZZ_AUTO_SEED_ROWS": "1"})
    table(out)


if __name__ == "__main__":
    runs = int(sys.argv[1]) if len(sys.argv) > 1 else 60
    for r in [alone.alone(n, **alone.VARIANTS[n]) for n in ("qual-south-v-side", "qual-south-v-corner")] + [
            alone.partner_left_hold("partner-left-slow-3000-hold", 3000), alone.partner_left_hold("partner-left-silent-hold", None)]:
        r.folder = autogen.EXPERIMENTS
        r.write()
    lines = []
    print("| Our Auto | Design | Points | 3 TIPs | 2 TIPs | 1 TIP | Our PARK | Robots collide | Our other problems |")
    print("|---|---|---|---|---|---|---|---|---|")
    for auto, design in ROUTES.items():
        specs = ";".join(f"{auto},{p}@50" for p in PARTNERS)
        lines += autogen.study(specs, designs=design, runs=runs, extra_env={"BIOBUZZ_AUTO_SEED_ROWS": "1"})
    table(lines)

