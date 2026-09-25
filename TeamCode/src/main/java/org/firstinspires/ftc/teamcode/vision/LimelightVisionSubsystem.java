package org.firstinspires.ftc.teamcode.vision;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.teamcode.subsystems.Subsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Tracks the BIOBUZZ HIVE CELLs with a Limelight 3A and reports where they are
 * <em>relative to the robot</em>.
 *
 * <h2>This is not a localizer</h2>
 *
 * It deliberately produces no field pose. BIOBUZZ publishes no AprilTag field
 * positions — see {@link BiobuzzTags} — so the Limelight's MegaTag botpose has no
 * field map to solve against and would return the origin sentinel forever. None
 * of {@code getBotpose()}, {@code getBotpose_MT2()} or
 * {@code updateRobotOrientation()} is used here, and adding them back would be
 * building on sand.
 *
 * <p>What this gives a turret is range, bearing and elevation to a cell in the
 * chassis frame. Because the goals pivot, a remembered sighting propagated by
 * odometry is the right way to keep aiming when the cell leaves view — the anchor
 * is the target, not the field origin.
 *
 * <h2>Setup this depends on</h2>
 *
 * <ul>
 *   <li>Limelight configured in the hardware map under {@link #DEFAULT_DEVICE_NAME}.</li>
 *   <li>An AprilTag pipeline at {@link Tuning#pipelineIndex} with tag size set to
 *       {@link BiobuzzTags#TAG_SIZE_INCHES} — a wrong tag size scales every range
 *       by a constant and looks perfectly plausible.</li>
 *   <li>That pipeline emitting full 3D per-tag pose. Without it every fiducial is
 *       skipped; {@link #framesMissing3dPose()} counts that so a diagnostic can
 *       say so out loud instead of just reporting "no target".</li>
 * </ul>
 */
public class LimelightVisionSubsystem implements Subsystem {

    /** Hardware map name this looks for. */
    public static final String DEFAULT_DEVICE_NAME = "limelight";

    /** Live-tunable timing and pipeline settings. */
    @Configurable
    public static class Tuning {
        /** AprilTag pipeline index to select on init. */
        public static int pipelineIndex = 0;

        /** Minimum gap between Limelight polls, milliseconds. */
        public static long pollIntervalMs = 20L;

        /**
         * Discard a camera result the Limelight reports as older than this, ms.
         *
         * <p>Measured with the camera's own staleness figure rather than the RC's
         * wall clock, so it tracks the age of the measurement rather than the age
         * of our last read of it.
         */
        public static long maxResultStalenessMs = 200L;

        /** Drop a stored sighting once it is older than this, milliseconds. */
        public static long sightingExpiryMs = 1000L;
    }

    /** Whether the camera is streaming. */
    public enum State { OFF, STREAMING, UNAVAILABLE }

    private final Limelight3A limelight;
    private final boolean available;
    private final String unavailableReason;

    private State state = State.OFF;

    private final Map<HiveCell, CellSighting> sightings = new EnumMap<>(HiveCell.class);
    private final Map<HiveCell, CellStateTracker> stateTrackers = new EnumMap<>(HiveCell.class);

    private long lastPollMs = 0L;
    private long lastResultTimestampNs = Long.MIN_VALUE;
    private long framesMissing3dPose = 0L;
    private long freshResultCount = 0L;
    private double lastPeriodicMs = 0.0;
    private LLResult lastResult;

    public LimelightVisionSubsystem(HardwareMap hardwareMap) {
        this(hardwareMap, DEFAULT_DEVICE_NAME);
    }

    public LimelightVisionSubsystem(HardwareMap hardwareMap, String deviceName) {
        Limelight3A device = null;
        String reason = null;
        try {
            device = hardwareMap.get(Limelight3A.class, deviceName);
        } catch (RuntimeException e) {
            // Absent or misconfigured hardware disables vision rather than taking
            // the whole OpMode down — a robot with a dead camera should still drive.
            reason = e.getMessage() == null ? e.toString() : e.getMessage();
        }
        this.limelight = device;
        this.available = device != null;
        this.unavailableReason = reason;
        if (!available) this.state = State.UNAVAILABLE;
    }

    /** True if the Limelight was found in the hardware map. */
    public boolean isAvailable() { return available; }

    /** Why the camera is unavailable, or null if it is available. */
    public String unavailableReason() { return unavailableReason; }

    public State state() { return state; }

    /** Selects the configured pipeline and starts streaming. Safe if unavailable. */
    @Override
    public void initialize() {
        if (!available) {
            state = State.UNAVAILABLE;
            return;
        }
        limelight.pipelineSwitch(Tuning.pipelineIndex);
        limelight.start();
        state = State.STREAMING;
    }

    /**
     * One update step. Called every loop, either directly by a LinearOpMode or through
     * {@link Subsystem#periodic()} under the Ivy scheduler.
     *
     * <p>{@code periodic()} is inherited unchanged from {@link Subsystem} — the default there is
     * exactly what this class used to declare for itself.
     */
    @Override
    public void update() {
        long startNs = System.nanoTime();
        try {
            if (!available) return;

            long nowMs = System.currentTimeMillis();
            if (nowMs - lastPollMs < Tuning.pollIntervalMs) return;
            lastPollMs = nowMs;

            // Expire first. poll() overwrites a cell's sighting in place, so a
            // stale entry polled in the same tick would look fresh by the time
            // expireStaleSightings() saw it — and its tracker would never be
            // reset. The pre-blackout candidate run would then be extended by
            // the new observation instead of restarted, handing back a settled
            // UP or DOWN with none of the three-sample dwell re-earned. Only
            // reachable when a tick is missed across the expiry window (a loop
            // stall, a GC pause), which is exactly when it must not happen.
            expireStaleSightings();
            poll();
        } finally {
            lastPeriodicMs = (System.nanoTime() - startNs) / 1_000_000.0;
        }
    }

    private void poll() {
        LLResult result = limelight.getLatestResult();
        lastResult = result;
        if (result == null || !result.isValid()) return;

        // The camera's own age for this measurement, not the age of our last read.
        if (result.getStaleness() > Tuning.maxResultStalenessMs) return;

        // Re-reading the same frame is not a new measurement. Without this a
        // stationary robot looks like it is producing fresh data every tick, which
        // makes any noise statistic gathered downstream far too optimistic.
        long resultTimestampNs = result.getControlHubTimeStampNanos();
        if (resultTimestampNs == lastResultTimestampNs) return;
        lastResultTimestampNs = resultTimestampNs;
        freshResultCount++;

        List<LLResultTypes.FiducialResult> fiducials = result.getFiducialResults();
        if (fiducials == null || fiducials.isEmpty()) return;

        Map<HiveCell, List<CellSighting.Member>> byCell = new EnumMap<>(HiveCell.class);
        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            HiveCell cell = BiobuzzTags.cellForTag(fiducial.getFiducialId());
            if (cell == null) continue; // not a BIOBUZZ cluster member

            Vec3 cameraSpace = cameraSpaceInches(fiducial.getTargetPoseCameraSpace());
            if (cameraSpace == null) {
                framesMissing3dPose++;
                continue;
            }

            // Per-fiducial area, not the frame-level LLResult.getTa(): the latter
            // describes whatever the camera considers its primary target, which
            // need not be this tag.
            CellSighting.Member member = new CellSighting.Member(
                    fiducial.getFiducialId(),
                    CameraMount.toRobotFrame(cameraSpace),
                    fiducial.getTargetArea());

            List<CellSighting.Member> members = byCell.get(cell);
            if (members == null) {
                members = new ArrayList<>(BiobuzzTags.MEMBERS_PER_CLUSTER);
                byCell.put(cell, members);
            }
            members.add(member);
        }

        long captureTimeNs = System.nanoTime()
                - (long) (result.getStaleness() * 1_000_000.0);
        for (Map.Entry<HiveCell, List<CellSighting.Member>> entry : byCell.entrySet()) {
            HiveCell cell = entry.getKey();
            CellSighting sighting =
                    CellSighting.fromMembers(cell, entry.getValue(), captureTimeNs);
            sightings.put(cell, sighting);
            trackerFor(cell).update(
                    CellStateTracker.classifyByHeight(sighting.rowCentreRobot().z()),
                    captureTimeNs);
        }
    }

    /**
     * Converts a Limelight camera-space pose to inches, or null if the pipeline
     * did not supply one.
     *
     * <p>The camera reports an all-zero position when it has no 3D solution, which
     * is a sentinel rather than "the tag is inside the lens".
     */
    private static Vec3 cameraSpaceInches(Pose3D pose) {
        if (pose == null) return null;
        Position position = pose.getPosition();
        if (position == null || position.unit == null) return null;

        Position inches = position.toUnit(DistanceUnit.INCH);
        if (Math.abs(inches.x) < 1e-6 && Math.abs(inches.y) < 1e-6 && Math.abs(inches.z) < 1e-6) {
            return null;
        }
        return new Vec3(inches.x, inches.y, inches.z);
    }

    private void expireStaleSightings() {
        long cutoffNs = System.nanoTime() - Tuning.sightingExpiryMs * 1_000_000L;
        Iterator<Map.Entry<HiveCell, CellSighting>> it = sightings.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<HiveCell, CellSighting> entry = it.next();
            if (entry.getValue().captureTimeNs() < cutoffNs) {
                // A cell that has not been observed recently must not retain its
                // previous settled state. Otherwise state(cell) could report UP
                // or DOWN indefinitely after the robot turns away from the cell.
                CellStateTracker tracker = stateTrackers.get(entry.getKey());
                if (tracker != null) tracker.reset();
                it.remove();
            }
        }
    }

    // ------------------------------------------------------------------
    // Queries. None of these mutate state, so reading twice gives the same
    // answer and a telemetry call cannot consume a result the control loop
    // was about to act on.
    // ------------------------------------------------------------------

    private CellStateTracker trackerFor(HiveCell cell) {
        CellStateTracker tracker = stateTrackers.get(cell);
        if (tracker == null) {
            tracker = new CellStateTracker();
            stateTrackers.put(cell, tracker);
        }
        return tracker;
    }

    /**
     * Which resting position {@code cell} is settled in, or
     * {@link HiveCellState#UNKNOWN} if it is mid-tip, unseen, or the classifier
     * geometry in {@link CellStateTracker.Geometry} has not been measured yet.
     *
     * <p>A field pose must not be derived from a cell reporting UNKNOWN.
     */
    public HiveCellState state(HiveCell cell) {
        CellSighting sighting = sightings.get(cell);
        if (sighting == null
                || sighting.ageMs() > Tuning.sightingExpiryMs) {
            return HiveCellState.UNKNOWN;
        }

        CellStateTracker tracker = stateTrackers.get(cell);
        return tracker == null ? HiveCellState.UNKNOWN : tracker.settledState();
    }

    /** The state tracker for {@code cell}, for diagnostics. Never null. */
    public CellStateTracker stateTracker(HiveCell cell) {
        return trackerFor(cell);
    }

    /** The most recent unexpired sighting of {@code cell}, or null. */
    public CellSighting sighting(HiveCell cell) {
        return cell == null ? null : sightings.get(cell);
    }

    /** True if {@code cell} has an unexpired sighting. */
    public boolean sees(HiveCell cell) {
        return sighting(cell) != null;
    }

    /** Every unexpired sighting. */
    public List<CellSighting> sightings() {
        return Collections.unmodifiableList(new ArrayList<>(sightings.values()));
    }

    /**
     * The best current sighting for {@code alliance}, preferring more visible tags
     * and then larger image area. Null if neither of that alliance's cells is
     * currently visible.
     */
    public CellSighting bestSightingFor(Alliance alliance) {
        CellSighting best = null;
        for (CellSighting candidate : sightings.values()) {
            if (!candidate.cell().belongsTo(alliance)) continue;
            if (best == null || betterThan(candidate, best)) best = candidate;
        }
        return best;
    }

    private static boolean betterThan(CellSighting candidate, CellSighting incumbent) {
        if (candidate.tagCount() != incumbent.tagCount()) {
            return candidate.tagCount() > incumbent.tagCount();
        }
        return candidate.totalAreaPercent() > incumbent.totalAreaPercent();
    }

    /** How many distinct camera frames have been accepted. */
    public long freshResultCount() { return freshResultCount; }

    /**
     * Fiducials skipped because the pipeline supplied no 3D pose. Nonzero while
     * tags are visible means the pipeline is not configured to output full 3D.
     */
    public long framesMissing3dPose() { return framesMissing3dPose; }

    /** Duration of the last {@link #update()}, milliseconds. */
    public double lastPeriodicMs() { return lastPeriodicMs; }

    /** The last polled result. Diagnostics only — prefer the sighting accessors. */
    public LLResult lastResult() { return lastResult; }

    /** Stops streaming. Safe to call during OpMode teardown. */
    @Override
    public void stop() {
        try {
            if (available) limelight.stop();
        } catch (RuntimeException ignored) {
            // The hub may already be tearing down; a failure to stop is not worth
            // masking the real reason the OpMode is ending.
        }
        state = available ? State.OFF : State.UNAVAILABLE;
        sightings.clear();
        for (CellStateTracker tracker : stateTrackers.values()) tracker.reset();
    }
}
