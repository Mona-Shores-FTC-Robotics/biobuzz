package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.firstinspires.ftc.teamcode.localization.generated.TeamSpots;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The team's spots ({@code TeamCode/autos/spots.json}) and their Java twin, the generated
 * {@link TeamSpots}, say the same thing. The Visualizer writes both whenever the team's spots are
 * saved, so they differ only when one was edited by hand: move the spot in the Visualizer and save,
 * which writes both again. {@link StartPositions} and {@link AutoLaunchSpots} read their poses from
 * {@link TeamSpots}, so a start or an Auto launch spot is moved there and nowhere else.
 *
 * <p>A textual scan of the file, like {@code AutoSourcesTest}: one spot per line, as the
 * Visualizer writes it ({@code "NAME": [x, y, heading]}, or {@code [x, y]} for a park).
 */
public class TeamSpotsTest {

    private static final Pattern SPOT =
            Pattern.compile("\"([A-Z][A-Z0-9_]*)\"\\s*:\\s*\\[\\s*([-0-9.eE]+)\\s*,\\s*([-0-9.eE]+)\\s*(?:,\\s*([-0-9.eE]+))?\\s*]");

    private static final String RESAVE = ": TeamSpots.java and spots.json are written together by the Visualizer;"
            + " one was edited by hand. Move the spot in the Visualizer and save, which writes both.";

    @Test
    public void theGeneratedJavaIsSpotsJson() {
        Map<String, double[]> json = spots();
        Map<String, TeamSpot> java = new LinkedHashMap<>();
        for (TeamSpot spot : TeamSpots.all()) java.put(spot.name, spot);
        assertEquals("The spots' names" + RESAVE, new TreeSet<>(json.keySet()), new TreeSet<>(java.keySet()));
        for (Map.Entry<String, double[]> e : json.entrySet()) {
            double[] j = e.getValue();
            TeamSpot spot = java.get(e.getKey());
            String where = spot.name + " is (" + j[0] + ", " + j[1] + ", " + j[2] + ") in spots.json but ("
                    + spot.x + ", " + spot.y + ", " + spot.headingDeg + ") in TeamSpots.java" + RESAVE;
            assertEquals(where, j[0], spot.x, 0);
            assertEquals(where, j[1], spot.y, 0);
            if (Double.isNaN(j[2])) assertTrue(where, Double.isNaN(spot.headingDeg));
            else assertEquals(where, j[2], spot.headingDeg, 0);
        }
    }

    @Test
    public void eachStartIsItsStartSpot() {
        assertEquals("StartPositions.TEAM_SPOTS names each start in ALL, in order",
                StartPositions.ALL.length, StartPositions.TEAM_SPOTS.length);
        for (String name : StartPositions.TEAM_SPOTS) {
            assertTrue(name + " is a team spot name, …_START", name.endsWith("_START"));
            StartPosition start = StartPositions.forTeamSpot(name);
            assertNotNull(start);
            TeamSpot spot = spot(name);
            assertTrue(name + " has no heading; a start needs one", spot.hasHeading());
            assertEquals(spot.pose().x(), start.pose.x(), 0);
            assertEquals(spot.pose().y(), start.pose.y(), 0);
            assertEquals(spot.pose().heading(), start.pose.heading(), 1e-12);
        }
    }

    @Test
    public void eachAutoLaunchSpotIsItsTeamSpot() {
        for (AutoLaunchSpots.Spot launch : AutoLaunchSpots.Spot.values()) {
            TeamSpot spot = spot(launch.name());
            assertEquals(launch + " reads the team spot of its own name", spot.pose().x(), launch.red.x(), 0);
            assertEquals(launch + " reads the team spot of its own name", spot.pose().y(), launch.red.y(), 0);
            assertEquals(launch + " reads the team spot of its own name",
                    spot.pose().heading(), launch.red.heading(), 1e-12);
        }
    }

    @Test
    public void aSpotThatIsNotAMeasuredStartIsNotOne() {
        assertNull(StartPositions.forTeamSpot("WALL_FLOWER_STAGING"));
        assertEquals("LEFT_START is measured since 31 Oct", StartPositions.RED_FAR,
                StartPositions.forTeamSpot("LEFT_START"));
    }

    private static TeamSpot spot(String name) {
        for (TeamSpot spot : TeamSpots.all()) {
            if (spot.name.equals(name)) return spot;
        }
        fail(name + " is not a team spot: add it in the Visualizer (a team spot named " + name + ") and save");
        return null;
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
            // A spot with no heading (a park) is reached at whatever heading the robot arrives with.
            spots.put(m.group(1), new double[]{
                    Double.parseDouble(m.group(2)), Double.parseDouble(m.group(3)),
                    m.group(4) == null ? Double.NaN : Double.parseDouble(m.group(4))});
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
