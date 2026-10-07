"""Every spill guide on the Flat Intake (the baseline robot) on every current Auto (mentor, 6 Oct 2026): no guide,
the Rigid V and the Ramp Hook (simulated as the 8 in hook; qual_shapes.O3_SHAPES), each through Qual-PartnerShootsRight (qual_shapes draws its
route) and both Qual-PartnerStages (drawn here, as qual_right.stages_staged draws the baselines, with a hook's
slide to the hook spot as TIP 2 starts, as qual_shapes does for ShootsRight). 60 runs (the README's count),
through AutoStudyTest, the same runner as the README's baselines.

    python3 shape_matrix.py [runs]

Prints the table for doc/shape-matrix.md.
"""
import sys
import autogen
import qual_right
import qual_shapes

V3 = qual_right.VARIANTS["qual-right-v3"]
STAGES = {  # suffix: (partner, plan, options), as qual_right.STAGES' baselines (6 Oct 2026: TIP-timed wait, TIP 2 from y 119)
    # garden "none" since 7 Oct 2026 (with the dwell before a TIP, issue #167): TIP 2 comes at 20 s and the GARDEN at
    # 26 s, too late to fire; the GARDEN load never made TIP 3 (0 of 60) and the endgame guard's cut-short park from the
    # GARDEN (a path drawn from S_FIRE) turned the V's tips into the west wall and clipped the HIVE frame. PARK straight
    # after TIP 2's spill is fired, as the wall Auto does.
    "angled": ("B", "chase", {**V3, "third": False, "garden": "two", "settle": False, "extra": 1300, "n_fire_y": 119}),
    # park: True since 7 Oct 2026 (mentor: both robots always PARK; the wall partner now parks at y 112, above our spot).
    # The matrix's numbers above were run with the partner on our spot and no PARK for us.
    "wall": ("A", "west", {**V3, "third": False, "garden": "two", "park": True, "settle": False, "extra": 1300, "n_fire_y": 119}),
}
SHAPES = {  # shape (its Autos' file suffix): (robot design, hook spot or None)
    "plain": ("flat intake", None),
    "rigid-v": qual_shapes.O3_SHAPES["qual-right-o3-rigid-v"],
    "small-hook": qual_shapes.O3_SHAPES["qual-right-o3-small-hook"],  # the Ramp Hook
    # The Rigid V's width and angle (mentor, 6 Oct 2026).
    **{f"rigid-v-{w}-{a}": qual_shapes.O3_SHAPES[f"qual-right-o3-rigid-v-{w}-{a}"] for w, a in qual_shapes.RIGID_V_VARIANTS},
}
PARTNERS = {"right": "PartnerPreloadsRightAuto", "angled": "PartnerAngledParkAuto", "wall": "PartnerStage19SideParkAuto"}


def autos(shape):
    """{kind: (Auto name, class)} for the shape: the baselines for "plain", else its own routes."""
    if shape == "plain":
        names = {"right": "qual-right-o3", "angled": "qual-stages-angled", "wall": "qual-stages-wall"}
    else:
        names = {"right": f"qual-right-o3-{shape}", "angled": f"qual-stages-angled-{shape}", "wall": f"qual-stages-wall-{shape}"}
    return {k: (n, qual_right.cls(n)) for k, n in names.items()}


def stages_for(shape, kind, name):
    partner, plan, kw = STAGES[kind]
    if shape.startswith("rigid-v") and kind == "wall":
        # 18 in across its flaps, it doesn't fit the west lane between the HIVE frame's foot bar (x 45) and the
        # parked partner (x 28): 17 in. North through the tunnel instead ("chase").
        plan = "chase"
    hook_at = SHAPES[shape][1]
    kw = dict(kw)
    tail = qual_right.tail
    if hook_at:
        kw["extra"] = kw["extra"] + 500  # as qual_shapes: the hook holds the spill 0.5 s longer

        def hooked(r, **t):
            r.pt("N_HOOK", *hook_at)
            go = r.go("N_HOOK", heading=270)
            r.at = "N_HOOK"
            return [go, *tail(r, **t)]
        qual_right.tail = hooked
    try:
        return qual_right.stages_staged(name, partner=partner, plan=plan, **kw)
    finally:
        qual_right.tail = tail


def write_all():
    for shape in SHAPES:
        if shape == "plain":
            continue
        for kind in STAGES:
            name = autos(shape)[kind][0]
            r = stages_for(shape, kind, name)
            r.folder = autogen.EXPERIMENTS
            r.write()
    for name, (design, hook_at) in qual_shapes.O3_SHAPES.items():
        qual_shapes.o3_shaped(name, hook_at).write()  # into TeamCode/autos, where qual_shapes keeps them


if __name__ == "__main__":
    runs = int(sys.argv[1]) if len(sys.argv) > 1 else 20
    write_all()
    for shape, (design, _) in SHAPES.items():
        specs = ";".join(f"{cls},{PARTNERS[kind]}@50" for kind, (_, cls) in autos(shape).items())
        for f in autogen.FRICTIONS:
            autogen.study(specs, runs=runs, designs=design,
                          extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                                     "BIOBUZZ_AUTO_FRICTION": f})
