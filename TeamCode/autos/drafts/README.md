# Drafts

Simulator-only Autos on master: a `.pp` here, its generated class (no OpMode, so the Driver Station
never lists it) and its request in `TeamCode/sim-requests/`. The 3-TIP swap pairs (r-3tip-a/b,
l-3tip-a/b) moved to the `autos-experiment` bench on 4 Nov 2026; their README is
`TeamCode/autos/3tip-drafts.md` there.

## R-Solo-Push, for a partner with no Auto (4 Nov 2026)

`r-solo-push.pp`: R-Solo's 2-TIP plan with a push. The partner, powered off or with no Auto, starts
with its back on our alliance wall centred on tile A4 (`PARTNER_PUSH_START`, 8.1, 82.5, facing the
field), just south of the LOADING ZONE. After the WALL FLOWER pick, our robot backs into its south
side and pushes it 10 in north and 3 in off the wall, partly into the zone, then slides off east and
goes north between the partner and the HIVE leg to LEFT_LAUNCH_OFFSET. The pushed partner earns
LEAVE (Q&A Q28) and AUTO PARK (the same reasoning, not yet asked), so SWARM no longer needs our own
AUTO PARK. It ends with a plain drive to LEFT_PARK after the last launch: no park step, so the
endgame guard never parks early through the partner.

Simulated with `partners/partner-push-start` and `"pushable": true` (`sim-requests/r-solo-push.json`):
2 TIPs (TIP 2 at 25–27.5 s), partner LEAVE and PARK in every seed; the push costs 2.4 s at our
second launch against R-Solo (1.7 s route, 0.7 s pushing at a placeholder half speed). PR #427 has
the details; the robot check is in `doc/robot-todo.md` §3.

Open it: <https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=TeamCode/autos/drafts/r-solo-push.pp>
