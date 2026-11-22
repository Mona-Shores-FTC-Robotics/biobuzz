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
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RNoCrossAuto;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * R-NoCross's INIT rows: its wait for TIP 2 ("waiting for TIP 2 until 20 s") and {@code if no TIP 2}:
 * WAIT FOR PARTNER (as drawn, the safe choice) stays at RIGHT_AUTO_HIVE watching for TIP 2 until 23 s,
 * fires if it comes, and parks; CROSS AND FIX IT drives the wall side to LEFT_AUTO_OFFSET and puts its
 * FLOWER load in the LEFT CELL if it is still up, then parks. Under the real guard
 * (parkAfterVolley, the registry's volley and grab marks). Every drive takes its exported time and
 * every command the registry's typical time, so the guard's plan is the drawing's.
 */
public class RNoCrossAutoTest {
    private static final String CROSSING = "path wall side to LEFT_AUTO_OFFSET";

    private double now;
    /** When the RIGHT CELL tips and the LEFT comes up (TIP 1), and when it comes back (TIP 2). */
    private double tip1 = 6, tip2 = Double.POSITIVE_INFINITY;
    private boolean feeding;
    private final List<Pose> launchedFrom = new ArrayList<>();
    private final List<String> trace = new ArrayList<>();
    private final Map<String, Integer> choices = new HashMap<>();
    private Pose pose;
    private double doneAt, parkedAt = -1;

    @Before public void reset() {
        Scheduler.reset();
        pose = RNoCrossAuto.startPose(false);
    }

    private Command lasting(String name, double seconds) {
        double[] began = new double[1];
        return new CommandBuilder()
                .setStart(() -> {
                    began[0] = now;
                    feeding = name.equals("LaunchAll");
                    if (feeding) launchedFrom.add(pose);
                })
                .setDone(() -> now - began[0] >= seconds)
                .setEnd(end -> feeding = false);
    }

    private boolean rightUp() { return now < tip1 || now >= tip2; }

    private void run() {
        AutoRegistry registry = new AutoRegistry().holdTimeLimitsWhile(() -> feeding)
                .command("SpinUp", 0, () -> lasting("SpinUp", 0))
                .volley("LaunchAll", AutoRegistration.LAUNCH_ALL_TYPICAL_S, AutoRegistration.LAUNCH_ALL_LAST_PIECE_S,
                        () -> lasting("LaunchAll", AutoRegistration.LAUNCH_ALL_TYPICAL_S))
                .grab("PickFlower", AutoRegistration.PICK_FLOWER_TYPICAL_S,
                        () -> lasting("PickFlower", AutoRegistration.PICK_FLOWER_TYPICAL_S))
                .grab("GardenGather", AutoRegistration.GARDEN_GATHER_TYPICAL_S,
                        () -> lasting("GardenGather", AutoRegistration.GARDEN_GATHER_TYPICAL_S))
                // The CELL at the robot's end of the field (red: RIGHT at the audience end).
                .trigger("CellUp", () -> pose.y() < FieldFrame.FIELD_CENTRE_INCHES ? rightUp() : !rightUp());
        AutoDrive drive = new AutoDrive() {
            @Override public void follow(Path path) {
                pose = path.endPose();
                if (Math.abs(pose.x() - 14.5) < .01) parkedAt = now;
                doneAt = now + 3.3;
            }
            @Override public boolean pathDone() { return now >= doneAt; }
            @Override public Pose pose() { return pose; }
            @Override public boolean poseReferenced() { return true; }
            @Override public void hold(Pose p) { pose = p; }
        };
        AutoKit kit = new AutoKit(drive, registry, () -> now).trace(trace::add)
                .picks(new AutoKit.Picks() {
                    @Override public int choice(String option) { return choices.getOrDefault(option, -1); }
                    @Override public double seconds(String option) { return Double.NaN; }
                });
        Command auto = RNoCrossAuto.build(kit, false);
        auto.schedule();
        for (int i = 0; i < 2000 && auto.isScheduled(); i++) {
            Scheduler.execute();
            now += 0.02;
        }
        assertFalse("Still running: " + trace, auto.isScheduled());
    }

    private void parkedRightPark() {
        assertEquals(14.5, pose.x(), .01);
        assertEquals(98.7, pose.y(), .01);
        assertTrue("parked by 30 s: " + now + " " + trace, now <= 30);
    }

    @Test public void theMenuWaitsForTip2Until20sThenWaitsForThePartner() {
        assertEquals(2, RNoCrossAuto.OPTIONS.length);
        assertEquals(20, RNoCrossAuto.OPTIONS[0].drawnSeconds, 1e-9);
        assertEquals("if no TIP 2", RNoCrossAuto.OPTIONS[1].name);
        assertEquals("[WAIT FOR PARTNER, CROSS AND FIX IT]", RNoCrossAuto.OPTIONS[1].choices.toString());
    }

    private void crossAndFixIt() {
        choices.put("if no TIP 2", 1);
    }

    /** CROSS AND FIX IT, TIP 2 never comes, the LEFT CELL still up: R crosses and puts its load in at LEFT. */
    @Test public void crossAndFixItFiresAtTheLeftCellAndParks() {
        crossAndFixIt();
        run();
        assertTrue(trace.toString(), trace.contains(CROSSING));
        Pose last = launchedFrom.get(launchedFrom.size() - 1);
        assertTrue("topped up LEFT: " + launchedFrom, last.y() > FieldFrame.FIELD_CENTRE_INCHES);
        parkedRightPark();
    }

    /** WAIT FOR PARTNER (the default), TIP 2 never comes: no crossing, the load stays in, parked after 23 s. */
    @Test public void waitForPartnerKeepsTheLoadAndParksFromTheRight() {
        run();
        assertFalse(trace.toString(), trace.contains(CROSSING));
        assertEquals("only the preloads: " + launchedFrom, 1, launchedFrom.size());
        assertTrue("waited to 23 s: " + parkedAt, parkedAt >= 23);
        parkedRightPark();
    }

    /** WAIT FOR PARTNER, the partner's TIP 2 comes late (21.5 s): R fires at RIGHT and parks. */
    @Test public void waitForPartnerFiresAtALateTip2() {
        tip2 = 21.5;
        run();
        assertFalse(trace.toString(), trace.contains(CROSSING));
        assertEquals("the late volley: " + launchedFrom, 2, launchedFrom.size());
        assertTrue("fired at RIGHT: " + launchedFrom, launchedFrom.get(1).y() < FieldFrame.FIELD_CENTRE_INCHES);
        parkedRightPark();
    }

    /** TIP 2 on time: the pick never matters, R fires at RIGHT and never crosses. */
    @Test public void tip2OnTimeNeverCrosses() {
        tip2 = 15;
        run();
        assertFalse(trace.toString(), trace.contains(CROSSING));
        assertTrue("fired at RIGHT: " + launchedFrom,
                launchedFrom.size() >= 2 && launchedFrom.get(1).y() < FieldFrame.FIELD_CENTRE_INCHES);
        parkedRightPark();
    }
}
