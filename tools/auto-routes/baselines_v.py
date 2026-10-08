"""The three qualifier baselines for the Rigid V robot (mentor, 6 Oct 2026 21:15 UTC: one robot, the Rigid V as drawn in
the team's CAD; the Flat Intake baseline dropped). Each is the body-designs branch's best route for the V, exported
under the baseline's name into TeamCode/autos:

- qual-shoots-right-v-fixed-west45-r: ShootsRight (alone.shoots_right_v, since 8 Oct 2026); qual-right-v, the sweep route with PARK first, is the previous one (experiments/).
- qual-stages-angled-v: the angled partner, "chase", turning out of the tunnel at x 55.5 (guide_routes.turning_west).
- qual-stages-wall-v: the wall partner, the row swept square (guide_routes.row_sweep, 90), the same turn.

    python3 baselines_v.py [runs]     exports them, then simulates them on the "rigid V" design (0 runs: export only).
"""
import sys
import autogen
import guide_routes
import park_first
import qual_right
import qual_shapes
import shape_matrix


def park_from_garden(r, tag="", **kw):
    """The park path, drawn from the GARDEN with the heading held at 270: the endgame guard cuts the GARDEN wait or
    the drive back at 26.7 s (the dwell puts the GARDEN at 25.5 s), and a path drawn from S_FIRE facing 90 turned the
    robot half round in the corner, the V's tips through the west wall (4 runs of 60, 7 Oct 2026). Held at 270 there is
    no turn: out of the corner north-east, then north up the west side, backwards into PARK. From S_FIRE (the fire not
    cut) the follower turns there, where there is room, and joins the path."""
    r.at = "GARDEN"
    return [r.go("PARK", ctrl=[(24, 30)], heading=270, park=True)]


def shoots_right(name):
    qual_shapes.ROUTE_OF[name] = qual_shapes.ROUTE_OF["qual-right-o3-rigid-v"]
    third = qual_right.third_load
    # PARK guarded (7 Oct 2026, with the dwell before a TIP, issue #167): the GARDEN's shots end at 27-28.4 s and the
    # unguarded "TIP 3 coming?" wait parked in 4 runs of 10. Now the park path follows the fire directly, so the
    # endgame guard cuts the fire for it; shots away still TIP after the buzzer (Competition Manual 10.5).
    qual_right.third_load = park_from_garden
    try:
        return qual_shapes.o3_shaped(name, None)
    finally:
        qual_right.third_load = third


# The wall partner parks at y 103-121 (qual_right.wall_partner): the row sweep runs with its west edge east of the
# partner's x 28, the row 6.5 in left of the centre line (guide_routes.ROW_X_OFFSET; mentor, 7 Oct 2026: both robots
# always PARK).
ROW_X_OFFSET_V = 5.5
WALL_KW = {"garden": "none"}  # PARK after TIP 2's spill, no GARDEN load (tail's garden="none")  # west edge 27.5 (the partner at x 18 ends at 27); the V tips at x 44 clear the far FLOWER (45.0)


# The fallback (the row fails to TIP 2, the far FLOWER does, about 1 run in 10): from N_FIRE one path to PARK, straight
# down the tunnel at x 57.5 with the heading held until it is south of the HIVE's feet (y 51-90), then round to PARK.
# As a tunnel run plus a park card, the guard cut the run between the feet and the park path's lead-in turned the robot
# there (clips in 23 of 60); from the north no path fits between the parked partner and the west foot (7 Oct 2026).
FALLBACK_PARK_CTRL = [(57.5, -5), (57.5, -10)]  # both under x 57.5: with (30, 10) the curve bent west inside the feet (the V tips at x 47, clips in 4 of 10)


def sweep_east_of_partner(build, name):
    import shape_matrix
    guide_routes.ROW_X_OFFSET[0] = ROW_X_OFFSET_V
    wall = shape_matrix.STAGES["wall"]
    shape_matrix.STAGES["wall"] = (wall[0], wall[1], {**wall[2], **WALL_KW})
    tail = qual_right.tail

    def park_down_the_tunnel(r, tag="", **kw):
        if tag == " (B)":
            return [r.wait("It lands (B)", when=["IntakeFull"], ms=1300),
                    r.go("PARK", ctrl=FALLBACK_PARK_CTRL, turn_after=0.45, turn_by=0.9, park=True)]
        return tail(r, tag=tag, **kw)
    qual_right.tail = park_down_the_tunnel
    try:
        return build(name)
    finally:
        guide_routes.ROW_X_OFFSET[0] = 0.0
        shape_matrix.STAGES["wall"] = wall
        qual_right.tail = tail


def shoots_right_fixed(name):
    """ShootsRight since 8 Oct 2026 (mentor: "yes agreed"): alone.shoots_right_v, TIP 3 from the wall FLOWER's 4 and the
    GARDEN's 4 fired from x 45, and a plan for a TIP 1 that never comes (wait 7 s, then our preloads at the right CELL,
    TIP 2, home down the lane, PARK). Needs no turret: 75.0 at 60 runs on the turret held still (qual-right-v 73.7),
    53.7 against a partner that never shoots (qual-right-v 18)."""
    import alone
    return alone.shoots_right_v(name, fire3=(45, 26, 90), ending="west", rescue=True, tip1_wait_ms=7000, rescue_home="lane")


BASELINES = {
    "qual-shoots-right-v-fixed-west45-r": shoots_right_fixed,
    "qual-stages-angled-v": guide_routes.turning_west(lambda n: shape_matrix.stages_for("rigid-v", "angled", n)),
    "qual-stages-wall-v": guide_routes.turning_west(lambda n: sweep_east_of_partner(lambda m: guide_routes.rigid_v_wall("rigid-v", m, 90, sweep=True), n)),
}
PARTNER = {"qual-shoots-right-v-fixed-west45-r": "PartnerPreloadsRightHighAuto", "qual-stages-angled-v": "PartnerAngledParkAuto",
           "qual-stages-wall-v": "PartnerStage19SideParkAuto"}

# The drawn V's body is 15.12 in long (its face 7.56 in from the centre), not the Flat Intake's 14.5 (7.25): the
# routes' FLOWER and GARDEN spots and the start move by the difference (qual_right.fit), or the body overlaps the
# FLOWER tube by 0.17 in at the pickup ("DRIVES INTO A FLOWER" in every run of the body-designs branch's V routes).
FRONT_IN_V = 15.12 / 2  # the body is 15.12 in long (15.24 wide): its face 7.56 in from the centre
# The V takes a FLOWER with the CAD's extractor, seated with the face 4.59 in from the FLOWER's centre (doc/robot-cad.md;
# 7.09 until 7 Oct 2026, a sign error in the CAD chat's measure), not with the intake mouth against the tube (2.2): its
# FLOWER points sit 2.39 in further out (mentor, 6 Oct 2026: the mechanism meets the FLOWER, not the body;
# RobotDesign.extractorSeatIn, the simulator deploys it on the approach).
FLOWER_FACE_V = 4.59


FLAP_AHEAD_V = 2.8  # the drawn V's flap tips ahead of the face (RobotDesign.flapForwardIn)
GARDEN_X_V = 9.5  # the V's tips 8.89 in aside, 0.6 in off the west wall


def build_for_v(build, name):
    front, face, flap = qual_right.FRONT_IN["option3"], qual_right.FLOWER_FACE_IN["option3"], qual_right.FLAP_AHEAD_IN["option3"]
    qual_right.FRONT_IN["option3"] = FRONT_IN_V
    qual_right.FLOWER_FACE_IN["option3"] = FLOWER_FACE_V
    qual_right.FLAP_AHEAD_IN["option3"] = FLAP_AHEAD_V
    garden_x = qual_right.GARDEN_X_IN["option3"]
    qual_right.GARDEN_X_IN["option3"] = GARDEN_X_V
    sweep = qual_right.O3["qual-right-o3-sweep"]
    qual_right.O3["qual-right-o3-sweep"] = {**sweep, "sweep_y": 12}  # the sweep 2 in off the south wall, for the turn in
    try:
        return build(name)
    finally:
        qual_right.FRONT_IN["option3"], qual_right.FLOWER_FACE_IN["option3"], qual_right.FLAP_AHEAD_IN["option3"] = front, face, flap
        qual_right.GARDEN_X_IN["option3"] = garden_x
        qual_right.O3["qual-right-o3-sweep"] = sweep


if __name__ == "__main__":
    runs = int(sys.argv[1]) if len(sys.argv) > 1 else 60
    for name, build in BASELINES.items():
        r = build_for_v(build, name)
        r.folder = autogen.AUTOS_DIR
        r.write()
        print("wrote", name)
    if runs:
        autogen.study(";".join(f"{qual_right.cls(n)},{PARTNER[n]}@50" for n in BASELINES), runs=runs, designs="rigid V",
                      extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40"})
