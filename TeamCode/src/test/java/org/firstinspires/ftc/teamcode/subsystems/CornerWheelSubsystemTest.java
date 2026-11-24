package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.subsystems.CornerWheelSubsystem.Why;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The corner wheels' stop rules ({@link CornerWheelSubsystem#stopFor}): they rake only after PLAY, while the
 * OpMode's intake takes in, with fewer than 4 held (G407), no wrong NECTAR in or going out (G408), and the
 * FLOWER stick up (G418). Nothing moving before PLAY is {@code InitMovesNothingTest}'s.
 */
public class CornerWheelSubsystemTest {

    private boolean fitted;
    private boolean rightServo;
    private double power;
    private boolean leftReversed;
    private boolean rightReversed;

    @Before
    public void remember() {
        fitted = CornerWheelSubsystem.fitted;
        rightServo = CornerWheelSubsystem.rightServo;
        power = CornerWheelSubsystem.power;
        leftReversed = CornerWheelSubsystem.leftReversed;
        rightReversed = CornerWheelSubsystem.rightReversed;
    }

    @After
    public void restore() {
        CornerWheelSubsystem.fitted = fitted;
        CornerWheelSubsystem.rightServo = rightServo;
        CornerWheelSubsystem.power = power;
        CornerWheelSubsystem.leftReversed = leftReversed;
        CornerWheelSubsystem.rightReversed = rightReversed;
    }

    // ------------------------------------------------------------ the rules, pure

    @Test
    public void rakesOnlyWithEveryRuleClear() {
        assertEquals(Why.RAKING, CornerWheelSubsystem.stopFor(true, true, false, 0, false, false));
        assertEquals(Why.RAKING, CornerWheelSubsystem.stopFor(true, true, false, 3, false, false));
    }

    @Test
    public void nothingBeforePlay() {
        assertEquals(Why.BEFORE_PLAY, CornerWheelSubsystem.stopFor(false, true, false, 0, false, false));
    }

    @Test
    public void onlyWhileTheIntakeTakesIn() {
        assertEquals(Why.INTAKE_NOT_TAKING_IN, CornerWheelSubsystem.stopFor(true, false, false, 0, false, false));
    }

    @Test
    public void stopAtFourHeld() {
        assertEquals("the lane reads full", Why.HOLDS_FOUR,
                CornerWheelSubsystem.stopFor(true, true, true, 0, false, false));
        assertEquals("the tracker counts 4", Why.HOLDS_FOUR,
                CornerWheelSubsystem.stopFor(true, true, false, 4, false, false));
        assertEquals("or more", Why.HOLDS_FOUR,
                CornerWheelSubsystem.stopFor(true, true, false, 5, false, false));
    }

    @Test
    public void stopWhileAWrongNectarMayBeIn() {
        assertEquals(Why.WRONG_NECTAR, CornerWheelSubsystem.stopFor(true, true, false, 0, true, false));
    }

    @Test
    public void stopWhileTheFlowerStickIsDown() {
        assertEquals(Why.STICK_DOWN, CornerWheelSubsystem.stopFor(true, true, false, 0, false, true));
    }

    @Test
    public void everyStopWinsOverRaking() {
        for (int mask = 0; mask < 32; mask++) {
            boolean started = (mask & 1) != 0;
            boolean takingIn = (mask & 2) != 0;
            boolean full = (mask & 4) != 0;
            boolean wrong = (mask & 8) != 0;
            boolean stick = (mask & 16) != 0;
            boolean rakes = started && takingIn && !full && !wrong && !stick;
            assertEquals("mask " + mask, rakes,
                    CornerWheelSubsystem.stopFor(started, takingIn, full, 0, wrong, stick) == Why.RAKING);
        }
    }

    @Test
    public void powerIsClampedAndStoppedIsAPlainZero() {
        assertEquals(1.0, CornerWheelSubsystem.powerFor(true, 1.0, false), 0.0);
        assertEquals(-1.0, CornerWheelSubsystem.powerFor(true, 1.0, true), 0.0);
        assertEquals(1.0, CornerWheelSubsystem.powerFor(true, 7.0, false), 0.0);
        assertEquals(0.5, CornerWheelSubsystem.powerFor(true, 0.5, false), 0.0);
        assertEquals(0.0, CornerWheelSubsystem.powerFor(true, Double.NaN, false), 0.0);
        assertEquals(0.0, CornerWheelSubsystem.powerFor(true, -1.0, true), 0.0);
        double stopped = CornerWheelSubsystem.powerFor(false, 1.0, true);
        assertEquals("never -0", Double.doubleToRawLongBits(0.0), Double.doubleToRawLongBits(stopped));
    }

    @Test
    public void theTwoWheelsTurnOppositeWaysByDefault() {
        CornerWheelSubsystem.leftReversed = false;
        CornerWheelSubsystem.rightReversed = true;
        assertTrue(CornerWheelSubsystem.powerFor(true, 1.0, CornerWheelSubsystem.leftReversed)
                * CornerWheelSubsystem.powerFor(true, 1.0, CornerWheelSubsystem.rightReversed) < 0);
    }

    // ------------------------------------------------------------ the subsystem, on fakes

    @Test
    public void eachStopWritesZeroToBothServos() {
        CornerWheelSubsystem.fitted = true;
        CornerWheelSubsystem.rightServo = true;
        Fakes fakes = new Fakes(DeviceNames.CORNER_WHEEL_LEFT, DeviceNames.CORNER_WHEEL_RIGHT);
        CornerWheelSubsystem wheels = new CornerWheelSubsystem(fakes.map);
        boolean[] takingIn = {true};
        boolean[] full = {false};
        int[] held = {0};
        boolean[] wrong = {false};
        boolean[] stick = {false};
        wheels.watch(() -> takingIn[0], () -> full[0], () -> held[0], () -> wrong[0], () -> stick[0]);
        wheels.initialize();
        wheels.start();

        wheels.update();
        assertEquals(Why.RAKING, wheels.why());
        assertTrue(fakes.last(DeviceNames.CORNER_WHEEL_LEFT) != 0.0);
        assertTrue(fakes.last(DeviceNames.CORNER_WHEEL_RIGHT) != 0.0);

        held[0] = 4;
        assertStopped(wheels, fakes, Why.HOLDS_FOUR);
        held[0] = 0;
        full[0] = true;
        assertStopped(wheels, fakes, Why.HOLDS_FOUR);
        full[0] = false;
        wrong[0] = true;
        assertStopped(wheels, fakes, Why.WRONG_NECTAR);
        wrong[0] = false;
        stick[0] = true;
        assertStopped(wheels, fakes, Why.STICK_DOWN);
        stick[0] = false;
        takingIn[0] = false;
        assertStopped(wheels, fakes, Why.INTAKE_NOT_TAKING_IN);

        takingIn[0] = true;
        wheels.update();
        assertEquals("all clear again: raking again", Why.RAKING, wheels.why());

        wheels.stop();
        assertEquals(0.0, fakes.last(DeviceNames.CORNER_WHEEL_LEFT), 0.0);
        assertEquals(0.0, fakes.last(DeviceNames.CORNER_WHEEL_RIGHT), 0.0);
    }

    @Test
    public void unwiredItNeverRakes() {
        CornerWheelSubsystem.fitted = true;
        Fakes fakes = new Fakes(DeviceNames.CORNER_WHEEL_LEFT, DeviceNames.CORNER_WHEEL_RIGHT);
        CornerWheelSubsystem wheels = new CornerWheelSubsystem(fakes.map);
        wheels.initialize();
        wheels.start();
        wheels.update();
        assertEquals(Why.INTAKE_NOT_TAKING_IN, wheels.why());
        assertEquals(0.0, fakes.last(DeviceNames.CORNER_WHEEL_LEFT), 0.0);
    }

    @Test
    public void oneServoOnlyAsksForTheLeft() {
        CornerWheelSubsystem.fitted = true;
        CornerWheelSubsystem.rightServo = false;
        Fakes fakes = new Fakes(DeviceNames.CORNER_WHEEL_LEFT);
        CornerWheelSubsystem wheels = new CornerWheelSubsystem(fakes.map);
        assertTrue(wheels.present());
        assertEquals(Collections.singletonList(DeviceNames.CORNER_WHEEL_LEFT), fakes.askedFor);
    }

    @Test
    public void notFittedAsksForNothing() {
        CornerWheelSubsystem.fitted = false;
        Fakes fakes = new Fakes(DeviceNames.CORNER_WHEEL_LEFT, DeviceNames.CORNER_WHEEL_RIGHT);
        CornerWheelSubsystem wheels = new CornerWheelSubsystem(fakes.map);
        assertTrue(fakes.askedFor.isEmpty());
        assertEquals("corner wheels not fitted", wheels.health());
    }

    @Test
    public void fittedWithoutTheServoFailsLoudly() {
        CornerWheelSubsystem.fitted = true;
        CornerWheelSubsystem.rightServo = true;
        Fakes fakes = new Fakes(DeviceNames.CORNER_WHEEL_LEFT);
        try {
            new CornerWheelSubsystem(fakes.map);
            fail("a switch on with no servo in the config must not run without it");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(DeviceNames.CORNER_WHEEL_RIGHT));
        }
    }

    @Test
    public void checkHardwareKnowsTheSwitches() {
        CornerWheelSubsystem.fitted = true;
        CornerWheelSubsystem.rightServo = false;
        assertTrue(CornerWheelSubsystem.fitted(DeviceNames.CORNER_WHEEL_LEFT));
        assertEquals(false, CornerWheelSubsystem.fitted(DeviceNames.CORNER_WHEEL_RIGHT));
        assertTrue(DeviceNames.COMPETITION_OPTIONAL.containsAll(DeviceNames.CORNER_WHEELS));
    }

    private static void assertStopped(CornerWheelSubsystem wheels, Fakes fakes, Why why) {
        wheels.update();
        assertEquals(why, wheels.why());
        assertEquals(why.label, 0.0, fakes.last(DeviceNames.CORNER_WHEEL_LEFT), 0.0);
        assertEquals(why.label, 0.0, fakes.last(DeviceNames.CORNER_WHEEL_RIGHT), 0.0);
    }

    /** CR servos that remember their last power, behind a map that answers only {@code tryGet}. */
    private static final class Fakes {
        final Map<String, Double> power = new HashMap<>();
        final List<String> askedFor = new ArrayList<>();
        final Map<String, CRServo> servos = new HashMap<>();
        final HardwareMap map = new HardwareMap(null, null) {
            @Override
            public <T> T tryGet(Class<? extends T> type, String name) {
                askedFor.add(name);
                CRServo servo = servos.get(name);
                return servo == null ? null : type.cast(servo);
            }
        };

        Fakes(String... names) {
            for (String name : names) {
                servos.put(name, (CRServo) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class<?>[] {CRServo.class}, (proxy, method, args) -> {
                            if (method.getName().equals("setPower")) power.put(name, (Double) args[0]);
                            Class<?> t = method.getReturnType();
                            if (t == double.class) return 0.0;
                            if (t == int.class) return 0;
                            if (t == boolean.class) return false;
                            return null;
                        }));
            }
        }

        double last(String name) {
            Double p = power.get(name);
            assertTrue(name + " was never written", p != null);
            return p;
        }
    }
}
