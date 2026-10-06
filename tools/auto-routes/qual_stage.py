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

What the simulator found (5 Oct 2026, StagedPreloadsTest): staged from N_FIRE as drawn above, the pieces stop at
y 96-101, where the left CELL swings up at TIP 1, and it throws most of them. The "back" routes stage from y 120
instead (pieces at y 104-110), fire from y 119, turn round only at y 127 so the corners never sweep the pieces, and
take them back with the webcam one at a time. The plain 18 in robot runs the same route (HookDown and HookUp do
nothing) to show what the hook adds. The .pp files go in TeamCode/autos/ like qual_shapes.py's; StagedPreloadsTest
runs them against plain qual-right-v3 and qual-right-v3-large-hook.
"""
import autogen
import sys
import helpers
import qual_right
import qual_shapes
from qual import *

STAGE_FWD = 2.0  # how far the robot creeps forward with the hook down before pushing the pieces out, in
SETTLE_MS = 400  # after the last piece is out, before the hook lifts: the pieces stop rolling first
PICK_FWD = 10.0  # the pickup drives to this far forward of where the robot staged from
PICK_MS = 1500  # then the webcam pickup for what it missed, at most

# name: (robot design, chassis length, hook spot or None, staging options), the first three as qual_shapes.SHAPES.
# The first is the idea as first drawn, kept for the README's table; the "back" ones are what came of it.
LARGE_HOOK_SPOT = qual_shapes.SHAPES["qual-right-v3-large-hook"][2]
STAGED = {
    "qual-stage-large-hook": ("spring hood, large right hook", 14, LARGE_HOOK_SPOT, {}),
    # Staged from N_FIRE the pieces stop at y 96-101, where the left CELL swings up at TIP 1 and throws them
    # (simulated 5 Oct 2026: 3 of 4 thrown in most runs on normal tiles). Back: staged from y 120, so they stop at
    # about y 104-106, clear of it; both volleys fired from y 119 (the left CELL scores from 113-129, ShotMapTest),
    # the robot's face 2 in or more short of them; it turns only at y 127, clear of them, and the webcam takes them
    # back one at a time.
    "qual-stage-back-large-hook": ("spring hood, large right hook", 14, LARGE_HOOK_SPOT,
                                   BACK := {"stage_y": 120, "fire_y": 119, "fwd": 0, "clear_y": 127, "pick_fwd": 1}),
    # Quicker: the hook comes down on the drive out (so it is down on arrival), half the settling time.
    "qual-stage-back-large-hook-quick": ("spring hood, large right hook", 14, LARGE_HOOK_SPOT,
                                         {**BACK, "hook_early": True, "settle_ms": 200}),
    # The plain 18 in robot on the same route (HookDown and HookUp do nothing): what the hook adds. It fires from
    # 2 in further back, its face being 2 in further forward.
    "qual-stage-back-plain": ("spring hood, full-width intake", 18, None, {**BACK, "fire_y": 121}),
}


def staged(name, length, hook_at, hook_early=False, fwd=STAGE_FWD, settle_ms=SETTLE_MS, stage_y=None, fire_y=None,
           clear_y=None, pick_fwd=None, **kw):
    """qual_shapes.shaped's route for a `length` in chassis, with steps 1-5 above in place of qual-right-v3's
    wait at N_FIRE, preload volley and FLOWER trip. hook_early: HookDown before the drive to N_FIRE, not after;
    fwd: the creep forward (0: none); settle_ms: SETTLE_MS; stage_y: stage from (57.5, stage_y) instead of N_FIRE;
    fire_y: fire both volleys from (57.5, fire_y) instead of N_FIRE. The pickup drives to PICK_FWD in short of
    where it staged from. clear_y: before the FLOWER, back straight off to y clear_y, turn there and drive to the
    FLOWER already facing it (a robot turning near the pieces sweeps them with its corners: 12.7 in out)."""
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
    fire_at = (N_FIRE[0], N_FIRE[1] if fire_y is None else fire_y, 270)
    stage = N_FIRE[1] if stage_y is None else stage_y
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", *fire_at)
    r.pt("N_STAGE", N_FIRE[0], stage - fwd, 270)
    # The pickup ends as far past where they were staged from as the plain route's does (PICK_FWD - fwd).
    r.pt("N_PICK", N_FIRE[0], stage - PICK_FWD if pick_fwd is None else fire_at[1] - pick_fwd, 270)
    first = "N_STAGE" if stage_y is not None else "N_FIRE"
    if hook_early:
        r.add(r.action("SpinUp"), r.action("HookDown"), r.go(first, heading=270))
    else:
        r.add(r.action("SpinUp"), r.go(first, heading=270), r.action("HookDown"))
    if fwd:
        r.add(r.go("N_STAGE", heading=270))
    r.at = "N_STAGE" if fwd or stage_y is not None else "N_FIRE"
    r.add(r.wait("Stage the preloads in the hook", when=["Empty"], ms=1500, alongside="Outtake"),
          # A time only (the intake is off and empty, so IntakeFull never comes), as qual_right's "It lands".
          r.wait("They stop rolling", when=["IntakeFull"], ms=settle_ms),
          r.action("HookUp"))
    if clear_y is None:
        trip = flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300)
        # The intake comes back on only once the robot has backed off the pieces it just staged.
        r.add(trip[0], r.action("IntakeOn"), *trip[1:])
    else:
        r.pt("N_CLEAR", N_FIRE[0], clear_y, 270).pt("N_CLEAR_TURN", N_FIRE[0] - 2, clear_y, 90)
        r.add(r.go("N_CLEAR", heading=270), r.go("N_CLEAR_TURN", turn_by=1.0), r.action("IntakeOn"),
              r.go("FAR_FLOWER_IN", heading=90), r.go("FAR_FLOWER", heading=90),
              r.wait("The far FLOWER", when=["IntakeFull"], ms=2300))
    r.at = "FAR_FLOWER"
    if clear_y is None:
        r.add(r.go("N_FIRE", turn_after=0.3, turn_by=1.0))
    else:  # and back the same way: turned round clear of the pieces, then straight in to N_FIRE
        r.add(r.go("N_CLEAR_TURN", heading=90), r.go("N_CLEAR", turn_by=1.0), r.go("N_FIRE", heading=270))
    r.add(
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
        for f in autogen.FRICTIONS:
            for name, (design, _, _, _) in STAGED.items():
                study(f"{cls(name)},PartnerPreloadsRightAuto@50", runs=runs, designs=design,
                      extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                                 "BIOBUZZ_AUTO_FRICTION": f})
