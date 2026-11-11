package org.firstinspires.ftc.teamcode.vision;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.Subsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * Both HIVEs over the match (see {@link HiveTracker}): what the camera says, plus whether the robot
 * is steady enough for a mid-tip sighting to be believed. Reads {@link LimelightVisionSubsystem}
 * and the drive's pose; drives nothing.
 *
 * <p>The pose only has to be consistent with itself, so this works without a field reference.
 */
public final class HiveSubsystem implements Subsystem {

    /** How steady "steady" is. Set on a field. */
    @Configurable
    public static class Tuning {
        /**
         * Fastest the robot may drive for a mid-tip sighting to count, in/s ({@code HiveTracker}'s
         * {@code observe}). 10: the two TIPs on 4 and 5 Nov 2026 that started with the robot nearly
         * still came under 8 in/s; the false ones at 18 in/s and up. <b>Not measured</b> against a
         * real TIP seen while driving; a robot check.
         */
        public static double midTipMaxSpeedInPerSec = 10;

        /** Fastest the robot may turn for a mid-tip sighting to count, degrees/s. 30: as above, 21 and 60. */
        public static double midTipMaxTurnDegPerSec = 30;
    }

    private final LimelightVisionSubsystem vision;
    private final DriveSubsystem drive;
    private final HiveTracker red = new HiveTracker();
    private final HiveTracker blue = new HiveTracker();
    private Pose lastPose;
    private long lastMs;

    public HiveSubsystem(LimelightVisionSubsystem vision, DriveSubsystem drive) {
        this.vision = vision;
        this.drive = drive;
    }

    @Override
    public void initialize() {
        red.reset();
        blue.reset();
        lastPose = null;
    }

    /** Reads the camera; nothing to move. */
    @Override
    public void start() {
    }

    @Override
    public void update() {
        long nowMs = System.nanoTime() / 1_000_000L;
        Pose pose = drive.pose();
        boolean steady = steady(lastPose, pose, (nowMs - lastMs) / 1000.0);
        lastPose = pose;
        lastMs = nowMs;
        step(red, Alliance.RED, steady, nowMs);
        step(blue, Alliance.BLUE, steady, nowMs);
    }

    /** Feeds one tracker. */
    private void step(HiveTracker hive, Alliance alliance, boolean steady, long nowMs) {
        hive.observe(vision.seenHive(alliance), vision.hiveTagsInView(alliance),
                vision.msSinceHiveSeen(alliance), steady, nowMs);
    }

    /**
     * Whether the robot, from {@code before} to {@code now} over {@code dtS} seconds, was slow and
     * hardly turning ({@link Tuning#midTipMaxSpeedInPerSec}, {@link Tuning#midTipMaxTurnDegPerSec}).
     * True with no earlier pose or no time passed: nothing says it moved. Pure.
     */
    static boolean steady(Pose before, Pose now, double dtS) {
        if (before == null || now == null || !(dtS > 0)) return true;
        double speed = Math.hypot(now.x() - before.x(), now.y() - before.y()) / dtS;
        double turn = Math.toDegrees(Math.abs(Math.IEEEremainder(now.heading() - before.heading(), 2 * Math.PI))) / dtS;
        return !(speed > Tuning.midTipMaxSpeedInPerSec) && !(turn > Tuning.midTipMaxTurnDegPerSec);
    }

    /**
     * An Autonomous's PLAY: both HIVEs are assumed at the match-start position until the camera
     * sees them settle ({@link HiveTracker#assumeMatchStart()}).
     */
    public void assumeMatchStart() {
        red.assumeMatchStart();
        blue.assumeMatchStart();
    }

    /** {@code alliance}'s HIVE; null for UNKNOWN. */
    public HiveTracker of(Alliance alliance) {
        if (alliance == Alliance.RED) return red;
        if (alliance == Alliance.BLUE) return blue;
        return null;
    }

    @Override
    public void stop() {
    }

    @Override
    public void describe(Display display) {
        display.line("HIVE: red " + describe(red) + " · blue " + describe(blue));
    }

    private static String describe(HiveTracker hive) {
        return hive.state() + (hive.assumed() ? " (assumed)" : "") + ", at least " + hive.tips() + " TIPs";
    }
}
