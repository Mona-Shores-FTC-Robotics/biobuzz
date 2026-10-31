# The robot for AdvantageScope

`Robot_BIOBUZZ/` is the robot drawn from the team's Onshape CAD, for match logs and the simulator's logs in
AdvantageScope (`tools/advantagescope/` installs it). Pick "BIOBUZZ Robot" in a 3D tab.

## Building it from Onshape

```bash
pip install numpy trimesh fast_simplification
ONSHAPE_ACCESS_KEY=... ONSHAPE_SECRET_KEY=... python3 cad/advantagescope/from_onshape.py
```

The keys are an Onshape API key pair (Onshape → My account → Developer → API keys) that can read the robot document.
The script only reads: nothing in the document changes. It takes the **Robot** assembly as Onshape shows it:

- **Hidden or suppressed instances are left out**, the way the mentor sees them. Old launchers he has hidden stay out.
- **`onshape_model.json`'s `drop` list leaves out anything else**, matched by path (`Chassis <1> / Intake <1> / …`).
  Game pieces staged in the CAD are dropped there. Leaving a stray part out of the model is one line in this list;
  the CAD stays as it is.
- **Each run says what changed in the CAD since the last build** (`onshape_source.json`): new, removed, hidden or
  shown top-level instances. Look at what it lists, then add to `drop` or keep it, and commit the model, the list and
  `onshape_source.json` together.

Parts are cached in `~/.cache/biobuzz-onshape`, so a rebuild after a small change takes seconds. The model is thinned
to about `triangle_budget` triangles (600,000, about 9 MB with the normals AdvantageScope needs) so AdvantageScope stays quick on a laptop.

## The frame

AdvantageScope's, as TeamCode's logs use it: metres, +X forward, +Y left, +Z up. The origin is on the floor under the
middle of the four drive axles, which the script finds each run from the four Wheel Assembly shafts. The floor is
48 mm (a 96 mm mecanum's radius) below the axles, because the CAD has the wheels' hubs but not the wheels. If the
assembly is ever turned so the axles no longer run along its x, the script stops and says so.

## Moving parts

Each entry in `onshape_model.json`'s `components` becomes `model_<index>.glb`, cut out of the same assembly:

- **`parts`**: the paths that move with it. A component that matches nothing stops the build.
- **`axis_part`** and **`axis`**: the one part it turns about, and that part's thinnest direction (a bearing race)
  or longest (a shaft), through its centre.
- **`angle`**: what the logged angle means. **`drawn_at_deg`**: that angle as the CAD draws it.

The script writes the axes to `Robot_BIOBUZZ/components.json`. TeamCode's `logging/RobotComponents` holds the same
numbers (the robot can't read this folder), and `RobotComponentsTest` fails while the two differ. So after a rebuild
that moves an axis, copy the new numbers into `RobotComponents`. The match log writes the poses under
`/Internals/Components`, the key the simulator writes too, and the layout's 3D tabs show them.

| # | Component | Angle | Turns about |
|---|---|---|---|
| 0 | Turret: the 176T gear, the bearing's inner race and the hood on them | `TurretSubsystem.angleDegrees()`, counter-clockwise from home (the nose) | The turntable bearing's centre, vertical |
| 1 | FLOWER stick: the two arms, the 312 mm shaft across their tops, `Part 4` and the bearings on them | `FlowerStickSubsystem.commandedDegrees()`: 0 in, 162 out | The SWYFT servo's hub and bearing <1>, left to right, 6.1 in ahead of the axles' middle, 7.3 in up |

**The turret is drawn at 51.5°.** In the CAD the hood faces 51.5° left of the nose (the big flat faces of the hood
and its channel point there, and the ball leaves on that open side, away from the tall wall). So at 0, home, the
model turns it back 51.5° to face forward. If the CAD is ever redrawn facing forward, `drawn_at_deg` becomes 0.

**The FLOWER stick is drawn stowed**, leaning up and back over the robot. Out, it has swung 162° forward and down, and
its top shaft lies on the tile, where the stick's wheels rest. Nothing measures the stick, so the log shows what it
was commanded to do: it jumps out at the request, while the real stick takes about a second.

## What's not right yet

- **The intake's float doesn't move.** `Flower Poker Mid` is a permanent part of the intake: it rides up and down on
  the Viper slides as pieces come in. Nothing on the robot measures that height, so the log can't show it and the
  model draws it at its CAD height. In the CAD it has no mates yet; fastening it to the sliding carriage keeps the CAD
  true, and the model is unchanged. A sensor on the slides would let it become a sliding component later.
- **The drive wheels aren't drawn**: the CAD has their hubs and shafts only.
- **The camera** in `config.json` is the old hand-measured one (5.58 in ahead, 14.25 in up, pitched 45°). The CAD's
  Limelight 3A sits about 2 in further back (X 2.7 to 4.4 in, Z 14.0 to 15.7 in). Reading its exact pose from the CAD
  is a small next step.

## The old model

`build_model.py` built the previous model from a hand-exported STEP of the mentor's CAD plus the team's own drawings
of the floating-roller intake and the transfer, with eight components. It needs the CAD tooling on the simulator
branch (`claude/simulator`) and is kept for that branch.
