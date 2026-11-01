package org.firstinspires.ftc.teamcode.localization;

import org.firstinspires.ftc.teamcode.util.WelfordVariance;

import java.util.Locale;

/**
 * Calibrates the camera's mount offsets from a spot where the robot's position is known: the
 * robot sits on its marks, still, and whatever the camera says it is off by <i>is</i> the camera's
 * error. Pure math; {@code Vision: HIVE &amp; Pose} feeds it and applies its answer.
 *
 * <p><b>The one line of maths.</b> {@code CellFix} puts the robot at the tag's field point minus the
 * sighting rotated by the heading, and the sighting carries the mount translation
 * ({@code CameraMount.toRobotFrame} adds {@code mountForwardIn}, {@code mountLeftIn}). So if the
 * mount is wrong by {@code d} (robot axes), every tag puts the robot at {@code -d} from the truth,
 * whichever way the robot faces. The residual camera-minus-truth, turned into robot axes, is
 * therefore the correction to <b>add</b> to the mount: {@code new = old + residual}.
 * {@code CameraCalibrationTest} closes that loop through the real {@code CameraMount} and
 * {@code CellFix}.
 *
 * <p>What it cannot do: tell a mount offset from a HIVE field point that is off on this field, or
 * from a robot that is not on its marks. All three look the same from one spot. A mount error gives
 * the same answer from a second marked spot that faces a HIVE the other way round (the red far wall
 * against the red audience start); the others do not ({@link #agree}). Turning on the spot is no
 * use: the camera is pitched up at the HIVE and sees nothing elsewhere. The caller says so on the
 * screen.
 *
 * <p>Height is reported but never applied: a height error can also be the row, the CELL's flex or
 * the lens axis inside the housing, and on 20245 the number that zeroed it (12 in) contradicted a
 * tape (14 in). It is on the screen for the record.
 */
public final class CameraCalibration {

    /** Still frames to average before the answer counts. About 1 s of camera frames. */
    public static final int FRAMES_WANTED = 50;

    /** A mean this large is not a mount error: the robot is off its marks or the map is. */
    public static final double TOO_FAR_IN = 3.0;

    /** Two spots' suggested mounts closer than this, inches, are the same answer: it is the mount. */
    public static final double AGREE_IN = 0.5;

    /**
     * Whether two spots' suggested mounts (absolute forward/left, inches) agree to {@link #AGREE_IN}.
     * A mount error gives the same answer from every spot; a HIVE point or a placement that is off
     * gives a different one from a spot that faces the other way.
     */
    public static boolean agree(double fwd1, double left1, double fwd2, double left2) {
        return Math.hypot(fwd1 - fwd2, left1 - left2) <= AGREE_IN;
    }

    private final WelfordVariance forward = new WelfordVariance();
    private final WelfordVariance left = new WelfordVariance();
    private final WelfordVariance height = new WelfordVariance();

    /**
     * One still frame: camera-minus-odometry in <b>field</b> axes ({@code dxIn}, {@code dyIn}) with
     * the robot at {@code headingRad}, and the frame's mean tag height error (measured minus map),
     * NaN if unknown.
     */
    public void add(double dxIn, double dyIn, double headingRad, double heightErrorIn) {
        double c = Math.cos(headingRad);
        double s = Math.sin(headingRad);
        forward.update(c * dxIn + s * dyIn);
        left.update(-s * dxIn + c * dyIn);
        if (!Double.isNaN(heightErrorIn)) height.update(heightErrorIn);
    }

    public void reset() {
        forward.reset();
        left.reset();
        height.reset();
    }

    public int frames() { return forward.n(); }

    public boolean ready() { return frames() >= FRAMES_WANTED; }

    /** Mean residual, robot axes, inches: how far the camera says the robot is from where it is. */
    public double forwardIn() { return forward.mean(); }
    public double leftIn() { return left.mean(); }

    /** Mean tag height error, inches; NaN if no frame carried one. */
    public double heightErrorIn() { return height.n() == 0 ? Double.NaN : height.mean(); }

    /** The frames' scatter about the mean, inches (one sigma, both axes combined). */
    public double spreadIn() {
        return frames() < 2 ? Double.NaN : Math.hypot(forward.stdDev(), left.stdDev());
    }

    /** Whether the mean is too big to be a mount error ({@link #TOO_FAR_IN}). */
    public boolean tooFar() { return Math.hypot(forwardIn(), leftIn()) > TOO_FAR_IN; }

    /** The mount forward offset that zeroes the mean, given the one in force when the frames were taken. */
    public double suggestedForwardIn(double currentForwardIn) { return currentForwardIn + forwardIn(); }

    /** The mount left offset that zeroes the mean, given the one in force when the frames were taken. */
    public double suggestedLeftIn(double currentLeftIn) { return currentLeftIn + leftIn(); }

    /**
     * The mount height that would zero the mean height error, for the record only (see the class
     * note). NaN without height samples. Height error is measured minus map, so a positive error
     * means the lens is modelled too high.
     */
    public double heightZeroingUpIn(double currentUpIn) {
        double h = heightErrorIn();
        return Double.isNaN(h) ? Double.NaN : currentUpIn - h;
    }

    /** One line for a screen or a log. */
    public String describe(double currentForwardIn, double currentLeftIn, double currentUpIn) {
        if (frames() == 0) return "no still frames yet";
        String line = String.format(Locale.US,
                "%d frames · camera is %+.2f fwd, %+.2f left of odometry (spread %.2f) · CameraMount would be fwd %.2f, left %.2f",
                frames(), forwardIn(), leftIn(), spreadIn(),
                suggestedForwardIn(currentForwardIn), suggestedLeftIn(currentLeftIn));
        if (!Double.isNaN(heightErrorIn())) {
            line += String.format(Locale.US, " · height %+.2f (up %.1f would zero it; not applied)",
                    heightErrorIn(), heightZeroingUpIn(currentUpIn));
        }
        return line;
    }
}
