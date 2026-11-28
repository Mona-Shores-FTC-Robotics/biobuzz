package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import org.firstinspires.ftc.teamcode.subsystems.BeamBreak;
import org.firstinspires.ftc.teamcode.subsystems.FeedWatch;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.vision.HiveState;
import org.junit.Test;

import static org.firstinspires.ftc.teamcode.opmodes.auto.CatchNectarAimed.Phase.*;
import static org.junit.Assert.*;

public class CatchNectarAimedTest {
    private final CatchNectarAimed.Decision catchShot = new CatchNectarAimed.Decision();

    @Test public void waitingBallAtBehindGateBeamCanBeTheOnlyLaneSignal() {
        BeamBreak beam = new BeamBreak();
        beam.step(0, true, 2);
        assertTrue(CatchNectarAimed.pieceWaiting(FeedWatch.Lane.EMPTY, true, beam));
        assertFalse(CatchNectarAimed.pieceWaiting(FeedWatch.Lane.EMPTY, false, beam));
        assertFalse(CatchNectarAimed.pieceWaiting(FeedWatch.Lane.EMPTY, true, null));
        beam.step(20, false, 2);
        assertFalse(CatchNectarAimed.pieceWaiting(FeedWatch.Lane.EMPTY, true, beam));
    }

    @Test public void onlyAPositiveOppositeSettledSightingVetoesTheEarlierCameraCheck() {
        assertTrue(CatchNectarAimed.oppositeSettled(HiveState.RIGHT_CELL_UP, true));
        assertTrue(CatchNectarAimed.oppositeSettled(HiveState.LEFT_CELL_UP, false));
        assertFalse(CatchNectarAimed.oppositeSettled(HiveState.LEFT_CELL_UP, true));
        assertFalse(CatchNectarAimed.oppositeSettled(HiveState.UNSEEN, true));
        assertFalse(CatchNectarAimed.oppositeSettled(HiveState.TRANSITION, true));
    }

    @Test public void correctHeadingAwayFromTheCatchOrWithBadToleranceCannotFeed() {
        double within = PathEndSettle.launchWithinIn;
        double yaw = PathEndSettle.aimToleranceDeg;
        try {
            PathEndSettle.launchWithinIn = 6;
            PathEndSettle.aimToleranceDeg = 3;
            PoseFactory p = PoseFactory.degrees();
            Pose end = p.of(40, 120, 210);
            assertTrue(CatchNectarAimed.atReceiveSpot(p.of(41, 121, 209), end));
            assertFalse(CatchNectarAimed.atReceiveSpot(p.of(43, 120, 210), end));
            assertFalse(CatchNectarAimed.atReceiveSpot(p.of(40, 120, 220), end));
            PathEndSettle.launchWithinIn = Double.NaN;
            assertFalse(CatchNectarAimed.atReceiveSpot(end, end));
        } finally {
            PathEndSettle.launchWithinIn = within;
            PathEndSettle.aimToleranceDeg = yaw;
        }
    }

    @Test public void previousPreloadsDoNotCountAndTheNextCountClosesTheGate() {
        catchShot.reset(10);
        assertEquals(WAIT, catchShot.step(12.48, true, true, 4));
        assertEquals(FEED, catchShot.step(12.5, true, true, 4));
        assertEquals(FEED, catchShot.step(13.0, true, false, 4));
        assertEquals(DONE, catchShot.step(13.32, true, true, 5));
        assertEquals(DONE, catchShot.step(13.34, true, true, 5));
    }

    @Test public void losingAimStopsAnOpenFeedAndItCannotReopen() {
        catchShot.reset(11);
        assertEquals(FEED, catchShot.step(12.5, true, true, 4));
        assertEquals(DONE, catchShot.step(12.52, false, true, 4));
        assertEquals(DONE, catchShot.step(12.54, true, true, 4));
    }

    @Test public void oneSecondHardBackstopEvenWhenCounterMissesTheShot() {
        catchShot.reset(11);
        assertEquals(FEED, catchShot.step(12.5, true, true, 4));
        assertEquals(FEED, catchShot.step(13.49, true, true, 4));
        assertEquals(DONE, catchShot.step(13.5, true, true, 4));
    }

    @Test public void emptyLateUnsafeAndUnknownClockAllLeaveWithGateShut() {
        catchShot.reset(11);
        assertEquals(DONE, catchShot.step(12.5, true, false, 4));
        assertEquals(DONE, catchShot.step(12.52, true, true, 4));
        catchShot.reset(12.52);
        assertEquals(DONE, catchShot.step(12.52, true, true, 4));
        catchShot.reset(11);
        assertEquals(DONE, catchShot.step(12.7, true, true, 4));
        catchShot.reset(11);
        assertEquals(DONE, catchShot.step(12.5, false, true, 4));
        catchShot.reset(11);
        assertEquals(DONE, catchShot.step(Double.NaN, true, true, 4));
    }

    @Test public void aPreviousHomeHoldOrAnAimAtTheArcEdgeCannotOpenTheGate() {
        double left = TurretSubsystem.arcLeftDeg;
        double right = TurretSubsystem.arcRightDeg;
        double tolerance = TurretSubsystem.holdToleranceDeg;
        try {
            TurretSubsystem.arcLeftDeg = 90;
            TurretSubsystem.arcRightDeg = 90;
            TurretSubsystem.holdToleranceDeg = 2;
            assertTrue(CatchNectarAimed.aligned(81, 82, 81));
            assertFalse(CatchNectarAimed.aligned(0, 81, 81));
            assertFalse(CatchNectarAimed.aligned(0, 0, 81));
            assertFalse(CatchNectarAimed.aligned(86, 85, 81));
            assertFalse(CatchNectarAimed.aligned(85, 86, 81));
            assertFalse(CatchNectarAimed.aligned(Double.NaN, 82, 81));
            assertFalse(CatchNectarAimed.aligned(81, Double.NaN, 81));
            TurretSubsystem.arcLeftDeg = Double.NaN;
            assertFalse(CatchNectarAimed.aligned(81, 81, 81));
        } finally {
            TurretSubsystem.arcLeftDeg = left;
            TurretSubsystem.arcRightDeg = right;
            TurretSubsystem.holdToleranceDeg = tolerance;
        }
    }
}
