# The Auto baseline (15 Oct 2026)

The mentor's decision, 15 Oct: a small, reasonable baseline for the first robot. Others can come back later, but these
are what we build, test and tune first.

## The five Autos

| Auto | Class | What it is for | Published (60 runs, G409 touch runs) |
|---|---|---|---|
| **L-Quals** | `LQualsAuto` (`TeamCode/autos/l-quals.pp`) | Qualifiers, from the left start, partner shoots reliably | 73.7, 3 TIPs in 55, 0 |
| **R-Quals** | `RQualsAuto` (`r-quals.pp`) | Qualifiers, from the right start, partner can't shoot or might not | 72.7, 3 TIPs in 54, 1 |
| **Sister** | `SisterRightFixedAuto` + `SisterLeftFixedAuto` | Playoffs with the sister team: two of our robots | 89.0, 4 TIPs in 45, 0 |
| **L-Simple** | `LSimpleAuto` (`l-simple.pp`) | Path testing: left start, preloads at the left CELL, PARK | LEAVE and PARK 60, all 4 preloads 54 (the turret still slewing when the partner tips the CELL in the rest), 0 |
| **R-Simple** | `RSimpleAuto` (`r-simple.pp`) | Path testing: left start, waits for TIP 1, preloads, the far FLOWER's 4, PARK | LEAVE and PARK 60, the FLOWER's 4 every run, all 8 fired 57, 0 |

Which qualifier Auto to run (mentor, 8 Oct): partner can't shoot → R-Quals; shoots reliably → either; says it shoots
but might not → R-Quals. None of the five needs the turret. L-Simple and R-Simple both start from the left start
(the names are the mentor's; R-Simple is the one that waits for TIP 1 and takes the far FLOWER).

The partners they're simulated with: `PartnerPreloadsRightHighAuto` (L-Quals), `PartnerLeftVAuto` (R-Quals) and
`PartnerPreloadsRightSouthAuto` (the simple Autos, which park at the LOADING ZONE's north end, so the partner takes the
south end). The simple Autos' numbers are from `tools/auto-routes/batch` (both robots rigid V at 50 in/s). The whole-match row (L-Quals against R-Quals) stays as a check, not an Auto to run.

## Retired, and why

Kept in git history; any can come back if what killed it changes.

| Auto | Why |
|---|---|
| **Stages, angled and wall** (`qual-stages-*-v`) | Built for a partner that can't shoot, which R-Quals already handles and scores about 19 points more (72.7 against 53-54). The angled Auto's third TIP came from catching TIP 1's spill close in, which G409 rules out (`doc/g409-routes.md`): 12 → 3 of 60 once it stayed clear. |
| **ShootsRight, flower first** (`qual-right-v-flower-first-carry-settle-b2`) | Needs the turret; scores below L-Quals for the same partner (68.0 against 73.7); still 8 G409 touch runs after the fixes. |
| **ShootsRight, seat fire** | Needs the turret; shelved 7 Oct (not realistic from the far FLOWER's seat with the extractor as drawn); 12 G409 touch runs, never reworked. |
| **Sister five** (`sister5p-*`) | Its fifth TIP needs a dead front: with the measured 0.4 restitution it makes 5 TIPs in 6 of 60 and scores below Sister (86.9 against 89.0). Back on the table only if a face measures under about 0.1 (`doc/tip1-catch.md`). |
| Everything else in `TeamCode/autos/` and the experiments folder | Earlier body shapes (hooks, walls, wide and short bodies, the 18 in robot, the Flat Intake), research routes and their variants. Superseded by the Rigid V and the qualifier Autos, or by the G409 reading that rules out hooks. |

## What stays in the repo (proposed; nothing deleted yet)

- `TeamCode/autos/`: `l-quals.pp`, `r-quals.pp`, `l-simple.pp`, `r-simple.pp`, `partner-left-v.pp`,
  `partner-preloads-right-high.pp`, `partner-preloads-right-south` (now in the experiments folder), and Sister's two `.pp` (moved in from the experiments folder).
- Their generated classes, and the simulator's own partners and tests.
- `tools/auto-routes/`: the scripts those Autos are built from. L-Quals and R-Quals come through `baselines_v.py`,
  which pulls in `alone, autogen, guide_routes, helpers, park_first, partners, qual, qual_right, qual_shapes,
  shape_matrix`; Sister and the simple Autos need only `alone, autogen, helpers, partners, sister, simple`.
  Untangling `baselines_v.py` to need only `alone.py` is a follow-up.
- `tools/auto-routes/batch/`, and the docs that record what was learned.

Everything else (about 360 experimental `.pp`, their generated classes, about 25 route scripts) goes to an archive
branch, after the mentor approves the list.
