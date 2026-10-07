# The whole robot as drawn, in one STEP

`build.py` writes `BIOBUZZ-robot.step`. It contains:
- **The mentor's latest CAD** (Robot.step, 7 Oct). It moves its origin between exports, so it's lined up by the drive
  rails. The parts we replaced are left out:
  - his old intake: its roller, motor, mount, carriage and the V-guides it rode, and the pattern spacers;
  - his pinwheel and its servo, his two star wheels and the staged balls;
  - the drive wheels' shafts, which our outer plates' 80 mm ones replace.

  His 11-hole channel is raised 30 mm, and the launcher is moved 0.8 in forward, its front cross-channel and U-beams
  taken out (`cad/transfer/README.md` says why). His "Launcher Concept" is kept: goBILDA's turret, with two pairs of
  96 mm flywheels under it.
- **The unified front** (`cad/intake-b/`): side and float plates, the floating vector-wheel roller and its motor, the
  spread-arm FLOWER extractor drawn deployed, the Rigid V plates and the outer wheel plates' standoffs.
- **goBILDA's odometry pods**, part by part with their colours, cut from the example chassis.
- **The transfer** (`cad/transfer/`): ramp, wheel lane, sprung ceiling, the feeder and its pad under the flywheels.
- **The Limelight 3A** (Limelight's own STEP) on a goBILDA mount, the right way up. A 1121 low-side U-channel mast
  (6 hole) stands on the mentor's 9-hole front channel, bolted to his 35-hole L-beam. A 1111 angle pattern bracket
  (one leg bent 45°) sits on the back of the mast's top. A 1102 flat beam (9 hole) goes across it, and the camera
  bolts to the beam's end holes through its own M4 holes. The lens comes out at X 5.43, Y 0, 14.22 in up, looking 45°
  up. `TeamCode`'s `CameraMount` is still placeholders, to be measured on the robot.
- **Real vendor parts** in place of our drawn envelopes, when `VENDOR_DIR` is set (below): the Yellow Jacket motors, the
  servo, the pulleys, the bearings, the Gecko wheels, the collars and WCP's vector wheels.

**For Onshape** the top level is `FRAME` (everything that doesn't move) and one `MOVES n` group per moving body, each
named with the mate it needs. `--mentor` and `--ours` (in place of the STEP's first argument) write the mentor's half
and ours as two files in the same frame, each small enough to send. `ONSHAPE.md` covers importing, fixing, mating and
animating them.

**Still not drawn:** the roller's spring; screws in the mounts.

**Frame:** the AdvantageScope model's. +X forward, +Y left, +Z up, origin on the floor under the chassis centre (7.56 in
behind the front face). Millimetres. (The separate `cad/intake-b/` and `cad/transfer/` STEPs stay in the team CAD's own
frame, for importing into its Onshape assembly.)

**Where the file is:** about 440 MB as `LEAN=1` builds it with the vendor parts, too big for git. It goes in the
team's Drive, in the `biobuzz` folder next to the mentor's Robot.step.

**Rebuild:**

    LEAN=1 VENDOR_DIR=<vendor STEPs> EXAMPLE_STEP=<example chassis STEP> LL_STEP=LIMELIGHT3ACAD_STEP.stp \
        python3 cad/full-robot/build.py Robot.step BIOBUZZ-robot.step

It takes about 5 minutes.
- **The example chassis** is the team's, in Drive; the pods come from it.
- **The Limelight STEP** is https://downloads.limelightvision.io/cad/LIMELIGHT3ACAD_STEP.stp.
- **`VENDOR_DIR`** is a folder holding goBILDA's and WCP's STEP files, unzipped, any layout. goBILDA serves each one at
  `https://www.gobilda.com/content/step_files/<part number>.zip`; WCP's are at
  `https://ctre.download/wcp/cad/WCP-0353.step` and `-0354`. `real_parts.VENDOR` lists which part names each replaces.

Without `EXAMPLE_STEP` the pods are drawn as envelope boxes. Without `VENDOR_DIR` our parts stay as drawn envelopes and
the Limelight sits on the old stand-in mount. Without `LL_STEP` the camera itself is left out.

`LEAN=1` leaves out the mentor's screws, nuts, washers, other fasteners and the lettering on the mecanum rollers. It
opens faster. Use it for a file to look at; use the full one to build from.

**Only our parts, for Onshape:**

    EXAMPLE_STEP=... LL_STEP=... VENDOR_DIR=... python3 cad/full-robot/build.py --additions Robot.step BIOBUZZ-additions.step

This writes the front, the transfer, the launcher's new parts and the Limelight, and nothing of the mentor's. They are
placed in the frame of that Robot.step, so in Onshape you insert the file at the origin of his assembly and they land
where they belong. The changes to his own parts (the launcher 0.8 in forward, the 11-hole channel raised 30 mm, the
parts that come out) can't travel in this file; they are listed in `cad/transfer/README.md`.

`python3 cad/full-robot/real_parts.py <example chassis STEP> <LIMELIGHT3ACAD_STEP.stp> vendor_mesh.pkl` writes the same
vendor parts as meshes for the AdvantageScope model (its `VENDOR_PKL`).
