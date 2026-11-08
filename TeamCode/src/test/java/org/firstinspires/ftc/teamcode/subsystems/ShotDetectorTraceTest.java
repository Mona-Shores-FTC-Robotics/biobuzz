package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem.ShotDetector;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ShotDetector} replayed on loops copied from the Belding Qualifier's match logs (7 Nov
 * 2026, branch {@code events/2026-11-belding}), one row per logged loop, as the robot fed it:
 * {@code src/test/resources/launcher-traces/}. The rule the robot ran at Belding counted 2 shots
 * in the first trace, 0 in the third, and 2 in the last with its gate forced open.
 */
public class ShotDetectorTraceTest {

    /** {@link LauncherSubsystem#shotDipTicksPerSecond}'s default, the depth these traces were checked at. */
    private static final double COUNT_DIP = 55.0;

    /** The robot's at-speed tolerance at Belding, ticks/s. */
    private static final double TOLERANCE = 60.0;

    /** One logged loop: ms, target, left, right, gate open. */
    private static final class Loop {
        final long ms;
        final double target;
        final double left;
        final double right;
        final boolean gateOpen;

        Loop(long ms, double target, double left, double right, boolean gateOpen) {
            this.ms = ms;
            this.target = target;
            this.left = left;
            this.right = right;
            this.gateOpen = gateOpen;
        }
    }

    private static List<Loop> trace(String name) throws IOException {
        InputStream in = ShotDetectorTraceTest.class.getResourceAsStream("/launcher-traces/" + name);
        assertNotNull("missing trace " + name, in);
        List<Loop> loops = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] f = line.split(",");
                loops.add(new Loop(Long.parseLong(f[0]), Double.parseDouble(f[1]), Double.parseDouble(f[2]),
                        Double.parseDouble(f[3]), f[4].trim().equals("1")));
            }
        }
        return loops;
    }

    /** Replays a trace; {@code forceGateOpen} lets every dip start. Returns the shots counted. */
    private static int replay(String name, boolean forceGateOpen) throws IOException {
        ShotDetector d = new ShotDetector();
        for (Loop l : trace(name)) {
            boolean within = Math.abs(l.left - l.target) <= TOLERANCE && Math.abs(l.right - l.target) <= TOLERANCE;
            d.update(l.target, l.left, l.target, l.right, within, forceGateOpen || l.gateOpen, l.target,
                    COUNT_DIP, l.ms);
        }
        return d.shots();
    }

    @Test
    public void aPreloadedVolleyWithShallowPollenDipsCountsAllFour() throws IOException {
        // 19429, 11:36. Four dips 0.45–0.55 s apart; the last two only 65–75 deep on the average,
        // about 50 on one wheel. The Belding rule counted 2 and the Auto fed on to its 3.5 s limit.
        assertEquals(4, replay("belding-19429-11-36-28-auto-preload.csv", false));
    }

    @Test
    public void aGateOpeningWhileTheWheelsComeDownFromAnOvershootStillCountsFour() throws IOException {
        // 20245, 14:39. The wheels are 100 over target from a spin-up when the gate opens and fall
        // back to target: that fall is not a dip, and the four real ones are.
        assertEquals(4, replay("belding-20245-14-39-52-auto-preload.csv", false));
    }

    @Test
    public void aTeleOpVolleyTheBeldingRuleMissedEntirely() throws IOException {
        // 19429, 17:32. Four dips at the volley's normal cadence; the Belding rule counted none.
        assertEquals(4, replay("belding-19429-17-32-26-teleop.csv", false));
    }

    @Test
    public void sagsWithTheGateShutAreNotShotsEvenIfTheGateWereOpen() throws IOException {
        // 20245, 16:53, 3 s of TeleOp with the gate shut: nothing can be a shot. Forced open, the
        // Belding rule would have counted 2 of its slow sags; the duration window counts none.
        assertEquals(0, replay("belding-20245-16-53-50-gate-shut.csv", false));
        assertEquals(0, replay("belding-20245-16-53-50-gate-shut.csv", true));
    }
}
