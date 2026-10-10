# Parts, layout v1 (written by build.py)

Screws, nuts and inserts come with v2. CHECK marks a size not read from a vendor file.

| Part | Module | Make | What |
|---|---|---|---|
| `battery` | elec | buy | 12 V battery, as today's (CHECK) |
| `control_hub` | elec | buy | REV Control Hub |
| `expansion_hub` | elec | buy | REV Expansion Hub |
| `limelight` | elec | buy | Limelight 3A |
| `limelight_mast` | elec | print | PETG: Limelight mast; lens about 14 in up, 45 deg up, as today |
| `tray` | elec | print | PETG: electronics tray on the front cross channel and the launcher's top plate |
| `ex_arm_L` | extractor | print | PETG, 6 mm: extractor arm (cad/intake-b's, printed) |
| `ex_arm_R` | extractor | print | PETG, 6 mm: extractor arm (cad/intake-b's, printed) |
| `ex_block` | extractor | print | PETG: the FLOWER block (doc/ramp-hook.md's profile; drawn as its box here) |
| `ex_cross` | extractor | buy | goBILDA 1516-4008-2160 8mm REX standoff (216 mm) |
| `ex_servo` | extractor | buy | goBILDA 2000-0025-0002 Torque servo, on the right flap, 1:1 printed gear pair to the right stub (CHECK the box) |
| `ex_stub_L` | extractor | buy | goBILDA 1516-4008-0960 8mm REX standoff, cut |
| `ex_stub_R` | extractor | buy | goBILDA 1516-4008-0960 8mm REX standoff, cut |
| `front_cross` | frame | buy | goBILDA 1120-0008-0216 U-channel, 8 hole (216 mm), over the lane and the front drive motors |
| `front_upright_L` | frame | print | PETG: stands on the rail's top flange, carries the front cross channel |
| `front_upright_R` | frame | print | PETG: stands on the rail's top flange, carries the front cross channel |
| `rail_L` | frame | buy | goBILDA 1121-0013-0336 low-side U-channel, 13 hole (336 mm), as today's rails |
| `rail_R` | frame | buy | goBILDA 1121-0013-0336 low-side U-channel, 13 hole (336 mm), as today's rails |
| `rear_cross` | frame | buy | goBILDA 1120-0009-0240 U-channel, 9 hole (240 mm), between the rails' webs on goBILDA pattern brackets (CHECK the bracket) |
| `flap_L` | front_L | print | PETG, 6 mm: the fixed flap and the extractor's side plate; bolts to the front pod |
| `flap_foam_L` | front_L | buy | 1/2 in EVA or polyethylene foam on a clip-on backer (the drop test picks it) |
| `flap_R` | front_R | print | PETG, 6 mm: the fixed flap and the extractor's side plate; bolts to the front pod |
| `flap_foam_R` | front_R | buy | 1/2 in EVA or polyethylene foam on a clip-on backer (the drop test picks it) |
| `intake_arm_L` | intake | print | PETG: swing arm, pivot to roller bearing (the right one also carries the motor) |
| `intake_arm_R` | intake | print | PETG: swing arm, pivot to roller bearing (the right one also carries the motor) |
| `intake_motor` | intake | buy | goBILDA 5203-2402-0003 Yellow Jacket, 1620 RPM: the roller (about 170 in/s at its surface) and the lane |
| `intake_pivot_L` | intake | print | PETG: pivot bracket on the rail's web, outside the arm; an M5 shoulder screw is the pivot |
| `intake_pivot_R` | intake | print | PETG: pivot bracket on the rail's web, outside the arm; an M5 shoulder screw is the pivot |
| `roller` | intake | buy | WCP-0353 x6 / WCP-0354 x6 2 in vector wheels and a 48 mm gecko, as cad/intake-b (or printed TPU, if a test says so) |
| `roller_belt` | intake | buy | goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0410 belt |
| `roller_shaft` | intake | buy | goBILDA 8mm REX shaft, cut to 371 mm (CHECK) |
| `backstop` | lane | print | PETG: backstop, on +-0.2 in slots (set with real balls: the 4-piece count) |
| `ceiling` | lane | print | PETG: the sprung ceiling's frame, on pins in slotted posts (as cad/transfer), carrying free rollers |
| `ceiling_roller_0` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `ceiling_roller_1` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `ceiling_roller_2` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `ceiling_roller_3` | lane | print | PETG: a free-spinning ceiling roller on two 608 bearings (the ball moves at the lane's full tread speed, not half) |
| `floor` | lane | print | PETG: the floor under the feeder and pad |
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
| `lane_wall_L` | lane | print | PETG, 1/4 in: lane wall with the shafts' bearings; on two REX standoffs to the rail |
| `lane_wall_R` | lane | print | PETG, 1/4 in: lane wall with the shafts' bearings; on two REX standoffs to the rail |
| `ramp` | lane | print | PETG: the ramp, in slots in the walls |
| `feeder` | launcher | buy | goBILDA 3632-0014-0072 72 mm Gecko x2, softest |
| `feeder_belt` | launcher | buy | goBILDA 3417-4008-0024 24T HTD5 x2, 3412-0009-0225 belt |
| `feeder_motor` | launcher | buy | goBILDA 5203-2402-0003 Yellow Jacket, 1620 RPM, belted 1:1 (stopping it is the gate) |
| `feeder_shaft` | launcher | buy | goBILDA 8mm REX shaft, 120 mm |
| `fly_belt_L` | launcher | buy | goBILDA 3417-4008-0024 24T HTD5 x2, 3412-0009-0315 belt (1:1; the launcher rig sets the ratio) |
| `fly_belt_R` | launcher | buy | goBILDA 3417-4008-0024 24T HTD5 x2, 3412-0009-0315 belt (1:1; the launcher rig sets the ratio) |
| `fly_cassette_L` | launcher | print | PETG: flywheel cassette, its two bearing plates (one per side; comes out as a unit with its wheel and motor) |
| `fly_cassette_R` | launcher | print | PETG: flywheel cassette, its two bearing plates (one per side; comes out as a unit with its wheel and motor) |
| `fly_motor_L` | launcher | buy | goBILDA 5203-2402-0001 Yellow Jacket, 6000 RPM |
| `fly_motor_R` | launcher | buy | goBILDA 5203-2402-0001 Yellow Jacket, 6000 RPM |
| `fly_shaft_L` | launcher | buy | goBILDA 8mm REX shaft, 120 mm |
| `fly_shaft_R` | launcher | buy | goBILDA 8mm REX shaft, 120 mm |
| `flywheel_L0` | launcher | buy | 96 mm flywheel, as the mentor's launcher (CHECK the part) |
| `flywheel_L1` | launcher | buy | 96 mm flywheel, as the mentor's launcher (CHECK the part) |
| `flywheel_R0` | launcher | buy | 96 mm flywheel, as the mentor's launcher (CHECK the part) |
| `flywheel_R1` | launcher | buy | 96 mm flywheel, as the mentor's launcher (CHECK the part) |
| `hood` | launcher | print | PETG: the hood that turns the shot out (drawn as a tube and a lid) |
| `pad` | launcher | print | PETG plate with 1/2 in foam, hinged at its foot (cad/transfer's pad) |
| `ring_roller_0` | launcher | buy | 625 or 608 bearing in a printed V-groove tyre |
| `ring_roller_1` | launcher | buy | 625 or 608 bearing in a printed V-groove tyre |
| `ring_roller_2` | launcher | buy | 625 or 608 bearing in a printed V-groove tyre |
| `ring_roller_3` | launcher | buy | 625 or 608 bearing in a printed V-groove tyre |
| `top_plate` | launcher | print | PETG, 6 mm... drawn 0.05 thick here: the launcher's top plate on the cassettes, carries the ring's rollers, the servo and the encoders |
| `turret_encoder_0` | launcher | buy | REV-11-1271 Thru-Bore encoder on a printed pinion: the two-encoder decode, ratios chosen in v2 |
| `turret_encoder_1` | launcher | buy | REV-11-1271 Thru-Bore encoder on a printed pinion: the two-encoder decode, ratios chosen in v2 |
| `turret_ring` | launcher | print | PETG: turret ring with its gear cut in its rim (drawn plain); rides on four V-groove rollers |
| `turret_servo` | launcher | buy | goBILDA 2000-0025-0003 Speed servo, continuous: drives the ring by a printed pinion |
| `drive_belt_BL` | pod_BL | buy | goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0225 belt |
| `drive_motor_BL` | pod_BL | buy | goBILDA 5203 Yellow Jacket, as today's drive (CHECK the ratio) |
| `pod_BL` | pod_BL | print | PETG: inner plate, outer plate and bridge in one; wheel bearings in both plates, motor on the inner |
| `wheel_BL` | pod_BL | buy | goBILDA 3213-3606-0002 96 mm mecanum (set of 4) |
| `wheel_shaft_BL` | pod_BL | buy | goBILDA 2106-4008-0640 8mm REX shaft, 64 mm (CHECK the length) |
| `drive_belt_BR` | pod_BR | buy | goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0225 belt |
| `drive_motor_BR` | pod_BR | buy | goBILDA 5203 Yellow Jacket, as today's drive (CHECK the ratio) |
| `pod_BR` | pod_BR | print | PETG: inner plate, outer plate and bridge in one; wheel bearings in both plates, motor on the inner |
| `wheel_BR` | pod_BR | buy | goBILDA 3213-3606-0002 96 mm mecanum (set of 4) |
| `wheel_shaft_BR` | pod_BR | buy | goBILDA 2106-4008-0640 8mm REX shaft, 64 mm (CHECK the length) |
| `drive_belt_FL` | pod_FL | buy | goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0340 belt |
| `drive_motor_FL` | pod_FL | buy | goBILDA 5203 Yellow Jacket, as today's drive (CHECK the ratio) |
| `pod_FL` | pod_FL | print | PETG: inner plate, outer plate and bridge in one; wheel bearings in both plates, motor on the inner |
| `wheel_FL` | pod_FL | buy | goBILDA 3213-3606-0002 96 mm mecanum (set of 4) |
| `wheel_shaft_FL` | pod_FL | buy | goBILDA 2106-4008-0640 8mm REX shaft, 64 mm (CHECK the length) |
| `drive_belt_FR` | pod_FR | buy | goBILDA 3417-4008-0024 24T HTD5 pulleys x2, 3412-0009-0340 belt |
| `drive_motor_FR` | pod_FR | buy | goBILDA 5203 Yellow Jacket, as today's drive (CHECK the ratio) |
| `pod_FR` | pod_FR | print | PETG: inner plate, outer plate and bridge in one; wheel bearings in both plates, motor on the inner |
| `wheel_FR` | pod_FR | buy | goBILDA 3213-3606-0002 96 mm mecanum (set of 4) |
| `wheel_shaft_FR` | pod_FR | buy | goBILDA 2106-4008-0640 8mm REX shaft, 64 mm (CHECK the length) |
