# The FLOWER stick page

The page at https://claude.ai/artifact/7aVCkSsMtQMnwm9SBgDyn2 is built from this folder plus the module's STLs and
photos. `build_page.py` is the current builder (it imports the two earlier ones for their CSS and viewer script):

- `sim.js`: the 2-D simulator in the browser, a port of `tools/ramp-hook/ramp.py` with the stall check and the soft
  Gecko; `simlib.js` is the same for node. `opt.js` is the design search that produced v5; `opt_all.jsonl` its results.
- `anim2.js`: the side view over `flower_section.json` (FIRST's field CAD cut at the centreline) and
  `robot_section.json` (the mentor's 16 Oct robot cut at the centreline, by `section.py`).
- `drive3d.js`: the same run in three.js; `flower3d.b64` is the simplified FLOWER mesh it uses.
- `v5.json`: the searched shape and its captions.

The builder expects, in the same folder: base64 of each STL it shows (`<name>.b64`, made with `base64 -w0`), the two
test photos as `ph.b64` and `frames.b64` (from `../test-2026-10-15/`), the sketch as `sk.b64`, and four cropped
screenshots of other teams' public videos in `clips/` (not committed). Then `python3 build_page.py` writes
`flower-stick.html`, which is published as the artifact. Anyone with edit access to the artifact can republish it
from their own Claude session; the sources here are what to change.
