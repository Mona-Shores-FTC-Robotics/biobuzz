package org.firstinspires.ftc.teamcode.controls;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * What Autonomous leaves for TeleOp: the alliance, where the robot ended up, and the match ID that
 * links the two OpModes' match logs.
 *
 * <p><b>This is the one piece of static robot state in the codebase, on purpose.</b> The SDK builds
 * a fresh OpMode for TeleOp, and a static field is the only thing that survives the switch. Nothing
 * else may be static; everything else is read through a subsystem's accessors.
 *
 * <p>{@code RobotOpMode} is the only writer (when an {@code @Autonomous} OpMode stops) and the only
 * reader (when any other OpMode initializes). Nobody else calls this.
 *
 * <h2>Staleness</h2>
 *
 * <p>A handoff older than {@link #MAX_AGE_MS} is ignored. In a match TeleOp initializes seconds after
 * Autonomous ends; a practice Auto from an hour ago must not put a stale pose into this TeleOp. A
 * handoff is not cleared when read, so re-initializing TeleOp in the pits still gets it. Restarting
 * the Robot Controller app, or a Sloth hot reload of this class, clears it — the Driver Station then
 * says there was no handoff, rather than anything guessing.
 */
public final class Handoff {

    /** Three minutes: generous for a delayed transition, far short of the next practice run. */
    public static final long MAX_AGE_MS = 3 * 60 * 1000L;

    /** An immutable record of one Autonomous ending. */
    public static final class Snapshot {
        public final Alliance alliance;
        /** Field pose in Pedro coordinates, or null if the Pinpoint was missing. */
        public final Pose pose;
        /** The Autonomous's match ID, which TeleOp's log carries on; null if it had none. */
        public final String matchId;
        public final long recordedAtMs;

        Snapshot(Alliance alliance, Pose pose, String matchId, long recordedAtMs) {
            this.alliance = alliance;
            this.pose = pose;
            this.matchId = matchId;
            this.recordedAtMs = recordedAtMs;
        }

        public long ageMs(long nowMs) {
            return nowMs - recordedAtMs;
        }
    }

    private static Snapshot latest;

    private Handoff() {
    }

    /** Called by {@code RobotOpMode} when an Autonomous OpMode stops. */
    public static void record(Alliance alliance, Pose pose, String matchId, long nowMs) {
        latest = new Snapshot(alliance == null ? Alliance.UNKNOWN : alliance, pose, matchId, nowMs);
    }

    /** The last Autonomous's handoff, or null if there is none or it is stale. */
    public static Snapshot fresh(long nowMs) {
        Snapshot snapshot = latest;
        if (snapshot == null) {
            return null;
        }
        long age = snapshot.ageMs(nowMs);
        return age >= 0 && age <= MAX_AGE_MS ? snapshot : null;
    }

    /** For tests. */
    static void clear() {
        latest = null;
    }
}
