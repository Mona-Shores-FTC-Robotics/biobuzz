# Motors and servos: the decisions any robot design needs

Written 8 Oct 2026 for the design meeting. It holds whichever robot the team builds, because the problem comes from the
game and the motor limit, not from one design.

## The budget

FTC allows eight motors (check the current manual for the servo limit). The drive takes four. A launcher robot then
wants five more jobs: intake, flywheel L, flywheel R, a feeder that pushes each piece into the flywheels, and a turret.
That's one job too many, so one of them has to share a motor or move to a servo.

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
  gear and a 48T gear on the 176T ring). The pair of readings is unique over about 393° of turret travel.
- Nothing on the turret has a wire (the Limelight stays on its fixed mast for localization), so it can turn without
  limit. A hood servo would need the slip ring.
