# Parts, layout v1 (written by build.py)

Screws, nuts and inserts come with v2. CHECK marks a size not read from a vendor file.

| Part | Module | Make | What |
|---|---|---|---|
| `battery` | elec | buy | 12 V battery, on edge across the robot, low between the rails under the rear cross channel (CHECK its size: the team's battery) |
| `battery_cradle_L` | elec | print | PETG: half the battery's cradle, hung from the rails' bottom flanges |
| `battery_cradle_R` | elec | print | PETG: half the battery's cradle, hung from the rails' bottom flanges |
| `control_hub` | elec | buy | REV Control Hub, standing on edge, plugs to the back |
| `expansion_hub` | elec | buy | REV Expansion Hub, standing on edge, plugs to the back |
| `limelight` | elec | buy | Limelight 3A |
| `limelight_mast_0` | elec | print | PETG: Limelight mast on the left front bracket, in two pieces (bed size); lens about 14 in up, 45 deg up (its offsets go in CameraMount) |
| `limelight_mast_1` | elec | print | PETG: Limelight mast on the left front bracket, in two pieces (bed size); lens about 14 in up, 45 deg up (its offsets go in CameraMount) |
| `rack_L` | elec | print | PETG: half the electronics rack, on the rear cross channel: a shelf and a back wall the hub screws to |
| `rack_R` | elec | print | PETG: half the electronics rack, on the rear cross channel: a shelf and a back wall the hub screws to |
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
| `ceiling` | lane | print | PETG: the sprung ceiling's frame, on pins in slotted posts (as cad/transfer), carrying free rollers |
| `ceiling_roller_0` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `ceiling_roller_1` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `ceiling_roller_2` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `ceiling_roller_3` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `front_bracket_L` | lane | print | PETG: bolts to the front drive motor's U-channel mount; hangs the star's servo and carries the intake arm's pivot (an M5 shoulder screw) |
| `front_bracket_R` | lane | print | PETG: bolts to the front drive motor's U-channel mount; hangs the star's servo and carries the intake arm's pivot (an M5 shoulder screw) |
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
| `lane_wall_L_front` | lane | print | PETG, 1/4 in: half a lane wall with its shafts' bearings; on two REX standoffs to the rail |
| `lane_wall_L_rear` | lane | print | PETG, 1/4 in: half a lane wall with its shafts' bearings; on two REX standoffs to the rail |
| `lane_wall_R_front` | lane | print | PETG, 1/4 in: half a lane wall with its shafts' bearings; on two REX standoffs to the rail |
| `lane_wall_R_rear` | lane | print | PETG, 1/4 in: half a lane wall with its shafts' bearings; on two REX standoffs to the rail |
| `mouth_floor_L` | lane | print | PETG: half the floor under the star wheels, at the lane's height; carries lane shafts 0 and 1's bearings on hangers |
| `mouth_floor_R` | lane | print | PETG: half the floor under the star wheels, at the lane's height; carries lane shafts 0 and 1's bearings on hangers |
| `ramp_L` | lane | print | PETG: half the ramp, the mouth's whole width |
| `ramp_R` | lane | print | PETG: half the ramp, the mouth's whole width |
| `star_L` | lane | buy | 3.5 in OD flexible star wheel, 7 mm hex bore, as the mentor's (vendor TBD); spins pieces in, toward the lane |
| `star_R` | lane | buy | 3.5 in OD flexible star wheel, 7 mm hex bore, as the mentor's (vendor TBD); spins pieces in, toward the lane |
| `star_clutch_L` | lane | buy | one-way bearing / clutch, as the mentor's (part TBD): the star can't be pushed backwards, and a fast piece overruns it |
| `star_clutch_R` | lane | buy | one-way bearing / clutch, as the mentor's (part TBD): the star can't be pushed backwards, and a fast piece overruns it |
| `star_servo_L` | lane | buy | continuous servo over the star, its spline on the star's axis (TBD: the mentor's choice; a goBILDA Speed servo drawn), clear of the feeder motor behind it |
| `star_servo_R` | lane | buy | continuous servo over the star, its spline on the star's axis (TBD: the mentor's choice; a goBILDA Speed servo drawn), clear of the feeder motor behind it |
| `star_shaft_L` | lane | buy | 7 mm hex shaft, vertical (TBD, with the mentor's parts) |
| `star_shaft_R` | lane | buy | 7 mm hex shaft, vertical (TBD, with the mentor's parts) |
| `hood` | launcher | print | PETG: the hood that turns the shot out (drawn as a tube and a lid) |
| `odo_adapter_F` | odometry | print | PETG: the odometry pod's adapter, inside the rail's web (the pod's offsets are measured on the robot, for the Pinpoint) |
| `odo_adapter_S` | odometry | print | PETG: the odometry pod's adapter, inside the rail's web (the pod's offsets are measured on the robot, for the Pinpoint) |
| `odo_pod_forward` | odometry | buy | goBILDA 3110-0001-0002 4-bar odometry pod (96 mm drive wheel version), its wheel rolling along X, on a printed adapter from the right rail |
| `odo_pod_strafe` | odometry | buy | goBILDA 3110-0001-0002 4-bar odometry pod, its wheel rolling along Y, on a printed adapter from the right rail |
| `lm_*` | launcher | buy | the mentor's launcher module (cad/modules/launcher-module), placed by the launch column |
| `dm_*` | corner_* | buy | the mentor's four drive corners (cad/modules/drive-module), each by its axle; the right ones mirrored from his left |
