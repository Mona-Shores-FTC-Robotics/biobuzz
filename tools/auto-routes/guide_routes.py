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


def rigid_v_wall(shape, name, angle=180):
    ANGLE[0] = angle
    qual_right.staged_row = row_face_first
    try:
        return shape_matrix.stages_for(shape, "wall", name)
    finally:
        qual_right.staged_row = _staged_row


ROUTES = {
    "qual-stages-angled-ramp-hook": lambda n: stages_hook(n, "B"),
    "qual-stages-wall-ramp-hook": lambda n: stages_hook(n, "A"),
    "qual-stages-angled-ramp-hook-h500": lambda n: stages_hook(n, "B", 500),
    "qual-stages-wall-ramp-hook-h500": lambda n: stages_hook(n, "A", 500),
    **{f"qual-stages-wall-{s}-face{'' if a == 180 else a}": (lambda n, s=s, a=a: rigid_v_wall(s, n, a))
       for s in ("rigid-v", "rigid-v-18-30", "rigid-v-20-45") for a in (180, 135, 150, 165)},
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
