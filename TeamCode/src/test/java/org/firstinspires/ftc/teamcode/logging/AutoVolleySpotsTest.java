package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.localization.AutoLaunchSpots;
import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * An Auto on master launches only from a start or one of the Autos' launch spots
 * ({@link AutoLaunchSpots}: a HIVE and an OFFSET at each end), never from a spot of its own or one
 * of the drivers' Square spots (mentor, 16 Nov 2026).
 *
 * <p>Read from each Auto's source, {@code TeamCode/autos/*.pp}: its cards are walked in order and
 * every {@code LaunchAll} is placed where the path card before it ends (the start, with none). Every
 * row of a branching block is walked from where the block begins. Drafts and partner stand-ins
 * ({@code drafts/}, {@code partners/}) are not held to it.
 *
 * <p>{@link #NOT_YET_MOVED} names the Autos that still launch from the older spots. An Auto leaves
 * the list in the PR that moves it, and the list goes when it is empty: an Auto in it that already
 * passes fails here too, so the list cannot outlive its reason.
 */
public class AutoVolleySpotsTest {

    /** Autos still on the older launch spots, by source file. */
    private static final Set<String> NOT_YET_MOVED = new HashSet<>(Arrays.asList(
            "r-solo.pp", "r-no-cross.pp", "l-no-cross.pp", "r-clockwise.pp", "l-clockwise.pp"));

    /** A volley's place and an allowed spot's are the same within this, inches (a .pp is to 0.1 in). */
    private static final double SAME_IN = 0.06;

    @Test
    public void everyVolleyIsFromAStartOrAnAutoLaunchSpot() throws Exception {
        File[] sources = new File(TeamCodeDir.get(), "autos").listFiles((dir, name) -> name.endsWith(".pp"));
        assertTrue("no Auto sources found in TeamCode/autos", sources != null && sources.length > 0);
        Set<String> seen = new TreeSet<>();
        for (File source : sources) {
            seen.add(source.getName());
            List<String> elsewhere = volleysElsewhere(source);
            if (NOT_YET_MOVED.contains(source.getName())) {
                assertFalse(source.getName() + " launches only from a start or an Auto launch spot now:"
                        + " take it out of NOT_YET_MOVED", elsewhere.isEmpty());
            } else {
                assertTrue(source.getName() + " launches from " + elsewhere + ": an Auto launches from its start,"
                        + " an AUTO_HIVE or an AUTO_OFFSET spot (AutoLaunchSpots)", elsewhere.isEmpty());
            }
        }
        for (String name : NOT_YET_MOVED) {
            assertTrue(name + " is in NOT_YET_MOVED but is not an Auto on master", seen.contains(name));
        }
    }

    /** The places {@code source}'s volleys are fired from that are neither a start nor an Auto launch spot. */
    @SuppressWarnings("unchecked")
    private static List<String> volleysElsewhere(File source) throws Exception {
        Map<String, Object> doc = (Map<String, Object>) MiniJson.parse(
                new String(Files.readAllBytes(source.toPath()), StandardCharsets.UTF_8));
        Map<String, Object> auto = (Map<String, Object>) doc.get("auto");
        assertTrue(source.getName() + " has no Auto (cards) to read", auto != null && auto.get("cards") != null);
        Walk walk = new Walk(doc, auto);
        walk.cards((List<Object>) auto.get("cards"));
        return walk.elsewhere;
    }

    private static final class Walk {
        private final Map<String, Object> lines = new java.util.HashMap<>();
        private final Map<String, Object> pathEnds;
        /** Where the robot is: x, y and what to call it. */
        private double x, y;
        private String name;
        final List<String> elsewhere = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Walk(Map<String, Object> doc, Map<String, Object> auto) {
            for (Object line : (List<Object>) doc.get("lines")) {
                lines.put((String) ((Map<String, Object>) line).get("id"), line);
            }
            pathEnds = auto.get("pathEnds") == null ? new java.util.HashMap<>() : (Map<String, Object>) auto.get("pathEnds");
            Map<String, Object> start = (Map<String, Object>) doc.get("startPoint");
            x = ((Number) start.get("x")).doubleValue();
            y = ((Number) start.get("y")).doubleValue();
            name = auto.get("startAt") == null ? "the start" : (String) auto.get("startAt");
        }

        @SuppressWarnings("unchecked")
        void cards(List<Object> cards) {
            for (Object o : cards) {
                Map<String, Object> card = (Map<String, Object>) o;
                Object kind = card.get("kind");
                if ("path".equals(kind)) {
                    Map<String, Object> line = (Map<String, Object>) lines.get(card.get("lineId"));
                    assertTrue("a path card names no path: " + card, line != null);
                    // A compound path ends where its last segment does.
                    Map<String, Object> last = line;
                    while (last.get("endPoint") == null && last.get("segments") != null) {
                        List<Object> segments = (List<Object>) last.get("segments");
                        last = (Map<String, Object>) segments.get(segments.size() - 1);
                    }
                    Map<String, Object> end = (Map<String, Object>) last.get("endPoint");
                    assertTrue("a path with no end: " + line.get("name"), end != null);
                    x = ((Number) end.get("x")).doubleValue();
                    y = ((Number) end.get("y")).doubleValue();
                    Object named = pathEnds.get(card.get("lineId"));
                    name = named != null ? (String) named : String.valueOf(line.get("name"));
                } else if ("action".equals(kind) && "LaunchAll".equals(card.get("name"))) {
                    if (!allowed(x, y)) elsewhere.add(name + " (" + x + ", " + y + ")");
                } else if ("firstOf".equals(kind) && card.get("rows") != null) {
                    double x0 = x, y0 = y;
                    String name0 = name;
                    for (Object row : (List<Object>) card.get("rows")) {
                        x = x0;
                        y = y0;
                        name = name0;
                        Object inner = ((Map<String, Object>) row).get("cards");
                        if (inner != null) cards((List<Object>) inner);
                    }
                }
            }
        }
    }

    /** ({@code x}, {@code y}) is a measured start or an Auto launch spot, drawn for red. */
    private static boolean allowed(double x, double y) {
        for (org.firstinspires.ftc.teamcode.localization.StartPosition start
                : new org.firstinspires.ftc.teamcode.localization.StartPosition[] {
                        StartPositions.RED_AUDIENCE, StartPositions.RED_FAR}) {
            if (Math.hypot(start.pose.x() - x, start.pose.y() - y) <= SAME_IN) return true;
        }
        for (AutoLaunchSpots.Spot spot : AutoLaunchSpots.Spot.values()) {
            if (Math.hypot(spot.red.x() - x, spot.red.y() - y) <= SAME_IN) return true;
        }
        return false;
    }
}
