# Deprecated Autos

**Not current. Don't use these to judge a robot or a route.** They were written and simulated for
earlier robots (two launchers, a catapult, or the 18 in one-launcher robot with an 18 in intake that
grabbed pieces up to 3 in out), before 5 Oct 2026, and have not been re-run since:

- not on the baseline robot (the build team's option 3, since 5 Oct 2026 17:11 UTC);
- not with the conservative intake, the launcher near the back, or the G409 check (some of them
  very likely touch falling pieces);
- their numbers and their simulated logs are from those older runs.

They are kept, unchanged, for their ideas. The current Autos are in
[the root README](../../README.md#latest). To bring one back: re-run
its script on the current robot (`BIOBUZZ_AUTO_DESIGNS="builders' option 3 (5 Oct CAD)"`), check
G409, and move it to the root README with the date it was run.

## As they were listed on the front page (4 Oct 2026)

For an earlier robot (4 Oct 2026): one two-wheel launcher for POLLEN and NECTAR and an 18 in front
intake; not re-run on option 3. Each Auto's name is who our alliance partner is and what it does, then our
plan: **Sister** (our other robot, full choreography between the two), **PartnerShoots** (fires its 4
preloads, then parks) or **PartnerStages** (can't shoot: lines its preloads up for us, then parks).
Compare Autos with the same partner. Points are average alliance AUTO points over 20 simulated runs,
with the spill landing and scattering as filmed on 3 Oct; the routes were tuned before the scatter.

| Auto | Partner | What we do | Points | Watch in the Visualizer | `.pp` files | Simulated `.wpilog` |
|---|---|---|---|---|---|---|
| **Sister-Recycle** | Sister: our other robot | Each robot catches its own CELL's spill and refires it when the CELL rises. | **49** (3 TIPs in 3 of 20) | [**Together**](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Sister-Recycle) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle3-right.pp) · [other](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle3-left.pp) | [recycle3-right.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/recycle3-right.pp) · [recycle3-left.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/recycle3-left.pp) | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/recycle3-right/latest-best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/recycle3-right/latest-typical.wpilog) |
| **Sister-ThrowBack** | Sister: our other robot | Sister-Recycle, with a launcher that also throws straight back. | **46** (2 TIPs) | [**Together**](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Sister-ThrowBack) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle5-right.pp) · [other](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle5-left.pp) | [recycle5-right.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/recycle5-right.pp) · [recycle5-left.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/recycle5-left.pp) | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/recycle5-right/latest-best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/recycle5-right/latest-typical.wpilog) |
| **PartnerShoots-ThreeTip** | Fires its 4 preloads, parks | Our preloads, both FLOWERs, the spilled NECTAR and the GARDEN, parks. | **59** (3 TIPs in 3 of 20) | [**Together**](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/PartnerShoots-ThreeTip) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/three-tip-adaptive.pp) · [other](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-park.pp) | [three-tip-adaptive.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/three-tip-adaptive.pp) · [partner-preloads-park.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/partner-preloads-park.pp) | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/three-tip-adaptive/latest-best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/three-tip-adaptive/latest-typical.wpilog) |
| **PartnerShoots-Tunnel** | Fires its 4 preloads, parks | We work the HIVE alone, written for qualification matches. | **56** (2 TIPs) | [**Together**](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/PartnerShoots-Tunnel) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/solo-tunnel.pp) · [other](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-park.pp) | [solo-tunnel.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/solo-tunnel.pp) · [partner-preloads-park.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/partner-preloads-park.pp) | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/solo-tunnel/latest-best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/solo-tunnel/latest-typical.wpilog) |
| **PartnerShootsRight-LeftTunnel** | Fires its preloads from the right start, parks | Partner makes TIP 1; we take the far FLOWER, tunnel, the GARDEN. | **52** (2 TIPs) | [**Together**](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/PartnerShootsRight-LeftTunnel) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/left-tunnel.pp) · [other](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-right.pp) | [left-tunnel.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/left-tunnel.pp) · [partner-preloads-right.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/partner-preloads-right.pp) | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/left-tunnel/latest-best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/left-tunnel/latest-typical.wpilog) |
| **PartnerStages-ThreeTip** | Can't shoot: lines its preloads up for us, parks | We fire ours, tunnel, pick its row up with the webcam. | **56** (2 TIPs) | [**Together**](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/PartnerStages-ThreeTip) · [ours](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/staged-three-tip.pp) · [other](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-leave-park.pp) | [staged-three-tip.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/staged-three-tip.pp) · [partner-leave-park.pp](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/TeamCode/autos/partner-leave-park.pp) | [best](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/staged-three-tip/latest-best.wpilog) · [typical](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/raw/sim-results/staged-three-tip/latest-typical.wpilog) |

- **Together** plays both robots at once; **ours** / **other** opens one robot's Auto. No login.
- **best** / **typical**: the highest-scoring and the median of the 20 runs, the newest simulation of that
  Auto. They change when someone re-simulates it.
- The simulator's guesses for this launcher (2 s spin-up, 0.45 s a shot, 75° hood) are unmeasured, and
  most of the commands these Autos use exist only in the simulator so far.

### Watch the routes (Visualizer)

| Auto (files) | Together | Our robot | The other robot |
|---|---|---|---|
| Sister-Recycle (`recycle3`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Sister-Recycle) | [right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle3-right.pp) | [left](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle3-left.pp) |
| Sister-SetDown (`recycle4`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Sister-SetDown) | [right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle4-right.pp) | [left](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle4-left.pp) |
| Sister-ThrowBack (`recycle5`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/Sister-ThrowBack) | [right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle5-right.pp) | [left](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/recycle5-left.pp) |
| PartnerShoots-ThreeTip (`three-tip-adaptive`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/PartnerShoots-ThreeTip) | [three-tip-adaptive](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/three-tip-adaptive.pp) | partner: [partner-preloads-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-park.pp) |
| PartnerShootsRight-LeftTunnel (`left-tunnel`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/PartnerShootsRight-LeftTunnel) | [left-tunnel](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/left-tunnel.pp) | partner: [partner-preloads-right](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-right.pp) |
| PartnerStages-ThreeTip (`staged-three-tip`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/PartnerStages-ThreeTip) | [staged-three-tip](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/staged-three-tip.pp) | partner: [partner-leave-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-leave-park.pp) |
| PartnerShoots-Tunnel (`solo-tunnel`) | [together](https://mona-shores-ftc-robotics.github.io/Visualizer/#team=claude/simulator/PartnerShoots-Tunnel) | [solo-tunnel](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/solo-tunnel.pp) | partner: [partner-preloads-park](https://mona-shores-ftc-robotics.github.io/Visualizer/#gh=claude/simulator/partner-preloads-park.pp) |

## The candidates, how they were compared (4 Oct 2026, on the filmed spill and its scatter)

Each Auto's name is who our alliance partner is and what it does, then our plan: **Sister** (our other
robot, full choreography between the two), **PartnerShoots** (fires its 4 preloads, then parks;
PartnerShootsRight: from the right start) or **PartnerStages** (can't shoot: lines its preloads up for
us, then parks). The files keep their older names, in brackets.

Average alliance AUTO points over 20 simulated runs, on the spill fitted to the 3 Oct films: it pours
off the lowered CELL's lip, first touches the tiles about 42 in out from the wall, and scatters across
the field (`TeamCode/README.md`, "The spill, filmed"). The robots wait to catch it short of the
landing (`helpers.catch_spill`); the routes were tuned before the scatter.

"Designed for" is the robot each Auto was written for, on normal tiles / tiles with 3× the friction.
"One launcher" is the robot the build team is building now (4 Oct): one two-wheel launcher for both
pieces and an 18 in intake (the simulator's "spring hood, full-width intake"), normal tiles.

| Match | Script | Auto (files) | Designed for | One launcher | Needs (as designed) |
|---|---|---|---|---|---|
| Partner fires its preloads | `three_tip_adaptive.py` | PartnerShoots-ThreeTip (`three-tip-adaptive`) | 76 / 76 | **59** | two spring hoods, front intake, webcam pickup |
| Partner fires its preloads from the right start | `right_partner.py` | PartnerShootsRight-LeftTunnel (`left-tunnel`) | 63 / 73 | 52 | catapult, front intake |
| Our two sister robots | `recycle5.py` | Sister-ThrowBack (`recycle5`) | 60 / 49 | 46 | recycle3, and slats that let the left robot's launcher throw straight back |
| Partner can't shoot, stages its preloads | `three_tip_adaptive.py` (`staged=True`) | PartnerStages-ThreeTip (`staged-three-tip`) | 59 / 59 | 56 | two spring hoods, webcam pickup |
| Our two sister robots | `recycle3.py` | Sister-Recycle (`recycle3`) | 58 / 55 | 49 | catapult (triangle cup), 18 in front intake, piece counter, webcam pickup |
| Qualification: we work the HIVE alone, the partner fires its preloads | `solo_tunnel.py` | PartnerShoots-Tunnel (`solo-tunnel`) | 55 / 57 | 56 | two spring hoods, front intake |
| Our two sister robots | `recycle4.py` | Sister-SetDown (`recycle4`) | 52 / 52 | — | not redesigned for the filmed spill: its right robot stages its catch against the wall |

Since the scatter every Auto lost points, the catapult Sister Autos most (76–79 before it): pieces spread
across the field are harder to collect than a tidy pile, and the routes were tuned for one. Only
PartnerShoots-ThreeTip on two launchers held (76). Problems flagged: Sister-ThrowBack's robots touch once
in 20 (27.2 s), and PartnerShoots-Tunnel brushes the far FLOWER in 1 of 20 (6 of 20 on one launcher):
when time runs short against the FLOWER, the endgame guard parks it from there.

The simulated robots always know the HIVE's state: `LeftCellUp`, `RightCellUp` and `Tip` read the
simulated HIVE directly, whichever way the robot faces. A real robot gets the same answers from the
CELLs' AprilTags (`vision/HiveTracker`). Of the names these Autos use, the robot's `AutoRegistration`
has only `Tip` so far: `LeftCellUp`, `RightCellUp`, `IntakeFull`, `Empty`, `SpinUp`, `LaunchAll`,
`CollectSeen` and `SetDown` exist only in the simulator until they are built on the robot.

## The Flat Intake baselines (superseded 6 Oct 2026 21:15 UTC)

`qual-right-o3`, `qual-stages-angled`, `qual-stages-wall` (`qual_right.py`): the three qualifier Autos on the
Flat Intake, the baseline robot with no spill guide, as of 6 Oct 2026 13:40 UTC (54.8 / 51.6 / 47.3 points, 60
runs). The mentor then made the Rigid V the one robot (`doc/unified-design.md`); the baselines are the V's,
`qual-right-v`, `qual-stages-angled-v`, `qual-stages-wall-v` (`baselines_v.py`). The Flat Intake Autos stay
exported and runnable for comparison; their logs on `sim-results` are the last published for them.

