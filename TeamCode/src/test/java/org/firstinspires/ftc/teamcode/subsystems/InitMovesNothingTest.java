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
    public void turretWaitsForPlay() {
        Devices devices = new Devices().motor(DeviceNames.TURRET_ENCODER_HOST).crServo(DeviceNames.TURRET);
        TurretSubsystem turret = new TurretSubsystem(devices.map);
        runInit(turret, devices);

        turret.start();
        turret.update();
        assertTrue("at PLAY the turret's servo is written",
                devices.calls(DeviceNames.TURRET).get(0).startsWith("setPower("));
    }

    @Test
    public void intakeStarsWaitForPlay() {
        Devices devices = new Devices().motor(DeviceNames.INTAKE).crServo(DeviceNames.INTAKE_STARS);
        IntakeSubsystem intake = new IntakeSubsystem(devices.map);
        runInit(intake, devices);

        intake.start();
        intake.update();
        assertEquals("at PLAY the star wheels are written, still stopped",
                "setPower(0.0)", devices.calls(DeviceNames.INTAKE_STARS).get(0));
    }

    @Test
    public void feederStaysAtZeroInInit() {
        Devices devices = new Devices().motor(DeviceNames.FEEDER);
        runInit(new FeederSubsystem(devices.map), devices);
    }

    @Test
    public void launcherStaysAtZeroInInit() {
        Devices devices = new Devices().motor(DeviceNames.LAUNCHER_LEFT).motor(DeviceNames.LAUNCHER_RIGHT);
        runInit(new LauncherSubsystem(devices.map), devices);
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

    private static final List<String> RUN_HERE = Arrays.asList(
            "FlowerStickSubsystem", "TurretSubsystem", "IntakeSubsystem", "FeederSubsystem", "LauncherSubsystem");

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
