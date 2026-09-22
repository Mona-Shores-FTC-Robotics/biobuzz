package org.firstinspires.ftc.teamcode.shooter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.shooter.config.FlywheelTuningConfig;
import org.junit.Test;

/**
 * The lane-state decision, tested off the robot.
 *
 * <p>Worth a test class of its own because it is the one piece of the rig whose
 * bugs are invisible at a meeting. A wrong threshold here does not crash or
 * throw — it just quietly reports {@code [spin ]} forever at a lane nobody is
 * going to fix, or cries {@code [ENCDR]} at a healthy wheel until people stop
 * reading the warnings.
 *
 * <p>The defaults are exercised rather than a hand-built {@code Diagnostics},
 * so a later edit to a default that breaks the separation between "spinning up"
 * and "never going to" fails here rather than on a robot.
 */
public class FlywheelDiagnosisTest {

    private final FlywheelTuningConfig.Diagnostics limits =
            new FlywheelTuningConfig.Diagnostics();

    private final FlywheelTuningConfig defaults = new FlywheelTuningConfig();

    /** Closed loop, lane wired and ticked on, not tripped, with the numbers under test. */
    private FlywheelDiagnosis closedLoop(boolean driven, boolean atSpeed, double rpm,
                                         double power, double msAtPower) {
        return FlywheelDiagnosis.evaluate(true, true, false, driven, false, atSpeed, rpm,
                power, msAtPower, limits);
    }

    // ------------------------------------------------------- the normal path

    @Test
    public void anUntickedLaneIsOffWhateverElseIsTrue() {
        assertEquals(FlywheelDiagnosis.OFF,
                FlywheelDiagnosis.evaluate(false, true, false, true, false, false, 0, 1.0, 5000,
                        limits));
    }

    @Test
    public void aMissingMotorOutranksEverythingButBeingSwitchedOff() {
        assertEquals(FlywheelDiagnosis.NOT_IN_CONFIG,
                FlywheelDiagnosis.evaluate(true, false, false, true, false, false, 0, 1.0, 5000,
                        limits));
    }

    @Test
    public void anUndrivenLaneIsStoppedRatherThanFaulty() {
        // Not being asked to spin is why it is not spinning. Reporting a dead
        // encoder here would fire on every lane every time the rig is stopped.
        assertEquals(FlywheelDiagnosis.STOPPED, closedLoop(false, false, 0.0, 0.0, 0.0));
    }

    @Test
    public void aLaneClimbingTowardsTargetIsSpinningUp() {
        assertEquals(FlywheelDiagnosis.SPINNING_UP, closedLoop(true, false, 900, 0.45, 300));
    }

    @Test
    public void aLaneInToleranceForTheHoldTimeIsAtSpeed() {
        assertEquals(FlywheelDiagnosis.AT_SPEED, closedLoop(true, true, 1500, 0.28, 4000));
    }

    // ---------------------------------------------------- the dead encoder

    @Test
    public void realPowerWithNoMovementForLongEnoughIsADeadEncoder() {
        assertEquals(FlywheelDiagnosis.DEAD_ENCODER, closedLoop(true, false, 0.0, 1.0, 1500));
    }

    @Test
    public void aStillWheelDuringTheFirstMomentOfSpinUpIsNotYetAFault() {
        // The whole point of the time threshold: at 300ms a real flywheel has
        // barely broken away, and calling that a dead encoder would make the
        // warning fire on every healthy spin-up.
        assertEquals(FlywheelDiagnosis.SPINNING_UP, closedLoop(true, false, 0.0, 1.0, 300));
    }

    @Test
    public void aStillWheelUnderTrivialPowerIsNotADeadEncoder() {
        // Below the power threshold a stationary wheel is evidence of nothing —
        // it may simply not have been asked to move hard enough yet.
        assertEquals(FlywheelDiagnosis.SPINNING_UP, closedLoop(true, false, 0.0, 0.05, 9000));
    }

    @Test
    public void aSlowlyTurningWheelIsNotADeadEncoder() {
        // Above deadEncoderRpm the encoder is demonstrably alive, however badly
        // the lane is performing. That is a gains problem, not a wiring one.
        assertEquals(FlywheelDiagnosis.SPINNING_UP, closedLoop(true, false, 40, 1.0, 9000));
    }

    @Test
    public void aDeadEncoderIsReportedInOpenLoopToo() {
        // Open loop exists partly *because* of dead encoders, so it must still
        // be the mode that tells you that is what you are looking at.
        assertEquals(FlywheelDiagnosis.DEAD_ENCODER,
                FlywheelDiagnosis.evaluate(true, true, true, true, false, false, 0.0, 0.6, 2000,
                        limits));
    }

    // ------------------------------------------------------ wrong direction

    @Test
    public void aClearlyNegativeRpmIsABackwardsWheel() {
        assertEquals(FlywheelDiagnosis.BACKWARDS, closedLoop(true, false, -1200, 0.5, 3000));
    }

    @Test
    public void encoderNoiseAroundZeroIsNotCalledBackwards() {
        // The first version of this check used -1 RPM, which a stationary
        // encoder reaches from quantization alone — and a lane flickering
        // between BACKWARDS and DEAD_ENCODER teaches people to ignore both.
        assertEquals(FlywheelDiagnosis.DEAD_ENCODER, closedLoop(true, false, -2.0, 0.8, 2000));
    }

    @Test
    public void backwardsIsReportedEvenWhileTheLaneLooksStalled() {
        // A reversed wheel under full power is both "not moving forwards" and
        // "moving"; naming the direction is the actionable half.
        assertEquals(FlywheelDiagnosis.BACKWARDS, closedLoop(true, false, -800, 1.0, 5000));
    }

    // --------------------------------------------------------- open loop

    @Test
    public void aHealthyOpenLoopLaneReportsOpenLoopRatherThanReadiness() {
        // There is no target in open loop, so AT_SPEED would be meaningless
        // even when the caller passes atSpeed=true from a stale closed-loop run.
        assertEquals(FlywheelDiagnosis.OPEN_LOOP,
                FlywheelDiagnosis.evaluate(true, true, true, true, false, true, 2400, 0.4, 3000,
                        limits));
    }

    // ---------------------------------------------------------- overspeed

    @Test
    public void aTrippedLaneReportsOverspeedAheadOfAnythingElse() {
        // Power is already cut, so every other reading describes a wheel that is
        // coasting down rather than the reason anyone should be looking at it.
        assertEquals(FlywheelDiagnosis.OVERSPEED,
                FlywheelDiagnosis.evaluate(true, true, false, true, true, true, 5500, 0.0, 0,
                        limits));
    }

    @Test
    public void overspeedOutranksADeadEncoderReading() {
        // Power cut to zero and a coasting wheel briefly satisfy neither test
        // cleanly; the latch is authoritative over anything inferred from RPM.
        assertEquals(FlywheelDiagnosis.OVERSPEED,
                FlywheelDiagnosis.evaluate(true, true, false, true, true, false, 0.0, 0.0, 9000,
                        limits));
    }

    @Test
    public void overspeedOutranksABackwardsWheel() {
        assertEquals(FlywheelDiagnosis.OVERSPEED,
                FlywheelDiagnosis.evaluate(true, true, false, true, true, false, -5800, 0.0, 0,
                        limits));
    }

    @Test
    public void aSwitchedOffLaneIsNeverReportedAsOverspeed() {
        // Nothing is being driven, so a stale latch must not shout at somebody
        // about a lane they already turned off.
        assertEquals(FlywheelDiagnosis.OFF,
                FlywheelDiagnosis.evaluate(false, true, false, true, true, false, 5500, 0.0, 0,
                        limits));
    }

    // ------------------------------------------------------------- advice

    @Test
    public void everyStateThatNeedsAHumanCarriesAdviceAndEveryOtherOneDoesNot() {
        for (FlywheelDiagnosis diagnosis : FlywheelDiagnosis.values()) {
            assertEquals(diagnosis + " must carry advice exactly when it needs attention",
                    diagnosis.needsAttention(), !diagnosis.advice().isEmpty());
        }
        assertTrue(FlywheelDiagnosis.DEAD_ENCODER.needsAttention());
        assertTrue(FlywheelDiagnosis.BACKWARDS.needsAttention());
        assertTrue(FlywheelDiagnosis.NOT_IN_CONFIG.needsAttention());
        assertFalse(FlywheelDiagnosis.AT_SPEED.needsAttention());
        assertFalse(FlywheelDiagnosis.OPEN_LOOP.needsAttention());
        assertFalse(FlywheelDiagnosis.OFF.needsAttention());
        assertFalse(FlywheelDiagnosis.STOPPED.needsAttention());
    }

    @Test
    public void everyTagIsTheSameWidthSoTheLaneTableLinesUp() {
        int width = FlywheelDiagnosis.AT_SPEED.tag.length();
        for (FlywheelDiagnosis diagnosis : FlywheelDiagnosis.values()) {
            assertEquals(diagnosis + " tag breaks the lane table's column width",
                    width, diagnosis.tag.length());
        }
    }

    // ------------------------------------------- the defaults hang together

    @Test
    public void theDefaultLimitsFormAChainBelowFreeSpeed() {
        double freeSpeed = defaults.measurement.freeSpeedRpm;

        assertTrue("target.maxRpm defaulted to exactly free speed once, which let the"
                        + " rig ask for the one thing the limits exist to prevent",
                defaults.target.maxRpm < freeSpeed);

        // maxPower is the limit that actually binds, so the speed it permits
        // unloaded has to sit between the target ceiling and the cutout — above
        // the ceiling or the rig cannot reach its own target, below the cutout
        // or the cutout nuisance-trips on a rig that is behaving.
        double reachable = defaults.limits.maxPower * freeSpeed;
        assertTrue("maxPower must still allow the highest requestable target",
                reachable > defaults.target.maxRpm);
        assertTrue("the cutout must sit above what maxPower can reach, or it fires"
                        + " on a healthy rig at the top of its range",
                defaults.limits.overspeedRpm > reachable);
        assertTrue("the cutout must still be below free speed to be worth having",
                defaults.limits.overspeedRpm < freeSpeed);
    }

    @Test
    public void theDefaultThresholdsSeparateSpinUpFromFailure() {
        assertTrue("a healthy flywheel shows its first tick well inside this",
                limits.deadEncoderAfterMs >= 500);
        assertTrue("the dead-encoder speed floor must sit below the backwards floor"
                        + " or a backwards wheel can satisfy both",
                limits.deadEncoderRpm <= limits.backwardsRpm);
        assertTrue("a power threshold at or below zero would fire on a stopped lane",
                limits.deadEncoderPower > 0.0);
    }
}
