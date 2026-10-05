"""The qualifier Autos retimed for G409 (5 Oct 2026): wait for a TIP's spill to reach the tiles before
driving into it, with and without side walls.

G409: "A ROBOT may not catch or deflect a SCORING ELEMENT released by a TIPPED HIVE unless and until
that SCORING ELEMENT contacts anything else besides that ROBOT." The routes in qual.py and
qual_right.py leave for the spill when "TIP n settles" (the CELL is up), about 0.6 s after the TIP
starts, and reach its landing 0.1-0.2 s before the last pieces do: 5-11 spilled pieces a run touch our
robot first (the simulator's `sim: G409` events). Here each "TIP n settles" wait is followed by an
extra wait, "It lands", before the tunnel path.

The routes themselves come from qual.py and qual_right.py unchanged (another session works on qual.py):
this builds them with those functions and inserts the wait into the built card list.

    python3 g409.py [runs] [auto ...] [--extra 0,300,500] [--designs plain,walls]

exports each Auto and extra into auto-builder/experiments as <auto>-g409-<ms> and simulates it with its
partner on normal and slow tiles. Autos: v2 (qual-right-v2), shoots-left, stages, shoots-right (v1).
"""
import sys
import autogen
import qual
import qual_right
from qual import QUALS, D

WALLS = D + ", side walls"
DESIGNS = {"plain": D, "walls": WALLS}

# auto: (how to build it, its partner's class)
AUTOS = {
    "v2": (lambda name: qual_right.right(name, **qual_right.V2), "PartnerPreloadsRightAuto"),
    "shoots-left": (lambda name: qual.shoots_left(name), QUALS["shoots-left"][1]),
    "stages": (lambda name: qual.stages(name), QUALS["stages"][1]),
    "shoots-right": (lambda name: qual.shoots_right(name), QUALS["shoots-right"][1]),
}
BASE = {"v2": "qual-right-v2", "shoots-left": "qual-partner-shoots-left", "stages": "qual-partner-stages",
        "shoots-right": "qual-partner-shoots-right"}


def retime(r, extra_ms):
    """After every "TIP n settles" wait in r's cards (nested ones included), wait extra_ms more, or
    until the intake is full: the spill is on the tiles before we drive into it."""
    def walk(cards):
        out = []
        for c in cards:
            if c.get("kind") == "firstOf":
                for row in c["rows"]:
                    row["cards"] = walk(row["cards"])
            out.append(c)
            label = c.get("label", "")
            if extra_ms and c.get("kind") == "firstOf" and label.startswith("TIP") and "settles" in label:
                out.append(r.wait("It lands" + label[label.index("settles") + len("settles"):],
                                  when=["IntakeFull"], ms=extra_ms))
        return out
    r.cards = walk(r.cards)
    return r


def name(auto, extra):
    return f"{BASE[auto]}-g409-{extra}"


def build(auto, extra):
    make, _ = AUTOS[auto]
    r = retime(make(name(auto, extra)), extra)
    r.folder = autogen.EXPERIMENTS
    r.write()
    return r


if __name__ == "__main__":
    args = sys.argv[1:]
    extras, designs = [0, 300, 500], ["plain", "walls"]
    if "--extra" in args:
        i = args.index("--extra")
        extras = [int(x) for x in args[i + 1].split(",")]
        del args[i:i + 2]
    if "--designs" in args:
        i = args.index("--designs")
        designs = args[i + 1].split(",")
        del args[i:i + 2]
    runs = int(args[0]) if args else 20
    autos = args[1:] or ["v2", "shoots-left", "stages"]
    specs = []
    for a in autos:
        for e in extras:
            build(a, e)
            specs.append(f"{qual_right.cls(name(a, e))},{AUTOS[a][1]}@50")
    for f in ("1", "3"):
        print(f"--- tiles friction x{f}")
        autogen.study(";".join(specs), runs=runs, designs="|".join(DESIGNS[d] for d in designs),
                      extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                                 "BIOBUZZ_AUTO_FRICTION": f})
