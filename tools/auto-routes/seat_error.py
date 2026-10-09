"""How much a FLOWER seat-position error costs each extractor (body-designs chat, 8 Oct 2026; mentor: "having to line
up as close as we do is a real problem"). The centre block (today's: the FLOWER within 1.5 in of the centre line),
the corner extractor (at -7.3 in, the right front corner) and a wide bar (a T-bar out past the V's tips: the FLOWER
anywhere in -7.3..7.3 in, plus 1.5 in at each end; down, a solid strip across the seat line). Each FLOWER seat is
off by a lateral error drawn evenly in +-N in (RobotDesign.seatErrorIn), the pieces see the FLOWER that far aside.

    python3 seat_error.py [runs]
"""
import sys
from collections import defaultdict

import autogen

L = "LQualsAuto,PartnerPreloadsRightHighAuto"
R = "RQualsAuto,PartnerLeftVAuto"
C = "QualSouthVCornerAuto,PartnerLeftVAuto"
CI = "QualSouthVCornerInAuto,PartnerLeftVAuto"  # the corner route aimed 1.5 in inside the bar's end
BASE = "rigid V, fixed turret"
DESIGNS = {L: [BASE, BASE + ", CAD bar", BASE + ", wide bar"], R: [BASE, BASE + ", CAD bar", BASE + ", wide bar"],
           C: [BASE + ", corner extractor", BASE + ", wide bar"], CI: [BASE + ", wide bar"]}
ERRORS = (0, 1, 2, 4)
ENV = {"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40", "BIOBUZZ_AUTO_SEED_ROWS": "1"}
NAMES = {L: "L-Quals", R: "R-Quals", C: "qual-south-v-corner", CI: "qual-south-v-corner-in"}
EXTRACTOR = {BASE: "centre block", BASE + ", CAD bar": "CAD bar (+-3.0)", BASE + ", corner extractor": "corner", BASE + ", wide bar": "wide bar"}


def design(base, e):
    return base if e == 0 else f"{base}, seat error {e} in"


def run(runs):
    import alone
    for n in ("qual-south-v-corner", "qual-south-v-corner-in"):
        r = alone.alone(n, **alone.VARIANTS[n])
        r.folder = autogen.EXPERIMENTS
        r.write()
    rows = defaultdict(lambda: defaultdict(int))
    g409 = {}
    for spec, bases in DESIGNS.items():
        designs = "|".join(design(b, e) for b in bases for e in ERRORS)
        for l in autogen.study(spec + "@50", designs=designs, runs=runs, extra_env=ENV):
            if l.startswith("STUDY SEEDROW "):
                f = l[len("STUDY SEEDROW "):].split("|")
                k = rows[(spec, f[1])]
                k["runs"] += 1
                k[f"tips{f[3]}"] += 1
                k["pts"] += int(f[4])
                k["park"] += f[6].startswith("P")
                k["collide"] += f[5] != "-"
                k["problems"] += any(c in f[6] for c in "HFW")
            elif l.startswith("STUDY ") and "G409" in l and "@" in l:
                parts = l.split()
                g409[(spec, l[l.index(parts[2]):].split("  ")[0].strip())] = l[l.index("G409"):].split(",")[0]
    print("| Auto | Extractor | Seat error (in) | Points | 3 TIPs | 2 TIPs | Our PARK | Robots collide | Our problems |")
    print("|---|---|---|---|---|---|---|---|---|")
    for spec, bases in DESIGNS.items():
        for b in bases:
            for e in ERRORS:
                k = rows[(spec, design(b, e))]
                n = k["runs"] or 1
                print(f"| {NAMES[spec]} | {EXTRACTOR[b]} | ±{e} | {k['pts'] / n:.1f} | {k['tips3']} | {k['tips2']} | "
                      f"{k['park']} | {k['collide']} | {k['problems']} |")
    return g409


if __name__ == "__main__":
    run(int(sys.argv[1]) if len(sys.argv) > 1 else 60)
