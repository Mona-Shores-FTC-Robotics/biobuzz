package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.localization.AutoLaunchSpots;
import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An Auto on master launches from its start, or from a named spot that is fit to launch from
 * ({@link AutoLaunchSpots#launchSpotProblem}: aimed at the CELL at its end with the turret locked,
 * inside the field, short of the centre line). The spot may be a team spot or one of the Auto's
 * own (mentor, 17 Nov 2026: a spot moved for one Auto need not go on the team's list).
 *
 * <p>Read from each Auto's source, {@code TeamCode/autos/*.pp}: its cards are walked in order and
 * every {@code LaunchAll} is placed where the path card before it ends (the start, with none). Every
 * row of a branching block is walked from where the block begins. Drafts and partner stand-ins
 * ({@code drafts/}, {@code partners/}) are not held to it.
 *
 * <p>One exception, narrow (mentor, 19 Nov 2026): {@code LaunchAllAimed}, the turret aimed from where the robot stands at the
 * CELL at its end ({@code AimedVolley}), is fired from a park spot (a team spot or the Auto's own, named {@code …_PARK}) right
 * after the park card that drove there, and nowhere else. The park comes first; the aim is the TeleOp's, at the near
 * CELL only.
 */
public class AutoVolleySpotsTest {

    /** A volley's place and an allowed spot's are the same within this, inches (a .pp is to 0.1 in). */
    private static final double SAME_IN = 0.06;

    @Test
    public void everyVolleyIsFromAStartOrASpotFitToLaunchFrom() throws Exception {
        File[] sources = new File(TeamCodeDir.get(), "autos").listFiles((dir, name) -> name.endsWith(".pp"));
        assertTrue("no Auto sources found in TeamCode/autos", sources != null && sources.length > 0);
        for (File source : sources) {
            List<String> elsewhere = volleysElsewhere(source);
            assertTrue(source.getName() + " launches from " + elsewhere + ": an Auto launches from its start"
                    + " or a named spot aimed at its CELL (AutoLaunchSpots.launchSpotProblem)", elsewhere.isEmpty());
        }
    }

    /**
     * The exception's draft, which the test above does not read (drafts are not held to it): its plain
     * LaunchAlls are where L-UnderHive's are, and its LaunchAllAimed is from LEFT_AUTO_PARK after the park.
     */
    @Test
    public void theParkShotDraftFiresItsAimedVolleyOnlyFromThePark() throws Exception {
        File draft = new File(TeamCodeDir.get(), "autos/drafts/l-under-hive-park-shot.pp");
        if (!draft.isFile()) return;
        List<String> elsewhere = volleysElsewhere(draft);
        assertTrue(draft.getName() + " launches from " + elsewhere, elsewhere.isEmpty());
    }

    /** The places {@code source}'s volleys are fired from that are neither its start nor a spot fit to launch from, and why. */
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
        private final Map<String, Object> points;
        /** The Auto is drawn for blue: its poses are turned half a turn to red's before they are checked. */
        private final boolean blue;
        /** Where the robot is: x, y, what to call it, and the named spot it is on (null: none). */
        private double x, y;
        private String name, spot;
        /** The last path card was a park ({@code "park": true}). */
        private boolean parked;
        final List<String> elsewhere = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Walk(Map<String, Object> doc, Map<String, Object> auto) {
            for (Object line : (List<Object>) doc.get("lines")) {
                lines.put((String) ((Map<String, Object>) line).get("id"), line);
            }
            pathEnds = auto.get("pathEnds") == null ? new java.util.HashMap<>() : (Map<String, Object>) auto.get("pathEnds");
            points = auto.get("points") == null ? new java.util.HashMap<>() : (Map<String, Object>) auto.get("points");
            blue = "BLUE".equals(auto.get("drawnFor"));
            Map<String, Object> start = (Map<String, Object>) doc.get("startPoint");
            x = ((Number) start.get("x")).doubleValue();
            y = ((Number) start.get("y")).doubleValue();
            name = auto.get("startAt") == null ? "the start" : (String) auto.get("startAt");
            spot = null;
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
                    spot = (String) named;
                    parked = Boolean.TRUE.equals(card.get("park"));
                } else if ("action".equals(kind) && "LaunchAllAimed".equals(card.get("name"))) {
                    if (!parked || spot == null || !spot.endsWith("_PARK")) {
                        elsewhere.add(name + " (" + x + ", " + y + ") LaunchAllAimed is fired only from a …_PARK spot,"
                                + " right after the park card that drove there");
                    }
                } else if ("action".equals(kind) && "LaunchAll".equals(card.get("name"))) {
                    String problem = problem();
                    if (problem != null) elsewhere.add(name + " (" + x + ", " + y + ") " + problem);
                } else if ("firstOf".equals(kind) && card.get("rows") != null) {
                    double x0 = x, y0 = y;
                    String name0 = name, spot0 = spot;
                    boolean parked0 = parked;
                    for (Object row : (List<Object>) card.get("rows")) {
                        x = x0;
                        y = y0;
                        name = name0;
                        spot = spot0;
                        parked = parked0;
                        Object inner = ((Map<String, Object>) row).get("cards");
                        if (inner != null) cards((List<Object>) inner);
                    }
                }
            }
        }

        /**
         * Why a volley from where the robot is breaks the rule, or null when it is from a start or a
         * named spot fit to launch from. The spot's own heading is the robot's: the path ends on it.
         */
        @SuppressWarnings("unchecked")
        private String problem() {
            double rx = blue ? FieldFrame.FIELD_SIZE_INCHES - x : x;
            double ry = blue ? FieldFrame.FIELD_SIZE_INCHES - y : y;
            if (atStart(rx, ry)) return null;
            List<Object> pose = spot == null ? null : (List<Object>) points.get(spot);
            if (pose == null || pose.size() < 3) return "is not a named spot with a heading";
            double h = Math.toRadians(((Number) pose.get(2)).doubleValue() + (blue ? 180 : 0));
            return AutoLaunchSpots.launchSpotProblem(rx, ry, h,
                    TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn);
        }
    }

    /** ({@code x}, {@code y}), drawn for red, is a start (a team …_START spot). */
    private static boolean atStart(double x, double y) {
        for (org.firstinspires.ftc.teamcode.localization.StartPosition start : StartPositions.known()) {
            if (start.alliance != org.firstinspires.ftc.teamcode.util.Alliance.RED) continue;
            if (Math.hypot(start.pose.x() - x, start.pose.y() - y) <= SAME_IN) return true;
        }
        return false;
    }
}
