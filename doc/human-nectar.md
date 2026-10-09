# Entering NECTAR in AUTO, reliably (9 Oct, draft)

If the pending Q&A confirms it, our human player enters one NECTAR through the LOADING ZONE after each of our HIVE's
TIPs during AUTO (G426.A), and an Auto picks it up. This is how to make that repeatable. Nothing here has been tried
on a field yet: the numbers to aim for are targets for practice, not measurements.

## What the rules require (TU04)

- One NECTAR per TIP of our HIVE (G426.A). It does not have to go in the instant the TIP happens (G426 note).
- By hand, no tool, by our own drive team member (G427 A, B).
- It must touch the tile **inside the LOADING ZONE before it touches a robot or a field element** (G427.C). So it never
  goes in while a robot is in the zone or about to enter it.
- The human stays in the ALLIANCE AREA (G422) and never touches a robot, or a piece already on the tiles or touching a
  robot (G425). Reaching over the wall to release it is how G427 expects it to go in.
- Nobody may signal the robot (G401). The Auto has to find the NECTAR by itself.

## The technique: place it, don't roll it

The one entry we have on film (Saline P3) was rolled in at 17 to 20 in/s and stopped 11 in from the wall, at the far
edge of the 11 in deep zone. A rolled or dropped NECTAR goes somewhere different every time, and a piece against a
wall or at the zone's edge is hard for an intake to take.

1. **Hold it low, then let go.** Reach over the wall at a marked spot along it and lower the NECTAR as close to the
   tile as is comfortable, about 4 to 6 in out from the wall, then open the hand. No throw, no spin: it should land
   and stay within a couple of inches.
2. **Always the same spot.** Pick one spot along the zone (its middle, unless the routes want otherwise) and use a
   landmark on the wall or a tile seam to find it without looking twice. The Auto is written for that spot.
3. **Cue on the TIP, then wait for the gap.** The cue is our HIVE tipping (the rocker swinging over). The NECTAR goes in
   about 1 s after it, and only if no robot is in or heading for the zone. If a robot is in the way, wait: a late
   NECTAR costs a little time, while one that hits a robot is a MINOR FOUL and is lost.
4. **Keep the parking spots clear.** Both robots park in this zone at the end of AUTO. The NECTAR goes where the Auto
   collects it before parking, not on a parking spot.

## The robot's side

- **Find it with the webcam, don't assume where it is.** The Auto drives to the zone and lets the piece camera
  (`vision/PieceVisionSubsystem`) steer onto the red NECTAR, as the spill pickups do. That absorbs a few inches of
  placement spread, and if no NECTAR is seen (the human waited, or it rolled), the Auto moves on after a set time
  instead of waiting.
- **Approach along the wall, intake first, from the side away from the HIVE**, so a piece a few inches off the wall
  goes straight into the intake.
- The simulator currently drops each NECTAR at the centre of the zone, 2 s after the TIP, with no spread. Once the
  practice below gives real numbers, the simulator uses them.

## A loading-zone station (the user's idea, 9 Oct, to be scored)

With the turret, a robot can sit at the LOADING ZONE with its intake facing the drop spot and keep the launcher on the
CELL while it waits: the human places the NECTAR, the intake takes it, and it fires without turning. The routes
already know a robot parked in the zone is inside the north CELL's firing wedge (duo-lz), so the station also earns
AUTO PARK.
- **The limit is one NECTAR per TIP** in AUTO (G426.A), so in AUTO it's a pickup and a shot after each TIP, not a
  stream. The rest of the NECTAR can only come in with 60 s left (G426.B), in TELEOP.
- **Stay on the right side of G427.C.** The NECTAR must touch the tile before the robot. Placing it with the mouth an
  inch away so it rolls straight in is the kind of thing G427's note warns against ("should not attempt to push the
  boundaries"). Keep the intake a few inches back from the spot and let the robot take it once it's resting on the
  tile.
- **Whose job it is:** in the Sister pair, L works the left end, which is where the shortfall is. Whether L can afford
  to wait at the zone between TIPs is for the simulator and the body-designs chat to score.

## Practice drill (to do before an Auto relies on it)

On the practice field, with a tape grid on the tiles of the LOADING ZONE:

1. Someone calls "TIP" at random moments; the human player enters a NECTAR at the spot. Do 20 a person.
2. Record for each: the time from the call to release, and where it came to rest (inches from the wall and from the
   spot).
3. Targets: released within about 1 s of the call, and 19 of 20 resting within 2 in of the spot, at least 3 in off the
   wall.
4. Then with a robot: run the Auto's pickup on 20 entries and count the clean pickups and the time from release to the
   NECTAR being held.

Send the numbers to the simulator chat: they turn its perfect drop into the real spread and timing.
