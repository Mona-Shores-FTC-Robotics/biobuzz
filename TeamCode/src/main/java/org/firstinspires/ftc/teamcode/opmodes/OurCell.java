package org.firstinspires.ftc.teamcode.opmodes;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveState;

/**
 * Whether the CELL at the robot's end of the field, the one the TeleOp aims at (a CELL is scored
 * only from its own end), is up, as the camera last saw it settled: for the Match page, so a robot
 * that owns one end (doc/drive-team-cycle-playbook.md) knows whether to shoot or to load and wait.
 * The HIVE's state comes from {@code HiveTracker}: {@link HiveState#UNSEEN} out of view, and a TIP in
 * progress away from this CELL's up position reads DOWN (it is on its way down).
 *
 * <p>Pure, so a test drives it.
 */
final class OurCell {

    enum Reading { UP, DOWN, UNKNOWN }

    private OurCell() {}

    /**
     * @param cell     the CELL at the robot's end (the turret's aimed CELL, else the near-end one); null: unknown
     * @param alliance ours
     * @param state    {@code HiveTracker.state()}
     * @param settled  {@code HiveTracker.position()}: where it last rested, for a TIP in progress
     */
    static Reading of(HiveCell cell, Alliance alliance, HiveState state, HiveState settled) {
        if (cell == null || state == null || !cell.belongsTo(alliance)) return Reading.UNKNOWN;
        HiveState up;
        if (cell == HiveCell.leftCell(alliance)) {
            up = HiveState.LEFT_CELL_UP;
        } else if (cell == HiveCell.rightCell(alliance)) {
            up = HiveState.RIGHT_CELL_UP;
        } else {
            return Reading.UNKNOWN;
        }
        if (state == up) return Reading.UP;
        if (state.settled()) return Reading.DOWN;
        if (state == HiveState.TRANSITION && settled == up) return Reading.DOWN;
        return Reading.UNKNOWN;
    }

    /**
     * How long the Match page keeps showing the last reading once the camera loses the HIVE, seconds.
     * Our HIVE was seen settled in only 25 to 35% of a Fruitport TeleOp's loops (21 Nov 2026), so
     * without it the line would read ? most of the time. A TIP can happen unseen, so it is short.
     */
    static final double MEMORY_S = 8.0;

    /**
     * The Match page's line, huge: seen now, green UP or amber DOWN; not seen now but seen within
     * {@link #MEMORY_S}, the last reading plain with its age; otherwise ?.
     *
     * @param now      this loop's reading
     * @param lastSeen the last UP or DOWN reading, or UNKNOWN
     * @param ageS     seconds since {@code lastSeen}; NaN when none
     */
    static String line(Reading now, Reading lastSeen, double ageS) {
        if (now == Reading.UP) return Display.huge("OUR CELL UP", Display.Level.OK);
        if (now == Reading.DOWN) return Display.huge("OUR CELL DOWN: LOAD 4", Display.Level.WARN);
        if (lastSeen != Reading.UNKNOWN && ageS >= 0.0 && ageS <= MEMORY_S) {
            return Display.huge(String.format(java.util.Locale.US, "OUR CELL %s? (%.0f s ago)", lastSeen, ageS),
                    (Display.Level) null);
        }
        return Display.huge("OUR CELL ?", (Display.Level) null);
    }
}
