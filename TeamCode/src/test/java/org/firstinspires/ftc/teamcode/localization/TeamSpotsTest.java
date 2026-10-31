package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The team's spots ({@code TeamCode/autos/spots.json}, written by the Visualizer) and the starts
 * measured in {@link StartPositions} say the same thing: each measured start has its
 * {@code START_} spot, at the same pose. The Visualizer's start buttons are those spots, so a start
 * moved in one place and not the other would put every Auto drawn from it somewhere the robot was
 * not measured to start. A {@code START_} spot with no measured start (one still as drawn) is
 * allowed.
 *
 * <p>A textual scan of the file, like {@code AutoSourcesTest}: one spot per line, as the
 * Visualizer writes it ({@code "NAME": [x, y, heading]}).
 */
public class TeamSpotsTest {

    private static final Pattern SPOT =
            Pattern.compile("\"([A-Z][A-Z0-9_]*)\"\\s*:\\s*\\[\\s*([-0-9.eE]+)\\s*,\\s*([-0-9.eE]+)\\s*,\\s*([-0-9.eE]+)\\s*]");

    @Test
    public void everyMeasuredStartIsItsTeamSpot() {
        Map<String, double[]> spots = spots();
        assertEquals("StartPositions.TEAM_SPOTS names each start in ALL, in order",
                StartPositions.ALL.length, StartPositions.TEAM_SPOTS.length);
        for (String name : StartPositions.TEAM_SPOTS) {
            assertTrue(name + " is a team spot name, START_…", name.startsWith("START_"));
            StartPosition start = StartPositions.forTeamSpot(name);
            assertNotNull(start);
            double[] spot = spots.get(name);
            if (spot == null) {
                fail(start.name + " is measured but spots.json has no " + name
                        + ": add it in the Visualizer (a team spot named " + name + ") and Save team spots");
            }
            String where = name + " in spots.json is (" + spot[0] + ", " + spot[1] + ", " + spot[2]
                    + " deg) but " + start.name + " was measured at (" + start.pose.x() + ", "
                    + start.pose.y() + ", " + Math.toDegrees(start.pose.heading()) + " deg): change the one that is wrong";
            assertEquals(where, start.pose.x(), spot[0], 0.01);
            assertEquals(where, start.pose.y(), spot[1], 0.01);
            double turn = Math.IEEEremainder(Math.toDegrees(start.pose.heading()) - spot[2], 360);
            assertEquals(where, 0, turn, 0.01);
        }
    }

    @Test
    public void aSpotThatIsNotAMeasuredStartIsNotOne() {
        assertNull(StartPositions.forTeamSpot("WALL_FLOWER"));
        assertEquals("START_LEFT is measured since 31 Oct", StartPositions.RED_FAR,
                StartPositions.forTeamSpot("START_LEFT"));
    }

    private static Map<String, double[]> spots() {
        File file = spotsFile();
        String text;
        try {
            text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Could not read " + file, e);
        }
        Map<String, double[]> spots = new LinkedHashMap<>();
        Matcher m = SPOT.matcher(text);
        while (m.find()) {
            spots.put(m.group(1), new double[]{
                    Double.parseDouble(m.group(2)), Double.parseDouble(m.group(3)), Double.parseDouble(m.group(4))});
        }
        return spots;
    }

    /** TeamCode/autos/spots.json, from the module dir under Gradle or the repo root in an IDE. */
    private static File spotsFile() {
        File candidate = new File(System.getProperty("user.dir"));
        for (int depth = 0; depth < 4 && candidate != null; depth++) {
            for (String path : new String[]{"autos/spots.json", "TeamCode/autos/spots.json"}) {
                File file = new File(candidate, path);
                if (file.isFile()) return file.getAbsoluteFile();
            }
            candidate = candidate.getParentFile();
        }
        throw new AssertionError("TeamCode/autos/spots.json not found from " + System.getProperty("user.dir"));
    }
}
