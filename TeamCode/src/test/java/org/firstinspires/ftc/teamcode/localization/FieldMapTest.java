package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

public class FieldMapTest {

    /** A map row with its markup taken out: "6 |. . * . . .|". */
    private static String plain(String line) {
        return line.replaceAll("<[^>]+>", "").replace("&nbsp;", " ")
                .replace("&gt;", ">").replace("&lt;", "<");
    }

    @Test
    public void theRedAudienceStartSitsInC1AmongTheFieldsLandmarks() {
        String[] map = FieldMap.lines(StartPositions.RED_AUDIENCE);

        assertEquals("   A B C D E F", plain(map[0]));
        assertEquals("6  . .*. . . .", plain(map[1]));  // red SCORING FLOWER, on seam W at the scoring-table wall
        assertEquals("5  | . . . . .", plain(map[2]));  // red LOADING ZONE tape in A5
        assertEquals("4  . . h H . .*", plain(map[3])); // SCORING CELLs; blue WALL FLOWER raised to seam 4|5
        assertEquals("3  . . H h . .", plain(map[4]));  // AUDIENCE CELLs: red up, blue down
        assertEquals("2 *. . . . . |", plain(map[5]));  // red WALL FLOWER raised to seam 2|3; blue LOADING ZONE
        assertTrue(map[5].contains("<sup>"));
        assertEquals("1  . . ^ .*. .", plain(map[6]));  // our robot; blue AUDIENCE FLOWER on seam Y
        assertEquals("   audience", plain(map[7]));
    }

    /** Red's drive team sees the field from the red wall: it at the bottom, rows 6 to 1 across. */
    @Test
    public void redsViewHasTheRedWallAtTheBottom() {
        String[] map = FieldMap.lines(StartPositions.RED_AUDIENCE, Alliance.RED);

        assertEquals("   6 5 4 3 2 1", plain(map[0]));
        assertEquals("F  . .*. . | .", plain(map[1]));  // blue wall at the top: blue LOADING ZONE in F2
        assertEquals("D  . . H h . .*", plain(map[3]));
        assertEquals("C  . . h H . <", plain(map[4]));  // our robot in C1, facing away from the audience: to the left
        assertEquals("A  . | . .*. .", plain(map[6]));  // our wall: the red LOADING ZONE in A5
        assertTrue(plain(map[7]).contains("your wall (red)"));
        assertTrue("the audience is to red's right", map[7].indexOf("audience") > map[7].indexOf("red"));
    }

    /** The field is a half turn symmetric, so blue at its start sees what red sees at its own. */
    @Test
    public void bluesViewOfItsStartLooksLikeRedsOfTheirs() {
        String[] red = FieldMap.lines(StartPositions.RED_AUDIENCE, Alliance.RED);
        String[] blue = FieldMap.lines(StartPositions.BLUE_FROM_RED_AUDIENCE, Alliance.BLUE);

        assertEquals("   1 2 3 4 5 6", plain(blue[0]));
        assertEquals("D  . . h H . <", plain(blue[4]));
        assertEquals("F  . | . .*. .", plain(blue[6]));  // our wall: the blue LOADING ZONE in F2
        for (int i = 1; i <= 6; i++) {
            assertEquals("row " + i, plain(red[i]).substring(1), plain(blue[i]).substring(1));
        }
        assertTrue(plain(blue[7]).contains("your wall (blue)"));
    }

    @Test
    public void noAllianceIsTheAudiencesView() {
        org.junit.Assert.assertArrayEquals(FieldMap.lines(StartPositions.RED_AUDIENCE),
                FieldMap.lines(StartPositions.RED_AUDIENCE, Alliance.UNKNOWN));
    }
}
