package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

/**
 * A Pedro {@link Localizer}: the Pinpoint, corrected by AprilTag fixes through {@link PoseFusion}.
 * Anything that reads a pose — the drivetrain, a Pedro {@code Follower}, a turret — reads this and
 * never needs to know fusion exists. With no fixes it is exactly the Pinpoint.
 */
public final class FusedLocalizer implements Localizer {

    private final Localizer odometry;
    private final PoseFusion fusion = LocalizationTuning.newFusion();

    public FusedLocalizer(Localizer odometry) {
        this.odometry = odometry;
    }

    @Override
    public void update() {
        odometry.update();
        Pose raw = odometry.pose();
        fusion.recordOdometry(System.nanoTime(), raw.x(), raw.y(), raw.heading());
    }

    @Override
    public Pose pose() {
        Pose raw = odometry.pose();
        return new Pose(raw.x() + fusion.offsetX(), raw.y() + fusion.offsetY(), raw.heading());
    }

    @Override
    public MotionState state() {
        return odometry.state().withPose(pose());
    }

    /**
     * Pedro's entry point (a {@code Follower} calls this). Treated as a field pose of unknown
     * quality: headings become field headings, but nothing is trusted until fixes confirm it.
     */
    @Override
    public void setPose(Pose pose) {
        setPose(pose, LocalizationTuning.declaredStartSigmaIn);
    }

    /** Put the robot at a field pose known to within {@code sigmaIn}. */
    public void setPose(Pose pose, double sigmaIn) {
        odometry.setPose(pose);
        fusion.reset(sigmaIn, true);
    }

    @Override
    public void reset() {
        odometry.reset();
        fusion.reset(Double.POSITIVE_INFINITY, false);
    }

    /**
     * Offer a CELL sighting. Used only when the cell's state is settled UP or DOWN and that state's
     * field point is measured; otherwise returns null and nothing changes.
     */
    public PoseFusion.Verdict offer(CellSighting sighting, HiveCellState state) {
        Vec3 field = HiveFieldPoints.rowCentre(sighting.cell(), state);
        if (field == null) {
            return null;
        }
        Vec3 seen = sighting.rowCentreRobot();
        return fusion.addFix(sighting.captureTimeNs(), field.x(), field.y(), seen.x(), seen.y(),
                LocalizationTuning.fixVariance(sighting.groundRangeIn(), sighting.tagCount()));
    }

    public PoseFusion fusion() {
        return fusion;
    }
}
