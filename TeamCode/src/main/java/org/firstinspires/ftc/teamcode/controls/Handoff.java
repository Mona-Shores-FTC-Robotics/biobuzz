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
 * <p>Whichever of the two was written last wins, because it describes the robot more recently.
 *
 * <h2>No expiry</h2>
 *
 * <p>A handoff does not go stale. The break between an Auto and the TeleOp that follows it is not
 * always seconds (a robot reset on the field, a queue, a practice block), and a timer that quietly
 * dropped the pose, the alliance and the turret's zero made the TeleOp start somewhere the robot
 * is not. The record is good until something replaces it: the next OpMode's loops, the drive
 * team's Triangle (Y) held in INIT ("start fresh"), a restart of the Robot Controller app, or a
 * Sloth hot reload of this class; the Driver Station then says there was no handoff, rather than
 * anything guessing. It is not cleared when read, so re-initializing in the pits still gets it.
 * The Driver Station shows how old it is, so a pose from last hour is visible as such.
 *
 * <p>Only the match log keeps a clock ({@link #LOG_MAX_AGE_MS}): a paused log closes by itself if
 * nobody picks it up, and an OpMode joins the earlier OpMode's match (its file, its match id) only
 * within that time. The pose does not depend on it.
 */
public final class Handoff {

    /**
     * Three minutes: how long an Auto's paused match log waits for the TeleOp, and how long after
     * the last OpMode a new one still joins its match. Only the log and the match id use it; the
     * handoff itself has no expiry.
     */
    public static final long LOG_MAX_AGE_MS = 3 * 60 * 1000L;

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

        /** How long ago this was recorded; never negative, even if the clock was set back. */
        public long ageMs(long nowMs) {
            return Math.max(0L, nowMs - recordedAtMs);
        }

        /** For the Driver Station: "45s ago", "6 min 25 s ago", "2 h 5 min ago". */
        public String ageText(long nowMs) {
            long s = ageMs(nowMs) / 1000;
            if (s < 120) {
                return s + "s ago";
            }
            long m = s / 60;
            if (m < 120) {
                return m + " min " + (s % 60) + " s ago";
            }
            return (m / 60) + " h " + (m % 60) + " min ago";
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
     * What the OpMode that just ended left, or null if nothing has run since the app started (or
     * since a Sloth reload). The more recent of the Auto's final record and the latest loop state;
     * see the class comment. It never expires.
     */
    public static Snapshot last() {
        Snapshot recorded = latest;
        if (liveValid && (recorded == null || liveAtMs > recorded.recordedAtMs)) {
            // The loop state is the newer one, so it decides.
            // The Auto's paused log goes along only if it is this match's; an older match's is not ours.
            MatchLog log = recorded != null && recorded.matchId != null
                    && recorded.matchId.equals(liveMatchId) ? recorded.log : null;
            return new Snapshot(liveAlliance, liveHasPose ? new Pose(liveX, liveY, liveHeading) : null,
                    liveMatchId, log, liveHasTurretZero ? Long.valueOf(liveTurretZero) : null,
                    liveFromAuto, liveAtMs);
        }
        return recorded;
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
