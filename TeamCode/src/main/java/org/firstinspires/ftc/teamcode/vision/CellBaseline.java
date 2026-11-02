package org.firstinspires.ftc.teamcode.vision;

import org.firstinspires.ftc.teamcode.util.Alliance;

import java.util.EnumMap;
import java.util.Map;

/**
 * Where each HIVE CELL was last known to rest: the camera's last settled UP or DOWN, remembered
 * through the gaps when the camera does not see it (a CELL out of view reads UNKNOWN).
 *
 * <p>At an Autonomous's PLAY the match-start state is known by rule, so {@link #seedMatchStart()}
 * sets all four: each alliance's LEFT CELL DOWN and its RIGHT CELL UP ({@link HiveCell#leftCell},
 * {@link HiveCell#rightCell}). A settled observation always replaces whatever is held, seed or not.
 * Until seeded or seen, a CELL is UNKNOWN.
 *
 * <p>The {@code Tip} trigger compares the camera's settled state against this, so a TIP that
 * happens while a CELL is out of view, or before the camera has settled it once, still counts
 * (31 Oct 2026: L-Simple on blue read BLUE AUDIENCE as UNKNOWN at PLAY, and its TIP never fired).
 *
 * <p>Pure: no hardware, so a test drives it directly.
 */
public final class CellBaseline {

    private final Map<HiveCell, HiveCellState> known = new EnumMap<>(HiveCell.class);

    public CellBaseline() {
        clear();
    }

    /** Every CELL in its match-start state: each alliance's LEFT DOWN, RIGHT UP. */
    public void seedMatchStart() {
        for (Alliance alliance : new Alliance[]{Alliance.RED, Alliance.BLUE}) {
            known.put(HiveCell.leftCell(alliance), HiveCellState.DOWN);
            known.put(HiveCell.rightCell(alliance), HiveCellState.UP);
        }
    }

    /** The camera's state for {@code cell} this tick: UP or DOWN replaces what is held; UNKNOWN keeps it. */
    public void observe(HiveCell cell, HiveCellState state) {
        if (state == HiveCellState.UP || state == HiveCellState.DOWN) known.put(cell, state);
    }

    /** Where {@code cell} was last known to rest, or UNKNOWN if never seeded or seen. */
    public HiveCellState known(HiveCell cell) {
        return known.get(cell);
    }

    /** Forgets everything: every CELL UNKNOWN. */
    public void clear() {
        for (HiveCell cell : HiveCell.values()) known.put(cell, HiveCellState.UNKNOWN);
    }
}
