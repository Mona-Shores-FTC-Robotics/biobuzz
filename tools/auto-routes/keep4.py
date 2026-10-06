"""The three qualifier Autos, with and without a hook, for the deep dive (mentor, 6 Oct 2026).

    python3 keep4.py        writes them (TeamCode/autos and generated/); DeepDive simulates them

The goal: 3 TIPs, both robots LEAVE and PARK, every qualifier. The plan the routes already follow: after each
TIP, wait facing the spill, drive through it picking up 4, fire them at the other CELL. The three partners:

  right   partner at the right start fires its 4 at once (TIP 1); we take TIP 2 and 3: qual-right-o3
  left    partner at the left start fires its 4 once we've tipped (TIP 2 with our catch): qual-left-o3, new here,
          qual.shoots_left's plan on qual_right's tail
  nothing partner at the left start only drives and parks: the left routes (as qual-partner-parks-left)
  stages  partner can't shoot, its 4 POLLEN start on the tiles beside it, it drives to PARK: qual-stages-angled
          (partner B) and qual-stages-wall (partner A)

Each comes for option 3 (14.5 in) and option 3 shortened to 12.5 in, plain and with a hook. A hook route waits at
the hook spot instead of the catch spot after each of our TIPs it can reach, 1 s after the TIP settles, not 0.5:
TIP 2 at the north end (as qual_shapes), and in left and stages TIP 1 at the south end too, for which it fires its
preloads from S_FIRE (in reach of the HIVE: the hook comes down as the TIP starts only near it) rather than the
start. The hook spot: the robot's face 37.6 in from the wall (36.1 on 12.5 in), its centre-line side 7.25 in from
the centre line's x 70.6 (BodyShapeSpillTest; qual_shapes). At the south end the arm toward the centre line is the
robot's right, at the north its left: a robot with a hook at both ends needs a dual hook (RobotDesign.flapEitherSide).
"""
import math
import autogen
import qual_right
from qual_right import fit, tail, O3, VARIANTS, stages_staged
from qual import *

FIELD_IN = 141.5
qual_right.FRONT_IN.setdefault("option3-short", 12.5 / 2)
LENGTH = {"option3": 14.5, "option3-short": 12.5, "baseline": 18}
FACE = {"option3": 37.6, "option3-short": 36.1}  # the hook spot: the face this far from the wall
HOOK_X = 70.6 - 7.25


def hook_spots(robot, south=None, north=None):
    """The hook spots at each end: the face `south` / `north` in from that end's wall (FACE unless given)."""
    half = LENGTH[robot] / 2
    n, s = north or FACE[robot], south or FACE[robot]
    return {"N": (HOOK_X, round(FIELD_IN - (n - half), 2), 270), "S": (HOOK_X, round(s - half, 2), 90)}


class hooked_tail:
    """Within it, qual_right.tail starts by sliding to N_HOOK (TIP 2's spill at the north end)."""

    def __init__(self, spot):
        self.spot = spot

    def __enter__(self):
        self.tail = qual_right.tail
        spot, tail_ = self.spot, self.tail

        def t(r, **kw):
            r.pt("N_HOOK", *spot)
            go = r.go("N_HOOK", heading=270)
            r.at = "N_HOOK"
            return [go, *tail_(r, **kw)]
        qual_right.tail = t
        return self

    def __exit__(self, *a):
        qual_right.tail = self.tail


def right(name, robot, hook, north=None, extra=1000):
    kw = {**O3["qual-right-o3"], "robot": robot}
    if not hook:
        return qual_right.right(name, **kw)
    kw["extra"] = extra
    with hooked_tail(hook_spots(robot, north=north)["N"]):
        return qual_right.right(name, **kw)


def left(name, robot, hook, land=500, south=None, north=None, extra=1000):
    """Partner at the left start fires its 4 when the left CELL rises (partners.preloads_left). TIP 1: our preloads;
    its spill caught driving north through it and the tunnel, fired at the left CELL with the partner's 4 (TIP 2);
    if TIP 2 hasn't come, the far FLOWER. Then qual_right's tail: TIP 2's spill caught going south, the sweep, the
    GARDEN, PARK."""
    kw = {**O3["qual-right-o3"]}
    kw.pop("robot", None)
    r = fit(robot)(name, S_START, speed=50)
    ends(r)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", *N_FIRE)
    if hook:
        kw["extra"] = extra
        r.pt("S_HOOK", *hook_spots(robot, south, north)["S"])
        r.add(r.action("SpinUp"), r.go("S_FIRE", heading=90), fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000),
              r.go("S_HOOK", heading=90), r.wait("TIP 1 settles", when=["LeftCellUp"], ms=3500),
              r.wait("It lands", when=["IntakeFull"], ms=extra))
    else:
        r.add(fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000), r.go("S_CATCH"),
              r.wait("TIP 1 settles", when=["LeftCellUp"], ms=3500), r.wait("It lands", when=["IntakeFull"], ms=land))
    r.add(tunnel(r, "N_TURN"), r.go("N_LOW", heading=270), fire(r, "Fire the catch at the left CELL", "Empty", ms=2200))
    with hooked_tail(hook_spots(robot, south, north)["N"]) if hook else _nothing():
        r.at = "N_LOW"
        tipped = qual_right.tail(r, **kw)
        r.at = "N_LOW"
        more = [*flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300)]
        r.at = "FAR_FLOWER"
        more += [r.go("N_FIRE", turn_after=0.3, turn_by=1.0), fire(r, "Fire the far FLOWER (TIP 2)", "Tip", ms=2500)]
        r.at = "N_FIRE"
        more += qual_right.tail(r, tag=" (B)", **kw)
    r.at = "N_LOW"
    r.add(r.wait("TIP 2?", when=["Tip"], ms=1000, yes=tipped, no=more, yes_label="Yes", no_label="No: the far FLOWER"))
    return r


class _nothing:
    def __enter__(self):
        return self

    def __exit__(self, *a):
        pass


def stages(name, robot, hook, partner, south=None, north=None, extra=1000):
    """qual-stages-angled (partner B, plan "chase") or qual-stages-wall (A, "west", no PARK), for `robot`; a hook
    route waits at the hook spots instead (TIP 1's only in "chase": "west" leaves TIP 1's spill)."""
    v3 = VARIANTS["qual-right-v3"]
    kw = {**v3, "third": False, "garden": "two"} if partner == "B" else {**v3, "third": False, "garden": "two", "park": False}
    plan = "chase" if partner == "B" else "west"
    if not hook:
        return stages_staged(name, partner=partner, plan=plan, robot=robot, **kw)
    kw["extra"] = extra
    spots = hook_spots(robot, south, north)
    with hooked_tail(spots["N"]):
        if plan == "west":
            return stages_staged(name, partner=partner, plan=plan, robot=robot, **kw)
        # "chase" with TIP 1 fired from S_FIRE and caught at S_HOOK: stages_staged's opening, rewritten here.
        r = fit(robot)(name, S_START, speed=50)
        ends(r)
        flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
        r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", *N_FIRE).pt("S_HOOK", *spots["S"])
        r.add(r.action("SpinUp"), r.go("S_FIRE", heading=90), fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000),
              r.go("S_HOOK", heading=90), r.wait("TIP 1 settles", when=["LeftCellUp"], ms=3500),
              r.wait("It lands", when=["IntakeFull"], ms=extra),
              tunnel(r, "N_TURN"), r.go("N_LOW", heading=270), fire(r, "Fire the catch", "Empty", ms=2200))
        r.at = "N_LOW"
        r.add(*qual_right.staged_row(r, partner, 1600, west=False))
        r.add(r.go("N_LOW", turn_after=0.2, turn_by=0.9), fire(r, "Fire the row", "Tip", ms=2500))
        r.at = "N_LOW"
        more = [*flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300)]
        r.at = "FAR_FLOWER"
        more += [r.go("N_FIRE", turn_after=0.3, turn_by=1.0), fire(r, "Fire the far FLOWER (TIP 2)", "Tip", ms=2500)]
        r.at = "N_FIRE"
        more += qual_right.tail(r, tag=" (B)", **kw)
        r.at = "N_LOW"
        tipped = qual_right.tail(r, **kw)
        r.add(r.wait("TIP 2?", when=["Tip"], ms=600, yes=tipped, no=more, yes_label="Yes", no_label="No: the far FLOWER"))
        return r


# name: builder. "-short": option 3 at 12.5 in; "-hook": the hook route.
ROUTES = {}
for robot, suffix in (("option3", ""), ("option3-short", "-short")):
    for hook in (False, True):
        h = "-hook" if hook else ""
        if robot == "option3-short" or hook:  # qual-right-o3 itself is claude/simulator's, already written
            ROUTES[f"qual-right-o3{suffix}{h}"] = lambda n, robot=robot, hook=hook: right(n, robot, hook)
        ROUTES[f"qual-left-o3{suffix}{h}"] = lambda n, robot=robot, hook=hook: left(n, robot, hook)
        if robot == "option3-short" or hook:  # qual-stages-angled / -wall themselves are already written
            ROUTES[f"qual-stages-angled{suffix}{h}"] = lambda n, robot=robot, hook=hook: stages(n, robot, hook, "B")
            ROUTES[f"qual-stages-wall{suffix}{h}"] = lambda n, robot=robot, hook=hook: stages(n, robot, hook, "A")

# The 18 in robot with the 16.2 in intake (the wide-intake what-if), plain only.
ROUTES["qual-right-wide"] = lambda n: right(n, "baseline", False)
ROUTES["qual-left-wide"] = lambda n: left(n, "baseline", False)
ROUTES["qual-stages-angled-wide"] = lambda n: stages(n, "baseline", False, "B")
ROUTES["qual-stages-wall-wide"] = lambda n: stages(n, "baseline", False, "A")

if __name__ == "__main__":
    import sys
    only = sys.argv[1:]
    for name, build in ROUTES.items():
        if only and name not in only:
            continue
        r = build(name)
        r.write()
        print(name)
