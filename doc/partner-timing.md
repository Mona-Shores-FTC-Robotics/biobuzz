# Partners in AUTO: timing, waits and branches

**Belding lineup update, 5 Nov:** L-Partner is now L-NoCross and R-Partner-Tip is
R-NoCross. R-Partner-Preload and R-Solo-Staging are benched on `autos-experiment`.
The discussion below records the earlier decisions; the accepted Belding priorities
and remaining fallback work are in `robot-todo.md` and `TeamCode/autos/README.md`.

The decisions from the 3 Nov 2026 discussion of the first partner simulations (PR #385), written
down so the logic does not have to be rebuilt. They shape how the Autos are drawn, what the
simulator runs, and what the drive team asks a partner before a match.

## What a wait is really waiting for

An Auto that waits for the HIVE to tip is waiting for the partner. L-Partner relies on its partner
at once: its first card after the drive is "wait for TIP or CELL up". The R Autos rely on the
partner later, at the launch that follows the partner's volley. Either way the thing we cannot
control is *when the partner's last preload leaves its robot*, and everything after that is
physics we can measure:

| Between their last ball leaving and our `CellUp` firing | Time |
|---|---|
| Flight to the CELL | about 0.5 s |
| Dwell before the rocker moves (filmed 6 Oct; shorter the heavier the load) | 0.25 to 3.4 s |
| The swing itself (filmed) | 0.6 to 1.1 s |
| The camera sees it settle and `HiveTracker` confirms | a few tenths |

So the lag from their last shot to our trigger is about 1.5 to 5 s, at the fast end with a heavy
load (four preloads on top of the staged pieces) and at the slow end with a light one or a miss.

## The contract with the partner: one number

Ask the partner one question: **"when does your last preload leave?"** Teams answer that
consistently; "when do you fire" means different things to different teams.

- The driver enters that number in init (dpad). The DS shows it.
- The Auto's partner wait is that number plus a named TIP-lag constant, kept with the other
  measured times, never hidden in code. The DS line reads "partner's last shot 8 s, waiting
  until 12 s". The program adds the margin, but on screen. **Changed 12 Nov 2026 (mentor):**
  teams rarely know when their last preload leaves. The DS now reads "waiting for TIP 1
  until 9 s" (the TIP's number since 13 Nov; "partner's TIP" before), the D-pad moves that one number, and the question is "do you shoot your preloads
  right at the start?". The simulator still takes a stand-in's last shot and adds the lag.
- Err long. The costs are not symmetric: waiting a few seconds too long costs the last thing in
  the Auto, probably the GARDEN; giving up a few seconds too early costs four preloads fired at a
  down CELL, a collision at the partner's firing spot, and the TIP we were waiting for anyway. The
  Otherwise branch is the plan for a partner who never fires, not for one who is a little late.

## What the simulator runs

The simulator acts as the driver: a partner entry gives the one number, and from it the stand-in's
timing is set so its last preload leaves then, and our Auto's wait is set as the driver would set
it. Nobody types a wait into a request. With an honest partner the run shows what we gain.

Partners are then simulated in exactly three states, nothing in between:

1. **On time.** Its last preload leaves when it said.
2. **Missed everything.** Its shots go wide, no TIP comes, and it carries on with its own Auto
   and parks on schedule. This is the case that moves through the field while we are deciding.
3. **Dead on its start.** It never moves (`partner-r-broken-down`, `partner-l-broken-down`, on the start it was meant to leave).

A partner that breaks down partway through its route is not modelled either (mentor, 3 Nov 2026):
when a teammate's robot goes wrong it usually does not move at all, which is case 3.

"Later than they said" is deliberately not modelled: we can never make that collision not
happen, and planning for it would hide the margin we chose to make visible. The simulator keeps
the TIP lag's spread, which is physics, not the partner's honesty.

Built in the follow-up to PR #385: a request's partner entry gives `lastShotS`, and
`"fails": "misses"` or a still stand-in for the two failures (`TeamCode/autos/README.md` has the
curation, `SimRunTest` the format). The raw `timing` knobs stay as an override for studies. On the
robot the number goes in on gamepad 1's D-pad in INIT (`InitOptions`), and the TIP lag is
`AutoRegistration.PARTNER_TIP_LAG_S` (5.5 s, a give-up limit set from the Belding films: `TeamCode/autos/timing-terms.md`), also `partnerTipLagS` in `TeamCode/autos/timing.json`.

## LaunchAll does what it is told

The robot's `LaunchAll` does not check the HIVE. It waits for the launcher to reach speed and the
robot to settle, then feeds for its set time, whatever the CELL is doing. Its 5 s card limit is a
mechanical guard (the feed never started: move on), never the thing that decides whether we score.
Keep it that way: a hidden "only if a CELL is up" check inside the card would make a plain card
wait for something the Auto never asked about, and the preview could no longer say when it fires.
The wait card is the decision; the launch card launches.

The simulator used to differ here (its `LaunchAll` held fire until a CELL was up and in range, so
a late partner's TIP let it score where the robot would have wasted four). Since the follow-up it
fires like the robot's, at our CELL at the robot's end, up or not, and the results table counts
the pieces fired at a down CELL.

## One Otherwise per Auto, spent wisely

For now each Auto gets at most one branch (one wait with an Otherwise). Spend it on the moment the
partner can change what we should do next. For L-Partner that is the first wait, since it relies
on the partner at once. For the R Autos it is the launch that follows the partner's volley.

Nested branches (a second "is the CELL up?" check before the Otherwise branch's own launch, which
would save the preloads for the other CELL when the partner tipped just as we arrived) are
shelved until the basics work. Strictly, every launch that should cause a TIP is a branch point,
because we can miss too; the simulator will show which branches earn their place before we draw
them.

## Where the Otherwise branch fires from

L-Partner's Otherwise branch drives to RIGHT_LAUNCH_CATCH, 10 in in front of RIGHT_START, and
fires at the audience CELL. The two failure cases pull in opposite directions:

- **Partner missed and leaves:** its park path from RIGHT_START to the LOADING ZONE passes within
  a robot's width of RIGHT_LAUNCH_OFFSET, so that spot is in its lane; the catch spot is clear
  once the partner has gone.
- **Partner dead on its start:** the catch spot is ten inches in front of it, so we back into it;
  the offset spot is clear.

Decision: keep the catch spot and accept the dead-partner bump ("even if we back into them and
shoot, it might be fine"). The simulator flags any overlap of two bodies as a collision; that is
a fact about the bodies, not a judgment that the shot failed or that a foul was called. If the
dead case turns out to matter more than the missed case, the fix is a new spot with more room,
not the offset spot.

## What the first simulations showed (3 Nov 2026, before the follow-up)

- R-Solo grazes a partner that stays on LEFT_START by about an inch while squaring up on the
  scoring FLOWER: the FLOWER's line is x 47.4, a robot on it reaches x 55.5, and a partner on
  LEFT_START starts at x 54.2. Moving the staging spot does not help; the pick must be on the
  FLOWER's line. Either ask a partner that stays put to sit two inches further along the wall, or
  skip the scoring FLOWER when the partner stays.
- R-Partner-Preload collides with the left launcher when it fires late, because the partner's
  park path from LEFT_START crosses our approach to LEFT_LAUNCH_OFFSET; a partner done by about
  6.5 s is parked before we arrive, one done at 9 to 15 s is in the way.
- R-Solo-Staging's staged row survives the partner leaving but is swept by our own robot at
  LEFT_LAUNCH_OFFSET before we come to collect it; PARTNER_STAGED_POLLEN or that launch spot has
  to move in the Visualizer.
