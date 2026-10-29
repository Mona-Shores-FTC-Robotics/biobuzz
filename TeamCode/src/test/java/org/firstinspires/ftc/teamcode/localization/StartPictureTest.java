package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StartPictureTest {

    /** A row's colours, one letter per square: G start, A camera, . empty. */
    private static String colours(String row) {
        StringBuilder out = new StringBuilder();
        String colour = null;
        for (int i = 0; i < row.length(); i++) {
            if (row.startsWith("<font color=\"", i)) {
                int end = row.indexOf('"', i + 13);
                colour = row.substring(i + 13, end);
                i = row.indexOf('>', end);
            } else if (row.charAt(i) == '■') {
                out.append(StartPicture.TARGET.equals(colour) ? 'G'
                        : StartPicture.CAMERA.equals(colour) ? 'A' : '.');
            }
        }
        return out.toString();
    }

    @Test
    public void theRedAudienceStartIsTileC1() {
        String[] map = StartPicture.tileMap(55.0, 9.7, Double.NaN, Double.NaN);

        assertEquals(7, map.length);
        assertEquals("......", colours(map[0]));  // row 6, the far wall, first
        assertEquals("..G...", colours(map[5]));  // row 1, the audience wall, last; column C
        assertTrue(map[5].endsWith(" 1"));
        assertTrue(map[6].contains("A B C D E F"));
    }

    @Test
    public void aRobotOnTheWrongTileShowsBoth() {
        // Placed a tile to the right (column D) and a row up (row 2).
        String[] map = StartPicture.tileMap(55.0, 9.7, 55.0 + 24, 9.7 + 24);

        assertEquals("...A..", colours(map[4]));
        assertEquals("..G...", colours(map[5]));
        assertFalse(StartPicture.sameTile(55.0, 9.7, 55.0 + 24, 9.7 + 24));
        assertTrue(StartPicture.sameTile(55.0, 9.7, 56.0, 11.0));
    }

    /** "Move it 2 in back, 1 in left": the camera has it 2 in ahead and 1 in to its right. */
    @Test
    public void theCloseUpPutsTheCameraWhereTheRobotSits() {
        String[] up = StartPicture.closeUp(-2.0, 1.0);

        assertEquals(2 * StartPicture.CLOSE_UP_REACH + 1, up.length);
        assertEquals("....A..", colours(up[1])); // two rows above the centre, one to the right
        assertEquals("...G...", colours(up[3])); // the start, in the middle
    }

    @Test
    public void aCameraBeyondTheCloseUpIsHeldAtItsEdge() {
        String[] up = StartPicture.closeUp(10.0, 0.0); // move it 10 in forward: camera far behind

        assertEquals("...A...", colours(up[up.length - 1]));
    }
}
