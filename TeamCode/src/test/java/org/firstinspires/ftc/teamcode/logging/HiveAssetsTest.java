package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The asset builder and the glTF cutting it relies on.
 *
 * <p><b>To build the AdvantageScope assets</b> (once per laptop, and after AdvantageScope updates
 * its field): find the stock 2026-2027 field folder in AdvantageScope's {@code autoAssets} folder,
 * the {@code Field3d_…} one whose {@code config.json} says {@code "2026-2027 Field"}, and run
 * <pre>
 * BIOBUZZ_FIELD3D=/path/to/that/folder ./gradlew :TeamCode:testDebugUnitTest --tests '*HiveAssetsTest*'
 * </pre>
 * (an environment variable, because Gradle does not pass {@code -D} properties on to tests). It
 * writes {@code TeamCode/build/advantagescope/}. Copy the two folders in it into AdvantageScope's
 * {@code userAssets} folder (Show Assets Folder, in the app menu) and restart AdvantageScope. Without the
 * variable the build test is skipped.
 */
public class HiveAssetsTest {

    @Test
    public void buildsTheAdvantageScopeAssetsFromTheStockField() throws IOException {
        String stock = System.getenv("BIOBUZZ_FIELD3D");
        assumeTrue("set BIOBUZZ_FIELD3D to the stock field folder to build the assets", stock != null);
        File out = new File(TeamCodeDir.get(), "build/advantagescope");
        List<HiveAssets.StagedPiece> staged = HiveAssets.build(new File(stock), out);

        // The committed starting positions are what this field says.
        assertEquals(HiveAssets.csv(HiveAssets.committedStagedPieces()), HiveAssets.csv(staged));
        for (String name : new String[] {"model.glb", "model_0.glb", "model_1.glb", "config.json"}) {
            assertEquals(name, true, new File(out, HiveAssets.ROBOT_FOLDER + "/" + name).isFile());
        }
        System.out.println("Wrote " + out.getAbsolutePath() + ": copy " + HiveAssets.FIELD_FOLDER + " and "
                + HiveAssets.ROBOT_FOLDER + " into AdvantageScope's userAssets folder.");
    }

    @Test
    public void committedStartingPositionsHoldEveryPiece() throws IOException {
        List<HiveAssets.StagedPiece> staged = HiveAssets.committedStagedPieces();
        Map<String, Integer> kinds = new HashMap<>();
        Map<String, Integer> holders = new HashMap<>();
        for (HiveAssets.StagedPiece p : staged) {
            kinds.merge(p.kind, 1, Integer::sum);
            holders.merge(p.holder, 1, Integer::sum);
        }
        assertEquals(40, (int) kinds.get("Pollen"));
        assertEquals(8, (int) kinds.get("Red Nectar"));
        assertEquals(8, (int) kinds.get("Blue Nectar"));
        assertEquals(6, (int) holders.get("cell")); // three NECTAR in each raised CELL
        assertEquals(16, (int) holders.get("flower")); // four stacks of four POLLEN
        assertEquals(staged.size(), HiveAssets.parseCsv(HiveAssets.csv(staged)).size());
    }

    /**
     * Cutting a model keeps each kept mesh's own vertex bytes, even out of a shared, interleaved
     * buffer view, drops what is not used, and hangs the kept trees under a root with the matrix.
     */
    @Test
    public void subsetKeepsOnlyWhatTheKeptNodesUse() {
        Glb model = Glb.read(twoTriangles().write());
        double[] matrix = HiveAssets.MODEL_TO_CENTER_ROTATED;
        Glb part = Glb.read(model.subset("part", Arrays.asList(2), matrix).write());

        assertEquals(2, part.list("nodes").size());
        assertEquals("part", part.name(0));
        assertArrayEquals(matrix, Glb.doubles(part.list("nodes").get(0).get("matrix")), 0);
        assertEquals("second", part.name(part.children(0).get(0)));
        assertEquals(1, part.list("meshes").size());
        assertEquals(1, part.list("materials").size());
        assertEquals("blue", part.list("materials").get(0).get("name"));
        assertEquals(1, part.list("accessors").size());

        // The second triangle's three vertices, and nothing of the first.
        ByteBuffer b = ByteBuffer.wrap(part.bin).order(ByteOrder.LITTLE_ENDIAN);
        float[] v = new float[9];
        for (int i = 0; i < 9; i++) v[i] = b.getFloat();
        assertArrayEquals(new float[] {10, 0, 0, 11, 0, 0, 10, 1, 0}, v, 0);
        assertEquals(36, part.bin.length);

        // Bounds go through the root's matrix: model (x, y, z) lands at (z, x, y).
        double[] box = part.bounds(0, Glb.IDENTITY);
        assertArrayEquals(new double[] {0, 10, 0, 0, 11, 1}, box, 1e-9);
    }

    @Test
    public void jsonRoundTripsWholeNumbersAsIntegers() {
        String text = "{\"a\":[1,2.5,-3],\"s\":\"q\\\"x\",\"t\":true,\"n\":null}";
        assertEquals(text, MiniJson.write(MiniJson.parse(text)));
    }

    /** Two one-triangle meshes whose positions share one interleaved buffer view. */
    private static Glb twoTriangles() {
        ByteBuffer bin = ByteBuffer.allocate(72).order(ByteOrder.LITTLE_ENDIAN);
        float[] first = {0, 0, 0, 1, 0, 0, 0, 1, 0};
        float[] second = {10, 0, 0, 11, 0, 0, 10, 1, 0};
        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 3; k++) bin.putFloat(first[3 * i + k]);
            for (int k = 0; k < 3; k++) bin.putFloat(second[3 * i + k]);
        }
        String json = "{\"asset\":{\"version\":\"2.0\"},\"scene\":0,\"scenes\":[{\"nodes\":[0]}],"
                + "\"nodes\":[{\"name\":\"root\",\"children\":[1,2]},{\"name\":\"first\",\"mesh\":0},"
                + "{\"name\":\"second\",\"mesh\":1}],"
                + "\"meshes\":[{\"primitives\":[{\"attributes\":{\"POSITION\":0},\"material\":0}]},"
                + "{\"primitives\":[{\"attributes\":{\"POSITION\":1},\"material\":1}]}],"
                + "\"materials\":[{\"name\":\"red\"},{\"name\":\"blue\"}],"
                + "\"accessors\":[{\"bufferView\":0,\"componentType\":5126,\"count\":3,\"type\":\"VEC3\","
                + "\"min\":[0,0,0],\"max\":[1,1,0]},"
                + "{\"bufferView\":0,\"byteOffset\":12,\"componentType\":5126,\"count\":3,\"type\":\"VEC3\","
                + "\"min\":[10,0,0],\"max\":[11,1,0]}],"
                + "\"bufferViews\":[{\"buffer\":0,\"byteLength\":72,\"byteStride\":24,\"target\":34962}],"
                + "\"buffers\":[{\"byteLength\":72}]}";
        @SuppressWarnings("unchecked")
        Map<String, Object> parsed = (Map<String, Object>) MiniJson.parse(json);
        return new Glb(parsed, bin.array());
    }
}
