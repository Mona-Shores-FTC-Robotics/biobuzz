"""A whole match in the simulator (mentor, 8 Oct 2026): our two qualifier Autos with their test partners on red, and
the same Autos on blue (turned half a turn about the field centre, as the generated class does for BLUE), so the
blue versions get run and each alliance meets the other's traffic, pieces and spills. AutoSim.alsoRunOpponent
(claude/simulator) runs the opponents; a study spec names them after a "|".

    python3 match.py [runs]     prints each alliance's AUTO points and TIPs, PARK, and the contacts
"""
import sys
from collections import defaultdict

import autogen

L = "LQualsAuto,PartnerPreloadsRightHighAuto"  # L-Quals, partner at the right start
R = "RQualsAuto,PartnerLeftVAuto"              # R-Quals, partner at the standard left start
SPECS = [L, R, f"{L}|{R}", f"{R}|{L}", f"{L}|{L}", f"{R}|{R}"]
DESIGN = "rigid V, fixed turret"
ENV = {"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40", "BIOBUZZ_AUTO_SEED_ROWS": "1"}
NAMES = {L: "L-Quals", R: "R-Quals"}


def flags(robots, k, side):
    for i, f in enumerate(robots):
        who = side(i)
        k[f"{who}_park"] += f.startswith("P")
        k[f"{who}_problem"] += any(c in f for c in "HFW")  # HIVE frame, FLOWER, wall
        k[f"{who}_g402"] += "C" in f                       # reached into the other alliance's half


def table(lines):
    rows = defaultdict(lambda: defaultdict(int))
    for l in lines:
        if not l.startswith("STUDY SEEDROW "):
            continue
        f = l[len("STUDY SEEDROW "):].split("|")
        end = next(i for i, x in enumerate(f) if "@" in x)  # the spec holds a "|" when opponents ran
        spec = "|".join(f[:end + 1]).split("@")[0]
        f = [spec] + f[end + 1:]
        k = rows[spec]
        k["runs"] += 1
        k[f"red_tips{f[3]}"] += 1  # spec, design, seed, TIPs, points, collided at, ["vs", their TIPs, points, met at], robots
        k["red_pts"] += int(f[4])
        k["collide"] += f[5] != "-"
        if len(f) > 6 and f[6] == "vs":
            k[f"blue_tips{min(int(f[7]), 3)}"] += 1
            k["blue_pts"] += int(f[8])
            k["across"] += f[9] != "-"
            flags(f[10:], k, lambda i: "red" if i < 2 else "blue")
        else:
            flags(f[6:], k, lambda i: "red")
    print("| Red | Blue | Red pts | Red 3 TIPs | Red PARK | Blue pts | Blue 3 TIPs | Blue PARK | Any robots collide "
          "| Red meets blue | Into the other half (red, blue) | Field problems (red, blue) |")
    print("|---|---|---|---|---|---|---|---|---|---|---|---|")
    for spec, k in rows.items():
        n = k["runs"]
        red, _, blue = spec.partition("|")
        blue_cols = (f"{k['blue_pts'] / n:.1f} | {k['blue_tips3']} | {k['blue_park']}/{2 * n}" if blue else "- | - | -")
        print(f"| {NAMES[red]} | {NAMES.get(blue, 'none')} | {k['red_pts'] / n:.1f} | {k['red_tips3']} | {k['red_park']}/{2 * n} "
              f"| {blue_cols} | {k['collide']} | {k['across'] if blue else '-'} | {k['red_g402']}, {k['blue_g402']} "
              f"| {k['red_problem']}, {k['blue_problem']} |")


if __name__ == "__main__":
    runs = int(sys.argv[1]) if len(sys.argv) > 1 else 60
    table(autogen.study(";".join(s + "@50" for s in SPECS), designs=DESIGN, runs=runs, extra_env=ENV))
