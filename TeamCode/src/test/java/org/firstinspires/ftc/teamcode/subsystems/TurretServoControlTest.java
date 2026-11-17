package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@link TurretServoControl}: the law itself, and the law against a model of a continuous servo
 * turning the turret. The model is not the robot: it shows the law is sound (it settles, takes the
 * shortest way, follows a turning robot), and the numbers that matter are measured on
 * {@code Bench: Turret}.
 */
public class TurretServoControlTest {

    private static final double EPS = 1e-9;

    private static TurretServoControl.Gains gains() {
        return new TurretServoControl.Gains(TurretSubsystem.kP, TurretSubsystem.kD, TurretSubsystem.minPower,
                TurretSubsystem.maxPower, TurretSubsystem.topSpeedDps, TurretSubsystem.feedforward,
                TurretSubsystem.holdToleranceDeg, TurretSubsystem.targetStillDps);
    }

    // ------------------------------------------------------------ the law

    @Test
    public void theErrorIsTheShortestWayRound() {
        assertEquals(20.0, TurretServoControl.shortestErrorDeg(-170.0, 170.0), EPS);
        assertEquals(-20.0, TurretServoControl.shortestErrorDeg(170.0, -170.0), EPS);
        assertEquals(10.0, TurretServoControl.shortestErrorDeg(10.0, 720.0), EPS);
        assertEquals(180.0, TurretServoControl.shortestErrorDeg(180.0, 0.0), EPS);
        assertTrue(Double.isNaN(TurretServoControl.shortestErrorDeg(Double.NaN, 0.0)));
    }

    @Test
    public void onAStillTargetInsideTheToleranceThePowerIsZero() {
        TurretServoControl.Gains g = gains();
        assertEquals(0.0, TurretServoControl.power(g.toleranceDeg * 0.5, 0.0, 0.0, g), EPS);
        assertEquals(0.0, TurretServoControl.power(Double.NaN, 0.0, 0.0, g), EPS);
        assertEquals(0.0, TurretServoControl.power(0.0, 0.0, g.stillDps * 0.5, g), EPS);
    }

    @Test
    public void anyPowerThatIsNotZeroIsAtLeastTheFloorAndSignedTowardTheTarget() {
        TurretServoControl.Gains g = gains();
        double left = TurretServoControl.power(g.toleranceDeg + 0.1, 0.0, 0.0, g);
        double right = TurretServoControl.power(-g.toleranceDeg - 0.1, 0.0, 0.0, g);
        assertTrue(left >= g.minPower && left < g.minPower + 0.1);
        assertEquals(-left, right, EPS);
        assertEquals(g.maxPower, TurretServoControl.power(170.0, 0.0, 0.0, g), EPS);
    }

    @Test
    public void aMovingTargetIsFedForwardEvenWithNoError() {
        TurretServoControl.Gains g = gains();
        double half = TurretServoControl.power(0.0, 0.0, g.topSpeedDps / 2.0, g);
        assertEquals(g.minPower + (1.0 - g.minPower) * 0.5 * g.feedforward, half, 1e-6);
        assertTrue(TurretServoControl.power(0.0, 0.0, -g.topSpeedDps / 2.0, g) < 0.0);
    }

    @Test
    public void dampingEasesTheCorrectionAndNeverTurnsItRound() {
        TurretServoControl.Gains g = new TurretServoControl.Gains(0.01, 0.01, 0.07, 1.0, 196.0, 1.0, 1.0, 5.0);
        double atRest = TurretServoControl.power(20.0, 0.0, 0.0, g);
        double closing = TurretServoControl.power(20.0, 10.0, 0.0, g);
        assertTrue(closing > 0.0 && closing < atRest);
        assertEquals("closing fast: no drive, never a reverse", 0.0, TurretServoControl.power(20.0, 150.0, 0.0, g), EPS);
    }

    @Test
    public void badGainsBecomeTheHarmlessEnd() {
        TurretServoControl.Gains g = new TurretServoControl.Gains(Double.NaN, -1.0, 5.0, Double.NaN,
                Double.POSITIVE_INFINITY, Double.NaN, -3.0, Double.NaN);
        assertEquals(0.0, g.kP, EPS);
        assertEquals(0.0, g.kD, EPS);
        assertEquals(0.0, g.maxPower, EPS);
        assertEquals(0.0, g.minPower, EPS);
        assertEquals(0.0, g.topSpeedDps, EPS);
        assertEquals(0.0, TurretServoControl.power(90.0, 0.0, 100.0, g), EPS);
    }

    // ------------------------------------------------------------ against a model

    /**
     * A continuous servo turning the turret: nothing below the floor, speed in proportion above it
     * up to {@code top}, reached with a first-order lag, after a delay of whole loops (the servo
     * reads its pulse every 20 ms, and the write goes out a loop late).
     */
    private static final class Plant {
        final double top;
        final double floor;
        final double lagS;
        final double[] pipeline;
        double angle;
        double rate;

        Plant(double top, double floor, double lagS, int delayLoops) {
            this.top = top;
            this.floor = floor;
            this.lagS = lagS;
            this.pipeline = new double[delayLoops];
        }

        void step(double power, double dt) {
            double applied = power;
            for (int i = 0; i < pipeline.length; i++) {
                double next = pipeline[i];
                pipeline[i] = applied;
                applied = next;
            }
            double mag = Math.abs(applied);
            double want = mag <= floor ? 0.0 : Math.copySign(top * (mag - floor) / (1.0 - floor), applied);
            rate += (want - rate) * Math.min(1.0, dt / lagS);
            angle += rate * dt;
        }
    }

    private static final double DT = 0.015;

    /** Runs the law on the plant for {@code seconds}; returns the largest |error| over the last {@code tailS}. */
    private static double run(Plant plant, TurretServoControl.Gains g, double seconds, double tailS,
                              java.util.function.DoubleUnaryOperator targetAt,
                              java.util.function.DoubleUnaryOperator targetRateAt) {
        double worst = 0.0;
        for (double t = 0.0; t < seconds; t += DT) {
            double error = TurretServoControl.shortestErrorDeg(targetAt.applyAsDouble(t), plant.angle);
            plant.step(TurretServoControl.power(error, plant.rate, targetRateAt.applyAsDouble(t), g), DT);
            if (t >= seconds - tailS) {
                worst = Math.max(worst, Math.abs(
                        TurretServoControl.shortestErrorDeg(targetAt.applyAsDouble(t + DT), plant.angle)));
            }
        }
        return worst;
    }

    @Test
    public void aStepSettlesInsideTheToleranceWithoutSwingingPast() {
        TurretServoControl.Gains g = gains();
        Plant plant = new Plant(g.topSpeedDps, g.minPower, 0.06, 2);
        double peak = 0.0;
        for (double t = 0.0; t < 3.0; t += DT) {
            double error = TurretServoControl.shortestErrorDeg(90.0, plant.angle);
            plant.step(TurretServoControl.power(error, plant.rate, 0.0, g), DT);
            peak = Math.max(peak, plant.angle);
        }
        assertEquals(90.0, plant.angle, g.toleranceDeg + 0.5);
        assertTrue("overshoot " + (peak - 90.0), peak - 90.0 < 3.0);
        assertEquals(0.0, plant.rate, 1.0);
    }

    @Test
    public void aServoFasterOrSlowerThanTheNumberStillSettles() {
        TurretServoControl.Gains g = gains();
        for (double scale : new double[] {0.6, 1.5}) {
            Plant plant = new Plant(g.topSpeedDps * scale, g.minPower * 0.5, 0.10, 3);
            double worst = run(plant, g, 4.0, 1.0, t -> -120.0, t -> 0.0);
            assertTrue("scale " + scale + " error " + worst, worst <= g.toleranceDeg + 0.5);
        }
    }

    @Test
    public void itTakesTheShortestWayRound() {
        TurretServoControl.Gains g = gains();
        Plant plant = new Plant(g.topSpeedDps, g.minPower, 0.06, 2);
        plant.angle = 170.0;
        double lowest = plant.angle;
        for (double t = 0.0; t < 2.0; t += DT) {
            double error = TurretServoControl.shortestErrorDeg(-170.0, plant.angle);
            plant.step(TurretServoControl.power(error, plant.rate, 0.0, g), DT);
            lowest = Math.min(lowest, plant.angle);
        }
        assertEquals("through 180, 20 degrees, not 340 back through 0", 190.0, plant.angle, g.toleranceDeg + 0.5);
        assertTrue(lowest > 165.0);
    }

    @Test
    public void itCancelsTheRobotsTurning() {
        // The robot turns at 150 deg/s (three quarters of the turret's top speed): the target, a
        // chassis-frame angle, moves the other way at that rate.
        TurretServoControl.Gains g = gains();
        Plant plant = new Plant(g.topSpeedDps, g.minPower, 0.06, 2);
        double worst = run(plant, g, 3.0, 2.0, t -> -150.0 * t, t -> -150.0);
        assertTrue("following error " + worst, worst < 5.0);
    }

    @Test
    public void withTheTopSpeedAQuarterOffItStillFollows() {
        TurretServoControl.Gains g = gains();
        Plant plant = new Plant(g.topSpeedDps * 0.75, g.minPower, 0.06, 2);
        // The feedforward asks for 100 deg/s and gets 75: the correction has to make up 25 deg/s,
        // and at the robot's kP that costs about 9 degrees of trailing. So topSpeedDps must be
        // measured: the lower the kP, the more the turret leans on the feedforward being right.
        double worst = run(plant, g, 3.0, 2.0, t -> 100.0 * t, t -> 100.0);
        assertTrue("following error " + worst, worst < 12.0);
    }

    @Test
    public void aTurnFasterThanTheTurretIsCaughtUpWithAfterwards() {
        // 300 deg/s for half a second, then the robot stops: the turret falls behind, then closes.
        TurretServoControl.Gains g = gains();
        Plant plant = new Plant(g.topSpeedDps, g.minPower, 0.06, 2);
        double worst = run(plant, g, 2.5, 0.5,
                t -> t < 0.5 ? 300.0 * t : 150.0, t -> t < 0.5 ? 300.0 : 0.0);
        assertTrue("error after catching up " + worst, worst <= g.toleranceDeg + 0.5);
    }
}
