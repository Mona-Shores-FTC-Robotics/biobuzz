"""qual-right-v3 for the spill shapes (sim-review/body-shapes-shortlist.png, mentor 5 Oct 2026): the same Auto
for a robot with a rigid V, or a large or small right hook, simulated against the plain baseline.

    python3 qual_shapes.py [runs]

Each shape keeps qual-right-v3's route, with two changes:
- the spots where the robot's front must reach something follow the chassis, which is 2 or 1 in shorter at each
  end than the 18 in robot (14 or 16 in long): the far FLOWER's pickup and the GARDEN stop that much nearer, and
  PARK that much further into the LOADING ZONE, so the front corner is still in it;
- a hook, once TIP 2 has started (its last shot from N_FIRE), slides to the hook spot before the spill lands:
  its chassis face halfway between the spill's 100% and 90% lines, its arm on the side toward the centre line
  (the simulator lowers the arm 0.4 s after the TIP starts, once the robot has stopped, and lifts it as soon as
  the robot drives), and holds it there 1 s after TIP 2 settles, not 0.5, so the spill lands inside it.
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


def options(hook_at):
    """qual-right-v3's tail; a hook holds TIP 2's spill 0.5 s longer before driving into it (1 s after TIP 2 settles,
    not 0.5): it lifts as soon as the robot drives, and held only 0.5 s it kept 18% of the spill off the blue half
    against 8% held 1 s (5 Oct 2026, normal tiles), at the price of PARK in more runs."""
    base = dict(qual_right.VARIANTS[qual_right.WINNER])
    if hook_at:
        base["extra"] = 1000
    return base


def cls(name):
    return "".join(w.capitalize() for w in name.split("-")) + "Auto"


if __name__ == "__main__":
    runs = int(sys.argv[1]) if len(sys.argv) > 1 else 20
    for name, (design, length, hook_at) in SHAPES.items():
        shaped(name, length, hook_at, **options(hook_at)).write()
    for f in ("1", "3"):
        study(f"{cls(qual_right.WINNER)},PartnerPreloadsRightAuto@50", runs=runs, designs="spring hood, full-width intake",
              extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                         "BIOBUZZ_AUTO_FRICTION": f})
        for name, (design, _, _) in SHAPES.items():
            study(f"{cls(name)},PartnerPreloadsRightAuto@50", runs=runs, designs=design,
                  extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                             "BIOBUZZ_AUTO_FRICTION": f})
