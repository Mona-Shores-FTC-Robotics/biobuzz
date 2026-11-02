package org.firstinspires.ftc.teamcode.localization;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

/**
 * The field on the Driver Station, one character per tile, as the manual draws it (§9.4: columns
 * A–F, rows 1–6, audience at the bottom): the LOADING ZONE tape in its alliance's colour (which
 * also says which side is whose, so the walls are not drawn), the HIVE CELLs as they start the
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

    /** The audience's view, as the manual draws the field: see {@link #lines(StartPosition, Alliance)}. */
    public static String[] lines(StartPosition start) {
        return lines(start, Alliance.UNKNOWN);
    }

    /**
     * The map with {@code start}'s robot on it, as {@code viewer}'s drive team sees the field from
     * its station: its own wall at the bottom, the other alliance's at the top (red's station is on
     * the red wall, the field's -x side, blue's on the blue wall facing it; {@link
     * FieldFrame#driverForwardHeading}). {@code UNKNOWN} draws it as the audience sees it, the
     * manual's view: audience wall at the bottom. Tiles keep their manual names, so the labels run
     * the way the viewer sees them (red: rows 6 to 1 across, columns A at the bottom to F at the
     * top). Header, six rows, and a footer that says whose wall is at the bottom and where the
     * audience is. No key: every mark is recognisable on the field it stands for.
     */
    public static String[] lines(StartPosition start, Alliance viewer) {
        View view = View.of(viewer);
        String[][] cells = new String[6][6];
        // gaps[r][k]: the seam between printed columns k and k+1 on printed row r (row 0 at the
        // bottom); walls: a FLOWER on the printed left or right wall, else blank (a non-breaking
        // space on the left, so HTML does not fold it into the next one).
        String[][] gaps = new String[6][5];
        String[] leftWall = new String[6];
        String[] rightWall = new String[6];
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) cells[r][c] = ".";
            for (int k = 0; k < 5; k++) gaps[r][k] = " ";
            leftWall[r] = "&nbsp;";
            rightWall[r] = "";
        }
        double tile = FieldFrame.TILE_INCHES;
        double field = FieldFrame.FIELD_SIZE_INCHES;
        putTile(cells, view, RED_LOADING_ZONE, colour(RED, "<b>|</b>"));
        putTile(cells, view, BLUE_LOADING_ZONE, colour(BLUE, "<b>|</b>"));
        String flower = colour(FLOWER, "*");
        for (FlowerFieldPoints.Flower f : FlowerFieldPoints.ALL) {
            double u = view.u(f.x, f.y), v = view.v(f.x, f.y);
            // Which printed wall it is on: the nearest one.
            double toLeft = u, toRight = field - u, toBottom = v, toTop = field - v;
            double nearest = Math.min(Math.min(toLeft, toRight), Math.min(toBottom, toTop));
            if (nearest == toBottom || nearest == toTop) {
                int k = (int) Math.round(u / tile) - 1;  // the printed column seam it sits on
                int r = nearest == toBottom ? 0 : 5;
                if (k >= 0 && k < 5) gaps[r][k] = flower;
            } else {
                // On a side of the print, a FLOWER sits on the seam between two printed rows, and
                // there is no gap between lines: raised to the top of the lower row, the seam.
                int m = (int) Math.round(v / tile);
                String[] wall = nearest == toLeft ? leftWall : rightWall;
                if (m >= 1 && m <= 5) wall[m - 1] = "<sup>" + flower + "</sup>";
            }
        }
        for (HiveCell cell : HiveCell.values()) {
            // Each alliance's right CELL starts the match up, its left down.
            boolean up = cell == HiveCell.rightCell(cell.alliance());
            Vec3 point = HiveFieldPoints.rowCentre(cell, up ? HiveCellState.UP : HiveCellState.DOWN);
            if (point == null) continue;
            String colour = cell.alliance() == Alliance.RED ? RED : BLUE;
            put(cells, view, point.x(), point.y(), up ? colour(colour, "<b>H</b>") : colour(colour, "h"));
        }
        if (start != null) {
            put(cells, view, start.pose.x(), start.pose.y(),
                    colour(ROBOT, "<b>" + view.arrow(start.pose.heading()) + "</b>"));
        }

        String[] lines = new String[8];
        StringBuilder header = new StringBuilder("<tt>&nbsp;&nbsp;");
        for (int c = 0; c < 6; c++) header.append("&nbsp;").append(view.columnLabel(c));
        lines[0] = header.append("</tt>").toString();
        for (int r = 5; r >= 0; r--) {
            StringBuilder line = new StringBuilder("<tt>").append(view.rowLabel(r)).append(' ').append(leftWall[r]);
            for (int c = 0; c < 6; c++) {
                if (c > 0) line.append(gaps[r][c - 1]);
                line.append(cells[r][c]);
            }
            lines[6 - r] = line.append(rightWall[r]).append("</tt>").toString();
        }
        lines[7] = "<tt>&nbsp;&nbsp;&nbsp;" + view.footer + "</tt>";
        return lines;
    }

    /**
     * Where a viewer stands: field (x, y) to printed (u across to the right, v up the page), both
     * 0 to the field's size, and the labels and arrows that go with it.
     */
    enum View {
        /** The manual's view: audience wall at the bottom; u = x, v = y. */
        AUDIENCE(Math.PI / 2, "audience"),
        /** Red's station, on the red wall (x = 0), looking toward +x: u = size - y, v = x. */
        RED_STATION(0.0, "your wall (red) &middot; audience &rarr;"),
        /** Blue's station, on the blue wall, looking toward -x: u = y, v = size - x. */
        BLUE_STATION(Math.PI, "&larr; audience &middot; your wall (blue)");

        /** The field heading that points up the page. */
        final double up;
        final String footer;

        View(double up, String footer) {
            this.up = up;
            this.footer = footer;
        }

        static View of(Alliance viewer) {
            return viewer == Alliance.RED ? RED_STATION : viewer == Alliance.BLUE ? BLUE_STATION : AUDIENCE;
        }

        double u(double x, double y) {
            double size = FieldFrame.FIELD_SIZE_INCHES;
            switch (this) {
                case RED_STATION: return size - y;
                case BLUE_STATION: return y;
                default: return x;
            }
        }

        double v(double x, double y) {
            double size = FieldFrame.FIELD_SIZE_INCHES;
            switch (this) {
                case RED_STATION: return x;
                case BLUE_STATION: return size - x;
                default: return y;
            }
        }

        /** The manual's name for what printed column {@code c} (0 at the left) runs along. */
        String columnLabel(int c) {
            switch (this) {
                case RED_STATION: return String.valueOf(6 - c);           // rows 6..1
                case BLUE_STATION: return String.valueOf(c + 1);          // rows 1..6
                default: return String.valueOf((char) ('A' + c));        // columns A..F
            }
        }

        /** The manual's name for printed row {@code r} (0 at the bottom). */
        String rowLabel(int r) {
            switch (this) {
                case RED_STATION: return String.valueOf((char) ('A' + r));   // A at the bottom
                case BLUE_STATION: return String.valueOf((char) ('F' - r));  // F at the bottom
                default: return String.valueOf(r + 1);                      // 1 at the bottom
            }
        }

        /** A robot facing {@code headingRad}, as an arrow on this view, to the nearest quarter turn. */
        String arrow(double headingRad) {
            int quarter = (int) Math.floorMod(Math.round((headingRad - up) / (Math.PI / 2)), 4L);
            switch (quarter) {
                case 0: return "^";
                case 1: return "&lt;";
                case 2: return "v";
                default: return "&gt;";
            }
        }
    }

    private static void putTile(String[][] cells, View view, String tile, String mark) {
        double size = FieldFrame.TILE_INCHES;
        double x = (tile.charAt(0) - 'A' + 0.5) * size;
        double y = (tile.charAt(1) - '1' + 0.5) * size;
        put(cells, view, x, y, mark);
    }

    private static void put(String[][] cells, View view, double x, double y, String mark) {
        double tile = FieldFrame.TILE_INCHES;
        double u = view.u(x, y), v = view.v(x, y);
        if (u < 0 || v < 0) return;
        int c = (int) Math.floor(u / tile), r = (int) Math.floor(v / tile);
        if (c > 5 || r > 5) return;
        cells[r][c] = mark;
    }

    private static String colour(String colour, String text) {
        return "<font color=\"" + colour + "\">" + text + "</font>";
    }
}
