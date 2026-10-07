# The whole robot as drawn, in one STEP

`build.py` writes `BIOBUZZ-robot.step`. It contains:
- the team's CAD (DHS Robot Copy) with the parts we replaced left out (the old intake's roller, motor, motor mount and
  pattern spacers; the "Launcher Concept"; the staged NECTARs), and the old intake's 11-hole channel raised 8 mm;
- the unified front (`cad/intake-b/`): side and float plates, the floating roller and its motor, the spread-arm FLOWER
  extractor drawn deployed, the Rigid V plates, the outer wheel plates' standoffs and the real odometry pods;
- the transfer (`cad/transfer/`), drawn at rest, with the turret bearing and its two cross-channels for reference;
- the Limelight on the stand-in mount the AdvantageScope model draws.

**Frame:** the AdvantageScope model's. +X forward, +Y left, +Z up, origin on the floor under the chassis centre (7.56 in
behind the front face). Millimetres. (The separate `cad/intake-b/` and `cad/transfer/` STEPs stay in the team CAD's own
frame, for importing into its Onshape assembly.)

**Where the file is:** it's about 284 MB (53 MB zipped), too big for git. The zip, `BIOBUZZ-robot-step.zip`, goes in the
team's Drive, in the `biobuzz` folder next to "DHS Robot Copy.step".

**Rebuild:**

    POD_DIR=<folder with pod1.brep, pod2.brep> python3 cad/full-robot/build.py "DHS Robot Copy.step" BIOBUZZ-robot.step

It takes about 4 minutes. `pod1.brep` and `pod2.brep` are the goBILDA pods cut from the example chassis
(`tools/robot-cad/`). Without `POD_DIR`, the pods are drawn as their envelope boxes.
