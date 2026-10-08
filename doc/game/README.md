# BIOBUZZ game documents

| File | What | Source |
|---|---|---|
| `biobuzz-competition-manual-TU03.pdf` | 2026-2027 FIRST Tech Challenge Competition Manual, BIOBUZZ, **version TU03** (173 pages, dated 1 Oct 2026) | <https://ftc-resources.firstinspires.org/ftc/game/manual>, fetched 8 Oct 2026 |

When FIRST publishes a new Team Update, add the new version next to it (keep the old one) and update this table.
The official copy is always the one at the link above.

## Field facts the code depends on

- **The field is a 180° rotation between the alliances, not a mirror.** §9.3: the GARDENs are "in opposite
  corners of the FIELD", and §10 (match setup) places GARDEN POLLEN "in the corner closest to the ALLIANCE AREA
  and contacting the audience or rear perimeter wall" (red on one, blue on the other). This is why the Auto
  Builder's exports run on the other alliance with `PoseFactory.mirrorAroundPoint(70.75, 70.75)` (a half turn
  about the field centre).
- So **left/right as the drive team sees it is the same on both alliances**, while AUDIENCE/SCORING swap: the
  end of the field a red Auto starts at by the AUDIENCE CELL is, for blue, the end by the SCORING (rear) CELL.
- §9.5: "the red ALLIANCE AREA is located on the left from the primary audience viewing direction."
- §9.2: the field is "approximately 144 in. by 144 in." nominal; we use 141.5 in, wall face to wall face
  (`util/FieldFrame`), because that is what the field measures.
- §9.6: one HIVE Structure in the centre, a red and a blue HIVE, each with 2 CELLS; AprilTag clusters on each
  CELL's bottom face (§9.6, tag IDs listed by CELL and audience side).
