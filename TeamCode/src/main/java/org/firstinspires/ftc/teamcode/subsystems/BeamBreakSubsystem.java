package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * The two IR break-beams bought for the Michigan State Championship (#213, #505): one across the
 * launcher's <b>throat</b> that sees each ball leave, and one across the <b>lane</b> where the
 * third piece sits when the lane is packed behind the gate. Both are optional devices
 * ({@link DeviceNames#BEAM_BREAKS}) and <b>off until a robot has them</b>.
 *
 * <h2>Turning one on, the day it is fitted</h2>
 *
 * <ol>
 *   <li>Wire it to a Control Hub digital channel and add it to that robot's
 *       {@code res/xml/robot_<team>.xml}, inside the Control Hub's {@code <LynxModule>}:
 *       {@code <DigitalDevice name="throatBeam" port="N" />} ({@code laneBeam} for the lane's).</li>
 *   <li>One {@code RobotDifferences} line under that robot, with the date:
 *       {@code () -> BeamBreakSubsystem.throatFitted = true} ({@code laneFitted} for the lane's).</li>
 *   <li>Full TeamCode install (an XML changed), Activate the config, run <b>Check: Hardware</b>,
 *       then the bench checks in {@code doc/robot-todo.md}.</li>
 * </ol>
 *
 * <p>With a switch on, the constructor asks for the device and the OpMode refuses to start if the
 * XML lacks it: never a silent "no beam". With it off, nothing is looked up and every answer here is
 * "not fitted", so the robot runs exactly as before.
 *
 * <h2>What each beam changes</h2>
 *
 * <ul>
 *   <li><b>Throat:</b> the launcher counts each ball that passes it instead of guessing from the
 *       wheels' dips ({@code LauncherSubsystem.launched()}; the dip count stays, for the speed table
 *       and PIDF timing); an Auto {@code LaunchAll} ends the moment its 4th ball leaves, or the throat
 *       goes quiet with the lane empty, instead of at its thin feed timer, and recovers a stuck feed
 *       ({@code LaunchCards}); the lane tracker loses a piece per real ball; a TeleOp feed that sticks
 *       is on the Match page ({@link #feedStuck()}).</li>
 *   <li><b>Lane:</b> "full" needs the entrance colour sensor and this beam to agree
 *       ({@code ColorSensorSubsystem.full()}): at Fruitport the colour sensor alone read FULL with a
 *       piece held at the entrance and two or fewer ahead of it about 20 s a match on 19429.</li>
 * </ul>
 *
 * <p>Read once a loop, from the bulk read: free. Nothing here moves anything.
 */
@Configurable
public class BeamBreakSubsystem implements Subsystem {

    /** The throat beam is fitted on this robot: a {@code RobotDifferences} line turns it on. */
    public static boolean throatFitted = false;

    /** The lane beam is fitted on this robot: a {@code RobotDifferences} line turns it on. */
    public static boolean laneFitted = false;

    /**
     * The receiver reads LOW when its beam is broken: true for the Adafruit 2167/2168 (open
     * collector, the hub's pull-up). A beam that reads the other way: false, in Panels, then here.
     */
    public static boolean brokenReadsLow = true;

    /**
     * Readings a break must last to be a piece ({@link BeamBreak}). 2: a piece blocks the beam six
     * to a dozen loops (#505); one reading is noise. <b>Bench-check</b> ({@code doc/robot-todo.md}).
     */
    public static int minBlockedReads = 2;

    /**
     * After a ball, a throat quiet this long with the lane not holding a piece is the lane run dry:
     * the volley ends ({@link FeedWatch}). 0.9 s: balls leave about 0.45 s apart (311 Auto feeds,
     * 12 to 21 Nov 2026), so two missed beats. <b>Not measured on a beam.</b>
     */
    public static double quietS = 0.9;

    /**
     * No ball through the throat this long after the gate opened is an empty lane, or a stuck one
     * if the lane says it holds a piece. 1.5 s: the first ball took 0.58 to 0.93 s from the gate to
     * the wheels at Belding (7 Nov 2026), slower from a standstill.
     */
    public static double firstBallWithinS = 1.5;

    /** The throat beam broken this long is a ball stuck there: a piece passes in 120 to 250 ms (#505). */
    public static double jamBlockedS = 0.5;

    /** With the lane holding a piece, no ball this long after the last is a stuck feed. */
    public static double stuckGapS = 1.2;

    private final DigitalChannel throatChannel;
    private final DigitalChannel laneChannel;
    private final BeamBreak throat = new BeamBreak();
    private final BeamBreak lane = new BeamBreak();
    private final FeedWatch teleOpWatch = new FeedWatch();
    private final String atInit;

    private BooleanSupplier gateOpen = () -> false;
    private BooleanSupplier launcherAtSpeed = () -> false;
    private Supplier<FeedWatch.Lane> laneHolds = () -> FeedWatch.Lane.UNKNOWN;
    private boolean started;
    private boolean wasFeeding;
    private boolean stuck;
    private int stuckFeeds;
    private String stuckWhy = "";

    /**
     * Looks up each beam whose switch is on, and only those. A switch on with no such device in
     * the active configuration fails here with what to add, rather than running without it.
     */
    public BeamBreakSubsystem(HardwareMap hardwareMap) {
        throatChannel = throatFitted ? lookUp(hardwareMap, DeviceNames.THROAT_BEAM, "throatFitted") : null;
        laneChannel = laneFitted ? lookUp(hardwareMap, DeviceNames.LANE_BEAM, "laneFitted") : null;
        atInit = (throatChannel == null ? "throat beam not fitted" : "throat beam fitted")
                + ", " + (laneChannel == null ? "lane beam not fitted" : "lane beam fitted");
    }

    private static DigitalChannel lookUp(HardwareMap hardwareMap, String name, String switchName) {
        DigitalChannel channel = hardwareMap.tryGet(DigitalChannel.class, name);
        if (channel == null) {
            throw new IllegalStateException("BeamBreakSubsystem." + switchName + " is on, but the active"
                    + " configuration has no DigitalDevice \"" + name + "\". Add <DigitalDevice name=\"" + name
                    + "\" port=\"N\" /> to the robot's XML (full TeamCode install, Activate), or turn the"
                    + " switch off in RobotDifferences.");
        }
        return channel;
    }

    /** Whether the beam named {@code name} ({@link DeviceNames#THROAT_BEAM} or {@code LANE_BEAM}) has its switch on. */
    public static boolean fitted(String name) {
        if (DeviceNames.THROAT_BEAM.equals(name)) return throatFitted;
        if (DeviceNames.LANE_BEAM.equals(name)) return laneFitted;
        return false;
    }

    /** The switch for the beam named {@code name}, for Check: Hardware. */
    public static String switchName(String name) {
        if (DeviceNames.THROAT_BEAM.equals(name)) return "BeamBreakSubsystem.throatFitted";
        if (DeviceNames.LANE_BEAM.equals(name)) return "BeamBreakSubsystem.laneFitted";
        return "(no switch)";
    }

    /**
     * What the TeleOp's stuck-feed watch reads: the gate open, the launcher at speed, and what the
     * lane holds. Wired once by {@code Robot}; until then nothing is ever stuck.
     */
    public void watchFeeds(BooleanSupplier gateOpen, BooleanSupplier launcherAtSpeed, Supplier<FeedWatch.Lane> lane) {
        this.gateOpen = gateOpen;
        this.launcherAtSpeed = launcherAtSpeed;
        this.laneHolds = lane;
    }

    @Override
    public void initialize() {
        if (throatChannel != null) throatChannel.setMode(DigitalChannel.Mode.INPUT);
        if (laneChannel != null) laneChannel.setMode(DigitalChannel.Mode.INPUT);
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void update() {
        long nowMs = System.nanoTime() / 1_000_000L;
        if (throatChannel != null) throat.step(nowMs, broken(throatChannel.getState()), minBlockedReads);
        if (laneChannel != null) lane.step(nowMs, broken(laneChannel.getState()), minBlockedReads);
        watchFeed(nowMs / 1000.0);
    }

    private static boolean broken(boolean high) {
        return brokenReadsLow != high;
    }

    /** The stuck-feed watch, for the Match page and the match log; it moves nothing. */
    private void watchFeed(double nowS) {
        boolean feeding = throatChannel != null && started && gateOpen.getAsBoolean();
        if (feeding && !wasFeeding) teleOpWatch.start(nowS, throat.passes());
        if (!feeding) {
            teleOpWatch.stop();
            stuck = false;
        } else if (launcherAtSpeed.getAsBoolean()) {
            // A feed the robot holds at the gate on purpose waits for speed; only an at-speed feed sticks.
            boolean now = teleOpWatch.step(nowS, throat.passes(), throat.blockedForMs() / 1000.0, laneHolds.get(),
                    Double.NaN, quietS, firstBallWithinS, jamBlockedS, stuckGapS) == FeedWatch.Verdict.STUCK;
            if (now && !stuck) {
                stuckFeeds++;
                stuckWhy = teleOpWatch.why();
            }
            stuck = now;
        }
        wasFeeding = feeding;
    }

    @Override
    public void stop() {
    }

    /** The throat beam's state and count, or null when it is not fitted. */
    public BeamBreak throat() {
        return throatChannel == null ? null : throat;
    }

    /** The lane beam's state, or null when it is not fitted. */
    public BeamBreak lane() {
        return laneChannel == null ? null : lane;
    }

    /** The throat beam is fitted (its switch on and the device found). */
    public boolean throatPresent() {
        return throatChannel != null;
    }

    /** The lane beam is fitted (its switch on and the device found). */
    public boolean lanePresent() {
        return laneChannel != null;
    }

    /**
     * A feed is open, the launcher at speed, and the throat says it is stuck ({@link FeedWatch}):
     * for the Match page. False without the throat beam.
     */
    public boolean feedStuck() {
        return stuck;
    }

    /** Why the feed last stuck, for the match log. */
    public String feedStuckWhy() {
        return stuckWhy;
    }

    /** Feeds that have stuck since this OpMode started. */
    public int stuckFeeds() {
        return stuckFeeds;
    }

    @Override
    public String health() {
        return atInit;
    }

    @Override
    public void describe(Display display) {
        if (throatChannel == null && laneChannel == null) {
            display.status("Beam breaks", Display.Level.OK, "not fitted (BeamBreakSubsystem.throatFitted, laneFitted)");
            return;
        }
        if (throatChannel != null) {
            display.status("Throat beam", stuck ? Display.Level.WARN : Display.Level.OK, String.format(Locale.US,
                    "%s · %d balls · last blocked %d ms%s", throat.blocked() ? "BROKEN" : "clear", throat.passes(),
                    throat.lastPassBlockedMs(), stuck ? " · FEED STUCK: " + stuckWhy : ""));
        }
        if (laneChannel != null) {
            display.status("Lane beam", Display.Level.OK, lane.blocked()
                    ? String.format(Locale.US, "BROKEN %.0f ms", lane.blockedForMs()) : "clear");
        }
    }
}
