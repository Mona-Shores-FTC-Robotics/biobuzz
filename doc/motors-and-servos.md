# Motors and servos: the decisions any robot design needs

Written 8 Oct 2026 for the design meeting. It holds whichever robot the team builds, because the problem comes from the
game and the motor limit, not from one design.

## The budget

FTC allows eight motors (check the current manual for the servo limit). The drive takes four. A launcher robot then
wants five more jobs: intake, flywheel L, flywheel R, a feeder that pushes each piece into the flywheels, and a turret.
That's one job too many, so one of them has to share a motor or move to a servo.

## Decided at the 8 Oct meeting

- **Turret: a servo**, using the two-absolute-encoder gear trick (below) so it always knows its angle. Plan D. The
  flywheels stay off the turret (9 Oct), so nothing electrical rides on it.
- **Two launcher motors**, one per flywheel.
- **The feeder is independent of the launcher**, so the flywheels can spin up without firing. It gets the motor the
  turret freed. Stopping the feeder is the gate, so no gate servo.
- **The transfer is gravity-fed or tied to the intake** (still being designed). There's no motor left for it, so
  "tied to the intake" is the fallback.

| Motors (8) | Servos |
|---|---|
| drive x4 | turret (continuous, with two REV Thru-Bore encoders on an OctoQuad) |
| flywheel L, flywheel R | FLOWER extractor |
| feeder | |
| intake (+ transfer if tied to it) | |

The feeder needs no encoder, so its motor port's encoder input is free (a lane-full sensor, if the intake current
spike isn't enough).

## The turret's encoders: why two, not one at 1:1

Checked against goBILDA's catalogue on 9 Oct. The turret kit (3208-0004-0001, 2.75:1, 105 mm bore) is a 176-tooth
mod 0.8 ring driven by its 64T hub-mount gear, and its page has no encoder provision. A single absolute encoder at
exactly 1:1 (issue #170, rule 2) would need a 176T gear meshing the ring, and goBILDA's largest mod 0.8 gear is 108T.
The 105 mm centre is the ball path, so nothing can sit on the axis either. The two-encoder trick works with stock
parts: an encoder on the kit's 64T drive gear's shaft (2.75 turns per turret turn) and one on a 2303-4008-0036 36T pinion
that meshes that 64T (176/36 = 4.89 turns). The pair repeats every lcm(64, 36)/176 = 3.27 turret turns, so the turret
knows its angle anywhere in a 1178 deg window (the routes wind up to about 1080 deg). The 48T first drawn gave only
393 deg; a 30T would give 1964 deg but only about +-2 deg of decode tolerance, where the 36T keeps about +-4 deg.
Two REV Thru-Bore encoders on the OctoQuad (#169) read them. Both sit on the turret's drive plate, in front of the
ring, clear of anything that turns with the turret (cad/transfer). The flywheels stay off the turret (8 Oct), so nothing
electrical rides on it and no slip ring is needed unless a hood servo is added. goBILDA's own drive table for this kit
lists a servo option (1x Axon MINI: 40.4 RPM at the turret, 242 deg/s), which matches the speed assumed above.

## The plans

| Plan | The four non-drive motors | Servos | Trade-off |
|---|---|---|---|
| **A.** Feeder belted to the left flywheel (Option B as drawn) | intake + lane, flywheel L (+ feeder), flywheel R, turret | extractor, gate | Each shot takes extra energy from the left flywheel only. It dips more than the right: side-spin, slower recovery. |
| **B.** Feeder on a continuous servo | intake + lane, flywheel L, flywheel R, turret | extractor, feeder | Flywheels balanced; no gate needed (stopping the feeder is the gate). Risk: the servo's torque pushing a piece against the pad. |
| **C.** One motor for both flywheels | intake + lane, both flywheels geared together, feeder, turret | extractor | The flywheels can't differ in speed; the feeder's speed is its own. Recovery is slower; heavier flywheels help. |
| **D.** Turret on a servo | intake + lane, flywheel L, flywheel R, feeder | extractor, turret (continuous + encoder) | Flywheels balanced, but the turret is less stiff and less accurate under hits. |
| **E.** Feeder on the intake motor | intake + lane + feeder, flywheel L, flywheel R, turret | extractor, gate | Flywheels balanced, but the intake motor may bog with a full lane while feeding. |

**Recommendation:** B if the servo test passes, otherwise C. Both keep the turret on a motor, the most accurate option.

## The tests that decide

1. **Shared feeder (A):** on the launcher rig, belt a feeder to one flywheel, run a volley and log both flywheels' RPM
   with their encoders. Fail: the driven side dips noticeably more than the other, or shots curve.
2. **Servo feeder (B):** a goBILDA Super Speed servo in continuous mode, belted about 2:1 up to the feeder wheels,
   pushing a POLLEN and a NECTAR against the pad. Fail: it stalls or slips.

## Turret position (settled whichever drives it)

- Count the turret's encoder: the motor's own, or a REV Through Bore on the drive gear's shaft for a servo turret.
- At setup, line up a painted mark on the ring with one on the frame, and zero in init.
- A REV magnetic limit switch on the frame and a magnet on the ring re-zero the count every time the turret passes.
  That cancels drift and recovers after a brownout.
- Never homing at all takes two analog absolute encoders geared off the ring with different ratios (e.g. the 64T drive
  gear and a 36T pinion meshing it). The pair of readings is unique over 1178° of turret travel; past that window it
  repeats, so the code keeps the turret inside it or re-zeroes.
- Nothing on the turret has a wire (the Limelight stays on its fixed mast for localization), so mechanically it can turn
  without limit; the encoders' absolute window (1178°) is the practical limit unless something re-zeroes it. A hood servo would need the slip ring.
