package org.firstinspires.ftc.teamcode.logging;

import java.io.IOException;
import java.util.Locale;

/**
 * Which robot on the field a log entry belongs to, and the keys it is logged under, so a log with
 * more than one robot (a duo Auto, a partner standing still, the other alliance) opens in
 * AdvantageScope with every robot where it is.
 *
 * <p>Every log that shows a robot uses these keys, so every log is read the same way:
 * <ul>
 *   <li>{@code /Odometry/<Name>} ({@code struct:Pose2d}) and {@code /Odometry/<Name>3d}
 *       ({@code struct:Pose3d}): where that robot is;</li>
 *   <li>{@code /Odometry/<Name>PedroInches/X}, {@code Y}, {@code HeadingDeg}: the same pose in
 *       Visualizer numbers;</li>
 *   <li>{@code /Path/<Name>}, {@code /Path/<Name>Full}, {@code /Path/<Name>Target}: the path it is
 *       driving, all of its paths, and where the current one ends;</li>
 *   <li>its mechanisms under {@link #prefix}.</li>
 * </ul>
 * {@link #ROBOT}, our robot, keeps the keys every single-robot log already uses
 * ({@code /Odometry/Robot3d}, {@code /Path/Active}, {@code /Odometry/PedroInches/*}, mechanisms at
 * the root), so a log with one robot is unchanged and a saved AdvantageScope layout still works.
 *
 * <p>A log with more than one robot also writes {@link #ALL_3D} (and {@link #ALL_2D}): every robot
 * on the field as one array. Dragged onto AdvantageScope's 3D Field as a Robot, it draws all of them
 * at once; the per-robot keys draw each in its own model or Ghost colour. {@link #putViewingHint}
 * writes these instructions into the log's Metadata tab.
 *
 * <p>Only simulations and offline tools log more than one robot. A robot logs itself, as
 * {@link #ROBOT}.
 */
public enum FieldRobot {
    /** Our robot. */
    ROBOT("Robot", "", "/Path/Active", "/Path/Full", "/Path/Target", "/Odometry/PedroInches"),
    /** Our alliance partner. */
    PARTNER("Partner"),
    /** The other alliance's robots. */
    OPPONENT_A("OpponentA"),
    OPPONENT_B("OpponentB");

    /** {@code struct:Pose2d[]}: every robot in the log, in slot order, for the 2D field. */
    public static final String ALL_2D = "/Odometry/AllRobots";
    /** {@code struct:Pose3d[]}: every robot in the log, in slot order, for the 3D field. */
    public static final String ALL_3D = "/Odometry/AllRobots3d";

    /** As it appears in keys and on screens: {@code Robot}, {@code Partner}, … */
    public final String label;
    /** {@code struct:Pose2d}. */
    public final String pose;
    /** {@code struct:Pose3d}, flat on the tiles. */
    public final String pose3d;
    /** Prepended to this robot's mechanism keys: {@code ""} for ours, {@code /Partner} for the partner's. */
    public final String prefix;
    /** {@code struct:Pose2d[]}: the path being driven now, empty when none. */
    public final String activePath;
    /** {@code struct:Pose2d[]}: every path of the Auto, written once. */
    public final String fullPath;
    /** {@code struct:Pose2d}: where the current path ends. */
    public final String target;
    /** Folder of {@code X}, {@code Y}, {@code HeadingDeg}: the pose in Pedro inches and degrees. */
    public final String pedroInches;

    FieldRobot(String label) {
        this(label, "/" + label, "/Path/" + label, "/Path/" + label + "Full", "/Path/" + label + "Target",
                "/Odometry/" + label + "PedroInches");
    }

    FieldRobot(String label, String prefix, String activePath, String fullPath, String target, String pedroInches) {
        this.label = label;
        this.pose = "/Odometry/" + label;
        this.pose3d = "/Odometry/" + label + "3d";
        this.prefix = prefix;
        this.activePath = activePath;
        this.fullPath = fullPath;
        this.target = target;
        this.pedroInches = pedroInches;
    }

    /** The robot in slot {@code index}: 0 is ours, 1 our partner, 2 and 3 the other alliance. */
    public static FieldRobot slot(int index) {
        FieldRobot[] all = values();
        if (index < 0 || index >= all.length) {
            throw new IllegalArgumentException("a field holds " + all.length + " robots, not robot " + (index + 1));
        }
        return all[index];
    }

    /**
     * Where this robot is, given in Pedro's frame (inches, radians CCW): {@link #pose},
     * {@link #pose3d} and the {@link #pedroInches} numbers.
     */
    public void putPose(WpiLog log, double xIn, double yIn, double headingRad, long timestampUs) throws IOException {
        double x = AdvantageScopeFrame.xMeters(xIn, yIn);
        double y = AdvantageScopeFrame.yMeters(xIn, yIn);
        double h = AdvantageScopeFrame.headingRad(headingRad);
        log.putPose2d(pose, x, y, h, timestampUs);
        log.putPose3dFlat(pose3d, x, y, 0.0, h, timestampUs);
        log.put(pedroInches + "/X", xIn, timestampUs);
        log.put(pedroInches + "/Y", yIn, timestampUs);
        log.put(pedroInches + "/HeadingDeg", Math.toDegrees(headingRad), timestampUs);
    }

    /**
     * Every robot on the field as one value, {@link #ALL_2D} and {@link #ALL_3D}, written once a
     * loop after each robot's own {@link #putPose}.
     *
     * @param pedroXyHeading packed {@code x, y, heading} per robot, in slot order: Pedro inches and
     *                       radians
     */
    public static void putAll(WpiLog log, double[] pedroXyHeading, long timestampUs) throws IOException {
        if (pedroXyHeading.length % 3 != 0) {
            throw new IllegalArgumentException("pose array length must be a multiple of 3");
        }
        int count = pedroXyHeading.length / 3;
        double[] flat = new double[3 * count];
        double[] solid = new double[7 * count];
        for (int i = 0; i < count; i++) {
            double xIn = pedroXyHeading[3 * i], yIn = pedroXyHeading[3 * i + 1];
            double x = AdvantageScopeFrame.xMeters(xIn, yIn);
            double y = AdvantageScopeFrame.yMeters(xIn, yIn);
            double h = AdvantageScopeFrame.headingRad(pedroXyHeading[3 * i + 2]);
            flat[3 * i] = x;
            flat[3 * i + 1] = y;
            flat[3 * i + 2] = h;
            // A turn about z only: the quaternion (cos h/2, 0, 0, sin h/2).
            solid[7 * i] = x;
            solid[7 * i + 1] = y;
            solid[7 * i + 2] = 0.0;
            solid[7 * i + 3] = Math.cos(h / 2);
            solid[7 * i + 4] = 0.0;
            solid[7 * i + 5] = 0.0;
            solid[7 * i + 6] = Math.sin(h / 2);
        }
        log.putPose2dArray(ALL_2D, flat, timestampUs);
        log.putPose3dArray(ALL_3D, solid, timestampUs);
    }

    /**
     * Writes, on the Metadata tab, which robots the log holds and how to see them all in
     * AdvantageScope. {@code what[i]} says what robot {@code i} is doing (an Auto's name, "stands
     * still"); it may be shorter than {@code count}.
     */
    public static void putViewingHint(WpiLog log, int count, String... what) throws IOException {
        StringBuilder robots = new StringBuilder();
        for (int i = 0; i < count; i++) {
            FieldRobot r = slot(i);
            if (robots.length() > 0) robots.append("; ");
            robots.append(r.label).append(" = ").append(r.pose3d);
            if (i < what.length && what[i] != null) robots.append(" (").append(what[i]).append(')');
        }
        log.putMetadata("Robots", String.format(Locale.ROOT, "%d on the field: %s", count, robots));
        if (count > 1) {
            log.putMetadata("RobotsHowToView", "3D Field: drag " + ALL_3D + " onto the field as a Robot to see all "
                    + count + " at once. To tell them apart, drag " + ROBOT.pose3d + " as a Robot and each other "
                    + "/Odometry/<Name>3d as a Ghost in its own colour. 2D Field: " + ALL_2D + ".");
        }
    }
}
