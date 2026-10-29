# When the code crashes in a match

Read this once before a competition. The short version: **photograph the red error, then do the
next thing on the list for whichever OpMode crashed.** Nothing here needs a laptop.

## What happens when any OpMode crashes

- The robot stops. The Driver Station shows a red error. The SDK says a Restart Robot is no longer
  needed after an uncaught exception.
- **Take a photo of that red text first.** It is the only place the full error lives, and it goes
  away when you start anything else.
- The match log has it too: an event starting `CRASH in loop`, with the exception and the first
  lines of the stack, written within about half a second. After the match, pull the logs.
- Every loop, the robot also writes down where it is: pose, alliance and the turret's zero
  (`Handoff`). That is what lets the next OpMode pick up from there. It does not depend on the
  crashed OpMode's stop code having run.

## The Auto crashed, say 15 seconds in

1. The Auto is over. Whether a team may restart it in a match is a rules question: ask the head
   referee or the field technical advisor, and do not assume. Replays are not granted for
   programming problems.
2. When it is time for TeleOp, choose **Basic Drive** as usual and INIT.
3. Read the line at the top of the Match page:
   - **"pose from Auto Ns ago (x, y, °)"**: the robot knows where it stopped. The turret kept its
     zero. Nothing to do. Play.
   - **"no handoff"**: it does not know. The driver's forward is whichever way the robot faces at
     PLAY, so face it away from the driver first. The turret then zeroes where it stands at PLAY:
     if it is not at its home mark, leave it LOCKED and do not use AIM, or turn it home by hand
     before PLAY.
4. If the pose looks wrong, hold still with two tags in view and press **driver D-pad left** (camera relocalize).
   The page says what it did.

## TeleOp crashed, say 20 seconds in

1. Take the photo. Then, **if the rules let you restart** (ask), choose Basic Drive, INIT, PLAY.
2. A restart inside **30 seconds** resumes where the robot was: the Match page says
   **"pose from the TeleOp that just stopped Ns ago"**. The alliance, the pose and the turret's zero
   all come back. The log starts a new file for the same match.
3. After 30 seconds it no longer trusts the last pose, because someone may have moved the robot. The
   page says **"no handoff"**. Face the robot away from the driver, hold **driver D-pad up**
   for a second to reset forward, and press **driver D-pad left** with two tags in view to fix the pose
   (or back into the left corner by the LOADING ZONE, intake toward the other alliance, and press
   **driver D-pad down**).
4. The alliance is not remembered after 30 seconds either: press X (blue) or B (red) in INIT.

## Things nobody has checked on a robot yet

- Whether the **Pinpoint keeps its own position** across an OpMode restart. The code puts the last
  recorded pose back either way, which is at most one loop old.
- Whether the SDK runs our stop code after a crash. The handoff does not need it.
- What the rules allow after a crash. Ask before the event, not during a match.

## For the person fixing it afterwards

- Open the match log in AdvantageScope and search the Console for `CRASH`. The first stack line is
  where to look.
- A crash in `init_loop`, `start` or `loop` is logged and rethrown unchanged; the robot behaves
  exactly as it would have without the logging.
