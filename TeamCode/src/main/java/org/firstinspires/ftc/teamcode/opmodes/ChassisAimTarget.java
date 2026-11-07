package org.firstinspires.ftc.teamcode.opmodes;

import org.firstinspires.ftc.teamcode.subsystems.TurretEncoder;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveCell;

import java.util.function.ToDoubleFunction;

/**
 * Where the driver's chassis aim (Circle, turret LOCKED) points the robot: <b>camera first</b>, so it
 * still aims when the odometry is off. The go-to-launch-spot button (Square) trusts the odometry;
 * this trusts the camera, and that is the drive team's choice between them: pose good, Square; pose
 * off, a gyro reset and Circle, with the operator's FIRE deciding the moment (mentor, 7 Nov 2026).
 *
 * <p>Per press, every loop:
 * <ol>
 *   <li><b>Which CELL:</b> the first of ours the camera sees this press, and it stays that CELL for
 *       the press, so the target never swaps as tags come and go. Only our UP CELL counts while the
 *       HIVE's state is known (only an UP CELL scores); unknown, the nearest of ours in view. A
 *       locked CELL that the HIVE then says is down is dropped and the choice made again. The pose
 *       never chooses it: that is the number this button does not trust.</li>
 *   <li><b>Which angle:</b> the camera's bearing to that CELL while it sees it now. If the camera
 *       then loses it (a robot in the way, the turn carrying it out of view), the robot keeps
 *       turning to the field heading the camera last gave, on the gyro alone, which the driver
 *       keeps right (D-pad up). That held heading ignores the robot's own sideways driving since,
 *       so it is a bridge for a short gap, not a replacement for the camera.</li>
 *   <li><b>Otherwise</b> (the camera has not seen one of ours this press), the odometry's aim, as
 *       before this class ({@link TurretAimer#solve}); and with none, no aim: the robot holds its
 *       heading rather than guess.</li>
 * </ol>
 *
 * <p>The range for the launch speed is not chosen here: it already comes from the camera whenever
 * it sees one of our CELLs ({@code BasicDriveTeleOp.shotRangeIn}).
 *
 * <p>Pure and allocation-free: the OpMode passes the camera's answers as two functions made once at
 * init, and calls {@link #update} every loop.
 */
public final class ChassisAimTarget {

    /** Where this loop's aim came from. */
    public enum Source { NONE, CAMERA, GYRO_HOLD, ODOMETRY }

    /** Our CELL's bearing from the turret axis now, degrees CCW from forward; NaN when not freshly in view. */
    private final ToDoubleFunction<HiveCell> bearingDeg;
    /** Our CELL's range from the turret axis now, inches; NaN when not freshly in view. */
    private final ToDoubleFunction<HiveCell> rangeIn;

    private boolean wasHeld;
    private HiveCell locked;
    /** The field heading, radians, that faced the locked CELL at its last camera frame; NaN before one. */
    private double holdHeadingRad = Double.NaN;
    private double aimDeg = Double.NaN;
    private Source source = Source.NONE;
    private boolean lockedThisLoop;
    private boolean cameraThisPress;

    public ChassisAimTarget(ToDoubleFunction<HiveCell> bearingDeg, ToDoubleFunction<HiveCell> rangeIn) {
        this.bearingDeg = bearingDeg;
        this.rangeIn = rangeIn;
    }

    /**
     * One loop.
     *
     * @param held           the chassis aim is wanted: the button held, the turret LOCKED
     * @param alliance       ours; UNKNOWN aims at nothing
     * @param upCell         our UP CELL from the HIVE's state, or null while unknown
     * @param headingRad     the robot's heading, radians (the gyro's); NaN without one
     * @param odometryAimDeg the odometry's aim this loop, trim included ({@link TurretAimer#aimDeg()}); NaN without one
     * @param trimDeg        the operator's trim, degrees, added to the camera's and the held aim
     * @return whether there is an aim this loop ({@link #aimDeg()})
     */
    public boolean update(boolean held, Alliance alliance, HiveCell upCell, double headingRad,
                          double odometryAimDeg, double trimDeg) {
        lockedThisLoop = false;
        if (!held || alliance == null || alliance == Alliance.UNKNOWN) {
            if (!held) wasHeld = false;
            clearPress();
            source = Source.NONE;
            aimDeg = Double.NaN;
            return false;
        }
        if (!wasHeld) {
            wasHeld = true;
            clearPress();
        }
        if (locked != null && upCell != null && locked != upCell) {
            // The HIVE tipped under the press: that CELL no longer scores.
            locked = null;
            holdHeadingRad = Double.NaN;
        }
        if (locked == null) {
            locked = choose(alliance, upCell);
            if (locked != null) {
                lockedThisLoop = true;
                cameraThisPress = true;
            }
        }
        if (locked != null) {
            double bearing = bearingDeg.applyAsDouble(locked);
            if (!Double.isNaN(bearing)) {
                holdHeadingRad = Double.isNaN(headingRad) ? Double.NaN : headingRad + Math.toRadians(bearing);
                aimDeg = bearing + trimDeg;
                source = Source.CAMERA;
                return true;
            }
            if (!Double.isNaN(holdHeadingRad) && !Double.isNaN(headingRad)) {
                aimDeg = TurretEncoder.wrap180(Math.toDegrees(holdHeadingRad - headingRad)) + trimDeg;
                source = Source.GYRO_HOLD;
                return true;
            }
        }
        if (!Double.isNaN(odometryAimDeg)) {
            aimDeg = odometryAimDeg;
            source = Source.ODOMETRY;
            return true;
        }
        aimDeg = Double.NaN;
        source = Source.NONE;
        return false;
    }

    /** Our UP CELL if the camera sees it; with the HIVE unknown, the nearest of ours in view; else null. */
    private HiveCell choose(Alliance alliance, HiveCell upCell) {
        if (upCell != null) {
            return upCell.belongsTo(alliance) && !Double.isNaN(bearingDeg.applyAsDouble(upCell)) ? upCell : null;
        }
        HiveCell best = null;
        double bestRange = Double.POSITIVE_INFINITY;
        HiveCell left = HiveCell.leftCell(alliance);
        HiveCell right = HiveCell.rightCell(alliance);
        for (int i = 0; i < 2; i++) {
            HiveCell c = i == 0 ? left : right;
            if (Double.isNaN(bearingDeg.applyAsDouble(c))) continue;
            double range = rangeIn.applyAsDouble(c);
            if (best == null || range < bestRange) {
                best = c;
                bestRange = Double.isNaN(range) ? Double.POSITIVE_INFINITY : range;
            }
        }
        return best;
    }

    private void clearPress() {
        locked = null;
        holdHeadingRad = Double.NaN;
        cameraThisPress = false;
    }

    /** This loop's aim, degrees from the robot's forward, + left, trim included; NaN with none. */
    public double aimDeg() {
        return aimDeg;
    }

    public Source source() {
        return source;
    }

    /** The CELL this press locked onto from the camera, or null. */
    public HiveCell locked() {
        return locked;
    }

    /** The camera locked a CELL this loop: the press's first sighting of it, once, for the log. */
    public boolean lockedThisLoop() {
        return lockedThisLoop;
    }

    /** The camera has locked a CELL at some point during this press. */
    public boolean cameraThisPress() {
        return cameraThisPress;
    }

    /** LEFT or RIGHT, as the drivers name our CELLs from their alliance area. */
    public static String driverName(HiveCell cell) {
        return cell == null ? "?" : cell == HiveCell.leftCell(cell.alliance()) ? "LEFT" : "RIGHT";
    }

    /** For the Match page: "camera, LEFT CELL" and so on. */
    public String describe() {
        String cell = locked == null ? "" : ", " + driverName(locked) + " CELL";
        switch (source) {
            case CAMERA: return "camera" + cell;
            case GYRO_HOLD: return "camera lost, holding its heading on the gyro" + cell;
            case ODOMETRY: return "odometry (camera sees none of ours" + (cameraThisPress ? " now" : "") + ")";
            default: return "no CELL in view and no pose: holding heading";
        }
    }
}
