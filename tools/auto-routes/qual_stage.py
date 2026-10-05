"""qual-right-v3 with our preloads staged in the hook while we wait for TIP 1 (mentor idea, 5 Oct 2026).

    python3 qual_stage.py [runs]

qual-right-v3 drives to N_FIRE and waits there for the partner's TIP 1 (about 3.6 s in the simulator) before it
fires its 4 preloads at the left CELL, then fetches the far FLOWER and fires that for TIP 2. Here the wait is
used instead:

1. At N_FIRE, lower the hook (HookDown) and creep forward STAGE_FWD in.
2. Push the preloads out of the intake (Outtake) into the pocket between the chassis' face, the arm and the
   crossbeam; give them SETTLE_MS to stop rolling.
3. Lift the hook (HookUp), back off and fetch the far FLOWER (intake back on once clear of the pieces).
4. Back at N_FIRE, wait for TIP 1 and fire the FLOWER's 4 (half of TIP 2: a TIP needs about 8 POLLEN).
5. Drive PICK_FWD in forward through the staged preloads, intake first, take any it missed with the webcam
   (CollectSeen), and fire them from N_FIRE: TIP 2.
Then as qual-right-v3-large-hook: slide to the hook spot, hold TIP 2's spill, and the same tail.

The same route on the plain 18 in robot (no hook: HookDown and HookUp do nothing) shows what the hook adds.
The .pp files go in TeamCode/autos/ like qual_shapes.py's; StagedPreloadsTest runs them against plain
qual-right-v3 and qual-right-v3-large-hook.
"""
import sys
import helpers
import qual_right
import qual_shapes
from qual import *

STAGE_FWD = 2.0  # how far the robot creeps forward with the hook down before pushing the pieces out, in
SETTLE_MS = 400  # after the last piece is out, before the hook lifts: the pieces stop rolling first
PICK_FWD = 10.0  # how far forward of N_FIRE the robot drives to take them back
PICK_MS = 1500  # then the webcam pickup for what it missed, at most

# name: (robot design, chassis length, hook spot or None, staging options), the first three as qual_shapes.SHAPES.
LARGE_HOOK_SPOT = qual_shapes.SHAPES["qual-right-v3-large-hook"][2]
STAGED = {
    "qual-stage-large-hook": ("spring hood, large right hook", 14, LARGE_HOOK_SPOT, {}),
    # Quicker: the hook comes down on the drive out to N_FIRE (so it is down on arrival), no creep forward,
    # and half the settling time.
    "qual-stage-large-hook-quick": ("spring hood, large right hook", 14, LARGE_HOOK_SPOT,
                                    {"hook_early": True, "fwd": 0, "settle_ms": 200}),
    # The plain 18 in robot on the same route (HookDown and HookUp do nothing): what the hook adds.
    "qual-stage-plain": ("spring hood, full-width intake", 18, None, {}),
}


def staged(name, length, hook_at, hook_early=False, fwd=STAGE_FWD, settle_ms=SETTLE_MS, **kw):
    """qual_shapes.shaped's route for a `length` in chassis, with steps 1-5 above in place of qual-right-v3's
    wait at N_FIRE, preload volley and FLOWER trip. hook_early: HookDown before the drive to N_FIRE, not after;
    fwd: the creep forward (0: none); settle_ms: SETTLE_MS."""
    pickup = helpers.FLOWER_PICKUP_IN
    helpers.FLOWER_PICKUP_IN = length / 2 + qual_shapes.PICKUP_FROM_FRONT
    try:
        r = Route(name, N_START, speed=50)
        ends(r)
    finally:
        helpers.FLOWER_PICKUP_IN = pickup
    d = (18 - length) / 2
    for name_, dy in (("GARDEN", -d), ("PARK", d)):
        p = r.points[name_]
        r.pt(name_, p[0], round(p[1] + dy, 2), p[2])
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", *N_FIRE)
    r.pt("N_STAGE", N_FIRE[0], N_FIRE[1] - fwd, 270)
    r.pt("N_PICK", N_FIRE[0], N_FIRE[1] - PICK_FWD, 270)
    if hook_early:
        r.add(r.action("SpinUp"), r.action("HookDown"), r.go("N_FIRE", heading=270))
    else:
        r.add(r.action("SpinUp"), r.go("N_FIRE", heading=270), r.action("HookDown"))
    if fwd:
        r.add(r.go("N_STAGE", heading=270))
    r.at = "N_STAGE" if fwd else "N_FIRE"
    r.add(r.wait("Stage the preloads in the hook", when=["Empty"], ms=1500, alongside="Outtake"),
          # A time only (the intake is off and empty, so IntakeFull never comes), as qual_right's "It lands".
          r.wait("They stop rolling", when=["IntakeFull"], ms=settle_ms),
          r.action("HookUp"))
    trip = flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300)
    # The intake comes back on only once the robot has backed off the pieces it just staged.
    r.add(trip[0], r.action("IntakeOn"), *trip[1:])
    r.at = "FAR_FLOWER"
    r.add(r.go("N_FIRE", turn_after=0.3, turn_by=1.0),
          r.wait("TIP 1 (the partner)", when=["LeftCellUp"], ms=9000),
          fire(r, "Fire the far FLOWER", "Empty", ms=2500),
          # Forward through where they lie (3-10 in past the face), intake first, then the webcam for any that
          # rolled further; back to N_FIRE to fire (the left CELL scores from y 113).
          r.go("N_PICK", heading=270),
          r.wait("Pick up the staged preloads", when=["IntakeFull"], ms=PICK_MS, alongside="CollectSeen"))
    r.at = "N_PICK"
    r.add(r.go("N_FIRE", heading=270), fire(r, "Fire the staged preloads (TIP 2)", "Tip", ms=2500))
    r.at = "N_FIRE"
    if hook_at:
        r.pt("N_HOOK", *hook_at)
        r.add(r.go("N_HOOK", heading=270))
        r.at = "N_HOOK"
    r.add(*qual_right.tail(r, **kw))
    return r


def cls(name):
    return "".join(w.capitalize() for w in name.split("-")) + "Auto"


def write_all():
    for name, (design, length, hook_at, opts) in STAGED.items():
        staged(name, length, hook_at, **opts, **qual_shapes.options(hook_at)).write()


if __name__ == "__main__":
    write_all()
    if len(sys.argv) > 1:
        runs = int(sys.argv[1])
        for f in ("1", "3"):
            for name, (design, _, _, _) in STAGED.items():
                study(f"{cls(name)},PartnerPreloadsRightAuto@50", runs=runs, designs=design,
                      extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                                 "BIOBUZZ_AUTO_FRICTION": f})
