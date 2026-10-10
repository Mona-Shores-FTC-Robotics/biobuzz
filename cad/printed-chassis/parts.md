# Parts (written by build.py)

CHECK marks a size not read from a vendor file. Two robots: double every line.

| Part | Module | Make | What |
|---|---|---|---|
| `battery` | elec | buy | Modern Robotics 12 V NiMH battery (the mentor's), on edge across the robot, low between the rails under the rear cross channel (CHECK its size) |
| `battery_cradle_L` | elec | print | PETG: half the battery's cradle, hung from the rail's bottom flange on two risers (the battery held in by a strap) |
| `battery_cradle_R` | elec | print | PETG: half the battery's cradle, hung from the rail's bottom flange on two risers (the battery held in by a strap) |
| `control_hub` | elec | buy | REV Control Hub, standing on edge, plugs to the back |
| `expansion_hub` | elec | buy | REV Expansion Hub, standing on edge, plugs to the back |
| `limelight` | elec | buy | Limelight 3A, on two M4 into the mast's top |
| `limelight_mast_0` | elec | print | PETG: the Limelight mast's lower half, its foot on two M4 through the left front bracket's top, a tenon into the upper half |
| `limelight_mast_1` | elec | print | PETG: the mast's upper half, on the tenon with one M3 through both; the Limelight on its top, about 14 in up, 45 deg up (its offsets go in CameraMount) |
| `rack_L` | elec | print | PETG: half the electronics rack, on the rear cross channel: a shelf and a back wall the hub screws to; two M4 at its inner end, the shelf resting on the flange the rest of the way |
| `rack_R` | elec | print | PETG: half the electronics rack, on the rear cross channel: a shelf and a back wall the hub screws to; two M4 at its inner end, the shelf resting on the flange the rest of the way |
| `ex_arm_L` | extractor | print | PETG, 6 mm: extractor arm (cad/intake-b's, printed) |
| `ex_arm_R` | extractor | print | PETG, 6 mm: extractor arm (cad/intake-b's, printed) |
| `ex_block` | extractor | print | PETG: the FLOWER block (doc/ramp-hook.md's profile; drawn as its box here) |
| `ex_cross` | extractor | buy | goBILDA 1516-4008-2160 8mm REX standoff (216 mm) |
| `ex_servo` | extractor | buy | goBILDA 2000-0025-0002 Torque servo, on the right flap, 1:1 printed gear pair to the right stub (CHECK the box) |
| `ex_stub_L` | extractor | buy | goBILDA 1516-4008-0960 8mm REX standoff, cut |
| `ex_stub_R` | extractor | buy | goBILDA 1516-4008-0960 8mm REX standoff, cut |
| `rail_L` | frame | buy | goBILDA 1121-0014-0360 low-side U-channel (360 mm), his track |
| `rail_R` | frame | buy | goBILDA 1121-0014-0360 low-side U-channel (360 mm), his track |
| `flap_L` | front_L | print | PETG-CF, 6 mm (material: it carries the extractor's stub, so it must not flex): the fixed flap and the extractor's side plate; bolts to the front pod |
| `flap_face_L` | front_L | print | swappable face plate on three M3 screws into the flap's heat-set inserts: TPU 95A printed on a PETG backer (dual-material) by default; a bare PETG plate or foam glued to a backer for the drop test, all the same outline |
| `flap_R` | front_R | print | PETG-CF, 6 mm (material: it carries the extractor's stub, so it must not flex): the fixed flap and the extractor's side plate; bolts to the front pod |
| `flap_face_R` | front_R | print | swappable face plate on three M3 screws into the flap's heat-set inserts: TPU 95A printed on a PETG backer (dual-material) by default; a bare PETG plate or foam glued to a backer for the drop test, all the same outline |
| `intake_arm_L` | intake | print | PETG: swing arm, pivot to roller bearing (the right one also carries the motor) |
| `intake_arm_R` | intake | print | PETG: swing arm, pivot to roller bearing (the right one also carries the motor) |
| `intake_motor` | intake | buy | goBILDA 5203-2402-0003 Yellow Jacket, 1620 RPM: the roller (about 170 in/s at its surface) and the lane |
| `roller` | intake | buy | goBILDA 3632-4008-0048 48 mm Gecko wheels, as the mentor's roller (about 10 across the 9.8 in span) |
| `roller_belt` | intake | buy | goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0275 belt |
| `roller_shaft` | intake | buy | goBILDA 8mm REX shaft, cut to 371 mm (CHECK) |
| `ceiling` | lane | print | PETG: the sprung ceiling's frame, hinged at its front on the posts' pins, its rear banded down (a piece lifts it), carrying free rollers |
| `ceiling_post_L0` | lane | print | PETG: a ceiling post, screwed to the wall's outer face; the ceiling's hinge pin through its top |
| `ceiling_post_R0` | lane | print | PETG: a ceiling post, screwed to the wall's outer face; the ceiling's hinge pin through its top |
| `ceiling_roller_0` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `ceiling_roller_1` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `ceiling_roller_2` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `ceiling_roller_3` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `front_bracket_L` | lane | print | PETG: on the rail's top flange (his motor fills the U-channel mount, so nothing bolts through that); hangs the star's servo and carries the intake arm's pivot (a shoulder screw) |
| `front_bracket_R` | lane | print | PETG: on the rail's top flange (his motor fills the U-channel mount, so nothing bolts through that); hangs the star's servo and carries the intake arm's pivot (a shoulder screw) |
| `lane_roller_0` | lane | print | TPU 95A: 24 mm x 1 in lane roller |
| `lane_roller_1` | lane | print | TPU 95A: 24 mm x 1 in lane roller |
| `lane_roller_2` | lane | print | TPU 95A: 24 mm x 1 in lane roller |
| `lane_roller_3` | lane | print | TPU 95A: 24 mm x 1 in lane roller |
| `lane_roller_4` | lane | print | TPU 95A: 24 mm x 1 in lane roller |
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
| `star_L` | lane | buy | SWYFT Intake Wheel 3.5 in, 7 mm hex (SR-INTAKEWHEEL-35-7mm, 4 for $24.99): 0.5 in wide, 30A TPE on a nylon core, cut by the team into 16 flaps, as the mentor's; on a 7 mm hex through a ~25 mm black adapter (TBD) |
| `star_R` | lane | buy | SWYFT Intake Wheel 3.5 in, 7 mm hex (SR-INTAKEWHEEL-35-7mm, 4 for $24.99): 0.5 in wide, 30A TPE on a nylon core, cut by the team into 16 flaps, as the mentor's; on a 7 mm hex through a ~25 mm black adapter (TBD) |
| `star_clutch_L` | lane | print | PETG hub, 13.9-13.95 mm press bore for the one-way clutch (the mentor's: Amazon Sankoly-US SK230309GZZC-6P, read as HF081412, 8 x 14 x 12 mm drawn cup; CHECK with calipers; free-wheel direction TBD), its other end driving the star's hex adapter: the star can't be pushed backwards, and a fast piece overruns it |
| `star_clutch_R` | lane | print | PETG hub, 13.9-13.95 mm press bore for the one-way clutch (the mentor's: Amazon Sankoly-US SK230309GZZC-6P, read as HF081412, 8 x 14 x 12 mm drawn cup; CHECK with calipers; free-wheel direction TBD), its other end driving the star's hex adapter: the star can't be pushed backwards, and a fast piece overruns it |
| `star_servo_L` | lane | buy | continuous servo over the star, its spline on the star's axis (TBD: the mentor's choice and its coupling; a goBILDA Speed servo drawn) |
| `star_servo_R` | lane | buy | continuous servo over the star, its spline on the star's axis (TBD: the mentor's choice and its coupling; a goBILDA Speed servo drawn) |
| `star_shaft_L` | lane | buy | 8 mm ROUND hardened shaft (ground steel, h6), vertical, from the servo: the clutch's rollers run on it, so not 8mm REX (its flats would let them slip) |
| `star_shaft_R` | lane | buy | 8 mm ROUND hardened shaft (ground steel, h6), vertical, from the servo: the clutch's rollers run on it, so not 8mm REX (its flats would let them slip) |
| `wall_standoff_L_front0` | lane | buy | goBILDA 1501-0006-0800 M4 standoff, 80 mm (CHECK the length is stocked) |
| `wall_standoff_L_front1` | lane | buy | goBILDA 1501-0006-0800 M4 standoff, 80 mm (CHECK the length is stocked) |
| `wall_standoff_L_rear0` | lane | buy | goBILDA 1501-0006-0800 M4 standoff, 80 mm (CHECK the length is stocked) |
| `wall_standoff_L_rear1` | lane | buy | goBILDA 1501-0006-0800 M4 standoff, 80 mm (CHECK the length is stocked) |
| `wall_standoff_R_front0` | lane | buy | goBILDA 1501-0006-0800 M4 standoff, 80 mm (CHECK the length is stocked) |
| `wall_standoff_R_front1` | lane | buy | goBILDA 1501-0006-0800 M4 standoff, 80 mm (CHECK the length is stocked) |
| `wall_standoff_R_rear0` | lane | buy | goBILDA 1501-0006-0800 M4 standoff, 80 mm (CHECK the length is stocked) |
| `wall_standoff_R_rear1` | lane | buy | goBILDA 1501-0006-0800 M4 standoff, 80 mm (CHECK the length is stocked) |
| `hood` | launcher | print | PETG: the hood that turns the shot out (drawn as a tube and a lid) |
| `launcher_foot_L0` | launcher | print | PETG: a launcher foot, between the rail's top flange and the launcher's side channel: an M4 down through a pocket into a nut under the rail's flange (fitted before the launcher goes on), and one down from inside the launcher's channel into an insert (the launcher lifts off its feet) |
| `launcher_foot_L1` | launcher | print | PETG: a launcher foot, between the rail's top flange and the launcher's side channel: an M4 down through a pocket into a nut under the rail's flange (fitted before the launcher goes on), and one down from inside the launcher's channel into an insert (the launcher lifts off its feet) |
| `launcher_foot_R0` | launcher | print | PETG: a launcher foot, between the rail's top flange and the launcher's side channel: an M4 down through a pocket into a nut under the rail's flange (fitted before the launcher goes on), and one down from inside the launcher's channel into an insert (the launcher lifts off its feet) |
| `launcher_foot_R1` | launcher | print | PETG: a launcher foot, between the rail's top flange and the launcher's side channel: an M4 down through a pocket into a nut under the rail's flange (fitted before the launcher goes on), and one down from inside the launcher's channel into an insert (the launcher lifts off its feet) |
| `odo_adapter_F` | odometry | print | PETG: the odometry pod's adapter, inside the rail's web (the pod's offsets are measured on the robot, for the Pinpoint) |
| `odo_adapter_S` | odometry | print | PETG: the odometry pod's adapter, inside the rail's web (the pod's offsets are measured on the robot, for the Pinpoint) |
| `odo_pod_forward` | odometry | buy | goBILDA 3110-0001-0002 4-bar odometry pod (96 mm drive wheel version), its wheel rolling along X, on a printed adapter from the right rail |
| `odo_pod_strafe` | odometry | buy | goBILDA 3110-0001-0002 4-bar odometry pod, its wheel rolling along Y, on a printed adapter from the right rail |
| `lm_*` | launcher | buy | the mentor's launcher module (cad/modules/launcher-module), placed by the launch column |
| `dm_*` | corner_* | buy | the mentor's four drive corners (cad/modules/drive-module), each by its axle; the right ones mirrored from his left |

## Screws (v2, drawn and checked by build.py)

| Screw | One robot | Two robots |
|---|---|---|
| M3 x 8 socket head (countersunk for the faces) | 6 | 12 |
| M3 x 12 socket head | 4 | 8 |
| M3 x 16 socket head | 1 | 2 |
| M4 x 10 socket head | 16 | 32 |
| M4 x 12 socket head | 16 | 32 |
| M4 x 16 socket head | 12 | 24 |
| M4 x 20 socket head | 6 | 12 |
| M4 x 25 socket head | 4 | 8 |
| M5 x 18 socket head | 2 | 4 |
| M3 heat-set insert | 10 | 20 |
| M4 heat-set insert | 12 | 24 |
| M5 heat-set insert | 2 | 4 |
| M3 nyloc nut | 1 | 2 |
| M4 nyloc nut | 18 | 36 |
| on the devices (servos, hubs, intake motor, Limelight): see below | 26 | 52 |

Buy 20% over for each line: dropped screws and stripped inserts.

## Every joint

| Joint | Holds | Screw | Through | Into |
|---|---|---|---|---|
| flap_L_6.68 | the flap's root to the rail's web | M4 x 12 | `rail_L` | an insert in `flap_L` |
| flap_L_6.99 | the flap's root to the rail's web | M4 x 12 | `rail_L` | an insert in `flap_L` |
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
| mouth_L_3.21 | the mouth to the rail's bottom flange | M4 x 16 | `mouth_floor_L`, `rail_L` | a nyloc |
| mouth_L_5.10 | the mouth to the rail's bottom flange | M4 x 16 | `mouth_floor_L`, `rail_L` | a nyloc |
| bracket_L_2.90 | the front bracket's foot to the rail's top flange | M4 x 16 | `front_bracket_L`, `rail_L` | a nyloc |
| bracket_L_3.53 | the front bracket's foot to the rail's top flange | M4 x 16 | `front_bracket_L`, `rail_L` | a nyloc |
| arm_pivot_L | the intake arm's pivot: goBILDA-style 6 mm shoulder screw, 8 mm shoulder, M5 thread (CHECK the SKU) | M5 x 18 | `intake_arm_L` | an insert in `front_bracket_L` |
| post_L0_1.4 | a ceiling post to the wall (counterbored in the wall) | M3 x 12 | `lane_wall_L_front` | an insert in `ceiling_post_L0` |
| post_L0_2.2 | a ceiling post to the wall (counterbored in the wall) | M3 x 12 | `lane_wall_L_front` | an insert in `ceiling_post_L0` |
| cradle_L_-6.55 | the battery cradle's riser to the rail's bottom flange | M4 x 12 | `rail_L` | an insert in `battery_cradle_L` |
| cradle_L_-5.92 | the battery cradle's riser to the rail's bottom flange | M4 x 12 | `rail_L` | an insert in `battery_cradle_L` |
| rack_L_0.31496062992125984 | the electronics rack to the rear cross channel's top flange | M4 x 16 | `rack_L`, `dm_BR_1107-0013-0336_1` | a nyloc |
| rack_L_1.2598425196850394 | the electronics rack to the rear cross channel's top flange | M4 x 16 | `rack_L`, `dm_BR_1107-0013-0336_1` | a nyloc |
| foot_rail_L0 | a launcher foot to the rail's top flange | M4 x 20 | `launcher_foot_L0`, `rail_L` | a nyloc |
| foot_lm_L0 | the launcher's side channel to its foot (CHECK: his channel's flange holes don't read cleanly from his model: mark the foot's insert from the real channel) | M4 x 12 | `lm_Hole_Lowside_U-Channel_GB_6__1` | an insert in `launcher_foot_L0` |
| foot_rail_L1 | a launcher foot to the rail's top flange | M4 x 20 | `launcher_foot_L1`, `rail_L` | a nyloc |
| foot_lm_L1 | the launcher's side channel to its foot (CHECK: his channel's flange holes don't read cleanly from his model: mark the foot's insert from the real channel) | M4 x 12 | `lm_Hole_Lowside_U-Channel_GB_6__1` | an insert in `launcher_foot_L1` |
| flap_R_6.68 | the flap's root to the rail's web | M4 x 12 | `rail_R` | an insert in `flap_R` |
| flap_R_6.99 | the flap's root to the rail's web | M4 x 12 | `rail_R` | an insert in `flap_R` |
| face_R0 | the swappable face to its flap (countersunk, flush on the face) | M3 x 8 | `flap_face_R` | an insert in `flap_R` |
| face_R1 | the swappable face to its flap (countersunk, flush on the face) | M3 x 8 | `flap_face_R` | an insert in `flap_R` |
| face_R2 | the swappable face to its flap (countersunk, flush on the face) | M3 x 8 | `flap_face_R` | an insert in `flap_R` |
| odo_F13 | the odometry pod, through its adapter and the rail's web (from outside) | M4 x 25 | `rail_R`, `odo_adapter_F` | `odo_pod_forward`'s thread |
| odo_F14 | the odometry pod, through its adapter and the rail's web (from outside) | M4 x 25 | `rail_R`, `odo_adapter_F` | `odo_pod_forward`'s thread |
| odo_S21 | the odometry pod, through its adapter and the rail's web (from outside) | M4 x 25 | `rail_R`, `odo_adapter_S` | `odo_pod_strafe`'s thread |
| odo_S23 | the odometry pod, through its adapter and the rail's web (from outside) | M4 x 25 | `rail_R`, `odo_adapter_S` | `odo_pod_strafe`'s thread |
| wall_R_rear0 | a lane wall to its standoff (counterbored: the head below the lane's face) | M4 x 10 | `lane_wall_R_rear` | `wall_standoff_R_rear0`'s thread |
| wall_rail_R_rear0 | the standoff to the rail's web (from outside) | M4 x 10 | `rail_R` | `wall_standoff_R_rear0`'s thread |
| wall_R_rear1 | a lane wall to its standoff (counterbored: the head below the lane's face) | M4 x 10 | `lane_wall_R_rear` | `wall_standoff_R_rear1`'s thread |
| wall_rail_R_rear1 | the standoff to the rail's web (from outside) | M4 x 10 | `rail_R` | `wall_standoff_R_rear1`'s thread |
| wall_R_front0 | a lane wall to its standoff (counterbored: the head below the lane's face) | M4 x 10 | `lane_wall_R_front` | `wall_standoff_R_front0`'s thread |
| wall_rail_R_front0 | the standoff to the rail's web (from outside) | M4 x 10 | `rail_R` | `wall_standoff_R_front0`'s thread |
| wall_R_front1 | a lane wall to its standoff (counterbored: the head below the lane's face) | M4 x 10 | `lane_wall_R_front` | `wall_standoff_R_front1`'s thread |
| wall_rail_R_front1 | the standoff to the rail's web (from outside) | M4 x 10 | `rail_R` | `wall_standoff_R_front1`'s thread |
| mouth_R_3.21 | the mouth to the rail's bottom flange | M4 x 16 | `mouth_floor_R`, `rail_R` | a nyloc |
| mouth_R_5.10 | the mouth to the rail's bottom flange | M4 x 16 | `mouth_floor_R`, `rail_R` | a nyloc |
| bracket_R_2.90 | the front bracket's foot to the rail's top flange | M4 x 16 | `front_bracket_R`, `rail_R` | a nyloc |
| bracket_R_3.53 | the front bracket's foot to the rail's top flange | M4 x 16 | `front_bracket_R`, `rail_R` | a nyloc |
| arm_pivot_R | the intake arm's pivot: goBILDA-style 6 mm shoulder screw, 8 mm shoulder, M5 thread (CHECK the SKU) | M5 x 18 | `intake_arm_R` | an insert in `front_bracket_R` |
| post_R0_1.4 | a ceiling post to the wall (counterbored in the wall) | M3 x 12 | `lane_wall_R_front` | an insert in `ceiling_post_R0` |
| post_R0_2.2 | a ceiling post to the wall (counterbored in the wall) | M3 x 12 | `lane_wall_R_front` | an insert in `ceiling_post_R0` |
| cradle_R_-6.55 | the battery cradle's riser to the rail's bottom flange | M4 x 12 | `rail_R` | an insert in `battery_cradle_R` |
| cradle_R_-5.92 | the battery cradle's riser to the rail's bottom flange | M4 x 12 | `rail_R` | an insert in `battery_cradle_R` |
| rack_R_0.31496062992125984 | the electronics rack to the rear cross channel's top flange | M4 x 16 | `rack_R`, `dm_BR_1107-0013-0336_1` | a nyloc |
| rack_R_1.2598425196850394 | the electronics rack to the rear cross channel's top flange | M4 x 16 | `rack_R`, `dm_BR_1107-0013-0336_1` | a nyloc |
| foot_rail_R0 | a launcher foot to the rail's top flange | M4 x 20 | `launcher_foot_R0`, `rail_R` | a nyloc |
| foot_lm_R0 | the launcher's side channel to its foot (CHECK: his channel's flange holes don't read cleanly from his model: mark the foot's insert from the real channel) | M4 x 12 | `lm_Hole_Lowside_U-Channel_GB_4__1` | an insert in `launcher_foot_R0` |
| foot_rail_R1 | a launcher foot to the rail's top flange | M4 x 20 | `launcher_foot_R1`, `rail_R` | a nyloc |
| foot_lm_R1 | the launcher's side channel to its foot (CHECK: his channel's flange holes don't read cleanly from his model: mark the foot's insert from the real channel) | M4 x 12 | `lm_Hole_Lowside_U-Channel_GB_4__1` | an insert in `launcher_foot_R1` |
| mast_4.25 | the mast's foot to the bracket's top | M4 x 20 | `limelight_mast_0`, `front_bracket_L` | a nyloc |
| mast_4.95 | the mast's foot to the bracket's top | M4 x 20 | `limelight_mast_0`, `front_bracket_L` | a nyloc |
| mast_splice | the mast's splice | M3 x 16 | `limelight_mast_1`, `limelight_mast_0` | a nyloc |
| hood_0 | the hood's flange to the turret's top (CHECK: through the 176T gear's holes into the kit's inner race, as longer copies of the kit's own screws: read the pattern off the kit) | M4 x 12 | `hood` | `lm_2325-0105-0176_1`'s thread |
| hood_1 | the hood's flange to the turret's top (CHECK: through the 176T gear's holes into the kit's inner race, as longer copies of the kit's own screws: read the pattern off the kit) | M4 x 12 | `hood` | `lm_2325-0105-0176_1`'s thread |
| hood_2 | the hood's flange to the turret's top (CHECK: through the 176T gear's holes into the kit's inner race, as longer copies of the kit's own screws: read the pattern off the kit) | M4 x 12 | `hood` | `lm_2325-0105-0176_1`'s thread |
| hood_3 | the hood's flange to the turret's top (CHECK: through the 176T gear's holes into the kit's inner race, as longer copies of the kit's own screws: read the pattern off the kit) | M4 x 12 | `hood` | `lm_2325-0105-0176_1`'s thread |

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
| `ex_block` | `ex_cross` | clamped on the cross shaft |
| `roller` | `roller_shaft` | on the REX shaft |
| `roller_belt` | `roller_shaft` | on the 24T pulleys |
| `lane_shaft_0` | `mouth_floor_L` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_shaft_0` | `mouth_floor_R` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_roller_0` | `lane_shaft_0` | on the REX shaft |
| `lane_shaft_1` | `mouth_floor_L` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_shaft_1` | `mouth_floor_R` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_roller_1` | `lane_shaft_1` | on the REX shaft |
| `lane_shaft_2` | `lane_wall_L_front` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_shaft_2` | `lane_wall_R_front` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_roller_2` | `lane_shaft_2` | on the REX shaft |
| `lane_shaft_3` | `lane_wall_L_rear` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_shaft_3` | `lane_wall_R_rear` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_roller_3` | `lane_shaft_3` | on the REX shaft |
| `lane_shaft_4` | `lane_wall_L_rear` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_shaft_4` | `lane_wall_R_rear` | in a goBILDA 1611 flanged bearing, e-clips |
| `lane_roller_4` | `lane_shaft_4` | on the REX shaft |
| `ceiling_roller_0` | `ceiling` | on two 608 bearings and an 8 mm shaft through the ceiling's sides |
| `ceiling_roller_1` | `ceiling` | on two 608 bearings and an 8 mm shaft through the ceiling's sides |
| `ceiling_roller_2` | `ceiling` | on two 608 bearings and an 8 mm shaft through the ceiling's sides |
| `ceiling_roller_3` | `ceiling` | on two 608 bearings and an 8 mm shaft through the ceiling's sides |
| `battery` | `battery_cradle_L` | a strap round it and both halves |
| `star_servo_L` | `front_bracket_L` | M4 x 12 through the servo's tabs into the bracket's inserts (4) |
| `star_servo_R` | `front_bracket_R` | M4 x 12 through the servo's tabs into the bracket's inserts (4) |
| `ex_servo` | `flap_R` | M4 x 12 through the servo's tabs into the flap's inserts (4) |
| `intake_motor` | `intake_arm_R` | M4 x 8 into the motor's face (goBILDA pattern) through the arm (4) |
| `control_hub` | `rack_L` | M3 x 8 into its four mounting holes, through the rack's wall (4) |
| `expansion_hub` | `rack_R` | M3 x 8 into its four mounting holes, through the rack's wall (4) |
| `limelight` | `limelight_mast_1` | M4 into the mast's inserts (its mounting holes) (2) |
