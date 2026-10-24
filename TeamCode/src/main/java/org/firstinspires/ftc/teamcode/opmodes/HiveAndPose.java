package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.subsystems.TurretAim;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldView;
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
 *       apart; what the camera last did to the pose (seeded, relocalized, refused); the
 *       would-relocalize error while still.</li>
 * </ul>
 *
 * <h2>Panels</h2>
 * <ul>
 *   <li><b>Field</b>: the robot as odometry has it (blue), as the camera has it (amber), and each
 *       CELL's tag row where it is now (red / blue dots).</li>
 *   <li><b>Graphs</b>: {@code cell_<name>_height_in} and {@code _angle_deg} (a TIP is a clean
 *       curve), {@code hive_red} / {@code hive_blue} (+1 right CELL up, -1 left, 0 tipping),
 *       {@code tips_red} / {@code tips_blue}, {@code pose_*}, {@code camera_*},
 *       {@code camera_gap_in}, {@code would_relocalize_in}.</li>
 * </ul>
 *
 * <h2>Try</h2>
 * <ol>
 *   <li>Start facing the HIVE: the pose seeds from the camera within a second (Pose turns green).</li>
 *   <li>Hold still and tip the HIVE by hand: TRANSITION, then the other CELL up.</li>
 *   <li>Drive, get shoved, stop facing the HIVE: the two robots on the field have drifted apart by
 *       the would-relocalize gap. A brings them back together.</li>
 * </ol>
 */
@TeleOp(name = "Vision: HIVE & Pose", group = "Vision")
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

    /** A camera sighting older than this is not used to aim; odometry takes over. */
    private static final double AIM_SIGHTING_FRESH_MS = 300;
    private boolean aiming;
    private String aimSource = "off";
    private HiveCell aimCell;
    private double aimTargetDeg = Double.NaN;
    private double aimRangeIn = Double.NaN;

    @Override
    protected void onInit() {
        panels = PanelsTelemetry.INSTANCE.getTelemetry();
        field = FieldView.pedroCoordinates();
        for (int i = 0; i < CELLS.length; i++) {
            String name = CELLS[i].name().toLowerCase(Locale.US);
            heightKeys[i] = "cell_" + name + "_height_in";
            angleKeys[i] = "cell_" + name + "_angle_deg";
            stateKeys[i] = "cell_" + name + "_state";
        }

        driver.note("Left stick", "Drive");
        driver.note("Right stick X", "Turn");
        driver.note("Left bumper (hold)", "Slow");
        driver.when("Cross (A)", "Relocalize from the camera", () -> gamepad1.a)
                .onPress(() -> relocalizeNote = robot.drive.relocalizeFromCamera());
        driver.when("Triangle (Y)", "Reset field-centric forward", () -> gamepad1.y)
                .onPress(robot.drive::resetHeading);
        driver.when("Circle (B)", "Toggle field / robot-centric", () -> gamepad1.b)
                .onPress(robot.drive::toggleFieldCentric);
        driver.when("D-pad up", "Turret: zero here (turret at its home mark; also in INIT)", () -> gamepad1.dpad_up)
                .duringInit()
                .onPress(robot.turret::zeroHere);
        driver.when("Square (X)", "Turret: aim at our raised CELL / off", () -> gamepad1.x)
                .onPress(this::toggleAim);
        driver.when("D-pad down", "Turret: home (0°), aim off", () -> gamepad1.dpad_down)
                .onPress(() -> {
                    aiming = false;
                    aimSource = "off";
                    robot.turret.home();
                });
    }

    private void toggleAim() {
        aiming = !aiming;
        if (!aiming) {
            aimSource = "off";
            robot.turret.idle();
        }
    }

    /**
     * Where the turret should point this loop: at our alliance's raised CELL, from the camera when
     * it sees that CELL now, else from odometry and the CELL's measured field point. With neither,
     * the turret holds its last target.
     */
    private void updateAim() {
        if (!aiming) return;
        Alliance alliance = setup.alliance();
        if (alliance == Alliance.UNKNOWN) {
            aimSource = "no alliance chosen";
            return;
        }
        HiveTracker hive = robot.hive.of(alliance);
        HiveCell cell = hive.rightCellUp() ? HiveCell.rightCell(alliance)
                : hive.leftCellUp() ? HiveCell.leftCell(alliance) : null;
        if (cell == null) {
            aimSource = "HIVE state unknown, holding last";
            return;
        }
        aimCell = cell;
        double fwd = TurretSubsystem.turretForwardIn;
        double left = TurretSubsystem.turretLeftIn;
        CellSighting sighting = robot.vision.sighting(cell);
        if (sighting != null && sighting.ageMs() <= AIM_SIGHTING_FRESH_MS) {
            Vec3 row = sighting.rowCentreRobot();
            aimTargetDeg = TurretAim.fromRobotFrameDeg(row, fwd, left);
            aimRangeIn = Math.hypot(row.x() - fwd, row.y() - left);
            aimSource = "camera";
        } else if (robot.drive.poseReferenced()) {
            Pose pose = robot.drive.pose();
            Vec3 row = HiveFieldPoints.rowCentre(cell, HiveCellState.UP);
            if (pose == null || row == null) {
                aimSource = "no field point for " + cellName(cell);
                return;
            }
            aimTargetDeg = TurretAim.fromFieldDeg(pose.x(), pose.y(), pose.heading(), fwd, left, row.x(), row.y());
            aimRangeIn = TurretAim.rangeFromFieldIn(pose.x(), pose.y(), pose.heading(), fwd, left, row.x(), row.y());
            aimSource = "odometry";
        } else {
            aimSource = "no camera on it, pose not on the field";
            return;
        }
        robot.turret.aim(aimTargetDeg);
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
        updateAim();
        showEverything();
        display.line("<small>Cross (A) relocalize · Triangle (Y) reset forward · Circle (B) field/robot-centric · "
                + "Square (X) aim · D-pad up zero turret · D-pad down turret home · "
                + "Share (Back): controls, robot pages</small>");
    }

    /** The Match page, the Panels graphs and the field view, from this loop's state. */
    private void showEverything() {
        Pose camera = robot.drive.cameraPose();
        showHives();
        showCells();
        showPose(camera);
        showTurret();
        try {
            publish(camera);
        } catch (RuntimeException ignored) {
            // A Panels hiccup must never take the robot down.
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
                    String.format(Locale.US, "<b>%s</b> · %.1f in up, %+.0f° · %d tag%s · %.0f in away",
                            state, height, rockerAngle(height), sighting.tagCount(),
                            sighting.tagCount() == 1 ? "" : "s", sighting.groundRangeIn()));
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
        display.line("Camera set the pose: " + robot.drive.cameraPoseNote());
        display.line("Last A: " + relocalizeNote);
        if (robot.drive.wouldRelocalizeCount() > 0) {
            display.line(String.format(Locale.US,
                    "Still and in view: off by %.1f in (worst %.1f in, %d samples)%s",
                    robot.drive.wouldRelocalizeErrorIn(), robot.drive.wouldRelocalizeMaxErrorIn(),
                    robot.drive.wouldRelocalizeCount(), robot.drive.isStill() ? " · still now" : ""));
        }
    }

    private void showTurret() {
        display.section("Turret");
        double angle = robot.turret.angleDegrees();
        if (!robot.turret.zeroed()) {
            display.status("Turret", Display.Level.WARN, String.format(Locale.US,
                    "%.1f° · NOT ZEROED — put it at its home mark, press D-pad up", angle));
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
            display.line("Aim: " + aimSource
                    + (aimCell == null ? "" : " · " + cellName(aimCell) + " CELL")
                    + (Double.isNaN(aimRangeIn) ? "" : String.format(Locale.US, " · %.0f in", aimRangeIn))
                    + (TurretSubsystem.turretForwardIn == 0.0 && TurretSubsystem.turretLeftIn == 0.0
                    ? " · turret offset not measured: aiming from the robot centre"
                    : String.format(Locale.US, " · turret at %+.1f fwd, %+.1f left",
                    TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn)));
        } else {
            display.line("Aim: off (Square)");
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
        panels.addData("turret_angle_deg", robot.turret.angleDegrees());
        panels.addData("turret_target_deg", robot.turret.targetDegrees());
        panels.addData("turret_error_deg", robot.turret.errorDegrees());
        panels.addData("turret_aim_source", !aiming ? 0 : "camera".equals(aimSource) ? 1
                : "odometry".equals(aimSource) ? 2 : 3);
        panels.update();

        if (field.shouldDraw()) {
            drawCells();
            if (pose != null) field.drawRobot(pose.x(), pose.y(), pose.heading());
            if (camera != null) field.drawRobot(camera.x(), camera.y(), camera.heading(), CAMERA_ROBOT);
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
            double length = Double.isNaN(aimRangeIn) ? TURRET_POINTER_IN : aimRangeIn;
            double wanted = pose.heading() + Math.toRadians(target);
            field.drawLine(axisX, axisY, axisX + length * Math.cos(wanted), axisY + length * Math.sin(wanted),
                    TURRET_TARGET_RAY);
        }
        if (aiming && aimCell != null) {
            Vec3 row = HiveFieldPoints.rowCentre(aimCell, HiveCellState.UP);
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
