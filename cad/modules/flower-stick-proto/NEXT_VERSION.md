# Next version of the poker: notes from 28 Oct 2026 (after the first full night on 20245)

The locked part (`stl/poker_u_hi.stl`) is on the robot and works. These are the changes asked for in the next one.

**What worked.** The extend, drive in, wait, drive out FLOWER pick runs well and took all four balls fast. The part fitted
centred on its pegs. The relief over the front screw and the pegs held it rock solid with one zip tie on the far side.

**Fastening (done in `poker_u_hi_bolt.stl`, `SCREWS=1`).** The front screw standing out of the channel is threaded, so it can come
out and go back through the roof. The relief becomes a 4.5 mm hole with an 8 mm counterbore 2.5 mm deep, for an M4 head. The head
then sits 1.5 mm proud of the roof, so the screw needs to be 1.5 mm longer than the standing one: use the next size up (2 mm).
Result: the screw on one side, a zip tie on the other. The rear screw stays in its notch. Nothing out the back (it fouls the slides).

**Shape (not yet built, needs a measurement first).**
- The poke does not go deep enough: it works partly on inertia, and a slow approach fails to knock the piece loose.
- It may need to sit a little lower and reach a little further in.
- A chamfer on the top: no reason for it to be square.
- A V at the nose instead of a square end.
- Idea: bristles instead of a rigid poke (not recommended, see the chat; more friction makes the push worse).

**Constraint to check before lowering it.** The stowed blade's lip passes only 2.8 mm under the poke. Any lowering or
reach into 0.8 to 2.1 in ahead of the lip has to be run against the stow sweep first.

## The poke's reach follows the blade's lip (28 Oct 2026)

Dustin found the blade (the ramp with the hex through it) was canted up about 5 degrees and tightened it flat, so the wheels
are on the tile and the blade lies flat when deployed. The drive-in distance is right but the poke no longer matches.

`page/reach_vs_lip.js` scans it. The poke feeds all 24 cases when its tip sits about 2.5 in behind the blade's lip, so the
reach wanted is **the lip's distance ahead of the intake face, minus 2.5 in**. Both directions fail, but not equally:

| lip ahead of the face (in) | reach 1.3 | 1.6 | 1.9 | 2.2 | 2.5 |
|---|---|---|---|---|---|
| 3.5 | 19 | 15 | 8 | 0 | 0 |
| 3.8 (the model's) | **24** | 19 | 15 | 8 | 0 |
| 4.1 | 20 | **24** | 19 | 15 | 8 |
| 4.4 | 16 | 20 | **24** | 19 | 15 |
| 4.7 | 16 | 16 | 20 | **24** | 19 |
| 5.0 | 16 | 16 | 16 | 20 | **24** |

(Cases fed of 24 at 3 in/s.) Too shallow plateaus at 16; too deep falls to nothing. The window is about 0.3 in wide, so the
number to measure is the lip's distance from the face, extended, flat, wheels down. The model assumed 3.81.
