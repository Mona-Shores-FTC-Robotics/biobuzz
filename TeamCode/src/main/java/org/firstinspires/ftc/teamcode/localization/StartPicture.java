package org.firstinspires.ftc.teamcode.localization;

import org.firstinspires.ftc.teamcode.util.FieldFrame;

/**
 * The start check as pictures for the Driver Station, drawn with coloured squares (it renders HTML;
 * every cell is the same character, so the grid stays square in any font). Pure: each method
 * returns the lines to show.
 *
 * <ul>
 *   <li>{@link #tileMap}: the six-by-six field as the manual draws it (audience at the bottom, red
 *       on the left), the start's tile green and the camera's amber. For a big mistake: the wrong
 *       tile, the wrong side.</li>
 *   <li>{@link #closeUp}: one square per inch around the start, the robot's forward at the top,
 *       the start green and the camera amber. For the last nudge: the amber square moves the way
 *       the robot sits, so move the robot toward green.</li>
 * </ul>
 */
public final class StartPicture {

    static final String TARGET = "#4CAF50";
    static final String CAMERA = "#FFB300";
    static final String EMPTY = "#555555";
    private static final String SQUARE = "■";

    /** Squares each side of the start in {@link #closeUp}: 3 shows up to 3 in either way. */
    static final int CLOSE_UP_REACH = 3;

    private StartPicture() {
    }

    /**
     * The field's tiles, top row first (row 6, the far wall) down to row 1 (the audience wall),
     * columns A to F left to right, then a line naming the columns. The camera's tile is skipped while {@code cameraX} is NaN; where
     * both fall on one tile, green wins.
     */
    public static String[] tileMap(double startX, double startY, double cameraX, double cameraY) {
        int startColumn = column(startX);
        int startRow = column(startY);
        boolean camera = !Double.isNaN(cameraX) && !Double.isNaN(cameraY);
        int cameraColumn = camera ? column(cameraX) : -1;
        int cameraRow = camera ? column(cameraY) : -1;
        String[] rows = new String[7];
        for (int r = 5; r >= 0; r--) {
            String[] colours = new String[6];
            for (int c = 0; c < 6; c++) {
                colours[c] = c == startColumn && r == startRow ? TARGET
                        : c == cameraColumn && r == cameraRow ? CAMERA : EMPTY;
            }
            rows[5 - r] = squares(colours) + " " + (r + 1);
        }
        rows[6] = "<small>A B C D E F, red wall to blue wall</small>";
        return rows;
    }

    /**
     * The inches around the start in the robot's own frame, top row first: up is the way the robot
     * faces at its start, right is its right. {@code moveForwardIn} / {@code moveLeftIn} are the
     * start check's (the move that puts the robot on the start), so the camera's square sits the
     * opposite way from the centre; beyond the edge it is held at the edge.
     */
    public static String[] closeUp(double moveForwardIn, double moveLeftIn) {
        int size = 2 * CLOSE_UP_REACH + 1;
        // Where the camera puts the robot, relative to the start: the opposite of the move.
        int cameraRow = clamp(CLOSE_UP_REACH + (int) Math.round(moveForwardIn), size);
        int cameraColumn = clamp(CLOSE_UP_REACH + (int) Math.round(moveLeftIn), size);
        String[] rows = new String[size];
        for (int r = 0; r < size; r++) {
            String[] colours = new String[size];
            for (int c = 0; c < size; c++) {
                colours[c] = r == CLOSE_UP_REACH && c == CLOSE_UP_REACH ? TARGET
                        : r == cameraRow && c == cameraColumn ? CAMERA : EMPTY;
            }
            rows[r] = squares(colours);
        }
        return rows;
    }

    /** Whether the camera puts the robot on the start's own tile. */
    public static boolean sameTile(double startX, double startY, double cameraX, double cameraY) {
        return column(startX) == column(cameraX) && column(startY) == column(cameraY);
    }

    /** Tile index 0–5 along one axis, held to the field. */
    private static int column(double inches) {
        return clamp((int) Math.floor(inches / FieldFrame.TILE_INCHES), 6);
    }

    private static int clamp(int i, int size) {
        return Math.max(0, Math.min(size - 1, i));
    }

    /** One row of squares, one font tag per run of a colour, drawn large enough to read at arm's length. */
    private static String squares(String[] colours) {
        StringBuilder row = new StringBuilder("<big>");
        int i = 0;
        while (i < colours.length) {
            int j = i;
            while (j < colours.length && colours[j].equals(colours[i])) j++;
            row.append("<font color=\"").append(colours[i]).append("\">");
            for (int k = i; k < j; k++) row.append(SQUARE);
            row.append("</font>");
            i = j;
        }
        return row.append("</big>").toString();
    }
}
