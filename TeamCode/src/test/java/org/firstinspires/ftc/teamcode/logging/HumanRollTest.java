package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Locale;

/**
 * The aimed human roll ({@link AutoSim#humanAims}, {@code BIOBUZZ_AUTO_HUMAN_ENTRY=aim}): a NECTAR released in the
 * LOADING ZONE at the placeholder speed and dead on its aim reaches the intake mouth of a robot on RIGHT_LAUNCH_OFFSET,
 * about 80 in away, still rolling, on an empty field; and for blue it is red's turned half a turn.
 *
 * <p>Opt-in, like every simulator test but the smoke test: {@code BIOBUZZ_SIM_CHECKS=1}.
 */
public class HumanRollTest {

    @Before
    public void optIn() {
        assumeTrue("set BIOBUZZ_SIM_CHECKS=1 to run the simulator's human-roll check", "1".equals(System.getenv("BIOBUZZ_SIM_CHECKS")));
    }

    /** {closest approach in, speed there in/s, seconds to it} of a roll at the mouth of a robot on the spot. */
    static double[] roll(Alliance alliance) throws Exception {
        FieldSim sim = new FieldSim(HiveAssets.committedStagedPieces(), 1, HiveCalibration.current().fit());
        // An empty field but for the NECTAR the human holds.
        sim.pieces.removeIf(p -> p.where != FieldSim.Where.OUTSIDE);
        boolean red = alliance == Alliance.RED;
        double[] spot = AutoSim.aimSpot("RIGHT_LAUNCH_OFFSET");
        double reach = RobotDesign.real().frameIn / 2;
        double[] mouth = {spot[0] + reach * Math.cos(spot[2]), spot[1] + reach * Math.sin(spot[2])};
        double[] from = AutoSim.humanAimFrom.clone();
        if (!red) {
            mouth = new double[] {FieldSim.FIELD_SIZE_IN - mouth[0], FieldSim.FIELD_SIZE_IN - mouth[1]};
            from = new double[] {FieldSim.FIELD_SIZE_IN - from[0], FieldSim.FIELD_SIZE_IN - from[1]};
        }
        assertTrue(sim.enterNectarAimed(alliance, from, Math.atan2(mouth[1] - from[1], mouth[0] - from[0]), AutoSim.humanAimSpeedInPerS));
        FieldSim.Piece nectar = null;
        for (FieldSim.Piece p : sim.pieces) if (p.where == FieldSim.Where.FIELD) nectar = p;
        double best = Double.POSITIVE_INFINITY, speedThere = 0, at = 0;
        for (int k = 0; k < 600; k++) {
            sim.step(0.02);
            double d = Math.hypot(nectar.x - mouth[0], nectar.y - mouth[1]);
            if (d < best) {
                best = d;
                speedThere = Math.hypot(nectar.vx, nectar.vy);
                at = (k + 1) * 0.02;
            }
        }
        return new double[] {best, speedThere, at};
    }

    @Test
    public void anAimedRollReachesTheRobotOnTheSpot() throws Exception {
        double[] r = roll(Alliance.RED), b = roll(Alliance.BLUE);
        System.out.println(String.format(Locale.ROOT, "HumanRollTest red: %.1f in from the mouth at %.1f in/s after %.1f s;"
                + " blue %.1f in, %.1f in/s, %.1f s", r[0], r[1], r[2], b[0], b[1], b[2]));
        assertTrue("red missed the mouth by " + r[0], r[0] < 4);
        assertTrue("red stopped short: " + r[1] + " in/s", r[1] > 3);
        assertTrue("blue differs from red", Math.abs(r[0] - b[0]) < 3 && Math.abs(r[2] - b[2]) < 1);
    }
}
