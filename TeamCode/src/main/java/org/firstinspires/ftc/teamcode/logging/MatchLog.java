package org.firstinspires.ftc.teamcode.logging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The robot's match log: what happened every loop, written to a {@code .wpilog} that AdvantageScope
 * opens, with no laptop attached.
 *
 * <p><b>The loop never waits on the file.</b> The loop thread only copies numbers into a
 * preallocated {@link Frame} and hands it over ({@link #commit}); a background thread turns frames
 * into {@code .wpilog} records and writes them. Nothing on the loop side formats, encodes or
 * allocates per loop (an event's text is the caller's one allocation). If the writer falls behind
 * and every frame is in use, the loop drops that loop's data and counts it rather than waiting.
 *
 * <p><b>Logging never takes the robot down.</b> No method here throws on the loop thread. A write
 * failure stops logging, keeps the reason ({@link #failure}) and leaves the robot running.
 *
 * <p><b>A brownout costs at most about half a second.</b> The writer flushes, and syncs a file to
 * storage, every {@link #FLUSH_EVERY_MS}.
 *
 * <p><b>One file per match.</b> An Autonomous ends with {@link #pause} rather than {@link #close}:
 * the file stays open, and the TeleOp that follows {@link #resume}s it, so Auto, the break and
 * TeleOp are one timeline (AdvantageScope colors each part). A pause nobody resumes closes the file
 * by itself after its timeout.
 *
 * <p>What every loop records, so no OpMode or subsystem has to: match state, alliance, both
 * gamepads (written when they change), the robot's pose, loop time. Anything else goes in with
 * {@link #put(String, double)} or {@link #event(String)}.
 *
 * <p>Has no dependency on the FTC SDK, so {@code MatchLogTest} runs it against the simulated
 * match's checks on a laptop. {@code RobotOpMode} is what fills it on the robot.
 */
public final class MatchLog {

    /** Distinct keys {@link #put} accepts; later keys are ignored, with one event saying so. */
    public static final int MAX_CHANNELS = 128;
    /** Events one loop can carry; more are counted as dropped. */
    public static final int MAX_EVENTS_PER_LOOP = 8;
    /** Frames in flight between the loop and the writer: about 1.3 s of loops at 50 Hz. */
    public static final int FRAMES = 64;
    /** How often the writer flushes (and syncs a file): the most a brownout can lose. */
    public static final long FLUSH_EVERY_MS = 500;

    /** The robot's mode, as AdvantageScope's 2027 {@code RobotMode} key spells it. */
    public enum Mode {
        DISABLED("disabled"), AUTONOMOUS("autonomous"), TELEOP("teleop");

        final String key;

        Mode(String key) {
            this.key = key;
        }
    }

    /** Called on the writer thread after each flush; a file target syncs to storage here. */
    interface Syncer {
        void sync() throws IOException;
    }

    /** Monotonic microseconds; {@code System.nanoTime() / 1000} on the robot. */
    public interface Clock {
        long nowUs();
    }

    /** One loop's data. Preallocated; the loop fills it, the writer reads it, then it is reused. */
    static final class Frame {
        long timestampUs;
        Mode mode = Mode.DISABLED;
        /** 0 when unknown; see {@link AdvantageScopeKeys#allianceStation}. */
        long allianceStation;
        final GamepadLog.State gamepad1 = new GamepadLog.State();
        final GamepadLog.State gamepad2 = new GamepadLog.State();
        boolean hasPose;
        double xIn, yIn, headingRad;
        double loopMs = Double.NaN;
        final int[] channels = new int[MAX_CHANNELS];
        final double[] values = new double[MAX_CHANNELS];
        int channelCount;
        final String[] events = new String[MAX_EVENTS_PER_LOOP];
        int eventCount;
        /** True for the last frame: the writer closes the file after it. */
        boolean last;

        void clearPerLoop() {
            hasPose = false;
            loopMs = Double.NaN;
            channelCount = 0;
            for (int i = 0; i < eventCount; i++) events[i] = null;
            eventCount = 0;
        }
    }

    private final Clock clock;
    private final ArrayBlockingQueue<Frame> free = new ArrayBlockingQueue<>(FRAMES);
    private final ArrayBlockingQueue<Frame> full = new ArrayBlockingQueue<>(FRAMES);
    private final String name;
    private final Thread writerThread;

    // The loop thread of whichever OpMode holds the log. Passing it from one OpMode to the next
    // goes through pause() and resume(), whose lock makes these visible to the new thread.
    private Frame frame;
    private final Map<String, Integer> channelIndex = new HashMap<>();
    private boolean channelsFullReported;
    private int droppedLoops;
    private int droppedEvents;
    private volatile boolean closed;

    // Written by the loop thread before a frame naming them is handed over, so the queue's
    // happens-before makes them visible to the writer.
    private final String[] channelNames = new String[MAX_CHANNELS];

    private volatile String failure;
    private volatile long framesWritten;

    // Pause and resume, shared by the OpMode that pauses, the one that resumes, and the writer that
    // closes an unclaimed pause. Guarded by lifecycle.
    private final Object lifecycle = new Object();
    private boolean paused;
    private long closeAtNs;
    private boolean writerDone;

    /** A log that records nothing, for when the file cannot be opened. */
    public static MatchLog disabled(String reason) {
        return new MatchLog(reason);
    }

    /**
     * A log written to {@code file} (its folder is created), synced to storage on every flush. Never
     * throws: a file that cannot be opened gives a {@link #disabled} log saying why.
     */
    public static MatchLog toFile(File file, String header, Map<String, String> metadata, Clock clock) {
        try {
            File dir = file.getParentFile();
            if (dir != null && !dir.isDirectory() && !dir.mkdirs()) {
                return disabled("cannot create " + dir);
            }
            final FileOutputStream raw = new FileOutputStream(file);
            OutputStream out = new BufferedOutputStream(raw, 64 * 1024);
            return new MatchLog(file.getName(), out, () -> raw.getFD().sync(), header, metadata, clock);
        } catch (IOException | RuntimeException e) {
            return disabled("cannot open " + file + ": " + e);
        }
    }

    /** A log written to any stream: tests, or a target that syncs itself. */
    public static MatchLog toStream(String name, OutputStream out, String header,
                                    Map<String, String> metadata, Clock clock) {
        return new MatchLog(name, out, null, header, metadata, clock);
    }

    private MatchLog(String reason) {
        this.clock = () -> 0L;
        this.name = "(not logging)";
        this.writerThread = null;
        this.failure = reason;
        this.frame = new Frame();
    }

    private MatchLog(String name, OutputStream out, Syncer syncer, String header,
                     Map<String, String> metadata, Clock clock) {
        this.clock = clock;
        this.name = name;
        for (int i = 0; i < FRAMES - 1; i++) free.add(new Frame());
        this.frame = new Frame();
        Writer writer = new Writer(out, syncer, header, metadata);
        this.writerThread = new Thread(writer, "MatchLog writer");
        this.writerThread.setDaemon(true);
        // Below the loop: the writer only has to keep up on average.
        this.writerThread.setPriority(Thread.MIN_PRIORITY);
        this.writerThread.start();
    }

    // ---- Loop side ---------------------------------------------------------------------------

    /** The file name, or {@code "(not logging)"}. */
    public String name() {
        return name;
    }

    /** Why logging stopped, or null while it works. */
    public String failure() {
        return failure;
    }

    public boolean isLogging() {
        synchronized (lifecycle) {
            return failure == null && !closed && !writerDone;
        }
    }

    public long framesWritten() {
        return framesWritten;
    }

    /** Loops whose data was dropped because the writer was behind. */
    public int droppedLoops() {
        return droppedLoops;
    }

    /** The match state this loop. */
    public void match(Mode mode, long allianceStation) {
        frame.mode = mode;
        frame.allianceStation = allianceStation;
    }

    /** Gamepad 1's state this loop, to fill in place. */
    public GamepadLog.State gamepad1() {
        return frame.gamepad1;
    }

    /** Gamepad 2's state this loop, to fill in place. */
    public GamepadLog.State gamepad2() {
        return frame.gamepad2;
    }

    /** The robot's pose this loop, in Pedro's frame: inches and radians. */
    public void pose(double xIn, double yIn, double headingRad) {
        frame.hasPose = true;
        frame.xIn = xIn;
        frame.yIn = yIn;
        frame.headingRad = headingRad;
    }

    public void loopMs(double ms) {
        frame.loopMs = ms;
    }

    /**
     * A number this loop, under a slash key ({@code "/Shooter/LeftRPM"}). The key costs a map lookup
     * per call and an allocation only the first time it is seen.
     */
    public void put(String key, double value) {
        Integer index = channelIndex.get(key);
        if (index == null) {
            if (channelIndex.size() >= MAX_CHANNELS) {
                if (!channelsFullReported) {
                    channelsFullReported = true;
                    event("log: more than " + MAX_CHANNELS + " keys; ignoring " + key + " and later ones");
                }
                return;
            }
            index = channelIndex.size();
            channelNames[index] = key;
            channelIndex.put(key, index);
        }
        if (frame.channelCount < MAX_CHANNELS) {
            frame.channels[frame.channelCount] = index;
            frame.values[frame.channelCount] = value;
            frame.channelCount++;
        }
    }

    /** A line on the events stream (AdvantageScope's Console tab), stamped with this loop. */
    public void event(String text) {
        if (frame.eventCount < MAX_EVENTS_PER_LOOP) {
            frame.events[frame.eventCount++] = text;
        } else {
            droppedEvents++;
        }
    }

    /** Hands this loop's frame to the writer and starts the next. Never waits. */
    public void commit() {
        if (!isLogging()) {
            frame.clearPerLoop();
            return;
        }
        Frame next = free.poll();
        if (next == null) {
            // Writer behind: keep this frame, lose its data. The match state and gamepads carry on.
            droppedLoops++;
            frame.clearPerLoop();
            return;
        }
        frame.timestampUs = clock.nowUs();
        carryOver(frame, next);
        full.offer(frame);
        frame = next;
    }

    /**
     * Ends this OpMode's part of the file but keeps it open for the next OpMode: writes
     * {@code lastEvent} and a disabled loop, then waits. If nobody {@link #resume}s it within
     * {@code closeAfterMs}, the writer closes the file itself.
     */
    public void pause(String lastEvent, long closeAfterMs) {
        if (!isLogging()) return;
        if (lastEvent != null) event(lastEvent);
        frame.mode = Mode.DISABLED;
        commit();
        synchronized (lifecycle) {
            paused = true;
            closeAtNs = System.nanoTime() + closeAfterMs * 1_000_000L;
        }
    }

    /**
     * Continues a paused log in a new OpMode. False when it has already closed (its pause timed
     * out, or it failed), in which case the caller opens a new file.
     */
    public boolean resume() {
        synchronized (lifecycle) {
            if (!paused || writerDone || closed || failure != null) return false;
            paused = false;
            return true;
        }
    }

    /**
     * The last frame, then the writer closes the file. Waits at most {@code waitMs} for it (stop
     * runs once, at the end of the OpMode); a writer still busy after that finishes on its own.
     */
    public void close(String lastEvent, long waitMs) {
        if (closed) return;
        if (lastEvent != null) event(lastEvent);
        if (droppedLoops > 0 || droppedEvents > 0) {
            event("log: dropped " + droppedLoops + " loops and " + droppedEvents + " events (writer behind)");
        }
        boolean writerRunning;
        synchronized (lifecycle) {
            writerRunning = failure == null && writerThread != null && !writerDone;
            paused = false;
        }
        if (writerRunning) {
            frame.timestampUs = clock.nowUs();
            frame.last = true;
            // Always room: the loop holds at most one frame outside the two queues.
            full.offer(frame);
            closed = true;
            try {
                writerThread.join(waitMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        closed = true;
    }

    /** The state that persists from loop to loop rides into the next frame. */
    private static void carryOver(Frame from, Frame to) {
        to.clearPerLoop();
        to.last = false;
        to.mode = from.mode;
        to.allianceStation = from.allianceStation;
        to.gamepad1.set(from.gamepad1);
        to.gamepad2.set(from.gamepad2);
    }

    // ---- Writer side -------------------------------------------------------------------------

    private final class Writer implements Runnable {
        private final OutputStream out;
        private final Syncer syncer;
        private final String header;
        private final Map<String, String> metadata;

        private WpiLog log;
        private final GamepadLog gamepad1Log = new GamepadLog(0);
        private final GamepadLog gamepad2Log = new GamepadLog(1);
        private final GamepadLog.State lastGamepad1 = new GamepadLog.State();
        private final GamepadLog.State lastGamepad2 = new GamepadLog.State();
        private boolean gamepadsWritten;
        private Mode lastMode;
        private long lastAllianceStation = -1;
        private long lastFlushNs = System.nanoTime();

        Writer(OutputStream out, Syncer syncer, String header, Map<String, String> metadata) {
            this.out = out;
            this.syncer = syncer;
            this.header = header;
            this.metadata = metadata;
        }

        @Override
        public void run() {
            try {
                log = new WpiLog(new WpiLogWriter(out, header));
                log.putMetadata("PoseFrame", AdvantageScopeFrame.DESCRIPTION);
                if (metadata != null) {
                    for (Map.Entry<String, String> e : metadata.entrySet()) {
                        log.putMetadata(e.getKey(), e.getValue());
                    }
                }
                while (true) {
                    Frame f = full.poll(100, TimeUnit.MILLISECONDS);
                    if (f != null) {
                        boolean last = f.last;
                        write(f);
                        framesWritten++;
                        f.last = false;
                        f.clearPerLoop();
                        free.offer(f);
                        if (last) break;
                    }
                    if (System.nanoTime() - lastFlushNs >= FLUSH_EVERY_MS * 1_000_000L) {
                        flush();
                    }
                    if (f == null && unclaimedPauseExpired()) break;
                }
                flush();
                log.close();
            } catch (Throwable t) {
                failure = "log write failed: " + t;
                closeQuietly();
            } finally {
                synchronized (lifecycle) {
                    writerDone = true;
                }
            }
        }

        /** True once, when a pause has gone unclaimed past its time: the writer then closes. */
        private boolean unclaimedPauseExpired() {
            synchronized (lifecycle) {
                if (paused && full.isEmpty() && System.nanoTime() >= closeAtNs) {
                    writerDone = true;
                    return true;
                }
                return false;
            }
        }

        private void write(Frame f) throws IOException {
            long us = f.timestampUs;
            if (f.mode != lastMode) {
                log.put(AdvantageScopeKeys.ENABLED, f.mode != Mode.DISABLED, us);
                log.put(AdvantageScopeKeys.AUTONOMOUS, f.mode == Mode.AUTONOMOUS, us);
                log.put(AdvantageScopeKeys.ROBOT_MODE, f.mode.key, us);
                lastMode = f.mode;
            }
            if (f.allianceStation != lastAllianceStation) {
                log.put(AdvantageScopeKeys.ALLIANCE_STATION, f.allianceStation, us);
                lastAllianceStation = f.allianceStation;
            }
            if (!gamepadsWritten || !f.gamepad1.sameAs(lastGamepad1)) {
                gamepad1Log.write(log, f.gamepad1, us);
                lastGamepad1.set(f.gamepad1);
            }
            if (!gamepadsWritten || !f.gamepad2.sameAs(lastGamepad2)) {
                gamepad2Log.write(log, f.gamepad2, us);
                lastGamepad2.set(f.gamepad2);
            }
            gamepadsWritten = true;
            if (f.hasPose) {
                double x = AdvantageScopeFrame.xMeters(f.xIn, f.yIn);
                double y = AdvantageScopeFrame.yMeters(f.xIn, f.yIn);
                double h = AdvantageScopeFrame.headingRad(f.headingRad);
                log.putPose2d("/Odometry/Robot", x, y, h, us);
                log.putPose3dFlat("/Odometry/Robot3d", x, y, 0.0, h, us);
                log.put("/Odometry/PedroInches/X", f.xIn, us);
                log.put("/Odometry/PedroInches/Y", f.yIn, us);
                log.put("/Odometry/PedroInches/HeadingDeg", Math.toDegrees(f.headingRad), us);
            }
            if (!Double.isNaN(f.loopMs)) {
                log.put("/Robot/LoopMs", f.loopMs, us);
            }
            for (int i = 0; i < f.channelCount; i++) {
                log.put(channelNames[f.channels[i]], f.values[i], us);
            }
            for (int i = 0; i < f.eventCount; i++) {
                // putEvent moves a second event in the same microsecond 1 µs on, so none is lost.
                log.putEvent(f.events[i], us);
            }
        }

        private void flush() throws IOException {
            log.flush();
            if (syncer != null) syncer.sync();
            lastFlushNs = System.nanoTime();
        }

        private void closeQuietly() {
            try {
                if (log != null) log.close();
                else out.close();
            } catch (IOException | RuntimeException ignored) {
                // Already failed; the reason is in failure.
            }
        }
    }
}
