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

    python3 g409.py [runs] [auto ...] [--extra 0,300,500] [--back 0,4] [--north 4] [--tip 700]
                    [--designs plain,walls,early]

--back moves both catch spots back (or only S_CATCH with --north); --tip waits before a path a TIP
sets off. The winners (README, "G409-safe versions"):

    python3 g409.py 20 v2 --extra 500
    python3 g409.py 20 shoots-left stages --extra 300 --back 8 --north 4 --tip 700 --designs plain,early

exports each Auto and extra into auto-builder/experiments as <auto>-g409-<ms> and simulates it with its
partner on normal and slow tiles. Autos: v2 (qual-right-v2), shoots-left, stages, shoots-right (v1).
"""
import sys
import autogen
import qual
import qual_right
from qual import QUALS, D

WALLS = D + ", side walls"
DESIGNS = {"plain": D, "walls": WALLS, "early": WALLS + " out at the TIP"}

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


def after_tip(r, ms):
    """Where a TIP is the cue to drive straight off (TIP 3? Yes: PARK), first wait ms for its spill to
    reach the tiles: the PARK path from S_FIRE runs under where TIP 3's spill lands."""
    def walk(cards):
        for c in cards:
            if c.get("kind") != "firstOf":
                continue
            for row in c["rows"]:
                if "Tip" in row.get("when", []) and row["cards"] and row["cards"][0].get("kind") == "path":
                    row["cards"].insert(0, r.wait("Its spill lands", when=["IntakeFull"], ms=ms))
                walk(row["cards"])
    if ms:
        walk(r.cards)
    return r


def back_off(r, south, north=None):
    """Wait for a TIP further from its landing: the catch spots S_CATCH and N_LOW move `south` and
    `north` in toward their own walls, with every path that ends on them. A robot waiting there is otherwise
    occasionally hit by a bouncing piece before it reaches the tiles. N_LOW stays inside the left
    CELL's shot map (y 113-129)."""
    north = south if north is None else north
    moves = {"S_CATCH": -south, "N_LOW": north}
    for p, dy in moves.items():
        if not dy:
            continue
        x, y, h = r.points[p]
        for line in r.lines:
            e = line["endPoint"]
            if abs(e["x"] - x) < 1e-6 and abs(e["y"] - y) < 1e-6:
                e["y"] = y + dy
        for q, (qx, qy, qh) in list(r.points.items()):  # N_FIRE is N_LOW under another name
            if abs(qx - x) < 1e-6 and abs(qy - y) < 1e-6:
                r.points[q] = [qx, y + dy, qh]
    return r


def name(auto, extra, back=0, north=None, tip_ms=0):
    out = f"{BASE[auto]}-g409-{extra}"
    if back or north:
        out += f"-back{back}" if north is None or north == back else f"-s{back}n{north}"
    return out + (f"-t{tip_ms}" if tip_ms else "")


def build(auto, extra, back=0, north=None, tip_ms=0):
    make, _ = AUTOS[auto]
    r = after_tip(back_off(retime(make(name(auto, extra, back, north, tip_ms)), extra), back, north), tip_ms)
    r.folder = autogen.EXPERIMENTS
    r.write()
    return r


if __name__ == "__main__":
    args = sys.argv[1:]
    opts = {"--extra": "0,300,500", "--back": "0", "--north": "", "--tip": "0", "--designs": "plain,walls"}
    for k in list(opts):
        if k in args:
            i = args.index(k)
            opts[k] = args[i + 1]
            del args[i:i + 2]
    extras = [int(x) for x in opts["--extra"].split(",")]
    backs = [int(x) for x in opts["--back"].split(",")]
    north = int(opts["--north"]) if opts["--north"] else None
    tip_ms = int(opts["--tip"])
    designs = opts["--designs"].split(",")
    runs = int(args[0]) if args else 20
    autos = args[1:] or ["v2", "shoots-left", "stages"]
    specs = []
    for a in autos:
        for e in extras:
            for b in backs:
                build(a, e, b, north, tip_ms)
                specs.append(f"{qual_right.cls(name(a, e, b, north, tip_ms))},{AUTOS[a][1]}@50")
    for f in autogen.FRICTIONS:
        print(f"--- tiles friction x{f}")
        autogen.study(";".join(specs), runs=runs, designs="|".join(DESIGNS[d] for d in designs),
                      extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                                 "BIOBUZZ_AUTO_FRICTION": f})
