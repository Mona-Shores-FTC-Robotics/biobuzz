package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The turret's home switch: a pass across it finds the mark ({@link TurretHomeSwitch}), and the
 * turret puts its zero there ({@link TurretSubsystem}, {@link TurretZeroPolicy#atSwitchPass}).
 */
public class TurretHomeSwitchTest {

    /** 8192 counts per encoder turn, 2.75 encoder turns per turret turn, count rises counter-clockwise. */
    private static final TurretEncoder ENCODER = new TurretEncoder(8192, 2.75, 1);
    private static final double COUNTS_PER_DEG = 8192 * 2.75 / 360.0;
    /** The band: the switch is on from 3° clockwise of the mark to 3° counter-clockwise. */
    private static final double HALF_BAND_DEG = 3.0;

    // ------------------------------------------------------------ the detector

    @Test
    public void aSlowPassFindsTheMarkBothWays() {
        for (int way : new int[] {1, -1}) {
            TurretHomeSwitch home = new TurretHomeSwitch();
            Sweep sweep = new Sweep(home, 0.0);
            TurretHomeSwitch.Result result = sweep.across(-20.0 * way, 20.0 * way, 0.5, 30.0);
            assertEquals("way " + way, TurretHomeSwitch.Result.PASS, result);
            assertEquals("way " + way + ": the mark", 0.0, home.markCounts() / COUNTS_PER_DEG, 0.3);
            assertEquals("way " + way + ": the band", 2 * HALF_BAND_DEG, home.bandDeg(), 0.6);
            assertEquals(way, home.passDirection());
            assertEquals(1, home.passes());
        }
    }

    @Test
    public void theMeasuredOffsetMovesTheMarkOffTheBandsMiddle() {
        TurretHomeSwitch home = new TurretHomeSwitch();
        Sweep sweep = new Sweep(home, 1.5);
        assertEquals(TurretHomeSwitch.Result.PASS, sweep.across(-20.0, 20.0, 0.5, 30.0));
        assertEquals("the mark is 1.5° counter-clockwise of the band's middle",
                1.5, home.markCounts() / COUNTS_PER_DEG, 0.3);
    }

    @Test
    public void turningBackInsideTheBandIsNoPass() {
        TurretHomeSwitch home = new TurretHomeSwitch();
        Sweep sweep = new Sweep(home, 0.0);
        sweep.across(-20.0, 0.0, 0.5, 30.0);
        TurretHomeSwitch.Result result = sweep.across(0.0, -20.0, 0.5, 30.0);
        assertEquals(TurretHomeSwitch.Result.REJECTED, result);
        assertTrue(home.rejectReason(), home.rejectReason().contains("turned back"));
        assertTrue(Double.isNaN(home.markCounts()));
    }

    @Test
    public void aFastPassIsNotTaken() {
        TurretHomeSwitch home = new TurretHomeSwitch();
        Sweep sweep = new Sweep(home, 0.0);
        TurretHomeSwitch.Result result = sweep.across(-20.0, 20.0, 3.0, 150.0);
        assertEquals(TurretHomeSwitch.Result.REJECTED, result);
        assertTrue(home.rejectReason(), home.rejectReason().contains("faster"));
    }

    @Test
    public void aTurretStartingInTheBandHasNoEntryEdge() {
        TurretHomeSwitch home = new TurretHomeSwitch();
        Sweep sweep = new Sweep(home, 0.0);
        // From the mark itself, out one side: no entry was seen, so nothing.
        assertEquals(TurretHomeSwitch.Result.NONE, sweep.across(0.0, 20.0, 0.5, 30.0));
        assertTrue(Double.isNaN(home.markCounts()));
        // Back across, the whole band: a pass.
        assertEquals(TurretHomeSwitch.Result.PASS, sweep.across(20.0, -20.0, 0.5, 30.0));
        assertEquals(0.0, home.markCounts() / COUNTS_PER_DEG, 0.3);
    }

    @Test
    public void aFlickerIsNotTheMagnet() {
        TurretHomeSwitch home = new TurretHomeSwitch();
        long c = 0;
        home.step(false, c, ENCODER, 30.0, 90.0, 0.5, 30.0, 0.0);
        home.step(false, c += 10, ENCODER, 30.0, 90.0, 0.5, 30.0, 0.0);
        home.step(true, c += 10, ENCODER, 30.0, 90.0, 0.5, 30.0, 0.0);
        TurretHomeSwitch.Result result = home.step(false, c + 10, ENCODER, 30.0, 90.0, 0.5, 30.0, 0.0);
        assertEquals(TurretHomeSwitch.Result.REJECTED, result);
        assertTrue(home.rejectReason(), home.rejectReason().contains("flicker"));
    }

    // ------------------------------------------------------------ the turret

    private boolean savedSwitch;
    private int savedDirection;
    private double savedOffset;
    private double savedMax;
    private boolean savedLocked;

    @Before
    public void saveStatics() {
        savedSwitch = TurretSubsystem.homeSwitch;
        savedDirection = TurretSubsystem.direction;
        savedOffset = TurretSubsystem.homeSwitchOffsetDeg;
        savedMax = TurretSubsystem.homeSwitchMaxCorrectionDeg;
        savedLocked = TurretSubsystem.lockedForward;
        TurretSubsystem.lockedForward = false;
        TurretSubsystem.homeSwitch = true;
        TurretSubsystem.direction = 1;
        TurretSubsystem.homeSwitchOffsetDeg = 0.0;
        TurretSubsystem.homeSwitchMaxCorrectionDeg = 30.0;
    }

    @After
    public void restoreStatics() {
        TurretSubsystem.homeSwitch = savedSwitch;
        TurretSubsystem.direction = savedDirection;
        TurretSubsystem.homeSwitchOffsetDeg = savedOffset;
        TurretSubsystem.homeSwitchMaxCorrectionDeg = savedMax;
        TurretSubsystem.lockedForward = savedLocked;
    }

    @Test
    public void turnedByHandAcrossItsMarkInInitTheTurretZeroesItselfAndMovesNothing() {
        Rig rig = new Rig();
        rig.atDeg = 40.0;   // not on its mark
        TurretSubsystem turret = rig.turret();
        turret.initialize();
        assertFalse(turret.zeroed());
        assertFalse(turret.onMark());
        rig.sweep(turret, 40.0, -40.0);
        assertTrue(turret.zeroed());
        assertEquals(TurretSubsystem.ZeroSource.SWITCH, turret.zeroSource());
        turret.update();
        assertEquals("the mark is now 0", -40.0, turret.angleDegrees() + TurretSubsystem.aimOffset(), 0.5);
        assertEquals("nothing written to the servo in INIT", new ArrayList<String>(), rig.servoCalls);
        assertNotNull(turret.takeSwitchEvent());
        assertNull(turret.takeSwitchEvent());
        // At PLAY the switch's zero stands.
        assertEquals(TurretZeroPolicy.Action.NOTHING, TurretZeroPolicy.atPlay(turret.zeroed()));
    }

    @Test
    public void aPassPutsAZeroThatWasOffBackOnTheMark() {
        Rig rig = new Rig();
        rig.atDeg = 5.0;   // 5° off its mark when it was assumed on it
        TurretSubsystem turret = rig.turret();
        turret.initialize();
        turret.zeroAtPlay();
        rig.sweep(turret, 5.0, -30.0);
        assertEquals(TurretSubsystem.ZeroSource.SWITCH, turret.zeroSource());
        assertEquals("the zero moved by the 5° it was off", -5.0, turret.lastSwitchCorrectionDeg(), 0.5);
        turret.update();
        assertEquals(-30.0, turret.angleDegrees() + TurretSubsystem.aimOffset(), 0.5);
    }

    @Test
    public void aTurretWoundATurnRoundKeepsItsWinding() {
        Rig rig = new Rig();
        rig.atDeg = 0.0;
        TurretSubsystem turret = rig.turret();
        turret.initialize();
        turret.zeroAtPlay();
        // A turn counter-clockwise and back across its mark there, which the count by now puts 2° on.
        rig.offsetDeg = 2.0;
        rig.sweep(turret, 380.0, 340.0);
        assertEquals(2.0, turret.lastSwitchCorrectionDeg(), 0.5);
        turret.update();
        assertEquals("still a turn round, not unwound", 340.0 - 2.0 + 0.0,
                turret.angleDegrees() + TurretSubsystem.aimOffset(), 0.6);
    }

    @Test
    public void aZeroSetByHandWinsAndAPassFarOffIsNotTaken() {
        Rig rig = new Rig();
        TurretSubsystem turret = rig.turret();
        turret.initialize();
        turret.zeroHere();
        rig.offsetDeg = 4.0;
        rig.sweep(turret, 20.0, -20.0);
        assertEquals(TurretSubsystem.ZeroSource.HAND, turret.zeroSource());
        assertTrue(turret.takeSwitchEvent().contains("by hand"));

        Rig far = new Rig();
        TurretSubsystem other = far.turret();
        other.initialize();
        other.zeroAtPlay();
        far.offsetDeg = 45.0;
        far.sweep(other, 80.0, 0.0);
        assertEquals(TurretSubsystem.ZeroSource.PLAY, other.zeroSource());
        assertTrue(other.takeSwitchEvent().contains("zero kept"));
    }

    @Test
    public void anAutosInitRemembersWhetherTheMagnetWasThere() {
        Rig rig = new Rig();
        rig.atDeg = 0.0;
        TurretSubsystem on = rig.turret();
        on.initialize();
        on.holdAtInit();
        assertTrue(on.magnetAtInitZero());

        Rig off = new Rig();
        off.atDeg = 12.0;
        TurretSubsystem turret = off.turret();
        turret.initialize();
        turret.holdAtInit();
        assertFalse(turret.magnetAtInitZero());
    }

    // ------------------------------------------------------------ an Auto's INIT sweep

    @Test
    public void anAutosInitSweepFindsTheMarkFromAnywherePreciselyAndHoldsHome() {
        for (double startDeg : new double[] {20.0, -20.0, 1.0, -1.0, -45.0, 170.0, -170.0, 300.0}) {
            Rig rig = new Rig();
            rig.atDeg = startDeg;
            // Turns on 2.5° from the mark, off 3.5° from it: a one-way pass is 0.5° off.
            rig.hysteresisDeg = 1.0;
            TurretSubsystem turret = rig.turret();
            turret.useClock(() -> rig.nowNs);
            turret.initialize();
            turret.holdAtInit();
            assertEquals(TurretHomeSweep.State.SEARCH, turret.sweepState());
            rig.run(turret, 25.0);
            assertEquals("from " + startDeg, TurretHomeSweep.State.FOUND, turret.sweepState());
            assertEquals("from " + startDeg, TurretSubsystem.ZeroSource.SWITCH, turret.zeroSource());
            assertEquals("from " + startDeg + ": the zero is on the mark",
                    0.0, TurretEncoder.wrap180(rig.atDeg - turret.angleDegrees()), 0.2);
            assertEquals("from " + startDeg + ": held on its mark", 0.0, TurretEncoder.wrap180(rig.atDeg), 3.0);
            assertEquals("from " + startDeg + ": unwound", 0.0, turret.angleDegrees(), 3.0);
            assertNull(turret.autoInitMarkProblem());
            String events = turret.takeSwitchEvent();
            assertTrue(events, events.contains("the sweep found the mark"));
        }
    }

    @Test
    public void the19429SweepWithItsOwnValues() {
        int direction = TurretSubsystem.direction;
        double offset = TurretSubsystem.aimOffsetDeg, floor = TurretSubsystem.minPower, kP = TurretSubsystem.kP;
        try {
            TurretSubsystem.direction = -1;
            TurretSubsystem.aimOffsetDeg = 5.0;
            TurretSubsystem.minPower = 0.065;
            TurretSubsystem.kP = 0.008;
            for (double startDeg : new double[] {0.0, 20.0, -20.0, 170.0}) {
                Rig rig = new Rig();
                rig.atDeg = startDeg;
                TurretSubsystem turret = rig.turret();
                turret.useClock(() -> rig.nowNs);
                turret.initialize();
                turret.holdAtInit();
                rig.run(turret, 25.0);
                assertEquals("from " + startDeg + ": " + turret.takeSwitchEvent(),
                        TurretHomeSweep.State.FOUND, turret.sweepState());
                assertEquals("from " + startDeg + ": held 5° round from the mark", 5.0,
                        TurretEncoder.wrap180(rig.atDeg), 3.0);
            }
        } finally {
            TurretSubsystem.direction = direction;
            TurretSubsystem.aimOffsetDeg = offset;
            TurretSubsystem.minPower = floor;
            TurretSubsystem.kP = kP;
        }
    }

    @Test
    public void aSweepThatFindsNoMagnetHoldsWhereTheTurretStoodAndWarns() {
        Rig rig = new Rig();
        rig.atDeg = 100.0;
        rig.noMagnet = true;
        TurretSubsystem turret = rig.turret();
        turret.useClock(() -> rig.nowNs);
        turret.initialize();
        turret.holdAtInit();
        rig.run(turret, 25.0);
        assertEquals(TurretHomeSweep.State.NOT_FOUND, turret.sweepState());
        assertEquals(TurretSubsystem.ZeroSource.INIT, turret.zeroSource());
        assertEquals("back where it stood", 0.0, TurretEncoder.wrap180(rig.atDeg - 100.0), 3.0);
        assertEquals("the sweep did not find the magnet", turret.autoInitMarkProblem());
    }

    @Test
    public void playStopsASweepStillLookingAndATeleOpNeverSweeps() {
        Rig rig = new Rig();
        rig.atDeg = 100.0;
        TurretSubsystem turret = rig.turret();
        turret.useClock(() -> rig.nowNs);
        turret.initialize();
        turret.holdAtInit();
        rig.run(turret, 1.0);
        turret.start();
        assertEquals(TurretHomeSweep.State.STOPPED, turret.sweepState());

        Rig teleOp = new Rig();
        teleOp.atDeg = 20.0;
        TurretSubsystem other = teleOp.turret();
        other.initialize();
        teleOp.run(other, 5.0);
        assertEquals(TurretHomeSweep.State.IDLE, other.sweepState());
        assertEquals("a TeleOp's INIT writes nothing", new ArrayList<String>(), teleOp.servoCalls);
        assertEquals(20.0, teleOp.atDeg, 1e-9);
    }

    @Test
    public void withTheSweepOffAnAutosInitOnlyChecksTheSwitch() {
        boolean was = TurretSubsystem.homeSweep;
        try {
            TurretSubsystem.homeSweep = false;
            Rig rig = new Rig();
            rig.atDeg = 12.0;
            TurretSubsystem turret = rig.turret();
            turret.initialize();
            turret.holdAtInit();
            assertEquals(TurretHomeSweep.State.IDLE, turret.sweepState());
            assertEquals("zeroed with no magnet over the switch", turret.autoInitMarkProblem());
        } finally {
            TurretSubsystem.homeSweep = was;
        }
    }

    // ------------------------------------------------------------ plumbing

    /** Feeds a detector a turret turning steadily, the switch on within the band of {@code 0}. */
    private static final class Sweep {
        private final TurretHomeSwitch home;
        private final double offsetDeg;

        Sweep(TurretHomeSwitch home, double offsetDeg) {
            this.home = home;
            this.offsetDeg = offsetDeg;
        }

        /** From one angle to another in steps; the last non-NONE result. */
        TurretHomeSwitch.Result across(double fromDeg, double toDeg, double stepDeg, double rateDps) {
            double sign = Math.signum(toDeg - fromDeg);
            TurretHomeSwitch.Result last = TurretHomeSwitch.Result.NONE;
            for (double deg = fromDeg; sign * (toDeg - deg) >= -1e-9; deg += sign * stepDeg) {
                TurretHomeSwitch.Result r = home.step(Math.abs(deg) <= HALF_BAND_DEG, Math.round(deg * COUNTS_PER_DEG),
                        ENCODER, sign * rateDps, 90.0, 0.5, 30.0, offsetDeg);
                if (r != TurretHomeSwitch.Result.NONE) last = r;
            }
            return last;
        }
    }

    /** A turret on fakes: an encoder host whose count follows {@link #atDeg}, a servo, a switch. */
    private static final class Rig {
        /** Where the turret really is, degrees from its mark. */
        double atDeg;
        /** Where the magnet's band sits, degrees from where the count says the mark is (for a drift). */
        double offsetDeg;
        double rateDps;
        final List<String> servoCalls = new ArrayList<>();
        /** The switch turns on this much closer to the mark than it turns off, degrees. */
        double hysteresisDeg;
        boolean noMagnet;
        private boolean on;
        /** What the servo was last sent; NaN limp. */
        double servoPower = Double.NaN;
        long nowNs;

        /**
         * Runs the turret for {@code seconds} as a plant: the servo's power, less its stop, at
         * {@code topSpeedDps} per unit, turns it. 20 ms loops on the turret's own clock.
         */
        void run(TurretSubsystem turret, double seconds) {
            turret.useClock(() -> nowNs);
            for (int i = 0; i < seconds / 0.02; i++) {
                nowNs += 20_000_000L;
                double ccw = Double.isNaN(servoPower) ? 0.0
                        : (servoPower - TurretSubsystem.servoStopPower) * TurretSubsystem.servoDirection;
                rateDps = ccw * TurretSubsystem.topSpeedDps;
                atDeg += rateDps * 0.02;
                turret.update();
            }
        }

        TurretSubsystem turret() {
            Map<String, Object> fakes = new HashMap<>();
            fakes.put(DeviceNames.TURRET_ENCODER_HOST, proxy(DcMotorEx.class, (m, args) -> {
                switch (m) {
                    case "getCurrentPosition": return (int) Math.round(TurretSubsystem.direction * atDeg * COUNTS_PER_DEG);
                    case "getVelocity": return TurretSubsystem.direction * rateDps * COUNTS_PER_DEG;
                    case "getDirection": return DcMotorSimple.Direction.FORWARD;
                    default: return null;
                }
            }));
            fakes.put(DeviceNames.TURRET, proxy(CRServo.class, (m, args) -> {
                if (m.startsWith("set")) servoCalls.add(m);
                if (m.equals("setPower")) servoPower = (Double) args[0];
                if (m.equals("setPwmDisable")) servoPower = Double.NaN;
                return null;
            }, PwmControl.class));
            fakes.put(DeviceNames.TURRET_HOME, proxy(DigitalChannel.class, (m, args) -> {
                // Active low: false with the magnet over it.
                if (m.equals("getState")) {
                    double from = Math.abs(TurretEncoder.wrap180(atDeg - offsetDeg));
                    on = !noMagnet && from <= HALF_BAND_DEG + (on ? 1 : -1) * hysteresisDeg / 2.0;
                    return !on;
                }
                return null;
            }));
            HardwareMap map = new HardwareMap(null, null) {
                @Override
                public <T> T get(Class<? extends T> type, String name) {
                    return type.cast(fakes.get(name));
                }
            };
            return new TurretSubsystem(map);
        }

        /** Turned slowly from one angle to the other, one update a step. */
        void sweep(TurretSubsystem turret, double fromDeg, double toDeg) {
            double sign = Math.signum(toDeg - fromDeg);
            rateDps = sign * 30.0;
            for (double deg = fromDeg; sign * (toDeg - deg) >= -1e-9; deg += sign * 0.5) {
                atDeg = deg;
                turret.update();
            }
            rateDps = 0.0;
        }

        interface Handler {
            Object call(String method, Object[] args);
        }

        private static Object proxy(Class<?> type, Handler handler, Class<?>... more) {
            Class<?>[] types = new Class<?>[more.length + 1];
            types[0] = type;
            System.arraycopy(more, 0, types, 1, more.length);
            return Proxy.newProxyInstance(TurretHomeSwitchTest.class.getClassLoader(), types, (p, method, args) -> {
                Object value = handler.call(method.getName(), args);
                if (value != null) return value;
                Class<?> r = method.getReturnType();
                if (r == boolean.class) return false;
                if (r == int.class) return 0;
                if (r == long.class) return 0L;
                if (r == double.class) return 0.0;
                if (r == float.class) return 0.0f;
                return null;
            });
        }
    }
}
