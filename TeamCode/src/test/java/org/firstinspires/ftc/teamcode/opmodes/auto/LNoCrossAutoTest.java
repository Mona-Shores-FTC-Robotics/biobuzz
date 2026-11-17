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
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LNoCrossAuto;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * L-NoCross's INIT row {@code then}: what it does when TIP 1 has not come by its wait's time.
 * CROSS AND FIX IT (as drawn) crosses under the HIVE and fixes TIP 1 itself; KEEP WAITING stays on
 * LEFT_AUTO_HIVE for a partner that fixes its own miss (R-NoCross does). Control flow only: every
 * drive takes half a second and every command three, so the times are not the field's.
 */
public class LNoCrossAutoTest {
    private static final String CROSSING = "path under the HIVE to RIGHT_AUTO_FIX_TIP";

    private double now;
    /** When the RIGHT CELL tips and the LEFT comes up (TIP 1). */
    private double tip1 = Double.POSITIVE_INFINITY;
    private boolean feeding;
    /** Where each LaunchAll began, in order. */
    private final List<Pose> launchedFrom = new ArrayList<>();
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
                    feeding = name.equals("LaunchAll");
                    if (feeding) launchedFrom.add(drive.pose);
                })
                .setDone(() -> now - began[0] >= seconds)
                .setEnd(end -> feeding = false);
    }

    private boolean leftUp() { return now >= tip1; }

    private AutoKit kit() {
        AutoRegistry registry = new AutoRegistry().holdTimeLimitsWhile(() -> feeding)
                .command("SpinUp", 0, () -> lasting("SpinUp", 0))
                .command("LaunchAll", 3, () -> lasting("LaunchAll", 3))
                .command("PickFlower", 3, () -> lasting("PickFlower", 3))
                // The CELL at the robot's end of the field (red: LEFT at the scoring end).
                .trigger("CellUp", () -> drive.pose.y() >= FieldFrame.FIELD_CENTRE_INCHES ? leftUp() : !leftUp())
                .trigger("RightCellUp", () -> !leftUp());
        return new AutoKit(drive, registry, () -> now).trace(trace::add).picks(new AutoKit.Picks() {
            @Override public int choice(String option) { return choices.getOrDefault(option, -1); }
            @Override public double seconds(String option) { return Double.NaN; }
        });
    }

    private void run() {
        Command command = LNoCrossAuto.build(kit(), false);
        command.schedule();
        for (int i = 0; i < 1800 && command.isScheduled(); i++) {
            Scheduler.execute();
            now += 0.02;
        }
        assertFalse("Still running: " + trace, command.isScheduled());
    }

    private void parkedLeft() {
        assertEquals(14.5, drive.pose.x(), .01);
        assertEquals(117.4, drive.pose.y(), .01);
        assertTrue("parked after AUTO: " + now, now < 30);
    }

    private void keepWaiting() {
        choices.put("then", 1);
    }

    @Test public void theMenuOffersCrossFirstThenKeepWaiting() {
        assertEquals("then", LNoCrossAuto.OPTIONS[1].name);
        assertEquals("[CROSS AND FIX IT, KEEP WAITING]", LNoCrossAuto.OPTIONS[1].choices.toString());
    }

    /** TIP 1 on time: the pick never matters, L shoots LEFT from its own end. */
    @Test public void tip1OnTimeRunsTheNormalRouteWhicheverIsPicked() {
        tip1 = 4;
        keepWaiting();
        run();
        assertFalse(trace.toString(), trace.contains(CROSSING));
        assertTrue("shot LEFT: " + launchedFrom, launchedFrom.get(0).y() > FieldFrame.FIELD_CENTRE_INCHES);
        parkedLeft();
    }

    /** CROSS AND FIX IT (as drawn): no TIP 1 by 9 s, L crosses and shoots its preloads at RIGHT. */
    @Test public void crossAndFixItCrossesWhenTip1DoesNotCome() {
        run();
        assertTrue(trace.toString(), trace.contains(CROSSING));
        assertTrue("fixed TIP 1 at RIGHT: " + launchedFrom, launchedFrom.get(0).y() < FieldFrame.FIELD_CENTRE_INCHES);
        parkedLeft();
    }

    /** KEEP WAITING with a partner that never fixes it: L stays home, holds its load, parks. */
    @Test public void keepWaitingNeverCrossesAndParksWhenTip1NeverComes() {
        keepWaiting();
        run();
        assertFalse(trace.toString(), trace.contains(CROSSING));
        assertTrue("no volley at a down CELL: " + launchedFrom, launchedFrom.isEmpty());
        assertTrue("waited to 23 s: " + drive.parkAt, drive.parkAt >= 23);
        parkedLeft();
    }

    /** KEEP WAITING beside a partner that fixes its own miss (R-NoCross's, about 14 s): L makes TIP 2. */
    @Test public void keepWaitingShootsLeftWhenThePartnerFixesTip1Late() {
        tip1 = 14;
        keepWaiting();
        run();
        assertFalse(trace.toString(), trace.contains(CROSSING));
        assertTrue("shot LEFT after the late TIP: " + launchedFrom,
                !launchedFrom.isEmpty() && launchedFrom.get(0).y() > FieldFrame.FIELD_CENTRE_INCHES);
        parkedLeft();
    }

    private final class FakeDrive implements AutoDrive {
        Pose pose = LNoCrossAuto.startPose(false);
        double doneAt;
        double parkAt = -1;

        @Override public void follow(Path path) {
            pose = path.endPose();
            if (Math.abs(pose.x() - 14.5) < .01) parkAt = now;
            doneAt = now + 0.5;
        }
        @Override public boolean pathDone() { return now >= doneAt; }
        @Override public Pose pose() { return pose; }
        @Override public boolean poseReferenced() { return true; }
        @Override public void hold(Pose p) { pose = p; }
    }
}
