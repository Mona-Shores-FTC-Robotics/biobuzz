package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.autokit.AutoDrive;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import com.pedropathing.localization.FusionLocalizer;

import org.firstinspires.ftc.teamcode.localization.CellFix;
import org.firstinspires.ftc.teamcode.localization.FixAverager;
import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.localization.LocalizationTuning;
import org.firstinspires.ftc.teamcode.localization.ResetPositions;
import org.firstinspires.ftc.teamcode.localization.SettledStats;
import org.firstinspires.ftc.teamcode.localization.TagGate;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.util.AccelLimiter;
import org.firstinspires.ftc.teamcode.util.Throttle;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.LimelightVisionSubsystem;
import org.firstinspires.ftc.teamcode.vision.Vec3;

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
 * <p>The pose comes from Pedro's {@link FusionLocalizer} over the Pinpoint. Every camera frame goes
 * through {@link TagGate} first: tags off their map height, too far away, or disagreeing with the
 * others are dropped, and a frame needs {@link LocalizationTuning#minFixTags} tags left. Once the
 * pose is referenced the camera gives <b>position only</b>, with the Pinpoint's heading: a heading
 * solved from a 13 in row of tags is the noisy part, and 1° of it is an inch at 60 in.
 * <ul>
 *   <li><b>Seed.</b> While the pose is not {@link #poseReferenced()} (no declared start, no Auto
 *       handoff), two or more gated tags on settled CELLs give the whole pose, heading included
 *       ({@link CellFix#fit}); once {@link LocalizationTuning#seedFrames} frames in a row agree,
 *       the pose is set from it. The driver's field-centric forward does not move.</li>
 *   <li><b>Relocalize.</b> {@link #relocalizeFromCamera()} on demand: x and y only, heading kept,
 *       refused beyond {@link LocalizationTuning#maxRelocalizeJumpIn}.</li>
 *   <li><b>Reset spot.</b> {@link #resetToSpot} puts the robot at its alliance's measured spot,
 *       heading included, for when the pose is lost too far for the camera.</li>
 *   <li><b>Stationary fixes.</b> While still, {@link LocalizationTuning#stillFixFrames} gated frames
 *       are averaged; if they agree and sit near the pose, the mean goes into the filter with
 *       {@link LocalizationTuning#stillFixes} on ({@link StillFix} says how the last batch went).</li>
 *   <li><b>Fixes.</b> Each gated frame is a position fix. They feed the filter only with
 *       {@link LocalizationTuning#continuousFixes} on, which is off until shown to help; they are
 *       always counted, for the start check.</li>
 *   <li><b>Would-relocalize.</b> Whenever the robot has held still for
 *       {@link LocalizationTuning#stillForMs} and a gated frame is in, it records how far the camera
 *       says the pose is off ({@link #wouldRelocalizeCount()} and friends). Nothing is changed.</li>
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
    /**
     * The latest fixes, newest overwriting oldest: the start check averages these, not every fix
     * since the pose was set, so a robot nudged during INIT reads its new spot within a second.
     */
    private static final int RECENT_FIXES = 10;
    private final double[] recentFixX = new double[RECENT_FIXES];
    private final double[] recentFixY = new double[RECENT_FIXES];
    private int recentFixNext;
    private int recentFixFilled;
    /** Camera poses in a row that agreed, toward a seed; and the last of them. */
    private int seedAgreeing;
    private Pose seedCandidate;
    /** What last set the pose (camera, reset spot), for the Robot page. */
    private String poseNote = "not yet";

    /** For "still": the pose last loop, when, and since when the robot has been still. */
    private Pose lastPose;
    private long lastPoseNs;
    private long stillSinceNs = -1;
    private boolean still;
    /** This frame's tags and what the checks made of them. Sized for every HIVE tag. */
    private final TagGate gate = new TagGate(16);
    private long newestCaptureNs;
    /** Gated frames while still, toward one stationary fix; and what the last batch came to. */
    private final FixAverager averager = new FixAverager();
    private StillFix stillFix = StillFix.WAITING;
    private double stillFixSpreadIn = Double.NaN;
    private double stillFixJumpIn = Double.NaN;
    private int stillFixesApplied;
    private Pose stillFixPose;
    /** The camera's view of the placement over the time the robot has sat untouched: see {@link SettledStats}. */
    private final SettledStats settled = new SettledStats();
    /** Whether stationary fixes go into the filter this run: {@link LocalizationTuning#stillFixes} at init. */
    private boolean applyStillFixes = LocalizationTuning.stillFixes;
    private Pose lastCameraPose;
    private long lastCameraPoseNs;

    /** Would-relocalize: samples so far, the latest, and the worst. */
    private int wouldCount;
    private double wouldErrorIn = Double.NaN;
    private double wouldDx = Double.NaN;
    private double wouldDy = Double.NaN;
    private double wouldHeadingErrDeg = Double.NaN;
    private double wouldMaxErrorIn = 0.0;
    private Pose wouldCameraPose;
    private final String localizerFault;

    private final AccelLimiter forwardLimiter = new AccelLimiter();
    private final AccelLimiter strafeLimiter = new AccelLimiter();
    private final AccelLimiter turnLimiter = new AccelLimiter();

    private double forward;
    private double strafe;
    private double turn;
    /** This command is in the robot's own frame, whatever {@link #fieldCentric} says. */
    private boolean robotCentricCommand;
    private boolean accelLimited = true;

    /** Built by {@link #preparePathFollowing()}; null until then. */
    private Follower follower;
    /** The path last given to {@link #follow}, for progress. */
    private Path followedPath;
    /** True while a path or hold, not the sticks, drives the robot. */
    private boolean pathControl;
    /** The Pinpoint the localizer reads, for its status only; null without one. */
    private final GoBildaPinpointDriver pinpoint;
    /** What the wheels were last told by the sticks or a routine, after the ramp; NaN under a path. */
    private double appliedForward = Double.NaN;
    private double appliedStrafe = Double.NaN;
    private double appliedTurn = Double.NaN;
    /**
     * The four drive wheels' encoder speeds, ticks/s, front left, front right, back left, back
     * right, for the match log. Wheels turning while the dead-wheel pods read still is the pods
     * doing their job (the robot pushing on something and not moving), not a fault: these mark
     * when the robot was driven into something, which is when a pod could lift. A pod off the
     * floor misses motion that did happen, so only an outside reference (the camera's gap,
     * {@code /Drift/*}) shows it. Read ten times a second, since the first read after the bulk-cache
     * clear costs the Control Hub's bulk read (about 1 ms), which nothing else in the loop needs.
     */
    private final DcMotorEx[] wheels;
    private final double[] wheelTicksPerSec = {Double.NaN, Double.NaN, Double.NaN, Double.NaN};
    private final Throttle wheelRead = new Throttle(100, 50);
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
        // The same motor objects Pedro's drivetrain drives; only their encoders are read here.
        wheels = new DcMotorEx[] {
                hardwareMap.get(DcMotorEx.class, DeviceNames.FRONT_LEFT),
                hardwareMap.get(DcMotorEx.class, DeviceNames.FRONT_RIGHT),
                hardwareMap.get(DcMotorEx.class, DeviceNames.BACK_LEFT),
                hardwareMap.get(DcMotorEx.class, DeviceNames.BACK_RIGHT)};
        // The same device the localizer reads; its status comes with the localizer's own read.
        pinpoint = found != null ? hardwareMap.get(GoBildaPinpointDriver.class, DeviceNames.PINPOINT) : null;
    }

    // ------------------------------------------------------------- commands

    /**
     * What to do this loop, in Pedro's robot frame: +forward, +strafe is left, +turn is
     * counter-clockwise, each in [-1, 1]. Holds until the next call.
     */
    public void drive(double forward, double strafe, double turn) {
        pathControl = false;
        robotCentricCommand = false;
        this.forward = forward;
        this.strafe = strafe;
        this.turn = turn;
    }

    /**
     * As {@link #drive}, but always in the robot's own frame (+forward is the way it faces), even
     * while the driver is field-centric. For a sequence that drives the robot into something it
     * faces, such as a FLOWER. The next {@link #drive} call goes back to the driver's mode.
     */
    public void driveRobotCentric(double forward, double strafe, double turn) {
        drive(forward, strafe, turn);
        robotCentricCommand = true;
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
     * field-referenced (handed over from Autonomous, or seeded by the camera) and the driver's
     * forward for the alliance is known.
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
            recentFixNext = 0;
            recentFixFilled = 0;
            settled.reset();
            averager.reset();
        }
    }

    /**
     * Sets the pose from the camera now. Once the pose is field-referenced, only x and y move: the
     * gated tags ({@link TagGate}) give the position with the Pinpoint's heading, which is kept;
     * averaged over the frames since the robot stopped, if it is still. Before that, two or more
     * gated tags give the whole pose ({@link CellFix#fit}). Refused if referenced and this would move
     * the robot more than {@link LocalizationTuning#maxRelocalizeJumpIn}. The driver's field-centric
     * forward stays where it was. For a button; best done holding still.
     *
     * @return what happened, for the driver
     */
    public String relocalizeFromCamera() {
        if (localizer == null) {
            return poseNote = "relocalize: no Pinpoint";
        }
        gatherTags(false);
        if (!poseReferenced) {
            gate.solve(Double.NaN);
            Pose best = fitAccepted();
            if (best == null) {
                return poseNote = "relocalize: need 2+ tags on settled CELLs in view";
            }
            setPoseKeepingDriverForward(best);
            return poseNote = "relocalized " + describePose(best) + " from " + gate.acceptedCount() + " tags";
        }
        Pose now = localizer.pose();
        TagGate.Verdict verdict = gate.solve(now.heading());
        double x;
        double y;
        String from;
        if (still && averager.frames() >= 2) {
            x = averager.x();
            y = averager.y();
            from = averager.frames() + " still frames";
        } else if (verdict == TagGate.Verdict.OK) {
            x = gate.x();
            y = gate.y();
            from = gate.acceptedCount() + " tags";
        } else {
            return poseNote = "relocalize refused: " + describe(verdict);
        }
        double jumpIn = Math.hypot(x - now.x(), y - now.y());
        if (jumpIn > LocalizationTuning.maxRelocalizeJumpIn) {
            return poseNote = String.format(java.util.Locale.US,
                    "relocalize refused: %.1f in from the current pose", jumpIn);
        }
        Pose moved = new Pose(x, y, now.heading());
        setPoseKeepingDriverForward(moved);
        return poseNote = "relocalized " + describePose(moved) + " from " + from + ", heading kept";
    }

    /**
     * Puts the robot at {@code alliance}'s marked reset spot in {@code corner} ({@link ResetPositions}),
     * heading included: the drive team has squared it into the corner there. The driver's field-centric
     * forward stays where it was. For a button, when the pose is lost.
     *
     * @return what happened, for the driver
     */
    public String resetToSpot(Alliance alliance, ResetPositions.Corner corner) {
        if (localizer == null) {
            return poseNote = "reset: no Pinpoint";
        }
        String where = alliance + "'s " + corner.toString().toLowerCase(java.util.Locale.US) + " corner";
        Pose spot = ResetPositions.of(alliance, corner);
        if (spot == null) {
            return poseNote = alliance == Alliance.UNKNOWN ? "reset: no alliance chosen"
                    : "reset: " + where + " is not measured yet (ResetPositions)";
        }
        setPoseKeepingDriverForward(spot);
        return poseNote = "reset to " + where + " " + describePose(spot);
    }

    private static String describe(TagGate.Verdict verdict) {
        switch (verdict) {
            case NO_TAGS: return "no tag on a settled CELL in view";
            case TOO_FEW_TAGS: return "fewer than " + LocalizationTuning.minFixTags + " tags passed the checks";
            case OUTSIDE_FIELD: return "the tags put the robot outside the field";
            default: return verdict.toString();
        }
    }

    /** {@link CellFix#fit} over the tags the gate accepted; null with too few. */
    private Pose fitAccepted() {
        return CellFix.fit(gate.acceptedFieldX(), gate.acceptedFieldY(), gate.acceptedRobotX(),
                gate.acceptedRobotY(), gate.acceptedCount(), LocalizationTuning.minTagSpreadIn);
    }

    /**
     * Fills the gate from the CELLs in view that are settled UP or DOWN: each tag's field position
     * against where the robot sees it. {@code freshOnly} takes only sightings not used before (once
     * per camera frame) and marks them used; with none new it leaves the last frame in place, so a
     * loop between camera frames does not blank it, and returns false.
     */
    private boolean gatherTags(boolean freshOnly) {
        if (vision == null || !vision.isAvailable()) {
            gate.clear();
            return false;
        }
        if (freshOnly && !anyFreshSighting()) {
            if (!anySighting()) gate.clear(); // nothing in view any more: no stale frame on show
            return false;
        }
        gate.clear();
        newestCaptureNs = 0;
        for (int i = 0; i < CELLS.length; i++) {
            CellSighting sighting = vision.sighting(CELLS[i]);
            if (sighting == null) continue;
            if (freshOnly) {
                if (sighting.captureTimeNs() <= consumedNs[i]) continue;
                consumedNs[i] = sighting.captureTimeNs();
            }
            HiveCellState state = vision.state(CELLS[i]);
            boolean used = false;
            java.util.List<CellSighting.Member> members = sighting.members();
            for (int m = 0; m < members.size(); m++) { // index loop: no iterator per loop
                CellSighting.Member member = members.get(m);
                // Every settled row has a CAD field point (HiveFieldPoints); an unsettled CELL gives none.
                Vec3 field = HiveFieldPoints.tagPositionForFix(member.tagId, state);
                if (field == null) continue;
                gate.add(member.tagId, field.x(), field.y(), field.z(),
                        member.positionRobot.x(), member.positionRobot.y(), member.positionRobot.z());
                used = true;
            }
            if (used) {
                newestCaptureNs = Math.max(newestCaptureNs, sighting.captureTimeNs());
            }
        }
        return true;
    }

    private boolean anySighting() {
        for (int i = 0; i < CELLS.length; i++) {
            if (vision.sighting(CELLS[i]) != null) return true;
        }
        return false;
    }

    private boolean anyFreshSighting() {
        for (int i = 0; i < CELLS.length; i++) {
            CellSighting sighting = vision.sighting(CELLS[i]);
            if (sighting != null && sighting.captureTimeNs() > consumedNs[i]) return true;
        }
        return false;
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

    /**
     * Average x of the latest few fixes (up to ten), inches — where the camera says the robot is
     * now. For the start check. NaN with none.
     */
    public double meanFixX() {
        return recentMean(recentFixX);
    }

    /** Average y of the latest few fixes, inches. */
    public double meanFixY() {
        return recentMean(recentFixY);
    }

    private double recentMean(double[] values) {
        if (recentFixFilled == 0) return Double.NaN;
        double sum = 0;
        for (int i = 0; i < recentFixFilled; i++) sum += values[i];
        return sum / recentFixFilled;
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

    // Read-only, for the Auto's settle check and its path-end log (opmodes/auto/PathEndSettle).
    // Pedro's own numbers, so the log can say which end condition ended a path.

    /** Speed over the ground, inches/s, from Pedro's Follower; NaN without one. */
    public double speedInPerSec() {
        return follower == null ? Double.NaN : follower.velocity().linSpeed();
    }

    // Read-only, for the match log's drift signals (RobotOpMode, localization/ImpactWatch).

    /** The drive's last command after the ramp, before field-centric turns it; NaN under a path. */
    public double appliedForward() { return appliedForward; }
    public double appliedStrafe() { return appliedStrafe; }
    public double appliedTurn() { return appliedTurn; }

    /** Drive wheel {@code i}'s encoder speed, ticks/s (0 FL, 1 FR, 2 BL, 3 BR), as of the last read. */
    public double wheelTicksPerSec(int i) { return wheelTicksPerSec[i]; }

    /** Whether the sticks' command was field-centric (then {@link #appliedForward} is field "away"). */
    public boolean commandFieldCentric() {
        return fieldCentric && !robotCentricCommand;
    }

    /** Speed over the ground from the localizer, inches/s; NaN without one. Works without a Follower. */
    public double odometrySpeedInPerSec() {
        return localizer == null ? Double.NaN : localizer.velocity().linSpeed();
    }

    /** The Pinpoint's status as of the localizer's last read (READY when well), or null without one. */
    public GoBildaPinpointDriver.DeviceStatus pinpointStatus() {
        return pinpoint == null ? null : pinpoint.getDeviceStatus();
    }

    /** When the camera last gave a gated pose ({@link #cameraPose()}'s source), System.nanoTime; 0 never. */
    public long cameraPoseNs() { return lastCameraPoseNs; }

    /** The camera's last gated pose, however old; null before the first. See {@link #cameraPoseNs()}. */
    public Pose lastCameraPose() { return lastCameraPose; }

    /** Turn rate, radians/s, either sign, from Pedro's Follower; NaN without one. */
    public double turnRadPerSec() {
        return follower == null ? Double.NaN : follower.velocity().omega;
    }

    /** The Follower's mode (FOLLOW, HOLD, MANUAL, IDLE), or NONE before one is built. */
    public String followerMode() {
        return follower == null ? "NONE" : follower.mode().name();
    }

    /** Pedro's {@code atParametricEnd()}; false without a Follower. */
    public boolean atParametricEnd() {
        return follower != null && follower.atParametricEnd();
    }

    /** Pedro's {@code distanceToEndpoint()}, inches; NaN without a Follower. */
    public double distanceToEndpoint() {
        return follower == null ? Double.NaN : follower.distanceToEndpoint();
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
        if (wheelRead.due(now)) {
            for (int i = 0; i < wheels.length; i++) wheelTicksPerSec[i] = wheels[i].getVelocity();
        }

        if (pathControl && follower != null) {
            follower.update(dt); // updates the localizer too, so it is not updated twice
            offerSightings();
            appliedForward = Double.NaN;
            appliedStrafe = Double.NaN;
            appliedTurn = Double.NaN;
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

        appliedForward = f;
        appliedStrafe = s;
        appliedTurn = t;
        DrivePowers powers = fieldCentric && !robotCentricCommand
                ? ManualDrive.fieldCentric(f, s, t, heading())
                : new DrivePowers(f, s, t);

        // manual = true selects BRAKE over FLOAT when the command is zero, if the drivetrain
        // config asks for it: under a driver, the robot stops where it is put.
        drivetrain.drive(powers, true);
    }

    /**
     * Each new CELL sighting, once, through the gate: toward a seed while the pose is unreferenced,
     * a fix after, and toward a stationary fix while the robot holds still.
     */
    private void offerSightings() {
        still = trackStillness();
        if (!still) {
            averager.reset();
            settled.reset();
            if (stillFix == StillFix.AVERAGING) stillFix = StillFix.WAITING;
        }
        if (!gatherTags(true) || gate.count() == 0) {
            return;
        }
        if (!poseReferenced) {
            gate.solve(Double.NaN);
            Pose full = fitAccepted();
            if (full != null) {
                lastCameraPose = full;
                lastCameraPoseNs = System.nanoTime();
                if (LocalizationTuning.seedFromCamera) considerSeed(full);
            }
            return;
        }
        double heading = localizer.pose().heading();
        if (gate.solve(heading) != TagGate.Verdict.OK) {
            return;
        }
        // Position only: heading NaN, so the filter leaves the Pinpoint's heading alone.
        Pose fix = new Pose(gate.x(), gate.y(), Double.NaN);
        lastCameraPose = new Pose(gate.x(), gate.y(), heading);
        lastCameraPoseNs = System.nanoTime();
        if (still) {
            Pose full = fitAccepted();
            recordWouldRelocalize(fix, full);
            Pose now = localizer.pose();
            settled.add(gate, now.x(), now.y(), heading, full == null ? Double.NaN : full.heading(),
                    lastCameraPoseNs);
            offerStillFix(heading);
        }
        if (LocalizationTuning.continuousFixes) {
            localizer.addMeasurement(fix, newestCaptureNs,
                    CellFix.variance(gate.nearestRangeIn(), gate.acceptedCount()));
        }
        fixCount++;
        recentFixX[recentFixNext] = fix.x();
        recentFixY[recentFixNext] = fix.y();
        recentFixNext = (recentFixNext + 1) % RECENT_FIXES;
        recentFixFilled = Math.min(recentFixFilled + 1, RECENT_FIXES);
    }

    /**
     * Adds this gated frame toward a stationary fix; once {@link LocalizationTuning#stillFixFrames}
     * agree to {@link LocalizationTuning#maxFixSpreadIn} and sit within
     * {@link LocalizationTuning#maxFixJumpIn} of the pose, their mean goes into the filter (with
     * {@link LocalizationTuning#stillFixes} on). Then a new batch starts.
     *
     * <p>The fix keeps a single frame's variance rather than dividing it by the frame count:
     * averaging removes jitter, not a mount or field-point error, which every frame shares.
     */
    private void offerStillFix(double heading) {
        averager.add(gate.x(), gate.y(), gate.acceptedCount(), gate.nearestRangeIn(), newestCaptureNs);
        if (!averager.ready()) {
            stillFix = StillFix.AVERAGING;
            return;
        }
        Pose now = localizer.pose();
        double x = averager.x();
        double y = averager.y();
        stillFixSpreadIn = averager.spreadIn();
        stillFixJumpIn = Math.hypot(x - now.x(), y - now.y());
        stillFixPose = new Pose(x, y, heading);
        if (stillFixSpreadIn > LocalizationTuning.maxFixSpreadIn) {
            stillFix = StillFix.TOO_SPREAD;
        } else if (stillFixJumpIn > LocalizationTuning.maxFixJumpIn) {
            stillFix = StillFix.TOO_FAR;
        } else if (applyStillFixes) {
            localizer.addMeasurement(new Pose(x, y, Double.NaN), averager.newestCaptureNs(),
                    CellFix.variance(averager.nearestRangeIn(), averager.minTags()));
            stillFixesApplied++;
            stillFix = StillFix.APPLIED;
        } else {
            stillFix = StillFix.READY;
        }
        averager.reset();
    }

    /**
     * Where the camera put the robot in the last half second, from the gated tags of the settled
     * CELLs in view: position with the Pinpoint's heading once the pose is referenced, the whole
     * fitted pose before. Null if nothing recent. For showing; it changes nothing.
     */
    public Pose cameraPose() {
        return lastCameraPose != null && System.nanoTime() - lastCameraPoseNs < 500_000_000L
                ? lastCameraPose : null;
    }

    /** Whether the robot has been still for {@link LocalizationTuning#stillForMs}; updates the tracking. */
    private boolean trackStillness() {
        Pose pose = localizer.pose();
        long now = System.nanoTime();
        boolean stillThisLoop = false;
        if (lastPose != null && now > lastPoseNs) {
            double dt = (now - lastPoseNs) / 1e9;
            double speed = Math.hypot(pose.x() - lastPose.x(), pose.y() - lastPose.y()) / dt;
            double turn = Math.toDegrees(Math.abs(
                    Math.IEEEremainder(pose.heading() - lastPose.heading(), 2 * Math.PI))) / dt;
            stillThisLoop = speed <= LocalizationTuning.stillSpeedInPerSec
                    && turn <= LocalizationTuning.stillTurnDegPerSec;
        }
        lastPose = pose;
        lastPoseNs = now;
        if (!stillThisLoop) {
            stillSinceNs = -1;
            return false;
        }
        if (stillSinceNs < 0) stillSinceNs = now;
        return (now - stillSinceNs) / 1e6 >= LocalizationTuning.stillForMs;
    }

    /**
     * Records where a stationary relocalize would move the robot: position from the camera with the
     * Pinpoint's heading ({@code fix}), and the camera's own heading from the row ({@code full},
     * null if this CELL's row direction is unknown). Changes nothing.
     */
    private void recordWouldRelocalize(Pose fix, Pose full) {
        Pose now = localizer.pose();
        wouldDx = fix.x() - now.x();
        wouldDy = fix.y() - now.y();
        wouldErrorIn = Math.hypot(wouldDx, wouldDy);
        wouldHeadingErrDeg = full == null ? Double.NaN : Math.toDegrees(
                Math.IEEEremainder(full.heading() - now.heading(), 2 * Math.PI));
        wouldCameraPose = new Pose(fix.x(), fix.y(), full == null ? now.heading() : full.heading());
        wouldMaxErrorIn = Math.max(wouldMaxErrorIn, wouldErrorIn);
        wouldCount++;
    }

    /** What last set the pose: "not yet", "seeded (…)", "relocalized (…)", "reset to …", "refused …". */
    public String poseNote() { return poseNote; }

    /** Where the stationary fix stands: see {@link StillFix}. */
    public enum StillFix {
        /** Not still, or no gated frame yet. */
        WAITING,
        /** Still, collecting frames. */
        AVERAGING,
        /** The last batch disagreed with itself by more than {@link LocalizationTuning#maxFixSpreadIn}. */
        TOO_SPREAD,
        /** The last batch was further than {@link LocalizationTuning#maxFixJumpIn} from the pose. */
        TOO_FAR,
        /** The last batch passed, but stationary fixes are off ({@link #stillFixesOn}). */
        READY,
        /** The last batch went into the filter. */
        APPLIED
    }

    public StillFix stillFix() { return stillFix; }

    /**
     * Turns stationary fixes on or off for this run only, for a test OpMode's button. The next
     * OpMode starts from {@link LocalizationTuning#stillFixes} again.
     */
    public void setStillFixes(boolean on) { applyStillFixes = on; }

    /** Whether stationary fixes go into the filter this run. */
    public boolean stillFixesOn() { return applyStillFixes; }

    /** Frames in the batch being averaged. */
    public int stillFixFrames() { return averager.frames(); }

    /** The last batch: how far its frames spread, and how far its mean sat from the pose, inches. */
    public double stillFixSpreadIn() { return stillFixSpreadIn; }
    public double stillFixJumpIn() { return stillFixJumpIn; }

    /** The last batch's mean, with the Pinpoint's heading; null before the first. */
    public Pose stillFixPose() { return stillFixPose; }

    /** Stationary fixes that went into the filter since init. */
    public int stillFixesApplied() { return stillFixesApplied; }

    /**
     * The latest camera frame's tags and what the checks made of each, for a diagnostic screen.
     * Read only: the drive refills it every frame.
     */
    public TagGate lastFrame() { return gate; }

    /** The camera's view of the placement since the robot last moved. Read only. */
    public SettledStats settled() { return settled; }

    /** Whether the robot has been still long enough for a would-relocalize sample. */
    public boolean isStill() { return still; }

    /** Stationary sightings recorded as would-relocalize samples since init; one per camera frame. */
    public int wouldRelocalizeCount() { return wouldCount; }

    /** Latest: how far the camera says the pose is off, inches; NaN before the first. */
    public double wouldRelocalizeErrorIn() { return wouldErrorIn; }

    /** Latest: camera minus pose, field x and y, inches. */
    public double wouldRelocalizeDx() { return wouldDx; }
    public double wouldRelocalizeDy() { return wouldDy; }

    /** Latest: camera heading minus pose heading, degrees; NaN if that CELL gives no heading. */
    public double wouldRelocalizeHeadingErrDeg() { return wouldHeadingErrDeg; }

    /** The worst of them so far, inches. */
    public double wouldRelocalizeMaxErrorIn() { return wouldMaxErrorIn; }

    /** Latest: where the camera put the robot. Null before the first. */
    public Pose wouldRelocalizePose() { return wouldCameraPose; }

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
            poseNote = "seeded " + describePose(candidate);
        }
    }

    @Override
    public void stop() {
        pathControl = false;
        drivetrain.stop();
    }

    @Override
    public String health() {
        return localizer != null ? "Pinpoint present (" + pinpointIdentity()
                + "); drive motors configured, presence unknown until they move"
                : "Pinpoint missing (" + localizerFault + "): driving robot-centric, no paths";
    }

    /**
     * Which Pinpoint this is, as the board reports it: its device ID and firmware version, which
     * tell a first Pinpoint from a V2 (whose IMU also measures tilt). Two register reads over I2C,
     * so only at INIT, through {@link #health()}.
     */
    private String pinpointIdentity() {
        try {
            return "device ID " + pinpoint.getDeviceID() + ", version " + pinpoint.getDeviceVersion();
        } catch (RuntimeException e) {
            return "identity unread: " + e;
        }
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
        display.line("<small>Pose set by: " + poseNote + "</small>");
        if (wouldCount > 0) {
            display.line(String.format(java.util.Locale.US,
                    "<small>Would relocalize: %.1f in, %s (worst %.1f in, %d samples)</small>",
                    wouldErrorIn,
                    Double.isNaN(wouldHeadingErrDeg) ? "heading n/a"
                            : String.format(java.util.Locale.US, "%+.1f°", wouldHeadingErrDeg),
                    wouldMaxErrorIn, wouldCount));
        }
        display.line("<small>Camera fixes: " + fixCount
                + (LocalizationTuning.continuousFixes ? " (correcting)" : " (counted only)")
                + " · still fixes: " + stillFix
                + (applyStillFixes ? ", " + stillFixesApplied + " applied" : " (off)") + "</small>");
    }
}
