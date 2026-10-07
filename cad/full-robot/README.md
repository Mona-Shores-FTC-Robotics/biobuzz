# The whole robot as drawn, in one STEP

`build.py` writes `BIOBUZZ-robot.step`. It contains:
- the mentor's latest CAD (Robot.step, 7 Oct; it moves its origin between exports, so it's lined up by the drive rails) with the parts we replaced left out (the old intake's roller, motor, mount, guides and
  pattern spacers; his pinwheel and its servo; his two star wheels; the staged balls; the drive wheels' shafts, which
  our outer plates' 80 mm ones replace), the old intake's 11-hole channel raised 21 mm, and the launcher's front
  cross-channel removed and its front U-beams 0.25 in further apart (`cad/transfer/README.md` says why). The designer's "Launcher
  Concept" is kept: goBILDA's turret with two pairs of 96 mm flywheels under it;
- the unified front (`cad/intake-b/`): side and float plates, the floating roller and its motor, the spread-arm FLOWER
  extractor drawn deployed, the Rigid V plates and the outer wheel plates' standoffs;
- goBILDA's odometry pods, part by part with their colours, cut from the example chassis;
- the transfer (`cad/transfer/`): ramp, wheel lane, sprung ceiling and the feeder cup under the flywheels;
- Limelight's own 3A STEP at the camera's pose, on the stand-in mount the AdvantageScope model draws.

**Still stand-ins:** the Limelight's mount (beam, plate, wedge), until the Limelight chat draws the real one; the
roller's spring isn't drawn.

**Frame:** the AdvantageScope model's. +X forward, +Y left, +Z up, origin on the floor under the chassis centre (7.56 in
behind the front face). Millimetres. (The separate `cad/intake-b/` and `cad/transfer/` STEPs stay in the team CAD's own
frame, for importing into its Onshape assembly.)

**Where the file is:** it's about 380 MB (37 MB as `BIOBUZZ-robot.step.xz`), too big for git. It goes in the
team's Drive, in the `biobuzz` folder next to the mentor's Robot.step.

**Rebuild:**

    EXAMPLE_STEP=<example chassis STEP> LL_STEP=LIMELIGHT3ACAD_STEP.stp \
        python3 cad/full-robot/build.py Robot.step BIOBUZZ-robot.step

It takes about 5 minutes. The example chassis is the team's (in Drive, where the pods come from); the Limelight STEP is
https://downloads.limelightvision.io/cad/LIMELIGHT3ACAD_STEP.stp. Without `EXAMPLE_STEP` the pods are drawn as their
envelope boxes; without `LL_STEP` only the camera's mount is drawn.

`python3 cad/full-robot/real_parts.py <example chassis STEP> <LIMELIGHT3ACAD_STEP.stp> vendor_mesh.pkl` writes the same
vendor parts as meshes for the AdvantageScope model (its `VENDOR_PKL`).
