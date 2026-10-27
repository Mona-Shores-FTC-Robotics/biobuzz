package org.firstinspires.ftc.teamcode.opmodes;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.subsystems.TurretAim;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

import java.util.Locale;

/**
 * The turret's auto-aim, shared by the OpModes that offer it ({@link BasicDriveTeleOp} and
 * {@link HiveAndPose}), so there is one copy of the rules. An OpMode owns the decision to aim;
 * this owns where to point and tells {@code robot.turret} once per loop while aiming.
 *
 * <p>Where the turret should point this loop. The target CELL is the one at the robot's end of
 * the field ({@link TurretAim#nearEndCell}), unless the camera sees one of our CELLs raised right
 * now, which is what a shot would go at. The angle comes from the camera when it sees the chosen
 * CELL now, else from odometry and the CELL's field point (its raised point: the two differ by an
 * inch and a half and the turret aims the same way at either). With no pose on the field and
 * nothing in view, the turret holds its last target.
 *
 * <p>The HIVE's tip state is deliberately not what picks the CELL: a phantom TIP would swing the
 * turret to the wrong end. It decides whether a shot scores and the launcher's range.
 *
 * <p>If the turret drops its hold (it ran away from the target, so a direction is wrong),
 * {@link #update} stops asking until {@link #start()} is called again: asking every loop would have
 * the turret drive off, drop, and drive off again.
 */
public final class TurretAimer {

    /** A camera sighting older than this is not used to aim; odometry takes over. */
    public static final double SIGHTING_FRESH_MS = 300;

    private final Robot robot;
    private String source = "off";
    private HiveCell cell;
    private double targetDeg = Double.NaN;
    private double rangeIn = Double.NaN;

    public TurretAimer(Robot robot) {
        this.robot = robot;
    }

    /**
     * Begin aiming: the turret holds straight ahead until the first target is found. Also clears a
     * dropped hold, since a new request is what lets the turret try again.
     */
    public void start() {
        source = "waiting for a target";
        robot.turret.home();
    }

    /** Stop reporting an aim. Does not move the turret; the caller says where it goes next. */
    public void off() {
        source = "off";
    }

    /**
     * One loop of aiming. {@code switchBandIn} is the half-width of the band about the HIVE's axle
     * line inside which the aimed CELL does not switch ends, inches.
     */
    public void update(Alliance alliance, double switchBandIn) {
        if (robot.turret.runawayNote() != null) {
            source = "turret dropped the hold: see its note; press the mode button again to retry";
            return;
        }
        if (alliance == Alliance.UNKNOWN) {
            source = "no alliance chosen";
            return;
        }
        Pose pose = robot.drive.poseReferenced() ? robot.drive.pose() : null;

        // The CELL: a raised one the camera sees now beats the near-end rule.
        HiveCell chosen = raisedCellInView(alliance);
        if (chosen == null) {
            chosen = TurretAim.nearEndCell(alliance, pose == null ? Double.NaN : pose.y(), cell,
                    HiveFieldPoints.AXLE_Y_IN, switchBandIn);
        }
        if (chosen == null) {
            source = "no pose on the field, no CELL in view: holding last";
            return;
        }
        cell = chosen;

        double fwd = TurretSubsystem.turretForwardIn;
        double left = TurretSubsystem.turretLeftIn;
        CellSighting sighting = robot.vision.sighting(chosen);
        if (sighting != null && sighting.ageMs() <= SIGHTING_FRESH_MS) {
            Vec3 row = sighting.rowCentreRobot();
            targetDeg = TurretAim.fromRobotFrameDeg(row, fwd, left);
            rangeIn = Math.hypot(row.x() - fwd, row.y() - left);
            source = "camera";
        } else if (pose != null) {
            Vec3 row = HiveFieldPoints.rowCentre(chosen, HiveCellState.UP);
            if (row == null) {
                source = "no field point for " + name(chosen);
                return;
            }
            targetDeg = TurretAim.fromFieldDeg(pose.x(), pose.y(), pose.heading(), fwd, left, row.x(), row.y());
            rangeIn = TurretAim.rangeFromFieldIn(pose.x(), pose.y(), pose.heading(), fwd, left, row.x(), row.y());
            source = "odometry";
        } else {
            source = "no camera on it, pose not on the field: holding last";
            return;
        }
        robot.turret.aim(targetDeg);
    }

    /** One of {@code alliance}'s CELLs the camera sees settled UP right now, else null. */
    private HiveCell raisedCellInView(Alliance alliance) {
        HiveCell[] cells = HiveCell.values();
        for (int i = 0; i < cells.length; i++) {
            HiveCell candidate = cells[i];
            if (!candidate.belongsTo(alliance)) continue;
            CellSighting sighting = robot.vision.sighting(candidate);
            if (sighting != null && sighting.ageMs() <= SIGHTING_FRESH_MS
                    && robot.vision.state(candidate) == HiveCellState.UP) {
                return candidate;
            }
        }
        return null;
    }

    /** Where the last target came from: "camera", "odometry", or why there is none. */
    public String source() {
        return source;
    }

    /** The CELL last aimed at, or null. */
    public HiveCell cell() {
        return cell;
    }

    /** Horizontal range from the turret's axis to the aimed CELL, inches; NaN before the first target. */
    public double rangeIn() {
        return rangeIn;
    }

    /** One line for the Match page while aiming: source, CELL, range, and whether the turret offset is measured. */
    public String line() {
        return "Turret aim: " + source
                + (cell == null ? "" : " · " + name(cell) + " CELL")
                + (Double.isNaN(rangeIn) ? "" : String.format(Locale.US, " · %.0f in", rangeIn))
                + (TurretSubsystem.turretForwardIn == 0.0 && TurretSubsystem.turretLeftIn == 0.0
                ? " · turret offset not measured: aiming from the robot centre"
                : String.format(Locale.US, " · turret at %+.1f fwd, %+.1f left",
                TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn));
    }

    private static String name(HiveCell c) {
        return c == null ? "?" : c.side().toString();
    }
}
