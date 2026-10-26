package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.api.Paths;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.autokit.AutoDrive;
import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.logging.AdvantageScopeFrame;
import org.firstinspires.ftc.teamcode.logging.FieldRobot;
import org.firstinspires.ftc.teamcode.logging.MatchLog;
import org.firstinspires.ftc.teamcode.logging.WpiLogReader;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RSimpleAuto;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * An Auto run the way {@link BuiltAuto} runs one — built with an {@link AutoKit} that an
 * {@link AutoPathLog} watches, {@link AutoPathLog#plan} called once it is built, then Ivy's real
 * scheduler loop by loop with the match log committing a frame each loop — against a fake
 * drivetrain and a made-up clock, read back with the test's own {@code .wpilog} reader.
 */
public class AutoPathLogTest {

    private static final double LOOP_S = 0.02;
    private static final double EPS = 1e-9;

    private long nowUs;
    private double now;
    private FakeDrive drive;
    private ByteArrayOutputStream out;
    private MatchLog log;
    private AutoPathLog pathLog;
    private AutoKit kit;

    @Before
    public void setUp() {
        Scheduler.reset();
        nowUs = 0;
        now = 0;
        drive = new FakeDrive();
        out = new ByteArrayOutputStream();
        log = MatchLog.toStream("test", out, "", null, () -> nowUs);
        pathLog = new AutoPathLog(log);
        // Every name R-Simple uses, as instant fakes: the paths are what this test is about.
        AutoRegistry registry = new AutoRegistry()
                .command("LaunchAll", 0.1, () -> Commands.instant(() -> { }))
                .command("SpinUp", 0.1, () -> Commands.instant(() -> { }))
                .trigger("Empty", () -> true);
        kit = new AutoKit(drive, registry, () -> now).watchPaths(pathLog);
    }

    /** One loop as {@code RobotOpMode} runs it: the scheduler, the drivetrain, then the log's frame. */
    private void loop() {
        Scheduler.execute();
        drive.tick();
        now += LOOP_S;
        nowUs += 20_000;
        log.commit();
    }

    /** Builds, plans and runs {@code build}'s Auto to its end, as {@link BuiltAuto#onStart} does. */
    private void runAuto(Command auto) {
        pathLog.plan(kit.paths());
        auto.schedule();
        for (int i = 0; i < 2000; i++) {
            loop();
            if (!auto.isScheduled()) return;
        }
        fail("Auto still running after " + (2000 * LOOP_S) + " s");
    }

    private WpiLogReader readBack() {
        log.close(null, 5000);
        assertNull(log.failure(), log.failure());
        return new WpiLogReader(out.toByteArray());
    }

    // ------------------------------------------------------------------ tests

    /** R-Simple is a BuiltAuto with one path: the park, which the endgame guard shares. */
    @Test
    public void aBuiltAutoWithOnePathWritesThatPathAsTheFullPlan() {
        Command auto = RSimpleAuto.build(kit, false);
        List<Path> paths = kit.paths();
        assertEquals("R-Simple drives one path", 1, paths.size());
        runAuto(auto);

        WpiLogReader r = readBack();
        WpiLogReader.Entry full = r.entry(FieldRobot.ROBOT.fullPath);
        assertEquals("struct:Pose2d[]", full.type);
        assertEquals("the plan is written once", 1, full.records.size());
        double[] expected = FieldRobot.toAdvantageScope(AutoPathLog.sample(paths.get(0)));
        assertArrayEquals(expected, full.records.get(0).asDoubles(), EPS);
        assertTrue("one path, so the plan and the path are the same samples", expected.length >= 2 * 3);
    }

    @Test
    public void theActivePathIsWrittenWhenItStartsAndClearedWhenItEnds() {
        Command auto = RSimpleAuto.build(kit, false);
        runAuto(auto);

        WpiLogReader r = readBack();
        WpiLogReader.Entry active = r.entry(FieldRobot.ROBOT.activePath);
        assertEquals("struct:Pose2d[]", active.type);
        assertEquals("written at the start and at the end", 2, active.records.size());
        double[] expected = FieldRobot.toAdvantageScope(AutoPathLog.sample(kit.paths().get(0)));
        assertArrayEquals(expected, active.records.get(0).asDoubles(), EPS);
        assertEquals("empty once the path is done", 0, active.records.get(1).payload.length);
        assertTrue(active.records.get(1).timestampUs > active.records.get(0).timestampUs);
        assertEquals(1, drive.followed.size());
    }

    @Test
    public void theTargetIsWhereThePathEnds() {
        Command auto = RSimpleAuto.build(kit, false);
        runAuto(auto);

        WpiLogReader r = readBack();
        WpiLogReader.Entry target = r.entry(FieldRobot.ROBOT.target);
        assertEquals("struct:Pose2d", target.type);
        assertEquals(1, target.records.size());
        // PARK in r-simple.pp: (14, 95) facing 90°, in the frame /Odometry/Robot uses.
        double[] expected = {
                AdvantageScopeFrame.xMeters(14, 95), AdvantageScopeFrame.yMeters(14, 95),
                AdvantageScopeFrame.headingRad(Math.toRadians(90))};
        assertArrayEquals(expected, target.records.get(0).asDoubles(), EPS);
        WpiLogReader.Entry active = r.entry(FieldRobot.ROBOT.activePath);
        assertEquals("stamped with the loop the path started in",
                active.records.get(0).timestampUs, target.records.get(0).timestampUs);
    }

    /** Before any path, the ghost sits where the robot starts, not at AdvantageScope's origin. */
    @Test
    public void theTargetDuringInitIsTheDeclaredStart() {
        Pose start = RSimpleAuto.startPose(false);
        pathLog.target(start);
        loop();
        runAuto(RSimpleAuto.build(kit, false));

        WpiLogReader r = readBack();
        WpiLogReader.Entry target = r.entry(FieldRobot.ROBOT.target);
        assertEquals("the start, then the one path's end", 2, target.records.size());
        assertArrayEquals(new double[] {
                AdvantageScopeFrame.xMeters(start.x(), start.y()), AdvantageScopeFrame.yMeters(start.x(), start.y()),
                AdvantageScopeFrame.headingRad(start.heading())}, target.records.get(0).asDoubles(), EPS);
        assertTrue(target.records.get(0).timestampUs < r.entry(FieldRobot.ROBOT.fullPath).records.get(0).timestampUs);
    }

    @Test
    public void theFullPlanIsEveryPathInCardOrder() {
        Path a = Paths.line(new Pose(10, 10, 0), new Pose(30, 10, 0)).tangent();
        Path b = Paths.line(new Pose(30, 10, 0), new Pose(30, 50, 0)).tangent();
        Path park = Paths.line(new Pose(30, 50, 0), new Pose(60, 60, 0)).tangent();
        Command auto = kit.guarded("Auto", park, 1.0, kit.path("A", a), kit.path("B", b), kit.path("park", park));
        assertEquals(3, kit.paths().size());
        runAuto(auto);

        WpiLogReader r = readBack();
        double[] full = r.entry(FieldRobot.ROBOT.fullPath).records.get(0).asDoubles();
        double[] sa = AutoPathLog.sample(a), sb = AutoPathLog.sample(b), sp = AutoPathLog.sample(park);
        assertEquals(sp.length + sa.length + sb.length, full.length);
        // The cards are built left to right, so their paths come in card order; the park, which the
        // guard also holds, is listed once.
        double[] expected = new double[full.length];
        System.arraycopy(FieldRobot.toAdvantageScope(sa), 0, expected, 0, sa.length);
        System.arraycopy(FieldRobot.toAdvantageScope(sb), 0, expected, sa.length, sb.length);
        System.arraycopy(FieldRobot.toAdvantageScope(sp), 0, expected, sa.length + sb.length, sp.length);
        assertArrayEquals(expected, full, EPS);
        // Three starts and three ends.
        assertEquals(6, r.entry(FieldRobot.ROBOT.activePath).records.size());
        assertEquals(3, r.entry(FieldRobot.ROBOT.target).records.size());
    }

    @Test
    public void samplesRunFromStartToEndAboutTwoInchesApart() {
        Path line = Paths.line(new Pose(0, 0, 0), new Pose(20, 0, 0)).constant(Math.PI / 2);
        double[] s = AutoPathLog.sample(line);
        assertEquals(11 * 3, s.length);
        assertEquals(0.0, s[0], EPS);
        assertEquals(20.0, s[s.length - 3], EPS);
        assertEquals(0.0, s[s.length - 2], EPS);
        assertEquals(Math.PI / 2, s[s.length - 1], EPS);
        for (int i = 3; i < s.length; i += 3) {
            assertEquals(AutoPathLog.SAMPLE_SPACING_IN, s[i] - s[i - 3], EPS);
        }
    }

    @Test
    public void aVeryLongPathIsCappedAndAVeryShortOneStillHasBothEnds() {
        double[] far = AutoPathLog.sample(Paths.line(new Pose(0, 0, 0), new Pose(0, 5000, 0)).tangent());
        assertEquals(AutoPathLog.MAX_SAMPLES_PER_PATH * 3, far.length);
        assertEquals(5000.0, far[far.length - 2], EPS);
        double[] near = AutoPathLog.sample(Paths.line(new Pose(1, 1, 0), new Pose(1.5, 1, 0)).tangent());
        assertEquals(2 * 3, near.length);
        assertEquals(1.0, near[0], EPS);
        assertEquals(1.5, near[3], EPS);
    }

    // ------------------------------------------------------------- fake drive

    /** Moves a fifth of the current path each loop. */
    private static final class FakeDrive implements AutoDrive {
        final java.util.List<Path> followed = new java.util.ArrayList<>();
        private Path current;
        private double progress = 1;
        private Pose pose = new Pose(0, 0, 0);

        void tick() {
            if (current == null) return;
            progress = Math.min(1, progress + 0.2);
            pose = current.get(progress);
        }

        @Override public void follow(Path path) {
            current = path;
            progress = 0;
            followed.add(path);
        }

        @Override public boolean pathDone() {
            return current == null || progress >= 1;
        }

        @Override public Pose pose() {
            return pose;
        }

        @Override public boolean poseReferenced() {
            return true;
        }

        @Override public void hold(Pose pose) {
            current = null;
        }
    }
}
