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
to about `triangle_budget` triangles (600,000, about 7 MB) so AdvantageScope stays quick on a laptop.

## The frame

AdvantageScope's, as TeamCode's logs use it: metres, +X forward, +Y left, +Z up. The origin is on the floor under the
middle of the four drive axles, which the script finds each run from the four Wheel Assembly shafts. The floor is
48 mm (a 96 mm mecanum's radius) below the axles, because the CAD has the wheels' hubs but not the wheels. If the
assembly is ever turned so the axles no longer run along its x, the script stops and says so.

## What's not right yet

- **No moving parts.** The next stages are the turret turning and the flower stick moving, each as a component
  (`model_0.glb` …) cut out of the same assembly by name, with the logs writing their poses.
- **The drive wheels aren't drawn**: the CAD has their hubs and shafts only.
- **The camera** in `config.json` is the old hand-measured one (5.58 in ahead, 14.25 in up, pitched 45°). The CAD's
  Limelight 3A sits about 2 in further back (X 2.7 to 4.4 in, Z 14.0 to 15.7 in). Reading its exact pose from the CAD
  is a small next step.

## The old model

`build_model.py` built the previous model from a hand-exported STEP of the mentor's CAD plus the team's own drawings
of the floating-roller intake and the transfer, with eight components. It needs the CAD tooling on the simulator
branch (`claude/simulator`) and is kept for that branch.
