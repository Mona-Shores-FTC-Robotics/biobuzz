package org.firstinspires.ftc.teamcode.controls;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.logging.MatchLog;
import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * What one OpMode leaves for the next: the alliance, where the robot is, the turret's zero, and (from
 * an Autonomous) the match log, still open, so TeleOp writes the rest of the match into the same file.
 *
 * <p><b>This is the one piece of static robot state in the codebase, on purpose.</b> The SDK builds
 * a fresh OpMode for TeleOp, and a static field is the only thing that survives the switch. Nothing
 * else may be static; everything else is read through a subsystem's accessors.
 *
 * <p>{@code RobotOpMode} is the only writer and the only reader. Nobody else calls this.
 *
 * <h2>Two writers: the end of an Auto, and every loop</h2>
 *
 * <p>An Autonomous that stops writes a full record ({@link #record}): the final state and the
 * match log, paused for the TeleOp that follows. Every loop of every OpMode also writes the
 * robot's current state ({@link #update}): three doubles and a few flags, no allocation. That is
 * what makes a crash survivable. An OpMode that throws leaves its last loop's pose, alliance and
 * turret zero behind whether or not its stop code ran, so the next OpMode, an Auto crashed 15
 * seconds in or a TeleOp restarted 20 seconds in, picks up where the robot is rather than where
 * the last Auto ended.
 *
 * <p>Whichever of the two was written last wins, because it describes the robot more recently; a
 * newer loop state that has gone stale means no handoff, never the older Auto's.
 *
 * <h2>Staleness</h2>
 *
 * <p>State from an Autonomous is good for {@link #MAX_AGE_MS}: in a match TeleOp initializes
 * seconds after Autonomous ends; a practice Auto from an hour ago must not put a stale pose into
 * this TeleOp. State from a TeleOp is good for only {@link #RESTART_MAX_AGE_MS}: long enough to
 * look at an error and start it again, short enough that the pose from the last practice run is not
 * put on a robot someone has since picked up and moved. A handoff is not cleared when read, so
 * re-initializing in the pits still gets it. Restarting the Robot Controller app, or a Sloth hot
 * reload of this class, clears it; the Driver Station then says there was no handoff, rather than
 * anything guessing.
 */
public final class Handoff {

    /** Three minutes: generous for a delayed transition, far short of the next practice run. */
    public static final long MAX_AGE_MS = 3 * 60 * 1000L;

    /** Thirty seconds: time to read an error and start TeleOp again, not time to move the robot. */
    public static final long RESTART_MAX_AGE_MS = 30 * 1000L;

    /** An immutable record of one Autonomous ending. */
    public static final class Snapshot {
        public final Alliance alliance;
        /** Field pose in Pedro coordinates, or null if the Pinpoint was missing. */
        public final Pose pose;
        /** The match ID (the Autonomous's start time, or the TeleOp's own); null if it had none. */
        public final String matchId;
        /** The Autonomous's match log, paused for TeleOp to resume; null if there is none to resume. */
        public final MatchLog log;
        /**
         * The turret's zero when the Autonomous ended, as an encoder count, or null if it never
         * had one. The hub keeps counting between OpModes, so the same offset still gives the
         * same angle in TeleOp, wherever the Auto turned the turret.
         */
        public final Long turretZeroCounts;
        /** True if an Autonomous wrote this, false if a TeleOp did (a restart resuming where it was). */
        public final boolean fromAuto;
        public final long recordedAtMs;

        Snapshot(Alliance alliance, Pose pose, String matchId, MatchLog log, Long turretZeroCounts,
                 boolean fromAuto, long recordedAtMs) {
            this.alliance = alliance;
            this.pose = pose;
            this.matchId = matchId;
            this.log = log;
            this.turretZeroCounts = turretZeroCounts;
            this.fromAuto = fromAuto;
            this.recordedAtMs = recordedAtMs;
        }

        public long ageMs(long nowMs) {
            return nowMs - recordedAtMs;
        }
    }

    private static Snapshot latest;

    // The latest loop's state, overwritten in place every loop (primitives, so nothing is allocated).
    private static boolean liveValid;
    private static boolean liveFromAuto;
    private static Alliance liveAlliance = Alliance.UNKNOWN;
    private static boolean liveHasPose;
    private static double liveX;
    private static double liveY;
    private static double liveHeading;
    private static boolean liveHasTurretZero;
    private static long liveTurretZero;
    private static String liveMatchId;
    private static long liveAtMs;

    private Handoff() {
    }

    /** Called by {@code RobotOpMode} when an Autonomous OpMode stops, with no turret zero to hand on. */
    public static void record(Alliance alliance, Pose pose, String matchId, MatchLog log, long nowMs) {
        record(alliance, pose, matchId, log, null, nowMs);
    }

    /** Called by {@code RobotOpMode} when an Autonomous OpMode stops. */
    public static void record(Alliance alliance, Pose pose, String matchId, MatchLog log,
                              Long turretZeroCounts, long nowMs) {
        latest = new Snapshot(alliance == null ? Alliance.UNKNOWN : alliance, pose, matchId, log,
                turretZeroCounts, true, nowMs);
    }

    /**
     * Called by {@code RobotOpMode} every loop of every OpMode: where the robot is now. Overwrites in
     * place and allocates nothing. {@code hasPose} is false when the pose is not on the field (no
     * Pinpoint, or nothing has set it), in which case x, y and heading are ignored.
     */
    public static void update(boolean fromAuto, Alliance alliance, boolean hasPose, double x, double y,
                              double heading, boolean hasTurretZero, long turretZero, String matchId,
                              long nowMs) {
        liveFromAuto = fromAuto;
        liveAlliance = alliance == null ? Alliance.UNKNOWN : alliance;
        liveHasPose = hasPose;
        liveX = x;
        liveY = y;
        liveHeading = heading;
        liveHasTurretZero = hasTurretZero;
        liveTurretZero = turretZero;
        liveMatchId = matchId;
        liveAtMs = nowMs;
        liveValid = true;
    }

    /**
     * What the OpMode that just ended left, or null if there is nothing or it is stale. The more
     * recent of the Auto's final record and the latest loop state; see the class comment.
     */
    public static Snapshot fresh(long nowMs) {
        Snapshot recorded = latest;
        if (liveValid && (recorded == null || liveAtMs > recorded.recordedAtMs)) {
            // The loop state is the newer one, so it decides; the Auto's older record does not
            // stand in for it once it has gone stale.
            long age = nowMs - liveAtMs;
            long max = liveFromAuto ? MAX_AGE_MS : RESTART_MAX_AGE_MS;
            if (age < 0 || age > max) {
                return null;
            }
            // The Auto's paused log goes along only if it is this match's; an older match's is not ours.
            MatchLog log = recorded != null && recorded.matchId != null
                    && recorded.matchId.equals(liveMatchId) ? recorded.log : null;
            return new Snapshot(liveAlliance, liveHasPose ? new Pose(liveX, liveY, liveHeading) : null,
                    liveMatchId, log, liveHasTurretZero ? Long.valueOf(liveTurretZero) : null,
                    liveFromAuto, liveAtMs);
        }
        if (recorded == null) {
            return null;
        }
        long age = recorded.ageMs(nowMs);
        return age >= 0 && age <= MAX_AGE_MS ? recorded : null;
    }

    /** For tests. */
    static void clear() {
        latest = null;
        liveValid = false;
        liveFromAuto = false;
        liveAlliance = Alliance.UNKNOWN;
        liveHasPose = false;
        liveHasTurretZero = false;
        liveMatchId = null;
    }
}
