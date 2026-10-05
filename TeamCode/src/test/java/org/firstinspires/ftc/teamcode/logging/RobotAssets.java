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
 * Builds an AdvantageScope robot model, {@value #FOLDER}, whose Limelight sits where
 * {@link CameraMount} says it does.
 *
 * <p>Two things come out of it in a 3D Field tab:
 * <ul>
 *   <li><b>You can see the camera on the robot.</b> The model is a chassis slab, the intake (orange) on
 *       its front edge, as the simulator models it (so heading reads at a glance), a post, the Limelight as a dark box at the
 *       measured lens position and angle, and a green rod along the optical axis.</li>
 *   <li><b>You can look through it.</b> The {@code config.json} declares the Limelight as a fixed
 *       camera, so right-clicking the field view offers {@value #CAMERA_NAME}: the view from the
 *       lens, at the Limelight 3A's field of view, following the logged robot pose.</li>
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
 * <p>Nothing here is CAD: the boxes are drawn from a handful of numbers, so the folder can be
 * rebuilt anywhere with no download. Re-measure the mount, change {@link CameraMount}, rebuild.
 */
final class RobotAssets {

    static final String FOLDER = "Robot_BIOBUZZ";
    static final String ROBOT_NAME = "BIOBUZZ Robot";
    static final String CAMERA_NAME = "Limelight";

    /** Limelight 3A, as AdvantageScope's FTC drive base declares it. */
    static final int[] LIMELIGHT_RESOLUTION = {640, 480};
    static final double LIMELIGHT_HFOV_DEG = 54.5;

    /** The FTC size limit. Drawing only: the camera's position does not depend on it. */
    static final double CHASSIS_SIZE_IN = 18.0;
    static final double CHASSIS_BOTTOM_IN = 0.5;
    static final double CHASSIS_TOP_IN = 3.0;
    /** How deep the intake block is drawn, inside the frame's front edge. Drawing only. */
    static final double INTAKE_DEPTH_IN = 1.5;
    /** Limelight 3A housing, roughly: depth along the lens axis, width, height. */
    static final double[] LIMELIGHT_BOX_IN = {1.0, 3.0, 2.0};
    static final double MAST_SIZE_IN = 1.0;
    static final double VIEW_ROD_LENGTH_IN = 8.0;
    static final double VIEW_ROD_THICKNESS_IN = 0.3;

    private static final double M = AdvantageScopeFrame.METERS_PER_INCH;

    private RobotAssets() {
    }

    /** Writes {@value #FOLDER} into {@code out}, with the mount's current numbers. */
    static File build(File out) throws IOException {
        File dir = new File(out, FOLDER);
        dir.mkdirs();
        Files.write(new File(dir, "model.glb").toPath(), model(
                CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn,
                CameraMount.pitchDeg, CameraMount.yawDeg).write());
        Files.write(new File(dir, "config.json").toPath(), config(
                CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn,
                CameraMount.pitchDeg, CameraMount.yawDeg).getBytes(StandardCharsets.UTF_8));
        return dir;
    }

    static String config(double forwardIn, double leftIn, double upIn, double pitchDeg, double yawDeg) {
        Map<String, Object> camera = new LinkedHashMap<>();
        camera.put("name", CAMERA_NAME);
        camera.put("rotations", Arrays.asList(rotation("y", -pitchDeg), rotation("z", yawDeg)));
        camera.put("position", Glb.toList(new double[] {forwardIn * M, leftIn * M, upIn * M}));
        camera.put("resolution", Arrays.asList((Object) LIMELIGHT_RESOLUTION[0], LIMELIGHT_RESOLUTION[1]));
        camera.put("fov", LIMELIGHT_HFOV_DEG);

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("name", ROBOT_NAME);
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

    /** The model: chassis, intake, Limelight and its view rod, in the robot frame, metres. */
    static Glb model(double forwardIn, double leftIn, double upIn, double pitchDeg, double yawDeg) {
        MeshBuilder b = new MeshBuilder();
        double h = CHASSIS_SIZE_IN / 2;
        b.box("Chassis", new double[] {0.55, 0.55, 0.6, 1},
                new double[] {0, 0, (CHASSIS_BOTTOM_IN + CHASSIS_TOP_IN) / 2},
                new double[] {CHASSIS_SIZE_IN, CHASSIS_SIZE_IN, CHASSIS_TOP_IN - CHASSIS_BOTTOM_IN},
                IDENTITY_3);
        // The intake, as the simulator takes pieces (RobotDesign#springHoodFullWidth): on the frame's
        // front edge, its width and opening height. It also shows which way the robot faces.
        RobotDesign intake = RobotDesign.springHoodFullWidth();
        b.box("Intake", new double[] {1.0, 0.45, 0.0, 1},
                new double[] {h - INTAKE_DEPTH_IN / 2, 0, (CHASSIS_BOTTOM_IN + intake.intakeHeightIn) / 2},
                new double[] {INTAKE_DEPTH_IN, intake.intakeWidthIn, intake.intakeHeightIn - CHASSIS_BOTTOM_IN},
                IDENTITY_3);

        // The lens is at the mount point; the housing sits behind it along the optical axis.
        double[] r = mountRotation(pitchDeg, yawDeg);
        double[] lens = {forwardIn, leftIn, upIn};
        double[] housingCentre = add(lens, mul(r, new double[] {-LIMELIGHT_BOX_IN[0] / 2, 0, 0}));
        // A post up to the housing, so it reads as mounted rather than floating. Drawing only.
        double mastTop = housingCentre[2] - LIMELIGHT_BOX_IN[2] / 2;
        if (mastTop > CHASSIS_TOP_IN) {
            b.box("Mast", new double[] {0.35, 0.35, 0.4, 1},
                    new double[] {housingCentre[0], housingCentre[1], (CHASSIS_TOP_IN + mastTop) / 2},
                    new double[] {MAST_SIZE_IN, MAST_SIZE_IN, mastTop - CHASSIS_TOP_IN},
                    IDENTITY_3);
        }
        b.box("Limelight", new double[] {0.12, 0.12, 0.14, 1}, housingCentre, LIMELIGHT_BOX_IN, r);
        double[] rodCentre = add(lens, mul(r, new double[] {VIEW_ROD_LENGTH_IN / 2, 0, 0}));
        b.box("View ray", new double[] {0.1, 0.85, 0.2, 1}, rodCentre,
                new double[] {VIEW_ROD_LENGTH_IN, VIEW_ROD_THICKNESS_IN, VIEW_ROD_THICKNESS_IN}, r);
        return b.glb(ROBOT_NAME);
    }

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

            int pos = accessor(floats(positions), 24, "VEC3", 5126, 34962, bounds(positions));
            int nor = accessor(floats(normals), 24, "VEC3", 5126, 34962, null);
            int idx = accessor(shorts(indices), 36, "SCALAR", 5123, 34963, null);

            Map<String, Object> pbr = new LinkedHashMap<>();
            pbr.put("baseColorFactor", Glb.toList(rgba));
            pbr.put("metallicFactor", 0.0);
            pbr.put("roughnessFactor", 0.8);
            Map<String, Object> material = new LinkedHashMap<>();
            material.put("name", name);
            material.put("pbrMetallicRoughness", pbr);
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
