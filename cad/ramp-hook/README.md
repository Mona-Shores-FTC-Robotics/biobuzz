# Ramp hook: printed parts on a goBILDA 8 mm shaft

The FLOWER block for the ramp hook (`doc/ramp-hook.md`), threaded with curtain clips onto one straight 8 mm shaft
across the hook's front. The ends bolt onto goBILDA structure on its 8 mm grid. Nothing here has been fitted to a
robot or a real FLOWER yet: print, test on Thursday, adjust the numbers at the top of `parts.py`, and print again.

![The parts on the shaft, and the block](parts.png)

## What's here

| File | What it is | Print |
|---|---|---|
| `ramp_block.stl` | The FLOWER block: 1.4 in front to back, 0.7 to 1.35 in above the tiles, curved front | 1 |
| `curtain_clip.stl` | Slides on the shaft and holds a curtain panel upright in a slot | 4 |
| `end_block.stl` | Bolts to goBILDA channel (M4, 16 mm apart) and holds the shaft's end | 2 |
| `fit_coupon.stl` | Four short bores at 0.1 mm steps, to find your printer's fit on the shaft | 1, first |
| `parts.py` | Makes the STLs. Every size is a constant at the top | `pip install trimesh manifold3d shapely` |
| `render.py` | Draws the picture above | |

## Why it's shaped like this

- **The curved front** has a 34.5 mm radius, just inside the bottom ring's 2.79 in hole (manual, Fig 9-12). The
  two grey uprights stand at the back of the hole, with their inside corners about on its circle. So the curve
  nests between them: driving in, it touches both corners, which **centres the robot sideways and stops it at the
  right depth.**
- **The side section:** a front face up to 1.35 in, a 0.5 in flat top, then a slope down toward the robot. Driven
  against the uprights in `tools/ramp-hook/ramp.py`, it emptied all 4 POLLEN in every bounce guess, even stopped
  0.15 in short.
  - A taller front (1.6 in or more) shoves the bottom POLLEN back instead of lifting it.
  - Under about 1.25 in front to back, the model loses the last POLLEN back into the pocket.
  - That's a 2-D model, so try a shorter block on Thursday too.
- **The bottom is 0.7 in above the tiles,** 0.27 in over the bottom ring's 0.43 in, so it won't catch the lip. The
  model empties every case with the bottom anywhere from 0.6 to 0.85 in.
- **The shaft runs through the block,** 12 mm behind the tip, under the flat top, halfway up (1.02 in above the
  tiles). It can't run behind the block, because the POLLEN roll out over the block's back edge. Where the bare
  shaft passes the uprights, it's about 4 mm clear of their faces.

## Buy (goBILDA, or any 8 mm shaft)

Check part numbers and lengths on gobilda.com; these are the kinds of parts, not a parts list.

| Item | Notes |
|---|---|
| goBILDA 8 mm REX shaft, about 14 in (350 mm or longer, cut) | Its rounded corners sit on an 8 mm circle, so it slides in the round bore, and the set screws bite on a flat. Any 8 mm round shaft works too |
| 2 to 4 goBILDA clamping collars for 8 mm REX | Either side of the block: they locate it sideways. Better than set screws alone |
| goBILDA U-channel (or the robot's own side-wall channel) | The end blocks bolt to it. One end on the side wall; the other on a short channel stub at the hook's left front corner |
| M4 socket screws and nylock nuts, 4 | Through the channel into the end blocks. The holes are 16 mm apart, which lines up with goBILDA's 8 mm grid |
| M3 screws: 2 short set screws for the block, 1 per clip, plus 8 to clamp the panels | Self-tapping into the printed holes |
| 1/16 in polycarbonate, two panels about 4.9 × 2.2 in | The curtains, either side of the block. Their tops are 3.5 in above the tiles, under the FLOWER's 3.55 in bracket |

## Print (send your friend this section and the STLs)

- **Fit coupon first.** Slide the shaft into its four bores and pick the one that's snug but slides. Change `FIT` in
  `parts.py` by the step that fitted (the bores are `FIT` − 0.1, `FIT`, + 0.1, + 0.2) and re-run. Or have your
  friend scale the bore in the slicer.
- **Material:** PETG (tough, a little give) or nylon. PLA cracks when the robot drives into the uprights.
- **Settings:** 0.2 mm layers, 5 walls, 40 % infill, 5 top and bottom layers. The block's walls around the shaft are
  about 4 mm, so wall count matters more than infill.
- **Orientation:** every STL is already flat side down. The block prints on its flat bottom with no supports. The
  bore is horizontal, so it will be a little oval at the top: the coupon tells you how much.
- **Units:** the STLs are in millimetres. If the block comes in at 1/25 or 25 times the size, the slicer guessed
  inches.

## Assemble

1. Bolt one end block to the inside of the side wall and the other to a short channel stub at the hook's left front
   corner, bores facing each other at the same height (1.02 in above the tiles with the hook down).
2. Thread the shaft through one end block, two clips, a collar, the ramp block, a collar, two more clips, and into
   the other end block.
3. Line the block up with the middle of the intake. Tighten the collars against it and the set screws from
   underneath.
4. Slide the curtain panels into the clips' slots, square them up, and screw through the clip's two holes.
5. Check with the hook down on a tile: the block's bottom should be 0.7 in above the tiles, and its top 1.35 in.

## Test on Thursday

1. Before anything else, measure how far apart the uprights' inside corners are. If the curve doesn't seat between
   them, change `ARC_R`.
2. Push the block in by hand with 4 POLLEN staged, 5 times, and film it. Count the POLLEN that come out.
3. Print a shorter block too (set `DEPTH = 1.0 * IN`) and compare. The model says 1.4 in, but it's 2-D and has
   never met a real wiffle ball.
