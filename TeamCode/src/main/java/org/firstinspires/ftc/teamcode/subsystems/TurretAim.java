package org.firstinspires.ftc.teamcode.subsystems;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveState;
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

    /**
     * Which of our CELLs to keep the turret on, from where the robot is: the one at the robot's end
     * of the field, AUDIENCE below the HIVE's axle line and SCORING above it, with a band either
     * side of the line inside which the previous choice stands, so driving along the HIVE does not
     * flip the turret back and forth. The two CELLs are nearly in line from anywhere near our end,
     * so the tip state barely changes where to point; it changes whether a shot scores and how
     * hard to throw, which is the launcher's business. A CELL the camera sees raised right now is
     * the better target, and the caller prefers it.
     *
     * @param alliance  ours; UNKNOWN gives null
     * @param robotY    the robot's field y, inches
     * @param previous  the CELL last chosen, or null; held while inside the band
     * @param axleY     the HIVE axle's field y ({@code HiveFieldPoints.AXLE_Y_IN})
     * @param bandIn    half-width of the band around the axle line, inches
     */
    public static HiveCell nearEndCell(Alliance alliance, double robotY, HiveCell previous,
                                       double axleY, double bandIn) {
        HiveCell audience = HiveCell.cell(alliance, HiveCell.Side.AUDIENCE);
        HiveCell scoring = HiveCell.cell(alliance, HiveCell.Side.SCORING);
        if (audience == null || scoring == null || Double.isNaN(robotY)) return null;
        double band = Double.isNaN(bandIn) ? 0.0 : Math.abs(bandIn);
        if (robotY < axleY - band) return audience;
        if (robotY > axleY + band) return scoring;
        if (previous == audience || previous == scoring) return previous;
        return robotY < axleY ? audience : scoring;
    }

    /**
     * Our CELL that is UP by the HIVE's state, else {@code fallback}: the right CELL for
     * {@link HiveState#RIGHT_CELL_UP}, the left for {@link HiveState#LEFT_CELL_UP}, and
     * {@code fallback} (the near-end CELL) while the HIVE is tipping or unseen. Only an UP CELL
     * scores, so a range from odometry is taken to this one: the near-end CELL by itself is the
     * DOWN one half the time. An assumed state counts (after a TIP the camera did not see finish,
     * or at an Autonomous's start): it is still a better guess than which end the robot is at.
     *
     * @param alliance ours; UNKNOWN gives {@code fallback}
     * @param state    our HIVE's {@code HiveTracker.state()}; null gives {@code fallback}
     */
    public static HiveCell upCellOr(Alliance alliance, HiveState state, HiveCell fallback) {
        HiveCell up = null;
        if (state == HiveState.RIGHT_CELL_UP) up = HiveCell.rightCell(alliance);
        if (state == HiveState.LEFT_CELL_UP) up = HiveCell.leftCell(alliance);
        return up != null ? up : fallback;
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
