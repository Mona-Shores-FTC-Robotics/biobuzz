package org.firstinspires.ftc.teamcode.opmodes.calibration;

import org.firstinspires.ftc.teamcode.subsystems.TurretAbsolute;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The guided setup of the turret's two absolute encoders, one step at a time: plain Java, so every
 * step is unit tested at a laptop. {@code Bench: OctoQuad} feeds it the OctoQuad's readings every
 * loop and shows {@link #title()} and {@link #lines()}.
 *
 * <ol>
 *   <li>{@link Step#BOARD}: the OctoQuad answers and both channels have a pulse.</li>
 *   <li>{@link Step#RANGES}: turn the turret slowly; each encoder's narrowest and widest pulse,
 *       once it has wrapped twice. The Cypher Max's clock differs per encoder, so these do too.</li>
 *   <li>{@link Step#DIRECTIONS}: turn it counter-clockwise; which way each reading moves, and
 *       whether B moved the gears' ratio to A.</li>
 *   <li>{@link Step#HOME}: on the home mark, Cross; both readings averaged while still.</li>
 *   <li>{@link Step#CHECK}: the solve with these values: a quarter turn counter-clockwise reads
 *       about +90°, and back on the mark reads 0°. Cross when it does.</li>
 *   <li>{@link Step#DONE}: the lines to paste into {@code RobotDifferences}.</li>
 * </ol>
 */
final class AbsoluteSetup {

    enum Step { BOARD, RANGES, DIRECTIONS, HOME, CHECK, DONE }

    /** Loops in a row with both pulses present before the board counts as working. */
    static final int BOARD_LOOPS = 10;
    /** Wraps each encoder must make before its range is trusted. */
    static final int WRAPS_NEEDED = 2;
    /** A step between two loops bigger than this, µs, is too fast to catch the ends of the range. */
    static final int SLOW_STEP_US = 12;
    /** A Cypher Max's pulse range, µs: its frame is 1.66 to 2.04 ms (490 to 603 Hz). Outside it, the range is wrong. */
    static final double MIN_SPAN_US = 1500;
    static final double MAX_SPAN_US = 2200;
    /** The direction step needs this much turret turn, degrees, then stillness. */
    static final double DIRECTION_TURN_DEG = 30;
    /** B's movement against A's may differ from the gears' ratio by this fraction. */
    static final double RATIO_TOLERANCE = 0.15;
    /** Loops with the readings this still (turns of the encoder per loop) count as still. */
    static final int STILL_LOOPS = 15;
    static final double STILL_TURNS = 0.002;
    /** Loops averaged for the home readings, and the most they may spread, turns. */
    static final int HOME_LOOPS = 25;
    static final double HOME_SPREAD_TURNS = 0.004;

    private final double ratioA;
    private final double ratioB;

    private Step step = Step.BOARD;
    private String problem;

    // BOARD
    private int boardLoops;

    // RANGES
    private final Range rangeA = new Range();
    private final Range rangeB = new Range();

    // DIRECTIONS
    private double accA;
    private double accB;
    private double lastA = Double.NaN;
    private double lastB = Double.NaN;
    private int stillLoops;
    private boolean ratioFailed;
    /** The turret has been let go after the ranges, so the counter-clockwise turn starts from rest. */
    private boolean armed;
    private int dirA;
    private int dirB;

    // HOME
    private boolean homing;
    private int homeLoops;
    private double homeRefA;
    private double homeRefB;
    private double homeSumA;
    private double homeSumB;
    private double homeLoA;
    private double homeHiA;
    private double homeLoB;
    private double homeHiB;
    private double homeA = Double.NaN;
    private double homeB = Double.NaN;

    // CHECK
    private TurretAbsolute.Solution solution;
    private double worstResidualDeg;

    AbsoluteSetup(double ratioA, double ratioB) {
        this.ratioA = ratioA;
        this.ratioB = ratioB;
    }

    Step step() {
        return step;
    }

    /** Why the step cannot go on, or null. */
    String problem() {
        return problem;
    }

    /** One loop's readings. {@code cross} is a press (true for one loop). */
    void update(boolean boardOk, String boardProblem, int pulseA, int pulseB, boolean cross) {
        switch (step) {
            case BOARD:
                board(boardOk, boardProblem, pulseA, pulseB);
                break;
            case RANGES:
                ranges(pulseA, pulseB);
                break;
            case DIRECTIONS:
                directions(pulseA, pulseB, cross);
                break;
            case HOME:
                home(pulseA, pulseB, cross);
                break;
            case CHECK:
                if (cross && solution != null && solution.ok()) {
                    step = Step.DONE;
                }
                break;
            default:
                break;
        }
    }

    /** The CHECK step's live solve, made by the bench with {@link #applyTo} values. */
    void checkSolution(TurretAbsolute.Solution solve) {
        solution = solve;
        if (step == Step.CHECK && solve != null && solve.ok()) {
            worstResidualDeg = Math.max(worstResidualDeg, solve.residualDeg);
        }
    }

    /** Start this step again from nothing. */
    void redo() {
        enter(step == Step.DONE ? Step.CHECK : step);
    }

    /** Back one step. */
    void back() {
        enter(step == Step.BOARD ? Step.BOARD : Step.values()[step.ordinal() - 1]);
    }

    private void enter(Step next) {
        step = next;
        problem = null;
        switch (next) {
            case BOARD:
                boardLoops = 0;
                break;
            case RANGES:
                rangeA.reset();
                rangeB.reset();
                break;
            case DIRECTIONS:
                accA = 0;
                accB = 0;
                lastA = Double.NaN;
                lastB = Double.NaN;
                stillLoops = 0;
                ratioFailed = false;
                armed = false;
                break;
            case HOME:
                homing = false;
                break;
            case CHECK:
                worstResidualDeg = 0;
                break;
            default:
                break;
        }
    }

    // ---------------------------------------------------------------- steps

    private void board(boolean ok, String why, int pulseA, int pulseB) {
        if (!ok) {
            problem = why;
            boardLoops = 0;
        } else if (pulseA <= 0 || pulseB <= 0) {
            problem = "no pulse on " + (pulseA <= 0 ? "A (64T, channel 4)" : "B (44T, channel 5)")
                    + ": absolute wire on the channel's A pin? switch on 3V3?";
            boardLoops = 0;
        } else {
            problem = null;
            if (++boardLoops >= BOARD_LOOPS) enter(Step.RANGES);
        }
    }

    private void ranges(int pulseA, int pulseB) {
        if (pulseA <= 0 || pulseB <= 0) {
            problem = "a pulse dropped out: check the cables";
            return;
        }
        rangeA.add(pulseA);
        rangeB.add(pulseB);
        problem = null;
        if (rangeA.lastStep > SLOW_STEP_US || rangeB.lastStep > SLOW_STEP_US) {
            problem = "a little slower: the ends of the range are easy to miss";
        }
        if (rangeA.done() && rangeB.done()) {
            String bad = spanProblem("A", rangeA);
            if (bad == null) bad = spanProblem("B", rangeB);
            if (bad != null) {
                problem = bad + " Square (X) to start the ranges again.";
                return;
            }
            enter(Step.DIRECTIONS);
        }
    }

    private static String spanProblem(String name, Range r) {
        double span = r.max - r.min;
        if (span < MIN_SPAN_US || span > MAX_SPAN_US) {
            return String.format(Locale.US, "%s's range is %.0f µs, not 1500 to 2200 like a Cypher Max's.", name, span);
        }
        return null;
    }

    private void directions(int pulseA, int pulseB, boolean cross) {
        double a = fraction(pulseA, rangeA);
        double b = fraction(pulseB, rangeB);
        if (Double.isNaN(a) || Double.isNaN(b)) {
            problem = "a pulse is outside the range just found: Back (D-pad left) and find the ranges again";
            return;
        }
        if (ratioFailed) {
            // The problem stays on screen until Cross tries again from here.
            if (cross) enter(Step.DIRECTIONS);
            return;
        }
        problem = null;
        if (!Double.isNaN(lastA)) {
            double da = TurretAbsolute.wrapHalf(a - lastA);
            double db = TurretAbsolute.wrapHalf(b - lastB);
            accA += da;
            accB += db;
            stillLoops = Math.abs(da) < STILL_TURNS && Math.abs(db) < STILL_TURNS ? stillLoops + 1 : 0;
        }
        lastA = a;
        lastB = b;
        if (!armed) {
            // Whatever turning carried on from the ranges step does not count.
            if (stillLoops >= STILL_LOOPS) {
                armed = true;
                accA = 0;
                accB = 0;
                stillLoops = 0;
            }
            return;
        }
        if (turnedDeg() >= DIRECTION_TURN_DEG && stillLoops >= STILL_LOOPS) {
            double measured = Math.abs(accB) / Math.abs(accA);
            double expected = ratioB / ratioA;
            if (Math.abs(measured / expected - 1.0) > RATIO_TOLERANCE) {
                problem = String.format(Locale.US,
                        "B moved %.2f× as far as A; the gears say %.2f×. A and B swapped (channels 4 and 5), or a ratio is wrong. Cross to try again.",
                        measured, expected);
                ratioFailed = true;
                return;
            }
            dirA = accA > 0 ? 1 : -1;
            dirB = accB > 0 ? 1 : -1;
            enter(Step.HOME);
        }
    }

    /** How far the turret has turned in the direction step, degrees, by encoder A. */
    double turnedDeg() {
        return Math.abs(accA) / ratioA * 360.0;
    }

    private void home(int pulseA, int pulseB, boolean cross) {
        double a = fraction(pulseA, rangeA);
        double b = fraction(pulseB, rangeB);
        if (Double.isNaN(a) || Double.isNaN(b)) {
            problem = "a pulse is outside the range found: Back (D-pad left) to the ranges";
            homing = false;
            return;
        }
        if (!homing) {
            if (cross) {
                homing = true;
                problem = null;
                homeLoops = 0;
                homeRefA = a;
                homeRefB = b;
                homeSumA = homeSumB = 0;
                homeLoA = homeLoB = Double.POSITIVE_INFINITY;
                homeHiA = homeHiB = Double.NEGATIVE_INFINITY;
            }
            return;
        }
        double oa = TurretAbsolute.wrapHalf(a - homeRefA);
        double ob = TurretAbsolute.wrapHalf(b - homeRefB);
        homeSumA += oa;
        homeSumB += ob;
        homeLoA = Math.min(homeLoA, oa);
        homeHiA = Math.max(homeHiA, oa);
        homeLoB = Math.min(homeLoB, ob);
        homeHiB = Math.max(homeHiB, ob);
        if (++homeLoops < HOME_LOOPS) return;
        homing = false;
        if (homeHiA - homeLoA > HOME_SPREAD_TURNS || homeHiB - homeLoB > HOME_SPREAD_TURNS) {
            problem = "it moved while measuring: hold it still on the mark, Cross again";
            return;
        }
        homeA = wrap01(homeRefA + homeSumA / HOME_LOOPS);
        homeB = wrap01(homeRefB + homeSumB / HOME_LOOPS);
        enter(Step.CHECK);
    }

    // ---------------------------------------------------------------- results

    /** True once every value is measured (from the CHECK step on). */
    boolean measured() {
        return step == Step.CHECK || step == Step.DONE;
    }

    double minA() { return rangeA.min; }
    double maxA() { return rangeA.max; }
    double minB() { return rangeB.min; }
    double maxB() { return rangeB.max; }
    int dirA() { return dirA; }
    int dirB() { return dirB; }
    double homeA() { return homeA; }
    double homeB() { return homeB; }

    /**
     * The lines to paste into {@code RobotDifferences}, under {@code robot} (an enum constant's
     * name, such as {@code TEAM_19429}), dated {@code date}.
     */
    List<String> pasteLines(String robot, String date) {
        List<String> out = new ArrayList<>();
        out.add("differs(RobotIdentity." + robot + ",");
        out.add("        \"Turret absolute encoders measured on Bench: OctoQuad's guided setup (" + date + ")\",");
        out.add("        () -> {");
        out.add(String.format(Locale.US, "            TurretSubsystem.absPulseMinUsA = %.0f; TurretSubsystem.absPulseMaxUsA = %.0f;", minA(), maxA()));
        out.add(String.format(Locale.US, "            TurretSubsystem.absPulseMinUsB = %.0f; TurretSubsystem.absPulseMaxUsB = %.0f;", minB(), maxB()));
        out.add(String.format(Locale.US, "            TurretSubsystem.absDirectionA = %d; TurretSubsystem.absDirectionB = %d;", dirA, dirB));
        out.add(String.format(Locale.US, "            TurretSubsystem.absHomeA = %.4f; TurretSubsystem.absHomeB = %.4f;", homeA, homeB));
        out.add("            TurretSubsystem.useAbsoluteZero = true;");
        out.add("        });");
        return out;
    }

    // ---------------------------------------------------------------- the page

    String title() {
        switch (step) {
            case BOARD: return "1 of 5 · Check the OctoQuad";
            case RANGES: return "2 of 5 · Each encoder's pulse range";
            case DIRECTIONS: return "3 of 5 · Which way each encoder turns";
            case HOME: return "4 of 5 · The home readings";
            case CHECK: return "5 of 5 · Check";
            default: return "Done";
        }
    }

    /** What to do now, and how far it has got. */
    List<String> lines() {
        List<String> out = new ArrayList<>();
        switch (step) {
            case BOARD:
                out.add("Nothing to do if both channels show a pulse: it moves on by itself.");
                break;
            case RANGES:
                out.add("Turn the turret slowly by hand, about one full turn, either way.");
                out.add(rangeLine("A (64T)", rangeA));
                out.add(rangeLine("B (44T)", rangeB));
                break;
            case DIRECTIONS:
                if (!armed) {
                    out.add("Let go of the turret for a moment…");
                } else {
                    out.add("Now turn the turret COUNTER-CLOCKWISE, seen from above, about 45°, then let go.");
                    out.add(String.format(Locale.US, "Turned %.0f° of %.0f°%s", turnedDeg(), DIRECTION_TURN_DEG,
                            turnedDeg() >= DIRECTION_TURN_DEG ? " · hold still…" : ""));
                }
                break;
            case HOME:
                out.add(homing ? "Measuring: hold still…"
                        : "Put the turret on its home mark, let go, and press Cross (A).");
                break;
            case CHECK:
                out.add("Turn the turret a quarter turn counter-clockwise: it should read about +90°.");
                out.add("(−90°: it was turned clockwise in step 3. D-pad left twice to redo it.)");
                out.add("Then a few turns each way and back onto the mark: about 0°. Cross (A) when it is.");
                if (solution == null) {
                    out.add("Angle: no reading");
                } else if (solution.ok()) {
                    out.add(String.format(Locale.US, "Angle %+.1f° · residual %.1f° (worst %.1f°) · margin %.0f°",
                            solution.turretDeg, solution.residualDeg, worstResidualDeg, solution.marginDeg));
                } else {
                    out.add("Angle: " + solution.problem);
                }
                break;
            default:
                out.add("Paste these into RobotDifferences for this robot (also on Panels, to copy),");
                out.add("and they are applied now until the app restarts: try Bench: Turret Absolute.");
                break;
        }
        return out;
    }

    private static String rangeLine(String name, Range r) {
        if (r.count == 0) return name + ": no pulse yet";
        return String.format(Locale.US, "%s: %d to %d µs · wraps %d of %d%s", name, (int) r.min, (int) r.max,
                Math.min(r.wraps, WRAPS_NEEDED), WRAPS_NEEDED, r.done() ? " ✓" : "");
    }

    // ---------------------------------------------------------------- helpers

    private static double fraction(int pulse, Range r) {
        return TurretAbsolute.fractionFromPulse(pulse, r.min, r.max);
    }

    private static double wrap01(double f) {
        return f - Math.floor(f);
    }

    /** One encoder's running range, its wraps, and the biggest ordinary step between loops. */
    static final class Range {
        double min;
        double max;
        int wraps;
        int count;
        /** This loop's step, µs, unless it was a wrap. */
        int lastStep;
        private int last;

        void reset() {
            min = Double.NaN;
            max = Double.NaN;
            wraps = 0;
            count = 0;
            lastStep = 0;
        }

        Range() {
            reset();
        }

        void add(int pulse) {
            if (count == 0) {
                min = pulse;
                max = pulse;
            } else {
                int step = Math.abs(pulse - last);
                double span = max - min;
                if (span > 500 && step > 0.6 * span) {
                    wraps++;
                    lastStep = 0;
                } else {
                    lastStep = step;
                }
                min = Math.min(min, pulse);
                max = Math.max(max, pulse);
            }
            last = pulse;
            count++;
        }

        boolean done() {
            return wraps >= WRAPS_NEEDED;
        }
    }
}
