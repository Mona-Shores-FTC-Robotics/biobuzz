package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.vision.CameraMount;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds AdvantageScope robot models from the simulator's designs: {@value #ROBOT_NAME}
 * ({@link RobotDesign#flatIntake}, the Flat Intake, the baseline robot the published logs use), {@value #PROTOTYPE_NAME}
 * ({@link RobotDesign#buildersPrototype}) and {@value #FULL_WIDTH_NAME}
 * ({@link RobotDesign#springHoodFullWidth}, what a wider intake would gain), so what you see is what
 * the simulator does.
 *
 * <p>In a 3D Field tab:
 * <ul>
 *   <li><b>The robot.</b> A mecanum chassis, the intake roller on the front, two flywheels that
 *       fling pieces up into a curved deflector, the Limelight on its post with a green rod along where it looks, and a faint
 *       see-through box: the body pieces bounce off.</li>
 *   <li><b>The intake.</b> A see-through orange box in front: once a ball's centre is inside it
 *       (and it isn't moving too fast), the intake takes it.</li>
 *   <li><b>You can look through the camera.</b> The {@code config.json} declares the Limelight as a
 *       fixed camera, so right-clicking the field view offers {@value #CAMERA_NAME}: the view from
 *       the lens, at the Limelight 3A's field of view, following the logged robot pose.</li>
 * </ul>
 *
 * <p><b>Frames.</b> The model is written straight in AdvantageScope's robot frame, which is the
 * robot frame {@link CameraMount} uses: {@code +X} forward, {@code +Y} left, {@code +Z} up, metres,
 * origin on the floor under the point Pedro tracks. So the config needs no rotations and the
 * camera's position is the mount's, converted to metres.
 *
 * <p><b>Camera angles.</b> AdvantageScope applies a camera's rotations in order to a camera looking
 * along {@code +X}. A rotation about {@code +Y} by a positive angle tips {@code +X} toward
 * {@code −Z} (its bundled KitBot pitches its cameras down with {@code +20}), so the mount's
 * upward pitch goes in negated; then yaw about {@code +Z}, positive left, as the mount has it. That
 * is the same order {@link CameraMount#toRobotFrame} applies them.
 *
 * <p><b>Field of view.</b> Limelight 3A: 640 × 480, 54.5° horizontal (Limelight's spec sheet, and
 * the numbers AdvantageScope's own FTC drive base uses). If the AprilTag pipeline runs at another
 * resolution, the aspect ratio here should follow it.
 *
 * <p>Nothing here is CAD: the shapes are drawn from a handful of numbers, so the folders can be
 * rebuilt anywhere with no download. Change a design or {@link CameraMount}, rebuild, recopy.
 */
final class RobotAssets {

    static final String FOLDER = "Robot_BIOBUZZ";
    static final String ROBOT_NAME = "BIOBUZZ Robot";
    /** The build team's prototype (RobotDesign#buildersPrototype), for logs simulated with it. */
    static final String PROTOTYPE_FOLDER = "Robot_BIOBUZZPrototype";
    static final String PROTOTYPE_NAME = "BIOBUZZ Prototype";
    /** The 18 in robot with an intake 90% of its width (RobotDesign#springHoodFullWidth): the "what if". */
    static final String FULL_WIDTH_FOLDER = "Robot_BIOBUZZFullWidth";
    static final String FULL_WIDTH_NAME = "BIOBUZZ Full width";

    /** What a model has beyond the design's numbers: the prototype's pinwheel (drawn only, not simulated). */
    enum Look { PLAIN, PROTOTYPE, FLAT_INTAKE }
    static final String CAMERA_NAME = "Limelight";

    /** Limelight 3A, as AdvantageScope's FTC drive base declares it. */
    static final int[] LIMELIGHT_RESOLUTION = {640, 480};
    static final double LIMELIGHT_HFOV_DEG = 54.5;

    // Drawing only (the simulator uses RobotDesign's numbers): goBILDA 104 mm mecanum wheels, a deck,
    // the intake roller, the flywheels and the prototype's pinwheel, sized from the build team's CAD
    // (5 Oct 2026), +-15%.
    static final double WHEEL_RADIUS_IN = 2.05;
    static final double WHEEL_WIDTH_IN = 1.5;
    static final double FRONT_WHEEL_SETBACK_IN = 3.0;
    static final int ROLLERS = 10;
    static final double CHANNEL_IN = 1.5;
    static final double SIDE_PLATE_IN = 0.25; // the frame's outer side plates, outside the wheels
    static final double SIDE_PLATE_HEIGHT_IN = 4.5;
    static final double[] BATTERY_SIZE_IN = {5.6, 2.2, 1.4};
    static final double[] HUB_SIZE_IN = {4.1, 5.6, 1.0}; // REV Control Hub, about 103 x 143 mm
    static final double DECK_Z_IN = 2.5;
    static final double DECK_THICKNESS_IN = 0.25;
    static final double INTAKE_ROLLER_RADIUS_IN = 0.75;
    static final double FLYWHEEL_RADIUS_IN = 2.0;
    static final double FLYWHEEL_HEIGHT_IN = 3.0;
    static final double DEFLECTOR_LENGTH_IN = 5.0;
    static final double LAUNCH_RISE_IN = 4.0; // the flywheels' axles are this far below the launch point ...
    static final double LAUNCH_SETBACK_IN = 0.8; // ... and this far behind it
    static final double SHOT_PATH_THICKNESS_IN = 0.25;
    static final double SHOT_PATH_LENGTH_IN = 10.0;
    static final double PINWHEEL_RADIUS_IN = 1.95;
    static final double PINWHEEL_AHEAD_IN = 0.9; // its centre, ahead of the frame's front ...
    static final double PINWHEEL_INSET_IN = 0.5; // ... and in from its right side
    static final double PINWHEEL_HEIGHT_IN = 4.2;
    /** Limelight 3A housing, roughly: depth along the lens axis, width, height. */
    static final double[] LIMELIGHT_BOX_IN = {1.0, 3.0, 2.0};
    static final double MAST_SIZE_IN = 1.0;
    static final double VIEW_ROD_LENGTH_IN = 8.0;
    static final double VIEW_ROD_THICKNESS_IN = 0.3;

    /* The spill-study sketches below (side walls, shapes): an 18 in chassis slab, drawn from these. */
    /** The FTC size limit. Drawing only: the camera's position does not depend on it. */
    static final double CHASSIS_SIZE_IN = 18.0;
    static final double CHASSIS_BOTTOM_IN = 0.5;
    static final double CHASSIS_TOP_IN = 3.0;

    /*
     * The side walls (RobotDesign#sideWallsSlideIn): an 18 in square robot whose side walls slide
     * forward 6 in, making it 24 in long. Each wall has a one-way flap at the bottom: it swings
     * inward only, so POLLEN (2.8 in) pushed against it rolls under and in and cannot roll back out,
     * and the opening is too low for NECTAR (3.6 in). The walls are the model's two articulated
     * components, so a log's {@code SideWalls/Components} poses slide them.
     */
    static final String WALLS_FOLDER = "Robot_BIOBUZZWalls";
    static final String WALLS_NAME = "BIOBUZZ Robot (side walls)";
    /** Component order in {@value #WALLS_FOLDER} and in a log's {@code SideWalls/Components}. */
    static final int LEFT_WALL = 0, RIGHT_WALL = 1;
    /** The walls slide this far forward: 18 in long stowed, 24 in out (R105: 18 x 24 in). */
    static final double WALL_SLIDE_IN = 6.0;
    static final double WALL_HEIGHT_IN = 6.0;
    static final double WALL_THICKNESS_IN = 0.25;
    /** Top of the door opening: between POLLEN's 2.8 in and NECTAR's 3.6 in. */
    static final double DOOR_TOP_IN = 3.2;
    static final double DOOR_BOTTOM_IN = 0.4;

    /*
     * The spill-study shapes (BodyShape#SHOWN): one model whose components are the shapes, each drawn
     * whole at the robot's origin. A log puts the shape it ran at zero and the rest out of sight
     * {@value #HIDDEN_Z_M} m under the field ({@link #shapeComponents}), so one layout shows them all.
     */
    static final String SHAPES_FOLDER = "Robot_BIOBUZZShapes";
    static final String SHAPES_NAME = "BIOBUZZ Robot (shapes)";
    static final double HIDDEN_Z_M = -20;
    /** The same for {@link AutoSim}'s match logs: {@link BodyShape#MATCH}, hooks folded and down. */
    static final String MATCH_FOLDER = "Robot_BIOBUZZMatchShapes";
    static final String MATCH_NAME = "BIOBUZZ Robot (match shapes)";

    private static final double M = AdvantageScopeFrame.METERS_PER_INCH;

    private RobotAssets() {
    }

    /**
     * {@value #ROBOT_NAME}, the one robot model (mentor, 6 Oct 2026): the Limelight in its base, and every
     * design a log can show as a component, in {@link BodyShape#MATCH}'s order. A log puts the design it ran at
     * the robot and the others out of sight ({@code BodyShape/Components}, {@link AutoSim}), so the one layout
     * shows any log's robot with nothing to pick. The Flat Intake and its guides are drawn in full (wheels,
     * frame, intake, launcher, the Rigid V's flaps); the others as the shape study's sketches. Append designs,
     * never reorder: older logs name components by their place.
     */
    static File robot(File out) throws IOException {
        File dir = new File(out, FOLDER);
        dir.mkdirs();
        MeshBuilder base = new MeshBuilder();
        addLimelight(base, CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn,
                CameraMount.pitchDeg, CameraMount.yawDeg, DECK_Z_IN);
        Files.write(new File(dir, "model.glb").toPath(), base.glb(ROBOT_NAME).write());
        RobotDesign flat = RobotDesign.flatIntake();
        for (int i = 0; i < BodyShape.MATCH.length; i++) {
            String name = BodyShape.MATCH[i].name;
            Glb part = name.equals("flat intake") || name.equals("flat intake, hook chassis")
                    ? model(flat, Look.FLAT_INTAKE, false)
                    : name.equals("flat intake, rigid V")
                    ? model(AutoStudyTest.flatIntakeWith("flat intake, rigid V", 0), Look.FLAT_INTAKE, false)
                    : name.startsWith("flat intake, rigid V ")  // its width and angle variants, drawn in full too
                    ? model(AutoStudyTest.designs().get(name), Look.FLAT_INTAKE, false)
                    : shapesRobot(BodyShape.MATCH[i]);
            Files.write(new File(dir, "model_" + i + ".glb").toPath(), part.write());
        }
        Files.write(new File(dir, "config.json").toPath(), config(ROBOT_NAME, BodyShape.MATCH.length,
                CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn,
                CameraMount.pitchDeg, CameraMount.yawDeg).getBytes(StandardCharsets.UTF_8));
        return dir;
    }

    /**
     * Writes {@value #FOLDER}, {@value #PROTOTYPE_FOLDER} and {@value #FULL_WIDTH_FOLDER} into {@code out}, and the
     * spill-study sketches {@value #WALLS_FOLDER}, {@value #SHAPES_FOLDER} and {@value #MATCH_FOLDER}; returns the first.
     */
    static File build(File out) throws IOException {
        File dir = robot(out);
        write(out, PROTOTYPE_FOLDER, PROTOTYPE_NAME, model(RobotDesign.buildersPrototype(), Look.PROTOTYPE));
        write(out, FULL_WIDTH_FOLDER, FULL_WIDTH_NAME, model(RobotDesign.springHoodFullWidth(), Look.PLAIN));
        File walls = new File(out, WALLS_FOLDER);
        walls.mkdirs();
        Files.write(new File(walls, "model.glb").toPath(), wallsBase(
                CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn,
                CameraMount.pitchDeg, CameraMount.yawDeg).write());
        Files.write(new File(walls, "model_" + LEFT_WALL + ".glb").toPath(), wall(1, 0).write());
        Files.write(new File(walls, "model_" + RIGHT_WALL + ".glb").toPath(), wall(-1, 0).write());
        Files.write(new File(walls, "config.json").toPath(), config(WALLS_NAME, 2,
                CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn,
                CameraMount.pitchDeg, CameraMount.yawDeg).getBytes(StandardCharsets.UTF_8));
        File shapes = new File(out, SHAPES_FOLDER);
        shapes.mkdirs();
        MeshBuilder base = new MeshBuilder();
        addLimelight(base, CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn,
                CameraMount.pitchDeg, CameraMount.yawDeg, CHASSIS_TOP_IN);
        Files.write(new File(shapes, "model.glb").toPath(), base.glb(SHAPES_NAME).write());
        for (int i = 0; i < BodyShape.SHOWN.length; i++) {
            Files.write(new File(shapes, "model_" + i + ".glb").toPath(), shapesRobot(BodyShape.SHOWN[i]).write());
        }
        Files.write(new File(shapes, "config.json").toPath(), config(SHAPES_NAME, BodyShape.SHOWN.length,
                CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn,
                CameraMount.pitchDeg, CameraMount.yawDeg).getBytes(StandardCharsets.UTF_8));
        File match = new File(out, MATCH_FOLDER);
        match.mkdirs();
        Files.write(new File(match, "model.glb").toPath(), base.glb(MATCH_NAME).write());
        for (int i = 0; i < BodyShape.MATCH.length; i++) {
            Files.write(new File(match, "model_" + i + ".glb").toPath(), shapesRobot(BodyShape.MATCH[i]).write());
        }
        Files.write(new File(match, "config.json").toPath(), config(MATCH_NAME, BodyShape.MATCH.length,
                CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn,
                CameraMount.pitchDeg, CameraMount.yawDeg).getBytes(StandardCharsets.UTF_8));
        return dir;
    }

    private static File write(File out, String folder, String name, Glb model) throws IOException {
        File dir = new File(out, folder);
        dir.mkdirs();
        Files.write(new File(dir, "model.glb").toPath(), model.write());
        Files.write(new File(dir, "config.json").toPath(), config(name,
                CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn,
                CameraMount.pitchDeg, CameraMount.yawDeg).getBytes(StandardCharsets.UTF_8));
        return dir;
    }

    static String config(double forwardIn, double leftIn, double upIn, double pitchDeg, double yawDeg) {
        return config(ROBOT_NAME, forwardIn, leftIn, upIn, pitchDeg, yawDeg);
    }

    static String config(String name, double forwardIn, double leftIn, double upIn, double pitchDeg, double yawDeg) {
        return config(name, 0, forwardIn, leftIn, upIn, pitchDeg, yawDeg);
    }

    /**
     * A robot config with the Limelight camera and {@code components} articulated components, each
     * zeroed where it was drawn: a component pose moves it from there, in the robot frame.
     */
    static String config(String name, int components, double forwardIn, double leftIn, double upIn,
            double pitchDeg, double yawDeg) {
        Map<String, Object> camera = new LinkedHashMap<>();
        camera.put("name", CAMERA_NAME);
        camera.put("rotations", Arrays.asList(rotation("y", -pitchDeg), rotation("z", yawDeg)));
        camera.put("position", Glb.toList(new double[] {forwardIn * M, leftIn * M, upIn * M}));
        camera.put("resolution", Arrays.asList((Object) LIMELIGHT_RESOLUTION[0], LIMELIGHT_RESOLUTION[1]));
        camera.put("fov", LIMELIGHT_HFOV_DEG);

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("name", name);
        config.put("isFTC", true);
        config.put("rotations", new ArrayList<>());
        config.put("position", Glb.toList(new double[] {0, 0, 0}));
        config.put("cameras", Arrays.asList((Object) camera));
        List<Object> parts = new ArrayList<>();
        for (int i = 0; i < components; i++) {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("zeroedRotations", new ArrayList<>());
            c.put("zeroedPosition", Glb.toList(new double[] {0, 0, 0}));
            parts.add(c);
        }
        config.put("components", parts);
        return MiniJson.write(config);
    }

    private static Map<String, Object> rotation(String axis, double degrees) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("axis", axis);
        r.put("degrees", degrees);
        return r;
    }

    /**
     * The robot as the simulator models {@code d}: its body (the see-through box pieces bounce off),
     * a mecanum chassis, the intake roller and, see-through orange, the volume a piece's centre must
     * be in for the intake to take it (FieldSim#inIntake: so a ball "is ours" once its centre enters
     * it), the two flywheels flinging pieces up into a deflector whose lip is where they leave
     * (RobotDesign#exitForwardIn, #exitHeightIn), the
     * Limelight from CameraMount and, for the prototype, the pinwheel that takes POLLEN out of a
     * FLOWER (drawn only: the simulator doesn't use it yet). Robot frame, inches: +X forward, +Y left.
     */
    static Glb model(RobotDesign d, Look look) {
        return model(d, look, true);
    }

    /** As {@link #model(RobotDesign, Look)}; {@code limelight} false leaves the Limelight to the model's base. */
    static Glb model(RobotDesign d, Look look, boolean limelight) {
        MeshBuilder b = new MeshBuilder();
        double half = d.frameIn / 2;
        // The body the simulator bounces pieces off (RobotDesign#bodyHeightIn), barely there.
        b.box("Body", new double[] {0.75, 0.78, 0.82, 0.05},
                new double[] {0, 0, d.bodyHeightIn / 2}, new double[] {d.frameIn, d.frameIn, d.bodyHeightIn}, IDENTITY_3);

        // Mecanum wheels at the corners: a yellow hub and rollers round the rim at 45 deg, in the usual X
        // seen from above. An intake wider than the gap between the front wheels runs in front of them:
        // the front wheels sit back, their front edge FRONT_WHEEL_SETBACK_IN behind the frame's front (as
        // in the Flat Intake's CAD).
        double wheelX = half - WHEEL_RADIUS_IN - 0.2, wheelY = half - SIDE_PLATE_IN - 0.2 - WHEEL_WIDTH_IN / 2;
        boolean wide = d.intakeWidthIn / 2 > wheelY - WHEEL_WIDTH_IN / 2;
        double frontWheelX = wide ? half - FRONT_WHEEL_SETBACK_IN - WHEEL_RADIUS_IN : wheelX;
        for (int fx = -1; fx <= 1; fx += 2) {
            for (int fy = -1; fy <= 1; fy += 2) {
                double[] c = {fx > 0 ? frontWheelX : -wheelX, fy * wheelY, WHEEL_RADIUS_IN};
                String where = (fx > 0 ? "front " : "back ") + (fy > 0 ? "left" : "right");
                mecanum(b, where, c, fx * fy);
            }
        }

        // The frame: a side plate outside each side's wheels and a channel inside them, so each wheel is
        // held from both sides (a real build is never cantilevered); a channel across the back at axle
        // height, one across the front above the intake's opening, and two cross members; open in the middle.
        double[] blue = {0.16, 0.32, 0.72, 1};
        double plateY = half - SIDE_PLATE_IN / 2;
        double railY = wheelY - WHEEL_WIDTH_IN / 2 - 0.2 - CHANNEL_IN / 2;
        double railZ = WHEEL_RADIUS_IN;
        for (int side = -1; side <= 1; side += 2) {
            String where = side > 0 ? "left" : "right";
            b.box("Side plate " + where, blue, new double[] {0, side * plateY, 0.5 + SIDE_PLATE_HEIGHT_IN / 2},
                    new double[] {d.frameIn, SIDE_PLATE_IN, SIDE_PLATE_HEIGHT_IN}, IDENTITY_3);
            b.box("Chassis " + where, blue, new double[] {-0.75, side * railY, railZ},
                    new double[] {d.frameIn - 1.5 - CHANNEL_IN, CHANNEL_IN, CHANNEL_IN}, IDENTITY_3);
        }
        double frontZ = d.intakeHeightIn + INTAKE_ROLLER_RADIUS_IN + CHANNEL_IN / 2 + 0.2;
        b.box("Chassis front", blue, new double[] {half - 0.2 - CHANNEL_IN / 2, 0, frontZ},
                new double[] {CHANNEL_IN, d.frameIn - 2 * SIDE_PLATE_IN, CHANNEL_IN}, IDENTITY_3);
        b.box("Chassis back", blue, new double[] {-(half - 0.2 - CHANNEL_IN / 2), 0, railZ},
                new double[] {CHANNEL_IN, d.frameIn - 2 * SIDE_PLATE_IN, CHANNEL_IN}, IDENTITY_3);
        for (int end = -1; end <= 1; end += 2) {
            b.box("Cross member " + (end > 0 ? "front" : "back"), new double[] {0.55, 0.57, 0.6, 1},
                    new double[] {end * 2.2, 0, railZ}, new double[] {1.0, 2 * railY - CHANNEL_IN, 1.0}, IDENTITY_3);
        }
        b.box("Floor plate", new double[] {0.55, 0.57, 0.6, 1}, new double[] {0, 0, railZ - 0.5},
                new double[] {1.0, 2 * railY - CHANNEL_IN, 0.25}, IDENTITY_3);
        // The electronics: the battery slung low in the middle, the Control Hub flat above it.
        b.box("Battery", new double[] {0.22, 0.22, 0.24, 1}, new double[] {0.2, 0, BATTERY_SIZE_IN[2] / 2 + 0.7},
                BATTERY_SIZE_IN, IDENTITY_3);
        b.box("Control Hub", new double[] {0.08, 0.08, 0.09, 1},
                new double[] {0.2, 0, railZ + CHANNEL_IN / 2 + HUB_SIZE_IN[2] / 2 + 0.2}, HUB_SIZE_IN, IDENTITY_3);
        b.box("Hub label", new double[] {0.9, 0.45, 0.1, 1},
                new double[] {0.2, 0, railZ + CHANNEL_IN / 2 + HUB_SIZE_IN[2] + 0.22},
                new double[] {HUB_SIZE_IN[0] * 0.6, HUB_SIZE_IN[1] * 0.25, 0.04}, IDENTITY_3);

        // The intake: a roller across the front at the top of its opening, and the volume its takes from.
        double mouth = half + d.intakeReachIn;
        b.cylinder("Intake roller", new double[] {1.0, 0.45, 0.0, 1},
                new double[] {mouth - INTAKE_ROLLER_RADIUS_IN, 0, d.intakeHeightIn}, INTAKE_ROLLER_RADIUS_IN,
                d.intakeWidthIn, IDENTITY_3);
        double reach = d.intakeOnContact ? FieldSim.POLLEN_RADIUS_IN + FieldSim.INTAKE_CONTACT_SLACK_IN : 3;
        double top = d.intakeOnContact ? d.intakeHeightIn - FieldSim.POLLEN_RADIUS_IN : d.intakeHeightIn;
        b.box("Pickup volume", new double[] {1.0, 0.55, 0.0, 0.3},
                new double[] {mouth + (reach - 2) / 2, 0, top / 2}, new double[] {reach + 2, d.intakeWidthIn, top}, IDENTITY_3);

        // The launcher, as the build team's prototypes have it: two flywheels side by side on axles
        // pointing forward, on uprights, throwing a piece straight up between them into a deflector plate
        // (a dummy: none is designed yet) that sends it off forward at the launch angle from the launch
        // point the simulator uses (RobotDesign#exitForwardIn, #exitHeightIn, #fixedPitchDeg). A thin
        // line shows the piece's path.
        double pitch = Double.isNaN(d.fixedPitchDeg) ? 75 : d.fixedPitchDeg;
        double lean = Math.toRadians(90 - pitch);
        double[] dir = {Math.sin(lean), 0, Math.cos(lean)};
        double[] back = {-Math.cos(lean), 0, Math.sin(lean)}; // across the plate, toward the back
        double flyR = FLYWHEEL_RADIUS_IN, gap = FieldSim.NECTAR_RADIUS_IN + flyR - 0.4;
        double flyX = d.exitForwardIn - LAUNCH_SETBACK_IN, flyZ = d.exitHeightIn - LAUNCH_RISE_IN;
        for (int side = -1; side <= 1; side += 2) {
            String where = side > 0 ? "left" : "right";
            b.cylinder("Flywheel " + where, new double[] {0.2, 0.2, 0.22, 1},
                    new double[] {flyX, side * gap, flyZ}, flyR, FLYWHEEL_HEIGHT_IN, AXIS_X);
            double uprightTop = flyZ + flyR + 0.5;
            b.box("Launcher upright " + where, new double[] {0.95, 0.7, 0.1, 1},
                    new double[] {flyX, side * (gap + 0.6), (DECK_Z_IN + uprightTop) / 2},
                    new double[] {2 * flyR + 1.2, 0.25, uprightTop - DECK_Z_IN}, IDENTITY_3);
        }
        double[] exit = {d.exitForwardIn, 0, d.exitHeightIn};
        double c = Math.cos(lean), sn = Math.sin(lean);
        double[] tilt = {c, 0, sn, 0, 1, 0, -sn, 0, c}; // +Z leaned forward by `lean`
        double offset = FieldSim.NECTAR_RADIUS_IN + 0.15;
        b.box("Deflector", new double[] {0.75, 0.88, 1.0, 0.5},
                new double[] {exit[0] + dir[0] * 1.5 + back[0] * offset, 0, exit[2] + dir[2] * 1.5 + back[2] * offset},
                new double[] {0.25, 2 * gap, DEFLECTOR_LENGTH_IN}, tilt);
        // The piece's path: up through the wheels to the plate, then off at the launch angle.
        double up = d.exitHeightIn - flyZ;
        double rise = Math.atan2(d.exitForwardIn - flyX, up);
        double[] riseTilt = {Math.cos(rise), 0, Math.sin(rise), 0, 1, 0, -Math.sin(rise), 0, Math.cos(rise)};
        double riseLength = Math.hypot(d.exitForwardIn - flyX, up);
        b.box("Shot path up", new double[] {0.6, 1.0, 0.3, 0.6},
                new double[] {(flyX + exit[0]) / 2, 0, (flyZ + exit[2]) / 2},
                new double[] {SHOT_PATH_THICKNESS_IN, SHOT_PATH_THICKNESS_IN, riseLength}, riseTilt);
        b.box("Shot path out", new double[] {0.6, 1.0, 0.3, 0.6},
                new double[] {exit[0] + dir[0] * SHOT_PATH_LENGTH_IN / 2, 0, exit[2] + dir[2] * SHOT_PATH_LENGTH_IN / 2},
                new double[] {SHOT_PATH_THICKNESS_IN, SHOT_PATH_THICKNESS_IN, SHOT_PATH_LENGTH_IN}, tilt);

        if (look == Look.PROTOTYPE) {
            b.cylinder("Pinwheel", new double[] {0.85, 0.86, 0.88, 1},
                    new double[] {half + PINWHEEL_AHEAD_IN, -(half - PINWHEEL_INSET_IN), PINWHEEL_HEIGHT_IN},
                    PINWHEEL_RADIUS_IN, 0.4, AXIS_X);
        }

        if (d.hasFlaps() && !d.flapsDeploy) {
            // Fixed flaps (the Rigid V): a thin plate from each front corner out to its free end, the tiles up.
            double halfWidth = d.frameWidthIn / 2;
            for (int side = -1; side <= 1; side += 2) {
                double fa = Math.atan2(side * d.flapOutIn, d.flapForwardIn), fc = Math.cos(fa), fs = Math.sin(fa);
                b.box(side > 0 ? "Left flap" : "Right flap", new double[] {0.2, 0.45, 0.85, 1},
                        new double[] {half + d.flapForwardIn / 2, side * (halfWidth + d.flapOutIn / 2), d.flapHeightIn / 2},
                        new double[] {d.flapLengthIn(), RobotDesign.FLAP_THICKNESS_IN, d.flapHeightIn},
                        new double[] {fc, -fs, 0, fs, fc, 0, 0, 0, 1});
            }
        }
        if (limelight) {
            addLimelight(b, CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn,
                    CameraMount.pitchDeg, CameraMount.yawDeg, DECK_Z_IN);
        }
        return b.glb(look == Look.PROTOTYPE ? PROTOTYPE_NAME : look == Look.FLAT_INTAKE ? ROBOT_NAME : FULL_WIDTH_NAME);
    }

    /**
     * A mecanum wheel at {@code c} on an axle along +Y: a yellow hub and {@value #ROLLERS} rollers round
     * the rim, each turned 45 deg between the axle and the rim's direction ({@code lean} +1 or -1).
     */
    private static void mecanum(MeshBuilder b, String where, double[] c, int lean) {
        b.cylinder("Wheel " + where, new double[] {0.95, 0.76, 0.0, 1}, c, WHEEL_RADIUS_IN - 0.85, WHEEL_WIDTH_IN - 0.3, IDENTITY_3);
        double r = 0.42, s = Math.sqrt(0.5);
        for (int k = 0; k < ROLLERS; k++) {
            double a = 2 * Math.PI * k / ROLLERS;
            double[] radial = {Math.cos(a), 0, Math.sin(a)};
            double[] tangent = {-Math.sin(a), 0, Math.cos(a)};
            double[] axis = {s * lean * tangent[0], s, s * lean * tangent[2]};
            double[] third = {radial[1] * axis[2] - radial[2] * axis[1], radial[2] * axis[0] - radial[0] * axis[2],
                    radial[0] * axis[1] - radial[1] * axis[0]};
            double[] rot = {radial[0], axis[0], third[0], radial[1], axis[1], third[1], radial[2], axis[2], third[2]};
            double[] at = {c[0] + (WHEEL_RADIUS_IN - r) * radial[0], c[1], c[2] + (WHEEL_RADIUS_IN - r) * radial[2]};
            b.cylinder("Roller " + (k + 1) + " (" + where + ")", new double[] {0.13, 0.13, 0.15, 1}, at, r, WHEEL_WIDTH_IN * 1.15, rot);
        }
    }

    /**
     * One spill-study shape, everything out: its frame, wheels and intake bar, its flaps or walls,
     * and, see-through, the {@link FieldSim#PLACEHOLDER_ROBOT_HEIGHT_IN} box the simulator bounces
     * pieces off.
     */
    static Glb shapesRobot(BodyShape shape) {
        MeshBuilder b = new MeshBuilder();
        double l = shape.length, w = shape.width, top = FieldSim.PLACEHOLDER_ROBOT_HEIGHT_IN;
        if (!shape.hookOnly) {
            b.box("Simulated body", new double[] {0.75, 0.78, 0.85, 0.15}, new double[] {0, 0, top / 2},
                    new double[] {l, w, top}, IDENTITY_3);
            b.box("Chassis", new double[] {0.55, 0.55, 0.6, 1},
                    new double[] {0, 0, (CHASSIS_BOTTOM_IN + CHASSIS_TOP_IN) / 2},
                    new double[] {l, w, CHASSIS_TOP_IN - CHASSIS_BOTTOM_IN}, IDENTITY_3);
            for (int fx = -1; fx <= 1; fx += 2) {
                for (int fy = -1; fy <= 1; fy += 2) {
                    b.box("Wheel", new double[] {0.12, 0.12, 0.14, 1},
                            new double[] {fx * (l / 2 - 3), fy * (w / 2 - 1.25), 2}, new double[] {4, 1.5, 4}, IDENTITY_3);
                }
            }
            b.box("Intake", new double[] {1.0, 0.55, 0.0, 1}, new double[] {l / 2 - 1, 0, CHASSIS_TOP_IN + 0.75},
                    new double[] {1.5, Math.min(w - 3, RobotDesign.standard().intakeWidthIn), 1.5}, IDENTITY_3);
        }
        if (shape.slide > 0) {
            addWall(b, 1, shape.slide);
            addWall(b, -1, shape.slide);
        }
        RobotDesign d = shape.design();
        if (d.hasFlaps()) {
            for (int side = -1; side <= 1; side += 2) {
                if (side > 0 ? !shape.leftArm : !shape.rightArm) continue;  // +y is the robot's left
                double a = Math.atan2(side * shape.out, shape.ahead), c = Math.cos(a), sn = Math.sin(a);
                b.box(side > 0 ? "Left flap" : "Right flap", new double[] {0.2, 0.45, 0.85, 1},
                        new double[] {l / 2 + shape.ahead / 2, side * (w / 2 + shape.out / 2), d.flapHeightIn / 2},
                        new double[] {d.flapLengthIn(), RobotDesign.FLAP_THICKNESS_IN, d.flapHeightIn},
                        new double[] {c, -sn, 0, sn, c, 0, 0, 0, 1});
            }
            if (shape.crossbeam) {
                b.box("Crossbeam", new double[] {0.2, 0.45, 0.85, 1},
                        new double[] {l / 2 + shape.ahead, 0, d.flapHeightIn / 2},
                        new double[] {RobotDesign.FLAP_THICKNESS_IN, w + 2 * shape.out, d.flapHeightIn}, IDENTITY_3);
            }
        }
        return b.glb(shape.name);
    }

    /**
     * {@value #MATCH_NAME} component poses: the chassis {@code shown}, and the hook {@code hook} (or none, -1)
     * swung {@code up} of the way from down (0) to stowed (1), 90 degrees up about its hinge at the bottom of
     * the chassis' front face, {@code length} in long. Stowed, it stands inside the front 4 in of the frame,
     * the arm up and the crossbeam across the top.
     */
    static double[] hookComponent(int shown, int hook, double up, double length) {
        double[] poses = shapeComponents(shown, BodyShape.MATCH.length);
        swing(poses, hook, up, length);
        return poses;
    }

    /** Shows component {@code hook} (none if -1) in {@code poses}, swung {@code up} as {@link #hookComponent} says. */
    static void swing(double[] poses, int hook, double up, double length) {
        if (hook < 0) return;
        double phi = -up * Math.PI / 2, hinge = length / 2 * M;  // about the robot's y axis; +x swings up
        poses[7 * hook] = hinge * (1 - Math.cos(phi));
        poses[7 * hook + 1] = 0;
        poses[7 * hook + 2] = hinge * Math.sin(phi);
        poses[7 * hook + 3] = Math.cos(phi / 2);
        poses[7 * hook + 4] = 0;
        poses[7 * hook + 5] = Math.sin(phi / 2);
        poses[7 * hook + 6] = 0;
    }

    /** The {@value #SHAPES_NAME} component poses that show {@code shown} and hide the rest. */
    static double[] shapeComponents(int shown) {
        return shapeComponents(shown, BodyShape.SHOWN.length);
    }

    /** As above, for a model of {@code count} components ({@value #MATCH_NAME} has {@link BodyShape#MATCH}'s). */
    static double[] shapeComponents(int shown, int count) {
        double[] poses = new double[7 * count];
        for (int i = 0; i < count; i++) {
            poses[7 * i + 2] = i == shown ? 0 : HIDDEN_Z_M;
            poses[7 * i + 3] = 1;
        }
        return poses;
    }

    /**
     * The side-walls robot without its walls: the chassis plate, stopping inside where the walls
     * run so they have room beside it, the front marker and the Limelight.
     */
    static Glb wallsBase(double forwardIn, double leftIn, double upIn, double pitchDeg, double yawDeg) {
        MeshBuilder b = new MeshBuilder();
        addWallsBase(b);
        addLimelight(b, forwardIn, leftIn, upIn, pitchDeg, yawDeg, CHASSIS_TOP_IN);
        return b.glb(WALLS_NAME);
    }

    /** One wall and its flap ({@code side} +1 left, −1 right), slid {@code slideIn} forward. */
    static Glb wall(int side, double slideIn) {
        MeshBuilder b = new MeshBuilder();
        addWall(b, side, slideIn);
        return b.glb(side > 0 ? "Left wall" : "Right wall");
    }

    /** The whole side-walls robot with its walls slid {@code slideIn} forward, as one model. */
    static Glb wallsRobot(double slideIn, double forwardIn, double leftIn, double upIn, double pitchDeg,
            double yawDeg) {
        MeshBuilder b = new MeshBuilder();
        addWallsBase(b);
        addWall(b, 1, slideIn);
        addWall(b, -1, slideIn);
        addLimelight(b, forwardIn, leftIn, upIn, pitchDeg, yawDeg, CHASSIS_TOP_IN);
        return b.glb(WALLS_NAME);
    }

    private static void addWallsBase(MeshBuilder b) {
        double h = CHASSIS_SIZE_IN / 2;
        b.box("Chassis", new double[] {0.55, 0.55, 0.6, 1},
                new double[] {0, 0, (CHASSIS_BOTTOM_IN + CHASSIS_TOP_IN) / 2},
                new double[] {CHASSIS_SIZE_IN, CHASSIS_SIZE_IN - 4 * WALL_THICKNESS_IN, CHASSIS_TOP_IN - CHASSIS_BOTTOM_IN},
                IDENTITY_3);
        b.box("Front", new double[] {1.0, 0.45, 0.0, 1},
                new double[] {h - 1.0, 0, CHASSIS_TOP_IN + 0.25},
                new double[] {2.0, CHASSIS_SIZE_IN - 4.0, 0.5},
                IDENTITY_3);
    }

    /** A wall from the top of the door to the top, and the flap that hangs below it, drawn shut. */
    private static void addWall(MeshBuilder b, int side, double slideIn) {
        double h = CHASSIS_SIZE_IN / 2, t = WALL_THICKNESS_IN;
        String label = side > 0 ? "Left" : "Right";
        double wallY = side * (h - t / 2);
        b.box(label + " wall", new double[] {0.2, 0.45, 0.85, 1},
                new double[] {slideIn, wallY, (DOOR_TOP_IN + WALL_HEIGHT_IN) / 2},
                new double[] {CHASSIS_SIZE_IN, t, WALL_HEIGHT_IN - DOOR_TOP_IN}, IDENTITY_3);
        b.box(label + " flap", new double[] {0.95, 0.85, 0.2, 1},
                new double[] {slideIn, wallY, (DOOR_BOTTOM_IN + DOOR_TOP_IN) / 2},
                new double[] {CHASSIS_SIZE_IN - 0.5, t, DOOR_TOP_IN - DOOR_BOTTOM_IN}, IDENTITY_3);
    }

    /** The Limelight at its mount, on a post from {@code fromZ}, with a rod along where it looks. */
    private static void addLimelight(MeshBuilder b, double forwardIn, double leftIn, double upIn, double pitchDeg,
            double yawDeg, double fromZ) {
        // The lens is at the mount point; the housing sits behind it along the optical axis.
        double[] r = mountRotation(pitchDeg, yawDeg);
        double[] lens = {forwardIn, leftIn, upIn};
        double[] housingCentre = add(lens, mul(r, new double[] {-LIMELIGHT_BOX_IN[0] / 2, 0, 0}));
        // A post up to the housing, so it reads as mounted rather than floating. Drawing only.
        double mastTop = housingCentre[2] - LIMELIGHT_BOX_IN[2] / 2;
        if (mastTop > fromZ) {
            b.box("Mast", new double[] {0.35, 0.35, 0.4, 1},
                    new double[] {housingCentre[0], housingCentre[1], (fromZ + mastTop) / 2},
                    new double[] {MAST_SIZE_IN, MAST_SIZE_IN, mastTop - fromZ},
                    IDENTITY_3);
        }
        b.box("Limelight", new double[] {0.12, 0.12, 0.14, 1}, housingCentre, LIMELIGHT_BOX_IN, r);
        double[] rodCentre = add(lens, mul(r, new double[] {VIEW_ROD_LENGTH_IN / 2, 0, 0}));
        b.box("View ray", new double[] {0.1, 0.85, 0.2, 1}, rodCentre,
                new double[] {VIEW_ROD_LENGTH_IN, VIEW_ROD_THICKNESS_IN, VIEW_ROD_THICKNESS_IN}, r);
    }

    /** Turn a cylinder's own +Y axis to +Z (upright) or to +X (facing forward). */
    private static final double[] AXIS_Z = {1, 0, 0, 0, 0, -1, 0, 1, 0};
    private static final double[] AXIS_X = {0, 1, 0, -1, 0, 0, 0, 0, 1};

    /**
     * Camera axes to robot axes, row-major 3×3: pitch up about {@code +Y}, then yaw about
     * {@code +Z}, as {@link CameraMount#toRobotFrame} does. Column 0 is where the lens points.
     */
    static double[] mountRotation(double pitchDeg, double yawDeg) {
        double p = Math.toRadians(pitchDeg), w = Math.toRadians(yawDeg);
        double cp = Math.cos(p), sp = Math.sin(p), cw = Math.cos(w), sw = Math.sin(w);
        double[] pitch = {cp, 0, -sp, 0, 1, 0, sp, 0, cp};
        double[] yaw = {cw, -sw, 0, sw, cw, 0, 0, 0, 1};
        return mul3(yaw, pitch);
    }

    private static final double[] IDENTITY_3 = {1, 0, 0, 0, 1, 0, 0, 0, 1};

    private static double[] mul3(double[] a, double[] b) {
        double[] o = new double[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) o[3 * i + j] += a[3 * i + k] * b[3 * k + j];
            }
        }
        return o;
    }

    static double[] mul(double[] m, double[] v) {
        return new double[] {
                m[0] * v[0] + m[1] * v[1] + m[2] * v[2],
                m[3] * v[0] + m[4] * v[1] + m[5] * v[2],
                m[6] * v[0] + m[7] * v[1] + m[8] * v[2]};
    }

    private static double[] add(double[] a, double[] b) {
        return new double[] {a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    /** Flat-shaded boxes, one node, mesh and material each, in a single buffer. */
    private static final class MeshBuilder {
        private final ByteArrayBuilder bin = new ByteArrayBuilder();
        private final List<Object> nodes = new ArrayList<>();
        private final List<Object> meshes = new ArrayList<>();
        private final List<Object> materials = new ArrayList<>();
        private final List<Object> accessors = new ArrayList<>();
        private final List<Object> bufferViews = new ArrayList<>();

        /** The face normals of a unit box, and each face's four corners, counter-clockwise from outside. */
        private static final int[][] FACES = {
                {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

        void box(String name, double[] rgba, double[] centreIn, double[] sizeIn, double[] rotation) {
            float[] positions = new float[24 * 3];
            float[] normals = new float[24 * 3];
            short[] indices = new short[36];
            int v = 0, i = 0;
            for (int[] n : FACES) {
                // Two axes spanning the face, ordered so (u × w) = n.
                double[] nn = {n[0], n[1], n[2]};
                double[] u = n[0] != 0 ? new double[] {0, n[0], 0} : n[1] != 0 ? new double[] {0, 0, n[1]} : new double[] {n[2], 0, 0};
                double[] w = cross(nn, u);
                double[][] corners = {
                        sum(nn, neg(u), neg(w)), sum(nn, u, neg(w)), sum(nn, u, w), sum(nn, neg(u), w)};
                double[] worldNormal = mul(rotation, nn);
                int first = v;
                for (double[] c : corners) {
                    double[] local = {c[0] * sizeIn[0] / 2, c[1] * sizeIn[1] / 2, c[2] * sizeIn[2] / 2};
                    double[] p = add(centreIn, mul(rotation, local));
                    for (int k = 0; k < 3; k++) {
                        positions[3 * v + k] = (float) (p[k] * M);
                        normals[3 * v + k] = (float) worldNormal[k];
                    }
                    v++;
                }
                int[] tri = {0, 1, 2, 0, 2, 3};
                for (int t : tri) indices[i++] = (short) (first + t);
            }

            mesh(name, rgba, positions, normals, indices);
        }

        /**
         * A cylinder of {@code radiusIn} and {@code lengthIn} along its local {@code +Y}, turned by
         * {@code rotation} and centred at {@code centreIn}: a wheel, roller or flywheel.
         */
        void cylinder(String name, double[] rgba, double[] centreIn, double radiusIn, double lengthIn,
                double[] rotation) {
            int n = 24;
            int vertices = n * 4 + 2 * (n + 1);
            float[] positions = new float[vertices * 3];
            float[] normals = new float[vertices * 3];
            short[] indices = new short[n * 6 + 2 * n * 3];
            int[] v = {0};
            int i = 0;
            java.util.function.BiConsumer<double[], double[]> put = (local, normal) -> {
                double[] p = add(centreIn, mul(rotation, local));
                double[] wn = mul(rotation, normal);
                for (int k = 0; k < 3; k++) {
                    positions[3 * v[0] + k] = (float) (p[k] * M);
                    normals[3 * v[0] + k] = (float) wn[k];
                }
                v[0]++;
            };
            double h = lengthIn / 2;
            for (int k = 0; k < n; k++) {
                double a0 = 2 * Math.PI * k / n, a1 = 2 * Math.PI * (k + 1) / n, am = (a0 + a1) / 2;
                double[] nn = {Math.cos(am), 0, Math.sin(am)};
                int first = v[0];
                put.accept(new double[] {radiusIn * Math.cos(a0), -h, radiusIn * Math.sin(a0)}, nn);
                put.accept(new double[] {radiusIn * Math.cos(a1), -h, radiusIn * Math.sin(a1)}, nn);
                put.accept(new double[] {radiusIn * Math.cos(a1), h, radiusIn * Math.sin(a1)}, nn);
                put.accept(new double[] {radiusIn * Math.cos(a0), h, radiusIn * Math.sin(a0)}, nn);
                for (int t : new int[] {0, 2, 1, 0, 3, 2}) indices[i++] = (short) (first + t);
            }
            for (int end = -1; end <= 1; end += 2) {
                int centre = v[0];
                put.accept(new double[] {0, end * h, 0}, new double[] {0, end, 0});
                for (int k = 0; k < n; k++) {
                    double a = 2 * Math.PI * k / n;
                    put.accept(new double[] {radiusIn * Math.cos(a), end * h, radiusIn * Math.sin(a)}, new double[] {0, end, 0});
                }
                for (int k = 0; k < n; k++) {
                    int a = centre + 1 + k, b = centre + 1 + (k + 1) % n;
                    indices[i++] = (short) centre;
                    indices[i++] = (short) (end > 0 ? b : a);
                    indices[i++] = (short) (end > 0 ? a : b);
                }
            }
            mesh(name, rgba, positions, normals, indices);
        }

        /** One mesh, its own node and material; a colour with alpha under 1 is drawn see-through. */
        private void mesh(String name, double[] rgba, float[] positions, float[] normals, short[] indices) {
            int count = positions.length / 3;
            int pos = accessor(floats(positions), count, "VEC3", 5126, 34962, bounds(positions));
            int nor = accessor(floats(normals), count, "VEC3", 5126, 34962, null);
            int idx = accessor(shorts(indices), indices.length, "SCALAR", 5123, 34963, null);

            Map<String, Object> pbr = new LinkedHashMap<>();
            pbr.put("baseColorFactor", Glb.toList(rgba));
            pbr.put("metallicFactor", 0.0);
            pbr.put("roughnessFactor", 0.8);
            Map<String, Object> material = new LinkedHashMap<>();
            material.put("name", name);
            material.put("pbrMetallicRoughness", pbr);
            if (rgba[3] < 1) {
                material.put("alphaMode", "BLEND");
                material.put("doubleSided", true);
            }
            materials.add(material);

            Map<String, Object> attributes = new LinkedHashMap<>();
            attributes.put("POSITION", pos);
            attributes.put("NORMAL", nor);
            Map<String, Object> primitive = new LinkedHashMap<>();
            primitive.put("attributes", attributes);
            primitive.put("indices", idx);
            primitive.put("material", materials.size() - 1);
            Map<String, Object> mesh = new LinkedHashMap<>();
            // NOSIMPLIFY: AdvantageScope's low-detail mode drops small meshes, which these all are.
            mesh.put("name", name + " NOSIMPLIFY");
            mesh.put("primitives", Arrays.asList((Object) primitive));
            meshes.add(mesh);

            Map<String, Object> node = new LinkedHashMap<>();
            node.put("name", name);
            node.put("mesh", meshes.size() - 1);
            nodes.add(node);
        }

        Glb glb(String rootName) {
            List<Object> children = new ArrayList<>();
            for (int n = 0; n < nodes.size(); n++) children.add(n);
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("name", rootName);
            root.put("children", children);
            nodes.add(root);

            Map<String, Object> scene = new LinkedHashMap<>();
            scene.put("nodes", Arrays.asList((Object) (nodes.size() - 1)));
            Map<String, Object> asset = new LinkedHashMap<>();
            asset.put("version", "2.0");
            asset.put("generator", "biobuzz RobotAssets");
            Map<String, Object> buffer = new LinkedHashMap<>();
            buffer.put("byteLength", bin.size());

            Map<String, Object> json = new LinkedHashMap<>();
            json.put("asset", asset);
            json.put("scene", 0);
            json.put("scenes", Arrays.asList((Object) scene));
            json.put("nodes", nodes);
            json.put("meshes", meshes);
            json.put("materials", materials);
            json.put("accessors", accessors);
            json.put("bufferViews", bufferViews);
            json.put("buffers", Arrays.asList((Object) buffer));
            return new Glb(json, bin.toArray());
        }

        private int accessor(byte[] data, int count, String type, int componentType, int target,
                double[][] minMax) {
            bin.align4();
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("buffer", 0);
            view.put("byteOffset", bin.size());
            view.put("byteLength", data.length);
            view.put("target", target);
            bufferViews.add(view);
            bin.add(data);

            Map<String, Object> a = new LinkedHashMap<>();
            a.put("bufferView", bufferViews.size() - 1);
            a.put("componentType", componentType);
            a.put("count", count);
            a.put("type", type);
            if (minMax != null) {
                a.put("min", Glb.toList(minMax[0]));
                a.put("max", Glb.toList(minMax[1]));
            }
            accessors.add(a);
            return accessors.size() - 1;
        }

        private static double[][] bounds(float[] xyz) {
            double[] min = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
            double[] max = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
            for (int i = 0; i < xyz.length; i++) {
                min[i % 3] = Math.min(min[i % 3], xyz[i]);
                max[i % 3] = Math.max(max[i % 3], xyz[i]);
            }
            return new double[][] {min, max};
        }

        private static byte[] floats(float[] values) {
            ByteBuffer b = ByteBuffer.allocate(values.length * 4).order(ByteOrder.LITTLE_ENDIAN);
            for (float f : values) b.putFloat(f);
            return b.array();
        }

        private static byte[] shorts(short[] values) {
            ByteBuffer b = ByteBuffer.allocate(values.length * 2).order(ByteOrder.LITTLE_ENDIAN);
            for (short s : values) b.putShort(s);
            return b.array();
        }

        private static double[] cross(double[] a, double[] b) {
            return new double[] {
                    a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
        }

        private static double[] neg(double[] a) {
            return new double[] {-a[0], -a[1], -a[2]};
        }

        private static double[] sum(double[] a, double[] b, double[] c) {
            return new double[] {a[0] + b[0] + c[0], a[1] + b[1] + c[1], a[2] + b[2] + c[2]};
        }
    }

    private static final class ByteArrayBuilder {
        private byte[] data = new byte[1024];
        private int size;

        void add(byte[] more) {
            while (size + more.length > data.length) data = Arrays.copyOf(data, data.length * 2);
            System.arraycopy(more, 0, data, size, more.length);
            size += more.length;
        }

        void align4() {
            while (size % 4 != 0) add(new byte[] {0});
        }

        int size() {
            return size;
        }

        byte[] toArray() {
            return Arrays.copyOf(data, size);
        }
    }
}
