package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

/**
 * One of the team's spots ({@code TeamCode/autos/spots.json}): a named pose every Auto can use,
 * drawn for red in Pedro's frame. The spots themselves are in the generated
 * {@code localization.generated.TeamSpots}, which the Visualizer writes beside {@code spots.json}
 * whenever the team's spots are saved, so the Java that needs a spot ({@link StartPositions},
 * {@link AutoLaunchSpots}) reads it there and never keeps a copy of its own.
 */
public final class TeamSpot {

    /** The spot's name in {@code spots.json}, side first: {@code RIGHT_START}, {@code LEFT_AUTO_HIVE}. */
    public final String name;
    /** Inches, Pedro's frame, red. */
    public final double x;
    /** Inches, Pedro's frame, red. */
    public final double y;
    /** Degrees, CCW; NaN for a spot reached at any heading (a park). */
    public final double headingDeg;

    public TeamSpot(String name, double x, double y, double headingDeg) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.headingDeg = headingDeg;
    }

    /** False for a spot reached at whatever heading the robot arrives with. */
    public boolean hasHeading() {
        return !Double.isNaN(headingDeg);
    }

    /**
     * The spot as a pose, red, radians.
     *
     * @throws IllegalStateException for a spot with no heading: it is not a pose
     */
    public Pose pose() {
        if (!hasHeading()) {
            throw new IllegalStateException(name + " has no heading in spots.json, so it is not a pose");
        }
        return new Pose(x, y, Math.toRadians(headingDeg));
    }

    @Override
    public String toString() {
        return name;
    }
}
