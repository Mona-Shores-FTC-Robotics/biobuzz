package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;

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
        assertEquals("5 |L . . . . .*", plain(map[2]));  // red LOADING ZONE; blue WALL FLOWER (rows 4|5)
        assertEquals("4 |. . h H . .*", plain(map[3]));  // SCORING CELLs: red down, blue up
        assertEquals("3 *. . H h . .|", plain(map[4]));  // red WALL FLOWER (rows 2|3); AUDIENCE CELLs
        assertEquals("2 *. . . . . L|", plain(map[5]));  // blue LOADING ZONE
        assertEquals("1 |. . ^ .*. .|", plain(map[6]));  // our robot; blue FAR FLOWER on seam Y
        assertEquals("   audience", plain(map[7]));
    }
}
