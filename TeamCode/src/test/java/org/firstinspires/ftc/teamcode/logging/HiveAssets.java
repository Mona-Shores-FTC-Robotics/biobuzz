package org.firstinspires.ftc.teamcode.logging;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builds the two AdvantageScope asset folders that let a {@code .wpilog} tip the HIVE, from the
 * stock 2026-2027 field AdvantageScope downloads.
 *
 * <p><b>Why two folders.</b> AdvantageScope 27 fields cannot move parts; robots can, through
 * articulated components (docs: <i>Custom Assets → Articulated Components</i>). So:
 * <ul>
 *   <li>{@value #FIELD_FOLDER}: the stock field with the HIVE frame and both rockers cut out.
 *       Everything else, the game pieces and their staged copies included, is unchanged.</li>
 *   <li>{@value #ROBOT_FOLDER}: the frame as the base model, the red rocker as component 0
 *       and the blue rocker as component 1. Put it on the field at the origin and give it the
 *       {@code /Sim/Hive/Components} poses, and the rockers swing about their axle.</li>
 * </ul>
 * Without component poses AdvantageScope draws the rockers as built, which is the match-start
 * position; so the HIVE looks right even in a log that only has the frame pose.
 *
 * <p><b>Frames.</b> The field model is glTF (Y up). The field config turns it 90° about x into
 * AdvantageScope's internal frame, and logged poses are drawn in the field's Center/Rotated frame,
 * a further −90° about z. Together, a model point {@code (x, y, z)} lands at Center/Rotated
 * {@code (z, x, y)}. The robot folder's models carry that as their root node's matrix, so the
 * robot config needs no rotations and an identity pose puts the frame where the field had it.
 *
 * <p><b>Nothing from FIRST's CAD is committed.</b> Run this on your own copy of the stock field;
 * see {@code HiveAssetsTest}. The only thing kept in the repo is where the game pieces start
 * ({@code advantagescope/biobuzz-staged-pieces.csv}), which the simulation needs and the same run
 * checks.
 */
final class HiveAssets {

    static final String FIELD_FOLDER = "Field3d_BIOBUZZHiveSim";
    static final String ROBOT_FOLDER = "Robot_BIOBUZZHive";
    static final String FIELD_NAME = "2026-2027 Field (HIVE sim)";
    static final String ROBOT_NAME = "BIOBUZZ HIVE";
    static final String STAGED_CSV = "biobuzz-staged-pieces.csv";

    /** Component order in {@value #ROBOT_FOLDER} and in {@code /Sim/Hive/Components}. */
    static final int RED_COMPONENT = 0;
    static final int BLUE_COMPONENT = 1;

    /** Model point {@code (x, y, z)} → Center/Rotated {@code (z, x, y)}, column-major. */
    static final double[] MODEL_TO_CENTER_ROTATED = {
            0, 1, 0, 0,
            0, 0, 1, 0,
            1, 0, 0, 0,
            0, 0, 0, 1};

    private HiveAssets() {
    }

    /** Where one game piece starts, in Pedro inches, and what holds it there. */
    static final class StagedPiece {
        final String kind; // "Pollen", "Red Nectar", "Blue Nectar"
        final String holder; // "floor", "flower", "cell", "outside"
        final double x, y, z;

        StagedPiece(String kind, String holder, double x, double y, double z) {
            this.kind = kind;
            this.holder = holder;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /**
     * Writes {@value #FIELD_FOLDER}, {@value #ROBOT_FOLDER} and {@value #STAGED_CSV} into
     * {@code out}, from the stock field folder {@code stock} (the one whose {@code config.json} says
     * {@code "2026-2027 Field"}).
     */
    @SuppressWarnings("unchecked")
    static List<StagedPiece> build(File stock, File out) throws IOException {
        Map<String, Object> config = (Map<String, Object>) MiniJson.parse(
                new String(Files.readAllBytes(new File(stock, "config.json").toPath()), StandardCharsets.UTF_8));
        Glb field = Glb.read(Files.readAllBytes(new File(stock, "model.glb").toPath()));

        List<Integer> sceneRoots = field.sceneRoots();
        if (sceneRoots.size() != 1) throw new IllegalStateException("expected one root node in the field model");
        int root = sceneRoots.get(0);
        int frame = field.childNamed(root, "Frame");
        int red = field.childNamed(root, "Red Hive");
        int blue = field.childNamed(root, "Blue Hive");

        // The field without the HIVE.
        List<Integer> kept = new ArrayList<>();
        for (int c : field.children(root)) if (c != frame && c != red && c != blue) kept.add(c);
        File fieldDir = new File(out, FIELD_FOLDER);
        fieldDir.mkdirs();
        write(new File(fieldDir, "model.glb"), field.subset(field.name(root), kept, field.localMatrix(root)).write());
        List<Object> pieces = (List<Object>) config.get("gamePieces");
        for (int i = 0; i < pieces.size(); i++) {
            Files.copy(new File(stock, "model_" + i + ".glb").toPath(),
                    new File(fieldDir, "model_" + i + ".glb").toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        Map<String, Object> fieldConfig = new LinkedHashMap<>(config);
        fieldConfig.put("name", FIELD_NAME);
        fieldConfig.remove("locales"); // the stock names would hide which field this is
        write(new File(fieldDir, "config.json"), MiniJson.write(fieldConfig).getBytes(StandardCharsets.UTF_8));

        // The HIVE as a robot: frame, red rocker, blue rocker.
        double[] toCr = Glb.multiply(MODEL_TO_CENTER_ROTATED, field.localMatrix(root));
        File robotDir = new File(out, ROBOT_FOLDER);
        robotDir.mkdirs();
        write(new File(robotDir, "model.glb"), field.subset("HIVE frame", list(frame), toCr).write());
        write(new File(robotDir, "model_" + RED_COMPONENT + ".glb"),
                field.subset("Red HIVE rocker", list(red), toCr).write());
        write(new File(robotDir, "model_" + BLUE_COMPONENT + ".glb"),
                field.subset("Blue HIVE rocker", list(blue), toCr).write());
        write(new File(robotDir, "config.json"), robotConfig().getBytes(StandardCharsets.UTF_8));

        List<StagedPiece> staged = stagedPieces(field, root);
        write(new File(out, STAGED_CSV), csv(staged).getBytes(StandardCharsets.UTF_8));
        return staged;
    }

    private static String robotConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("name", ROBOT_NAME);
        config.put("isFTC", true);
        config.put("rotations", new ArrayList<>());
        config.put("position", Glb.toList(new double[] {0, 0, 0}));
        config.put("cameras", new ArrayList<>());
        List<Object> components = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("zeroedRotations", new ArrayList<>());
            c.put("zeroedPosition", Glb.toList(new double[] {0, 0, 0}));
            components.add(c);
        }
        config.put("components", components);
        return MiniJson.write(config);
    }

    /**
     * Every POLLEN and NECTAR in the field model, at the centre of its bounding box, in Pedro
     * inches. A piece inside a Flower Assembly's footprint is {@code flower}; one above the
     * {@code cell} height is in a CELL; one beyond the walls is {@code outside} (the alliance areas).
     */
    static List<StagedPiece> stagedPieces(Glb field, int root) {
        double[] rootMatrix = field.localMatrix(root);
        List<double[]> flowers = new ArrayList<>();
        for (int c : field.children(root)) {
            if (field.name(c).contains("Flower Assembly")) flowers.add(pedroBox(field.bounds(c, rootMatrix)));
        }
        double fieldSize = 2 * AdvantageScopeFrame.PEDRO_FIELD_CENTER_IN;
        List<StagedPiece> out = new ArrayList<>();
        for (int c : field.children(root)) {
            String name = field.name(c);
            String kind = name.contains("Pollen") ? "Pollen"
                    : name.contains("Red Nectar") ? "Red Nectar"
                    : name.contains("Blue Nectar") ? "Blue Nectar" : null;
            if (kind == null) continue;
            double[] box = pedroBox(field.bounds(c, rootMatrix));
            double x = (box[0] + box[3]) / 2, y = (box[1] + box[4]) / 2, z = (box[2] + box[5]) / 2;
            String holder = "floor";
            if (x < 0 || y < 0 || x > fieldSize || y > fieldSize) holder = "outside";
            else if (z > CELL_HEIGHT_IN) holder = "cell";
            else for (double[] f : flowers) if (x > f[0] && x < f[3] && y > f[1] && y < f[4]) holder = "flower";
            out.add(new StagedPiece(kind, holder, x, y, z));
        }
        return out;
    }

    /** Anything resting this high is in a CELL; the HIVE's lowest point is 25.5 in up. */
    private static final double CELL_HEIGHT_IN = 25.0;

    /** A model-frame box (meters) as a Pedro box {@code {minX, minY, minZ, maxX, maxY, maxZ}}. */
    static double[] pedroBox(double[] m) {
        double c = AdvantageScopeFrame.PEDRO_FIELD_CENTER_IN;
        double k = AdvantageScopeFrame.METERS_PER_INCH;
        return new double[] {
                c + m[0] / k, c - m[5] / k, m[1] / k,
                c + m[3] / k, c - m[2] / k, m[4] / k};
    }

    static String csv(List<StagedPiece> pieces) {
        StringBuilder b = new StringBuilder("# Where BIOBUZZ game pieces start, from AdvantageScope's 2026-2027 field model.\n"
                + "# Pedro inches (piece centre). Regenerate with HiveAssetsTest; do not edit by hand.\n"
                + "kind,holder,x,y,z\n");
        for (StagedPiece p : pieces) {
            b.append(String.format(Locale.ROOT, "%s,%s,%.2f,%.2f,%.2f%n", p.kind, p.holder, p.x, p.y, p.z));
        }
        return b.toString();
    }

    static List<StagedPiece> parseCsv(String text) {
        List<StagedPiece> out = new ArrayList<>();
        for (String line : text.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("kind,")) continue;
            String[] f = line.split(",");
            out.add(new StagedPiece(f[0], f[1],
                    Double.parseDouble(f[2]), Double.parseDouble(f[3]), Double.parseDouble(f[4])));
        }
        return out;
    }

    /** The committed starting positions, from {@code src/test/resources/advantagescope}. */
    static List<StagedPiece> committedStagedPieces() throws IOException {
        File f = new File(TeamCodeDir.get(), "src/test/resources/advantagescope/" + STAGED_CSV);
        return parseCsv(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
    }

    private static List<Integer> list(int node) {
        List<Integer> l = new ArrayList<>();
        l.add(node);
        return l;
    }

    private static void write(File file, byte[] bytes) throws IOException {
        Files.write(file.toPath(), bytes);
    }
}
