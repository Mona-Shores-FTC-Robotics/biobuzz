package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.autokit.AutoDrive;
import org.firstinspires.ftc.teamcode.controls.Display;
import com.pedropathing.localization.FusionLocalizer;

import org.firstinspires.ftc.teamcode.localization.CellFix;
import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.localization.LocalizationTuning;
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
 * <p>The pose comes from Pedro's {@link FusionLocalizer} over the Pinpoint. The camera sets it and
 * may correct it, the way DECODE used MegaTag1 then MegaTag2:
 * <ul>
 *   <li><b>Seed.</b> While the pose is not {@link #poseReferenced()} (no declared start, no Auto
 *       handoff), a settled CELL seen with two or more tags gives the whole pose, heading included
 *       ({@link CellFix#pose}); once {@link LocalizationTuning#seedFrames} frames in a row agree,
 *       the pose is set from it. The driver's field-centric forward does not move.</li>
 *   <li><b>Relocalize.</b> {@link #relocalizeFromCamera()} does the same on demand, refusing a
 *       jump bigger than DECODE's guards.</li>
 *   <li><b>Fixes.</b> Once referenced, each new settled sighting gives a position fix
 *       ({@link CellFix#position}) with the Pinpoint's heading. They feed the filter only with
 *       {@link LocalizationTuning#continuousFixes} on, which is off until shown to help; they are
 *       always counted, for the start check.</li>
 * </ul>
 *
 * <h2>Following paths</h2>
 *
 * <p>An Autonomous built in the Auto Builder drives through this class as its {@link AutoDrive}.
 * The Pedro {@code Follower} is built only by {@link #preparePathFollowing()}, because
 * {@code Constants.createAlgorithm()} throws until the Foresight Tuner has run and this subsystem
 * has to work on an untuned robot. While a path or hold is in control, {@link #update()} hands the
 * whole loop to the Follower — it updates the localizer itself — and still offers camera fixes.
 * The next {@link #drive} call hands control back to the sticks.
 */
@Configurable
public class DriveSubsystem implements Subsystem, AutoDrive {

    /** How fast drive power may rise, in power per second. Slowing and stopping are instant. */
    public static double accelPerSec = 2.0;

    /** The same, for turning. Separate because a laggy turn makes aiming feel mushy. */
    public static double turnAccelPerSec = 4.0;

    /** A loop slower than this (a hiccup, a GC pause) is treated as this long, so power cannot jump. */
    private static final double MAX_LOOP_DT_SEC = 0.1;

    private final Mecanum drivetrain;
    private final FusionLocalizer localizer;
    private boolean poseReferenced;
    private final LimelightVisionSubsystem vision;

    /** Capture time of the last sighting consumed, per cell — so each frame is used once. */
    private final long[] consumedNs = new long[CELLS.length];
    private static final HiveCell[] CELLS = HiveCell.values();
    private int fixCount;
    private double fixSumX;
    private double fixSumY;
    /** Camera poses in a row that agreed, toward a seed; and the last of them. */
    private int seedAgreeing;
    private Pose seedCandidate;
    /** What the camera last did to the pose, for the Robot page. */
    private String cameraPoseNote = "not yet";
    private final String localizerFault;

    private final AccelLimiter forwardLimiter = new AccelLimiter();
    private final AccelLimiter strafeLimiter = new AccelLimiter();
    private final AccelLimiter turnLimiter = new AccelLimiter();

    private double forward;
    private double strafe;
    private double turn;
    private boolean accelLimited = true;

    /** Built by {@link #preparePathFollowing()}; null until then. */
    private Follower follower;
    /** The path last given to {@link #follow}, for progress. */
    private Path followedPath;
    /** True while a path or hold, not the sticks, drives the robot. */
    private boolean pathControl;
    private boolean fieldCentric;
    private double headingOffset;
    private long lastUpdateNs;

    public DriveSubsystem(HardwareMap hardwareMap, LimelightVisionSubsystem vision) {
        this.vision = vision;
        drivetrain = Constants.createDrivetrain(hardwareMap);

        FusionLocalizer found = null;
        String fault = null;
        try {
            found = LocalizationTuning.newFusionLocalizer(Constants.createLocalizer(hardwareMap));
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
        pathControl = false;
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
     * Put the robot at {@code pose} in Pedro field coordinates — how
     * TeleOp continues from where Autonomous left it, and how an Autonomous declares its start.
     * Headings become field headings from here on. No-op without a Pinpoint.
     */
    public void setPose(Pose pose) {
        if (localizer != null && pose != null) {
            localizer.setPose(pose);
            poseReferenced = true;
            fixCount = 0;
            fixSumX = 0.0;
            fixSumY = 0.0;
        }
    }

    /**
     * Sets the pose from the camera now: the best settled CELL in view with two or more tags gives
     * x, y and heading ({@link CellFix#pose}). Refused if the pose is already field-referenced and
     * this would move it more than {@link LocalizationTuning#maxRelocalizeJumpIn} or turn it more
     * than {@link LocalizationTuning#maxRelocalizeJumpDeg}. The driver's field-centric forward stays
     * where it was. For a button; best done holding still.
     *
     * @return what happened, for the driver
     */
    public String relocalizeFromCamera() {
        if (localizer == null) {
            return cameraPoseNote = "relocalize: no Pinpoint";
        }
        Pose best = null;
        int bestTags = 0;
        if (vision != null && vision.isAvailable()) {
            for (HiveCell cell : CELLS) {
                CellSighting sighting = vision.sighting(cell);
                if (sighting == null || sighting.tagCount() <= bestTags) continue;
                Pose pose = CellFix.pose(cell, vision.state(cell), sighting.rowCentreRobot(),
                        sighting.lateralAxisRobot());
                if (pose != null) {
                    best = pose;
                    bestTags = sighting.tagCount();
                }
            }
        }
        if (best == null) {
            return cameraPoseNote = "relocalize: no settled CELL with 2+ tags (and a known row direction)";
        }
        if (poseReferenced) {
            Pose now = localizer.pose();
            double jumpIn = Math.hypot(best.x() - now.x(), best.y() - now.y());
            double turnDeg = Math.toDegrees(
                    Math.abs(Math.IEEEremainder(best.heading() - now.heading(), 2 * Math.PI)));
            if (jumpIn > LocalizationTuning.maxRelocalizeJumpIn
                    || turnDeg > LocalizationTuning.maxRelocalizeJumpDeg) {
                return cameraPoseNote = String.format(java.util.Locale.US,
                        "relocalize refused: %.1f in / %.0f° from the current pose", jumpIn, turnDeg);
            }
        }
        setPoseKeepingDriverForward(best);
        return cameraPoseNote = "relocalized " + describePose(best);
    }

    /** {@link #setPose}, shifting the field-centric reference so the driver's forward stays put. */
    private void setPoseKeepingDriverForward(Pose pose) {
        double before = localizer.pose().heading();
        setPose(pose);
        headingOffset += pose.heading() - before;
        seedAgreeing = 0;
        seedCandidate = null;
    }

    private static String describePose(Pose pose) {
        return String.format(java.util.Locale.US, "(%.1f, %.1f, %.0f°)",
                pose.x(), pose.y(), Math.toDegrees(pose.heading()));
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
     * Whether the pose is in field coordinates — set from a declared start or Autonomous's handoff.
     * False without a Pinpoint, or in a TeleOp with no handoff, where (0, 0) is just where the robot
     * sat at init. Anything that drives or aims from field coordinates checks this first.
     */
    public boolean poseReferenced() {
        return poseReferenced;
    }

    /** Camera fixes offered since the pose was last set. */
    public int fixCount() {
        return fixCount;
    }

    /** Average x of those fixes, inches — where the camera says the robot is. For the start check. */
    public double meanFixX() {
        return fixCount == 0 ? Double.NaN : fixSumX / fixCount;
    }

    /** Average y of those fixes, inches. */
    public double meanFixY() {
        return fixCount == 0 ? Double.NaN : fixSumY / fixCount;
    }

    /** Why the Pinpoint is missing, or null when it is there. */
    public String localizerFault() {
        return localizerFault;
    }

    // ------------------------------------------------------- path following

    /**
     * Builds the Pedro Follower. Call it in an Autonomous's init, so an untuned robot or a missing
     * Pinpoint stops the OpMode there, with the reason, rather than when the first path starts.
     */
    public void preparePathFollowing() {
        if (follower != null) {
            return;
        }
        if (localizer == null) {
            throw new IllegalStateException("Following a path needs the Pinpoint: " + localizerFault);
        }
        follower = new Follower(localizer, drivetrain, Constants.createAlgorithm());
    }

    @Override
    public void follow(Path path) {
        preparePathFollowing();
        followedPath = path;
        follower.follow(path);
        pathControl = true;
    }

    @Override
    public boolean pathDone() {
        // The Follower leaves FOLLOW (for HOLD at the end point) once the path is finished.
        return follower == null || followedPath == null || !follower.following();
    }

    @Override
    public void hold(Pose pose) {
        preparePathFollowing();
        followedPath = null;
        follower.hold(pose);
        pathControl = true;
    }

    // ------------------------------------------------------------ lifecycle

    @Override
    public void initialize() {
        lastUpdateNs = System.nanoTime();
    }

    @Override
    public void update() {
        long now = System.nanoTime();
        double dt = Math.min((now - lastUpdateNs) / 1e9, MAX_LOOP_DT_SEC);
        lastUpdateNs = now;

        if (pathControl && follower != null) {
            follower.update(dt); // updates the localizer too, so it is not updated twice
            offerSightings();
            return;
        }

        if (localizer != null) {
            localizer.update();
            offerSightings();
        }

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

    /**
     * Each new CELL sighting, once: toward a seed while the pose is unreferenced, a fix after.
     */
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
            if (!poseReferenced) {
                if (LocalizationTuning.seedFromCamera) {
                    considerSeed(CellFix.pose(CELLS[i], state, sighting.rowCentreRobot(),
                            sighting.lateralAxisRobot()));
                }
                continue;
            }
            Pose fix = CellFix.position(CELLS[i], state, sighting.rowCentreRobot(),
                    localizer.pose().heading());
            if (fix == null) {
                continue; // cell mid-tip or unseen long enough, or its field point is unmeasured
            }
            if (LocalizationTuning.continuousFixes) {
                localizer.addMeasurement(fix, sighting.captureTimeNs(),
                        CellFix.variance(sighting.groundRangeIn(), sighting.tagCount()));
            }
            fixCount++;
            fixSumX += fix.x();
            fixSumY += fix.y();
        }
    }

    /** Seeds once {@link LocalizationTuning#seedFrames} camera poses in a row agree. */
    private void considerSeed(Pose candidate) {
        if (candidate == null) {
            return;
        }
        boolean agrees = seedCandidate != null
                && Math.hypot(candidate.x() - seedCandidate.x(), candidate.y() - seedCandidate.y())
                        <= LocalizationTuning.seedAgreementIn
                && Math.toDegrees(Math.abs(Math.IEEEremainder(
                        candidate.heading() - seedCandidate.heading(), 2 * Math.PI)))
                        <= LocalizationTuning.seedAgreementDeg;
        seedAgreeing = agrees ? seedAgreeing + 1 : 1;
        seedCandidate = candidate;
        if (seedAgreeing >= Math.max(1, LocalizationTuning.seedFrames)) {
            setPoseKeepingDriverForward(candidate);
            cameraPoseNote = "seeded " + describePose(candidate);
        }
    }

    @Override
    public void stop() {
        pathControl = false;
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
        Pose pose = localizer.pose();
        String where = String.format(java.util.Locale.US, "(%.1f, %.1f, %.0f°)",
                pose.x(), pose.y(), Math.toDegrees(pose.heading()));
        if (poseReferenced) {
            display.status("Pose", Display.Level.OK, where);
        } else {
            display.status("Pose", Display.Level.WARN,
                    "not field-referenced — no start, handoff or camera seed yet");
        }
        if (!HiveFieldPoints.anyMeasured()) {
            display.line("<small>Camera off: HIVE field points not measured</small>");
            return;
        }
        display.line("<small>Camera pose: " + cameraPoseNote + "</small>");
        display.line("<small>Camera fixes: " + fixCount
                + (LocalizationTuning.continuousFixes ? " (correcting)" : " (counted only)") + "</small>");
    }
}
