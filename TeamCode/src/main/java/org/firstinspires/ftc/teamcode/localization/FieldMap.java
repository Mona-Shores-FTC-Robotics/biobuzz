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
 * The HIVE and the FLOWERs are placed from the code's own field model ({@link HiveFieldPoints},
 * {@link FlowerFieldPoints}), so the map cannot disagree with what the robot uses.
 *
 * <p>Seams: the space between two columns is the seam between them (V to Z), so a FLOWER on the far
 * or audience wall is drawn in that gap. A FLOWER on a side wall sits on a seam between two rows,
 * and there is no gap between lines, so it is drawn on the wall of the lower row as a superscript:
 * raised to the top of that row, which is the seam. (Android's superscript moves the baseline and
 * keeps the size, so the column stays its width.)
 */
public final class FieldMap {

    static final String RED = "#E53935";
    static final String BLUE = "#1E88E5";
    static final String ROBOT = "#4CAF50";
    static final String FLOWER = "#FDD835";

    /**
     * The LOADING ZONEs' tiles (manual §9.3, Figures 9-2 and 9-3): one tile wide along the alliance's
     * wall ("width set by TILE seams") and 11 in deep, red's in A5 on the red wall, blue's in F2,
     * red's turned half a turn.
     */
    static final String RED_LOADING_ZONE = "A5";
    static final String BLUE_LOADING_ZONE = "F2";

    private FieldMap() {
    }

    /** The map with {@code start}'s robot on it: header, six rows (row 6 first), footer, key. */
    public static String[] lines(StartPosition start) {
        String[][] cells = new String[6][6];
        // gaps[r][k]: the seam between columns k and k+1 on row r; walls: each row's left and right.
        String[][] gaps = new String[6][5];
        String[] leftWall = new String[6];
        String[] rightWall = new String[6];
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) cells[r][c] = ".";
            for (int k = 0; k < 5; k++) gaps[r][k] = " ";
            leftWall[r] = colour(RED, "|");
            rightWall[r] = colour(BLUE, "|");
        }
        putTile(cells, RED_LOADING_ZONE, colour(RED, "L"));
        putTile(cells, BLUE_LOADING_ZONE, colour(BLUE, "L"));
        String flower = colour(FLOWER, "*");
        double tile = FieldFrame.TILE_INCHES;
        double field = FieldFrame.FIELD_SIZE_INCHES;
        for (FlowerFieldPoints.Flower f : FlowerFieldPoints.ALL) {
            // Which wall it is on: the nearest one.
            double toLeft = f.x, toRight = field - f.x, toBottom = f.y, toTop = field - f.y;
            double nearest = Math.min(Math.min(toLeft, toRight), Math.min(toBottom, toTop));
            if (nearest == toBottom || nearest == toTop) {
                int k = (int) Math.round(f.x / tile) - 1;  // the column seam it sits on
                int r = nearest == toBottom ? 0 : 5;
                if (k >= 0 && k < 5) gaps[r][k] = flower;
            } else {
                int m = (int) Math.round(f.y / tile);       // the row seam it sits on: rows m | m+1
                String[] wall = nearest == toLeft ? leftWall : rightWall;
                if (m >= 1 && m <= 5) wall[m - 1] = "<sup>" + flower + "</sup>"; // top of row m

            }
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
            StringBuilder line = new StringBuilder("<tt>").append(r + 1).append(' ').append(leftWall[r]);
            for (int c = 0; c < 6; c++) {
                if (c > 0) line.append(gaps[r][c - 1]);
                line.append(cells[r][c]);
            }
            lines[6 - r] = line.append(rightWall[r]).append("</tt>").toString();
        }
        lines[7] = "<tt>&nbsp;&nbsp;&nbsp;audience</tt>";
        lines[8] = "<small>" + colour(RED, "|") + " red wall · " + colour(BLUE, "|") + " blue wall · "
                + "<b>H</b> HIVE CELL up at the start, h down · L LOADING ZONE · "
                + flower + " FLOWER, on a seam" + (start == null ? "" : " · " + colour(ROBOT, start.arrow())
                + " our robot") + "</small>";
        return lines;
    }

    private static void putTile(String[][] cells, String tile, String mark) {
        cells[tile.charAt(1) - '1'][tile.charAt(0) - 'A'] = mark;
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
