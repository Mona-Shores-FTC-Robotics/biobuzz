package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.localization.LocalizationTuning;
import org.firstinspires.ftc.teamcode.localization.StartPosition;
import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.firstinspires.ftc.teamcode.localization.TagGate;
import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldView;
import org.firstinspires.ftc.teamcode.util.WelfordVariance;
import org.firstinspires.ftc.teamcode.vision.BiobuzzTags;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.CellStateTracker;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.HiveGeometry;
import org.firstinspires.ftc.teamcode.vision.HiveState;
import org.firstinspires.ftc.teamcode.vision.HiveTracker;
import org.firstinspires.ftc.teamcode.vision.TagTilt;
import org.firstinspires.ftc.teamcode.vision.Vec3;

import java.util.Locale;

/**
 * Everything the camera knows, in one place: drive the robot and watch what it makes of the HIVEs
 * and of where it is. The demo of {@code robot.hive} and camera localization, and the screen for
 * testing both.
 *
 * <h2>Driver Station (Match page)</h2>
 * <ul>
 *   <li><b>HIVEs</b>: which CELL is up on each, TIPs so far, "tipping" or "assumed" when the camera
 *       did not see it happen ({@code robot.hive}).</li>
 *   <li><b>CELLs</b>: each one in view with its state, row height, rocker angle and tag count.</li>
 *   <li><b>Pose</b>: where odometry says the robot is, where the camera says it is (from every
 *       tag of every settled CELL in view, {@code robot.drive.cameraPose()}), and how far
 *       apart; what last set the pose (seeded, relocalized, reset, refused); the
 *       would-relocalize error while still; the stationary fix (averaging, applied, refused and why).</li>
 *   <li><b>Tags</b>: each tag in the latest frame, accepted or why not, its height error and range,
 *       and the robot it implies against odometry, in field and robot axes. On a measured spot that
 *       is the mount-and-field-point measurement (README § "The camera fix: gates, averaging, and how to tune it").</li>
 * </ul>
 *
 * <h2>Panels</h2>
 * <ul>
 *   <li><b>Field</b>: the robot as odometry has it (blue), as the camera has it (amber), and each
 *       CELL's tag row where it is now (red / blue dots).</li>
 *   <li><b>Graphs</b>: {@code cell_<name>_height_in} and {@code _angle_deg} (a TIP is a clean
 *       curve), {@code hive_red} / {@code hive_blue} (+1 right CELL up, -1 left, 0 tipping),
 *       {@code tips_red} / {@code tips_blue}, {@code pose_*}, {@code camera_*},
 *       {@code camera_gap_in}, {@code would_relocalize_in}, {@code tags_accepted} /
 *       {@code tags_rejected}, and {@code still_fix_*}: the averaged position against the
 *       single-frame {@code camera_*}, which is the jitter comparison.</li>
 * </ul>
 *
 * <p><b>The one vision OpMode.</b> Everything the camera needs checking with lives here; to try
 * something new with the camera, add it here rather than a new OpMode. It needs no setup: it
 * starts from the red audience start, its own buttons switch what it tests, and the match log
 * keeps every number it shows, so a session's measurements survive without anyone writing them
 * down.
 *
 * <h2>Try</h2>
 * <ol>
 *   <li>Place the robot on the red audience start ({@link StartPositions#RED_AUDIENCE}) and INIT:
 *       the pose starts there and the start check says whether the camera agrees.</li>
 *   <li>Hold still and tip the HIVE by hand: TRANSITION, then the other CELL up.</li>
 *   <li>Drive, get shoved, stop facing the HIVE: the two robots on the field have drifted apart by
 *       the would-relocalize gap. A brings them back together.</li>
 * </ol>
 */
@Utility(name = "Vision: HIVE & Pose", description = "HIVE state, Pinpoint vs camera pose, relocalize, turret aim; the camera test bed")
@Configurable
public class HiveAndPose extends RobotOpMode {

    private static final double FORWARD_SIGN = -1.0;
    private static final double STRAFE_SIGN = -1.0;
    private static final double TURN_SIGN = -1.0;
    private static final double STICK_DEADBAND = 0.05;

    /** Stick multiplier. Gentle: this is for watching, not racing. */
    public static double speed = 0.5;

    /** Stick multiplier while the left bumper is held. */
    public static double slowSpeed = 0.25;

    private static final String CAMERA_ROBOT = "#FFB300";
    /** The last stationary batch's mean, on the field view. */
    private static final String STILL_FIX = "#00E5FF";
    /** Where the turret points (yellow) and where it should (green), on the field view. */
    private static final String TURRET_RAY = "#FFEB3B";
    private static final String TURRET_TARGET_RAY = "#00E676";
    /** The turret drawn on the robot: a small circle at its axis, and the length of its pointer, inches. */
    private static final double TURRET_RADIUS_IN = 2.5;
    private static final double TURRET_POINTER_IN = 12.0;
    private static final String RED_CELL = "#E53935";
    private static final String BLUE_CELL = "#1E88E5";
    private static final HiveCell[] CELLS = HiveCell.values();
    private static final Alliance[] ALLIANCES = {Alliance.RED, Alliance.BLUE};

    private final String[] heightKeys = new String[CELLS.length];
    private final String[] angleKeys = new String[CELLS.length];
    private final String[] stateKeys = new String[CELLS.length];

    private TelemetryManager panels;
    private FieldView field;
    private String relocalizeNote = "not pressed yet";

    /**
     * Half-width of the band about the HIVE's axle line inside which the aimed CELL does not
     * switch ends, inches. 19.75 is the HIVE frame's half-span, where nobody fires from anyway
     * (the simulator's rule, 9 Oct 2026).
     */
    public static double aimSwitchBandIn = 19.75;
    private boolean aiming;
    private TurretAimer aimer;

    /** How much the camera's position jitters while the robot holds still, x and y, inches. */
    private final WelfordVariance jitterX = new WelfordVariance();
    private final WelfordVariance jitterY = new WelfordVariance();
    /** The camera pose last added to the jitter: a new object means a new camera frame. */
    private Pose lastJitterPose;

    /** Match log keys per HIVE tag, made once: field residual x/y, height error, gate reason. */
    private static final int TAG_COUNT = BiobuzzTags.LAST_TAG_ID - BiobuzzTags.FIRST_TAG_ID + 1;
    private final String[] tagDxKeys = new String[TAG_COUNT];
    private final String[] tagDyKeys = new String[TAG_COUNT];
    private final String[] tagHeightKeys = new String[TAG_COUNT];
    private final String[] tagReasonKeys = new String[TAG_COUNT];

    /** Every session starts here, so the pose is known without an Auto first. */
    @Override
    protected StartPosition startPosition() {
        return StartPositions.RED_AUDIENCE;
    }

    @Override
    protected void onInit() {
        panels = PanelsTelemetry.INSTANCE.getTelemetry();
        field = FieldView.pedroCoordinates();
        aimer = new TurretAimer(robot);
        for (int i = 0; i < CELLS.length; i++) {
            String name = CELLS[i].name().toLowerCase(Locale.US);
            heightKeys[i] = "cell_" + name + "_height_in";
            angleKeys[i] = "cell_" + name + "_angle_deg";
            stateKeys[i] = "cell_" + name + "_state";
        }
        for (int i = 0; i < TAG_COUNT; i++) {
            String tag = "/Vision/Tag" + (BiobuzzTags.FIRST_TAG_ID + i) + "/";
            tagDxKeys[i] = tag + "FieldDxIn";
            tagDyKeys[i] = tag + "FieldDyIn";
            tagHeightKeys[i] = tag + "HeightErrorIn";
            tagReasonKeys[i] = tag + "Reason";
        }

        driver.note("Left stick", "Drive");
        driver.note("Right stick X", "Turn");
        driver.note("L1 (hold)", "Slow");
        driver.when("Cross (A)", "Relocalize from the camera", () -> gamepad1.a)
                .onPress(() -> relocalizeNote = robot.drive.relocalizeFromCamera());
        driver.when("Triangle (Y)", "Reset field-centric forward", () -> gamepad1.y)
                .onPress(robot.drive::resetHeading);
        driver.when("Circle (B)", "Toggle field / robot-centric", () -> gamepad1.b)
                .onPress(robot.drive::toggleFieldCentric);
        driver.when("D-pad left", "Pose: reset to our marked spot (squared to its wall)", () -> gamepad1.dpad_left)
                .onPress(() -> relocalizeNote = robot.drive.resetToSpot(setup.alliance()));
        driver.when("D-pad right", "Stationary camera fixes on / off (this run only; starts off)", () -> gamepad1.dpad_right)
                .onPress(() -> robot.drive.setStillFixes(!robot.drive.stillFixesOn()));
        driver.when("D-pad up", "Turret: zero here, at its home mark (it zeroes itself at PLAY)", () -> gamepad1.dpad_up)
                .duringInit()
                .onPress(robot.turret::zeroHere);
        driver.when("Square (X)", "Turret: aim at our raised CELL / off", () -> gamepad1.x)
                .onPress(this::toggleAim);
        driver.when("D-pad down", "Turret: home (0°), aim off", () -> gamepad1.dpad_down)
                .onPress(() -> {
                    aiming = false;
                    aimer.off();
                    robot.turret.home();
                });
    }

    private void toggleAim() {
        aiming = !aiming;
        if (aiming) {
            aimer.start();
        } else {
            aimer.off();
            robot.turret.idle();
        }
    }

    @Override
    protected void onInitLoop() {
        showEverything();
    }

    @Override
    protected void onLoop() {
        double scale = Math.max(0.0, Math.min(1.0, gamepad1.left_bumper ? slowSpeed : speed));
        robot.drive.drive(
                deadband(gamepad1.left_stick_y) * FORWARD_SIGN * scale,
                deadband(gamepad1.left_stick_x) * STRAFE_SIGN * scale,
                deadband(gamepad1.right_stick_x) * TURN_SIGN * scale);
        if (aiming) aimer.update(setup.alliance(), aimSwitchBandIn);
        showEverything();
        display.line("<small>Cross (A) relocalize · D-pad left reset spot · D-pad right fixes on/off · "
                + "Triangle (Y) reset forward · "
                + "Circle (B) field/robot-centric · Square (X) aim · D-pad up zero turret · D-pad down turret home · "
                + "Share (Back): controls, robot pages</small>");
    }

    /** The Match page, the Panels graphs and the field view, from this loop's state. */
    private void showEverything() {
        Pose camera = robot.drive.cameraPose();
        trackJitter(camera);
        showHives();
        showCells();
        showPose(camera);
        showTags();
        showTurret();
        try {
            publish(camera);
        } catch (RuntimeException ignored) {
            // A Panels hiccup must never take the robot down.
        }
        logTags();
    }

    /**
     * Each new camera frame while the robot holds still, into the jitter statistics; any movement
     * starts them again. The standard deviation is what {@code LocalizationTuning.fixSigmaIn} wants,
     * at this range.
     */
    private void trackJitter(Pose camera) {
        if (!robot.drive.isStill() || !robot.drive.poseReferenced()) {
            jitterX.reset();
            jitterY.reset();
            lastJitterPose = null;
            return;
        }
        if (camera != null && camera != lastJitterPose) {
            jitterX.update(camera.x());
            jitterY.update(camera.y());
            lastJitterPose = camera;
        }
    }

    /** The latest frame's tags into the match log, so a session's residuals can be read off later. */
    private void logTags() {
        TagGate frame = robot.drive.lastFrame();
        Pose pose = robot.drive.pose();
        for (int i = 0; i < frame.count(); i++) {
            int k = frame.tagId(i) - BiobuzzTags.FIRST_TAG_ID;
            if (k < 0 || k >= TAG_COUNT) continue;
            log.put(tagHeightKeys[k], frame.heightErrorIn(i));
            log.put(tagReasonKeys[k], frame.reason(i).ordinal());
            if (pose != null && !Double.isNaN(frame.candidateX(i))) {
                log.put(tagDxKeys[k], frame.candidateX(i) - pose.x());
                log.put(tagDyKeys[k], frame.candidateY(i) - pose.y());
            }
        }
        if (jitterX.n() >= 2) {
            log.put("/Vision/JitterXIn", jitterX.stdDev());
            log.put("/Vision/JitterYIn", jitterY.stdDev());
        }
    }

    // ------------------------------------------------------------ Match page

    private void showHives() {
        display.section("HIVEs");
        showHive(Alliance.RED);
        showHive(Alliance.BLUE);
    }

    private void showHive(Alliance alliance) {
        HiveTracker hive = robot.hive.of(alliance);
        HiveState state = hive.state();
        String which;
        Display.Level level = Display.Level.OK;
        if (state == HiveState.RIGHT_CELL_UP) {
            which = cellName(HiveCell.rightCell(alliance)) + " CELL up";
        } else if (state == HiveState.LEFT_CELL_UP) {
            which = cellName(HiveCell.leftCell(alliance)) + " CELL up";
        } else if (state == HiveState.TRANSITION) {
            which = "TIPPING";
            level = Display.Level.WARN;
        } else {
            which = "not seen";
            level = Display.Level.WARN;
        }
        display.status(alliance.toString(), level, "<b>" + which + "</b>"
                + (hive.assumed() ? " (assumed: TIP not seen to finish)" : "")
                + " · " + hive.tips() + " TIP" + (hive.tips() == 1 ? "" : "s"));
    }

    private void showCells() {
        display.section("CELLs in view");
        boolean any = false;
        for (HiveCell cell : CELLS) {
            CellSighting sighting = robot.vision.sighting(cell);
            if (sighting == null) continue;
            any = true;
            HiveCellState state = robot.vision.state(cell);
            double height = sighting.rowCentreRobot().z();
            display.status(cell.toString(),
                    state == HiveCellState.UNKNOWN ? Display.Level.WARN : Display.Level.OK,
                    String.format(Locale.US, "<b>%s</b> · %.1f in up, %+.0f° · %d tag%s · %.0f in away, %+.0f° (+ left)",
                            state, height, rockerAngle(height), sighting.tagCount(),
                            sighting.tagCount() == 1 ? "" : "s", sighting.groundRangeIn(),
                            Math.toDegrees(sighting.bearingRad())));
        }
        if (!any) {
            display.line(robot.vision.isAvailable()
                    ? "none — point the camera at a HIVE"
                    : "camera unavailable — see the Robot page");
        }
    }

    private void showPose(Pose camera) {
        display.section("Where the robot is");
        Pose pose = robot.drive.pose();
        if (pose == null) {
            display.status("Pose", Display.Level.FAULT, "no Pinpoint — " + robot.drive.localizerFault());
            return;
        }
        display.status("Odometry", robot.drive.poseReferenced() ? Display.Level.OK : Display.Level.WARN,
                describe(pose) + (robot.drive.poseReferenced() ? "" : " — not on the field yet"));
        if (camera == null) {
            display.status("Camera", Display.Level.WARN,
                    "no tag on a settled CELL in view");
        } else {
            double gap = Math.hypot(camera.x() - pose.x(), camera.y() - pose.y());
            display.status("Camera", gap <= 3 ? Display.Level.OK : Display.Level.WARN,
                    describe(camera) + String.format(Locale.US, " · %.1f in from odometry", gap));
        }
        display.line("Pose set by: " + robot.drive.poseNote());
        display.line("Last A / D-pad left: " + relocalizeNote);
        DriveSubsystem.StillFix stillFix = robot.drive.stillFix();
        String batch = Double.isNaN(robot.drive.stillFixSpreadIn()) ? "" : String.format(Locale.US,
                " · last batch spread %.1f in, %.1f in from odometry",
                robot.drive.stillFixSpreadIn(), robot.drive.stillFixJumpIn());
        display.status("Still fix", stillFix == DriveSubsystem.StillFix.TOO_SPREAD
                        || stillFix == DriveSubsystem.StillFix.TOO_FAR ? Display.Level.WARN : Display.Level.OK,
                (stillFix == DriveSubsystem.StillFix.AVERAGING
                        ? "averaging " + robot.drive.stillFixFrames() + "/" + LocalizationTuning.stillFixFrames
                        : stillFix.toString())
                        + batch + (robot.drive.stillFixesOn()
                        ? " · ON, " + robot.drive.stillFixesApplied() + " applied" : " · off (D-pad right)"));
        if (jitterX.n() >= 2) {
            display.line(String.format(Locale.US,
                    "Camera jitter while still: %.2f in x, %.2f in y, over %d frames, %.0f in away",
                    jitterX.stdDev(), jitterY.stdDev(), jitterX.n(), robot.drive.lastFrame().nearestRangeIn()));
        }
        if (robot.drive.wouldRelocalizeCount() > 0) {
            display.line(String.format(Locale.US,
                    "Still and in view: off by %.1f in (worst %.1f in, %d samples)%s",
                    robot.drive.wouldRelocalizeErrorIn(), robot.drive.wouldRelocalizeMaxErrorIn(),
                    robot.drive.wouldRelocalizeCount(), robot.drive.isStill() ? " · still now" : ""));
        }
    }

    /**
     * Each tag in the latest frame: what the gate made of it, and — once the pose is referenced —
     * how far the robot it implies sits from odometry, in the field frame and in the robot's own.
     * Placed on a measured spot, that residual is the session's measurement: one that turns with
     * the robot is the camera mount, one that stays put on the field is that tag's field point.
     */
    private void showTags() {
        TagGate frame = robot.drive.lastFrame();
        display.section("Tags (latest frame)");
        if (robot.vision.isAvailable() && !robot.vision.isConnected()) {
            display.status("Camera", Display.Level.FAULT, "no response from "
                    + robot.vision.connectionInfo() + " — plugged in? powered? IP matches robot_*.xml?");
        } else if (robot.vision.framesMissing3dPose() > 0) {
            display.status("Camera", Display.Level.WARN, robot.vision.framesMissing3dPose()
                    + " tags without a 3D pose: the Limelight pipeline is not set to full 3D");
        }
        if (frame.count() == 0) {
            display.line("none on a settled CELL");
            return;
        }
        display.line(String.format(Locale.US, "%d of %d passed · %s", frame.acceptedCount(), frame.count(),
                frame.verdict()));
        Pose pose = robot.drive.pose();
        for (int i = 0; i < frame.count(); i++) {
            String residual = "";
            if (pose != null && !Double.isNaN(frame.candidateX(i))) {
                double dx = frame.candidateX(i) - pose.x();
                double dy = frame.candidateY(i) - pose.y();
                double c = Math.cos(pose.heading());
                double s = Math.sin(pose.heading());
                residual = String.format(Locale.US, " · field %+.1f, %+.1f · robot fwd %+.1f, left %+.1f",
                        dx, dy, c * dx + s * dy, -s * dx + c * dy);
            }
            display.status("Tag " + frame.tagId(i),
                    frame.reason(i) == TagGate.Reason.ACCEPTED ? Display.Level.OK : Display.Level.WARN,
                    String.format(Locale.US, "%s · height %+.1f · %.0f in", frame.reason(i),
                            frame.heightErrorIn(i), frame.rangeIn(i)) + residual);
        }
    }

    private void showTurret() {
        display.section("Turret");
        double angle = robot.turret.angleDegrees();
        if (!robot.turret.zeroed()) {
            display.status("Turret", Display.Level.WARN, String.format(Locale.US,
                    "%.1f° · not zeroed yet — it zeroes where it stands at PLAY, or press D-pad up", angle));
        } else if (robot.turret.holding()) {
            display.status("Turret", robot.turret.onTarget() ? Display.Level.OK : Display.Level.WARN,
                    String.format(Locale.US, "%.1f° → %.1f° (%+.1f)", angle,
                            robot.turret.targetDegrees(), robot.turret.errorDegrees()));
        } else {
            display.status("Turret", Display.Level.OK, String.format(Locale.US, "%.1f° · idle", angle));
        }
        if (robot.turret.runawayNote() != null) {
            display.status("Turret", Display.Level.FAULT, robot.turret.runawayNote());
        }
        if (aiming) {
            display.line(aimer.line());
        } else {
            display.line("Turret aim: off (Square)");
        }
    }

    // ---------------------------------------------------------------- Panels

    private void publish(Pose camera) {
        for (int i = 0; i < CELLS.length; i++) {
            CellSighting sighting = robot.vision.sighting(CELLS[i]);
            if (sighting == null) continue;
            double height = sighting.rowCentreRobot().z();
            panels.addData(heightKeys[i], height);
            double angle = rockerAngle(height);
            if (!Double.isNaN(angle)) panels.addData(angleKeys[i], angle);
            HiveCellState state = robot.vision.state(CELLS[i]);
            panels.addData(stateKeys[i], state == HiveCellState.UP ? 1 : state == HiveCellState.DOWN ? -1 : 0);
        }
        publishHive("red", robot.hive.of(Alliance.RED));
        publishHive("blue", robot.hive.of(Alliance.BLUE));

        Pose pose = robot.drive.pose();
        if (pose != null) {
            panels.addData("pose_x_in", pose.x());
            panels.addData("pose_y_in", pose.y());
            panels.addData("pose_heading_deg", Math.toDegrees(pose.heading()));
        }
        if (camera != null) {
            panels.addData("camera_x_in", camera.x());
            panels.addData("camera_y_in", camera.y());
            panels.addData("camera_heading_deg", Math.toDegrees(camera.heading()));
            if (pose != null) {
                panels.addData("camera_gap_in", Math.hypot(camera.x() - pose.x(), camera.y() - pose.y()));
            }
        }
        if (robot.drive.wouldRelocalizeCount() > 0) {
            panels.addData("would_relocalize_in", robot.drive.wouldRelocalizeErrorIn());
        }
        TagGate frame = robot.drive.lastFrame();
        panels.addData("tags_accepted", frame.acceptedCount());
        panels.addData("tags_rejected", frame.count() - frame.acceptedCount());
        Pose stillFix = robot.drive.stillFixPose();
        if (stillFix != null) {
            // Against camera_x_in / camera_y_in (one frame each), this is the jitter the averaging removes.
            panels.addData("still_fix_x_in", stillFix.x());
            panels.addData("still_fix_y_in", stillFix.y());
            panels.addData("still_fix_spread_in", robot.drive.stillFixSpreadIn());
            panels.addData("still_fix_jump_in", robot.drive.stillFixJumpIn());
        }
        panels.addData("turret_angle_deg", robot.turret.angleDegrees());
        panels.addData("turret_target_deg", robot.turret.targetDegrees());
        panels.addData("turret_error_deg", robot.turret.errorDegrees());
        panels.addData("turret_aim_source", !aiming ? 0 : "camera".equals(aimer.source()) ? 1
                : "odometry".equals(aimer.source()) ? 2 : 3);
        panels.addData("loop_ms", loopTimer.lastMs());
        panels.addData("cost_onloop_ms", costs.onLoopMs());
        panels.addData("cost_subsystems_ms", costs.subsystemsMs());
        panels.addData("cost_page_ms", costs.pageMs());
        panels.addData("cost_log_ms", costs.logMs());
        panels.update();

        if (field.shouldDraw()) {
            drawCells();
            if (pose != null) field.drawRobot(pose.x(), pose.y(), pose.heading());
            if (camera != null) field.drawRobot(camera.x(), camera.y(), camera.heading(), CAMERA_ROBOT);
            if (stillFix != null) field.drawMarker(stillFix.x(), stillFix.y(), 1.5, STILL_FIX);
            if (pose != null) drawTurret(pose);
            field.send();
        }
    }

    /**
     * The turret on the field: a small yellow circle on the robot at the turret's axis with a
     * pointer the way it faces, a green line from there to the aim target, and a ring on the aimed
     * CELL's field point. If the green line lands on the ring and the real flag does not, the maths
     * is right and the zero or the mounting is off.
     */
    private void drawTurret(Pose pose) {
        double angle = robot.turret.angleDegrees();
        if (Double.isNaN(angle)) return;
        double c = Math.cos(pose.heading());
        double s = Math.sin(pose.heading());
        double fwd = TurretSubsystem.turretForwardIn;
        double left = TurretSubsystem.turretLeftIn;
        double axisX = pose.x() + fwd * c - left * s;
        double axisY = pose.y() + fwd * s + left * c;
        // The turret itself: a small circle on the robot where its axis is, with a pointer the way it faces.
        field.drawMarker(axisX, axisY, TURRET_RADIUS_IN, TURRET_RAY);
        double pointing = pose.heading() + Math.toRadians(angle);
        field.drawLine(axisX, axisY, axisX + TURRET_POINTER_IN * Math.cos(pointing),
                axisY + TURRET_POINTER_IN * Math.sin(pointing), TURRET_RAY);
        // The target: a thin line all the way to the CELL, so a miss is visible against the ring.
        double target = robot.turret.targetDegrees();
        if (!Double.isNaN(target)) {
            double length = Double.isNaN(aimer.rangeIn()) ? TURRET_POINTER_IN : aimer.rangeIn();
            double wanted = pose.heading() + Math.toRadians(target);
            field.drawLine(axisX, axisY, axisX + length * Math.cos(wanted), axisY + length * Math.sin(wanted),
                    TURRET_TARGET_RAY);
        }
        if (aiming && aimer.cell() != null) {
            Vec3 row = HiveFieldPoints.rowCentre(aimer.cell(), HiveCellState.UP);
            if (row != null) field.drawMarker(row.x(), row.y(), 1.5, TURRET_TARGET_RAY);
        }
    }

    private void publishHive(String alliance, HiveTracker hive) {
        HiveState state = hive.state();
        if (state != HiveState.UNSEEN) {
            panels.addData("hive_" + alliance,
                    state == HiveState.RIGHT_CELL_UP ? 1 : state == HiveState.LEFT_CELL_UP ? -1 : 0);
        }
        panels.addData("tips_" + alliance, hive.tips());
    }

    /** Each CELL's tag row where its HIVE says it is now (UP if unknown), as a dot. */
    private void drawCells() {
        for (Alliance alliance : ALLIANCES) {
            HiveState state = robot.hive.of(alliance).state();
            HiveCell right = HiveCell.rightCell(alliance);
            HiveCell left = HiveCell.leftCell(alliance);
            boolean rightUp = state != HiveState.LEFT_CELL_UP;
            drawCell(right, rightUp ? HiveCellState.UP : HiveCellState.DOWN, alliance);
            drawCell(left, rightUp ? HiveCellState.DOWN : HiveCellState.UP, alliance);
        }
    }

    private void drawCell(HiveCell cell, HiveCellState state, Alliance alliance) {
        Vec3 point = HiveFieldPoints.rowCentre(cell, state);
        if (point == null) return;
        field.drawMarker(point.x(), point.y(), state == HiveCellState.UP ? 3.0 : 1.5,
                alliance == Alliance.RED ? RED_CELL : BLUE_CELL);
    }

    // --------------------------------------------------------------- helpers

    private static double rockerAngle(double rowHeightIn) {
        return TagTilt.rockerAngleFromHeightDeg(rowHeightIn, CellStateTracker.Geometry.upRowHeightIn,
                CellStateTracker.Geometry.downRowHeightIn, HiveGeometry.CELL_TILT_DEG);
    }

    private static String cellName(HiveCell cell) {
        return cell == null ? "?" : cell.side().toString();
    }

    private static String describe(Pose pose) {
        return String.format(Locale.US, "(%.1f, %.1f) facing %.0f°",
                pose.x(), pose.y(), Math.toDegrees(pose.heading()));
    }

    private static double deadband(double value) {
        return Math.abs(value) < STICK_DEADBAND ? 0.0 : value;
    }
}
