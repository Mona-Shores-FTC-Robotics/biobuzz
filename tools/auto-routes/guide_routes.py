"""The Stages Autos redrawn for each guide (mentor review of the logs, 6 Oct 2026).

The shape matrix ran the Flat Intake's Stages routes with the guide added, and the logs showed it:

- Ramp Hook. One arm, on the right (doc/ramp-hook.md), so it faces the centre line only at the south end:
  the hook can work for TIP 1 (our preloads), never for TIP 2 at the north end ("hook stays up"). The baseline
  fires TIP 1 from the start and drives off, so the hook was down for under a second. Here: fire from S_FIRE
  (near the HIVE), stop at the hook spot (the face 37.5 in from the south wall, as at the north end on
  ShootsRight) with the hook down as TIP 1 starts, hold `hold` ms after the CELL settles (the films: first touch 1.1-1.4 s
  after the TIP starts, about when it settles), then the baseline's "chase": north through the
  tunnel, fire the catch, the staged row, TIP 2. No hook spot at the north end.
- Rigid V with the wall partner (A). The baseline reaches A's row (x 29.6, y 128.6-137) backing west along
  y 118, then sliding north beside it, face toward it: the V's flap tips reach 3 in past the face and shove
  the row (0 of 4 picked up in the 18 in, 30 deg typical run). Here: from N_LOW, turn to face west as it
  drives, and come in face first from the field side, all 4 inside the V's mouth, head-on ("-face") or at an
  angle ("-face135" etc.: the heading; mentor: "just come in at an angle"). Centred at y 129.5, south
  of the row's middle, to keep the robot's north edge off the far FLOWER (47.4, 138.8); the north flap
  guides the last piece in.

    python3 guide_routes.py     writes them into experiments/
"""
import autogen
import qual_right
import shape_matrix
from qual import *

FIELD_IN = 141.5
S_HOOK = (70.6 - 7.25, round(37.5 - 7.25, 2), 90)  # the face 37.5 in from the south wall; the arm's side 7.25 in off the centre line
ROW_Y = 129.5  # Rigid V at A's row: its centre line (see above)


def stages_hook(name, partner, hold=1000):
    """Qual-PartnerStages for the Ramp Hook: TIP 1's spill held at S_HOOK, then the baseline's "chase"."""
    _, _, kw = shape_matrix.STAGES["angled" if partner == "B" else "wall"]
    kw = dict(kw)
    n_fire_y = kw.pop("n_fire_y")
    r = qual_right.fit("option3")(name, S_START, speed=50)
    ends(r)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", N_FIRE[0], n_fire_y, N_FIRE[2]).pt("S_HOOK", *S_HOOK)
    r.add(r.action("SpinUp"), r.go("S_FIRE", heading=90), fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000),
          r.go("S_HOOK", heading=90), r.wait("TIP 1 settles", when=["LeftCellUp"], ms=3500),
          r.wait("The hook holds TIP 1's spill", when=["IntakeFull"], ms=hold),
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


def stages_hook2(name, partner, row=None, ending=None, hold=1000, spot=S_HOOK, north_hook=True):
    """stages_hook after the mentor's review of its logs (6 Oct 2026, the wall partner's best run):

    - The staged row (A's): rotate clockwise from N_LOW and sweep up the row from the field side at a slant
      (`row`: the heading, row_sweep), not round behind it. None: the baseline's approach.
    - TIP 2: the hook down anyway (HookDown as the row's last shot lands, HookUp before driving): its arm is on
      the side away from the centre line, so the opening faces the opponent, but it still deadens the spill.
    - `ending`: what follows TIP 2's spill (qual_right.tail's wall_flower): None, fire it and the GARDEN (as
      before); "first", fire it, the wall FLOWER, then the GARDEN; "direct", from the tunnel straight to the wall
      FLOWER topping up, fire, then the GARDEN; "garden-first", top up at the GARDEN, fire, then the wall FLOWER.
    `spot` and `north_hook=False`: the same for a guide without a hook (the Rigid V waiting for TIP 1 at `spot`)."""
    _, _, kw = shape_matrix.STAGES["angled" if partner == "B" else "wall"]
    kw = dict(kw)
    n_fire_y = kw.pop("n_fire_y")
    extra = kw.pop("extra")
    if ending:
        kw["wall_flower"] = ending
    r = qual_right.fit("option3")(name, S_START, speed=50)
    ends(r)
    flower_points(r, "WALL_FLOWER", WALL_FLOWER_AT, 180)
    r.pt("S_FIRE", *S_FIRE).pt("N_FIRE", N_FIRE[0], n_fire_y, N_FIRE[2]).pt("S_HOOK", *spot)
    r.add(r.action("SpinUp"), r.go("S_FIRE", heading=90), fire(r, "Fire the preloads (TIP 1)", "Empty", ms=4000),
          r.go("S_HOOK", heading=90), r.wait("TIP 1 settles", when=["LeftCellUp"], ms=3500),
          r.wait("The hook holds TIP 1's spill" if north_hook else "Hold for TIP 1's spill", when=["IntakeFull"], ms=hold),
          tunnel(r, "N_TURN"), r.go("N_LOW", heading=270), fire(r, "Fire the catch", "Empty", ms=2200))
    r.at = "N_LOW"
    if row is not None and partner == "A":
        ANGLE[0] = row
        r.add(*row_sweep(r, partner, 1600, west=False))
    else:
        r.add(*qual_right.staged_row(r, partner, 1600, west=False))
    r.add(r.go("N_LOW", turn_after=0.2, turn_by=0.9), fire(r, "Fire the row", "Tip", ms=2500))
    r.at = "N_LOW"
    more = [*flower(r, "FAR_FLOWER", "The far FLOWER", ms=2300)]
    r.at = "FAR_FLOWER"
    more += [r.go("N_FIRE", turn_after=0.3, turn_by=1.0), fire(r, "Fire the far FLOWER (TIP 2)", "Tip", ms=2500)]
    if north_hook:
        more += [r.action("HookDown"), r.wait("The hook holds TIP 2's spill (B)", when=["IntakeFull"], ms=extra), r.action("HookUp")]
    else:
        more.append(r.wait("It lands (B)", when=["IntakeFull"], ms=extra))
    r.at = "N_FIRE"
    more += qual_right.tail(r, tag=" (B)", extra=0, **kw)
    r.at = "N_LOW"
    tipped = ([r.action("HookDown"), r.wait("The hook holds TIP 2's spill", when=["IntakeFull"], ms=extra), r.action("HookUp")]
              if north_hook else [r.wait("It lands", when=["IntakeFull"], ms=extra)]) + qual_right.tail(r, extra=0, **kw)
    r.add(r.wait("TIP 2?", when=["Tip"], ms=600, yes=tipped, no=more, yes_label="Yes", no_label="No: the far FLOWER"))
    return r


_staged_row = qual_right.staged_row


ANGLE = [180]  # the heading the Rigid V meets A's row at (set per route)


def row_face_first(r, partner, row_ms, west):
    """staged_row, but A's row from N_LOW face first: straight in at ANGLE[0], the face's middle to the row's
    east edge (y 129.5 head-on, as above; 131.5 at an angle, the robot's back then clear of the far FLOWER)."""
    import math
    if partner != "A" or west:
        return _staged_row(r, partner, row_ms, west)
    h = ANGLE[0]
    d = (math.cos(math.radians(h)), math.sin(math.radians(h)))
    tx, ty = 29.6 + 1.4, ROW_Y if h == 180 else 131.5
    end = (tx - (7.25 - 0.3) * d[0], ty - (7.25 - 0.3) * d[1])
    r.pt("ROW_S", round(end[0] - 6 * d[0], 2), round(end[1] - 6 * d[1], 2), h).pt("ROW_N", round(end[0], 2), round(end[1], 2), h)
    out = [r.go("ROW_S", turn_by=0.8), r.go("ROW_N", heading=h), r.wait("The staged row", when=["IntakeFull"], ms=row_ms)]
    r.at = "ROW_N"
    return out


ROW_X_OFFSET = [0.0]
ROW_PIECES_Y = (128.0, 130.9, 133.7, 136.5)  # A's row (AutoStudyTest.stagedFor), x 29.6
# Where the face stops, one step a piece. The pieces touch, so the face pushes the rest of the row along ahead
# of it into the north wall (there at y 134.5, 137.3, 140.1): the steps follow them to the wall.
SWEEP_FACE_Y = (127.0, 130.0, 133.4, 136.2, 138.0)  # the last step 138 (was 139): the V's tips 2.8 in ahead stay 0.7 in off the north wall


def row_sweep(r, partner, row_ms, west):
    """staged_row, but A's row from N_LOW swept: lined up south of it, then north up the row mouth first, the
    face's middle on the row (x 29.6), stopping 0.45 s at each step (SWEEP_FACE_Y) so the intake takes the piece
    touching it; the V keeps the rest in its mouth. ANGLE[0]: the heading (90 square to the row; 75 or 105 skewed)."""
    import math
    if partner != "A" or west:
        return _staged_row(r, partner, row_ms, west)
    h = ANGLE[0]
    d = (math.cos(math.radians(h)), math.sin(math.radians(h)))
    # ROW_X_OFFSET: the face's middle this far east of the row (0: on it). With the partner parked at y 112, its body
    # (x 10-28, to y 121) sits under the sweep's start, so the baseline sweeps with its west edge just east of it
    # (6.5: the row 6.5 in left of the centre line, inside the 13.8 in mouth; baselines_v, 7 Oct 2026).
    at = lambda fy: (round(29.6 + ROW_X_OFFSET[0] - 7.25 * d[0], 2), round(fy - 7.25 * d[1], 2))
    # ROW_S's face 2 in short of the first piece (was 4): with the drawn V's 15.24 in body, 4 in put the robot's south
    # edge over the parked partner A's north edge (y 109) by 0.7 in, "robots collide at 11.0 s" in every run.
    r.pt("ROW_S", *at(ROW_PIECES_Y[0] - 1.4 - 2), h)
    # Turned by 70% of the way (was 80%): turning beside the parked partner, the V's corners reached over it
    # ("robots collide at 9.7 s", 5 runs of 60, 55.7 points). 60 runs on the drawn V: 0.45 50.3 and 0.6 51.3 (the
    # early turn slows the path: TIP 2 at 21.6 s instead of 19.6), 0.7 53.3 with no collision.
    out = [r.go("ROW_S", turn_by=0.7)]
    for i, y in enumerate(SWEEP_FACE_Y):
        name = "ROW_N" if i == len(SWEEP_FACE_Y) - 1 else f"ROW_{i + 1}"
        r.pt(name, *at(y), h)
        out += [r.go(name, heading=h), r.wait(f"Row piece {i + 1}", when=["IntakeFull"], ms=450)]
    r.at = "ROW_N"
    return out


def rigid_v_wall(shape, name, angle=180, sweep=False):
    ANGLE[0] = angle
    qual_right.staged_row = row_sweep if sweep else row_face_first
    try:
        return shape_matrix.stages_for(shape, "wall", name)
    finally:
        qual_right.staged_row = _staged_row


def rigid_v_right(shaped, name):
    """ShootsRight for a Rigid V: its route (qual-right-o3-sweep: TIP 2's spill driven through, 4 of 4 in the logs)
    without the sweep through TIP 1's leftovers (0-1 picked up, about 2.5 s), straight into the GARDEN, and
    PARK first (park_first.py)."""
    import park_first
    import qual_shapes
    qual_right.O3["qual-right-o3-sweep-garden"] = {**qual_right.O3["qual-right-o3-sweep"], "garden": "two"}
    qual_shapes.ROUTE_OF[name] = "qual-right-o3-sweep-garden"
    third = qual_right.third_load
    qual_right.third_load = park_first.park_first
    try:
        return qual_shapes.o3_shaped(name, None)
    finally:
        qual_right.third_load = third


TURN_X = [None]  # N_TURN's x for this route (None: qual's 57.5)
_ends = qual_right.ends


def ends_turning_west(r):
    """qual's ends, N_TURN moved west to TURN_X[0]: the 18 in, 30 deg and 20 in, 45 deg Vs' flap tips swing 13.7-14.1 in
    out as the robot turns there, over the centre line (x 70.6) from x 57.5 (G402 at 8.4 s in the logs)."""
    _ends(r)
    if TURN_X[0] is not None:
        p = r.points["N_TURN"]
        r.pt("N_TURN", TURN_X[0], p[1], p[2])


def turning_west(build):
    def b(name):
        TURN_X[0] = 55.5
        qual_right.ends = ends_turning_west
        try:
            return build(name)
        finally:
            TURN_X[0] = None
            qual_right.ends = _ends
    return b


ROUTES = {
    # The mentor's review of the Ramp Hook's wall run: the hook down at TIP 2 too, the row swept at a slant, and
    # what follows TIP 2's spill.
    "qual-stages-wall-ramp-hook2": lambda n: stages_hook2(n, "A"),
    **{f"qual-stages-wall-ramp-hook2-row{a}": (lambda n, a=a: stages_hook2(n, "A", row=a)) for a in (105, 120, 135)},
    **{f"qual-stages-wall-ramp-hook2-row120-{e}": (lambda n, e=e: stages_hook2(n, "A", row=120, ending=e))
       for e in ("first", "direct", "garden-first")},
    "qual-stages-angled-ramp-hook2": lambda n: stages_hook2(n, "B"),
    # The Rigid V on the same plan (mentor, 6 Oct 2026: the V over the hook; the hook's Stages gain was its TIP 1 wait):
    # TIP 1 fired from S_FIRE, its spill held at the drop zone's middle (x 57.5, face 37.5 in from the wall), the row swept.
    **{f"qual-stages-{k}-rigid-v-catch": (lambda n, p=p, row=row: stages_hook2(n, p, row=row, spot=(57.5, S_HOOK[1], 90), north_hook=False))
       for k, p, row in (("angled", "B", None), ("wall", "A", 105))},
    **{f"qual-stages-angled-{s}-t555": turning_west(lambda n, s=s: shape_matrix.stages_for(s, "angled", n))
       for s in ("rigid-v-18-30", "rigid-v-20-45")},
    **{f"qual-stages-wall-{s}-sweep{a}-t555": turning_west(lambda n, s=s, a=a: rigid_v_wall(s, n, a, sweep=True))
       for s in ("rigid-v-18-30", "rigid-v-20-45") for a in (90, 105)},
    **{f"{n}-garden": (lambda name, n=n: rigid_v_right(n, name))
       for n in ("qual-right-o3-rigid-v", "qual-right-o3-rigid-v-18-30", "qual-right-o3-rigid-v-20-45")},
    "qual-stages-angled-ramp-hook": lambda n: stages_hook(n, "B"),
    "qual-stages-wall-ramp-hook": lambda n: stages_hook(n, "A"),
    "qual-stages-angled-ramp-hook-h500": lambda n: stages_hook(n, "B", 500),
    "qual-stages-wall-ramp-hook-h500": lambda n: stages_hook(n, "A", 500),
    **{f"qual-stages-wall-{s}-face{'' if a == 180 else a}": (lambda n, s=s, a=a: rigid_v_wall(s, n, a))
       for s in ("rigid-v", "rigid-v-18-30", "rigid-v-20-45") for a in (180, 135, 150, 165)},
    # Face first stops at the row's south end and pushes the rest off (1 of 4): sweep up it instead.
    **{f"qual-stages-wall-{s}-sweep{a}": (lambda n, s=s, a=a: rigid_v_wall(s, n, a, sweep=True))
       for s in ("rigid-v", "rigid-v-18-30", "rigid-v-20-45") for a in (75, 90, 105)},
}

if __name__ == "__main__":
    import sys
    for name, build in ROUTES.items():
        if sys.argv[1:] and name not in sys.argv[1:]:
            continue
        r = build(name)
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(name)
