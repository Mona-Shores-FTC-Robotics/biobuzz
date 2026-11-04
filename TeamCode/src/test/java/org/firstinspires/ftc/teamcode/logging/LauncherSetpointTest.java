package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem;
import org.firstinspires.ftc.teamcode.vision.HiveState;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Locale;

/**
 * The real robot's launcher fires at one setpoint ({@link RobotDesign#fixedSetpoint}), and the simulator reads what
 * the robot did with it back: master's 1595 ticks/s, tuned at RIGHT_START on 1 Nov 2026, scores there nearly every
 * shot (4 of 4 on 4 Nov), 1575 lands a little short (3 and 2 of 4) and 1625 long (1 of 4)
 * ({@code LauncherSubsystem.targetTicksPerSecond}'s note); and from LEFT_LAUNCH_OFFSET, 41 in from the CELL, the 4 Nov
 * video's third volley went long and mostly missed. The spread is a placeholder set to the first three
 * ({@link FieldSim#SETPOINT_SPEED_SPREAD}).
 *
 * <p>Opt-in, like every simulator test but the smoke test: {@code BIOBUZZ_SIM_CHECKS=1}. A few seconds.
 */
public class LauncherSetpointTest {

    static final int SHOTS = Integer.parseInt(System.getenv().getOrDefault("BIOBUZZ_SIM_SHOTS", "200"));

    @Before
    public void optIn() {
        assumeTrue("set BIOBUZZ_SIM_CHECKS=1 to run the simulator's launcher checks", "1".equals(System.getenv("BIOBUZZ_SIM_CHECKS")));
    }

    /**
     * The share of {@link #SHOTS} single POLLEN shots from (x, y) that go into the red CELL raised by {@code state},
     * at {@code ticks}, the rocker held (so no TIP changes the target), each on a fresh field.
     */
    static double scored(double x, double y, HiveState state, double ticks, RobotDesign design) {
        double before = FieldSim.setpointTicksPerSecond;
        FieldSim.setpointTicksPerSecond = ticks;
        try {
            FieldSim.Physics physics = HiveCalibration.current().fit();
            int in = 0;
            for (long seed = 1; seed <= SHOTS; seed++) {
                FieldSim sim = new FieldSim(new ArrayList<>(), seed, physics);
                sim.red.angle = sim.red.angleFor(state);
                sim.red.locked = true;
                double[] aim = sim.red.aimPointFor(sim.red.raisedEnd());
                sim.main.design = design;
                double heading = Math.atan2(aim[1] - y, aim[0] - x);
                FieldSim.Piece held = new FieldSim.Piece(FieldSim.Kind.POLLEN, FieldSim.Where.ROBOT, x, y, 10);
                sim.pieces.add(held);
                sim.main.stored.add(held);
                sim.setRobot(x, y, heading, 0, 0, 0, false);
                sim.launch(aim);
                for (int k = 0; k < FieldSim.SHOT_SETTLE_STEPS; k++) {
                    sim.setRobot(x, y, heading, 0, 0, 0, false);
                    sim.step(0.02);
                }
                if (held.cell != null) in++;
            }
            return (double) in / SHOTS;
        } finally {
            FieldSim.setpointTicksPerSecond = before;
        }
    }

    static final double RIGHT_X = StartPositions.RED_AUDIENCE.pose.x(), RIGHT_Y = StartPositions.RED_AUDIENCE.pose.y();

    private static double fromRightStart(double ticks) {
        return scored(StartPositions.RED_AUDIENCE.pose.x(), StartPositions.RED_AUDIENCE.pose.y(), HiveState.RIGHT_CELL_UP,
                ticks, RobotDesign.realLocked());
    }

    /**
     * At RIGHT_START the setpoint is the middle of the speeds that score, as the robot's is. 1575 and 1625 are printed,
     * not checked: the simulated CELL takes about ±5% of speed where the robot's took about ±1.5%
     * ({@link FieldSim#SETPOINT_SPEED_SPREAD}).
     */
    @Test
    public void theSetpointScoresFromRightStart() {
        double target = LauncherSubsystem.targetTicksPerSecond;
        double slow = fromRightStart(1575), tuned = fromRightStart(target), fast = fromRightStart(1625);
        System.out.println(String.format(Locale.ROOT, "LauncherSetpointTest RIGHT_START: 1575 %.0f%%, %.0f %.0f%%, 1625 %.0f%%"
                        + " (robot, 1 Nov: 3 and 2 of 4, 4 of 4 on 4 Nov, 1 of 4)", 100 * slow, target, 100 * tuned, 100 * fast));
        assertTrue("the setpoint scores " + tuned, tuned >= 0.9);
        assertTrue("1500 scores less", fromRightStart(1500) < tuned);
        assertTrue("1680 scores less", fromRightStart(1680) < tuned);
    }

    /** LEFT_LAUNCH_OFFSET, (41, 121.5): about 41 in from the high-end CELL. Its volley went long on 4 Nov. */
    @Test
    public void fromLeftLaunchOffsetMostShotsGoLong() {
        double offset = scored(41, 121.5, HiveState.LEFT_CELL_UP, LauncherSubsystem.targetTicksPerSecond, RobotDesign.realLocked());
        boolean before = FieldSim.idealSpeed;
        FieldSim.idealSpeed = true;
        double ideal;
        try {
            ideal = scored(41, 121.5, HiveState.LEFT_CELL_UP, LauncherSubsystem.targetTicksPerSecond, RobotDesign.realLocked());
        } finally {
            FieldSim.idealSpeed = before;
        }
        System.out.println(String.format(Locale.ROOT, "LauncherSetpointTest LEFT_LAUNCH_OFFSET: setpoint %.0f%%, ideal speed %.0f%%",
                100 * offset, 100 * ideal));
        assertTrue("from 41 in the setpoint scores " + offset, offset <= 0.5);
        assertTrue("the ideal speed scores " + ideal, ideal >= 0.9);
    }

    /**
     * For refitting: {@code BIOBUZZ_SIM_SWEEP=1550,1575,1595,1625} prints both spots' scores at each setpoint, and
     * {@code BIOBUZZ_SIM_PITCHES=72,75,78} (NaN: the arc's own) does it at each pitch; {@code BIOBUZZ_SIM_SHOTS} sets the
     * shots per cell (200). 4 Nov 2026, 60 shots, from 41 in at 1595: pitch 67 (the arc's) 100%, 72 65%, 75 13%, 78 0%.
     */
    @Test
    public void sweep() {
        String sw = System.getenv("BIOBUZZ_SIM_SWEEP");
        if (sw == null) return;
        String pitches = System.getenv("BIOBUZZ_SIM_PITCHES");
        for (String pitch : (pitches == null ? String.valueOf(FieldSim.SETPOINT_PITCH_DEG) : pitches).split(",")) {
            RobotDesign d = RobotDesign.realLocked();
            d.setpointPitchDeg = Double.parseDouble(pitch);
            double[] tuning = FieldSim.setpointTuning(d);
            for (String v : sw.split(",")) {
                double t = Double.parseDouble(v);
                System.out.println(String.format(Locale.ROOT, "SWEEP pitch %.1f exit %.0f ticks %s right %.0f%% left41 %.0f%%",
                        Math.toDegrees(tuning[1]), tuning[0], v,
                        100 * scored(RIGHT_X, RIGHT_Y, HiveState.RIGHT_CELL_UP, t, d), 100 * scored(41, 121.5, HiveState.LEFT_CELL_UP, t, d)));
            }
        }
    }
}
