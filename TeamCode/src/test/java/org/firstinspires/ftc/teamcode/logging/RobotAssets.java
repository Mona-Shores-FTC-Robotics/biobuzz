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
 * ({@link RobotDesign#springHoodFullWidth}, the design the published logs use) and
 * {@value #PROTOTYPE_NAME} ({@link RobotDesign#buildersPrototype}), so what you see is what the
 * simulator does.
 *
 * <p>In a 3D Field tab:
 * <ul>
 *   <li><b>The robot.</b> A mecanum chassis, the intake roller on the front, two flywheels where
 *       pieces leave, the Limelight on its post with a green rod along where it looks, and a faint
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
    static final String CAMERA_NAME = "Limelight";

    /** Limelight 3A, as AdvantageScope's FTC drive base declares it. */
    static final int[] LIMELIGHT_RESOLUTION = {640, 480};
    static final double LIMELIGHT_HFOV_DEG = 54.5;

    // Drawing only (the simulator uses RobotDesign's numbers): goBILDA 104 mm mecanum wheels, a deck,
    // the intake roller, the flywheels and the prototype's pinwheel, sized from the build team's CAD
    // (5 Oct 2026), +-15%.
    static final double WHEEL_RADIUS_IN = 2.05;
    static final double WHEEL_WIDTH_IN = 1.5;
    static final double DECK_Z_IN = 2.5;
    static final double DECK_THICKNESS_IN = 0.25;
    static final double INTAKE_ROLLER_RADIUS_IN = 0.75;
    static final double FLYWHEEL_RADIUS_IN = 2.0;
    static final double FLYWHEEL_HEIGHT_IN = 3.0;
    static final double PINWHEEL_RADIUS_IN = 1.95;
    static final double PINWHEEL_AHEAD_IN = 0.9; // its centre, ahead of the frame's front ...
    static final double PINWHEEL_INSET_IN = 0.5; // ... and in from its right side
    static final double PINWHEEL_HEIGHT_IN = 4.2;
    /** Limelight 3A housing, roughly: depth along the lens axis, width, height. */
    static final double[] LIMELIGHT_BOX_IN = {1.0, 3.0, 2.0};
    static final double MAST_SIZE_IN = 1.0;
    static final double VIEW_ROD_LENGTH_IN = 8.0;
    static final double VIEW_ROD_THICKNESS_IN = 0.3;

    private static final double M = AdvantageScopeFrame.METERS_PER_INCH;

    private RobotAssets() {
    }

    /** Writes {@value #FOLDER} and {@value #PROTOTYPE_FOLDER} into {@code out}; returns the first. */
    static File build(File out) throws IOException {
        File dir = write(out, FOLDER, ROBOT_NAME, model(RobotDesign.springHoodFullWidth(), false));
        write(out, PROTOTYPE_FOLDER, PROTOTYPE_NAME, model(RobotDesign.buildersPrototype(), true));
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
        config.put("components", new ArrayList<>());
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
     * it), the two flywheels where pieces leave (RobotDesign#exitForwardIn, #exitHeightIn), the
     * Limelight from CameraMount and, for the prototype, the pinwheel that takes POLLEN out of a
     * FLOWER (drawn only: the simulator doesn't use it yet). Robot frame, inches: +X forward, +Y left.
     */
    static Glb model(RobotDesign d, boolean pinwheel) {
        MeshBuilder b = new MeshBuilder();
        double half = d.frameIn / 2;
        b.box("Body", new double[] {0.75, 0.78, 0.82, 0.12},
                new double[] {0, 0, d.bodyHeightIn / 2}, new double[] {d.frameIn, d.frameIn, d.bodyHeightIn}, IDENTITY_3);

        // Mecanum wheels at the corners, inside the frame's outline, and the deck between them.
        double wheelX = half - WHEEL_RADIUS_IN - 0.2, wheelY = half - WHEEL_WIDTH_IN / 2;
        for (int fx = -1; fx <= 1; fx += 2) {
            for (int fy = -1; fy <= 1; fy += 2) {
                double[] c = {fx * wheelX, fy * wheelY, WHEEL_RADIUS_IN};
                String where = (fx > 0 ? "front " : "back ") + (fy > 0 ? "left" : "right");
                b.cylinder("Wheel " + where, new double[] {0.12, 0.12, 0.14, 1}, c, WHEEL_RADIUS_IN, WHEEL_WIDTH_IN, IDENTITY_3);
                b.cylinder("Hub " + where, new double[] {0.95, 0.76, 0.0, 1}, c, WHEEL_RADIUS_IN * 0.5, WHEEL_WIDTH_IN + 0.1, IDENTITY_3);
            }
        }
        double deckWidth = d.frameIn - 2 * WHEEL_WIDTH_IN - 0.4;
        b.box("Chassis", new double[] {0.72, 0.74, 0.78, 1},
                new double[] {0, 0, DECK_Z_IN}, new double[] {d.frameIn - 0.4, deckWidth, DECK_THICKNESS_IN}, IDENTITY_3);
        for (int side = -1; side <= 1; side += 2) {
            b.box("Rail " + (side > 0 ? "left" : "right"), new double[] {0.6, 0.62, 0.66, 1},
                    new double[] {0, side * (deckWidth / 2 - 0.25), DECK_Z_IN + 1.25},
                    new double[] {d.frameIn - 0.4, 0.5, 2.5}, IDENTITY_3);
        }

        // The intake: a roller across the front at the top of its opening, and the volume its takes from.
        double mouth = half + d.intakeReachIn;
        b.cylinder("Intake roller", new double[] {1.0, 0.45, 0.0, 1},
                new double[] {mouth - INTAKE_ROLLER_RADIUS_IN, 0, d.intakeHeightIn}, INTAKE_ROLLER_RADIUS_IN,
                d.intakeWidthIn, IDENTITY_3);
        double reach = d.intakeOnContact ? FieldSim.POLLEN_RADIUS_IN + FieldSim.INTAKE_CONTACT_SLACK_IN : 3;
        double top = d.intakeOnContact ? d.intakeHeightIn - FieldSim.POLLEN_RADIUS_IN : d.intakeHeightIn;
        b.box("Pickup volume", new double[] {1.0, 0.55, 0.0, 0.3},
                new double[] {mouth + (reach - 2) / 2, 0, top / 2}, new double[] {reach + 2, d.intakeWidthIn, top}, IDENTITY_3);

        // The launcher: two flywheels on upright axles either side of where pieces leave, on side plates.
        double flyR = FLYWHEEL_RADIUS_IN, gap = FieldSim.NECTAR_RADIUS_IN + flyR - 0.4;
        for (int side = -1; side <= 1; side += 2) {
            String where = side > 0 ? "left" : "right";
            b.cylinder("Flywheel " + where, new double[] {0.55, 0.57, 0.6, 1},
                    new double[] {d.exitForwardIn, side * gap, d.exitHeightIn}, flyR, FLYWHEEL_HEIGHT_IN, AXIS_Z);
            double plateTop = d.exitHeightIn + FLYWHEEL_HEIGHT_IN / 2 + 0.3;
            b.box("Launcher plate " + where, new double[] {0.6, 0.62, 0.66, 1},
                    new double[] {d.exitForwardIn, side * (gap + flyR + 0.3), (DECK_Z_IN + plateTop) / 2},
                    new double[] {2 * flyR + 1, 0.25, plateTop - DECK_Z_IN}, IDENTITY_3);
        }

        if (pinwheel) {
            b.cylinder("Pinwheel", new double[] {0.85, 0.86, 0.88, 1},
                    new double[] {half + PINWHEEL_AHEAD_IN, -(half - PINWHEEL_INSET_IN), PINWHEEL_HEIGHT_IN},
                    PINWHEEL_RADIUS_IN, 0.4, AXIS_X);
        }

        addLimelight(b, CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn,
                CameraMount.pitchDeg, CameraMount.yawDeg, DECK_Z_IN);
        return b.glb(pinwheel ? PROTOTYPE_NAME : ROBOT_NAME);
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
