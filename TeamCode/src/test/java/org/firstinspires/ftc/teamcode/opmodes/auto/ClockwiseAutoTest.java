package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.autokit.AutoDrive;
import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.autokit.AutoOption;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.localization.AutoLaunchSpots;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LClockwiseAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RClockwiseAuto;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Clockwise pair's stops, as the generated Autos run them: which row each takes and what it
 * does then. Control flow only: every drive takes half a second and every command three, so the
 * times here are not the field's (the simulator has those, {@code TeamCode/autos/clockwise.md}).
 */
public class ClockwiseAutoTest {
    private double now;
    /** When the LEFT CELL is seen up (TIP 1), and when it goes down and the RIGHT comes up (TIP 2). */
    private double firstTip = 5;
    private double secondTip = 12;
    private boolean feeding;
    /** Where the robot stood, along the field's y, when its first LaunchAll began. */
    private double launchedFromY = Double.NaN;
    /** Where each LaunchAll began, in order. */
    private final List<Pose> launchedFrom = new ArrayList<>();
    private final List<String> actions = new ArrayList<>();
    private final Map<String, Double> startedAt = new HashMap<>();
    private final List<String> trace = new ArrayList<>();
    private final Map<String, Integer> choices = new HashMap<>();
    private FakeDrive drive;

    @Before public void reset() {
        Scheduler.reset();
        drive = new FakeDrive();
    }

    private Command lasting(String name, double seconds) {
        double[] began = new double[1];
        return new CommandBuilder()
                .setStart(() -> {
                    began[0] = now;
                    actions.add(name);
                    startedAt.putIfAbsent(name, now);
                    feeding = name.equals("LaunchAll");
                    if (feeding && Double.isNaN(launchedFromY)) launchedFromY = drive.pose.y();
                    if (feeding) launchedFrom.add(drive.pose);
                })
                .setDone(() -> now - began[0] >= seconds)
                .setEnd(end -> feeding = false);
    }

    private AutoKit kit() {
        AutoRegistry registry = new AutoRegistry().holdTimeLimitsWhile(() -> feeding)
                .command("SpinUp", 0, () -> lasting("SpinUp", 0))
                .command("LaunchAll", 3, () -> lasting("LaunchAll", 3))
                .command("PickFlower", 3, () -> lasting("PickFlower", 3))
                .command("GardenGather", 3, () -> lasting("GardenGather", 3))
                .parkedVolley("LaunchAllAimed", 3, 3, () -> lasting("LaunchAllAimed", 3))
                .trigger("LeftCellUp", () -> now >= firstTip && now < secondTip)
                .trigger("RightCellUp", () -> now >= secondTip + 0.7)
                .trigger("Always", () -> true);
        return new AutoKit(drive, registry, () -> now).trace(trace::add).picks(new AutoKit.Picks() {
            @Override public int choice(String option) { return choices.getOrDefault(option, -1); }
            @Override public double seconds(String option) { return Double.NaN; }
        });
    }

    private void run(Command command) {
        command.schedule();
        for (int i = 0; i < 1800 && command.isScheduled(); i++) {
            Scheduler.execute();
            now += 0.02;
        }
        assertFalse("Still running: " + trace, command.isScheduled());
    }

    private long count(String name) {
        return actions.stream().filter(name::equals).count();
    }

    /** R's park, RIGHT_PARK. */
    private void parked(double y) {
        parked(14.5, y);
    }

    /** L's park, LEFT_AIMED_PARK: turned at the LEFT CELL so its camera sees it, deeper in the LOADING ZONE. */
    private void parkedLeft() {
        parked(13.0, 119.0);
    }

    private void parked(double x, double y) {
        assertEquals(x, drive.pose.x(), .01);
        assertEquals(y, drive.pose.y(), .01);
        assertTrue("parked after AUTO: " + now, now < 30);
    }

    private static final String CROSS = "path BACKWARD under the HIVE from the scoring FLOWER to RIGHT_AUTO_FIX_TIP";

    // ---- R-Clockwise -------------------------------------------------------------------------

    /**
     * The normal plan with time to spare (Priority THREE_TIP): three volleys, the WALL FLOWER then the SCORING
     * FLOWER, from which R backs straight under the HIVE without looking, fires at RIGHT and parks on RIGHT_PARK.
     */
    @Test public void rightShootsThreeVolleysAndParksRightWhenThereIsTime() {
        run(RClockwiseAuto.build(kit(), false));
        assertEquals(3, count("LaunchAll"));
        assertEquals(2, count("PickFlower"));
        assertEquals(0, count("GardenGather"));
        assertTrue("it crossed under the HIVE: " + trace, trace.contains(CROSS));
        assertFalse("THREE_TIP does not look: " + trace, trace.contains("path turn to LEFT_AUTO_HIVE"));
        parked(98.7);
    }

    /**
     * THREE_TIP and TIP 2 never comes: R has crossed blind, so it waits at RIGHT_AUTO_FIX_TIP for RIGHT until 25.5 s
     * and then parks with its load. (L's park shot is what finishes TIP 2 then.)
     */
    @Test public void rightParksWithItsLoadAtTheRightEndWhenRightNeverComesUp() {
        secondTip = Double.POSITIVE_INFINITY;
        run(RClockwiseAuto.build(kit(), false));
        assertTrue(trace.toString(), trace.contains(CROSS));
        assertEquals("the preloads and the first load", 2, count("LaunchAll"));
        assertTrue("not before 25.5 s: " + drive.parkAt, drive.parkAt >= 25.5);
        parked(98.7);
    }

    @Test public void rightTakesTheGardenWhenPicked() {
        choices.put("Pickup", 1);
        run(RClockwiseAuto.build(kit(), false));
        assertEquals(1, count("GardenGather"));
        assertEquals("only the SCORING FLOWER", 1, count("PickFlower"));
        assertTrue(trace.toString(), trace.contains("path outside lane BACKWARD from the GARDEN to LEFT_AUTO_HIVE"));
        assertFalse(trace.toString(), trace.contains("path to WALL_FLOWER_STAGING"));
        parked(98.7);
    }

    /** R2 "no": LEFT is not up by T2, so R parks with its load. */
    @Test public void rightParksAtT2WhenLeftNeverComesUp() {
        firstTip = secondTip = Double.POSITIVE_INFINITY;
        run(RClockwiseAuto.build(kit(), false));
        assertEquals("the preloads only", 1, count("LaunchAll"));
        assertEquals(1, count("PickFlower"));
        assertTrue("not before T2: " + drive.parkAt, drive.parkAt >= 15.5);
        parked(98.7);
    }

    /** Priority PARK, RIGHT never comes up (TIP 2 has not come): R looks from the left end, stays, and finishes TIP 2 there. */
    @Test public void rightStaysLeftAndFinishesTip2WhenRightIsNotUp() {
        choices.put("Priority", 1);
        secondTip = Double.POSITIVE_INFINITY;
        run(RClockwiseAuto.build(kit(), false));
        assertFalse(trace.toString(), trace.contains(CROSS));
        assertTrue(trace.toString(), trace.contains("path turn to LEFT_AUTO_HIVE"));
        assertEquals("the preloads, the first load, and the SCORING FLOWER load at LEFT", 3, count("LaunchAll"));
        parked(98.7);
    }

    /**
     * A late pick (before T3b), and RIGHT is not up by T3: R goes to the close-row spot
     * in front of the LEFT CELL (the close row), finishes TIP 2 there, then waits for L to pass before it parks.
     */
    @Test public void rightShootsAtLeftAgainWhenRightIsNotUpAndParksAfterLHasPassed() {
        choices.put("Priority", 1);
        secondTip = Double.POSITIVE_INFINITY;
        drive.arriveAt(47.2, 119.3, 18.0);  // the pick ends at 21.0 s: before T3b
        run(RClockwiseAuto.build(kit(), false));
        assertEquals("preloads, the first load, the SCORING FLOWER load", 3, count("LaunchAll"));
        assertFalse("it never crossed: " + trace, trace.contains(CROSS));
        assertTrue(trace.toString(), trace.contains("path turn to LEFT_AUTO_HIVE"));
        assertTrue("not before TR: " + drive.parkAt, drive.parkAt >= 26.8);
        parked(98.7);
    }

    /** Past T3b the rescue volley and the park after it would not fit: R parks straight from the SCORING FLOWER. */
    @Test public void rightParksFromTheScoringFlowerWhenTheRescueWouldNotFit() {
        choices.put("Priority", 1);
        secondTip = Double.POSITIVE_INFINITY;
        drive.arriveAt(47.2, 119.3, 20.0);
        run(RClockwiseAuto.build(kit(), false));
        assertEquals(2, count("LaunchAll"));
        assertFalse(trace.toString(), trace.contains("path turn to LEFT_AUTO_HIVE"));
        assertTrue("straight from the FLOWER: " + drive.parkAt, drive.parkAt >= 23 && drive.parkAt < 23.5);
        parked(98.7);
    }

    /** RIGHT is up, and the drive team's Priority is the park: R keeps its load, waits for L to pass and parks. */
    @Test public void rightParksWithItsLoadWhenThePriorityIsThePark() {
        choices.put("Priority", 1);
        run(RClockwiseAuto.build(kit(), false));
        assertEquals(2, count("LaunchAll"));
        assertFalse(trace.toString(), trace.contains(CROSS));
        assertTrue(drive.parkAt >= 26.8);
        parked(98.7);
    }

    /** After a late first LEFT volley there is no time for the SCORING FLOWER: R parks from the left spot. */
    @Test public void rightSkipsTheScoringFlowerAfterTs() {
        firstTip = 15.0;
        secondTip = Double.POSITIVE_INFINITY;
        run(RClockwiseAuto.build(kit(), false));
        assertEquals(2, count("LaunchAll"));
        assertEquals("the WALL FLOWER only", 1, count("PickFlower"));
        parked(98.7);
    }

    // ---- L-Clockwise -------------------------------------------------------------------------

    @Test public void leftShootsAtLeftThenGathersAndShootsAtRightAndLeavesAtTl() {
        run(LClockwiseAuto.build(kit(), false));
        assertEquals(2, count("LaunchAll"));
        assertFalse(trace.toString(), trace.contains("path under the HIVE to RIGHT_AUTO_FIX_TIP"));
        assertEquals(1, count("GardenGather"));
        assertTrue("it waited for TIP 1", startedAt.get("LaunchAll") >= firstTip);
        assertTrue("not before TL: " + drive.parkAt, drive.parkAt >= 22.7);
        parkedLeft();
    }

    /**
     * L1 "no": LEFT is not up by T1. A CELL is scored only from its own end, so L crosses under the HIVE and
     * shoots its preloads at RIGHT from the right end. There is no time for a pickup after that: it waits for R
     * to park from the left spot, then parks.
     */
    @Test public void leftCrossesAndShootsAtRightFromTheRightEndWhenLeftIsNotUp() {
        firstTip = secondTip = Double.POSITIVE_INFINITY;
        run(LClockwiseAuto.build(kit(), false));
        assertTrue(trace.toString(), trace.contains("path under the HIVE to RIGHT_AUTO_FIX_TIP"));
        assertEquals("the preloads only", 1, count("LaunchAll"));
        assertTrue("not before T1, and only after the crossing", startedAt.get("LaunchAll") >= 9.5 + 0.5);
        assertTrue("it shot from the RIGHT end of the field: " + launchedFromY, launchedFromY < 70.75);
        assertEquals(0, count("GardenGather"));
        assertEquals(0, count("PickFlower"));
        assertTrue("not before TLr: " + drive.parkAt, drive.parkAt >= 22.5);
        parkedLeft();
    }

    @Test public void leftTakesTheWallFlowerWhenPickedAndWaitsForRToPass() {
        choices.put("Pickup", 1);
        run(LClockwiseAuto.build(kit(), false));
        assertEquals(1, count("PickFlower"));
        assertEquals(0, count("GardenGather"));
        assertTrue("not before TF: " + startedAt.get("PickFlower"), startedAt.get("PickFlower") >= 14.0);
        parkedLeft();
    }

    /**
     * L2 "no": RIGHT is not up by T4 (22 s), so L parks on LEFT_AIMED_PARK at once and, LEFT being still up,
     * fires its load at LEFT from there with the turret (LaunchAllAimed) to finish TIP 2.
     */
    @Test public void leftParksAndFiresAtLeftFromItsParkWhenRightIsNotUpByT4() {
        secondTip = 25;
        run(LClockwiseAuto.build(kit(), false));
        assertEquals("the preloads only", 1, count("LaunchAll"));
        assertEquals("the park shot", 1, count("LaunchAllAimed"));
        assertTrue(drive.parkAt >= 22.0 && drive.parkAt < 22.5);
        assertTrue("after the park: " + startedAt.get("LaunchAllAimed"), startedAt.get("LaunchAllAimed") > drive.parkAt);
        parkedLeft();
    }

    // ---- the launch spots are the Autos' own -----------------------------------------------

    private void shotFrom(int volley, String name, Pose spot) {
        Pose from = launchedFrom.get(volley);
        assertEquals(name + " x", spot.x(), from.x(), 0.05);
        assertEquals(name + " y", spot.y(), from.y(), 0.05);
    }

    private void shotFrom(int volley, AutoLaunchSpots.Spot spot) {
        shotFrom(volley, spot.name(), spot.red);
    }

    private void again() {
        reset();
        launchedFrom.clear();
        actions.clear();
        trace.clear();
        now = 0;
    }

    /**
     * Every volley of the pair is from a start or an Auto launch spot ({@link AutoLaunchSpots}). R fires its
     * preloads from the right HIVE spot, moving up to it as it spins up, its second load from the left HIVE spot, and after crossing under the HIVE
     * from the fix-TIP spot straight below the lane; L moves up to the left HIVE spot and fires its second
     * load from the right OFFSET spot, clear of R arriving beside it.
     */
    @Test public void everyVolleyIsFromAStartOrAnAutoLaunchSpot() {
        run(RClockwiseAuto.build(kit(), false));
        shotFrom(0, AutoLaunchSpots.Spot.RIGHT_AUTO_HIVE);
        shotFrom(1, AutoLaunchSpots.Spot.LEFT_AUTO_HIVE);
        shotFrom(2, AutoLaunchSpots.Spot.RIGHT_AUTO_FIX_TIP);
        again();
        run(LClockwiseAuto.build(kit(), false));
        shotFrom(0, AutoLaunchSpots.Spot.LEFT_AUTO_HIVE);
        shotFrom(1, AutoLaunchSpots.Spot.RIGHT_AUTO_OFFSET);
    }

    @Test public void theRescueVolleysAreFromAutoLaunchSpotsToo() {
        choices.put("Priority", 1);
        secondTip = Double.POSITIVE_INFINITY;
        drive.arriveAt(47.2, 119.3, 18.0);
        run(RClockwiseAuto.build(kit(), false));
        shotFrom(2, AutoLaunchSpots.Spot.LEFT_AUTO_HIVE);
        again();
        firstTip = Double.POSITIVE_INFINITY;
        run(LClockwiseAuto.build(kit(), false));
        shotFrom(0, AutoLaunchSpots.Spot.RIGHT_AUTO_FIX_TIP);
    }

    // ---- both alliances ----------------------------------------------------------------------

    /** Blue's side is red's turned half a turn about the field's centre: the parks turn with it. */
    @Test public void bothBuildForBlueAndParkOnBluesSpots() {
        run(RClockwiseAuto.build(kit(), true));
        assertEquals(141.5 - 14.5, drive.pose.x(), .01);
        assertEquals(141.5 - 98.7, drive.pose.y(), .01);
    }

    /** The whole INIT menu: one time each (the wait for the partner's TIP), the pickup, and R's priority. */
    @Test public void theMenusAreOneTimeAndTheChoices() {
        assertEquals(3, RClockwiseAuto.OPTIONS.length);
        assertEquals(AutoOption.Kind.MATCH_SECONDS, RClockwiseAuto.OPTIONS[0].kind);
        assertEquals("Pickup", RClockwiseAuto.OPTIONS[1].name);
        assertEquals("[WALL_FLOWER, GARDEN]", RClockwiseAuto.OPTIONS[1].choices.toString());
        assertEquals(AutoOption.PRIORITY, RClockwiseAuto.OPTIONS[2].name);
        assertEquals("[THREE_TIP, PARK]", RClockwiseAuto.OPTIONS[2].choices.toString());
        assertEquals(2, LClockwiseAuto.OPTIONS.length);
        assertEquals(AutoOption.Kind.MATCH_SECONDS, LClockwiseAuto.OPTIONS[0].kind);
        assertEquals("Pickup", LClockwiseAuto.OPTIONS[1].name);
        // R on its first pickup needs L on L's first: the two lists pair in order.
        assertEquals("[GARDEN, WALL_FLOWER]", LClockwiseAuto.OPTIONS[1].choices.toString());
    }

    private final class FakeDrive implements AutoDrive {
        Pose pose = RClockwiseAuto.startPose(false);
        double doneAt;
        double parkAt = -1;
        /** A spot the robot reaches no sooner than a time: the field's drive, where a test needs it. */
        private double lateX = Double.NaN, lateY, lateAt;

        void arriveAt(double x, double y, double at) {
            lateX = x;
            lateY = y;
            lateAt = at;
        }

        @Override public void follow(Path path) {
            pose = path.endPose();
            // RIGHT_PARK (x 14.5) or L's LEFT_AIMED_PARK (x 13.0), on either alliance's side.
            if (Math.abs(pose.x() - 14.5) < .01 || Math.abs(pose.x() - 127) < .01
                    || Math.abs(pose.x() - 13.0) < .01 || Math.abs(pose.x() - 128.5) < .01) parkAt = now;
            doneAt = now + 0.5;
            if (Math.abs(pose.x() - lateX) < .01 && Math.abs(pose.y() - lateY) < .01) doneAt = Math.max(doneAt, lateAt);
        }
        @Override public boolean pathDone() { return now >= doneAt; }
        @Override public Pose pose() { return pose; }
        @Override public boolean poseReferenced() { return true; }
        @Override public void hold(Pose p) { pose = p; }
    }
}
