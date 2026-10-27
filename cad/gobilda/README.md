# goBILDA's own CAD for the parts we buy

`parts.py` puts goBILDA's STEP of a SKU into our drawings, so the motor, servo, pulleys, bearings, wheels and collars
are the real parts and not cylinders and boxes. Every goBILDA product page links its STEP, always at

    https://www.gobilda.com/content/step_files/<SKU>.zip

Download the zips for the SKUs a builder names and leave them, as downloaded, in `step/` here (gitignored: they are
goBILDA's files and a few MB each). A builder that finds a SKU's zip draws the real part; one that doesn't draws its
stand-in solid, so a fresh checkout still builds. The intake uses:

| SKU | Part |
|---|---|
| 5203-2402-0005 | Yellow Jacket 5.2:1, 1150 RPM (the roller's motor) |
| 2000-0025-0002 | Dual Mode Servo 25-2, Torque (the FLOWER extractor) |
| 3417-4008-0024 | 24T HTD5 pulley, 8 mm REX bore (two) |
| 1611-0514-4008 | flanged bearing, 8 mm REX bore, 14 mm OD (eight) |
| 3613-4008-0048 | 48 mm gecko wheel, 8 mm REX bore (twenty) |
| 2910-0920-4008 | clamping collar, 8 mm REX bore (eight) |

Belts have no STEP; 8 mm REX shafts are drawn exactly by `rex()` in `cad/robot-addons/build.py` (an 8 mm round with
six 7 mm flats, in goBILDA's stock lengths, `2106-4008-LLL0`).

**Adding a SKU.** Download its zip, then

    python3 cad/gobilda/parts.py <SKU>

prints its parts and their bounding boxes in the STEP's own frame. Read off where the part's axis is and which face is
the reference, and add a line to `AXES`: `origin` (a point on the axis, on the reference face), `axis` (the direction
the part's length runs from there) and `x_ref` (a side direction, for parts that aren't round). A builder then says
`vendor(group, sku, label, point, direction, x_dir, stand-in, colour)`: the reference face lands at `point` and the
part runs along `direction`.

**Size.** goBILDA's STEPs are heavy (the gecko wheel is 16 MB written out, the motor 44 MB), so the committed
`dhs-intake-b.step` keeps the stand-ins and `build.py` writes a second, gitignored `dhs-intake-b-vendor.step` with
the real geometry, each body once and placed by location (about 80 MB). That one goes in the team's Drive, next to
"DHS Robot Copy.step". The AdvantageScope meshes and the whole-robot STEP (`cad/full-robot/`) get the real parts too.
