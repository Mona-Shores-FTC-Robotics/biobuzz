package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.*;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import org.firstinspires.ftc.teamcode.autokit.AutoDrive;
import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LInfravioletAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RInfravioletAuto;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

/** Control-flow checks, not a physics or scoring qualification of the paired paths. */
public class InfravioletAutoTest {
    private double now;
    private double firstTip = 5;
    private double secondTip = 12;
    private boolean lateRightArrival;
    private boolean feeding;
    private double firstLaunchAt = -1;
    private double gardenAt = -1;
    private final List<String> actions = new ArrayList<>();
    private final List<String> trace = new ArrayList<>();
    private FakeDrive drive;

    @Before public void reset() { Scheduler.reset(); drive = new FakeDrive(); }

    private Command lasting(String name, double seconds) {
        double[] began = new double[1];
        return new CommandBuilder().setStart(() -> { began[0] = now; actions.add(name); feeding = name.equals("LaunchAll");
                    if (feeding && firstLaunchAt < 0) firstLaunchAt = now;
                    if (name.equals("GardenGather")) gardenAt = now; })
                .setDone(() -> now - began[0] >= seconds).setEnd(end -> feeding = false);
    }

    private AutoKit kit() {
        AutoRegistry registry = new AutoRegistry().holdTimeLimitsWhile(() -> feeding)
                .command("SpinUp", 0, () -> lasting("SpinUp", 0))
                .command("LaunchAll", 3, () -> lasting("LaunchAll", 3))
                .command("PickFlower", 3, () -> lasting("PickFlower", 3))
                .command("GardenGather", 4.21, () -> lasting("GardenGather", 4.21))
                .trigger("LeftCellUp", () -> now >= firstTip && now < secondTip)
                .trigger("RightCellUp", () -> now >= secondTip + 0.7)
                .triggerSince("Tip", () -> () -> now >= secondTip && now < secondTip + 0.7);
        return new AutoKit(drive, registry, () -> now).trace(trace::add);
    }

    private void run(Command command) {
        command.schedule();
        for (int i = 0; i < 1800 && command.isScheduled(); i++) {
            Scheduler.execute();
            now += 0.02;
        }
        assertFalse("Still running: " + trace, command.isScheduled());
    }

    private long launches() { return actions.stream().filter("LaunchAll"::equals).count(); }
    private void parked(double y) {
        assertEquals(14.5, drive.pose.x(), .01);
        assertEquals(y, drive.pose.y(), .01);
        assertTrue("parked after AUTO: " + now, now < 30);
    }

    @Test public void leftWaitsForFirstTipThenCollectsGardenBeforeSecondTip() {
        secondTip = 18;
        run(LInfravioletAuto.build(kit(), false));
        assertEquals(2, launches());
        assertTrue(firstLaunchAt >= firstTip);
        assertTrue(gardenAt > firstLaunchAt && gardenAt < secondTip);
        assertFalse(actions.contains("PickFlower"));
        parked(117.4);
    }

    @Test public void missingSecondTipSkipsTheRightVolleyAndUsesTheFarPark() {
        secondTip = Double.POSITIVE_INFINITY;
        run(LInfravioletAuto.build(kit(), false));
        assertEquals(1, launches());
        assertTrue(actions.contains("GardenGather"));
        assertTrue(drive.parkAt >= 20);
        parked(117.4);
    }

    @Test public void missingFirstTipSkipsBothVolleysAndGarden() {
        firstTip = secondTip = Double.POSITIVE_INFINITY;
        run(LInfravioletAuto.build(kit(), false));
        assertEquals(0, launches());
        assertFalse(actions.contains("GardenGather"));
        assertEquals(2, drive.paths); // opening and its assigned park only
        assertTrue(drive.parkAt >= 8);
        parked(117.4);
    }

    @Test public void rightCompletesThreeVolleysWithBothFlowersAndUsesTheNearPark() {
        run(RInfravioletAuto.build(kit(), false));
        assertEquals(3, launches());
        assertEquals(2, actions.stream().filter("PickFlower"::equals).count());
        assertFalse(actions.contains("GardenGather"));
        parked(95.7);
    }

    @Test public void rightSkipsScoringFlowerWhenSecondTipNeverSettles() {
        secondTip = Double.POSITIVE_INFINITY;
        run(RInfravioletAuto.build(kit(), false));
        assertEquals(2, launches());
        assertEquals(1, actions.stream().filter("PickFlower"::equals).count());
        assertTrue(drive.parkAt >= 17);
        parked(95.7);
    }

    @Test public void leftParksWhenFirstTipArrivesAtTheDeadline() {
        firstTip = 8;
        run(LInfravioletAuto.build(kit(), false));
        assertEquals(0, launches());
        assertFalse(actions.contains("GardenGather"));
        parked(117.4);
    }

    @Test public void lateFinalArrivalParksEvenWhenRightCellIsAlreadyUp() {
        lateRightArrival = true;
        run(RInfravioletAuto.build(kit(), false));
        assertEquals(2, launches());
        assertTrue(trace.stream().anyMatch(s -> s.contains("22") && s.contains("after")));
        parked(95.7);
    }

    private final class FakeDrive implements AutoDrive {
        Pose pose = RInfravioletAuto.startPose(false);
        double doneAt;

        double parkAt = -1;
        int paths;
        @Override public void follow(Path path) {
            paths++;
            pose = path.endPose();
            if (Math.abs(pose.x() - 14.5) < .01) parkAt = now;
            if (lateRightArrival && Math.abs(pose.x() - 60.5) < .01 && Math.abs(pose.y() - 31) < .01) now = 23;
            doneAt = now + 0.5;
        }
        @Override public boolean pathDone() { return now >= doneAt; }
        @Override public Pose pose() { return pose; }
        @Override public boolean poseReferenced() { return true; }
        @Override public void hold(Pose p) { pose = p; }
    }
}
