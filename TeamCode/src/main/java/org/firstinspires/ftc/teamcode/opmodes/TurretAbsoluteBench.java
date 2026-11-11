package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.localization.StartPosition;
import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.firstinspires.ftc.teamcode.subsystems.TurretAbsolute;
import org.firstinspires.ftc.teamcode.subsystems.TurretEncoder;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.FieldView;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

import java.util.Locale;

/**
 * The turret's angle every way the robot can know it, side by side, and where that points on the
 * field: the bench for the two-Cypher-Max absolute encoders, whichever way they end up wired.
 *
 * <h2>What it shows</h2>
 *
 * <ul>
 *   <li><b>Absolute pair</b>: the two-encoder solve through the OctoQuad, live every loop (the
 *       match does it only in INIT), with its residual and margin.</li>
 *   <li><b>1 · Count</b>: the 64T encoder's quadrature in the feeder's hub socket, if a cable is
 *       there (the split-cable wiring). It stays at 0 with no cable in the socket.</li>
 *   <li><b>2 · Absolute only</b>: the 64T encoder's absolute reading with its wraps counted from
 *       the solve (one absolute cable per encoder, the OctoQuad read during the match).</li>
 *   <li><b>3 · Servo</b>: what the servo is being told (no encoder during the match).</li>
 *   <li><b>OctoQuad read</b>: how long each read takes, last and worst: the cost of wiring 2.</li>
 *   <li><b>On the field</b>: the robot's heading plus the turret's angle is where the turret points
 *       in Pedro's frame; beside it the bearing to our raised CELL and how far the turret is off it.
 *       Panels' field view draws the turret's pointer (yellow) and the line to the CELL (green).</li>
 * </ul>
 *
 * <h2>How to use it</h2>
 *
 * <p>In INIT the turret is limp: turn it by hand, push the robot around, and watch the numbers.
 * Nothing moves before PLAY. D-pad left/right picks which angle the field section uses. After PLAY
 * the turret holds home as it does in a match (or, unlocked, Square aims it at our raised CELL),
 * and every reading keeps updating. The values it needs are {@code TurretSubsystem}'s {@code abs*}
 * fields, measured on {@code Bench: OctoQuad} ({@code doc/robot-todo.md} § 7g).
 */
@Utility(name = "Bench: Turret Absolute",
        description = "The turret's angle from the absolute pair, the count, absolute-only and the servo, and where it points on the field")
public class TurretAbsoluteBench extends RobotOpMode {

    /** Which angle the field section and the drawing use. */
    enum Source {
        ABSOLUTE("absolute only (2)"),
        COUNT("count (1)"),
        SERVO("servo (3)");

        final String label;

        Source(String label) {
            this.label = label;
        }
    }

    private static final double AIM_SWITCH_BAND_IN = 19.75;
    private static final String TURRET_RAY = "#FFEB3B";
    private static final String TARGET_RAY = "#00E676";
    private static final double TURRET_RADIUS_IN = 2.5;
    private static final double TURRET_POINTER_IN = 12.0;

    private TurretAimer aimer;
    private TelemetryManager panels;
    private Source source = Source.ABSOLUTE;
    private boolean aiming;

    @Override
    protected StartPosition startPosition() {
        return hasHandoffPose() ? null : StartPositions.forAlliance(setup.alliance());
    }

    @Override
    protected void onInit() {
        panels = panelsTelemetry();
        aimer = new TurretAimer(robot);
        robot.turret.readAbsoluteLive();

        driver.when("D-pad right", "Field angle from the next source (absolute only, count, servo)", () -> gamepad1.dpad_right)
                .duringInit()
                .onPress(() -> source = Source.values()[(source.ordinal() + 1) % Source.values().length]);
        driver.when("D-pad left", "Field angle from the previous source", () -> gamepad1.dpad_left)
                .duringInit()
                .onPress(() -> source = Source.values()[(source.ordinal() + Source.values().length - 1) % Source.values().length]);
        driver.when("Cross (A)", "OctoQuad: set it up again (after a cable change)", () -> gamepad1.a)
                .duringInit()
                .onPress(robot.turret::retryOctoQuad);
        driver.when("D-pad up", "Turret: zero here, at its home mark", () -> gamepad1.dpad_up)
                .duringInit()
                .onPress(robot.turret::zeroHere);
        driver.when("Square (X)", "Turret: aim at our raised CELL / off (after PLAY, unlocked only)", () -> gamepad1.x)
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
            robot.turret.home();
        }
    }

    @Override
    protected void onInitLoop() {
        show();
    }

    @Override
    protected void onLoop() {
        if (aiming) aimer.update(setup.alliance(), AIM_SWITCH_BAND_IN);
        show();
    }

    // ---------------------------------------------------------------- the page

    private void show() {
        TurretSubsystem turret = robot.turret;
        TurretAbsolute.Solution solve = turret.absoluteSolution();
        double angle = angleFrom(source);

        display.section("Turret angle · " + source.label);
        display.line(Display.huge(Double.isNaN(angle) ? "—" : String.format(Locale.US, "%+.1f°",
                TurretEncoder.wrap180(angle)), Double.isNaN(angle) ? Display.Level.WARN : null));

        if (turret.absoluteFault() != null) {
            display.status("Absolute pair", Display.Level.FAULT, turret.absoluteFault() + " · Cross (A) retries");
        } else if (solve == null) {
            display.status("Absolute pair", Display.Level.WARN, "no reading yet");
        } else if (!solve.ok()) {
            display.status("Absolute pair", Display.Level.WARN, String.format(Locale.US,
                    "%s · pulses A %d B %d µs", solve.problem, turret.absolutePulseA(), turret.absolutePulseB()));
        } else {
            display.status("Absolute pair", Display.Level.OK, String.format(Locale.US,
                    "%+.1f° · residual %.1f° · margin %.0f°", solve.turretDeg, solve.residualDeg, solve.marginDeg));
        }
        double count = turret.countDegrees();
        display.status("1 · Count", turret.zeroed() ? Display.Level.OK : Display.Level.WARN, String.format(Locale.US,
                "%s · %s · vs pair %s", deg(count), turret.zeroSource().text, deg(turret.absoluteVsCountDegrees())));
        double tracked = turret.absoluteTrackedDegrees();
        double trackGap = solve != null && solve.ok() ? TurretEncoder.wrap180(tracked - solve.turretDeg) : Double.NaN;
        display.status("2 · Absolute only", Double.isNaN(tracked) ? Display.Level.WARN : Display.Level.OK,
                String.format(Locale.US, "%s (unwrapped %s) · vs pair %s", deg(TurretEncoder.wrap180(tracked)),
                        deg(tracked), deg(trackGap)));
        double servo = turret.commandedDegrees();
        display.status("3 · Servo", Display.Level.OK, Double.isNaN(servo)
                ? "limp (INIT, or idle): told nothing" : String.format(Locale.US, "told %+.1f°%s", servo,
                TurretSubsystem.lockedForward ? " (locked forward)" : ""));
        display.status("OctoQuad read", Display.Level.OK, String.format(Locale.US,
                "%s ms, worst %s ms · loop %.1f ms", ms(turret.octoQuadReadMs()), ms(turret.octoQuadReadMaxMs()),
                loopTimer.lastMs()));

        showField(angle);
        display.line("<small>Turn the turret by hand in INIT · D-pad ◀ ▶ angle source · Cross (A) OctoQuad setup · "
                + "D-pad up zero here · after PLAY: Square (X) aim, D-pad down home · Share (Back): robot pages</small>");
        publish(angle, tracked, count);
    }

    /** Where the turret points on the field, and how far it is off our raised CELL. */
    private void showField(double angle) {
        display.section("On the field");
        Pose pose = robot.drive.poseReferenced() ? robot.drive.pose() : null;
        if (pose == null) {
            display.status("Pose", Display.Level.WARN, "not on the field: no field direction (start on the marks, or relocalize)");
            return;
        }
        double heading = Math.toDegrees(pose.heading());
        double pointing = TurretEncoder.wrap180(heading + angle);
        display.status("Pose", Display.Level.OK, String.format(Locale.US, "(%.1f, %.1f) in · heading %.1f°",
                pose.x(), pose.y(), heading));
        display.status("Points", Double.isNaN(angle) ? Display.Level.WARN : Display.Level.OK,
                String.format(Locale.US, "%s on the field (heading %+.1f° + turret %s)", deg(pointing), heading,
                        deg(TurretEncoder.wrap180(angle))));
        if (aimer.solve(setup.alliance(), AIM_SWITCH_BAND_IN)) {
            double off = TurretEncoder.wrap180(aimer.aimDeg() - angle);
            display.status("Our CELL", Double.isNaN(off) || Math.abs(off) > 2.0 ? Display.Level.WARN : Display.Level.OK,
                    String.format(Locale.US, "%s on the robot, %.0f in (%s) · turret off by %s",
                            deg(aimer.aimDeg()), aimer.rangeIn(), aimer.source(), deg(off)));
        } else {
            display.status("Our CELL", Display.Level.WARN, aimer.source());
        }
        if (aiming) display.line(aimer.line());
    }

    private double angleFrom(Source which) {
        switch (which) {
            case COUNT:
                return robot.turret.countDegrees();
            case SERVO:
                return robot.turret.commandedDegrees();
            default:
                double tracked = robot.turret.absoluteTrackedDegrees();
                if (!Double.isNaN(tracked)) return tracked;
                TurretAbsolute.Solution solve = robot.turret.absoluteSolution();
                return solve != null && solve.ok() ? solve.turretDeg : Double.NaN;
        }
    }

    // ---------------------------------------------------------------- Panels

    private void publish(double angle, double tracked, double count) {
        if (panels == null) return;
        try {
            TurretAbsolute.Solution solve = robot.turret.absoluteSolution();
            panels.addData("tab_angle_deg", angle);
            panels.addData("tab_pair_deg", solve != null && solve.ok() ? solve.turretDeg : Double.NaN);
            panels.addData("tab_residual_deg", solve == null ? Double.NaN : solve.residualDeg);
            panels.addData("tab_margin_deg", solve == null ? Double.NaN : solve.marginDeg);
            panels.addData("tab_count_deg", count);
            panels.addData("tab_tracked_deg", tracked);
            panels.addData("tab_servo_deg", robot.turret.commandedDegrees());
            panels.addData("tab_pulse_a_us", robot.turret.absolutePulseA());
            panels.addData("tab_pulse_b_us", robot.turret.absolutePulseB());
            panels.addData("tab_octoquad_ms", robot.turret.octoQuadReadMs());
            panels.update();
        } catch (RuntimeException ignored) {
            // Telemetry never takes the robot down.
        }
    }

    /** The turret's pointer on the robot (yellow), and the line to our raised CELL (green). */
    @Override
    protected void drawOnField(FieldView field) {
        Pose pose = robot.drive.pose();
        double angle = angleFrom(source);
        if (pose == null || Double.isNaN(angle)) return;
        double c = Math.cos(pose.heading());
        double s = Math.sin(pose.heading());
        double fwd = TurretSubsystem.turretForwardIn;
        double left = TurretSubsystem.turretLeftIn;
        double axisX = pose.x() + fwd * c - left * s;
        double axisY = pose.y() + fwd * s + left * c;
        field.drawMarker(axisX, axisY, TURRET_RADIUS_IN, TURRET_RAY);
        double pointing = pose.heading() + Math.toRadians(angle);
        field.drawLine(axisX, axisY, axisX + TURRET_POINTER_IN * Math.cos(pointing),
                axisY + TURRET_POINTER_IN * Math.sin(pointing), TURRET_RAY);
        if (aimer.cell() != null && !Double.isNaN(aimer.aimDeg())) {
            double wanted = pose.heading() + Math.toRadians(aimer.aimDeg());
            double length = Double.isNaN(aimer.rangeIn()) ? TURRET_POINTER_IN : aimer.rangeIn();
            field.drawLine(axisX, axisY, axisX + length * Math.cos(wanted), axisY + length * Math.sin(wanted), TARGET_RAY);
            Vec3 row = HiveFieldPoints.rowCentre(aimer.cell(), HiveCellState.UP);
            if (row != null) field.drawMarker(row.x(), row.y(), 1.5, TARGET_RAY);
        }
    }

    private static String deg(double value) {
        return Double.isNaN(value) ? "—" : String.format(Locale.US, "%+.1f°", value);
    }

    private static String ms(double value) {
        return Double.isNaN(value) ? "—" : String.format(Locale.US, "%.2f", value);
    }
}
