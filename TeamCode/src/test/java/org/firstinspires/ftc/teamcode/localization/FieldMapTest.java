package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        assertEquals("6 |. .*. . . .|", plain(map[1]));  // red FAR FLOWER, on seam W at the far wall
        assertEquals("5 |L . . . . .|", plain(map[2]));  // red LOADING ZONE
        assertEquals("4 |. . h H . .*", plain(map[3]));  // SCORING CELLs; blue WALL FLOWER raised to seam 4|5
        assertEquals("3 |. . H h . .|", plain(map[4]));  // AUDIENCE CELLs: red up, blue down
        assertEquals("2 *. . . . . L|", plain(map[5]));  // red WALL FLOWER raised to seam 2|3; blue LOADING ZONE
        assertTrue(map[5].contains("<sup>"));
        assertEquals("1 |. . ^ .*. .|", plain(map[6]));  // our robot; blue FAR FLOWER on seam Y
        assertEquals("   audience", plain(map[7]));
    }
}
