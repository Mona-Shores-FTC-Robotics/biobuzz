package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.localization.AutoLaunchSpots;
import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An Auto on master launches only from a start or one of the Autos' launch spots
 * ({@link AutoLaunchSpots}: every team spot named …_AUTO_…), never from a spot of its own or one
 * of the drivers' Square spots (mentor, 16 Nov 2026).
 *
 * <p>Read from each Auto's source, {@code TeamCode/autos/*.pp}: its cards are walked in order and
 * every {@code LaunchAll} is placed where the path card before it ends (the start, with none). Every
 * row of a branching block is walked from where the block begins. Drafts and partner stand-ins
 * ({@code drafts/}, {@code partners/}) are not held to it.
 *
 * <p>All five baseline Autos launch from them since 16 Nov 2026; there is no list of exceptions.
 */
public class AutoVolleySpotsTest {

    /** A volley's place and an allowed spot's are the same within this, inches (a .pp is to 0.1 in). */
    private static final double SAME_IN = 0.06;

    @Test
    public void everyVolleyIsFromAStartOrAnAutoLaunchSpot() throws Exception {
        File[] sources = new File(TeamCodeDir.get(), "autos").listFiles((dir, name) -> name.endsWith(".pp"));
        assertTrue("no Auto sources found in TeamCode/autos", sources != null && sources.length > 0);
        for (File source : sources) {
            List<String> elsewhere = volleysElsewhere(source);
            assertTrue(source.getName() + " launches from " + elsewhere + ": an Auto launches from its start"
                    + " or an Auto launch spot (AutoLaunchSpots)", elsewhere.isEmpty());
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

    /** ({@code x}, {@code y}) is a start (a team …_START spot) or an Auto launch spot (…_AUTO_…), drawn for red. */
    private static boolean allowed(double x, double y) {
        for (org.firstinspires.ftc.teamcode.localization.StartPosition start : StartPositions.known()) {
            if (start.alliance != org.firstinspires.ftc.teamcode.util.Alliance.RED) continue;
            if (Math.hypot(start.pose.x() - x, start.pose.y() - y) <= SAME_IN) return true;
        }
        for (org.firstinspires.ftc.teamcode.localization.TeamSpot spot : AutoLaunchSpots.all()) {
            if (Math.hypot(spot.x - x, spot.y - y) <= SAME_IN) return true;
        }
        return false;
    }
}
