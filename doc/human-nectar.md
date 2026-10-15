# Entering NECTAR in AUTO, reliably (9 Oct, draft; rules confirmed 14 Oct)

The official Q&A confirmed it on 14 Oct 2026 (Q11; `doc/game/qa-review.md` on `claude/simulator`): our human
player enters one NECTAR through the LOADING ZONE after each of our HIVE's TIPs during AUTO (G426.A), and an Auto
picks it up. This is how to make that repeatable. Nothing here has been tried
on a field yet: the numbers to aim for are targets for practice, not measurements.

## What the rules require (TU04, and the Q&A of 14 Oct 2026)

- **In AUTO as in TELEOP** (Q11): one NECTAR each time our alliance TIPs our HIVE, whichever period it happens in.
  Every 11.4.6 rule still applies during AUTO.
- **Release it; never touch it on the tile** (Q6): the human "must drop the NECTAR into the LOADING ZONE", and
  there is no exception for the piece being entered. Once it is on the tile it is a SCORING ELEMENT in contact with
  the TILE, and touching it is G425.
- **A throw or roll in a useful direction is allowed** (Q7), on three conditions: it is released before it touches
  the tile, it touches the LOADING ZONE's tile before leaving the zone, and it doesn't end up scoring in a HIVE or
  FLOWER by itself.

- One NECTAR per TIP of our HIVE (G426.A). It does not have to go in the instant the TIP happens (G426 note).
- By hand, no tool, by our own drive team member (G427 A, B).
- It must touch the tile **inside the LOADING ZONE before it touches a robot or a field element** (G427.C). So it never
  goes in while a robot is in the zone or about to enter it.
- The human stays in the ALLIANCE AREA (G422) and never touches a robot, or a piece already on the tiles or touching a
  robot (G425). Reaching over the wall to release it is how G427 expects it to go in.
- Nobody may signal the robot (G401). The Auto has to find the NECTAR by itself.

## The technique: a gentle roll along the wall, into a waiting intake

Placing a NECTAR still takes a reach and a second or two; with only a few seconds between a TIP and the next shot,
a roll is faster (the user, 9 Oct). The one entry on film (Saline P3) was rolled at 17 to 20 in/s straight out from
the wall and stopped 11 in out, at the far edge of the 11 in deep zone: a roll across the zone has almost no room to
stop. So roll it **along** the zone instead, parallel to the wall, where it has the zone's full length (about 23.6 in):

1. **Release it in the air, inside the zone.** From a marked spot at one end of the zone, an underhand roll along
   the wall toward the other end, let go before it touches anything, so its first touch is the tile inside the zone
   (G427.C, Q7). Never touch it again once it is on the tile (G425, Q6): no nudge, no push, no second try. A slow,
   repeatable release matters more than a fast one.
2. **The robot waits at the other end**, intake facing back along the wall toward the release spot, a few inches
   off the wall. The NECTAR rolls into the intake, and the turret is already on the CELL.
3. **Cue on the TIP.** Roll as soon as our HIVE has tipped and the path along the wall is clear. Never roll it at a
   robot that's still moving into place.
4. **Keep the parking spots clear.** Both robots park in this zone at the end of AUTO; the roll ends at the robot,
   not on a parking spot.

On the rules: Q7 (14 Oct 2026) allows exactly this, a roll "in an advantageous direction" released before it
touches the tile, whose first touch is the zone's tile. Rolling into a waiting robot is within it. G427's note still
asks teams not to "push the boundaries", so show the head referee the roll at the first event's drivers' meeting.

## The robot's side

- **Find it with the webcam, don't assume where it is.** The Auto drives to the zone and lets the piece camera
  (`vision/PieceVisionSubsystem`) steer onto the red NECTAR, as the spill pickups do. That absorbs a few inches of
  placement spread, and if no NECTAR is seen (the human waited, or it rolled), the Auto moves on after a set time
  instead of waiting.
- **Wait at the far end of the zone, intake facing the roll**, so the NECTAR comes to the intake; if it stops short
  or wanders, the camera steers onto it.
- The simulator currently drops each NECTAR at the centre of the zone, 2 s after the TIP, with no spread. Once the
  practice below gives real numbers, the simulator uses them.

## A loading-zone station (the user's idea, 9 Oct, to be scored)

With the turret, a robot can sit at the LOADING ZONE with its intake facing the drop spot and keep the launcher on the
CELL while it waits: the human rolls the NECTAR along the wall into the intake, and it fires without turning. The routes
already know a robot parked in the zone is inside the north CELL's firing wedge (duo-lz), so the station also earns
AUTO PARK.
- **The limit is one NECTAR per TIP** in AUTO (G426.A), so in AUTO it's a pickup and a shot after each TIP, not a
  stream. The rest of the NECTAR can only come in with 60 s left (G426.B), in TELEOP.
- **G427.C:** the NECTAR's first touch must be the tile inside the zone, which a roll released in the air inside the
  zone meets (Q7; see the technique above).
- **Whose job it is:** in the Sister pair, L works the left end, which is where the shortfall is. Whether L can afford
  to wait at the zone between TIPs is for the simulator and the body-designs chat to score.

## Practice drill (to do before an Auto relies on it)

On the practice field, with a tape grid on the tiles of the LOADING ZONE:

1. Someone calls "TIP" at random moments; the human player rolls a NECTAR along the wall from the release spot,
   letting go before it touches the tile and never touching it again. Do 20 a person.
2. Record for each: the time from the call to release, the time the roll takes to reach the far end, and how far off
   the line along the wall it drifts.
3. Targets: released within about 1 s of the call, and 19 of 20 arriving within 2 in of the line at the robot's end.
4. Then with a robot: run the Auto's pickup on 20 entries and count the clean pickups and the time from release to the
   NECTAR being held.

Send the numbers to the simulator chat: they turn its perfect drop into the real spread and timing.
