package org.firstinspires.ftc.teamcode.subsystems;

import org.firstinspires.ftc.teamcode.vision.Vec3;

/**
 * Where the turret has to point to face a target: pure math, unit tested at a laptop.
 *
 * <p>Every angle is Pedro's: radians or degrees counter-clockwise, the robot's nose at 0 in the
 * chassis frame, field +x at 0 in the field frame. A turret angle is in the chassis frame, from
 * the turret's own axis, which is not the robot's tracked point: the offset is
 * ({@code turretForwardIn}, {@code turretLeftIn}) in the robot frame. Until it is measured the
 * callers pass 0, 0 and aim from the robot centre, which is off by at most a couple of degrees
 * at HIVE range for an offset of a few inches.
 */
public final class TurretAim {

    private TurretAim() {
    }

    /**
     * Turret angle to face a point seen by the camera, from a robot-frame vector to it (what
     * {@code CellSighting.rowCentreRobot()} gives). Degrees, counter-clockwise positive, (-180, 180].
     */
    public static double fromRobotFrameDeg(Vec3 targetRobot, double turretForwardIn, double turretLeftIn) {
        double dx = targetRobot.x() - turretForwardIn;
        double dy = targetRobot.y() - turretLeftIn;
        return Math.toDegrees(Math.atan2(dy, dx));
    }

    /**
     * Turret angle to face a field point from a field pose: the bearing from the turret's axis
     * to the point, minus the robot's heading. Degrees, counter-clockwise positive, (-180, 180].
     *
     * @param robotX, robotY  the tracked point, field inches
     * @param headingRad      the robot's field heading, radians
     * @param targetX, targetY the point, field inches
     */
    public static double fromFieldDeg(double robotX, double robotY, double headingRad,
                                      double turretForwardIn, double turretLeftIn,
                                      double targetX, double targetY) {
        double c = Math.cos(headingRad);
        double s = Math.sin(headingRad);
        double axisX = robotX + turretForwardIn * c - turretLeftIn * s;
        double axisY = robotY + turretForwardIn * s + turretLeftIn * c;
        double fieldBearing = Math.atan2(targetY - axisY, targetX - axisX);
        return TurretEncoder.wrap180(Math.toDegrees(fieldBearing - headingRad));
    }

    /** Horizontal distance from the turret's axis to a field point, inches, for a launcher's speed table. */
    public static double rangeFromFieldIn(double robotX, double robotY, double headingRad,
                                          double turretForwardIn, double turretLeftIn,
                                          double targetX, double targetY) {
        double c = Math.cos(headingRad);
        double s = Math.sin(headingRad);
        double axisX = robotX + turretForwardIn * c - turretLeftIn * s;
        double axisY = robotY + turretForwardIn * s + turretLeftIn * c;
        return Math.hypot(targetX - axisX, targetY - axisY);
    }
}
