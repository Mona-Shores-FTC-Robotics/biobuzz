package org.firstinspires.ftc.teamcode.localization;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

/**
 * The field on the Driver Station, one character per tile, as the manual draws it (§9.4: columns
 * A–F, rows 1–6, audience at the bottom): the red and blue walls, the HIVE CELLs as they start the
 * match, the FLOWERs, and a start's robot as an arrow the way it faces. The placement sentence
 * refers to it ("tile C1, the ^").
 *
 * <p>Fixed-width plain characters ({@code <tt>}), so the column letters stay over their columns.
 * Everything is placed from the code's own field model ({@link HiveFieldPoints},
 * {@link FlowerFieldPoints}), so the map cannot disagree with what the robot uses. A FLOWER sits on
 * a seam; it is drawn in the tile its centre falls in.
 */
public final class FieldMap {

    static final String RED = "#E53935";
    static final String BLUE = "#1E88E5";
    static final String ROBOT = "#4CAF50";
    static final String FLOWER = "#FDD835";

    private FieldMap() {
    }

    /** The map with {@code start}'s robot on it: header, six rows (row 6 first), footer, key. */
    public static String[] lines(StartPosition start) {
        String[][] cells = new String[6][6];
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) cells[r][c] = ".";
        }
        for (FlowerFieldPoints.Flower flower : FlowerFieldPoints.ALL) {
            put(cells, flower.x, flower.y, colour(FLOWER, "*"));
        }
        for (HiveCell cell : HiveCell.values()) {
            // Each alliance's right CELL starts the match up, its left down.
            boolean up = cell == HiveCell.rightCell(cell.alliance());
            Vec3 point = HiveFieldPoints.rowCentre(cell, up ? HiveCellState.UP : HiveCellState.DOWN);
            if (point == null) continue;
            String colour = cell.alliance() == Alliance.RED ? RED : BLUE;
            put(cells, point.x(), point.y(), up ? colour(colour, "<b>H</b>") : colour(colour, "h"));
        }
        if (start != null) {
            put(cells, start.pose.x(), start.pose.y(), colour(ROBOT, "<b>" + start.arrow() + "</b>"));
        }

        String[] lines = new String[9];
        lines[0] = "<tt>&nbsp;&nbsp;&nbsp;A B C D E F</tt>";
        for (int r = 5; r >= 0; r--) {
            StringBuilder line = new StringBuilder("<tt>").append(r + 1).append(' ').append(colour(RED, "|"));
            for (int c = 0; c < 6; c++) {
                if (c > 0) line.append(' ');
                line.append(cells[r][c]);
            }
            lines[6 - r] = line.append(colour(BLUE, "|")).append("</tt>").toString();
        }
        lines[7] = "<tt>&nbsp;&nbsp;&nbsp;audience</tt>";
        lines[8] = "<small>" + colour(RED, "|") + " red wall · " + colour(BLUE, "|") + " blue wall · "
                + "<b>H</b> HIVE CELL up at the start, h down · " + colour(FLOWER, "*") + " FLOWER"
                + (start == null ? "" : " · " + colour(ROBOT, start.arrow()) + " our robot") + "</small>";
        return lines;
    }

    private static void put(String[][] cells, double x, double y, String mark) {
        String tile = FieldFrame.tileName(x, y);
        if (tile == null) return;
        cells[tile.charAt(1) - '1'][tile.charAt(0) - 'A'] = mark;
    }

    private static String colour(String colour, String text) {
        return "<font color=\"" + colour + "\">" + text + "</font>";
    }
}
