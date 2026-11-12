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
import org.firstinspires.ftc.teamcode.localization.LaunchSpots;
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

    private void parked(double y) {
        assertEquals(14.5, drive.pose.x(), .01);
        assertEquals(y, drive.pose.y(), .01);
        assertTrue("parked after AUTO: " + now, now < 30);
    }

    // ---- R-Clockwise -------------------------------------------------------------------------

    /** The normal plan with time to spare: three volleys, the WALL FLOWER then the SCORING FLOWER, RIGHT_PARK. */
    @Test public void rightShootsThreeVolleysAndParksRightWhenThereIsTime() {
        run(RClockwiseAuto.build(kit(), false));
        assertEquals(3, count("LaunchAll"));
        assertEquals(2, count("PickFlower"));
        assertEquals(0, count("GardenGather"));
        assertTrue("it crossed under the HIVE: " + trace, trace.contains("path back under the HIVE to RIGHT_START"));
        parked(98.7);
    }

    @Test public void rightTakesTheGardenWhenPicked() {
        choices.put("Pickup", 1);
        run(RClockwiseAuto.build(kit(), false));
        assertEquals(1, count("GardenGather"));
        assertEquals("only the SCORING FLOWER", 1, count("PickFlower"));
        assertTrue(trace.toString(), trace.contains("path outside lane from the GARDEN to LEFT_WALL_NEAR"));
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

    /** In time to cross but RIGHT never comes up: R keeps its load, lets L leave, and parks by the outside lane. */
    @Test public void rightCrossesInTimeAndParksWithItsLoadWhenRightIsNotUp() {
        secondTip = Double.POSITIVE_INFINITY;
        run(RClockwiseAuto.build(kit(), false));
        assertTrue(trace.toString(), trace.contains("path back under the HIVE to RIGHT_START"));
        assertEquals("the preloads and the first load", 2, count("LaunchAll"));
        assertTrue("not before TRr: " + drive.parkAt, drive.parkAt >= 24.0);
        parked(98.7);
    }

    /**
     * Too late to cross (the pick ends after T3c) but before T3b, and RIGHT is not up by T3: R goes to the wall
     * in front of the LEFT CELL, finishes TIP 2 there, then waits for L to pass before it parks.
     */
    @Test public void rightShootsAtLeftAgainWhenRightIsNotUpAndParksAfterLHasPassed() {
        secondTip = Double.POSITIVE_INFINITY;
        drive.arriveAt(47.2, 119.3, 13.5);  // the pick ends at 16.5 s: after T3c, before T3b
        run(RClockwiseAuto.build(kit(), false));
        assertEquals("preloads, the first load, the SCORING FLOWER load", 3, count("LaunchAll"));
        assertFalse("it never crossed: " + trace, trace.contains("path back under the HIVE to RIGHT_START"));
        assertTrue(trace.toString(), trace.contains("path turn to LEFT_WALL_HIVE"));
        assertTrue("not before TR: " + drive.parkAt, drive.parkAt >= 26.4);
        parked(98.7);
    }

    /** Past T3b the rescue volley and the park after it would not fit: R parks straight from the SCORING FLOWER. */
    @Test public void rightParksFromTheScoringFlowerWhenTheRescueWouldNotFit() {
        secondTip = Double.POSITIVE_INFINITY;
        drive.arriveAt(47.2, 119.3, 20.0);
        run(RClockwiseAuto.build(kit(), false));
        assertEquals(2, count("LaunchAll"));
        assertFalse(trace.toString(), trace.contains("path turn to LEFT_WALL_HIVE"));
        assertTrue("straight from the FLOWER: " + drive.parkAt, drive.parkAt >= 23 && drive.parkAt < 23.5);
        parked(98.7);
    }

    /** Too late to cross, and RIGHT is up: TIP 2 is done, so R keeps its load, waits for L to pass and parks from the left. */
    @Test public void rightDoesNotCrossAfterT3c() {
        secondTip = 18;
        drive.arriveAt(47.2, 119.3, 13.5);
        run(RClockwiseAuto.build(kit(), false));
        assertEquals(2, count("LaunchAll"));
        assertFalse(trace.toString(), trace.contains("path back under the HIVE to RIGHT_START"));
        assertTrue(drive.parkAt >= 26.4);
        parked(98.7);
    }

    /** After a late first LEFT volley there is no time for the SCORING FLOWER: R parks from the left spot. */
    @Test public void rightSkipsTheScoringFlowerAfterTs() {
        firstTip = 14.5;
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
        assertFalse(trace.toString(), trace.contains("path under the HIVE to RIGHT_START"));
        assertEquals(1, count("GardenGather"));
        assertTrue("it waited for TIP 1", startedAt.get("LaunchAll") >= firstTip);
        assertTrue("not before TL: " + drive.parkAt, drive.parkAt >= 22.7);
        parked(117.4);
    }

    /**
     * L1 "no": LEFT is not up by T1. A CELL is scored only from its own end, so L crosses under the HIVE and
     * shoots its preloads at RIGHT from the right end. There is no time for a pickup after that: it waits for R
     * to park from the left spot, then parks.
     */
    @Test public void leftCrossesAndShootsAtRightFromTheRightEndWhenLeftIsNotUp() {
        firstTip = secondTip = Double.POSITIVE_INFINITY;
        run(LClockwiseAuto.build(kit(), false));
        assertTrue(trace.toString(), trace.contains("path under the HIVE to RIGHT_START"));
        assertEquals("the preloads only", 1, count("LaunchAll"));
        assertTrue("not before T1, and only after the crossing", startedAt.get("LaunchAll") >= 9.5 + 0.5);
        assertTrue("it shot from the RIGHT end of the field: " + launchedFromY, launchedFromY < 70.75);
        assertEquals(0, count("GardenGather"));
        assertEquals(0, count("PickFlower"));
        assertTrue("not before TLr: " + drive.parkAt, drive.parkAt >= 22.5);
        parked(117.4);
    }

    @Test public void leftTakesTheWallFlowerWhenPickedAndWaitsForRToPass() {
        choices.put("Pickup", 1);
        run(LClockwiseAuto.build(kit(), false));
        assertEquals(1, count("PickFlower"));
        assertEquals(0, count("GardenGather"));
        assertTrue("not before TF: " + startedAt.get("PickFlower"), startedAt.get("PickFlower") >= 15.5);
        parked(117.4);
    }

    /** L2 "no": RIGHT is not up by T4, so L keeps its load and still leaves at TL. */
    @Test public void leftKeepsItsLoadWhenRightIsNotUpByT4() {
        secondTip = 25;
        run(LClockwiseAuto.build(kit(), false));
        assertEquals("the preloads only", 1, count("LaunchAll"));
        assertTrue(drive.parkAt >= 22.7 && drive.parkAt < 23.5);
        parked(117.4);
    }

    // ---- shots on the timer ------------------------------------------------------------------

    /** On the timer nobody looks at the HIVE: each stop takes its "yes" row at its time. */
    @Test public void onTheTimerLeftShootsAtLeftAtT1AndAtRightAtT4() {
        choices.put(AutoOption.SHOT_TRIGGER, 1);
        firstTip = secondTip = Double.POSITIVE_INFINITY;
        run(LClockwiseAuto.build(kit(), false));
        assertFalse("never the crossing for TIP 1: " + trace, trace.contains("path under the HIVE to RIGHT_START"));
        assertEquals(2, count("LaunchAll"));
        assertEquals(9.5, startedAt.get("LaunchAll"), 0.05);
        parked(117.4);
    }

    @Test public void onTheTimerRightShootsAtLeftAtT2() {
        choices.put(AutoOption.SHOT_TRIGGER, 1);
        firstTip = secondTip = Double.POSITIVE_INFINITY;
        run(RClockwiseAuto.build(kit(), false));
        assertEquals("the preloads, then the first load at T2", 2, count("LaunchAll"));
        parked(98.7);
    }

    // ---- the launch spots are the TeleOp's ---------------------------------------------------

    private void shotFrom(int volley, LaunchSpots.Spot spot) {
        Pose from = launchedFrom.get(volley);
        assertEquals(spot + " x", spot.red.x(), from.x(), 0.05);
        assertEquals(spot + " y", spot.red.y(), from.y(), 0.05);
    }

    private void again() {
        reset();
        launchedFrom.clear();
        actions.clear();
        trace.clear();
        now = 0;
    }

    /** Every volley of the pair is from a wall-row spot the drivers' Square goes to ({@link LaunchSpots}): one set to tune. */
    @Test public void everyVolleyIsFromASquareSpot() {
        run(RClockwiseAuto.build(kit(), false));
        shotFrom(0, LaunchSpots.Spot.RIGHT_START);
        shotFrom(1, LaunchSpots.Spot.LEFT_WALL_NEAR);
        shotFrom(2, LaunchSpots.Spot.RIGHT_START);
        again();
        run(LClockwiseAuto.build(kit(), false));
        shotFrom(0, LaunchSpots.Spot.LEFT_WALL_HIVE);
        shotFrom(1, LaunchSpots.Spot.RIGHT_WALL_NEAR);
    }

    @Test public void theRescueVolleysAreFromSquareSpotsToo() {
        secondTip = Double.POSITIVE_INFINITY;
        drive.arriveAt(47.2, 119.3, 13.5);
        run(RClockwiseAuto.build(kit(), false));
        shotFrom(2, LaunchSpots.Spot.LEFT_WALL_HIVE);
        again();
        firstTip = Double.POSITIVE_INFINITY;
        run(LClockwiseAuto.build(kit(), false));
        shotFrom(0, LaunchSpots.Spot.RIGHT_START);
    }

    // ---- both alliances ----------------------------------------------------------------------

    /** Blue's side is red's turned half a turn about the field's centre: the parks turn with it. */
    @Test public void bothBuildForBlueAndParkOnBluesSpots() {
        run(RClockwiseAuto.build(kit(), true));
        assertEquals(141.5 - 14.5, drive.pose.x(), .01);
        assertEquals(141.5 - 98.7, drive.pose.y(), .01);
    }

    /** What the menu offers, as the drive teams read it to each other. */
    @Test public void theTwoMenusNameEachOthersPickup() {
        assertEquals("[WALL_FLOWER, GARDEN]", RClockwiseAuto.OPTIONS[0].choices.toString());
        assertEquals("[GARDEN, WALL_FLOWER]", LClockwiseAuto.OPTIONS[0].choices.toString());
        AutoOption rightsPartner = RClockwiseAuto.OPTIONS[RClockwiseAuto.OPTIONS.length - 1];
        AutoOption leftsPartner = LClockwiseAuto.OPTIONS[LClockwiseAuto.OPTIONS.length - 1];
        assertEquals("L-Clockwise", rightsPartner.name);
        assertEquals("R-Clockwise", leftsPartner.name);
        // R on its first choice needs L on L's first, and so on down the two lists.
        assertEquals(LClockwiseAuto.OPTIONS[0].choices, rightsPartner.choices);
        assertEquals(RClockwiseAuto.OPTIONS[0].choices, leftsPartner.choices);
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
            if (Math.abs(pose.x() - 14.5) < .01 || Math.abs(pose.x() - 127) < .01) parkAt = now;
            doneAt = now + 0.5;
            if (Math.abs(pose.x() - lateX) < .01 && Math.abs(pose.y() - lateY) < .01) doneAt = Math.max(doneAt, lateAt);
        }
        @Override public boolean pathDone() { return now >= doneAt; }
        @Override public Pose pose() { return pose; }
        @Override public boolean poseReferenced() { return true; }
        @Override public void hold(Pose p) { pose = p; }
    }
}
