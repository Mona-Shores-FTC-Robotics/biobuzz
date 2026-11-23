package org.firstinspires.ftc.teamcode.controls;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.localization.LaunchSpots;
import org.firstinspires.ftc.teamcode.opmodes.LaunchSpotDrive;
import org.firstinspires.ftc.teamcode.opmodes.TeleOpConfig;
import org.firstinspires.ftc.teamcode.opmodes.auto.LaunchCards;
import org.firstinspires.ftc.teamcode.opmodes.auto.PathEndSettle;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;

import java.util.Locale;

/**
 * The driver's Square (go to launch spot) as data, for {@link ControlsExport}: the twelve
 * {@link LaunchSpots}, each as drawn and where {@link LaunchSpotDrive} drives for each alliance with
 * its starting Panels numbers, the numbers the pick is made with, and the pick itself
 * ({@link LaunchSpots#choose}) from a grid of poses, so the Visualizer's driver drill can check its
 * port of the pick against this one; and the numbers its volley waits on ({@code SpotVolley}:
 * {@link PathEndSettle}'s settle and approach checks, {@link LaunchCards#maxSpinUpWaitS}). Shared values only, as the rest of the file: the turret
 * offsets are {@link TurretSubsystem}'s shared ones.
 */
final class LaunchSpotsExport {

    private LaunchSpotsExport() {
    }

    /** The grid the pick is exported from, for red (blue's is turned half a turn): inches. */
    static final double[] GRID_X = {12, 36, 60, 84, 96};
    static final double[] GRID_Y = {10, 30, 60, 82, 112, 132};

    /** The "launchSpots" object, its key indented {@code depth} levels, without a trailing comma. */
    static String json(int depth) {
        StringBuilder out = new StringBuilder();
        String in = indent(depth), in1 = indent(depth + 1), in2 = indent(depth + 2);
        out.append(in).append("\"launchSpots\": {\n");
        out.append(in1).append("\"about\": ").append(ControlsExport.quote("The driver's Square (LaunchSpots,"
                + " LaunchSpotDrive): Pedro's frame, inches from the red audience corner, headings in degrees."
                + " drawn: as drawn, for red; red and blue: where the button drives, with the numbers below."
                + " examples: LaunchSpots.choose from each pose (null with a refusal).")).append(",\n");
        String[][] numbers = {
                {"tileIn", num(FieldFrame.TILE_INCHES)},
                {"fieldCentreIn", num(FieldFrame.FIELD_CENTRE_INCHES)},
                {"axleYIn", num(HiveFieldPoints.AXLE_Y_IN)},
                {"maxPastCentreIn", num(LaunchSpotDrive.maxPastCentreIn)},
                {"wallCentreBiasIn", num(LaunchSpotDrive.wallCentreBiasIn)},
                {"closeCentreBiasIn", num(LaunchSpotDrive.closeCentreBiasIn)},
                {"closeBackIn", num(LaunchSpotDrive.closeBackIn)},
                {"wallGapIn", num(LaunchSpotDrive.wallGapIn)},
                {"speed", num(LaunchSpotDrive.speed)},
                {"timeoutMs", num(LaunchSpotDrive.timeoutMs)},
                {"turretForwardIn", num(TurretSubsystem.turretForwardIn)},
                {"turretLeftIn", num(TurretSubsystem.turretLeftIn)},
                {"gateOnApproach", String.valueOf(TeleOpConfig.squareGateOnApproach)},
                {"maxSpinUpWaitS", num(LaunchCards.maxSpinUpWaitS)},
                {"maxSettleWaitS", num(PathEndSettle.maxSettleWaitS)},
                {"settleAimToleranceDeg", num(PathEndSettle.aimToleranceDeg)},
                {"settleSpeedInPerS", num(PathEndSettle.slowSpeedInPerSec)},
                {"settleTurnDegPerS", num(PathEndSettle.stillTurnDegPerSec)},
                {"approachWithinIn", num(PathEndSettle.launchWithinIn)},
                {"approachTurnDegPerS", num(PathEndSettle.launchTurnDegPerSec)},
        };
        for (String[] n : numbers) out.append(in1).append(ControlsExport.quote(n[0])).append(": ").append(n[1]).append(",\n");
        out.append(in1).append("\"spots\": [\n");
        LaunchSpots.Spot[] spots = LaunchSpots.Spot.values();
        for (int i = 0; i < spots.length; i++) {
            LaunchSpots.Spot s = spots[i];
            out.append(in2).append("{\"name\": ").append(ControlsExport.quote(s.name()))
                    .append(", \"row\": ").append(ControlsExport.quote(s.row.name()))
                    .append(", \"column\": ").append(ControlsExport.quote(s.column.name()))
                    .append(", \"left\": ").append(s.left)
                    .append(", \"drawn\": ").append(pose(s.red))
                    .append(", \"red\": ").append(pose(target(s, Alliance.RED)))
                    .append(", \"blue\": ").append(pose(target(s, Alliance.BLUE))).append('}')
                    .append(i + 1 < spots.length ? ",\n" : "\n");
        }
        out.append(in1).append("],\n");
        out.append(in1).append("\"examples\": [\n");
        StringBuilder rows = new StringBuilder();
        double c = FieldFrame.FIELD_CENTRE_INCHES;
        for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
            for (double gx : GRID_X) {
                for (double gy : GRID_Y) {
                    boolean blue = alliance == Alliance.BLUE;
                    Pose p = new Pose(blue ? 2 * c - gx : gx, blue ? 2 * c - gy : gy, 0.0);
                    LaunchSpots.Choice choice = choose(p, alliance);
                    if (rows.length() > 0) rows.append(",\n");
                    rows.append(in2).append("{\"alliance\": ").append(ControlsExport.quote(alliance.name()))
                            .append(", \"x\": ").append(num(p.x())).append(", \"y\": ").append(num(p.y()))
                            .append(", \"spot\": ").append(choice.refused() ? "null" : ControlsExport.quote(choice.spot.name()))
                            .append('}');
                }
            }
        }
        out.append(rows).append('\n').append(in1).append("]\n");
        out.append(in).append('}');
        return out.toString();
    }

    private static Pose target(LaunchSpots.Spot s, Alliance alliance) {
        return s.target(alliance, LaunchSpotDrive.wallGapIn, LaunchSpotDrive.closeBackIn,
                TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn);
    }

    private static LaunchSpots.Choice choose(Pose p, Alliance alliance) {
        return LaunchSpots.choose(p, true, alliance, LaunchSpotDrive.wallGapIn, LaunchSpotDrive.maxPastCentreIn,
                TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn, LaunchSpotDrive.wallCentreBiasIn,
                LaunchSpotDrive.closeCentreBiasIn, LaunchSpotDrive.closeBackIn);
    }

    /** [x, y, heading in degrees, 0 to 360], to 0.01. */
    private static String pose(Pose p) {
        double deg = Math.toDegrees(p.heading()) % 360.0;
        if (deg < 0) deg += 360.0;
        return String.format(Locale.ROOT, "[%.2f, %.2f, %.2f]", p.x(), p.y(), deg);
    }

    private static String num(double v) {
        return v == Math.rint(v) && Math.abs(v) < 1e9 ? String.format(Locale.ROOT, "%.1f", v) : String.valueOf(v);
    }

    private static String indent(int depth) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < depth; i++) b.append("  ");
        return b.toString();
    }
}
