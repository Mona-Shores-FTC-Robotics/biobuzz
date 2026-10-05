package org.firstinspires.ftc.teamcode.vision;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.Subsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * Both HIVEs over the match (see {@link HiveTracker}): what the camera says, plus whether the robot
 * is still looking where it last saw each one, which is how a TIP is caught when a CELL's tags turn
 * away from the camera. Reads {@link LimelightVisionSubsystem} and the drive's pose; drives nothing.
 *
 * <p>The pose only has to be consistent with itself, so this works without a field reference;
 * without a Pinpoint it cannot tell whether the robot turned, and only a CELL seen mid-tip or
 * settled starts or ends a TIP.
 */
public final class HiveSubsystem implements Subsystem {

    /** How still "still looking" is. Set on a field. */
    @Configurable
    public static class Tuning {
        /** Most the robot may turn since it last saw the HIVE, degrees. */
        public static double lookToleranceDeg = 5;

        /** Most the robot may move since it last saw the HIVE, inches. */
        public static double lookToleranceIn = 2;
    }

    private final LimelightVisionSubsystem vision;
    private final DriveSubsystem drive;
    private final HiveTracker red = new HiveTracker();
    private final HiveTracker blue = new HiveTracker();
    private Pose redSeenFrom;
    private Pose blueSeenFrom;

    public HiveSubsystem(LimelightVisionSubsystem vision, DriveSubsystem drive) {
        this.vision = vision;
        this.drive = drive;
    }

    @Override
    public void initialize() {
        red.reset();
        blue.reset();
        redSeenFrom = null;
        blueSeenFrom = null;
    }

    @Override
    public void update() {
        long nowMs = System.nanoTime() / 1_000_000L;
        Pose pose = drive.pose();
        redSeenFrom = step(red, Alliance.RED, redSeenFrom, pose, nowMs);
        blueSeenFrom = step(blue, Alliance.BLUE, blueSeenFrom, pose, nowMs);
    }

    /** Feeds one tracker; returns where the robot was when it last saw that HIVE. */
    private Pose step(HiveTracker hive, Alliance alliance, Pose seenFrom, Pose pose, long nowMs) {
        double lostForMs = vision.msSinceHiveSeen(alliance);
        if (lostForMs < HiveTracker.Tuning.lostAfterMs) seenFrom = pose;
        hive.observe(vision.seenHive(alliance), lostForMs, stillLooking(seenFrom, pose), nowMs);
        return seenFrom;
    }

    private static boolean stillLooking(Pose seenFrom, Pose now) {
        if (seenFrom == null || now == null) return false;
        double turned = Math.abs(Math.IEEEremainder(now.heading() - seenFrom.heading(), 2 * Math.PI));
        double moved = Math.hypot(now.x() - seenFrom.x(), now.y() - seenFrom.y());
        return turned <= Math.toRadians(Tuning.lookToleranceDeg) && moved <= Tuning.lookToleranceIn;
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
        return hive.state() + (hive.assumed() ? " (assumed)" : "") + ", " + hive.tips() + " TIPs";
    }
}
