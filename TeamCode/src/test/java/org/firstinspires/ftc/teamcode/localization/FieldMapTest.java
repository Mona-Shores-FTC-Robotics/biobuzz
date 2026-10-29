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
        assertEquals("6 |. . * . . .|", plain(map[1]));  // red FAR FLOWER
        assertEquals("4 |. . h H . *|", plain(map[3]));  // SCORING CELLs: red down, blue up; blue WALL FLOWER
        assertEquals("3 |* . H h . .|", plain(map[4]));  // red WALL FLOWER; AUDIENCE CELLs: red up, blue down
        assertEquals("1 |. . ^ * . .|", plain(map[6]));  // our robot, facing up the field; blue FAR FLOWER
        assertEquals("   audience", plain(map[7]));
    }
}
