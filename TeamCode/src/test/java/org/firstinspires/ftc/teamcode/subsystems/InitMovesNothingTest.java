package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Nothing moves before PLAY ({@link Subsystem}): no servo is written and no motor gets power but
 * zero from {@code initialize()} through any number of INIT loops, and the mechanisms do take over
 * once {@code start()} runs. Competition Manual G304.H (motionless once INIT is done) and G403 (no
 * powered movement between AUTO and TELEOP, when the TeleOp is INIT'd).
 *
 * <p>The one exception is an Autonomous's INIT ({@code Robot.holdForAutoInit()}, R103.B): the FLOWER
 * stick held in, the gate held closed and the turret zeroed and held at its home angle. Those tests run it, and
 * {@link #onlyAnAutonomousInitHoldsAnything()} checks that nothing else can ask for it.
 *
 * <p>Each subsystem runs on recording fakes of its devices. A subsystem that looks up hardware is
 * either run here or named in {@link #NOT_RUN_HERE} with the reason, so a new mechanism cannot slip
 * past by not being listed.
 */
public class InitMovesNothingTest {

    /** INIT loops to run: a few seconds' worth, past the FLOWER stick's settle time. */
    private static final int INIT_LOOPS = 200;

    /** Subsystems that look up hardware but are not run here, and why that is safe. */
    private static final Map<String, String> NOT_RUN_HERE = new HashMap<>();

    static {
        NOT_RUN_HERE.put("IndicatorSubsystem", "a light on a servo port: it moves nothing, so it lights during INIT");
        NOT_RUN_HERE.put("LimelightVisionSubsystem", "a camera");
        NOT_RUN_HERE.put("ColorSensorSubsystem", "a sensor: it reads and moves nothing");
        NOT_RUN_HERE.put("PieceVisionSubsystem", "a camera");
        NOT_RUN_HERE.put("DriveSubsystem", "needs the Pinpoint and Pedro to build; its wheels turn only on drive(), "
                + "follow() or hold(), which an OpMode calls only after PLAY (bindings and commands start at PLAY)");
    }

    // ------------------------------------------------------------ the mechanisms

    @Test
    public void flowerStickWaitsForPlay() {
        Devices devices = new Devices().servo(DeviceNames.FLOWER_STICK);
        FlowerStickSubsystem stick = new FlowerStickSubsystem(devices.map, null);
        runInit(stick, devices);

        stick.start();
        stick.update();
        assertEquals("at PLAY the stick is taken and held in",
                Arrays.asList("setPosition(" + FlowerStickSubsystem.unitRange(FlowerStickSubsystem.RETRACTED) + ")",
                        "setPwmEnable()"),
                devices.calls(DeviceNames.FLOWER_STICK));
    }

    @Test
    public void flowerStickHeldInDuringAnAutosInit() {
        boolean was = FlowerStickSubsystem.roller;
        FlowerStickSubsystem.roller = true;
        try {
            Devices devices = new Devices().servo(DeviceNames.FLOWER_STICK).crServo(DeviceNames.FLOWER_ROLLER);
            FlowerStickSubsystem stick = new FlowerStickSubsystem(devices.map, null);
            stick.initialize();
            stick.holdAtInit();
            for (int i = 0; i < INIT_LOOPS; i++) {
                stick.update();
            }
            String in = "setPosition(" + FlowerStickSubsystem.unitRange(FlowerStickSubsystem.RETRACTED) + ")";
            List<String> calls = devices.calls(DeviceNames.FLOWER_STICK);
            assertEquals("the pulse is switched on once", in, calls.get(0));
            assertEquals("setPwmEnable()", calls.get(1));
            for (String call : calls.subList(2, calls.size())) {
                assertEquals("held in through INIT, never anywhere else", in, call);
            }
            assertTrue("the roller is not written before PLAY", devices.calls(DeviceNames.FLOWER_ROLLER).isEmpty());

            devices.clear();
            stick.start();
            stick.update();
            assertEquals("at PLAY it stays in", Arrays.asList(in), devices.calls(DeviceNames.FLOWER_STICK));

            devices.clear();
            stick.initialize();
            for (int i = 0; i < 10; i++) {
                stick.update();
            }
            assertTrue("initialize() clears the hold, so the next OpMode's INIT moves nothing",
                    devices.movingWrites().isEmpty());
        } finally {
            FlowerStickSubsystem.roller = was;
        }
    }

    @Test
    public void flowerRollerWaitsForPlayAndSpinsOnlyWhileTheStickIsDown() {
        boolean was = FlowerStickSubsystem.roller;
        FlowerStickSubsystem.roller = true;
        try {
            Devices devices = new Devices().servo(DeviceNames.FLOWER_STICK).crServo(DeviceNames.FLOWER_ROLLER);
            FlowerStickSubsystem stick = new FlowerStickSubsystem(devices.map, null);
            runInit(stick, devices);

            stick.start();
            stick.update();
            assertEquals("at PLAY, stick in, the roller is stopped",
                    Arrays.asList("setPower(0.0)"), devices.calls(DeviceNames.FLOWER_ROLLER));

            double spin = FlowerStickSubsystem.rollerPowerFor(true, FlowerStickSubsystem.rollerPower,
                    FlowerStickSubsystem.rollerReversed);
            assertTrue("the default roller power turns it", spin != 0.0);
            devices.clear();
            stick.extend();
            stick.update();
            assertEquals("asked down, the roller spins from the request on",
                    Arrays.asList("setPower(" + spin + ")"), devices.calls(DeviceNames.FLOWER_ROLLER));

            devices.clear();
            stick.retractAfter(60_000);
            stick.update();
            assertEquals("still down while it waits to come in, so still spinning",
                    Arrays.asList("setPower(" + spin + ")"), devices.calls(DeviceNames.FLOWER_ROLLER));

            devices.clear();
            stick.retract();
            stick.update();
            assertEquals("asked up, the roller stops",
                    Arrays.asList("setPower(0.0)"), devices.calls(DeviceNames.FLOWER_ROLLER));
        } finally {
            FlowerStickSubsystem.roller = was;
        }
    }

    @Test
    public void turretWaitsForPlay() {
        Devices devices = new Devices().motor(TurretSubsystem.encoderHostName()).crServo(DeviceNames.TURRET);
        TurretSubsystem turret = new TurretSubsystem(devices.map);
        runInit(turret, devices);

        turret.start();
        turret.update();
        assertEquals("at PLAY, not zeroed yet, the turret's servo is still not written",
                Arrays.asList(), devices.calls(DeviceNames.TURRET));

        turret.zeroAtPlay();
        turret.update();
        assertEquals("zeroed on its mark, the turret is held there: no error, so the stop power",
                Arrays.asList("setPower(" + TurretSubsystem.pwmStep(TurretSubsystem.servoStopPower) + ")",
                        "setPwmEnable()"),
                devices.calls(DeviceNames.TURRET));
    }

    @Test
    public void turretZeroedAndHeldAtItsHomeAngleDuringAnAutosInit() {
        double offset = TurretSubsystem.aimOffsetDeg;
        boolean locked = TurretSubsystem.lockedForward;
        try {
            for (boolean lockedForward : new boolean[] {false, true}) {
                TurretSubsystem.lockedForward = lockedForward;
                TurretSubsystem.aimOffsetDeg = 6.0;
                Devices devices = new Devices().motor(TurretSubsystem.encoderHostName()).crServo(DeviceNames.TURRET);
                TurretSubsystem turret = new TurretSubsystem(devices.map);
                turret.initialize();
                turret.holdAtInit();
                assertTrue(turret.zeroed());
                assertEquals(TurretSubsystem.ZeroSource.INIT, turret.zeroSource());
                for (int i = 0; i < INIT_LOOPS; i++) {
                    turret.update();
                }
                List<String> calls = devices.calls(DeviceNames.TURRET);
                assertFalse("locked " + lockedForward + ": held at INIT, so the servo is written", calls.isEmpty());
                assertTrue("locked " + lockedForward + ": the pulse is switched on", calls.contains("setPwmEnable()"));
                // On its mark the turret reads -6 from its home angle (6 round from the mark): it is
                // driven counter-clockwise toward it, not left at the stop.
                double first = Double.parseDouble(calls.get(0).substring("setPower(".length(), calls.get(0).length() - 1));
                double stop = TurretSubsystem.pwmStep(TurretSubsystem.servoStopPower);
                int sign = TurretSubsystem.servoDirection == -1 ? -1 : 1;
                assertTrue("locked " + lockedForward + ": driven toward the offset, not at the stop: " + calls.get(0),
                        (first - stop) * sign > 0.0);

                // PLAY keeps the INIT zero: the handoff to the TeleOp carries it.
                long zero = turret.zeroCounts();
                turret.start();
                turret.zeroAtPlay();
                assertEquals(TurretSubsystem.ZeroSource.INIT, turret.zeroSource());
                assertEquals(zero, turret.zeroCounts());

                // A person's zero still wins.
                turret.zeroHere();
                assertEquals(TurretSubsystem.ZeroSource.HAND, turret.zeroSource());

                // The next OpMode's INIT (a TeleOp's) holds nothing: initialize() clears the hold.
                devices.clear();
                runInit(turret, devices);
                assertFalse(turret.zeroed());
            }
        } finally {
            TurretSubsystem.aimOffsetDeg = offset;
            TurretSubsystem.lockedForward = locked;
        }
    }

    @Test
    public void intakeStarsWaitForPlay() {
        Devices devices = new Devices().motor(DeviceNames.INTAKE).crServo(DeviceNames.INTAKE_STARS)
                .motor(DeviceNames.FEEDER);
        IntakeSubsystem intake = new IntakeSubsystem(devices.map, new FeederSubsystem(devices.map));
        runInit(intake, devices);

        intake.start();
        intake.update();
        assertEquals("at PLAY the star wheels are written, still stopped",
                "setPower(0.0)", devices.calls(DeviceNames.INTAKE_STARS).get(0));
    }

    @Test
    public void rightStarWheelWaitsForPlayAndRunsWithTheLeft() {
        boolean was = IntakeSubsystem.rightStarWheel;
        IntakeSubsystem.rightStarWheel = true;
        try {
            Devices devices = new Devices().motor(DeviceNames.INTAKE).crServo(DeviceNames.INTAKE_STARS)
                    .crServo(DeviceNames.INTAKE_STARS_RIGHT).motor(DeviceNames.FEEDER);
            IntakeSubsystem intake = new IntakeSubsystem(devices.map, new FeederSubsystem(devices.map));
            runInit(intake, devices);

            intake.start();
            intake.intake();
            intake.update();
            assertEquals("at PLAY the right star wheel turns inward with the left",
                    "setPower(" + IntakeSubsystem.starsPowerFor(IntakeSubsystem.State.IN,
                            IntakeSubsystem.starsPower, IntakeSubsystem.starsRightReversed) + ")",
                    devices.calls(DeviceNames.INTAKE_STARS_RIGHT).get(0));
        } finally {
            IntakeSubsystem.rightStarWheel = was;
        }
    }

    @Test
    public void cornerWheelsWaitForPlayEvenWithEveryRuleSayingRake() {
        boolean was = CornerWheelSubsystem.fitted;
        CornerWheelSubsystem.fitted = true;
        try {
            Devices devices = new Devices().crServo(DeviceNames.CORNER_WHEEL_LEFT).crServo(DeviceNames.CORNER_WHEEL_RIGHT);
            CornerWheelSubsystem wheels = new CornerWheelSubsystem(devices.map);
            // Taking in, the lane empty, nothing wrong, the stick up: all that stops them is PLAY.
            wheels.watch(() -> true, () -> false, () -> 0, () -> false, () -> false);
            runInit(wheels, devices);

            wheels.start();
            wheels.update();
            assertEquals("at PLAY the left wheel rakes",
                    Arrays.asList("setPower(" + CornerWheelSubsystem.powerFor(true, CornerWheelSubsystem.power,
                            CornerWheelSubsystem.leftReversed) + ")"),
                    devices.calls(DeviceNames.CORNER_WHEEL_LEFT));
            assertEquals("and the right one, its own way",
                    Arrays.asList("setPower(" + CornerWheelSubsystem.powerFor(true, CornerWheelSubsystem.power,
                            CornerWheelSubsystem.rightReversed) + ")"),
                    devices.calls(DeviceNames.CORNER_WHEEL_RIGHT));
        } finally {
            CornerWheelSubsystem.fitted = was;
        }
    }

    @Test
    public void cornerWheelsNotFittedLookUpAndWriteNothing() {
        Devices devices = new Devices();
        CornerWheelSubsystem wheels = new CornerWheelSubsystem(devices.map);
        wheels.watch(() -> true, () -> false, () -> 0, () -> false, () -> false);
        runInit(wheels, devices);
        wheels.start();
        wheels.update();
        assertFalse(wheels.present());
        assertEquals(CornerWheelSubsystem.Why.NOT_FITTED, wheels.why());
    }

    @Test
    public void intakeJoinsAFeedOnlyAfterPlay() {
        Devices devices = new Devices().motor(DeviceNames.INTAKE).crServo(DeviceNames.INTAKE_STARS)
                .motor(DeviceNames.FEEDER);
        FeederSubsystem feeder = new FeederSubsystem(devices.map);
        feeder.initialize();
        devices.clear();
        feeder.feed();
        IntakeSubsystem intake = new IntakeSubsystem(devices.map, feeder);
        runInit(intake, devices);
        assertFalse("before PLAY a feed is not an intake request", intake.feedAssist());

        intake.start();
        intake.update();
        assertEquals("at PLAY the roller joins the feed", IntakeSubsystem.State.IN, intake.state());
        assertEquals("the OpMode asked for nothing", IntakeSubsystem.State.IDLE, intake.requested());
        assertTrue(intake.feedAssist());
        assertEquals("setPower(" + IntakeSubsystem.powerFor(IntakeSubsystem.State.IN,
                        IntakeSubsystem.inPower, IntakeSubsystem.outPower) + ")",
                devices.calls(DeviceNames.INTAKE).get(0));
        assertEquals("setPower(" + IntakeSubsystem.starsPowerFor(IntakeSubsystem.State.IN,
                        IntakeSubsystem.starsPower, IntakeSubsystem.starsReversed) + ")",
                devices.calls(DeviceNames.INTAKE_STARS).get(0));
    }

    @Test
    public void prefeedWaitsForPlayEvenWhileTheIntakeIsAskingForIt() {
        Devices devices = new Devices().motor(DeviceNames.INTAKE).crServo(DeviceNames.INTAKE_STARS)
                .motor(DeviceNames.FEEDER).crServo(DeviceNames.PREFEED);
        FeederSubsystem feeder = new FeederSubsystem(devices.map);
        IntakeSubsystem intake = new IntakeSubsystem(devices.map, feeder);
        feeder.initialize();
        intake.initialize();
        devices.clear();
        intake.intake();
        PrefeedSubsystem prefeed = new PrefeedSubsystem(devices.map, intake, feeder);
        runInit(prefeed, devices);

        // PLAY as Robot does it: every subsystem started, then updated in list order, the intake
        // before the prefeed, so the prefeed follows what the roller is doing this loop.
        intake.start();
        intake.update();
        prefeed.start();
        prefeed.update();
        double expected = PrefeedSubsystem.powerFor(PrefeedSubsystem.State.IN, PrefeedSubsystem.inPower,
                PrefeedSubsystem.outPower, PrefeedSubsystem.reversed);
        assertEquals("at PLAY the prefeed follows the intake, whichever way it is wired",
                Arrays.asList("setPower(" + expected + ")"),
                devices.calls(DeviceNames.PREFEED));
    }

    @Test
    public void feedGateWaitsForPlayEvenWhileTheFeederIsAskingForIt() {
        double blocking = FeedGateSubsystem.blockingPosition;
        double feeding = FeedGateSubsystem.feedingPosition;
        FeedGateSubsystem.blockingPosition = 0.2;
        FeedGateSubsystem.feedingPosition = 0.7;
        try {
            Devices devices = new Devices().motor(DeviceNames.FEEDER).servo(DeviceNames.FEED_GATE);
            FeederSubsystem feeder = new FeederSubsystem(devices.map);
            feeder.initialize();
            devices.clear();
            feeder.feed();
            FeedGateSubsystem gate = new FeedGateSubsystem(devices.map, feeder);
            runInit(gate, devices);

            feeder.idle();
            gate.start();
            gate.update();
            assertEquals("at PLAY the gate goes to BLOCKING",
                    Arrays.asList("setPosition(0.2)"), devices.calls(DeviceNames.FEED_GATE));
            gate.update();
            assertEquals("a held gate is not rewritten every loop",
                    1, devices.calls(DeviceNames.FEED_GATE).size());
            feeder.feed();
            gate.update();
            assertEquals("a feed opens it",
                    Arrays.asList("setPosition(0.2)", "setPosition(0.7)"), devices.calls(DeviceNames.FEED_GATE));
        } finally {
            FeedGateSubsystem.blockingPosition = blocking;
            FeedGateSubsystem.feedingPosition = feeding;
        }
    }

    @Test
    public void feedGateHeldClosedDuringAnAutosInitEvenWhileTheFeederIsAskingForIt() {
        double blocking = FeedGateSubsystem.blockingPosition;
        double feeding = FeedGateSubsystem.feedingPosition;
        FeedGateSubsystem.blockingPosition = 0.2;
        FeedGateSubsystem.feedingPosition = 0.7;
        try {
            Devices devices = new Devices().motor(DeviceNames.FEEDER).servo(DeviceNames.FEED_GATE);
            FeederSubsystem feeder = new FeederSubsystem(devices.map);
            feeder.initialize();
            devices.clear();
            feeder.feed();
            FeedGateSubsystem gate = new FeedGateSubsystem(devices.map, feeder);
            gate.initialize();
            gate.holdAtInit();
            for (int i = 0; i < INIT_LOOPS; i++) {
                gate.update();
            }
            assertEquals("closed once and held there through INIT",
                    Arrays.asList("setPosition(0.2)"), devices.calls(DeviceNames.FEED_GATE));
            assertEquals(FeedGateSubsystem.State.BLOCKING, gate.state());

            gate.start();
            gate.update();
            assertEquals("at PLAY it follows the feeder again",
                    Arrays.asList("setPosition(0.2)", "setPosition(0.7)"), devices.calls(DeviceNames.FEED_GATE));
        } finally {
            FeedGateSubsystem.blockingPosition = blocking;
            FeedGateSubsystem.feedingPosition = feeding;
        }
    }

    @Test
    public void feedGateWritesNothingWhileItsPositionsAreNaN() {
        double blocking = FeedGateSubsystem.blockingPosition;
        double feeding = FeedGateSubsystem.feedingPosition;
        FeedGateSubsystem.blockingPosition = Double.NaN;
        FeedGateSubsystem.feedingPosition = Double.NaN;
        try {
            Devices devices = new Devices().motor(DeviceNames.FEEDER).servo(DeviceNames.FEED_GATE);
            FeedGateSubsystem gate = new FeedGateSubsystem(devices.map, new FeederSubsystem(devices.map));
            runInit(gate, devices);
            gate.start();
            for (int i = 0; i < 10; i++) {
                gate.update();
            }
            assertTrue("unmeasured positions are never written, even after PLAY",
                    devices.calls(DeviceNames.FEED_GATE).isEmpty());
        } finally {
            FeedGateSubsystem.blockingPosition = blocking;
            FeedGateSubsystem.feedingPosition = feeding;
        }
    }

    @Test
    public void feederStaysAtZeroInInit() {
        Devices devices = new Devices().motor(DeviceNames.FEEDER);
        runInit(new FeederSubsystem(devices.map), devices);
    }

    @Test
    public void launcherStaysAtZeroInInit() {
        Devices devices = new Devices().motor(DeviceNames.LAUNCHER_LEFT).motor(DeviceNames.LAUNCHER_RIGHT)
                .motor(DeviceNames.FEEDER);
        runInit(new LauncherSubsystem(devices.map, new FeederSubsystem(devices.map)), devices);
    }

    @Test
    public void launcherReversesForAnUnjamOnlyAfterPlay() {
        Devices devices = new Devices().motor(DeviceNames.LAUNCHER_LEFT).motor(DeviceNames.LAUNCHER_RIGHT)
                .motor(DeviceNames.FEEDER);
        FeederSubsystem feeder = new FeederSubsystem(devices.map);
        feeder.initialize();
        devices.clear();
        feeder.reverse();
        LauncherSubsystem launcher = new LauncherSubsystem(devices.map, feeder);
        runInit(launcher, devices);
        assertFalse("before PLAY an unjam does not turn the wheels", launcher.reversing());

        launcher.start();
        launcher.update();
        assertTrue("at PLAY the wheels back up with the feeder", launcher.reversing());
        assertFalse(launcher.atSpeed());
        assertEquals("setVelocity(" + LauncherSubsystem.reverseCommandFor(
                        LauncherSubsystem.reverseTicksPerSecond, LauncherSubsystem.leftReversed) + ")",
                devices.calls(DeviceNames.LAUNCHER_LEFT).get(0));
        assertEquals("setVelocity(" + LauncherSubsystem.reverseCommandFor(
                        LauncherSubsystem.reverseTicksPerSecond, LauncherSubsystem.rightReversed) + ")",
                devices.calls(DeviceNames.LAUNCHER_RIGHT).get(0));

        feeder.idle();
        launcher.update();
        assertFalse("on release an idle launcher goes back to idle", launcher.reversing());
        assertFalse(launcher.isSpinning());
    }

    /** {@code initialize()} and the INIT loops, failing on any write that moves; then clears the record. */
    private static void runInit(Subsystem subsystem, Devices devices) {
        subsystem.initialize();
        for (int i = 0; i < INIT_LOOPS; i++) {
            subsystem.update();
        }
        List<String> moving = devices.movingWrites();
        if (!moving.isEmpty()) {
            fail(subsystem.getClass().getSimpleName() + " moved something before PLAY (G304.H, G403):\n  "
                    + String.join("\n  ", moving));
        }
        devices.clear();
    }

    // ------------------------------------------------------------ the guard

    @Test
    public void everySubsystemWithHardwareIsRunHereOrExplained() throws IOException {
        File dir = TeamCodeSources.subsystemsAndVision();
        List<String> unlisted = new ArrayList<>();
        int seen = 0;
        for (File file : TeamCodeSources.javaFiles(dir)) {
            String code = COMMENTS.matcher(read(file)).replaceAll("");
            if (!code.contains("implements Subsystem") || !code.contains("hardwareMap.get(")) {
                continue;
            }
            seen++;
            String name = file.getName().replace(".java", "");
            if (!RUN_HERE.contains(name) && !NOT_RUN_HERE.containsKey(name)) {
                unlisted.add(name);
            }
        }
        assertTrue("found no subsystems with hardware: is the scan broken?", seen >= 5);
        assertTrue("Run these through runInit here, or say in NOT_RUN_HERE why they cannot move before PLAY: "
                + unlisted, unlisted.isEmpty());
    }

    /**
     * The INIT hold is asked for in one place: {@code RobotOpMode}, for an {@code @Autonomous}, through
     * {@code Robot.holdForAutoInit()}. A TeleOp's INIT runs between AUTO and TELEOP (G403).
     */
    @Test
    public void onlyAnAutonomousInitHoldsAnything() throws IOException {
        File base = TeamCodeSources.subsystemsAndVision();
        List<String> wrong = new ArrayList<>();
        boolean robotOpModeCalls = false;
        for (File file : allJava(base)) {
            String code = COMMENTS.matcher(read(file)).replaceAll("");
            String name = file.getName();
            if (code.contains(".holdForAutoInit(")) {
                if (name.equals("RobotOpMode.java")) {
                    robotOpModeCalls = true;
                    assertTrue("RobotOpMode holds at INIT only for an Autonomous",
                            Pattern.compile("if \\(isAutonomous\\(\\)\\) \\{[^}]*robot\\.holdForAutoInit\\(\\);")
                                    .matcher(code).find());
                } else {
                    wrong.add(name + " calls holdForAutoInit()");
                }
            }
            if (code.contains(".holdAtInit(") && !name.equals("Robot.java")) {
                wrong.add(name + " calls holdAtInit()");
            }
        }
        assertTrue("RobotOpMode no longer holds the stick and gate in an Auto's INIT", robotOpModeCalls);
        assertTrue("Only RobotOpMode, for an Autonomous, may hold anything at INIT: " + wrong, wrong.isEmpty());
    }

    private static List<File> allJava(File dir) {
        List<File> files = new ArrayList<>();
        File[] entries = dir.listFiles();
        if (entries == null) return files;
        for (File entry : entries) {
            if (entry.isDirectory()) files.addAll(allJava(entry));
            else if (entry.getName().endsWith(".java")) files.add(entry);
        }
        return files;
    }

    private static final List<String> RUN_HERE = Arrays.asList(
            "FlowerStickSubsystem", "TurretSubsystem", "IntakeSubsystem", "FeederSubsystem", "LauncherSubsystem",
            "PrefeedSubsystem", "FeedGateSubsystem", "CornerWheelSubsystem");

    private static final Pattern COMMENTS = Pattern.compile("/\\*.*?\\*/|//[^\\n]*", Pattern.DOTALL);

    private static String read(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    @Test
    public void theRecorderCatchesAMove() {
        Devices devices = new Devices().servo("s").crServo("c").motor("m");
        devices.map.get(Servo.class, "s").setPosition(0.5);
        devices.map.get(CRServo.class, "c").setPower(0.0);
        devices.map.get(DcMotorEx.class, "m").setPower(0.0);
        devices.map.get(DcMotorEx.class, "m").setVelocity(10.0);
        devices.map.get(DcMotorEx.class, "m").setPower(0.05);
        assertEquals(4, devices.movingWrites().size());
        devices.clear();
        devices.map.get(DcMotorEx.class, "m").setPower(0.0);
        assertFalse(devices.movingWrites().size() > 0);
    }

    // ------------------------------------------------------------ recording fakes

    /** A hardware map of fakes that record every call that writes. */
    private static final class Devices {
        private final Map<String, Object> fakes = new HashMap<>();
        /** The SDK's own lookup reads Android's {@code Build}, which a JVM test does not have. */
        final HardwareMap map = new HardwareMap(null, null) {
            @Override
            public <T> T get(Class<? extends T> type, String name) {
                Object fake = fakes.get(name);
                if (fake == null) throw new IllegalArgumentException("no fake named " + name);
                return type.cast(fake);
            }

            @Override
            public <T> T tryGet(Class<? extends T> type, String name) {
                Object fake = fakes.get(name);
                return fake == null ? null : type.cast(fake);
            }
        };
        private final Map<String, List<String>> calls = new LinkedHashMap<>();
        private final Map<String, Boolean> isServo = new HashMap<>();

        Devices servo(String name) {
            return add(name, true, Servo.class, PwmControl.class);
        }

        Devices crServo(String name) {
            return add(name, true, CRServo.class, PwmControl.class);
        }

        Devices motor(String name) {
            return add(name, false, DcMotorEx.class);
        }

        private Devices add(String name, boolean servo, Class<?>... types) {
            List<String> record = new ArrayList<>();
            calls.put(name, record);
            isServo.put(name, servo);
            Object fake = Proxy.newProxyInstance(getClass().getClassLoader(), types, (proxy, method, args) -> {
                String m = method.getName();
                if (m.startsWith("set") || m.equals("resetDeviceConfigurationForOpMode")) {
                    record.add(m + "(" + (args == null ? "" : joined(args)) + ")");
                }
                return defaultFor(method.getReturnType(), m, name);
            });
            fakes.put(name, fake);
            return this;
        }

        List<String> calls(String name) {
            return calls.get(name);
        }

        void clear() {
            for (List<String> record : calls.values()) {
                record.clear();
            }
        }

        /** Every write that could move something: any servo pulse, and any motor power but zero. */
        List<String> movingWrites() {
            List<String> moving = new ArrayList<>();
            for (Map.Entry<String, List<String>> device : calls.entrySet()) {
                boolean servo = isServo.get(device.getKey());
                for (String call : device.getValue()) {
                    boolean moves = servo
                            ? call.startsWith("setPosition(") || call.startsWith("setPower(") || call.startsWith("setPwmEnable(")
                            : nonZero(call, "setPower(") || nonZero(call, "setVelocity(")
                                    || call.equals("setMode(" + DcMotor.RunMode.RUN_TO_POSITION + ")");
                    if (moves) {
                        moving.add(device.getKey() + "." + call);
                    }
                }
            }
            return moving;
        }

        /** A call to {@code method} whose first argument is not zero. */
        private static boolean nonZero(String call, String method) {
            if (!call.startsWith(method)) return false;
            String first = call.substring(method.length(), call.length() - 1).split(",")[0].trim();
            return Double.parseDouble(first) != 0.0;
        }

        private static String joined(Object[] args) {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) s.append(", ");
                s.append(args[i]);
            }
            return s.toString();
        }

        private static Object defaultFor(Class<?> type, String method, String name) {
            if (method.equals("toString") || method.equals("getDeviceName")) return name;
            if (method.equals("hashCode")) return name.hashCode();
            if (type == boolean.class) return false;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == double.class) return 0.0;
            if (type == float.class) return 0.0f;
            return null;
        }
    }

    /** TeamCode's main sources, found the way the other source scans find them. */
    private static final class TeamCodeSources {
        static File subsystemsAndVision() {
            File base = new File("src/main/java/org/firstinspires/ftc/teamcode");
            if (!base.isDirectory()) {
                base = new File("TeamCode/src/main/java/org/firstinspires/ftc/teamcode");
            }
            assertTrue("cannot find TeamCode's sources from " + new File("").getAbsolutePath(), base.isDirectory());
            return base;
        }

        static List<File> javaFiles(File base) {
            List<File> files = new ArrayList<>();
            for (String sub : new String[] {"subsystems", "vision"}) {
                File[] entries = new File(base, sub).listFiles();
                if (entries == null) continue;
                for (File entry : entries) {
                    if (entry.getName().endsWith(".java")) files.add(entry);
                }
            }
            return files;
        }
    }
}
