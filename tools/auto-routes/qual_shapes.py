"""qual-right-v3 for the spill shapes (sim-review/body-shapes-shortlist.png, mentor 5 Oct 2026): the same Auto
for a robot with a rigid V, or a large or small right hook, simulated against the plain baseline.

    python3 qual_shapes.py [runs]     (0: write the routes only; ShapeMatchTest simulates them)

Each shape keeps qual-right-v3's route, with two changes:
- the spots where the robot's front must reach something follow the chassis, which is 2 or 1 in shorter at each
  end than the 18 in robot (14 or 16 in long): the far FLOWER's pickup and the GARDEN stop that much nearer, and
  PARK that much further into the LOADING ZONE, so the front corner is still in it;
- a hook, once TIP 2 has started (its last shot from N_FIRE), slides to the hook spot before the spill lands:
  its chassis face halfway between the spill's 100% and 90% lines, its arm on the side toward the centre line
  (the simulator swings the hook down as the TIP starts, while the robot slides there, and back up as soon as the
  robot drives off), and holds it there 1 s after TIP 2 settles, not 0.5, so the spill lands inside it.
The start spot is qual-right-v3's; the simulator backs a shorter robot against the wall there.
"""
import sys
import autogen
import helpers
import qual_right
from qual import *

FIELD_IN = 141.5  # wall face to wall face (FieldFrame.FIELD_SIZE_INCHES)
PICKUP_FROM_FRONT = helpers.FLOWER_PICKUP_IN - 9  # 2.2 in from the front of the intake to the FLOWER

# name: (robot design, chassis length, hook spot or None). Hook spots are for the north spill (TIP 2's), facing
# south: centre 29.6 / 29.5 in from the north wall, x toward the centre line (BodyShapeSpillTest.rightHookAtTheSpill).
SHAPES = {
    "qual-right-v3-rigid-v": ("spring hood, rigid V", 16, None),
    "qual-right-v3-large-hook": ("spring hood, large right hook", 14, (61.6, FIELD_IN - 29.6, 270)),
    "qual-right-v3-small-hook": ("spring hood, small right hook", 16, (60.2, FIELD_IN - 29.5, 270)),
}


def shaped(name, length, hook_at, **kw):
    """qual_right.right(name, **kw) for a chassis `length` long, sliding to `hook_at` once TIP 2 starts."""
    pickup = helpers.FLOWER_PICKUP_IN
    helpers.FLOWER_PICKUP_IN = length / 2 + PICKUP_FROM_FRONT
    try:
        r = Route(name, N_START, speed=50)
        ends(r)
    finally:
        helpers.FLOWER_PICKUP_IN = pickup
    d = (18 - length) / 2
    for name_, dy in (("GARDEN", -d), ("PARK", d)):  # the GARDEN against the south wall (heading 270); PARK facing north
        p = r.points[name_]
        r.pt(name_, p[0], round(p[1] + dy, 2), p[2])
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", *N_FIRE)
    r.add(r.action("SpinUp"), r.go("N_FIRE", heading=270),
          r.wait("TIP 1 (the partner)", when=["LeftCellUp"], ms=9000),
          fire(r, "Fire the preloads at the left CELL", "Empty", ms=2500))
    r.at = "N_FIRE"
    r.add(*flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300))
    r.at = "FAR_FLOWER"
    r.add(r.go("N_FIRE", turn_after=0.3, turn_by=1.0), fire(r, "Fire the far FLOWER (TIP 2)", "Tip", ms=2500))
    r.at = "N_FIRE"
    if hook_at:
        r.pt("N_HOOK", *hook_at)
        r.add(r.go("N_HOOK", heading=270))
        r.at = "N_HOOK"
    r.add(*qual_right.tail(r, **kw))
    return r


# On option 3, the baseline robot (5 Oct 2026): qual-right-o3 as qual_right.py draws it for that robot, and a hook
# slide placed as above: the chassis face (14.5 in long) and the arm's side (14.5 in wide) on the same 95% lines.
# The large hook sits 1 in further from the wall (6 Oct 2026): a piece is 2.8 in across and comes down at a slant, so
# pieces landing just inside the 9.5 in arm's crossbeam clipped its top. Its face 37.6 in from the wall and the
# crossbeam at 47.1 touched falling pieces in 2 of 20 runs (normal tiles), against 7 on the 95% lines; 0.5 in from
# them 3, 1.5 in 6, toward the wall 10-17. The rest is the spill's near tail landing on the face: 9.5 in (R105's
# longest on option 3) can't clear both ends.
O3_SHAPES = {
    "qual-right-o3-rigid-v": ("option 3, rigid V", None),
    "qual-right-o3-large-hook": ("option 3, large right hook", (70.6 - 7.25, round(FIELD_IN - (37.6 - 7.25), 2), 270)),
    "qual-right-o3-small-hook": ("option 3, small right hook", (70.6 - 7.25, round(FIELD_IN - (37.5 - 7.25), 2), 270)),
}


# Option 3 shortened to 12.5 in (mentor, 6 Oct 2026), with the rigid V, alone and with an 11.5 in hook: the robot's face
# 36.1 in from the wall, 1.5 in further than the 14.5 in hook's so the V's tips (1.75 in ahead) clear the spill, and the
# crossbeam at 47.6 in. 20 runs, normal / slow tiles: the V alone 69.5 / 72.0 points, 28% / 22% of TIP 2's spill on the
# blue half; with the hook 73.0 / 72.8, 9% / 8%, touched in 2 / 2 runs (face 35.6: 3 / 3).
qual_right.FRONT_IN["option3-short"] = 12.5 / 2
SHORT_SHAPES = {
    "qual-right-o3-short-v": None,
    "qual-right-o3-short-v-hook": (70.6 - 7.25, round(FIELD_IN - (36.1 - 12.5 / 2), 2), 270),
}


def o3_shaped(name, hook_at, extra=None, robot=None):
    """qual-right-o3 (qual_right.right with its O3 options), sliding to `hook_at` as TIP 2 starts, before the tail;
    `extra`: ms to wait for TIP 2's spill to land before driving into it, if not qual-right-o3's."""
    kw = dict(qual_right.O3["qual-right-o3"])
    if robot:
        kw["robot"] = robot
    if extra is not None:
        kw["extra"] = extra
    tail = qual_right.tail

    def hooked(r, **t):
        r.pt("N_HOOK", *hook_at)
        go = r.go("N_HOOK", heading=270)
        r.at = "N_HOOK"
        return [go, *tail(r, **t)]
    qual_right.tail = hooked if hook_at else tail
    try:
        return qual_right.right(name, **kw)
    finally:
        qual_right.tail = tail


def options(hook_at):
    """qual-right-v3's tail; a hook holds TIP 2's spill 0.5 s longer before driving into it (1 s after TIP 2 settles,
    not 0.5): it lifts as soon as the robot drives, and held only 0.5 s it kept 18% of the spill off the blue half
    against 8% held 1 s (5 Oct 2026, normal tiles), at the price of PARK in more runs."""
    base = dict(qual_right.VARIANTS["qual-right-v3"])
    if hook_at:
        base["extra"] = 1000
    return base


def cls(name):
    return "".join(w.capitalize() for w in name.split("-")) + "Auto"


if __name__ == "__main__":
    runs = int(sys.argv[1]) if len(sys.argv) > 1 else 20
    for name, (design, length, hook_at) in SHAPES.items():
        shaped(name, length, hook_at, **options(hook_at)).write()
    for name, (design, hook_at) in O3_SHAPES.items():
        # A hook waits 1 s for TIP 2's spill, not qual-right-o3's 0.5 s (6 Oct 2026, before the slow-tile fix; 20 runs normal / slow tiles,
        # large hook: 0 s 65.8 / 63.5 points but touched in 20 / 18 runs; 0.5 s 61.0 / 63.5, 21% / 20% of the spill
        # on the blue half; 1 s 64.3 / 64.0, 10% / 14%. Plain qual-right-o3: 64.8 / 61.3, 31% / 30%).
        o3_shaped(name, hook_at, extra=1000 if hook_at else None).write()
    for name, hook_at in SHORT_SHAPES.items():
        o3_shaped(name, hook_at, extra=1000 if hook_at else None, robot="option3-short").write()
    if runs == 0:
        sys.exit()
    for f in ("1", "3"):
        study(f"{cls('qual-right-v3')},PartnerPreloadsRightAuto@50", runs=runs, designs="spring hood, full-width intake",
              extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                         "BIOBUZZ_AUTO_FRICTION": f})
        for name, (design, _, _) in SHAPES.items():
            study(f"{cls(name)},PartnerPreloadsRightAuto@50", runs=runs, designs=design,
                  extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                             "BIOBUZZ_AUTO_FRICTION": f})
