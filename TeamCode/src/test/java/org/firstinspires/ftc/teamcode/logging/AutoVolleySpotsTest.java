package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.AutoLaunchSpots;
import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.firstinspires.ftc.teamcode.localization.TeamSpot;
import org.firstinspires.ftc.teamcode.localization.generated.TeamSpots;
import org.firstinspires.ftc.teamcode.opmodes.auto.CatchNectarAimed;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
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
 * ({@code drafts/}, {@code partners/}) can use other poses for ordinary volleys.
 *
 * <p>One exception, narrow (mentor, 19 Nov 2026): {@code LaunchAllAimed}, the turret aimed from where the robot stands at the
 * CELL at its end ({@code AimedVolley}), is fired from a park spot (a team spot or the Auto's own, named {@code …_PARK}) right
 * after the park card that drove there, and nowhere else. The park comes first; the aim is the TeleOp's, at the near
 * CELL only. A second exception is {@code LaunchOneAimed} (mentor, 28 Nov 2026): one piece, turret-aimed, from a
 * named spot whose heading need not aim a locked turret, as long as the aim is inside the firing arc with margin
 * ({@code AutoLaunchSpots.aimedShotProblem}).
 * CatchNectarAimed requires a named receiving pose belonging to the Auto and a valid near-CELL bearing.
 */
public class AutoVolleySpotsTest {

    /** A volley's place and an allowed spot's are the same within this, inches (a .pp is to 0.1 in). */
    private static final double SAME_IN = 0.06;

    @Test
    public void everyVolleyIsFromAStartOrASpotFitToLaunchFrom() throws Exception {
        checkSources("autos", false);
        checkSources("autos/drafts", true);
    }

    private static void checkSources(String directory, boolean catchesOnly) throws Exception {
        File[] sources = new File(TeamCodeDir.get(), directory).listFiles((dir, name) -> name.endsWith(".pp"));
        assertTrue("missing Auto sources in " + directory, sources != null && (catchesOnly || sources.length > 0));
        for (File source : sources) {
            List<String> elsewhere = volleysElsewhere(source, catchesOnly);
            assertTrue(source.getName() + " launches from " + elsewhere + ": an Auto launches from its start"
                    + " or a named spot aimed at its CELL (AutoLaunchSpots.launchSpotProblem)", elsewhere.isEmpty());
        }
    }

    /** The places {@code source}'s volleys are fired from that are neither its start nor a spot fit to launch from, and why. */
    @SuppressWarnings("unchecked")
    private static List<String> volleysElsewhere(File source, boolean catchesOnly) throws Exception {
        Map<String, Object> doc = (Map<String, Object>) MiniJson.parse(
                new String(Files.readAllBytes(source.toPath()), StandardCharsets.UTF_8));
        Map<String, Object> auto = (Map<String, Object>) doc.get("auto");
        assertTrue(source.getName() + " has no Auto (cards) to read", auto != null && auto.get("cards") != null);
        Walk walk = new Walk(doc, auto, catchesOnly);
        walk.cards((List<Object>) auto.get("cards"));
        return walk.elsewhere;
    }

    @Test
    public void catchNeedsTheNearCellInsideTheArcAndTheFrameInsideTheField() {
        assertNull(receivingPoseProblem(40.7, 120.5, Math.toRadians(210)));
        assertNotNull(receivingPoseProblem(40.7, 120.5, Math.toRadians(200)));
        assertNotNull(receivingPoseProblem(FieldFrame.FIELD_CENTRE_INCHES - 1, 120.5, Math.toRadians(210)));
    }

    private static String receivingPoseProblem(double x, double y, double h) {
        String frame = AutoLaunchSpots.frameProblem(x, y, h);
        if (frame != null) return frame;
        double angle = CatchNectarAimed.spotBearing(new Pose(x, y, h), Alliance.RED);
        return CatchNectarAimed.insideArc(angle) ? null : "near CELL outside the usable firing arc at " + angle;
    }

    private static final class Walk {
        private final Map<String, Object> lines = new java.util.HashMap<>();
        private final Map<String, Object> pathEnds;
        private final Map<String, Object> points;
        /** The Auto is drawn for blue: its poses are turned half a turn to red's before they are checked. */
        private final boolean blue;
        private final boolean catchesOnly;
        /** Where the robot is: x, y, what to call it, and the named spot it is on (null: none). */
        private double x, y;
        private String name, spot;
        /** The last path card was a park ({@code "park": true}). */
        private boolean parked;
        final List<String> elsewhere = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Walk(Map<String, Object> doc, Map<String, Object> auto, boolean catchesOnly) {
            this.catchesOnly = catchesOnly;
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
                } else if ("action".equals(kind) && "CatchNectarAimed".equals(card.get("name"))) {
                    String problem = catchProblem();
                    if (problem != null) elsewhere.add("CatchNectarAimed at " + name + ": " + problem);
                } else if (!catchesOnly && "action".equals(kind) && "LaunchAllAimed".equals(card.get("name"))) {
                    // Right after a park card: the parked volley, from a …_PARK spot. Anywhere else it is an
                    // ordinary aimed volley (AutoKit.commandAwayFromPark), from a named spot fit to aim from.
                    if (parked && (spot == null || !spot.endsWith("_PARK"))) {
                        elsewhere.add(name + " (" + x + ", " + y + ") LaunchAllAimed after a park is fired only from a …_PARK spot");
                    } else if (spot == null) {
                        elsewhere.add(name + " (" + x + ", " + y + ") LaunchAllAimed is fired only from a named spot");
                    } else if (aimedProblem() != null) {
                        elsewhere.add(name + " (" + x + ", " + y + ") LaunchAllAimed " + aimedProblem());
                    }
                } else if (!catchesOnly && "action".equals(kind) && "LaunchOneAimed".equals(card.get("name"))) {
                    String problem = aimedProblem();
                    if (problem != null) elsewhere.add(name + " (" + x + ", " + y + ") LaunchOneAimed " + problem);
                } else if (!catchesOnly && "action".equals(kind) && "LaunchAll".equals(card.get("name"))) {
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

        @SuppressWarnings("unchecked")
        private String catchProblem() {
            for (TeamSpot shared : TeamSpots.all()) {
                if (shared.name.equals(spot)) return "requires an Auto's own named receiving pose";
            }
            List<Object> pose = spot == null ? null : (List<Object>) points.get(spot);
            if (pose == null || pose.size() < 3 || !(pose.get(2) instanceof Number)) {
                return "missing receiving pose or chassis heading";
            }
            if (Math.hypot(((Number) pose.get(0)).doubleValue() - x,
                    ((Number) pose.get(1)).doubleValue() - y) > SAME_IN) return "path ends off the receiving pose";
            double rx = blue ? FieldFrame.FIELD_SIZE_INCHES - x : x;
            double ry = blue ? FieldFrame.FIELD_SIZE_INCHES - y : y;
            double h = Math.toRadians(((Number) pose.get(2)).doubleValue() + (blue ? 180 : 0));
            return receivingPoseProblem(rx, ry, h);
        }

        /** Why a LaunchOneAimed from where the robot is breaks its rule, or null: a named spot with a heading, the aim inside the arc with margin. */
        @SuppressWarnings("unchecked")
        private String aimedProblem() {
            double rx = blue ? FieldFrame.FIELD_SIZE_INCHES - x : x;
            double ry = blue ? FieldFrame.FIELD_SIZE_INCHES - y : y;
            List<Object> pose = spot == null ? null : (List<Object>) points.get(spot);
            if (pose == null || pose.size() < 3) return "is not fired from a named spot with a heading";
            double h = Math.toRadians(((Number) pose.get(2)).doubleValue() + (blue ? 180 : 0));
            return AutoLaunchSpots.aimedShotProblem(rx, ry, h, TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn,
                    TurretSubsystem.arcLeftDeg, TurretSubsystem.arcRightDeg);
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

    /**
     * The one-ball aimed shot's rule (mentor, 28 Nov 2026), a spot whose heading faces the LOADING ZONE and whose
     * turret alone holds the CELL: any named spot, a team spot or the Auto's own, where the turret's aim at the CELL
     * at its end is inside the firing arc by {@code AutoLaunchSpots.AIMED_SHOT_ARC_MARGIN_DEG}, the frame inside
     * the field and short of the centre line.
     */
    @Test
    public void anAimedShotsSpotKeepsTheAimInsideTheArcWithMargin() {
        double f = TurretSubsystem.turretForwardIn, l = TurretSubsystem.turretLeftIn;
        // LEFT_AUTO_OFFSET's place: 215 deg puts the aim about 84 deg left (inside 90 less 5); 205 deg about 94 (outside).
        assertTrue(AutoLaunchSpots.aimedShotProblem(40.2, 120.8, Math.toRadians(215), f, l, 90, 90) == null);
        assertTrue(AutoLaunchSpots.aimedShotProblem(40.2, 120.8, Math.toRadians(205), f, l, 90, 90).contains("outside the firing arc"));
        assertTrue("88.7 deg is inside the arc but not by the margin",
                AutoLaunchSpots.aimedShotProblem(40.2, 120.8, Math.toRadians(210), f, l, 90, 90) != null);
        assertTrue("over the centre line", AutoLaunchSpots.aimedShotProblem(66, 120.8, Math.toRadians(250), f, l, 90, 90) != null);
        // A spot that aims a locked turret is fit for it too: the aim is dead ahead.
        assertTrue(AutoLaunchSpots.aimedShotProblem(40.7, 121.1, Math.toRadians(298.7), f, l, 90, 90) == null);
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
