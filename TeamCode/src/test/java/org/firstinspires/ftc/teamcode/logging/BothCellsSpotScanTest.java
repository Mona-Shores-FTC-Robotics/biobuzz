package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assume.assumeTrue;

import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretAim;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.vision.HiveState;
import org.junit.Test;

import java.util.Locale;

/**
 * For {@code doc/three-tips.md}: is there a spot from which a turret reaches both of our CELLs? The CELLs open away
 * from each other (the RIGHT CELL toward the audience wall, the LEFT CELL toward the far wall), so one spot can only
 * face both from the side, along the alliance wall. This fires single POLLEN from a grid of spots at each CELL, at the
 * speed the range needs ({@link FieldSim#idealSpeed}, a speed table with a row for every range) and the placeholder
 * shot-to-shot spread, and prints the share the simulated CELL keeps, with the range from the turret axis.
 *
 * <p>What it rests on: the simulated CELL's shape (the field CAD's opening, floor, roof and back) and a 75 degree
 * launch. Nobody has fired at a CELL from its side, so every share here is the model's, not a measurement.
 *
 * <p>Opt-in: {@code BIOBUZZ_SIM_BOTH_CELLS=1}; {@code BIOBUZZ_SIM_SHOTS} sets the shots a cell (40);
 * {@code BIOBUZZ_SIM_GRID="x0,x1,dx,y0,y1,dy"} the grid (red frame, inches).
 */
public class BothCellsSpotScanTest {

    @Test
    public void scan() {
        assumeTrue("set BIOBUZZ_SIM_BOTH_CELLS=1 to scan launch spots for both CELLs", "1".equals(System.getenv("BIOBUZZ_SIM_BOTH_CELLS")));
        String[] g = System.getenv().getOrDefault("BIOBUZZ_SIM_GRID", "8,32,4,40,100,6").split(",");
        double x0 = Double.parseDouble(g[0]), x1 = Double.parseDouble(g[1]), dx = Double.parseDouble(g[2]);
        double y0 = Double.parseDouble(g[3]), y1 = Double.parseDouble(g[4]), dy = Double.parseDouble(g[5]);
        boolean before = FieldSim.idealSpeed;
        FieldSim.idealSpeed = true;
        try {
            RobotDesign d = RobotDesign.real();
            FieldSim probe = new FieldSim(new java.util.ArrayList<>(), 1, HiveCalibration.current().fit());
            double[] left = probe.red.aimPointFor(1), right = probe.red.aimPointFor(-1);
            System.out.println(String.format(Locale.ROOT, "BOTHCELLS aim points: LEFT (%.1f, %.1f, %.1f) RIGHT (%.1f, %.1f, %.1f)",
                    left[0], left[1], left[2], right[0], right[1], right[2]));
            for (double x = x0; x <= x1 + 1e-9; x += dx) {
                for (double y = y0; y <= y1 + 1e-9; y += dy) {
                    double l = LauncherSetpointTest.scored(x, y, HiveState.LEFT_CELL_UP, LauncherSubsystem.targetTicksPerSecond, d);
                    double r = LauncherSetpointTest.scored(x, y, HiveState.RIGHT_CELL_UP, LauncherSubsystem.targetTicksPerSecond, d);
                    System.out.println(String.format(Locale.ROOT,
                            "BOTHCELLS (%5.1f, %5.1f): LEFT %3.0f%% at %4.1f in, RIGHT %3.0f%% at %4.1f in", x, y,
                            100 * l, range(x, y, left), 100 * r, range(x, y, right)));
                }
            }
        } finally {
            FieldSim.idealSpeed = before;
        }
    }

    /** The range from the turret axis with the robot facing the CELL, as the speed table's rows are measured. */
    private static double range(double x, double y, double[] aim) {
        double h = Math.atan2(aim[1] - y, aim[0] - x);
        return TurretAim.rangeFromFieldIn(x, y, h, TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn, aim[0], aim[1]);
    }
}
