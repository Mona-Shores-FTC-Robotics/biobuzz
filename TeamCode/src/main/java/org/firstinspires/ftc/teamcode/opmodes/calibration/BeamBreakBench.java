package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.util.LoopTimer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Bench test for the Adafruit 2168 IR break-beam (#213): does the wiring work, which channel is
 * the receiver's signal on, and what does a ball passing through look like?
 *
 * <p>It reads <b>all eight digital channels of every hub</b> from the hubs' bulk read, so the
 * sensor needs no configuration entry and no device name: plug it into any DIGITAL port under
 * whatever config is active, and the channel that changes when a hand breaks the beam is the one
 * the signal wire is on. The receiver's output is open-collector and the hub pulls its digital
 * inputs up, so:
 *
 * <ul>
 *   <li><b>HIGH</b>: beam clear, or nothing plugged in (the pull-up alone reads HIGH).</li>
 *   <li><b>LOW</b>: beam broken.</li>
 *   <li><b>LOW and never HIGH</b>: the receiver sees no beam. Emitter unpowered, not aimed at
 *       it, or too far.</li>
 * </ul>
 *
 * <p>For a ball rolling through, it counts raw breaks and <i>pieces</i>: breaks merged across
 * clear gaps shorter than {@link #mergeMs}. A ball's holes can let the beam through for a moment;
 * if one ball shows 2 or 3 breaks but 1 piece, the robot code needs that merge, and
 * {@code shortest gap} says how long it must be. A blip shorter than one loop can be missed, so
 * the loop time is on screen.
 *
 * <p>Standalone diagnostics, like {@code ValidateHardware}: no {@code Robot}, no
 * {@code Subsystem}, and nothing is driven. A (Cross) resets the counts.
 */
@Utility(name = "Beam Break Bench", description = "Every hub's digital channels live; find the beam-break's signal wire and watch a ball pass")
@Configurable
public class BeamBreakBench extends OpMode {

    public static final int CHANNELS = 8;

    /** Clear gaps shorter than this inside a break are a ball's holes, not two pieces. */
    public static double mergeMs = 60.0;

    private final List<LynxModule> hubs = new ArrayList<>();
    private final List<String> hubNames = new ArrayList<>();
    private final List<ChannelWatch[]> watches = new ArrayList<>();
    private final List<String[]> panelKeys = new ArrayList<>();
    private final LoopTimer loopTimer = new LoopTimer();
    private TelemetryManager panels;
    private String panelsProblem;
    private String fault;
    private boolean aWas;
    private String lastChanged = "none yet";

    @Override
    public void init() {
        // The parent hub (Control Hub) first, so the screen order is stable.
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            if (hub.isParent()) addHub(hub);
        }
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            if (!hub.isParent()) addHub(hub);
        }
        if (hubs.isEmpty()) fault = "No hubs in the active configuration";
        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
            if (panels == null) panelsProblem = "PanelsTelemetry returned no telemetry manager";
        } catch (RuntimeException | LinkageError e) {
            panels = null;
            panelsProblem = "not available at init: " + e;
        }
        telemetry.addLine("Beam Break Bench. INIT shows the readings; PLAY is the same. Cross (A) resets counts.");
        telemetry.update();
    }

    private void addHub(LynxModule hub) {
        hubs.add(hub);
        Iterator<String> names = hardwareMap.getNamesOf(hub).iterator();
        hubNames.add(names.hasNext() ? names.next() : "hub " + hub.getModuleAddress());
        ChannelWatch[] w = new ChannelWatch[CHANNELS];
        String[] keys = new String[CHANNELS];
        String prefix = hub.isParent() ? "dio_chub_" : "dio_ehub" + hub.getModuleAddress() + "_";
        for (int c = 0; c < CHANNELS; c++) {
            w[c] = new ChannelWatch();
            keys[c] = prefix + c;
        }
        watches.add(w);
        panelKeys.add(keys);
    }

    @Override
    public void init_loop() {
        step();
    }

    @Override
    public void loop() {
        step();
    }

    private void step() {
        loopTimer.lap();
        long now = System.nanoTime();
        boolean a = gamepad1.a;
        if (a && !aWas) {
            for (ChannelWatch[] w : watches) for (ChannelWatch cw : w) cw.reset();
            lastChanged = "none yet";
        }
        aWas = a;

        long mergeNanos = (long) (Math.max(0.0, mergeMs) * 1e6);
        for (int h = 0; h < hubs.size(); h++) {
            ChannelWatch[] w = watches.get(h);
            try {
                LynxModule.BulkData data = hubs.get(h).getBulkData();
                for (int c = 0; c < CHANNELS; c++) {
                    if (w[c].update(data.getDigitalChannelState(c), now, mergeNanos)) {
                        lastChanged = hubNames.get(h) + " channel " + c + " (port " + portLabel(c) + ")";
                    }
                }
            } catch (RuntimeException e) {
                fault = hubNames.get(h) + " read failed: " + (e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }
        report(now);
        publish();
    }

    private void report(long now) {
        if (fault != null) telemetry.addLine("FAULT: " + fault);
        telemetry.addLine("Last channel to change: " + lastChanged);
        telemetry.addLine("HIGH = clear or unplugged · LOW = beam broken");
        for (int h = 0; h < hubs.size(); h++) {
            ChannelWatch[] w = watches.get(h);
            telemetry.addLine("");
            telemetry.addLine(hubNames.get(h));
            for (int c = 0; c < CHANNELS; c += 2) {
                telemetry.addLine(String.format(Locale.US, "  port %s:  ch%d %s   ch%d %s",
                        portLabel(c), c, w[c].label(), c + 1, w[c + 1].label()));
            }
            for (int c = 0; c < CHANNELS; c++) {
                ChannelWatch cw = w[c];
                if (!cw.seen()) continue;
                telemetry.addLine(String.format(Locale.US,
                        "  ch%d: breaks %d · pieces %d · last break %s · shortest gap %s · changed %.1f s ago",
                        c, cw.breaks, cw.pieces, ms(cw.lastBreakNanos), ms(cw.shortestGapNanos),
                        (now - cw.lastChangeNanos) / 1e9));
            }
        }
        telemetry.addLine("");
        telemetry.addLine("Merge gaps under " + mergeMs + " ms into one piece (Panels: BeamBreakBench.mergeMs).");
        telemetry.addLine(loopTimer.summary());
        if (panelsProblem != null) telemetry.addLine("Panels: " + panelsProblem);
        telemetry.update();
    }

    private void publish() {
        if (panels == null) return;
        try {
            for (int h = 0; h < hubs.size(); h++) {
                ChannelWatch[] w = watches.get(h);
                String[] keys = panelKeys.get(h);
                for (int c = 0; c < CHANNELS; c++) {
                    panels.addData(keys[c], w[c].high ? 1.0 : 0.0);
                }
            }
            panels.addData("loop_ms", loopTimer.lastMs());
            panels.update();
        } catch (RuntimeException ignored) {
            // Telemetry never takes the bench down.
        }
    }

    /** The DIGITAL port label a channel is printed under on the hub: 0-1, 2-3, 4-5, 6-7. */
    static String portLabel(int channel) {
        int n = channel - channel % 2;
        return n + "-" + (n + 1);
    }

    private static String ms(long nanos) {
        return nanos < 0 ? "n/a" : String.format(Locale.US, "%.0f ms", nanos / 1e6);
    }

    /**
     * One digital channel's history: raw breaks (HIGH to LOW), pieces (breaks merged across short
     * clear gaps), the last break's length and the shortest clear gap seen. Times in nanoseconds;
     * -1 means not seen yet.
     */
    static final class ChannelWatch {
        boolean high = true;
        private boolean started;
        private boolean changed;
        int breaks;
        int pieces;
        long lastBreakNanos = -1;
        long shortestGapNanos = -1;
        long lastChangeNanos;
        private long breakStartNanos;
        private long clearStartNanos = -1;

        /** Takes this loop's reading. Returns true if the channel changed state. */
        boolean update(boolean nowHigh, long nowNanos, long mergeNanos) {
            if (!started) {
                started = true;
                high = nowHigh;
                lastChangeNanos = nowNanos;
                if (!nowHigh) breakStartNanos = nowNanos;
                return false;
            }
            if (nowHigh == high) return false;
            high = nowHigh;
            changed = true;
            lastChangeNanos = nowNanos;
            if (!nowHigh) {
                breaks++;
                breakStartNanos = nowNanos;
                if (clearStartNanos < 0) {
                    pieces++;
                } else {
                    long gap = nowNanos - clearStartNanos;
                    if (shortestGapNanos < 0 || gap < shortestGapNanos) shortestGapNanos = gap;
                    // Merge only into a piece already counted: after a reset there is none yet.
                    if (gap >= mergeNanos || pieces == 0) pieces++;
                }
            } else {
                lastBreakNanos = nowNanos - breakStartNanos;
                clearStartNanos = nowNanos;
            }
            return true;
        }

        /** Has this channel changed since the last reset? Untouched channels stay off the screen. */
        boolean seen() {
            return changed;
        }

        String label() {
            return (high ? "HIGH" : "LOW ") + (changed ? "*" : " ");
        }

        void reset() {
            changed = false;
            breaks = 0;
            pieces = 0;
            lastBreakNanos = -1;
            shortestGapNanos = -1;
            clearStartNanos = -1;
        }
    }
}
