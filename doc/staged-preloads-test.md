# Staging preloads in the hook: what needs a robot or a test

**Why.** The simulator tried setting our 4 preloads down inside the lowered hook while we wait for TIP 1, fetching
the far FLOWER, and picking the preloads back up (`StagedPreloadsTest`, issue #159; numbers in the repository
README, "Staging our preloads in the hook"). Every number it used for the steps below is a placeholder. These are
the measurements that would make its answer real, roughly in the order they matter. None of them needs the HIVE.

## Rules (for the rules discussion, before anyone builds anything)

- [ ] Are 4 staged pieces lying in a lowered, stationary hook CONTROLLED (G407, Glossary "CONTROL": "stuck in, on,
      or under the ROBOT", or herding)? If yes, staging them and then picking up the FLOWER's 4 is 8 CONTROLLED.
      The simulator lifts the hook and backs off before the intake comes back on, so they are never in contact
      while it holds the FLOWER. Is that enough?
- [ ] Is creeping forward with the hook down and pieces in it herding (CONTROL), as long as it lasts more than a
      MOMENTARY 3 s?
- [ ] Does anything stop us leaving our own pieces on the tiles near the HIVE in AUTO? (Nothing found in TU03; G411
      hoarding is about the other alliance's access.)
- [ ] The cheaper alternative §10.3.4 already allows: start with the preloads on the tiles, touching the robot. Is
      that worth a route of its own?

## On the robot or a bench

- [ ] **Can the intake reverse cleanly?** Reverse it with 4 POLLEN in, 10 times. Count jams and pieces that stay
      in. Time from the first piece out to the last (the simulator says 0.25 s apart, 0.75 s for 4).
- [ ] **How fast do they come out, and how far do they roll?** Film from above with a tape on the floor. Note
      where each piece stops, measured from the front face (the simulator pushes them at 20 in/s ±15%, and they
      stop 3-6 in out on normal tiles, under 2 in on slow ones). Do it on competition tiles if you can.
- [ ] **Do they leave the mouth in two lanes, one lane, or wherever?** The simulator alternates left and right of
      centre. Note the spread across the front.
- [ ] **With a mock hook** (a 10 in arm on the left and a crossbeam, cardboard is fine): how many of the 4 stay
      inside, and do any bounce off the crossbeam back under the robot's face where the intake cannot reach?
- [ ] **How long does the hook take to swing down and up?** The simulator uses 0.3 s each way.
- [ ] **Does lifting the hook disturb the pieces?** The simulator cannot say: it removes the hook the moment it
      starts lifting. Film the crossbeam and arm swinging up past pieces lying against them.
- [ ] **Backing off:** with the hook up, back the robot straight off 6 in. Do any pieces follow it or get dragged?
- [ ] **Taking them back:** drive forward 10 in through them with the intake on (`N_PICK`). Count how many it
      takes in one pass, and time it.
- [ ] Run `LoopTimeBaseline` before and after a hook subsystem exists (CLAUDE.md, "Loop time").

Put the numbers in the issue. Then `AutoSim.placeholderOuttakeInPerS`, `SET_DOWN_INTERVAL_S`, the hook's
`sideWallsTravelS` and the route's `SETTLE_MS` / `PICK_FWD` in `tools/auto-routes/qual_stage.py` get real values,
and the comparison gets run again.
