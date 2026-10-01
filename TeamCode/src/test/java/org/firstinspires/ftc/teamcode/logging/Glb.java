package org.firstinspires.ftc.teamcode.logging;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Just enough binary glTF ({@code .glb}) to cut AdvantageScope's field model into pieces: read it,
 * keep some of its node trees under a new root, and write the result.
 *
 * <p>Only what the BIOBUZZ field uses is supported: one buffer, plain (non-sparse) accessors and
 * untextured materials. Anything else throws rather than writing a model that silently renders
 * wrong.
 *
 * <p>Node order is kept. three.js names duplicate nodes {@code _1}, {@code _2}, … in load order, and
 * AdvantageScope finds staged game pieces by those names, so reordering would swap them.
 */
final class Glb {

    private static final int MAGIC = 0x46546C67; // "glTF"
    private static final int CHUNK_JSON = 0x4E4F534A;
    private static final int CHUNK_BIN = 0x004E4942;

    final Map<String, Object> json;
    final byte[] bin;

    Glb(Map<String, Object> json, byte[] bin) {
        this.json = json;
        this.bin = bin;
    }

    @SuppressWarnings("unchecked")
    static Glb read(byte[] file) {
        ByteBuffer b = ByteBuffer.wrap(file).order(ByteOrder.LITTLE_ENDIAN);
        if (b.getInt() != MAGIC) throw new IllegalArgumentException("not a .glb file");
        if (b.getInt() != 2) throw new IllegalArgumentException("only glTF 2 is supported");
        b.getInt(); // total length
        Map<String, Object> json = null;
        byte[] bin = new byte[0];
        while (b.remaining() >= 8) {
            int length = b.getInt();
            int type = b.getInt();
            byte[] chunk = new byte[length];
            b.get(chunk);
            if (type == CHUNK_JSON) {
                json = (Map<String, Object>) MiniJson.parse(new String(chunk, StandardCharsets.UTF_8));
            } else if (type == CHUNK_BIN) {
                bin = chunk;
            }
        }
        if (json == null) throw new IllegalArgumentException(".glb has no JSON chunk");
        return new Glb(json, bin);
    }

    byte[] write() {
        byte[] text = MiniJson.write(json).getBytes(StandardCharsets.UTF_8);
        int jsonLength = pad4(text.length);
        int binLength = pad4(bin.length);
        int total = 12 + 8 + jsonLength + (bin.length > 0 ? 8 + binLength : 0);
        ByteBuffer out = ByteBuffer.allocate(total).order(ByteOrder.LITTLE_ENDIAN);
        out.putInt(MAGIC).putInt(2).putInt(total);
        out.putInt(jsonLength).putInt(CHUNK_JSON).put(text);
        for (int i = text.length; i < jsonLength; i++) out.put((byte) ' ');
        if (bin.length > 0) {
            out.putInt(binLength).putInt(CHUNK_BIN).put(bin);
            for (int i = bin.length; i < binLength; i++) out.put((byte) 0);
        }
        return out.array();
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> list(String key) {
        Object v = json.get(key);
        return v == null ? new ArrayList<>() : (List<Map<String, Object>>) v;
    }

    /** The nodes the default scene starts from. */
    List<Integer> sceneRoots() {
        Object sceneIndex = json.get("scene");
        Map<String, Object> scene = list("scenes").get(sceneIndex == null ? 0 : index(sceneIndex));
        return indices(scene.get("nodes"));
    }

    List<Integer> children(int node) {
        return indices(list("nodes").get(node).get("children"));
    }

    String name(int node) {
        Object n = list("nodes").get(node).get("name");
        return n == null ? "" : (String) n;
    }

    /** The one child of {@code parent} whose name contains {@code part}; throws if not exactly one. */
    int childNamed(int parent, String part) {
        int found = -1;
        for (int c : children(parent)) {
            if (name(c).contains(part)) {
                if (found >= 0) throw new IllegalStateException("more than one node named like \"" + part + "\"");
                found = c;
            }
        }
        if (found < 0) throw new IllegalStateException("no node named like \"" + part + "\" under " + name(parent));
        return found;
    }

    /** A node's own transform as a column-major 4×4 matrix (glTF's layout). */
    double[] localMatrix(int node) {
        Map<String, Object> n = list("nodes").get(node);
        if (n.containsKey("matrix")) return doubles(n.get("matrix"));
        double[] t = n.containsKey("translation") ? doubles(n.get("translation")) : new double[] {0, 0, 0};
        double[] q = n.containsKey("rotation") ? doubles(n.get("rotation")) : new double[] {0, 0, 0, 1};
        double[] s = n.containsKey("scale") ? doubles(n.get("scale")) : new double[] {1, 1, 1};
        double x = q[0], y = q[1], z = q[2], w = q[3];
        double[] r = {
                1 - 2 * (y * y + z * z), 2 * (x * y + z * w), 2 * (x * z - y * w),
                2 * (x * y - z * w), 1 - 2 * (x * x + z * z), 2 * (y * z + x * w),
                2 * (x * z + y * w), 2 * (y * z - x * w), 1 - 2 * (x * x + y * y)};
        return new double[] {
                r[0] * s[0], r[1] * s[0], r[2] * s[0], 0,
                r[3] * s[1], r[4] * s[1], r[5] * s[1], 0,
                r[6] * s[2], r[7] * s[2], r[8] * s[2], 0,
                t[0], t[1], t[2], 1};
    }

    /**
     * The axis-aligned bounds of a node's subtree in the coordinates of {@code parentMatrix}, from
     * the accessors' declared min/max (each mesh's box corners, transformed). {@code {minX, minY,
     * minZ, maxX, maxY, maxZ}}.
     */
    double[] bounds(int node, double[] parentMatrix) {
        double[] box = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        addBounds(node, parentMatrix, box);
        return box;
    }

    @SuppressWarnings("unchecked")
    private void addBounds(int node, double[] parentMatrix, double[] box) {
        double[] m = multiply(parentMatrix, localMatrix(node));
        Map<String, Object> n = list("nodes").get(node);
        if (n.containsKey("mesh")) {
            for (Map<String, Object> primitive : primitives(index(n.get("mesh")))) {
                Map<String, Object> accessor = list("accessors").get(
                        index(((Map<String, Object>) primitive.get("attributes")).get("POSITION")));
                double[] lo = doubles(accessor.get("min"));
                double[] hi = doubles(accessor.get("max"));
                for (int corner = 0; corner < 8; corner++) {
                    double[] p = transform(m, new double[] {
                            (corner & 1) == 0 ? lo[0] : hi[0],
                            (corner & 2) == 0 ? lo[1] : hi[1],
                            (corner & 4) == 0 ? lo[2] : hi[2]});
                    for (int k = 0; k < 3; k++) {
                        box[k] = Math.min(box[k], p[k]);
                        box[k + 3] = Math.max(box[k + 3], p[k]);
                    }
                }
            }
        }
        for (int c : children(node)) addBounds(c, m, box);
    }

    /**
     * A new model holding only the trees under {@code roots}, all hung from one new root node with
     * {@code rootMatrix} (column-major) — how a part is moved into another frame without touching
     * its vertices. Meshes, accessors, buffer views and materials those trees use come along;
     * nothing else does.
     */
    @SuppressWarnings("unchecked")
    Glb subset(String rootName, Collection<Integer> roots, double[] rootMatrix) {
        TreeSet<Integer> nodes = new TreeSet<>();
        for (int r : roots) collect(r, nodes);

        Map<Integer, Integer> nodeMap = renumber(nodes, 1); // 0 is the new root
        TreeSet<Integer> meshes = new TreeSet<>();
        for (int n : nodes) {
            Map<String, Object> node = list("nodes").get(n);
            if (node.containsKey("skin") || node.containsKey("camera")) {
                throw new UnsupportedOperationException("skins and cameras are not supported");
            }
            if (node.containsKey("mesh")) meshes.add(index(node.get("mesh")));
        }
        Map<Integer, Integer> meshMap = renumber(meshes, 0);

        TreeSet<Integer> accessors = new TreeSet<>();
        TreeSet<Integer> materials = new TreeSet<>();
        for (int m : meshes) {
            for (Map<String, Object> p : primitives(m)) {
                for (Object a : ((Map<String, Object>) p.get("attributes")).values()) accessors.add(index(a));
                if (p.containsKey("indices")) accessors.add(index(p.get("indices")));
                if (p.containsKey("targets")) {
                    for (Map<String, Object> target : (List<Map<String, Object>>) p.get("targets")) {
                        for (Object a : target.values()) accessors.add(index(a));
                    }
                }
                if (p.containsKey("material")) materials.add(index(p.get("material")));
            }
        }
        Map<Integer, Integer> accessorMap = renumber(accessors, 0);
        Map<Integer, Integer> materialMap = renumber(materials, 0);
        for (int m : materials) {
            if (MiniJson.write(list("materials").get(m)).contains("\"index\"")) {
                throw new UnsupportedOperationException("textured materials are not supported");
            }
        }

        // Each accessor's own bytes, packed tightly into a buffer view of its own. The field model
        // shares three huge buffer views among all its parts, so copying whole views would copy
        // the whole field.
        ByteArrayOutputStream newBin = new ByteArrayOutputStream();
        List<Object> newViews = new ArrayList<>();
        List<Object> newAccessors = new ArrayList<>();
        for (int a : accessors) {
            Map<String, Object> accessor = new LinkedHashMap<>(list("accessors").get(a));
            if (accessor.containsKey("sparse")) throw new UnsupportedOperationException("sparse accessors");
            if (accessor.containsKey("bufferView")) {
                Map<String, Object> view = list("bufferViews").get(index(accessor.get("bufferView")));
                if (view.containsKey("buffer") && index(view.get("buffer")) != 0) {
                    throw new UnsupportedOperationException("only one buffer is supported");
                }
                int element = elementBytes(accessor);
                int stride = view.containsKey("byteStride") ? index(view.get("byteStride")) : element;
                int start = (view.containsKey("byteOffset") ? index(view.get("byteOffset")) : 0)
                        + (accessor.containsKey("byteOffset") ? index(accessor.get("byteOffset")) : 0);
                int count = index(accessor.get("count"));
                while (newBin.size() % 4 != 0) newBin.write(0);
                Map<String, Object> newView = new LinkedHashMap<>();
                newView.put("buffer", 0);
                newView.put("byteOffset", newBin.size());
                newView.put("byteLength", count * element);
                if (view.containsKey("target")) newView.put("target", view.get("target"));
                for (int i = 0; i < count; i++) newBin.write(bin, start + i * stride, element);
                accessor.remove("byteOffset");
                accessor.put("bufferView", newViews.size());
                newViews.add(newView);
            }
            newAccessors.add(accessor);
        }
        while (newBin.size() % 4 != 0) newBin.write(0);

        List<Object> newMeshes = new ArrayList<>();
        for (int m : meshes) {
            Map<String, Object> mesh = new LinkedHashMap<>(list("meshes").get(m));
            List<Object> newPrimitives = new ArrayList<>();
            for (Map<String, Object> p : primitives(m)) {
                Map<String, Object> primitive = new LinkedHashMap<>(p);
                primitive.put("attributes", remapValues((Map<String, Object>) p.get("attributes"), accessorMap));
                if (p.containsKey("indices")) primitive.put("indices", accessorMap.get(index(p.get("indices"))));
                if (p.containsKey("material")) primitive.put("material", materialMap.get(index(p.get("material"))));
                if (p.containsKey("targets")) {
                    List<Object> targets = new ArrayList<>();
                    for (Map<String, Object> t : (List<Map<String, Object>>) p.get("targets")) {
                        targets.add(remapValues(t, accessorMap));
                    }
                    primitive.put("targets", targets);
                }
                newPrimitives.add(primitive);
            }
            mesh.put("primitives", newPrimitives);
            newMeshes.add(mesh);
        }

        List<Object> newNodes = new ArrayList<>();
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("name", rootName);
        root.put("matrix", toList(rootMatrix));
        List<Object> rootChildren = new ArrayList<>();
        for (int r : roots) rootChildren.add(nodeMap.get(r));
        root.put("children", rootChildren);
        newNodes.add(root);
        for (int n : nodes) {
            Map<String, Object> node = new LinkedHashMap<>(list("nodes").get(n));
            if (node.containsKey("children")) {
                List<Object> c = new ArrayList<>();
                for (int child : indices(node.get("children"))) c.add(nodeMap.get(child));
                node.put("children", c);
            }
            if (node.containsKey("mesh")) node.put("mesh", meshMap.get(index(node.get("mesh"))));
            newNodes.add(node);
        }

        List<Object> newMaterials = new ArrayList<>();
        for (int m : materials) newMaterials.add(list("materials").get(m));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("asset", json.containsKey("asset") ? json.get("asset") : defaultAsset());
        out.put("scene", 0);
        Map<String, Object> scene = new LinkedHashMap<>();
        scene.put("nodes", java.util.Collections.singletonList(0));
        out.put("scenes", java.util.Collections.singletonList(scene));
        out.put("nodes", newNodes);
        if (!newMeshes.isEmpty()) out.put("meshes", newMeshes);
        if (!newMaterials.isEmpty()) out.put("materials", newMaterials);
        if (!newAccessors.isEmpty()) out.put("accessors", newAccessors);
        if (!newViews.isEmpty()) out.put("bufferViews", newViews);
        byte[] outBin = newBin.toByteArray();
        if (outBin.length > 0) {
            Map<String, Object> buffer = new LinkedHashMap<>();
            buffer.put("byteLength", outBin.length);
            out.put("buffers", java.util.Collections.singletonList(buffer));
        }
        return new Glb(out, outBin);
    }

    // ---- helpers ------------------------------------------------------------------------------

    private void collect(int node, TreeSet<Integer> into) {
        if (!into.add(node)) return;
        for (int c : children(node)) collect(c, into);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> primitives(int mesh) {
        return (List<Map<String, Object>>) list("meshes").get(mesh).get("primitives");
    }

    private static Map<Integer, Integer> renumber(TreeSet<Integer> kept, int first) {
        Map<Integer, Integer> map = new TreeMap<>();
        int next = first;
        for (int k : kept) map.put(k, next++);
        return map;
    }

    private static Map<String, Object> remapValues(Map<String, Object> in, Map<Integer, Integer> map) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : in.entrySet()) out.put(e.getKey(), map.get(index(e.getValue())));
        return out;
    }

    /** Bytes in one element of an accessor; matrices (which need column padding) are not supported. */
    private static int elementBytes(Map<String, Object> accessor) {
        int component;
        switch (index(accessor.get("componentType"))) {
            case 5120: case 5121: component = 1; break;
            case 5122: case 5123: component = 2; break;
            case 5125: case 5126: component = 4; break;
            default: throw new UnsupportedOperationException("componentType " + accessor.get("componentType"));
        }
        switch ((String) accessor.get("type")) {
            case "SCALAR": return component;
            case "VEC2": return 2 * component;
            case "VEC3": return 3 * component;
            case "VEC4": return 4 * component;
            default: throw new UnsupportedOperationException("accessor type " + accessor.get("type"));
        }
    }

    private static Map<String, Object> defaultAsset() {
        Map<String, Object> asset = new LinkedHashMap<>();
        asset.put("version", "2.0");
        return asset;
    }

    static int index(Object jsonNumber) {
        return ((Number) jsonNumber).intValue();
    }

    @SuppressWarnings("unchecked")
    static List<Integer> indices(Object jsonArray) {
        List<Integer> out = new ArrayList<>();
        if (jsonArray != null) for (Object o : (List<Object>) jsonArray) out.add(index(o));
        return out;
    }

    @SuppressWarnings("unchecked")
    static double[] doubles(Object jsonArray) {
        List<Object> l = (List<Object>) jsonArray;
        double[] out = new double[l.size()];
        for (int i = 0; i < out.length; i++) out[i] = ((Number) l.get(i)).doubleValue();
        return out;
    }

    static List<Object> toList(double[] values) {
        List<Object> out = new ArrayList<>();
        for (double v : values) out.add(v);
        return out;
    }

    /** {@code a · b} for column-major 4×4 matrices. */
    static double[] multiply(double[] a, double[] b) {
        double[] out = new double[16];
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                double s = 0;
                for (int k = 0; k < 4; k++) s += a[k * 4 + row] * b[col * 4 + k];
                out[col * 4 + row] = s;
            }
        }
        return out;
    }

    static double[] transform(double[] m, double[] p) {
        return new double[] {
                m[0] * p[0] + m[4] * p[1] + m[8] * p[2] + m[12],
                m[1] * p[0] + m[5] * p[1] + m[9] * p[2] + m[13],
                m[2] * p[0] + m[6] * p[1] + m[10] * p[2] + m[14]};
    }

    static final double[] IDENTITY = {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};

    private static int pad4(int n) {
        return (n + 3) & ~3;
    }
}
