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
    "angled": ("B", "chase", {**V3, "third": False, "garden": "two", "settle": False, "extra": 1300, "n_fire_y": 119}),
    "wall": ("A", "west", {**V3, "third": False, "garden": "two", "park": False, "settle": False, "extra": 1300, "n_fire_y": 119}),
}
SHAPES = {  # shape (its Autos' file suffix): (robot design, hook spot or None)
    "plain": ("flat intake", None),
    "rigid-v": qual_shapes.O3_SHAPES["qual-right-o3-rigid-v"],
    "small-hook": qual_shapes.O3_SHAPES["qual-right-o3-small-hook"],  # the Ramp Hook
}
PARTNERS = {"right": "PartnerPreloadsRightAuto", "angled": "PartnerAngledParkAuto", "wall": "PartnerStage19SideParkAuto"}
# The intake options on the Flat Intake's body (AutoStudyTest.intakeOptions; each also exists with ", rigid V").
INTAKES = ["DHS CAD intake (6 Oct)", "DHS CAD intake, roller at 2.84 in", "DHS CAD intake, roller at 2.4 in", "DHS CAD intake, 14 in roller",
           "DHS CAD intake, vectored 9.4 in", "DHS CAD intake, vectored 14 in"]


def autos(shape):
    """{kind: (Auto name, class)} for the shape: the baselines for "plain", else its own routes."""
    if shape == "plain":
        names = {"right": "qual-right-o3", "angled": "qual-stages-angled", "wall": "qual-stages-wall"}
    else:
        names = {"right": f"qual-right-o3-{shape}", "angled": f"qual-stages-angled-{shape}", "wall": f"qual-stages-wall-{shape}"}
    return {k: (n, qual_right.cls(n)) for k, n in names.items()}


def stages_for(shape, kind, name):
    partner, plan, kw = STAGES[kind]
    if shape == "rigid-v" and kind == "wall":
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
    only_intakes = "intakes" in sys.argv
    write_all()
    for shape, (design, _) in ({} if only_intakes else SHAPES).items():
        specs = ";".join(f"{cls},{PARTNERS[kind]}@50" for kind, (_, cls) in autos(shape).items())
        for f in autogen.FRICTIONS:
            autogen.study(specs, runs=runs, designs=design,
                          extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                                     "BIOBUZZ_AUTO_FRICTION": f})
    # The intake study (issue #160, doc/intake-design.md): the 6 Oct CAD's intake and its options
    # (AutoStudyTest.intakeOptions), through the plain Autos, and the Rigid V ones for the ", rigid V" designs.
    # `python3 shape_matrix.py 60 intakes` runs only these.
    if only_intakes:
        for shape in ("plain", "rigid-v"):
            specs = ";".join(f"{cls},{PARTNERS[kind]}@50" for kind, (_, cls) in autos(shape).items())
            designs = "|".join(d + (", rigid V" if shape == "rigid-v" else "") for d in INTAKES)
            autogen.study(specs, runs=runs, designs=designs,
                          extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40",
                                     "BIOBUZZ_AUTO_FRICTION": "1"})
