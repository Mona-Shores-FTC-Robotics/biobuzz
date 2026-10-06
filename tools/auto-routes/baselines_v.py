"""The three qualifier baselines for the Rigid V robot (mentor, 6 Oct 2026 21:15 UTC: one robot, the Rigid V as drawn in
the team's CAD; the Flat Intake baseline dropped). Each is the body-designs branch's best route for the V, exported
under the baseline's name into TeamCode/autos:

- qual-right-v: ShootsRight on the V's sweep route, PARK first when TIP 3 is late (park_first.py).
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


def shoots_right(name):
    qual_shapes.ROUTE_OF[name] = qual_shapes.ROUTE_OF["qual-right-o3-rigid-v"]
    third = qual_right.third_load
    qual_right.third_load = park_first.park_first
    try:
        return qual_shapes.o3_shaped(name, None)
    finally:
        qual_right.third_load = third


BASELINES = {
    "qual-right-v": shoots_right,
    "qual-stages-angled-v": guide_routes.turning_west(lambda n: shape_matrix.stages_for("rigid-v", "angled", n)),
    "qual-stages-wall-v": guide_routes.turning_west(lambda n: guide_routes.rigid_v_wall("rigid-v", n, 90, sweep=True)),
}
PARTNER = {"qual-right-v": "PartnerPreloadsRightAuto", "qual-stages-angled-v": "PartnerAngledParkAuto",
           "qual-stages-wall-v": "PartnerStage19SideParkAuto"}

# The drawn V's body is 15.12 in long (its face 7.56 in from the centre), not the Flat Intake's 14.5 (7.25): the
# routes' FLOWER and GARDEN spots and the start move by the difference (qual_right.fit), or the body overlaps the
# FLOWER tube by 0.17 in at the pickup ("DRIVES INTO A FLOWER" in every run of the body-designs branch's V routes).
FRONT_IN_V = 15.12 / 2  # the body is 15.12 in long (15.24 wide): its face 7.56 in from the centre
# The V takes a FLOWER with the CAD's extractor, seated with the face 7.09 in from the FLOWER's centre (doc/robot-cad.md),
# not with the intake mouth against the tube (2.2): its FLOWER points sit 4.89 in further out (mentor, 6 Oct 2026: the
# mechanism meets the FLOWER, not the body; RobotDesign.extractorSeatIn, the simulator deploys it on the approach).
FLOWER_FACE_V = 7.09


def build_for_v(build, name):
    front, face = qual_right.FRONT_IN["option3"], qual_right.FLOWER_FACE_IN["option3"]
    qual_right.FRONT_IN["option3"] = FRONT_IN_V
    qual_right.FLOWER_FACE_IN["option3"] = FLOWER_FACE_V
    try:
        return build(name)
    finally:
        qual_right.FRONT_IN["option3"], qual_right.FLOWER_FACE_IN["option3"] = front, face


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
