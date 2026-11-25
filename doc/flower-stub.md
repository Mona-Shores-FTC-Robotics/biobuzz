# The FLOWER stub (parked)

**Status: parked, 25 Nov 2026 (mentor).** Today's sticks stay for States. 20245's held passive stick already gets
4 of 4 on every clearly seen Auto pick, Tim's corner wheels work with it, and 19429's fix is the copy of that stick
already in `doc/robot-todo.md`. Pick this up after States, or sooner if today's stick starts failing.

The idea: replace the long FLOWER stick on the intake nose with a short, held, passive one that bolts only to fixed
chassis. The corner wheels take over corner and GARDEN gathers, so it only has to pull a FLOWER's POLLEN out.

The page that shows it, with sliders for the stub and the nose: https://claude.ai/artifact/8N8z4JnLJHF3fmdTjHDadU
(private to the mentor). The drawings and parts: [`cad/flower-stub/`](../cad/flower-stub/) on branch
`claude/flower-stub`.

## What was settled

- **The foot** is the October notebook's mountain blade, 5.4 in wide (17 holes), on an 8 mm REX shaft with two
  39 mm wheels on the tile. That's the FLOWER's 4.8 in (manual Figure 9-12) plus about a hole each side.
  - The notebook's shape: 60° nose, 1.1 in peak, Carter's lane, tail cut 2.0 in behind the lip.
  - The wheels sit outside the FLOWER, with 0.6 in of aim before one rides onto its 0.43 in ring.
- **Where the foot lands:** its lip 4.1 in (13 holes) ahead of the intake face, which is where today's stick lands
  and where the Pollen Kicker expects it.
- **The pivot** is low: 2 holes ahead of the intake face and 6 holes up. With the wheels on the tile, the FLOWER push
  goes down the arms into the pivot, not into the servo.
- **The arms** are 10 holes from pivot to shaft.
- **The servo is held**, and drives the arms through a crank, a link and a lever (a four-bar).
- **Stowed,** it folds to about 83° in front of the mouth:
  - 0.2 in inside the 18 in cube;
  - 0.2 in under the Pollen Kicker with the nose all the way down;
  - never behind the intake face, so the nose slides its whole travel.

## Why it has to be the wide U

**The narrow U** has its arms about 3.4 in from the centre, on mounts that bolt to the side channel's front flange
(`parts.py`). It clears everything, but folded its arms stand at ball height right where the corner wheels push
POLLEN into the mouth. With it, the corner wheels would be mostly useless.

**Other ways around that, and why they don't work:**

- **A higher pivot** (about 3½ in, so the arms clear a ball) folds the foot to 6¼–8 in up, through the Pollen
  Kicker at 5.7–7.5 in. The Kicker can't move up, because it has to reach the FLOWER's second ball. It also leaves
  about 0.02 in to the 18 in cube, and the steeper arm puts the FLOWER push on the servo.
- **Arms closer to the middle** stop blocking only within about 1.5 in of the centre, where they hit the FLOWER's
  bottom ball and ring.
- **No Kicker:** the notebook's model loses the stack on about a third of slow pushes.

**The wide U** puts the same arms, foot, pivot height and four-bar outboard of the corner wheels, at about
|Y| 7.25–7.45. The pivots sit on printed brackets bolted to each front drive module's outer plate, ahead of the axle.
The crossbar folds to about 5 in up, above the POLLEN, so the corner wheels keep the whole mouth.

- The robot becomes about 15 in wide.
- The crossbar spans about 15 in, so it needs to be stiffer than a bare 8 mm shaft.

**Not done:** the wide U's brackets, crossbar and parts, and a clearance run for them. They need, from the robot:

- what is bolted outside each front drive module, if anything;
- where the outer plate's free holes are, ahead of the front axle.

## Where the numbers come from

- The robot is from the 24 Nov STEP, as read by the corner-wheel session. It is approximate at the front and
  **not measured**.
- Tim's corner module is from `cad/corner-wheels/parts.py` on `claude/corner-module-wall-wip`.
- The foot and the Kicker's reach are from the October notebook ("The FLOWER Stick" page, branch
  `spike/179-flower-stick-proto`).

## The files

All in `cad/flower-stub/` on `claude/flower-stub`:

- `proposal.py`: the first to-scale side and top view.
- `parts.py`: the narrow U.
  - Every part as STEP, STL and 3MF.
  - Both assemblies.
  - A clearance sweep against the corner modules, the cube, the Kicker and the nose.
  - **Not for printing.**
- `views.py`: renders of the narrow U.

## If it comes back

1. **Build the wide U** in `parts.py`: brackets on the drive modules' outer plates, a stiff crossbar, the same foot
   and four-bar. Rerun the sweep.
2. **Ruler checks first,** on each robot, nose all the way down:
   - the Kicker's bottom height and reach;
   - the intake face's distance from the robot's back;
   - the outer plates' free holes.
3. **The test:** 10 FLOWER picks with the stub against 10 with today's stick, same robot and battery, counting
   4-of-4 on video.
4. **Code:** reuse `FlowerStickSubsystem`. Measure the stub's in and out positions on Bench: FLOWER Stick and put
   them in a `RobotDifferences` line. Auto INIT already holds the stick in.
