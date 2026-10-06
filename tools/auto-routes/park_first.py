"""ShootsRight with PARK first (mentor, 6 Oct 2026: in quals PARK is non-negotiable).

qual_right.third_load, when TIP 3 hasn't come 0.8 s after the GARDEN's shots, goes back to the GARDEN for a second
load. The endgame guard never cuts that branch (it is the route's last card), so the robot misses PARK in every run
TIP 3 is late: 25 of 60 on the Rigid V, and the second load never made TIP 3 in those runs. These routes PARK instead;
shots already away can still TIP (Competition Manual §10.5: a TIP finishing in the 8 s after AUTO counts).

    python3 park_first.py      writes qual-right-o3-rigid-v[-18-30, -20-45]-park, into experiments/
"""
import autogen
import qual_right
import qual_shapes

_third = qual_right.third_load


def park_first(r, tag="", wait_full=1100, catch3=False, tip_ms=0):
    r.at = "S_FIRE"
    if not tip_ms:
        return qual_right.go_park(r)
    park_now = qual_right.go_park(r)
    r.at = "S_FIRE"
    park_late = qual_right.go_park(r)
    return [r.wait(f"TIP 3 coming?{tag}", when=["Tip"], ms=tip_ms, yes=park_now, no=park_late,
                   yes_label="TIP 3: PARK", no_label="Not yet: PARK anyway")]


# Only the Rigid V's routes reach third_load (qual-right-o3-sweep); the Flat Intake's and the Ramp Hook's qual-right-o3
# ("first") end with a path toward PARK instead.
ROUTES = {n + "-park": n for n in ("qual-right-o3-rigid-v", "qual-right-o3-rigid-v-18-30", "qual-right-o3-rigid-v-20-45")}

if __name__ == "__main__":
    qual_right.third_load = park_first
    try:
        for name, shaped in ROUTES.items():
            qual_shapes.ROUTE_OF[name] = qual_shapes.ROUTE_OF[shaped]
            r = qual_shapes.o3_shaped(name, qual_shapes.O3_SHAPES[shaped][1])
            r.folder = autogen.EXPERIMENTS
            r.write()
            print(name)
    finally:
        qual_right.third_load = _third
