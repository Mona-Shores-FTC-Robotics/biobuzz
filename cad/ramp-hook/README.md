# Ramp hook: printed parts on a stock rod

The FLOWER block for the ramp hook (`doc/ramp-hook.md`), threaded with curtain clips on one straight rod across
the hook's front. Nothing here has been fitted to a robot or a real FLOWER yet: print, test on Thursday, adjust
the numbers at the top of `parts.py`, and print again.

![The parts on the rod, and the block](parts.png)

## What's here

| File | What it is | Print |
|---|---|---|
| `ramp_block.stl` | The FLOWER block: 1.4 in front to back, top 1.3 in above the tiles, curved front | 1 |
| `curtain_clip.stl` | Slides on the rod and holds a curtain panel upright in a slot | 4 |
| `end_block.stl` | Bolts to the inside of the side wall and holds the rod's end | 2 |
| `fit_coupon.stl` | Four short bores at 0.1 mm steps, to find your printer's fit on the rod | 1, first |
| `parts.py` | Makes the STLs. Every size is a constant at the top | `pip install trimesh manifold3d shapely` |
| `render.py` | Draws the picture above | |

## Why it's shaped like this

- **The curved front** has a 34.5 mm radius, just inside the bottom ring's 2.79 in hole (manual, Fig 9-12). The
  two grey uprights stand at the back of the hole, with their inside corners about on its circle. So the curve
  nests between them: driving in, it touches both corners, which **centres the robot sideways and stops it at the
  right depth.**
- **The side section** is the one `tools/ramp-hook/ramp.py` likes best against the manual's FLOWER: a front face
  up to 1.3 in, a 0.5 in flat top, then a slope down toward the robot. Driven against the uprights, it emptied all
  4 POLLEN in every bounce guess, even stopped 0.15 in short. A taller front (1.6 in or more) shoves the bottom
  POLLEN back instead of lifting it. Under about 1.25 in front to back, the model loses the last POLLEN back into
  the pocket. That's a 2-D model, so try a shorter block on Thursday too.
- **The rod runs through the block,** 17.6 mm back from the tip, not behind it. The POLLEN roll out over the
  block's back edge, so nothing may stand higher than that edge behind it.
- **The bottom sits 0.5 in above the tiles,** 0.07 in over the bottom ring. If it drags on foam tiles, raise
  `BOTTOM` to 0.55 or 0.6 in, and raise `ROD_Z` by the same amount. Keep `TOP` at 1.3 in.

## Buy

| Item | Notes |
|---|---|
| 1/4 in steel or aluminium rod, about 14 in | Any straight round rod. For a 6 mm rod or shaft, set `ROD_D = 6.0` and re-run `parts.py` |
| 1/16 in polycarbonate, two panels about 4.9 × 2.35 in (their tops 3.5 in above the tiles, under the FLOWER's 3.55 in bracket) | The curtains, either side of the block. Cut to fit |
| M3 screws: 2 short set screws for the block, 1 per clip, plus 8 to clamp the panels | Self-tapping into the printed holes |
| M4 screws, 4, through the side wall into the end blocks | Holes are on a 16 mm pitch (an 8 mm grid). Drill the wall to suit |
| 2 shaft collars for the rod, optional | Locate the block and clips without relying on set screws |

## Print

- **Fit coupon first.** Slide the rod into its four bores and pick the one that's snug but slides. Change `FIT` in
  `parts.py` by the step that fitted (the bores are `FIT` − 0.1, `FIT`, + 0.1, + 0.2) and re-run.
- **Material:** PETG (tough, a little give) or nylon. PLA cracks when the robot drives into the uprights.
- **Settings:** 0.2 mm layers, 5 walls, 40 % infill, 5 top and bottom layers. Wall count matters more than infill
  around the rod.
- **Orientation:** every STL is already flat side down. The block prints on its flat bottom with no supports. The
  bore is horizontal, so it will be a little oval at the top: the coupon tells you how much.

## Assemble

1. Screw the two end blocks to the inside of the side wall and the left end of the hook's frame, with the bores
   facing each other at the same height.
2. Thread the rod through one end block, two clips, the ramp block, two more clips, and into the other end block.
3. Centre the block across the hook, or line it up with the intake's middle, and tighten its two set screws from
   underneath. Shaft collars either side of it are better than set screws alone.
4. Slide the curtain panels into the clips' slots, square them up, and screw through the clip's two holes.
5. Check the height with the hook down on a tile: the block's bottom should be 0.5 in above the tiles, and its top
   1.3 in.

## Test on Thursday

1. Before anything else, measure how far the bottom POLLEN's back sits from the hole's front edge, and how far the
   uprights' inside corners are apart. If the curve doesn't seat between them, change `ARC_R`.
2. Push the block in by hand with 4 POLLEN staged, 5 times, and film it. Count the POLLEN that come out.
3. Print a shorter block too (set `DEPTH = 1.0 * IN`) and compare. The model says 1.4 in, but it's 2-D and has
   never met a real wiffle ball.
