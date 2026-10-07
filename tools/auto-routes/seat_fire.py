"""ShootsRight firing from the extractor's seat (mentor, 6 Oct 2026: "the robot shoots while extracting"; one Auto first,
the rest if it works). qual-right-v-seatfire is the baseline qual-right-v with qual_right.SEAT_FIRE: at each FLOWER the
robot streams shots while the extractor feeds, instead of taking 4 and driving to the firing spot; from the wall FLOWER
it goes round into the GARDEN directly (the sweep along y 10 made no sense from there).

    python3 seat_fire.py [runs]     writes qual-right-v-seatfire into experiments/, then studies it against qual-right-v
"""
import sys
import autogen
import baselines_v
import qual_right

# Two endings (qual_right.SEAT_FIRE): "catch", to N_FIRE at once after TIP 2 to catch its spill there as the baseline
# does; "west", down the west side to the wall FLOWER, fired from its seat, then the GARDEN.
VARIANTS = {"qual-right-v-seatfire-catch": "catch", "qual-right-v-seatfire-west": "west"}

if __name__ == "__main__":
    runs = int(sys.argv[1]) if len(sys.argv) > 1 else 60
    for name, mode in VARIANTS.items():
        qual_right.SEAT_FIRE = mode
        try:
            r = baselines_v.build_for_v(baselines_v.BASELINES["qual-right-v"], name)
        finally:
            qual_right.SEAT_FIRE = False
        r.folder = autogen.EXPERIMENTS
        r.write()
        print("wrote", name)
    if runs:
        autogen.study(";".join(f"{qual_right.cls(n)},{baselines_v.PARTNER['qual-right-v']}@50" for n in VARIANTS),
                      runs=runs, designs="rigid V",
                      extra_env={"BIOBUZZ_AUTO_PARTNER_DESIGN": "spring hood", "BIOBUZZ_AUTO_PARTNER_SPEED": "40"})
