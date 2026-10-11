# Parts (written by build.py)

CHECK marks a size not read from a vendor file. Two robots: double every line.

| Part | Module | Make | What |
|---|---|---|---|
| `drive_belt_BL` | corner_BL | buy | goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0275 belt |
| `drive_motor_BL` | corner_BL | buy | goBILDA 5203-2402-0014 Yellow Jacket, 435 RPM (13.7:1), as last season's drive (DECODE's Pedro constants: 537.7 ticks a rev, 96 mm wheels, 1:1) |
| `motor_plate_BL` | corner_BL | buy | goBILDA pattern plate 2 holes wide x 4 (CHECK the SKU), the motor's holes slotted for the belt, standing on the rail's web: the drive motor screws to it, the wheel's shaft through its lowest hole |
| `wheel_BL` | corner_BL | buy | goBILDA 3213-3606-0002 96 mm mecanum, one wheel of the set of 4 (its rollers make an X seen from above) |
| `wheel_shaft_BL` | corner_BL | buy | goBILDA 2106-4008-0800 8mm REX shaft, 80 mm, in a 1611 bearing in the rail's web and one in the outer plate; spacers and an e-clip |
| `drive_belt_BR` | corner_BR | buy | goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0275 belt |
| `drive_motor_BR` | corner_BR | buy | goBILDA 5203-2402-0014 Yellow Jacket, 435 RPM (13.7:1), as last season's drive (DECODE's Pedro constants: 537.7 ticks a rev, 96 mm wheels, 1:1) |
| `motor_plate_BR` | corner_BR | buy | goBILDA pattern plate 2 holes wide x 4 (CHECK the SKU), the motor's holes slotted for the belt, standing on the rail's web: the drive motor screws to it, the wheel's shaft through its lowest hole |
| `wheel_BR` | corner_BR | buy | goBILDA 3213-3606-0002 96 mm mecanum, one wheel of the set of 4 (its rollers make an X seen from above) |
| `wheel_shaft_BR` | corner_BR | buy | goBILDA 2106-4008-0800 8mm REX shaft, 80 mm, in a 1611 bearing in the rail's web and one in the outer plate; spacers and an e-clip |
| `drive_belt_FL` | corner_FL | buy | goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0360 belt |
| `drive_motor_FL` | corner_FL | buy | goBILDA 5203-2402-0014 Yellow Jacket, 435 RPM (13.7:1), as last season's drive (DECODE's Pedro constants: 537.7 ticks a rev, 96 mm wheels, 1:1) |
| `motor_plate_FL` | corner_FL | buy | goBILDA 1123-0048-0144, standing on the rail's web: the drive motor screws to it, the wheel's shaft through its lowest hole |
| `wheel_FL` | corner_FL | buy | goBILDA 3213-3606-0002 96 mm mecanum, one wheel of the set of 4 (its rollers make an X seen from above) |
| `wheel_shaft_FL` | corner_FL | buy | goBILDA 2106-4008-0800 8mm REX shaft, 80 mm, in a 1611 bearing in the rail's web and one in the outer plate; spacers and an e-clip |
| `drive_belt_FR` | corner_FR | buy | goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0360 belt |
| `drive_motor_FR` | corner_FR | buy | goBILDA 5203-2402-0014 Yellow Jacket, 435 RPM (13.7:1), as last season's drive (DECODE's Pedro constants: 537.7 ticks a rev, 96 mm wheels, 1:1) |
| `motor_plate_FR` | corner_FR | buy | goBILDA 1123-0048-0144, standing on the rail's web: the drive motor screws to it, the wheel's shaft through its lowest hole |
| `wheel_FR` | corner_FR | buy | goBILDA 3213-3606-0002 96 mm mecanum, one wheel of the set of 4 (its rollers make an X seen from above) |
| `wheel_shaft_FR` | corner_FR | buy | goBILDA 2106-4008-0800 8mm REX shaft, 80 mm, in a 1611 bearing in the rail's web and one in the outer plate; spacers and an e-clip |
| `battery` | elec | buy | Modern Robotics 12 V NiMH battery (the mentor's), on edge across the robot, low between the rails ahead of the rear cross channel, against the right rail (CHECK its size) |
| `battery_cradle_L` | elec | print | PETG: half the battery's cradle, hung from the rail's bottom flange on two risers (the battery held in by a strap) |
| `battery_cradle_R` | elec | print | PETG: half the battery's cradle, hung from the rail's bottom flange on two risers (the battery held in by a strap) |
| `control_hub` | elec | buy | REV Control Hub, standing on edge, plugs to the back |
| `expansion_hub` | elec | buy | REV Expansion Hub, standing on edge, plugs to the back |
| `limelight` | elec | buy | Limelight 3A (72 x 48 x 17 mm), pitched 45 deg up on the mast's wedge, two M4 into its back; its pose goes in CameraMount |
| `limelight_mast` | elec | print | PETG: the Limelight mast, its foot on two M4 through the front cross channel's top; the Limelight on its top's 45 deg wedge, about 14.5 in up, looking 45 deg up, on the robot's centreline (its offsets go in CameraMount) |
| `rack_L` | elec | print | PETG: half the electronics rack over the rear drive motors: a shelf and a back wall the hub screws to (slotted over the standoffs' screws, for the hex key); on two standoffs at its inner end |
| `rack_R` | elec | print | PETG: half the electronics rack over the rear drive motors: a shelf and a back wall the hub screws to (slotted over the standoffs' screws, for the hex key); on two standoffs at its inner end |
| `rack_standoff_L0` | elec | buy | goBILDA 1501-0006-0480 and 1501-0006-0240 M4 standoffs (48 + 24 mm) on an M4 set screw: 72 mm (goBILDA's 1501s stop at 56 mm) |
| `rack_standoff_L1` | elec | buy | goBILDA 1501-0006-0480 and 1501-0006-0240 M4 standoffs (48 + 24 mm) on an M4 set screw: 72 mm (goBILDA's 1501s stop at 56 mm) |
| `rack_standoff_R0` | elec | buy | goBILDA 1501-0006-0480 and 1501-0006-0240 M4 standoffs (48 + 24 mm) on an M4 set screw: 72 mm (goBILDA's 1501s stop at 56 mm) |
| `rack_standoff_R1` | elec | buy | goBILDA 1501-0006-0480 and 1501-0006-0240 M4 standoffs (48 + 24 mm) on an M4 set screw: 72 mm (goBILDA's 1501s stop at 56 mm) |
| `ex_arm_L` | extractor | print | PETG, 6 mm: extractor arm (cad/intake-b's, printed) |
| `ex_arm_R` | extractor | print | PETG, 6 mm: extractor arm (cad/intake-b's, printed) |
| `ex_block` | extractor | print | PETG: the FLOWER block (doc/ramp-hook.md's profile), threaded on the cross shaft (a REX bore), clamping collars either side |
| `ex_cross` | extractor | buy | goBILDA 1516-4008-2160 8mm REX standoff (216 mm) |
| `ex_gear_servo` | extractor | print | PETG-CF: the extractor's servo gear, on the servo's 25T horn (4 M3 on a 16 mm circle, CHECK on the horn), mod 1, 30 teeth, 1:1 with the stub's |
| `ex_gear_stub` | extractor | print | PETG-CF: the extractor's stub gear, clamped on the right stub (REX bore, a set screw), mod 1, 30 teeth |
| `ex_servo` | extractor | buy | goBILDA 2000-0025-0002 Torque servo, straight over the right stub, its spline inboard: its tabs on the mount (4 M4), its case out through the mount's window over the flap |
| `ex_servo_mount` | extractor | print | PETG-CF: the extractor servo's mount: the servo's tabs screw to its plate (4 M4 into inserts), its case out through the window; it sits astride the right flap's tall top, two M3 through its cheek into the flap |
| `ex_stub_L` | extractor | buy | goBILDA 1516-4008-0960 8mm REX standoff, cut |
| `ex_stub_R` | extractor | buy | goBILDA 1516-4008-0960 8mm REX standoff, cut |
| `front_cross` | frame | buy | goBILDA 1120-0009-0240 U-channel (240 mm), over the lane and the front drive motors, between the front motor plates on goBILDA pattern brackets (no uprights: the mouth stays open to its full width) |
| `outer_plate_L` | frame | buy | goBILDA 1123-0048-0336 pattern plate (1 x 13 hole): the wheels' outer bearings, on 56 mm standoffs |
| `outer_plate_R` | frame | buy | goBILDA 1123-0048-0336 pattern plate (1 x 13 hole): the wheels' outer bearings, on 56 mm standoffs |
| `rail_L` | frame | buy | goBILDA 1121-0014-0360 low-side U-channel (360 mm), the mentor's track |
| `rail_R` | frame | buy | goBILDA 1121-0014-0360 low-side U-channel (360 mm), the mentor's track |
| `rear_cross` | frame | buy | goBILDA 1121-0009-0240 low-side U-channel (240 mm), standing at the rails' back ends, between their webs on goBILDA pattern brackets (CHECK the bracket) |
| `standoff_L_-3_hi` | frame | buy | goBILDA 1501-0006-0560 M4 standoff, 56 mm |
| `standoff_L_-3_lo` | frame | buy | goBILDA 1501-0006-0560 M4 standoff, 56 mm |
| `standoff_L_1_hi` | frame | buy | goBILDA 1501-0006-0560 M4 standoff, 56 mm |
| `standoff_L_1_lo` | frame | buy | goBILDA 1501-0006-0560 M4 standoff, 56 mm |
| `standoff_R_-3_hi` | frame | buy | goBILDA 1501-0006-0560 M4 standoff, 56 mm |
| `standoff_R_-3_lo` | frame | buy | goBILDA 1501-0006-0560 M4 standoff, 56 mm |
| `standoff_R_1_hi` | frame | buy | goBILDA 1501-0006-0560 M4 standoff, 56 mm |
| `standoff_R_1_lo` | frame | buy | goBILDA 1501-0006-0560 M4 standoff, 56 mm |
| `flap_L` | front_L | print | PETG-CF, 6 mm (material: it carries the extractor's stub, so it must not flex): the fixed flap and the extractor's side plate; bolts to the front pod |
| `flap_face_L` | front_L | print | swappable face plate on three M3 screws into the flap's heat-set inserts: TPU 95A as a gyroid lattice, 10-20% infill, no top skin (it crushes and soaks up the hit); for the drop test also solid TPU (the control) and memory foam on a 1 mm backer (the deadest reference), all the same outline |
| `front_pad_L` | front_L | print | TPU 95A gyroid lattice, 10-20% infill, no top skin, 5 mm (or memory foam on a backer, as the drop test picks): the soft pad on the flap root's front face, where the spill lands; three countersunk M3 into the root, swappable |
| `flap_R` | front_R | print | PETG-CF, 6 mm (material: it carries the extractor's stub, so it must not flex): the fixed flap and the extractor's side plate; bolts to the front pod |
| `flap_face_R` | front_R | print | swappable face plate on three M3 screws into the flap's heat-set inserts: TPU 95A as a gyroid lattice, 10-20% infill, no top skin (it crushes and soaks up the hit); for the drop test also solid TPU (the control) and memory foam on a 1 mm backer (the deadest reference), all the same outline |
| `front_pad_R` | front_R | print | TPU 95A gyroid lattice, 10-20% infill, no top skin, 5 mm (or memory foam on a backer, as the drop test picks): the soft pad on the flap root's front face, where the spill lands; three countersunk M3 into the root, swappable |
| `intake_arm_L` | intake | print | PETG: swing arm, pivot to roller bearing (the right one also carries the motor) |
| `intake_arm_R` | intake | print | PETG: swing arm, pivot to roller bearing (the right one also carries the motor) |
| `intake_motor` | intake | buy | goBILDA 5203-2402-0003 Yellow Jacket, 1620 RPM: the roller (about 170 in/s at its surface) |
| `roller` | intake | buy | goBILDA 3632-4008-0048 48 mm Gecko wheels x10, as the mentor's roller, evenly spaced across the 8.7 in span on 8mm REX spacers |
| `roller_belt` | intake | buy | goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0275 belt |
| `roller_shaft` | intake | buy | goBILDA 8mm REX shaft, cut to 371 mm (CHECK) |
| `ceiling` | lane | print | PETG: the sprung ceiling's frame, hinged at its front on the posts' pins, its rear banded down (a piece lifts it), carrying free rollers |
| `ceiling_post_L0` | lane | print | PETG: a ceiling post, screwed to the wall's outer face; the ceiling's hinge pin through its top |
| `ceiling_post_R0` | lane | print | PETG: a ceiling post, screwed to the wall's outer face; the ceiling's hinge pin through its top |
| `ceiling_roller_0` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `ceiling_roller_1` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `ceiling_roller_2` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `ceiling_roller_3` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `front_bracket_L` | lane | print | PETG: on the rail's top flange (his motor fills the U-channel mount, so nothing bolts through that); the star's servo stands on its plate by its tabs (4 M4 into inserts), its case down through the plate's window to the clutch's hub; and it carries the intake arm's pivot (a shoulder screw) |
| `front_bracket_R` | lane | print | PETG: on the rail's top flange (his motor fills the U-channel mount, so nothing bolts through that); the star's servo stands on its plate by its tabs (4 M4 into inserts), its case down through the plate's window to the clutch's hub; and it carries the intake arm's pivot (a shoulder screw) |
| `lane_roller_0` | lane | print | TPU 95A: 24 mm x 1 in lane roller, free on two 8x14x5 bearings (the lane is passive: the roller's throw and the queue carry each piece back) |
| `lane_roller_1` | lane | print | TPU 95A: 24 mm x 1 in lane roller, free on two 8x14x5 bearings (the lane is passive: the roller's throw and the queue carry each piece back) |
| `lane_roller_2` | lane | print | TPU 95A: 24 mm x 1 in lane roller, free on two 8x14x5 bearings (the lane is passive: the roller's throw and the queue carry each piece back) |
| `lane_roller_3` | lane | print | TPU 95A: 24 mm x 1 in lane roller, free on two 8x14x5 bearings (the lane is passive: the roller's throw and the queue carry each piece back) |
| `lane_roller_4` | lane | print | TPU 95A: 24 mm x 1 in lane roller, free on two 8x14x5 bearings (the lane is passive: the roller's throw and the queue carry each piece back) |
| `lane_shaft_0` | lane | buy | goBILDA 8mm REX shaft, 144 mm (2106-4008-1440) |
| `lane_shaft_1` | lane | buy | goBILDA 8mm REX shaft, 144 mm (2106-4008-1440) |
| `lane_shaft_2` | lane | buy | goBILDA 8mm REX shaft, 144 mm (2106-4008-1440) |
| `lane_shaft_3` | lane | buy | goBILDA 8mm REX shaft, 144 mm (2106-4008-1440) |
| `lane_shaft_4` | lane | buy | goBILDA 8mm REX shaft, 144 mm (2106-4008-1440) |
| `lane_wall_L_front` | lane | print | PETG, 1/4 in: half a lane wall with its shafts' bearings; on two standoffs to the rail |
| `lane_wall_L_rear` | lane | print | PETG, 1/4 in: half a lane wall with its shafts' bearings; on two standoffs to the rail |
| `lane_wall_R_front` | lane | print | PETG, 1/4 in: half a lane wall with its shafts' bearings; on two standoffs to the rail |
| `lane_wall_R_rear` | lane | print | PETG, 1/4 in: half a lane wall with its shafts' bearings; on two standoffs to the rail |
| `mouth_floor_L` | lane | print | PETG: half the mouth, the ramp and the floor under the star wheels in one (the mouth's whole width), on two tabs on the rail's bottom flange; carries lane shafts 0 and 1's bearings on hangers |
| `mouth_floor_R` | lane | print | PETG: half the mouth, the ramp and the floor under the star wheels in one (the mouth's whole width), on two tabs on the rail's bottom flange; carries lane shafts 0 and 1's bearings on hangers |
| `ramp_skin_L` | lane | print | TPU 95A gyroid lattice, 10-20% infill, 2.5 mm, one thin top skin (pieces roll up it): the ramp's soft skin, where the spill lands; four countersunk M3 into the ramp, swappable like the flaps' faces |
| `ramp_skin_R` | lane | print | TPU 95A gyroid lattice, 10-20% infill, 2.5 mm, one thin top skin (pieces roll up it): the ramp's soft skin, where the spill lands; four countersunk M3 into the ramp, swappable like the flaps' faces |
| `star_L` | lane | buy | SWYFT Intake Wheel 3.5 in, 7 mm hex (SR-INTAKEWHEEL-35-7mm, 4 for $24.99): 0.5 in wide, 30A TPE on a nylon core, cut by the team into 16 flaps, as the mentor's; on a 7 mm hex through a ~25 mm black adapter (TBD) |
| `star_R` | lane | buy | SWYFT Intake Wheel 3.5 in, 7 mm hex (SR-INTAKEWHEEL-35-7mm, 4 for $24.99): 0.5 in wide, 30A TPE on a nylon core, cut by the team into 16 flaps, as the mentor's; on a 7 mm hex through a ~25 mm black adapter (TBD) |
| `star_clutch_L` | lane | print | PETG hub, 13.9-13.95 mm press bore for the one-way clutch (the mentor's: Amazon Sankoly-US SK230309GZZC-6P, read as HF081412, 8 x 14 x 12 mm drawn cup; CHECK with calipers): its top screwed to the servo's goBILDA 25T horn (4 M3), so the servo turns the hub, the clutch drives the shaft and the star one way only; a fast piece overruns it, and it can't be pushed backwards |
| `star_clutch_R` | lane | print | PETG hub, 13.9-13.95 mm press bore for the one-way clutch (the mentor's: Amazon Sankoly-US SK230309GZZC-6P, read as HF081412, 8 x 14 x 12 mm drawn cup; CHECK with calipers): its top screwed to the servo's goBILDA 25T horn (4 M3), so the servo turns the hub, the clutch drives the shaft and the star one way only; a fast piece overruns it, and it can't be pushed backwards |
| `star_servo_L` | lane | buy | goBILDA 2000-0025-0003 Speed servo, continuous mode, over the star, its spline on the star's axis |
| `star_servo_R` | lane | buy | goBILDA 2000-0025-0003 Speed servo, continuous mode, over the star, its spline on the star's axis |
| `star_shaft_L` | lane | buy | 8 mm ROUND hardened shaft (ground steel, h6), vertical: the clutch's rollers run on it, so not 8mm REX; the star on its lower end through a printed 7 mm hex adapter (a set screw on a ground flat) |
| `star_shaft_R` | lane | buy | 8 mm ROUND hardened shaft (ground steel, h6), vertical: the clutch's rollers run on it, so not 8mm REX; the star on its lower end through a printed 7 mm hex adapter (a set screw on a ground flat) |
| `wall_standoff_L_front0` | lane | buy | two goBILDA 1501-0006-0400 M4 standoffs (40 mm) on an M4 set screw: 80 mm (goBILDA's 1501s stop at 56 mm) |
| `wall_standoff_L_front1` | lane | buy | two goBILDA 1501-0006-0400 M4 standoffs (40 mm) on an M4 set screw: 80 mm (goBILDA's 1501s stop at 56 mm) |
| `wall_standoff_L_rear0` | lane | buy | two goBILDA 1501-0006-0400 M4 standoffs (40 mm) on an M4 set screw: 80 mm (goBILDA's 1501s stop at 56 mm) |
| `wall_standoff_L_rear1` | lane | buy | two goBILDA 1501-0006-0400 M4 standoffs (40 mm) on an M4 set screw: 80 mm (goBILDA's 1501s stop at 56 mm) |
| `wall_standoff_R_front0` | lane | buy | two goBILDA 1501-0006-0400 M4 standoffs (40 mm) on an M4 set screw: 80 mm (goBILDA's 1501s stop at 56 mm) |
| `wall_standoff_R_front1` | lane | buy | two goBILDA 1501-0006-0400 M4 standoffs (40 mm) on an M4 set screw: 80 mm (goBILDA's 1501s stop at 56 mm) |
| `wall_standoff_R_rear0` | lane | buy | two goBILDA 1501-0006-0400 M4 standoffs (40 mm) on an M4 set screw: 80 mm (goBILDA's 1501s stop at 56 mm) |
| `wall_standoff_R_rear1` | lane | buy | two goBILDA 1501-0006-0400 M4 standoffs (40 mm) on an M4 set screw: 80 mm (goBILDA's 1501s stop at 56 mm) |
| `feeder_arm_L` | launcher | print | PETG-CF, 6 mm: the feeder wheel's arm, two plates either side of the wheels on 1611 bearings, swinging on the pivot shaft; an extension spring (or a band) from it to the launcher's frame pulls it in, a stop screw sets the rest gap (the rig sets both) |
| `feeder_arm_R` | launcher | print | PETG-CF, 6 mm: the feeder wheel's arm, two plates either side of the wheels on 1611 bearings, swinging on the pivot shaft; an extension spring (or a band) from it to the launcher's frame pulls it in, a stop screw sets the rest gap (the rig sets both) |
| `feeder_belt_A` | launcher | buy | goBILDA 3417-4008-0024 24T HTD5 x2, 3412-0009-0215 belt: the feeder's motor to the left arm's pivot shaft |
| `feeder_belt_B` | launcher | buy | goBILDA 3417-4008-0024 24T HTD5 x2, 3412-0009-0215 belt: the pivot shaft up the arm to the feeder wheel |
| `feeder_front_block_L` | launcher | print | PETG: the arm's front pivot block, screwed to the lane wall's end |
| `feeder_front_block_R` | launcher | print | PETG: the arm's front pivot block, screwed to the lane wall's end |
| `feeder_motor` | launcher | buy | goBILDA 5203-2402-0005 Yellow Jacket, 1150 RPM (CHECK the speed on the rig), its face on the left rear block: fixed, never moves |
| `feeder_pivot_L` | launcher | buy | goBILDA 8mm REX shaft: the arm's pivot, and the feeder's drive: belt A's pulley behind, belt B's on the arm, turning in 1611 bearings in the rear and front blocks |
| `feeder_pivot_R` | launcher | buy | goBILDA 8mm REX shaft: the arm's pivot, in the rear and front blocks |
| `feeder_rear_block_L` | launcher | print | PETG: the arm's rear pivot block, the feeder motor's face plate, the spring's lug, and the foot the arm's tab rests on (an M3 set screw in it sets the rest gap), on the launcher tray |
| `feeder_rear_block_R` | launcher | print | PETG: the arm's rear pivot block, the spring's lug, and the foot the arm's tab rests on (an M3 set screw in it sets the rest gap), on the launcher tray |
| `feeder_shaft_L` | launcher | buy | goBILDA 8mm REX shaft, in 1611 bearings in the arm's two plates; belt B's 24T pulley on it |
| `feeder_shaft_R` | launcher | buy | goBILDA 8mm REX shaft, in 1611 bearings in the arm's two plates, the wheels free on bearings |
| `feeder_spring_L` | launcher | buy | compression spring, about 0.2 in OD, 0.6 in free: the rear block's lug pushes the arm in (rate and preload from the rig; CHECK) |
| `feeder_spring_R` | launcher | buy | compression spring, about 0.2 in OD, 0.6 in free: the rear block's lug pushes the arm in (rate and preload from the rig; CHECK) |
| `feeder_wheel_L` | launcher | buy | goBILDA 3632-4008-0048 48 mm Gecko wheels x2 (as the roller's): driven through its arm's belt |
| `feeder_wheel_R` | launcher | buy | goBILDA 3632-4008-0048 48 mm Gecko wheels x2 (as the roller's): free, in the pad's place |
| `fly_anchor_L` | launcher | print | PETG: the flywheel side's spring anchor and stop, on the rear top channel's front face; an M4 stop screw through it sets the rest gap (the rig sets it) |
| `fly_anchor_R` | launcher | print | PETG: the flywheel side's spring anchor and stop, on the rear top channel's front face; an M4 stop screw through it sets the rest gap (the rig sets it) |
| `fly_block_L_front` | launcher | print | PETG-CF: the flywheel's front bearing block, the shaft's 8x14 bearing in it, sliding in a 0.41 in slot in his front plate |
| `fly_block_R_front` | launcher | print | PETG-CF: the flywheel's front bearing block, the shaft's 8x14 bearing in it, sliding in a 0.41 in slot in his front plate |
| `fly_spring_L` | launcher | buy | extension spring, about 0.25 in OD: the anchor to the yoke (rate and preload from the rig; CHECK) |
| `fly_spring_R` | launcher | buy | extension spring, about 0.25 in OD: the anchor to the yoke (rate and preload from the rig; CHECK) |
| `fly_yoke_L` | launcher | print | PETG-CF, one piece: the flywheel's rear bearing block (in the back plate's slot), up behind the plate and over to the motor's bracket, so the motor rides with the wheels |
| `fly_yoke_R` | launcher | print | PETG-CF, one piece: the flywheel's rear bearing block (in the back plate's slot), up behind the plate and over to the motor's bracket, so the motor rides with the wheels |
| `hood` | launcher | print | PETG: the hood that turns the shot out (drawn as a tube and a lid) |
| `launcher_foot_L0` | launcher | print | PETG: a launcher foot, between the rail's top flange and the launcher's side channel: an M4 down through a pocket into a nut under the rail's flange (fitted before the launcher goes on), and one down from inside the launcher's channel into an insert (the launcher lifts off its feet) |
| `launcher_foot_L1` | launcher | print | PETG: a launcher foot, between the rail's top flange and the launcher's side channel: an M4 down through a pocket into a nut under the rail's flange (fitted before the launcher goes on), and one down from inside the launcher's channel into an insert (the launcher lifts off its feet) |
| `launcher_foot_R0` | launcher | print | PETG: a launcher foot, between the rail's top flange and the launcher's side channel: an M4 down through a pocket into a nut under the rail's flange (fitted before the launcher goes on), and one down from inside the launcher's channel into an insert (the launcher lifts off its feet) |
| `launcher_foot_R1` | launcher | print | PETG: a launcher foot, between the rail's top flange and the launcher's side channel: an M4 down through a pocket into a nut under the rail's flange (fitted before the launcher goes on), and one down from inside the launcher's channel into an insert (the launcher lifts off its feet) |
| `launcher_tray_L` | launcher | print | PETG, 3.3 mm: half the launcher's tray, across the rails' bottom flanges under the feeder floor; the feeder arms' blocks and spring posts stand on it |
| `launcher_tray_R` | launcher | print | PETG, 3.3 mm: half the launcher's tray, across the rails' bottom flanges under the feeder floor; the feeder arms' blocks and spring posts stand on it |
| `turret_drive_plate` | launcher | cut | 3/16 in aluminium (or 6 mm PETG-CF): the turret's drive plate, his redrawn shorter (to X 3.3, clear of the front drive motors), with windows for the servo and the encoders; under the launcher's front top channel |
| `odo_adapter_F` | odometry | print | PETG: the odometry pod's adapter, inside the rail's web (the pod's offsets are measured on the robot, for the Pinpoint) |
| `odo_pod_forward` | odometry | buy | goBILDA 3110-0001-0002 4-bar odometry pod (96 mm drive wheel version), its wheel rolling along X: screwed by its plate's side face (its tapped holes) to a printed adapter from the right rail |
| `odo_pod_strafe` | odometry | buy | goBILDA 3110-0001-0002 4-bar odometry pod, its wheel rolling along Y: outside the right rail between the wheels, its back plate on a printed spacer on the web |
| `odo_spacer_S` | odometry | print | PETG, 5 mm: the strafe pod's spacer on the right rail's web, pocketed over the lane walls' screw heads |
| `lm_*` | launcher | buy | the mentor's launcher module (cad/modules/launcher-module), placed by the launch column |

## Screws (v2, drawn and checked by build.py)

| Screw | One robot | Two robots |
|---|---|---|
| M3 x 8 socket head (countersunk for the faces) | 6 | 12 |
| M3 x 10 socket head | 2 | 4 |
| M3 x 12 socket head | 8 | 16 |
| M4 x 10 socket head | 20 | 40 |
| M4 x 12 socket head | 24 | 48 |
| M4 x 16 socket head | 12 | 24 |
| M4 x 20 socket head | 8 | 16 |
| M4 x 25 socket head | 6 | 12 |
| M5 x 18 socket head | 2 | 4 |
| M3 heat-set insert | 16 | 32 |
| M4 heat-set insert | 12 | 24 |
| M5 heat-set insert | 2 | 4 |
| M4 nyloc nut | 26 | 52 |
| on the devices (servos, hubs, intake motor, Limelight): see below | 120 | 240 |

Buy 20% over for each line: dropped screws and stripped inserts.

## Every joint

| Joint | Holds | Screw | Through | Into |
|---|---|---|---|---|
| flap_L_6.52 | the flap's root to the rail's web | M4 x 12 | `rail_L` | an insert in `flap_L` |
| flap_L_6.84 | the flap's root to the rail's web | M4 x 12 | `rail_L` | an insert in `flap_L` |
| face_L0 | the swappable face to its flap (countersunk, flush on the face) | M3 x 8 | `flap_face_L` | an insert in `flap_L` |
| face_L1 | the swappable face to its flap (countersunk, flush on the face) | M3 x 8 | `flap_face_L` | an insert in `flap_L` |
| face_L2 | the swappable face to its flap (countersunk, flush on the face) | M3 x 8 | `flap_face_L` | an insert in `flap_L` |
| wall_L_rear0 | a lane wall to its standoff (counterbored: the head below the lane's face) | M4 x 10 | `lane_wall_L_rear` | `wall_standoff_L_rear0`'s thread |
| wall_rail_L_rear0 | the standoff to the rail's web (from outside) | M4 x 10 | `rail_L` | `wall_standoff_L_rear0`'s thread |
| wall_L_rear1 | a lane wall to its standoff (counterbored: the head below the lane's face) | M4 x 10 | `lane_wall_L_rear` | `wall_standoff_L_rear1`'s thread |
| wall_rail_L_rear1 | the standoff to the rail's web (from outside) | M4 x 10 | `rail_L` | `wall_standoff_L_rear1`'s thread |
| wall_L_front0 | a lane wall to its standoff (counterbored: the head below the lane's face) | M4 x 10 | `lane_wall_L_front` | `wall_standoff_L_front0`'s thread |
| wall_rail_L_front0 | the standoff to the rail's web (from outside) | M4 x 10 | `rail_L` | `wall_standoff_L_front0`'s thread |
| wall_L_front1 | a lane wall to its standoff (counterbored: the head below the lane's face) | M4 x 10 | `lane_wall_L_front` | `wall_standoff_L_front1`'s thread |
| wall_rail_L_front1 | the standoff to the rail's web (from outside) | M4 x 10 | `rail_L` | `wall_standoff_L_front1`'s thread |
| mouth_L_3.37 | the mouth to the rail's bottom flange | M4 x 16 | `mouth_floor_L`, `rail_L` | a nyloc |
| mouth_L_5.26 | the mouth to the rail's bottom flange | M4 x 16 | `mouth_floor_L`, `rail_L` | a nyloc |
| bracket_L_3.06 | the front bracket's foot to the rail's top flange | M4 x 16 | `front_bracket_L`, `rail_L` | a nyloc |
| bracket_L_3.37 | the front bracket's foot to the rail's top flange | M4 x 16 | `front_bracket_L`, `rail_L` | a nyloc |
| arm_pivot_L | the intake arm's pivot: a shoulder screw from inside the bracket, turning in a flanged bushing in its boss, into the arm's insert (the drive motor's plate is just outboard of the arm) | M5 x 18 | `front_bracket_L` | an insert in `intake_arm_L` |
| post_L0_1.4 | a ceiling post to the wall (counterbored in the wall) | M3 x 12 | `lane_wall_L_front` | an insert in `ceiling_post_L0` |
| post_L0_2.2 | a ceiling post to the wall (counterbored in the wall) | M3 x 12 | `lane_wall_L_front` | an insert in `ceiling_post_L0` |
| cradle_L_-6.08 | the battery cradle's riser to the rail's bottom flange | M4 x 12 | `rail_L` | an insert in `battery_cradle_L` |
| cradle_L_-5.45 | the battery cradle's riser to the rail's bottom flange | M4 x 12 | `rail_L` | an insert in `battery_cradle_L` |
| rack_L0 | the electronics rack to its standoff | M4 x 12 | `rack_L` | `rack_standoff_L0`'s thread |
| rack_standoff_L0 | the rack's standoff to the rear cross channel's top flange (from inside the channel) | M4 x 10 | `rear_cross` | `rack_standoff_L0`'s thread |
| rack_L1 | the electronics rack to its standoff | M4 x 12 | `rack_L` | `rack_standoff_L1`'s thread |
| rack_standoff_L1 | the rack's standoff to the rear cross channel's top flange (from inside the channel) | M4 x 10 | `rear_cross` | `rack_standoff_L1`'s thread |
| foot_rail_L0 | a launcher foot to the rail's top flange | M4 x 20 | `launcher_foot_L0`, `rail_L` | a nyloc |
| foot_lm_L0 | the launcher's side channel to its foot (CHECK: his channel's flange holes don't read cleanly from his model: mark the foot's insert from the real channel) | M4 x 12 | `lm_Hole_Lowside_U-Channel_GB_6__1` | an insert in `launcher_foot_L0` |
| foot_rail_L1 | a launcher foot to the rail's top flange | M4 x 20 | `launcher_foot_L1`, `rail_L` | a nyloc |
| foot_lm_L1 | the launcher's side channel to its foot (CHECK: his channel's flange holes don't read cleanly from his model: mark the foot's insert from the real channel) | M4 x 12 | `lm_Hole_Lowside_U-Channel_GB_6__1` | an insert in `launcher_foot_L1` |
| tray_L_-4.19 | the launcher tray to the rail's bottom flange | M4 x 12 | `launcher_tray_L`, `rail_L` | a nyloc |
| tray_L_-3.56 | the launcher tray to the rail's bottom flange | M4 x 12 | `launcher_tray_L`, `rail_L` | a nyloc |
| feeder_block_L_2.6 | the feeder arm's rear block to the tray | M4 x 20 | `feeder_rear_block_L`, `launcher_tray_L` | a nyloc |
| feeder_block_L_2.95 | the feeder arm's rear block to the tray | M4 x 20 | `feeder_rear_block_L`, `launcher_tray_L` | a nyloc |
| feeder_front_L_0.95 | the feeder arm's front block to the lane wall's end | M3 x 12 | `feeder_front_block_L` | an insert in `lane_wall_L_rear` |
| feeder_front_L_1.42 | the feeder arm's front block to the lane wall's end | M3 x 12 | `feeder_front_block_L` | an insert in `lane_wall_L_rear` |
| anchor_L_0.75 | the flywheel spring's anchor to the rear top channel (CHECK: through the channel's front face: mark from the real channel) | M4 x 25 | `fly_anchor_L`, `lm_Hole_Lowside_U-Channel_GB_2__1` | a nyloc |
| anchor_L_2.2 | the flywheel spring's anchor to the rear top channel (CHECK: through the channel's front face: mark from the real channel) | M4 x 25 | `fly_anchor_L`, `lm_Hole_Lowside_U-Channel_GB_2__1` | a nyloc |
| flap_R_6.52 | the flap's root to the rail's web | M4 x 12 | `rail_R` | an insert in `flap_R` |
| flap_R_6.84 | the flap's root to the rail's web | M4 x 12 | `rail_R` | an insert in `flap_R` |
| face_R0 | the swappable face to its flap (countersunk, flush on the face) | M3 x 8 | `flap_face_R` | an insert in `flap_R` |
| face_R1 | the swappable face to its flap (countersunk, flush on the face) | M3 x 8 | `flap_face_R` | an insert in `flap_R` |
| face_R2 | the swappable face to its flap (countersunk, flush on the face) | M3 x 8 | `flap_face_R` | an insert in `flap_R` |
| odo_F2.52 | the forward odometry pod, through its adapter and the rail's web (from outside) into its side face | M4 x 25 | `rail_R`, `odo_adapter_F` | `odo_pod_forward`'s thread |
| odo_F1.26 | the forward odometry pod, through its adapter and the rail's web (from outside) into its side face | M4 x 25 | `rail_R`, `odo_adapter_F` | `odo_pod_forward`'s thread |
| odo_S2.52 | the strafe odometry pod: from inside the rail, through the web and its spacer into its back plate | M4 x 16 | `rail_R`, `odo_spacer_S` | `odo_pod_strafe`'s thread |
| odo_S1.26 | the strafe odometry pod: from inside the rail, through the web and its spacer into its back plate | M4 x 16 | `rail_R`, `odo_spacer_S` | `odo_pod_strafe`'s thread |
| wall_R_rear0 | a lane wall to its standoff (counterbored: the head below the lane's face) | M4 x 10 | `lane_wall_R_rear` | `wall_standoff_R_rear0`'s thread |
| wall_rail_R_rear0 | the standoff to the rail's web (from outside) | M4 x 10 | `rail_R` | `wall_standoff_R_rear0`'s thread |
| wall_R_rear1 | a lane wall to its standoff (counterbored: the head below the lane's face) | M4 x 10 | `lane_wall_R_rear` | `wall_standoff_R_rear1`'s thread |
| wall_rail_R_rear1 | the standoff to the rail's web (from outside) | M4 x 10 | `rail_R` | `wall_standoff_R_rear1`'s thread |
| wall_R_front0 | a lane wall to its standoff (counterbored: the head below the lane's face) | M4 x 10 | `lane_wall_R_front` | `wall_standoff_R_front0`'s thread |
| wall_rail_R_front0 | the standoff to the rail's web (from outside) | M4 x 10 | `rail_R` | `wall_standoff_R_front0`'s thread |
| wall_R_front1 | a lane wall to its standoff (counterbored: the head below the lane's face) | M4 x 10 | `lane_wall_R_front` | `wall_standoff_R_front1`'s thread |
| wall_rail_R_front1 | the standoff to the rail's web (from outside) | M4 x 10 | `rail_R` | `wall_standoff_R_front1`'s thread |
| mouth_R_3.37 | the mouth to the rail's bottom flange | M4 x 16 | `mouth_floor_R`, `rail_R` | a nyloc |
| mouth_R_5.26 | the mouth to the rail's bottom flange | M4 x 16 | `mouth_floor_R`, `rail_R` | a nyloc |
| bracket_R_3.06 | the front bracket's foot to the rail's top flange | M4 x 16 | `front_bracket_R`, `rail_R` | a nyloc |
| bracket_R_3.37 | the front bracket's foot to the rail's top flange | M4 x 16 | `front_bracket_R`, `rail_R` | a nyloc |
| arm_pivot_R | the intake arm's pivot: a shoulder screw from inside the bracket, turning in a flanged bushing in its boss, into the arm's insert (the drive motor's plate is just outboard of the arm) | M5 x 18 | `front_bracket_R` | an insert in `intake_arm_R` |
| post_R0_1.4 | a ceiling post to the wall (counterbored in the wall) | M3 x 12 | `lane_wall_R_front` | an insert in `ceiling_post_R0` |
| post_R0_2.2 | a ceiling post to the wall (counterbored in the wall) | M3 x 12 | `lane_wall_R_front` | an insert in `ceiling_post_R0` |
| cradle_R_-6.08 | the battery cradle's riser to the rail's bottom flange | M4 x 12 | `rail_R` | an insert in `battery_cradle_R` |
| cradle_R_-5.45 | the battery cradle's riser to the rail's bottom flange | M4 x 12 | `rail_R` | an insert in `battery_cradle_R` |
| rack_R0 | the electronics rack to its standoff | M4 x 12 | `rack_R` | `rack_standoff_R0`'s thread |
| rack_standoff_R0 | the rack's standoff to the rear cross channel's top flange (from inside the channel) | M4 x 10 | `rear_cross` | `rack_standoff_R0`'s thread |
| rack_R1 | the electronics rack to its standoff | M4 x 12 | `rack_R` | `rack_standoff_R1`'s thread |
| rack_standoff_R1 | the rack's standoff to the rear cross channel's top flange (from inside the channel) | M4 x 10 | `rear_cross` | `rack_standoff_R1`'s thread |
| foot_rail_R0 | a launcher foot to the rail's top flange | M4 x 20 | `launcher_foot_R0`, `rail_R` | a nyloc |
| foot_lm_R0 | the launcher's side channel to its foot (CHECK: his channel's flange holes don't read cleanly from his model: mark the foot's insert from the real channel) | M4 x 12 | `lm_Hole_Lowside_U-Channel_GB_4__1` | an insert in `launcher_foot_R0` |
| foot_rail_R1 | a launcher foot to the rail's top flange | M4 x 20 | `launcher_foot_R1`, `rail_R` | a nyloc |
| foot_lm_R1 | the launcher's side channel to its foot (CHECK: his channel's flange holes don't read cleanly from his model: mark the foot's insert from the real channel) | M4 x 12 | `lm_Hole_Lowside_U-Channel_GB_4__1` | an insert in `launcher_foot_R1` |
| tray_R_-4.19 | the launcher tray to the rail's bottom flange | M4 x 12 | `launcher_tray_R`, `rail_R` | a nyloc |
| tray_R_-3.56 | the launcher tray to the rail's bottom flange | M4 x 12 | `launcher_tray_R`, `rail_R` | a nyloc |
| feeder_block_R_2.6 | the feeder arm's rear block to the tray | M4 x 20 | `feeder_rear_block_R`, `launcher_tray_R` | a nyloc |
| feeder_block_R_2.95 | the feeder arm's rear block to the tray | M4 x 20 | `feeder_rear_block_R`, `launcher_tray_R` | a nyloc |
| feeder_front_R_0.95 | the feeder arm's front block to the lane wall's end | M3 x 12 | `feeder_front_block_R` | an insert in `lane_wall_R_rear` |
| feeder_front_R_1.42 | the feeder arm's front block to the lane wall's end | M3 x 12 | `feeder_front_block_R` | an insert in `lane_wall_R_rear` |
| anchor_R_0.75 | the flywheel spring's anchor to the rear top channel (CHECK: through the channel's front face: mark from the real channel) | M4 x 25 | `fly_anchor_R`, `lm_Hole_Lowside_U-Channel_GB_2__1` | a nyloc |
| anchor_R_2.2 | the flywheel spring's anchor to the rear top channel (CHECK: through the channel's front face: mark from the real channel) | M4 x 25 | `fly_anchor_R`, `lm_Hole_Lowside_U-Channel_GB_2__1` | a nyloc |
| ex_mount_0 | the extractor servo's mount: through its cheek into the right flap's tall top (an insert in the flap) | M3 x 10 | `ex_servo_mount` | an insert in `flap_R` |
| ex_mount_1 | the extractor servo's mount: through its cheek into the right flap's tall top (an insert in the flap) | M3 x 10 | `ex_servo_mount` | an insert in `flap_R` |
| mast_-0.55 | the mast's foot to the front cross channel's top | M4 x 16 | `limelight_mast`, `front_cross` | a nyloc |
| mast_0.55 | the mast's foot to the front cross channel's top | M4 x 16 | `limelight_mast`, `front_cross` | a nyloc |
| hood_0 | the hood's flange to the turret's 176T gear (its 4 mm holes on a 64 mm radius, at 0, 90, 180 and 270 deg from the drive gear) (CHECK: confirm the 64 mm-radius holes on the part, and whether they go on into the race (the screws' length); the gear's free 56 mm-radius holes are the fallback) | M4 x 12 | `hood` | `lm_2325-0105-0176_1`'s thread |
| hood_1 | the hood's flange to the turret's 176T gear (its 4 mm holes on a 64 mm radius, at 0, 90, 180 and 270 deg from the drive gear) (CHECK: confirm the 64 mm-radius holes on the part, and whether they go on into the race (the screws' length); the gear's free 56 mm-radius holes are the fallback) | M4 x 12 | `hood` | `lm_2325-0105-0176_1`'s thread |
| hood_2 | the hood's flange to the turret's 176T gear (its 4 mm holes on a 64 mm radius, at 0, 90, 180 and 270 deg from the drive gear) (CHECK: confirm the 64 mm-radius holes on the part, and whether they go on into the race (the screws' length); the gear's free 56 mm-radius holes are the fallback) | M4 x 12 | `hood` | `lm_2325-0105-0176_1`'s thread |
| hood_3 | the hood's flange to the turret's 176T gear (its 4 mm holes on a 64 mm radius, at 0, 90, 180 and 270 deg from the drive gear) (CHECK: confirm the 64 mm-radius holes on the part, and whether they go on into the race (the screws' length); the gear's free 56 mm-radius holes are the fallback) | M4 x 12 | `hood` | `lm_2325-0105-0176_1`'s thread |

| Held without a screw of its own | By | How |
|---|---|---|
| `ceiling` | `ceiling_post_L0` | hinged on a pin (an M4 shoulder screw) through its ear and the post |
| `ex_stub_L` | `flap_L` | turns in a goBILDA 1611 flanged bearing in the flap |
| `ex_arm_L` | `ex_stub_L` | clamped on the stub (REX bore, a set screw) |
| `ex_cross` | `ex_arm_L` | through both arms, M4 screws into its ends |
| `star_shaft_L` | `star_servo_L` | on the servo's spline through a printed coupler (TBD with the mentor) |
| `star_clutch_L` | `star_shaft_L` | the one-way clutch pressed in its bore, on the round shaft |
| `star_L` | `star_clutch_L` | on the hub's hex |
| `roller_shaft` | `intake_arm_L` | in a goBILDA 1611 flanged bearing in each arm, e-clips |
| `ceiling` | `ceiling_post_R0` | hinged on a pin (an M4 shoulder screw) through its ear and the post |
| `ex_stub_R` | `flap_R` | turns in a goBILDA 1611 flanged bearing in the flap |
| `ex_arm_R` | `ex_stub_R` | clamped on the stub (REX bore, a set screw) |
| `ex_cross` | `ex_arm_R` | through both arms, M4 screws into its ends |
| `star_shaft_R` | `star_servo_R` | on the servo's spline through a printed coupler (TBD with the mentor) |
| `star_clutch_R` | `star_shaft_R` | the one-way clutch pressed in its bore, on the round shaft |
| `star_R` | `star_clutch_R` | on the hub's hex |
| `roller_shaft` | `intake_arm_R` | in a goBILDA 1611 flanged bearing in each arm, e-clips |
| `wheel_shaft_FL` | `rail_L` | in a goBILDA 1611 bearing in the rail's web, and one in the outer plate |
| `wheel_FL` | `wheel_shaft_FL` | on the REX shaft, a hub |
| `drive_belt_FL` | `wheel_shaft_FL` | on the 24T pulleys |
| `wheel_shaft_BL` | `rail_L` | in a goBILDA 1611 bearing in the rail's web, and one in the outer plate |
| `wheel_BL` | `wheel_shaft_BL` | on the REX shaft, a hub |
| `drive_belt_BL` | `wheel_shaft_BL` | on the 24T pulleys |
| `wheel_shaft_FR` | `rail_R` | in a goBILDA 1611 bearing in the rail's web, and one in the outer plate |
| `wheel_FR` | `wheel_shaft_FR` | on the REX shaft, a hub |
| `drive_belt_FR` | `wheel_shaft_FR` | on the 24T pulleys |
| `wheel_shaft_BR` | `rail_R` | in a goBILDA 1611 bearing in the rail's web, and one in the outer plate |
| `wheel_BR` | `wheel_shaft_BR` | on the REX shaft, a hub |
| `drive_belt_BR` | `wheel_shaft_BR` | on the 24T pulleys |
| `ex_block` | `ex_cross` | clamped on the cross shaft |
| `feeder_wheel_L` | `feeder_shaft_L` | on the shaft (the left's pinned, the right's free on bearings) |
| `feeder_shaft_L` | `feeder_arm_L` | in 1611 bearings in the arm's plates, e-clips |
| `feeder_arm_L` | `feeder_pivot_L` | swings on 1611 bearings on the pivot shaft |
| `feeder_pivot_L` | `feeder_rear_block_L` | in 1611 bearings in the rear and front blocks, e-clips |
| `feeder_pivot_L` | `feeder_front_block_L` | in a 1611 bearing |
| `feeder_wheel_R` | `feeder_shaft_R` | on the shaft (the left's pinned, the right's free on bearings) |
| `feeder_shaft_R` | `feeder_arm_R` | in 1611 bearings in the arm's plates, e-clips |
| `feeder_arm_R` | `feeder_pivot_R` | swings on 1611 bearings on the pivot shaft |
| `feeder_pivot_R` | `feeder_rear_block_R` | in 1611 bearings in the rear and front blocks, e-clips |
| `feeder_pivot_R` | `feeder_front_block_R` | in a 1611 bearing |
| `fly_block_L_front` | `lm_Hole_Lowside_U-Channel_GB_11__1` | slides in the plate's slot, the shaft's bearing in it |
| `fly_yoke_L` | `lm_Hole_Lowside_U-Channel_GB_12__1` | its block slides in the plate's slot, the shaft's bearing in it |
| `fly_spring_L` | `fly_anchor_L` | hooked to the anchor's pin and the yoke's |
| `feeder_spring_L` | `feeder_rear_block_L` | seated in the lug's pocket and the arm's |
| `fly_block_R_front` | `lm_Hole_Lowside_U-Channel_GB_9__1` | slides in the plate's slot, the shaft's bearing in it |
| `fly_yoke_R` | `lm_Hole_Lowside_U-Channel_GB_10__1` | its block slides in the plate's slot, the shaft's bearing in it |
| `fly_spring_R` | `fly_anchor_R` | hooked to the anchor's pin and the yoke's |
| `feeder_spring_R` | `feeder_rear_block_R` | seated in the lug's pocket and the arm's |
| `feeder_belt_A` | `feeder_pivot_L` | on the 24T pulleys |
| `feeder_belt_B` | `feeder_pivot_L` | on the 24T pulleys |
| `ex_gear_stub` | `ex_stub_R` | clamped on the stub, a set screw |
| `roller` | `roller_shaft` | on the REX shaft |
| `roller_belt` | `roller_shaft` | on the 24T pulleys |
| `lane_shaft_0` | `mouth_floor_L` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_shaft_0` | `mouth_floor_R` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_roller_0` | `lane_shaft_0` | free on two 8x14x5 bearings on the shaft |
| `lane_shaft_1` | `mouth_floor_L` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_shaft_1` | `mouth_floor_R` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_roller_1` | `lane_shaft_1` | free on two 8x14x5 bearings on the shaft |
| `lane_shaft_2` | `lane_wall_L_front` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_shaft_2` | `lane_wall_R_front` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_roller_2` | `lane_shaft_2` | free on two 8x14x5 bearings on the shaft |
| `lane_shaft_3` | `lane_wall_L_rear` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_shaft_3` | `lane_wall_R_rear` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_roller_3` | `lane_shaft_3` | free on two 8x14x5 bearings on the shaft |
| `lane_shaft_4` | `lane_wall_L_rear` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_shaft_4` | `lane_wall_R_rear` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_roller_4` | `lane_shaft_4` | free on two 8x14x5 bearings on the shaft |
| `ceiling_roller_0` | `ceiling` | on two 608 bearings and an 8 mm shaft through the ceiling's sides |
| `ceiling_roller_1` | `ceiling` | on two 608 bearings and an 8 mm shaft through the ceiling's sides |
| `ceiling_roller_2` | `ceiling` | on two 608 bearings and an 8 mm shaft through the ceiling's sides |
| `ceiling_roller_3` | `ceiling` | on two 608 bearings and an 8 mm shaft through the ceiling's sides |
| `battery` | `battery_cradle_L` | a strap round it and both halves |
| `star_servo_L` | `front_bracket_L` | M4 x 12 through the servo's tabs into the bracket's inserts (4) |
| `star_servo_R` | `front_bracket_R` | M4 x 12 through the servo's tabs into the bracket's inserts (4) |
| `motor_plate_FL` | `rail_L` | M4 x 10 through the plate and the rail's web, nylocs inside (goBILDA grid) (4) |
| `drive_motor_FL` | `motor_plate_FL` | M4 x 8 into the motor's face (goBILDA pattern) (4) |
| `motor_plate_BL` | `rail_L` | M4 x 10 through the plate and the rail's web, nylocs inside (goBILDA grid) (4) |
| `drive_motor_BL` | `motor_plate_BL` | M4 x 8 into the motor's face (goBILDA pattern) (4) |
| `outer_plate_L` | `standoff_L_1_hi` | M4 x 8 into the standoffs (4) |
| `standoff_L_-3_hi` | `rail_L` | M4 x 10 through the motor plate side's rail web (1) |
| `standoff_L_-3_lo` | `rail_L` | M4 x 10 through the motor plate side's rail web (1) |
| `standoff_L_1_hi` | `rail_L` | M4 x 10 through the motor plate side's rail web (1) |
| `standoff_L_1_lo` | `rail_L` | M4 x 10 through the motor plate side's rail web (1) |
| `motor_plate_FR` | `rail_R` | M4 x 10 through the plate and the rail's web, nylocs inside (goBILDA grid) (4) |
| `drive_motor_FR` | `motor_plate_FR` | M4 x 8 into the motor's face (goBILDA pattern) (4) |
| `motor_plate_BR` | `rail_R` | M4 x 10 through the plate and the rail's web, nylocs inside (goBILDA grid) (4) |
| `drive_motor_BR` | `motor_plate_BR` | M4 x 8 into the motor's face (goBILDA pattern) (4) |
| `outer_plate_R` | `standoff_R_1_hi` | M4 x 8 into the standoffs (4) |
| `standoff_R_-3_hi` | `rail_R` | M4 x 10 through the motor plate side's rail web (1) |
| `standoff_R_-3_lo` | `rail_R` | M4 x 10 through the motor plate side's rail web (1) |
| `standoff_R_1_hi` | `rail_R` | M4 x 10 through the motor plate side's rail web (1) |
| `standoff_R_1_lo` | `rail_R` | M4 x 10 through the motor plate side's rail web (1) |
| `rear_cross` | `rail_L` | goBILDA pattern brackets, M4 x 8 into nylocs (4) |
| `rear_cross` | `rail_R` | goBILDA pattern brackets, M4 x 8 into nylocs (4) |
| `front_cross` | `motor_plate_FL` | goBILDA pattern brackets, M4 x 8 into nylocs (4) |
| `front_cross` | `motor_plate_FR` | goBILDA pattern brackets, M4 x 8 into nylocs (4) |
| `front_pad_L` | `flap_L` | 3 countersunk M3 x 10 into the root's inserts (3) |
| `ramp_skin_L` | `mouth_floor_L` | 4 countersunk M3 x 8 into the ramp's inserts (4) |
| `front_pad_R` | `flap_R` | 3 countersunk M3 x 10 into the root's inserts (3) |
| `ramp_skin_R` | `mouth_floor_R` | 4 countersunk M3 x 8 into the ramp's inserts (4) |
| `fly_yoke_L` | `lm_flywheel_motor_bracket_L_1_8_in_aluminium_L_bent_on_the_laun_1` | M4 x 10 through the bracket's top into the yoke's inserts (2) |
| `fly_yoke_R` | `lm_flywheel_motor_bracket_R_1_8_in_aluminium_L_bent_on_the_laun_1` | M4 x 10 through the bracket's top into the yoke's inserts (2) |
| `feeder_motor` | `feeder_rear_block_L` | M4 x 8 into the motor's face (goBILDA pattern) (4) |
| `turret_drive_plate` | `lm_Hole_Lowside_U-Channel_GB_8__1` | M4 x 10 up through the plate into nylocs in the launcher's front top channel (4) |
| `ex_servo` | `ex_servo_mount` | M4 x 12 through the servo's tabs into the mount's inserts (4) |
| `ex_gear_servo` | `ex_servo` | 4 M3 into the servo's goBILDA horn (4) |
| `intake_motor` | `intake_arm_R` | M4 x 8 into the motor's face (goBILDA pattern) through the arm (4) |
| `control_hub` | `rack_L` | M3 x 8 into its four mounting holes, through the rack's wall (4) |
| `expansion_hub` | `rack_R` | M3 x 8 into its four mounting holes, through the rack's wall (4) |
| `limelight` | `limelight_mast` | M4 into the mast's inserts (its mounting holes) (2) |
