package org.firstinspires.ftc.teamcode.opmodes;

import org.firstinspires.ftc.teamcode.subsystems.TurretAim;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveCell;

import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;

/**
 * Where the driver's chassis aim (Circle, turret LOCKED) points the robot: <b>camera first</b>, so it
 * still aims when the odometry is off. The go-to-launch-spot button (Square) trusts the odometry;
 * this trusts the camera, and that is the drive team's choice between them: pose good, Square; pose
 * off, a gyro reset and Circle, with the operator's FIRE deciding the moment (mentor, 7 Nov 2026).
 *
 * <p><b>The anchor.</b> Every fresh camera frame of our CELL, in a press or not, stores where that
 * CELL is as a point in the Pinpoint's own frame: the robot's pose plus the camera's bearing and
 * range. When the camera is not on the CELL, the aim and the range go to that point. The pose need
 * not be right, only consistent: a pose that is inches off or turned was just as wrong when the
 * point was stored, so the error cancels (at Belding the camera and the odometry were 0.4 to 5.3 in
 * apart at launch spots, up to 6&deg; at launch range). It survives a Pinpoint read fault, since the
 * Pinpoint keeps counting on its own board ({@code PinpointTrust}), and it has no time limit. It is
 * dropped when the pose is set on purpose (a corner or camera reset moves the frame), when the
 * HIVE's state says another CELL is up, and replaced by the next fresh frame. While the Pinpoint is
 * not reading, the pose is frozen and nothing measures the robot's movement, so the anchor is not
 * used: the camera aims or nothing does.
 *
 * <p>Per press, every loop:
 * <ol>
 *   <li><b>Which CELL:</b> the first of ours the camera sees this press, else the anchor's, and it
 *       stays that CELL for the press, so the target never swaps as tags come and go. Only our UP
 *       CELL counts while the HIVE's state is known (only an UP CELL scores); unknown, the nearest
 *       of ours in view. A CELL that the HIVE then says is down is dropped and the choice made
 *       again. The pose never chooses it: that is the number this button does not trust.</li>
 *   <li><b>Which angle and range:</b> the camera's to that CELL while it sees it now; otherwise the
 *       anchor's (a robot in the way, the turn carrying it out of view, a press that starts facing
 *       away).</li>
 *   <li><b>Otherwise</b> (no camera on one of ours and no anchor), the odometry's aim
 *       ({@link TurretAimer#solve}); and with none, no aim: the robot holds its heading rather than
 *       guess.</li>
 * </ol>
 *
 * <p>Pure and allocation-free: the OpMode passes the camera's answers as functions made once at
 * init, and calls {@link #update} every loop.
 */
public final class ChassisAimTarget {

    /** Where this loop's aim came from. */
    public enum Source { NONE, CAMERA, ANCHOR, ODOMETRY }

    /**
     * The oldest camera frame an anchor is stored from, ms. The frame is placed with the pose of
     * this loop, so an old frame taken while the robot turned would put the point in the wrong place.
     */
    public static final double ANCHOR_MAX_AGE_MS = 120;

    /** Our CELL's bearing from the turret axis now, degrees CCW from forward; NaN when not freshly in view. */
    private final ToDoubleFunction<HiveCell> bearingDeg;
    /** Our CELL's range from the turret axis now, inches; NaN when not freshly in view. */
    private final ToDoubleFunction<HiveCell> rangeIn;
    /** The age of the camera frame behind those two, ms; NaN or infinite with none. */
    private final ToDoubleFunction<HiveCell> ageMs;
    /** The turret axis in the robot's frame, inches forward and left of the tracked point. */
    private final DoubleSupplier turretForwardIn;
    private final DoubleSupplier turretLeftIn;

    private boolean wasHeld;
    private HiveCell locked;
    private HiveCell anchorCell;
    private double anchorX;
    private double anchorY;
    /** {@code poseSets} when the anchor was stored: a pose set since then moved the frame. */
    private int anchorPoseSets;
    private double aimDeg = Double.NaN;
    private double aimRangeIn = Double.NaN;
    private Source source = Source.NONE;
    private boolean lockedThisLoop;
    private boolean cameraThisPress;

    public ChassisAimTarget(ToDoubleFunction<HiveCell> bearingDeg, ToDoubleFunction<HiveCell> rangeIn,
                            ToDoubleFunction<HiveCell> ageMs,
                            DoubleSupplier turretForwardIn, DoubleSupplier turretLeftIn) {
        this.bearingDeg = bearingDeg;
        this.rangeIn = rangeIn;
        this.ageMs = ageMs;
        this.turretForwardIn = turretForwardIn;
        this.turretLeftIn = turretLeftIn;
    }

    /**
     * One loop, held or not: the anchor is kept up to date between presses too.
     *
     * @param held           the chassis aim is wanted: the button held, the turret LOCKED
     * @param alliance       ours; UNKNOWN aims at nothing
     * @param upCell         our UP CELL from the HIVE's state, or null while unknown
     * @param x              the Pinpoint's pose, inches, believed or not; NaN without one
     * @param y              as {@code x}
     * @param headingRad     the Pinpoint's heading, radians; NaN without one
     * @param poseLive       the Pinpoint is reading this loop (not in a held fault), so the pose moves
     *                       with the robot
     * @param poseSets       how many times the pose has been set on purpose ({@code DriveSubsystem.poseSets()})
     * @param odometryAimDeg the odometry's aim this loop, trim included ({@link TurretAimer#aimDeg()}); NaN without one
     * @param trimDeg        the operator's trim, degrees, added to the camera's and the anchor's aim
     * @return whether there is an aim this loop ({@link #aimDeg()})
     */
    public boolean update(boolean held, Alliance alliance, HiveCell upCell, double x, double y, double headingRad,
                          boolean poseLive, int poseSets, double odometryAimDeg, double trimDeg) {
        lockedThisLoop = false;
        aimRangeIn = Double.NaN;
        if (alliance == null || alliance == Alliance.UNKNOWN) {
            if (!held) wasHeld = false;
            clearPress();
            anchorCell = null;
            source = Source.NONE;
            aimDeg = Double.NaN;
            return false;
        }
        boolean live = poseLive && !Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(headingRad);
        if (anchorCell != null && (poseSets != anchorPoseSets || !anchorCell.belongsTo(alliance)
                || (upCell != null && anchorCell != upCell))) {
            anchorCell = null; // the frame moved, or that CELL no longer scores
        }
        if (!held) {
            wasHeld = false;
            clearPress();
            source = Source.NONE;
            aimDeg = Double.NaN;
            HiveCell seen = anchorCell != null && inView(anchorCell) ? anchorCell : choose(alliance, upCell);
            if (seen != null) anchor(seen, x, y, headingRad, live, poseSets);
            return false;
        }
        if (!wasHeld) {
            wasHeld = true;
            clearPress();
        }
        if (locked != null && upCell != null && locked != upCell) {
            locked = null; // the HIVE tipped under the press: that CELL no longer scores
        }
        if (locked != null && !cameraThisPress && locked != anchorCell) {
            locked = null; // the press was on the anchor alone, and the anchor is gone
        }
        if (locked == null || !cameraThisPress) {
            HiveCell seen = locked != null && inView(locked) ? locked : choose(alliance, upCell);
            if (seen != null) {
                locked = seen;
                lockedThisLoop = true;
                cameraThisPress = true;
            } else if (locked == null) {
                locked = anchorCell;
            }
        }
        if (locked != null) {
            double bearing = bearingDeg.applyAsDouble(locked);
            if (!Double.isNaN(bearing)) {
                anchor(locked, x, y, headingRad, live, poseSets);
                aimDeg = bearing + trimDeg;
                aimRangeIn = rangeIn.applyAsDouble(locked);
                source = Source.CAMERA;
                return true;
            }
            if (live && anchorCell == locked) {
                double fwd = turretForwardIn.getAsDouble();
                double left = turretLeftIn.getAsDouble();
                aimDeg = TurretAim.fromFieldDeg(x, y, headingRad, fwd, left, anchorX, anchorY) + trimDeg;
                aimRangeIn = TurretAim.rangeFromFieldIn(x, y, headingRad, fwd, left, anchorX, anchorY);
                source = Source.ANCHOR;
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

    private boolean inView(HiveCell cell) {
        return !Double.isNaN(bearingDeg.applyAsDouble(cell));
    }

    /** Stores {@code cell}'s point in the Pinpoint's frame from this loop's camera frame, if it is fresh and the pose is live. */
    private void anchor(HiveCell cell, double x, double y, double headingRad, boolean live, int poseSets) {
        if (!live) return;
        double age = ageMs.applyAsDouble(cell);
        double bearing = bearingDeg.applyAsDouble(cell);
        double range = rangeIn.applyAsDouble(cell);
        if (!(age <= ANCHOR_MAX_AGE_MS) || Double.isNaN(bearing) || Double.isNaN(range)) return;
        double fwd = turretForwardIn.getAsDouble();
        double left = turretLeftIn.getAsDouble();
        double c = Math.cos(headingRad);
        double s = Math.sin(headingRad);
        double toward = headingRad + Math.toRadians(bearing);
        anchorX = x + fwd * c - left * s + range * Math.cos(toward);
        anchorY = y + fwd * s + left * c + range * Math.sin(toward);
        anchorCell = cell;
        anchorPoseSets = poseSets;
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
        cameraThisPress = false;
    }

    /** This loop's aim, degrees from the robot's forward, + left, trim included; NaN with none. */
    public double aimDeg() {
        return aimDeg;
    }

    /**
     * This loop's range from the turret axis to the CELL aimed at, inches: the camera's, else the
     * anchor's; NaN when the aim is the odometry's or there is none.
     */
    public double rangeIn() {
        return aimRangeIn;
    }

    public Source source() {
        return source;
    }

    /** The CELL the anchor is on, or null with none. */
    public HiveCell anchored() {
        return anchorCell;
    }

    /** The CELL this press is on, from the camera or the anchor, or null. */
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
            case ANCHOR: return "camera not on it, aiming where it last saw it" + cell;
            case ODOMETRY: return "odometry (camera sees none of ours" + (cameraThisPress ? " now" : "") + ")";
            default: return "no CELL in view and no pose: holding heading";
        }
    }
}
