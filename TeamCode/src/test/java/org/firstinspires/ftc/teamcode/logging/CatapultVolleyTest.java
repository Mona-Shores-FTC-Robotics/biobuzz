package org.firstinspires.ftc.teamcode.logging;

import org.junit.Test;

import java.util.Locale;

/**
 * One volley of 4 POLLEN into a raised CELL held still (no TIP): how many stay in. Separates the
 * throw from the Autos around it, for comparing a flywheel with catapults. Opt in:
 *
 * <pre>
 * BIOBUZZ_VOLLEY_STUDY=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*CatapultVolleyTest*' -i
 * </pre>
 */
public class CatapultVolleyTest {

    static final double LOOP = 0.02;

    @Test
    public void volleys() throws java.io.IOException {
        if (System.getenv("BIOBUZZ_VOLLEY_STUDY") == null) return;
        java.util.Map<String, RobotDesign> designs = AutoStudyTest.designs();
        for (String name : new String[] {"two spring hoods, 24 in catcher", "patterned catapult, 24 in catcher",
                "plain catapult, 24 in catcher", "clump catapult, 24 in catcher"}) {
            RobotDesign d = designs.get(name);
            StringBuilder line = new StringBuilder(String.format(Locale.ROOT, "VOLLEY %-36s", name));
            for (double distance : new double[] {24, 38, 50, 65}) {
                int in = 0, runs = 40;
                for (long seed = 1; seed <= runs; seed++) in += volley(d, distance, seed);
                line.append(String.format(Locale.ROOT, " | %2.0f in: %.2f of 4", distance, in / (double) runs));
            }
            System.out.println(line);
        }
        // The catapult's launch angle, from the distances an Auto fires at.
        for (double pitch : new double[] {60, 65, 70, 75, 80}) {
            RobotDesign d = designs.get("clump catapult, 24 in catcher").copy("clump catapult, pitch " + pitch);
            d.fixedPitchDeg = pitch;
            StringBuilder line = new StringBuilder(String.format(Locale.ROOT, "VOLLEY %-36s", d.name));
            for (double distance : new double[] {24, 31, 38, 44, 50, 65}) {
                int in = 0, runs = 40;
                for (long seed = 1; seed <= runs; seed++) in += volley(d, distance, seed);
                line.append(String.format(Locale.ROOT, " | %2.0f in: %.2f", distance, in / (double) runs));
            }
            System.out.println(line);
        }
    }

    /** POLLEN still in the CELL 2 s after the last leaves the robot. */
    static int volley(RobotDesign d, double distance, long seed) throws java.io.IOException {
        FieldSim sim = new FieldSim(HiveAssets.committedStagedPieces(), seed);
        sim.main.design = d;
        sim.red.locked = true;
        double[] aim = sim.red.aimPoint();
        double[] open = sim.red.openingCentre();
        double y = open[1] - distance;
        sim.setRobot(open[0], y, Math.PI / 2, 0, 0, 0, false);
        int before = sim.count(sim.red.cell(sim.red.raisedEnd()));
        boolean catapult = d.launcher == RobotDesign.Launcher.CATAPULT;
        boolean clump = catapult && d.catapultClump;
        for (int i = 0; i < 4; i++) {
            FieldSim.Piece p = new FieldSim.Piece(FieldSim.Kind.POLLEN, FieldSim.Where.ROBOT, 0, 0, 0);
            sim.pieces.add(p);
            sim.stored.add(p);
        }
        if (catapult) {
            if (clump) {
                sim.volleyNoise = new double[] {sim.random.nextGaussian(), sim.random.nextGaussian(), sim.random.nextGaussian()};
                sim.volleyResidual = d.catapultResidual;
            }
            for (int i = 0; i < 4; i++) {
                double side = (i - 1.5) * d.catapultSideIn;
                if (clump) {
                    double pitch = 2 * FieldSim.NECTAR_RADIUS_IN + 0.2;
                    side = ((i % 2) - 0.5) * pitch;
                    sim.volleyUpIn = (i / 2) * pitch;
                }
                sim.launch(sim.main, aim, 0, side, clump ? 1.0 : d.catapultSpread);
            }
            sim.volleyNoise = null;
            sim.volleyUpIn = 0;
        } else {
            // Flywheels: d.launchers at a time, d.shotIntervalS apart.
            for (int i = 0; i < 4; ) {
                for (int k = 0; k < d.launchers && i < 4; k++, i++) {
                    double side = d.launchers == 1 ? 0 : (k - (d.launchers - 1) / 2.0) * 6.0;
                    sim.launch(sim.main, aim, 0, side, 1.0);
                }
                for (double t = 0; t < d.shotIntervalS; t += LOOP) sim.step(LOOP);
            }
        }
        for (double t = 0; t < 2; t += LOOP) sim.step(LOOP);
        return sim.count(sim.red.cell(sim.red.raisedEnd())) - before;
    }
}
