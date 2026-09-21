package org.firstinspires.ftc.teamcode.vision;

/**
 * Published BIOBUZZ HIVE geometry, transcribed from the FIRST Competition Manual.
 *
 * <p>Every value here is quoted from <i>Competition Manual V1, Section 9 (ARENA)</i>
 * with the figure it came from. Nothing is measured, inferred from CAD, or guessed;
 * the two derived quantities at the bottom are marked as such and are computed from
 * these constants rather than typed in.
 *
 * <h2>The mechanism</h2>
 *
 * The HIVE Structure sits at the centre of the field: one frame carrying a red HIVE
 * and a blue HIVE, each a <b>bi-stable</b> rocker with a CELL at either end, pivoting
 * about an axis at the top of the frame. Manual §9.6:
 *
 * <blockquote>"Each HIVE is bi-stable and will hold its position until enough POLLEN
 * or NECTAR are LAUNCHED into the upwards-facing CELL."</blockquote>
 *
 * That is what makes a field pose tractable at all — two discrete resting positions
 * per cell rather than a continuum.
 *
 * <h2>What is deliberately absent</h2>
 *
 * <b>No cluster field positions.</b> The manual publishes heights and spacings but no
 * XY coordinates for the AprilTag clusters, and §9.9 says why — the Reference Holes
 * used to align the sticker "can be used to measure the location of the AprilTag
 * Cluster relative to the rest of the FIELD". FIRST expects teams to measure it. The
 * Initial Field Element Assembly Guide likewise locates the sticker by reference hole
 * alignment, with no numeric offsets.
 *
 * <p>So the eight cluster poses the field-pose layer needs are not derivable from
 * here. See "Open work" in {@code TeamCode/README.md}.
 */
public final class HiveGeometry {

    private HiveGeometry() {
        // Constants only.
    }

    // ---------------------------------------------------------------
    // Frame — Manual §9.6.1, Figure 9-8
    // ---------------------------------------------------------------

    /** Height of the pivot axis above the TILES, inches. */
    public static final double PIVOT_AXIS_HEIGHT_IN = 43.95;

    /** Frame width, inches. */
    public static final double FRAME_WIDTH_IN = 49.46;

    /** Frame depth at its base, which is also its widest point, inches. */
    public static final double FRAME_DEPTH_IN = 38.95;

    // ---------------------------------------------------------------
    // HIVE and CELL — Manual §9.6.2, Figures 9-9 and 9-10
    // ---------------------------------------------------------------

    /** Spacing between the two CELLS of one HIVE, inches. Figure 9-9. */
    public static final double CELL_SPACING_IN = 18.84;

    /** Overall width of one HIVE assembly, inches. Figure 9-9. */
    public static final double HIVE_OVERALL_WIDTH_IN = 42.91;

    /** Lateral spacing between the red and blue HIVE centres, inches. Figure 9-10. */
    public static final double HIVE_CENTER_TO_CENTER_IN = 25.5;

    /** Tilt of a settled CELL from horizontal, degrees. Figure 9-10. */
    public static final double CELL_TILT_DEG = 30.0;

    /** Lowest point of the HIVE above the TILES, inches. Figure 9-10. */
    public static final double HIVE_BOTTOM_HEIGHT_IN = 25.5;

    /** Bottom edge of the raised CELL's opening above the TILES, inches. Figure 9-10. */
    public static final double OPENING_BOTTOM_HEIGHT_IN = 53.5;

    /** Top edge of the raised CELL's opening above the TILES, inches. Figure 9-10. */
    public static final double OPENING_TOP_HEIGHT_IN = 65.6;

    /** CELL opening width, inches. Manual §9.6.2, Figure 9-11. */
    public static final double OPENING_WIDTH_IN = 20.0;

    /** CELL opening height measured in the opening's own plane, inches. */
    public static final double OPENING_HEIGHT_IN = 14.0;

    /** CELL depth, inches. */
    public static final double CELL_DEPTH_IN = 12.0;

    // ---------------------------------------------------------------
    // Derived
    // ---------------------------------------------------------------

    /**
     * How much a given CELL's tag plane rises between its lowered and raised
     * positions, inches. <b>Derived, not published.</b>
     *
     * <p>For any point rigidly attached to the rocker, the height difference between
     * the two stable states works out to {@code 2 * dx * sin(tilt)}, where {@code dx}
     * is the point's horizontal distance from the pivot axis with the rocker level —
     * the vertical component cancels. Taking the CELL centres as symmetric about the
     * pivot puts {@code dx} at half the cell spacing, giving
     * {@code CELL_SPACING_IN * sin(CELL_TILT_DEG)}.
     *
     * <p>This is what makes height a usable state discriminator: roughly nine inches
     * of separation, comfortably more than AprilTag range noise. It holds even though
     * the <em>absolute</em> heights are still unmeasured, because it depends only on
     * the geometry of the swing.
     */
    public static final double STATE_HEIGHT_DELTA_IN =
            CELL_SPACING_IN * Math.sin(Math.toRadians(CELL_TILT_DEG));

    /**
     * Vertical extent of the raised CELL's opening, inches. <b>Derived.</b>
     *
     * <p>The opening is {@link #OPENING_HEIGHT_IN} tall in its own plane, tilted by
     * {@link #CELL_TILT_DEG}, so it spans {@code OPENING_HEIGHT_IN * cos(tilt)}
     * vertically. {@code HiveGeometryTest} checks this against the manual's own
     * published opening heights, which is what confirms the 30° is the tilt from
     * horizontal rather than from vertical.
     */
    public static final double OPENING_VERTICAL_EXTENT_IN =
            OPENING_HEIGHT_IN * Math.cos(Math.toRadians(CELL_TILT_DEG));
}
