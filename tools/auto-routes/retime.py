"""The three Rigid V baselines retimed for the transfer's 0.25 s shots (mentor, 6 Oct 2026): the wait for each spill
to land, which baselines_v times for the 0.45 s launcher. Variants of each baseline with that wait changed, into
experiments/:

    qual-right-v-x<ms>            ShootsRight's wait after TIP 2 settles (baseline 500)
    qual-stages-angled-v-x<ms>    the TIP-timed wait for TIP 2's spill (baseline 1300)
    qual-stages-wall-v-x<ms>      the same, the wall partner (baseline 1300)

    python3 retime.py      writes them; DeepDive runs them on "rigid V, transfer"
"""
import autogen
import baselines_v
import qual_right
import shape_matrix

WAITS = {"qual-right-v": (200, 800, 1100), "qual-stages-angled-v": (700, 1000, 1600), "qual-stages-wall-v": (700, 1000, 1600)}


def with_wait(base, ms):
    if base == "qual-right-v":
        table, key = qual_right.O3, "qual-right-o3-sweep"
        old = table[key]
        table[key] = {**old, "extra": ms}
        restore = lambda: table.__setitem__(key, old)
    else:
        kind = "angled" if "angled" in base else "wall"
        old = shape_matrix.STAGES[kind]
        shape_matrix.STAGES[kind] = (old[0], old[1], {**old[2], "extra": ms})
        restore = lambda: shape_matrix.STAGES.__setitem__(kind, old)
    return restore


ROUTES = {f"{b}-x{ms}": (b, ms) for b, waits in WAITS.items() for ms in waits}

if __name__ == "__main__":
    for name, (base, ms) in ROUTES.items():
        restore = with_wait(base, ms)
        try:
            r = baselines_v.build_for_v(baselines_v.BASELINES[base], name)
        finally:
            restore()
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(name)
