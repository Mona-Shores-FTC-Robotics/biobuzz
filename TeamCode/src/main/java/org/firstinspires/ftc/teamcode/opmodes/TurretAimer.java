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
 * <p>Where the turret should point this loop. The target CELL is ours at the robot's end of the
 * field ({@link TurretAim#nearEndCell}): left half, left CELL; right half, right CELL, with a band
 * about the HIVE's axle line so it does not flick between them. Nothing the camera sees changes
 * which CELL that is; the operator decides whether it is up. Only with no pose on the field, when
 * there is no half to go by, is it the CELL already chosen, else the nearest of ours in view.
 *
 * <p>The angle comes from the camera when it sees the chosen CELL now, else from odometry and the
 * CELL's field point (its raised point: the two differ by an inch and a half and the turret aims
 * the same way at either). With no pose on the field and nothing in view, the turret holds its
 * last target.
 *
 * <p>The operator's trim ({@link #trimLeft}, {@link #trimRight}) is added to every aim and stays
 * until {@link #resetTrim}. {@link #solve} works out the aim without moving the turret, which is
 * what the chassis aim and the aim gate use while the turret is LOCKED.
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
    private double trimDeg;
    private double aimDeg = Double.NaN;

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
     * One loop of aiming the turret: {@link #solve}, then point the turret there. {@code switchBandIn}
     * is the half-width of the band about the HIVE's axle line inside which the aimed CELL does not
     * switch ends, inches.
     */
    public void update(Alliance alliance, double switchBandIn) {
        if (robot.turret.runawayNote() != null) {
            source = "turret dropped the hold: see its note; press the mode button again to retry";
            aimDeg = Double.NaN;
            return;
        }
        if (solve(alliance, switchBandIn)) {
            robot.turret.aim(aimDeg);
        }
    }

    /**
     * Where a shot should go this loop, without moving anything: {@link #aimDeg()}, the angle from
     * the robot's forward, trim included. True when there is a fresh answer; false leaves the last
     * one and says why in {@link #source()}.
     */
    public boolean solve(Alliance alliance, double switchBandIn) {
        aimDeg = Double.NaN;
        if (alliance == Alliance.UNKNOWN) {
            source = "no alliance chosen";
            return false;
        }
        Pose pose = robot.drive.poseReferenced() ? robot.drive.pose() : null;
        HiveCell chosen = TurretAim.nearEndCell(alliance, pose == null ? Double.NaN : pose.y(), cell,
                HiveFieldPoints.AXLE_Y_IN, switchBandIn);
        if (chosen == null && pose == null) {
            // No pose, so no half of the field: keep the CELL we had, else the nearest of ours in view.
            chosen = cell != null && cell.belongsTo(alliance) ? cell : nearestInView(alliance);
        }
        if (chosen == null) {
            source = "no pose on the field, none of our CELLs in view: holding last";
            return false;
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
                return false;
            }
            targetDeg = TurretAim.fromFieldDeg(pose.x(), pose.y(), pose.heading(), fwd, left, row.x(), row.y());
            rangeIn = TurretAim.rangeFromFieldIn(pose.x(), pose.y(), pose.heading(), fwd, left, row.x(), row.y());
            source = "odometry";
        } else {
            source = "no camera on it, pose not on the field: holding last";
            return false;
        }
        aimDeg = targetDeg + trimDeg;
        return true;
    }

    /** This loop's aim from {@link #solve}, degrees from the robot's forward, + left; NaN when there is none. */
    public double aimDeg() {
        return aimDeg;
    }

    /** Trim one step to the left (counter-clockwise), degrees. */
    public void trimLeft(double stepDeg) {
        trimDeg += stepDeg;
    }

    /** Trim one step to the right (clockwise), degrees. */
    public void trimRight(double stepDeg) {
        trimDeg -= stepDeg;
    }

    public void resetTrim() {
        trimDeg = 0.0;
    }

    /** The operator's trim, degrees, + left. */
    public double trimDeg() {
        return trimDeg;
    }

    /** The nearest of {@code alliance}'s CELLs the camera sees now, in any state, else null. */
    private HiveCell nearestInView(Alliance alliance) {
        HiveCell best = null;
        double bestRange = Double.POSITIVE_INFINITY;
        HiveCell[] cells = HiveCell.values();
        for (int i = 0; i < cells.length; i++) {
            if (!cells[i].belongsTo(alliance)) continue;
            CellSighting sighting = robot.vision.sighting(cells[i]);
            if (sighting != null && sighting.ageMs() <= SIGHTING_FRESH_MS && sighting.groundRangeIn() < bestRange) {
                bestRange = sighting.groundRangeIn();
                best = cells[i];
            }
        }
        return best;
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
                + (trimDeg == 0.0 ? "" : String.format(Locale.US, " · trim %+.1f°", trimDeg))
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
