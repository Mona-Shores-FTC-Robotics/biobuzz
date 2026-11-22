package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.subsystems.LaneTracker.Piece;
import org.firstinspires.ftc.teamcode.subsystems.NectarReject.Phase;
import org.firstinspires.ftc.teamcode.subsystems.NectarReject.Sighting;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link NectarReject}: what a reading says ({@link NectarReject#judge}), when a burst starts, its
 * hold-off, when it re-arms, its cap, and when the lane is suspect; then the Fruitport readings
 * ({@code src/test/resources/nectar-traces/}) replayed through it.
 */
public class NectarRejectTest {

    // ColorSensorSubsystem's shared values on 22 Nov 2026 (20245's; 19429 has its own seen levels).
    private static final double SEEN = 0.05;
    private static final double FAINT = 0.015;
    private static final double FAINT_SAT = 0.45;
    private static final double DOUBT = 5;
    private static final double POLLEN_MIN = 43;
    private static final double POLLEN_MAX = 130;
    private static final double RED_MIN = 330;
    private static final double RED_MAX = 42;
    private static final double BLUE_MIN = 180;
    private static final double BLUE_MAX = 270;
    private static final double MAX_CHANNEL = 0.5;

    private static final double BURST_MS = 500;
    private static final double HOLD_OFF_MS = 300;
    private static final int MAX_BURSTS = 3;
    private static final double WINDOW_MS = 3000;
    private static final double FAINT_WINDOW_MS = 3000;
    private static final double CLEAR_MS = 1000;
    private static final double EJECT_CLEAR_MS = 1000;

    private final NectarReject reject = new NectarReject();

    private static Sighting judge(double hue, double sat, double brightness, Alliance ours, boolean allNectar) {
        return NectarReject.judge(hue, sat, brightness, ours, allNectar, SEEN, FAINT, FAINT_SAT, DOUBT,
                POLLEN_MIN, POLLEN_MAX, RED_MIN, RED_MAX, BLUE_MIN, BLUE_MAX);
    }

    private void step(long ms, boolean fresh, Sighting seen, boolean outward, int afterReadings) {
        reject.step(ms, fresh, seen, outward, true, afterReadings, BURST_MS, HOLD_OFF_MS, MAX_BURSTS, WINDOW_MS,
                FAINT_WINDOW_MS, CLEAR_MS, EJECT_CLEAR_MS);
    }

    private void wrong(long ms) {
        step(ms, true, Sighting.WRONG, false, 1);
    }

    private void wrongOut(long ms) {
        step(ms, true, Sighting.WRONG, true, 1);
    }

    private void clear(long ms) {
        step(ms, true, Sighting.NOTHING, false, 1);
    }

    private void clearOut(long ms) {
        step(ms, true, Sighting.NOTHING, true, 1);
    }

    private void tick(long ms) {
        step(ms, false, Sighting.NOTHING, false, 1);
    }

    // ---- what a reading says ----

    @Test
    public void theEnemyNectarIsTheOtherAlliancesColour() {
        assertEquals(Piece.BLUE_NECTAR, NectarReject.enemyNectar(Alliance.RED));
        assertEquals(Piece.RED_NECTAR, NectarReject.enemyNectar(Alliance.BLUE));
        assertNull(NectarReject.enemyNectar(Alliance.UNKNOWN));
    }

    /** Fruitport readings of real pieces (hue, saturation, brightness), each judged for both alliances. */
    @Test
    public void fruitportPiecesAreJudgedByAlliance() {
        // Q-11, 175.49 s: the red NECTAR that was later launched.
        assertEquals(Sighting.WRONG, judge(13.8, 0.777, 0.97, Alliance.BLUE, false));
        assertEquals(Sighting.KEEP, judge(13.8, 0.777, 0.97, Alliance.RED, false));
        // Q-28, 177.85 s: a blue NECTAR on RED.
        assertEquals(Sighting.WRONG, judge(230.0, 0.852, 0.925, Alliance.RED, false));
        assertEquals(Sighting.KEEP, judge(230.0, 0.852, 0.925, Alliance.BLUE, false));
        // Q-28, 154.66 s: our red NECTAR on RED, and the POLLEN behind it.
        assertEquals(Sighting.KEEP, judge(15.0, 0.777, 0.935, Alliance.RED, false));
        assertEquals(Sighting.KEEP, judge(76.0, 0.728, 0.99, Alliance.RED, false));
        assertEquals(Sighting.KEEP, judge(76.0, 0.728, 0.99, Alliance.BLUE, false));
        // A POLLEN leaving the spot dimly (Q-11, 175.42 s), 6° above red's range: kept.
        assertEquals(Sighting.KEEP, judge(48.2, 0.651, 0.09, Alliance.BLUE, false));
    }

    @Test
    public void anUncertainPieceIsWrong() {
        // 2° above red NECTAR's top: within the doubt band, so wrong on BLUE, but red's ours on RED.
        assertEquals(Sighting.WRONG, judge(44, 0.7, 0.5, Alliance.BLUE, false));
        assertEquals(Sighting.KEEP, judge(44, 0.7, 0.5, Alliance.RED, false));
        // Midway between POLLEN (130) and blue (180): a tie, wrong on RED; on BLUE both are ours.
        assertEquals(Sighting.WRONG, judge(155, 0.7, 0.5, Alliance.RED, false));
        assertEquals(Sighting.KEEP, judge(155, 0.7, 0.5, Alliance.BLUE, false));
        // Nearer blue than POLLEN, on RED.
        assertEquals(Sighting.WRONG, judge(165, 0.7, 0.5, Alliance.RED, false));
        // Magenta, midway between blue (270) and red (330): a tie, wrong whichever alliance.
        assertEquals(Sighting.WRONG, judge(300, 0.7, 0.5, Alliance.BLUE, false));
        assertEquals(Sighting.WRONG, judge(300, 0.7, 0.5, Alliance.RED, false));
        // Nearer red, on BLUE.
        assertEquals(Sighting.WRONG, judge(310, 0.7, 0.5, Alliance.BLUE, false));
        // 19429's lane grey-green at Fruitport, 2° past POLLEN's top: nearest POLLEN, kept.
        assertEquals(Sighting.KEEP, judge(133, 0.38, 0.139, Alliance.RED, false));
        assertEquals(Sighting.KEEP, judge(133, 0.38, 0.139, Alliance.BLUE, false));
        // No hue at all.
        assertEquals(Sighting.WRONG, judge(Double.NaN, 0.0, 0.5, Alliance.RED, false));
        // Blue's edge, 3° inside the doubt band, on RED.
        assertEquals(Sighting.WRONG, judge(177, 0.7, 0.5, Alliance.RED, false));
    }

    @Test
    public void allNectarRejectsBothColoursAndKeepsPollen() {
        assertEquals(Sighting.WRONG, judge(15, 0.78, 0.9, Alliance.RED, true));
        assertEquals(Sighting.WRONG, judge(230, 0.85, 0.9, Alliance.RED, true));
        assertEquals(Sighting.WRONG, judge(15, 0.78, 0.9, Alliance.UNKNOWN, true));
        assertEquals(Sighting.KEEP, judge(77, 0.74, 0.9, Alliance.RED, true));
    }

    @Test
    public void noAllianceIsNeverWrong() {
        assertEquals(Sighting.KEEP, judge(230, 0.85, 0.9, Alliance.UNKNOWN, false));
        assertEquals(Sighting.KEEP, judge(15, 0.78, 0.9, Alliance.UNKNOWN, false));
        assertEquals(Sighting.NOTHING, judge(31, 0.55, 0.022, Alliance.UNKNOWN, false));
    }

    @Test
    public void aFaintWrongColourIsAFaintSighting() {
        // Q-11, 178.71 s: the red NECTAR inside the lane, never above 0.05.
        assertEquals(Sighting.WRONG_FAINT, judge(31.0, 0.55, 0.022, Alliance.BLUE, false));
        assertEquals(Sighting.WRONG_FAINT, judge(33.9, 0.50, 0.018, Alliance.BLUE, false));
        // Our own colour faint is nothing.
        assertEquals(Sighting.NOTHING, judge(31.0, 0.55, 0.022, Alliance.RED, false));
    }

    @Test
    public void theEmptyLaneIsNothing() {
        // 20245 at Fruitport: 0.006 to 0.012 at 100° to 165°, saturation 0.33 to 0.42.
        assertEquals(Sighting.NOTHING, judge(120, 0.333, 0.008, Alliance.RED, false));
        assertEquals(Sighting.NOTHING, judge(120, 0.333, 0.008, Alliance.BLUE, false));
        // Q-11, 177.37 s: dim, between the NECTAR's and the lane's hue, at the lane's saturation.
        assertEquals(Sighting.NOTHING, judge(50.8, 0.38, 0.013, Alliance.BLUE, false));
        // 19429 lit (16 Nov 2026 on): up to 0.085 at 137° to 148°, saturation 0.46 to 0.50, its seen level 0.09.
        assertEquals(Sighting.NOTHING, NectarReject.judge(142, 0.48, 0.085, Alliance.RED, false, 0.09, FAINT,
                FAINT_SAT, DOUBT, POLLEN_MIN, POLLEN_MAX, RED_MIN, RED_MAX, BLUE_MIN, BLUE_MAX));
        assertEquals(Sighting.NOTHING, NectarReject.judge(142, 0.48, 0.085, Alliance.BLUE, false, 0.09, FAINT,
                FAINT_SAT, DOUBT, POLLEN_MIN, POLLEN_MAX, RED_MIN, RED_MAX, BLUE_MIN, BLUE_MAX));
        // A dim reading at a blue hue but the lane's saturation (20245, 5 Nov 2026: 0.023 at 200°).
        assertEquals(Sighting.NOTHING, judge(200, 0.40, 0.023, Alliance.RED, false));
    }

    @Test
    public void unsetNumbersJudgeNothingWrong() {
        assertEquals(Sighting.NOTHING, NectarReject.judge(230, 0.85, 0.9, Alliance.RED, false, Double.NaN,
                FAINT, FAINT_SAT, DOUBT, POLLEN_MIN, POLLEN_MAX, RED_MIN, RED_MAX, BLUE_MIN, BLUE_MAX));
        assertEquals("no faint level: no faint sightings", Sighting.NOTHING, NectarReject.judge(31, 0.55, 0.022,
                Alliance.BLUE, false, SEEN, Double.NaN, FAINT_SAT, DOUBT, POLLEN_MIN, POLLEN_MAX, RED_MIN,
                RED_MAX, BLUE_MIN, BLUE_MAX));
    }

    @Test
    public void glitchReadingsAreNotPlausible() {
        // 20245, Fruitport: brightness 1.00, red 1.0, green and blue 0 or 1.
        assertFalse(NectarReject.plausible(1.0, 0.0, 0.0, 1.0, MAX_CHANNEL));
        assertFalse(NectarReject.plausible(1.0, 1.0, 1.0, 1.0, MAX_CHANNEL));
        assertFalse(NectarReject.plausible(1.0, 0.0, 1.0, 1.0, MAX_CHANNEL));
        assertFalse(NectarReject.plausible(1.0, 0.07, 0.02, 1.0, Double.NaN));
        assertFalse(NectarReject.plausible(Double.NaN, 0, 0, 0.5, MAX_CHANNEL));
        // A POLLEN pressed up to the sensor (Q-28, 155.26 s) and the empty lane are real.
        assertTrue(NectarReject.plausible(0.046, 0.057, 0.016, 0.99, MAX_CHANNEL));
        assertTrue(NectarReject.plausible(0.0001, 0.0002, 0.0001, 0.008, MAX_CHANNEL));
        assertTrue("a real NECTAR's brightest channel, 0.19", NectarReject.plausible(0.19, 0.06, 0.03, 0.97, MAX_CHANNEL));
    }

    @Test
    public void distanceToARangeWrapsRoundTheCircle() {
        assertEquals(0.0, NectarReject.distanceToRange(10, 330, 42), 1e-9);
        assertEquals(5.0, NectarReject.distanceToRange(47, 330, 42), 1e-9);
        assertEquals(10.0, NectarReject.distanceToRange(320, 330, 42), 1e-9);
        assertEquals(3.0, NectarReject.distanceToRange(177, 180, 270), 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, NectarReject.distanceToRange(10, Double.NaN, 42), 0);
    }

    // ---- the burst ----

    @Test
    public void oneWrongReadingStartsABurst() {
        wrong(0);
        assertTrue(reject.rejecting());
        assertTrue(reject.justStarted());
        assertEquals(1, reject.bursts());
    }

    @Test
    public void twoReadingsCanStillBeAsked() {
        step(0, true, Sighting.WRONG, false, 2);
        assertFalse("one reading is not enough", reject.rejecting());
        step(50, true, Sighting.KEEP, false, 2);
        step(100, true, Sighting.WRONG, false, 2);
        assertFalse("another reading between starts the count again", reject.rejecting());
        step(150, true, Sighting.WRONG, false, 2);
        assertTrue(reject.rejecting());
    }

    @Test
    public void keptPiecesAndNothingNeverBurst() {
        for (int i = 0; i < 10; i++) step(i * 50L, true, Sighting.KEEP, false, 1);
        for (int i = 0; i < 10; i++) step(500 + i * 50L, true, Sighting.NOTHING, false, 1);
        assertEquals(0, reject.bursts());
        assertFalse(reject.suspect());
    }

    @Test
    public void aBurstThenAHoldOffThenIdle() {
        wrong(0);
        clear(300);
        assertEquals("the burst runs its time whatever the sensor sees", Phase.BURST, reject.phase());
        tick(499);
        assertEquals(Phase.BURST, reject.phase());
        tick(500);
        assertEquals(Phase.HOLD_OFF, reject.phase());
        assertTrue(reject.holdingOff());
        assertFalse(reject.rejecting());
        tick(799);
        assertEquals(Phase.HOLD_OFF, reject.phase());
        tick(800);
        assertEquals(Phase.IDLE, reject.phase());
    }

    @Test
    public void noHoldOffGoesStraightToIdle() {
        reject.step(0, true, Sighting.WRONG, false, true, 1, BURST_MS, 0, MAX_BURSTS, WINDOW_MS,
                FAINT_WINDOW_MS, CLEAR_MS, EJECT_CLEAR_MS);
        reject.step(500, false, Sighting.NOTHING, false, true, 1, BURST_MS, 0, MAX_BURSTS, WINDOW_MS,
                FAINT_WINDOW_MS, CLEAR_MS, EJECT_CLEAR_MS);
        assertEquals(Phase.IDLE, reject.phase());
    }

    @Test
    public void aPieceThatNeverLeftIsNotBurstAgain() {
        wrong(0);
        for (long t = 50; t < 3000; t += 50) wrong(t);
        assertEquals("one burst per arrival, never a chain", 1, reject.bursts());
        assertEquals(Phase.IDLE, reject.phase());
        assertTrue("and the lane stays suspect", reject.suspect());
    }

    @Test
    public void aPieceBackAfterAClearReadingGetsAnotherBurst() {
        wrong(0);
        clear(400);
        tick(800);
        wrong(900);
        assertTrue(reject.rejecting());
        assertEquals(2, reject.bursts());
    }

    @Test
    public void theCapStopsItUntilADifferentReading() {
        long t = 0;
        for (int i = 0; i < MAX_BURSTS; i++) {
            wrong(t);
            assertTrue(reject.rejecting());
            clear(t + 100);
            tick(t + 850);
            t += 900;
        }
        assertEquals(MAX_BURSTS, reject.bursts());
        wrong(t);
        assertFalse("the fourth within the window gives up", reject.rejecting());
        assertTrue(reject.justGaveUp());
        assertTrue(reject.gaveUp());
        clear(t + 100);
        assertFalse("a different reading re-arms it", reject.gaveUp());
    }

    @Test
    public void theCapLetsGoOnceTheWindowPasses() {
        long t = 0;
        for (int i = 0; i < MAX_BURSTS; i++) {
            wrong(t);
            clear(t + 100);
            tick(t + 850);
            t += 900;
        }
        t += 3000;
        wrong(t);
        assertTrue(reject.rejecting());
    }

    @Test
    public void theSwitchOffEndsItAndClearsSuspect() {
        wrong(0);
        reject.step(100, true, Sighting.WRONG, false, false, 1, BURST_MS, HOLD_OFF_MS, MAX_BURSTS, WINDOW_MS,
                FAINT_WINDOW_MS, CLEAR_MS, EJECT_CLEAR_MS);
        assertEquals(Phase.IDLE, reject.phase());
        assertFalse(reject.suspect());
    }

    @Test
    public void noBurstLengthNeverRejects() {
        reject.step(0, true, Sighting.WRONG, false, true, 1, Double.NaN, HOLD_OFF_MS, MAX_BURSTS, WINDOW_MS,
                FAINT_WINDOW_MS, CLEAR_MS, EJECT_CLEAR_MS);
        reject.step(50, true, Sighting.WRONG, false, true, 1, 0, HOLD_OFF_MS, MAX_BURSTS, WINDOW_MS,
                FAINT_WINDOW_MS, CLEAR_MS, EJECT_CLEAR_MS);
        assertEquals(0, reject.bursts());
    }

    @Test
    public void readingsCountOnlyWhenFresh() {
        step(20, false, Sighting.WRONG, false, 1);
        assertFalse(reject.rejecting());
        assertFalse(reject.suspect());
    }

    /**
     * 20245, 5 Nov 2026 (23:55 Basic Drive, 25.4 to 26.5 s): a burst left the NECTAR at the mouth,
     * it rolled back under the sensor during the hold-off. It must burst again as it rolls back.
     */
    @Test
    public void aPieceRollingBackDuringTheHoldOffIsBurstAgainAtOnce() {
        wrong(25380);
        assertTrue(reject.rejecting());
        clearOut(25681);
        tick(25950);
        assertTrue(reject.holdingOff());
        wrong(26165);
        assertTrue("burst again during the hold-off", reject.rejecting());
        assertEquals(2, reject.bursts());
    }

    // ---- suspect ----

    @Test
    public void seenLeavingThroughABurstClearsAfterClearMs() {
        wrong(0);          // inward: the reading that starts the burst
        wrongOut(60);      // still there as the lane runs outward
        clearOut(200);     // gone, still outward: seen leaving outward
        assertTrue(reject.suspect());
        clear(1100);
        assertTrue(reject.suspect());
        clear(1200);
        assertFalse(reject.suspect());
        assertTrue(reject.justCleared());
        assertFalse(reject.clearedByEject());
    }

    @Test
    public void goneBeforeTheLaneRanOutwardStaysSuspect() {
        wrong(0);
        clearOut(60);      // the first reading of the burst is already clean: it may have gone in
        for (long t = 100; t < 5000; t += 50) clear(t);
        assertTrue("direction unknown: suspect until an eject", reject.suspect());
    }

    @Test
    public void goneWhileTheLaneRanInwardStaysSuspect() {
        wrong(0);
        wrongOut(60);
        tick(600);
        clear(700);        // the burst is over; it left the spot while the lane took in
        for (long t = 750; t < 5000; t += 50) clear(t);
        assertTrue(reject.suspect());
    }

    @Test
    public void anEjectClearsItEvenWithFaintReadings() {
        wrong(0);
        clear(100);
        for (long t = 1000; t < 1950; t += 50) step(t, true, Sighting.WRONG_FAINT, true, 1);
        assertTrue(reject.suspect());
        step(2000, true, Sighting.WRONG_FAINT, true, 1);
        assertFalse("1 s of eject with no bright wrong reading", reject.suspect());
        assertTrue(reject.clearedByEject());
    }

    @Test
    public void aBrightWrongReadingDuringAnEjectRestartsIt() {
        wrong(0);
        clear(100);
        for (long t = 1000; t < 1600; t += 50) clearOut(t);
        wrongOut(1600);   // the NECTAR comes past the sensor on its way out
        for (long t = 1650; t < 2550; t += 50) clearOut(t);
        assertTrue(reject.suspect());
        tick(2600);
        assertTrue("seen leaving outward, but not yet clean for clearMs", reject.suspect());
        clear(2700);
        assertFalse(reject.suspect());
    }

    @Test
    public void aFaintSightingAloneNeverMakesTheLaneSuspect() {
        for (long t = 0; t < 10000; t += 50) step(t, true, Sighting.WRONG_FAINT, false, 1);
        assertFalse(reject.suspect());
        assertEquals(0, reject.bursts());
    }

    @Test
    public void aFaintSightingSoonAfterABrightOneCounts() {
        wrong(0);
        clearOut(60);
        tick(500);
        assertTrue(reject.suspect());
        // Pretend it had cleared: an eject.
        for (long t = 600; t <= 1600; t += 50) clearOut(t);
        assertFalse(reject.suspect());
        step(2500, true, Sighting.WRONG_FAINT, false, 1);
        assertTrue("2.5 s after a bright wrong reading", reject.suspect());
    }

    // ---- Fruitport, replayed ----

    /** One row of a colour-sensor trace. */
    private static final class Row {
        final long ms;
        final double red, green, blue, brightness, hue, sat;
        final boolean outward;
        final int shots;

        Row(String[] f) {
            ms = Long.parseLong(f[0]);
            red = Double.parseDouble(f[1]);
            green = Double.parseDouble(f[2]);
            blue = Double.parseDouble(f[3]);
            brightness = Double.parseDouble(f[4]);
            hue = Double.parseDouble(f[5]);
            sat = Double.parseDouble(f[6]);
            outward = f[7].trim().equals("1");
            shots = Integer.parseInt(f[8].trim());
        }
    }

    private static List<Row> trace(String name) throws IOException {
        InputStream in = NectarRejectTest.class.getResourceAsStream("/nectar-traces/" + name);
        assertNotNull("missing trace " + name, in);
        List<Row> rows = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                rows.add(new Row(line.split(",")));
            }
        }
        return rows;
    }

    /** What a replay saw. */
    private static final class Replay {
        int bursts;
        int suspects;
        int glitches;
        /** Shots counted while the lane was suspect: the feed gate would have stayed shut. */
        int shotsBlocked;
        int shotsLet;
        boolean suspectAtEnd;
    }

    private static Replay replay(String name, Alliance ours) throws IOException {
        NectarReject g = new NectarReject();
        Replay out = new Replay();
        for (Row row : trace(name)) {
            boolean usable = NectarReject.plausible(row.red, row.green, row.blue, row.brightness, MAX_CHANNEL);
            if (!usable) out.glitches++;
            Sighting s = usable ? judge(row.hue, row.sat, row.brightness, ours, false) : Sighting.NOTHING;
            g.step(row.ms, usable, s, row.outward, true, 1, BURST_MS, HOLD_OFF_MS, MAX_BURSTS, WINDOW_MS,
                    FAINT_WINDOW_MS, CLEAR_MS, EJECT_CLEAR_MS);
            if (g.justStarted()) out.bursts++;
            if (g.justSuspected()) out.suspects++;
            if (g.suspect()) out.shotsBlocked += row.shots; else out.shotsLet += row.shots;
        }
        out.suspectAtEnd = g.suspect();
        return out;
    }

    /**
     * Fruitport Q-11, 20245 on BLUE: a red NECTAR came in behind a POLLEN, two bursts did not clear
     * it, it sat inside the lane reading faint, and the second of the next two shots launched it.
     * Replayed now, the lane is suspect from the first sighting, and both shots find the feed gate
     * shut (until the driver's eject).
     */
    @Test
    public void q11TheRedNectarIsNeverLaunched() throws IOException {
        Replay r = replay("fruitport-20245-q11-blue-red-nectar-launched.csv", Alliance.BLUE);
        assertTrue("bursts at the red NECTAR", r.bursts >= 2);
        assertEquals("the two shots of 181.4 and 183.1 s", 2, r.shotsBlocked);
        assertEquals(0, r.shotsLet);
        assertTrue(r.suspectAtEnd);
    }

    /** The same readings on RED: the red NECTAR is ours, kept, and nothing is held. */
    @Test
    public void q11OnRedKeepsIt() throws IOException {
        Replay r = replay("fruitport-20245-q11-blue-red-nectar-launched.csv", Alliance.RED);
        assertEquals(0, r.bursts);
        assertEquals(0, r.suspects);
        assertEquals(2, r.shotsLet);
    }

    /** Fruitport Q-28, 20245 on RED: a blue NECTAR rejected in one burst, seen leaving, the lane clear after. */
    @Test
    public void q28TheBlueNectarIsRejectedAndTheLaneClears() throws IOException {
        Replay r = replay("fruitport-20245-q28-red-blue-nectar-rejected.csv", Alliance.RED);
        assertEquals(1, r.bursts);
        assertEquals(1, r.suspects);
        assertFalse("seen leaving outward through the burst, then clean", r.suspectAtEnd);
    }

    /** The same readings on BLUE: the blue NECTAR is ours, kept. */
    @Test
    public void q28OnBlueKeepsIt() throws IOException {
        Replay r = replay("fruitport-20245-q28-red-blue-nectar-rejected.csv", Alliance.BLUE);
        assertEquals(0, r.bursts);
        assertEquals(0, r.suspects);
    }

    /**
     * Fruitport Q-20, 20245 on BLUE, the whole match: no NECTAR came in, but two glitch readings
     * fired red NECTAR rejects. Dropped as glitches now, nothing fires and no shot is held.
     */
    @Test
    public void q20GlitchesFireNothing() throws IOException {
        for (Alliance ours : new Alliance[] {Alliance.BLUE, Alliance.RED}) {
            Replay r = replay("fruitport-20245-q20-blue-whole-match.csv", ours);
            assertEquals(24, r.glitches);
            assertEquals(ours + ": no burst", 0, r.bursts);
            assertEquals(ours + ": no suspect lane", 0, r.suspects);
            assertEquals(0, r.shotsBlocked);
        }
    }
}
