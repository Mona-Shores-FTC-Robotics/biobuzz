package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.localization.FusedLocalizer;
import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.localization.PoseFusion;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.util.AccelLimiter;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.LimelightVisionSubsystem;

/**
 * The mecanum drivetrain. Always drives: field-centric when the Pinpoint is there, robot-centric
 * when it is not.
 *
 * <p>An OpMode says what it wants with {@link #drive(double, double, double)} in {@code onLoop()};
 * {@link #update()} then does it, in the same loop. Stick mapping, deadband and speed modes belong
 * to the OpMode — they are driver preferences. How fast power may rise belongs here, because it is
 * about keeping this robot from lurching whoever is driving it.
 *
 * <h2>Fallback</h2>
 *
 * <p>The motors are required: without them this throws at construction, and the OpMode fails to
 * init with the SDK's "could not find" message. The Pinpoint is not: if it is missing, this builds
 * anyway, drives robot-centric, and {@link #localizerFault()} says why. That is the one optional
 * device here, and it is optional on purpose (#21).
 *
 * <h2>Localization</h2>
 *
 * <p>The pose is the Pinpoint's, corrected by AprilTag fixes through a {@link FusedLocalizer}: each
 * loop, every new CELL sighting from {@code vision} whose cell is settled UP or DOWN becomes a fix.
 * {@link #poseTrusted()} says whether the pose is good enough for anything that drives or aims from
 * field coordinates; driving itself never needs it.
 *
 * <p>This does not build a Pedro {@code Follower}. {@code Constants.createAlgorithm()} throws until
 * the Foresight Tuner has run, and this subsystem has to work on an untuned robot. When path
 * following is needed, the Follower joins here.
 */
@Configurable
public class DriveSubsystem implements Subsystem {

    /** How fast drive power may rise, in power per second. Slowing and stopping are instant. */
    public static double accelPerSec = 2.0;

    /** The same, for turning. Separate because a laggy turn makes aiming feel mushy. */
    public static double turnAccelPerSec = 4.0;

    /** A loop slower than this (a hiccup, a GC pause) is treated as this long, so power cannot jump. */
    private static final double MAX_LOOP_DT_SEC = 0.1;

    private final Mecanum drivetrain;
    private final FusedLocalizer localizer;
    private final LimelightVisionSubsystem vision;

    /** Capture time of the last sighting consumed, per cell — so each frame is used once. */
    private final long[] consumedNs = new long[CELLS.length];
    private static final HiveCell[] CELLS = HiveCell.values();
    private int unknownStateSightings;
    private final String localizerFault;

    private final AccelLimiter forwardLimiter = new AccelLimiter();
    private final AccelLimiter strafeLimiter = new AccelLimiter();
    private final AccelLimiter turnLimiter = new AccelLimiter();

    private double forward;
    private double strafe;
    private double turn;
    private boolean accelLimited = true;
    private boolean fieldCentric;
    private double headingOffset;
    private long lastUpdateNs;

    public DriveSubsystem(HardwareMap hardwareMap, LimelightVisionSubsystem vision) {
        this.vision = vision;
        drivetrain = Constants.createDrivetrain(hardwareMap);

        FusedLocalizer found = null;
        String fault = null;
        try {
            found = new FusedLocalizer(Constants.createLocalizer(hardwareMap));
        } catch (RuntimeException e) {
            fault = e.getMessage() == null ? e.toString() : e.getMessage();
        }
        localizer = found;
        localizerFault = fault;
        fieldCentric = found != null;
    }

    // ------------------------------------------------------------- commands

    /**
     * What to do this loop, in Pedro's robot frame: +forward, +strafe is left, +turn is
     * counter-clockwise, each in [-1, 1]. Holds until the next call.
     */
    public void drive(double forward, double strafe, double turn) {
        this.forward = forward;
        this.strafe = strafe;
        this.turn = turn;
    }

    /** False skips the acceleration limit, for a driver who wants speed now (turbo). */
    public void setAccelLimited(boolean limited) {
        accelLimited = limited;
    }

    /** Field-centric "forward" becomes the way the robot faces now. No-op without a Pinpoint. */
    public void resetHeading() {
        if (localizer != null) {
            headingOffset = localizer.pose().heading();
        }
    }

    /**
     * Field-centric "forward" becomes this field heading, radians. Used at PLAY when the pose is
     * field-absolute (handed over from Autonomous) and the driver's forward for the alliance is known.
     */
    public void setFieldForward(double fieldHeading) {
        headingOffset = fieldHeading;
    }

    /**
     * Put the robot at {@code pose} in Pedro field coordinates, known to within {@code sigmaIn} — how
     * TeleOp continues from where Autonomous left it, and how an Autonomous declares its start.
     * Headings become field headings from here on. No-op without a Pinpoint.
     */
    public void setPose(Pose pose, double sigmaIn) {
        if (localizer != null && pose != null) {
            localizer.setPose(pose, sigmaIn);
        }
    }

    /** Switch between field- and robot-centric. Stays robot-centric without a Pinpoint. */
    public void toggleFieldCentric() {
        fieldCentric = !fieldCentric && localizer != null;
    }

    // ---------------------------------------------------------------- state

    public boolean isFieldCentric() {
        return fieldCentric;
    }

    /** Whether a heading is available — false means the Pinpoint was missing at init. */
    public boolean hasHeading() {
        return localizer != null;
    }

    /** Heading relative to the last {@link #resetHeading()}, radians. 0 without a Pinpoint. */
    public double heading() {
        return localizer == null ? 0.0 : localizer.pose().heading() - headingOffset;
    }

    /** Field pose in Pedro coordinates, or null without a Pinpoint. */
    public Pose pose() {
        return localizer == null ? null : localizer.pose();
    }

    /**
     * Whether the field pose is good enough to drive or aim from — within
     * {@code LocalizationTuning.trustedSigmaIn}, with field-referenced headings. False without a
     * Pinpoint, after an init with no handoff, or after driving far without a camera fix.
     */
    public boolean poseTrusted() {
        return localizer != null && localizer.fusion().isTrusted();
    }

    /** Camera fixes accepted so far this OpMode. */
    public int acceptedFixes() {
        return localizer == null ? 0 : localizer.fusion().count(PoseFusion.Verdict.ACCEPTED);
    }

    /** Why the Pinpoint is missing, or null when it is there. */
    public String localizerFault() {
        return localizerFault;
    }

    // ------------------------------------------------------------ lifecycle

    @Override
    public void initialize() {
        lastUpdateNs = System.nanoTime();
    }

    @Override
    public void update() {
        if (localizer != null) {
            localizer.update();
            offerSightings();
        }

        long now = System.nanoTime();
        double dt = Math.min((now - lastUpdateNs) / 1e9, MAX_LOOP_DT_SEC);
        lastUpdateNs = now;

        double f = forward;
        double s = strafe;
        double t = turn;
        if (accelLimited) {
            f = forwardLimiter.step(f, accelPerSec, dt);
            s = strafeLimiter.step(s, accelPerSec, dt);
            t = turnLimiter.step(t, turnAccelPerSec, dt);
        } else {
            // Keep the limiters in step with what the wheels really get, so leaving turbo drops
            // straight to the new command rather than ramping from wherever they were before.
            forwardLimiter.reset(f);
            strafeLimiter.reset(s);
            turnLimiter.reset(t);
        }

        DrivePowers powers = fieldCentric
                ? ManualDrive.fieldCentric(f, s, t, heading())
                : new DrivePowers(f, s, t);

        // manual = true selects BRAKE over FLOAT when the command is zero, if the drivetrain
        // config asks for it: under a driver, the robot stops where it is put.
        drivetrain.drive(powers, true);
    }

    /** Each new CELL sighting, once, as a fix — if its cell is settled in a known state. */
    private void offerSightings() {
        if (vision == null || !vision.isAvailable()) {
            return;
        }
        for (int i = 0; i < CELLS.length; i++) {
            CellSighting sighting = vision.sighting(CELLS[i]);
            if (sighting == null || sighting.captureTimeNs() <= consumedNs[i]) {
                continue;
            }
            consumedNs[i] = sighting.captureTimeNs();
            HiveCellState state = vision.state(CELLS[i]);
            if (state == HiveCellState.UNKNOWN) {
                unknownStateSightings++;
                continue;
            }
            localizer.offer(sighting, state);
        }
    }

    @Override
    public void stop() {
        drivetrain.stop();
    }

    @Override
    public void describe(Display display) {
        if (localizer != null) {
            display.status("Pinpoint", Display.Level.OK, "present");
        } else {
            display.status("Pinpoint", Display.Level.WARN, "MISSING — " + localizerFault);
        }
        display.status("Mode", Display.Level.OK, fieldCentric ? "field-centric" : "robot-centric");
        if (localizer != null) {
            describeLocalization(display);
        }
        if (localizer != null) {
            display.line(String.format(java.util.Locale.US, "Heading %.1f°", Math.toDegrees(heading())));
        }
    }

    private void describeLocalization(Display display) {
        PoseFusion fusion = localizer.fusion();
        Pose pose = localizer.pose();
        String where = String.format(java.util.Locale.US, "(%.1f, %.1f, %.0f°) ±%.1f in",
                pose.x(), pose.y(), Math.toDegrees(pose.heading()), fusion.sigmaIn());
        if (fusion.isTrusted()) {
            display.status("Pose", Display.Level.OK, "trusted " + where);
        } else if (!fusion.isHeadingReferenced()) {
            display.status("Pose", Display.Level.WARN, "not field-referenced — no start or handoff");
        } else {
            display.status("Pose", Display.Level.WARN, "untrusted " + where);
        }
        if (!HiveFieldPoints.anyMeasured()) {
            display.line("<small>Tag fixes off: HIVE field points not measured</small>");
            return;
        }
        display.line(String.format(java.util.Locale.US,
                "<small>Fixes: %d used · rejected %d outlier, %d turning, %d old, %d unreferenced,"
                        + " %d cell state unknown</small>",
                fusion.count(PoseFusion.Verdict.ACCEPTED), fusion.count(PoseFusion.Verdict.OUTLIER),
                fusion.count(PoseFusion.Verdict.TURNING), fusion.count(PoseFusion.Verdict.TOO_OLD),
                fusion.count(PoseFusion.Verdict.HEADING_NOT_REFERENCED), unknownStateSightings));
    }
}
