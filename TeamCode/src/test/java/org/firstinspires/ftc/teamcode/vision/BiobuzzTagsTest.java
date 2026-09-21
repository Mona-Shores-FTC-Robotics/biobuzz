package org.firstinspires.ftc.teamcode.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterMemberMetadata;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterMetadata;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagLibrary;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds {@link BiobuzzTags} against the FTC SDK's own BIOBUZZ library.
 *
 * <p>The member offsets in {@code BiobuzzTags} are transcribed, because the SDK
 * keeps {@code AprilTagClusterMetadata.clusterMembers} package-private and there
 * is no public accessor for them. Transcribed field geometry that nothing checks
 * is exactly the kind of thing that quietly goes wrong, so this test reads the
 * real values reflectively — legitimate here, where it runs on the JVM at build
 * time and a failure is loud — and compares them.
 *
 * <p>If FIRST changes the BIOBUZZ geometry in a later SDK, this fails and names
 * the number to fix.
 */
public class BiobuzzTagsTest {

    private static final double EPS = 1e-9;

    @Test
    public void everyClusterMemberIsMappedToTheRightCell() {
        AprilTagLibrary library = AprilTagGameDatabase.getBioBuzzTagLibrary();
        for (int tagId = BiobuzzTags.FIRST_TAG_ID; tagId <= BiobuzzTags.LAST_TAG_ID; tagId++) {
            AprilTagClusterMetadata cluster = library.lookupCluster(tagId);
            assertNotNull("SDK has no cluster for member tag " + tagId, cluster);

            HiveCell expected = HiveCell.forClusterName(cluster.name);
            assertNotNull("No HiveCell matches SDK cluster name \"" + cluster.name + "\"", expected);
            assertEquals("Wrong cell for tag " + tagId, expected, BiobuzzTags.cellForTag(tagId));
            assertTrue(BiobuzzTags.isHiveTag(tagId));
        }
    }

    @Test
    public void memberOffsetsMatchTheSdk() throws Exception {
        AprilTagLibrary library = AprilTagGameDatabase.getBioBuzzTagLibrary();
        Field membersField = AprilTagClusterMetadata.class.getDeclaredField("clusterMembers");
        membersField.setAccessible(true);

        int checked = 0;
        for (AprilTagClusterMetadata cluster : library.getAllClusters()) {
            HiveCell cell = HiveCell.forClusterName(cluster.name);
            if (cell == null) continue; // a cluster from another game's library

            @SuppressWarnings("unchecked")
            List<AprilTagClusterMemberMetadata> members =
                    (List<AprilTagClusterMemberMetadata>) membersField.get(cluster);

            for (AprilTagClusterMemberMetadata member : members) {
                VectorF sdk = member.positionInClusterPlane;
                Vec3 ours = BiobuzzTags.memberOffset(member.id);

                assertEquals("x offset for tag " + member.id, sdk.get(0), ours.x(), EPS);
                assertEquals("y offset for tag " + member.id, sdk.get(1), ours.y(), EPS);
                assertEquals("z offset for tag " + member.id, sdk.get(2), ours.z(), EPS);
                assertEquals("tag size for tag " + member.id,
                        member.tagsize, BiobuzzTags.TAG_SIZE_INCHES, EPS);
                checked++;
            }
        }

        assertEquals("Expected every BIOBUZZ member tag to be checked",
                BiobuzzTags.LAST_TAG_ID - BiobuzzTags.FIRST_TAG_ID + 1, checked);
    }

    /**
     * The premise the whole design rests on. If a future SDK starts publishing real
     * field positions, the goals have presumably stopped moving and a field-pose
     * path becomes worth building — so this failing is good news, not a bug.
     */
    @Test
    public void sdkStillPublishesNoFieldPositions() {
        AprilTagLibrary library = AprilTagGameDatabase.getBioBuzzTagLibrary();

        assertEquals("BIOBUZZ library now has standalone tags with positions",
                0, library.getAllTags().length);

        for (int tagId = BiobuzzTags.FIRST_TAG_ID; tagId <= BiobuzzTags.LAST_TAG_ID; tagId++) {
            assertNull("Tag " + tagId + " now has standalone metadata", library.lookupTag(tagId));
        }

        for (AprilTagClusterMetadata cluster : library.getAllClusters()) {
            if (HiveCell.forClusterName(cluster.name) == null) continue;
            VectorF position = cluster.fieldPosition;
            assertNotNull(position);
            assertEquals(cluster.name + " now has a real field X", 0.0, position.get(0), EPS);
            assertEquals(cluster.name + " now has a real field Y", 0.0, position.get(1), EPS);
            assertEquals(cluster.name + " now has a real field Z", 0.0, position.get(2), EPS);
        }
    }

    @Test
    public void eachCellOwnsFourConsecutiveTags() {
        List<Integer> seen = new ArrayList<>();
        for (HiveCell cell : HiveCell.values()) {
            int[] ids = BiobuzzTags.tagIdsFor(cell);
            assertEquals(BiobuzzTags.MEMBERS_PER_CLUSTER, ids.length);
            for (int i = 1; i < ids.length; i++) {
                assertEquals("Tags for " + cell + " are not consecutive",
                        ids[i - 1] + 1, ids[i]);
            }
            for (int id : ids) {
                assertFalse("Tag " + id + " claimed by two cells", seen.contains(id));
                seen.add(id);
            }
        }
        assertEquals(BiobuzzTags.LAST_TAG_ID - BiobuzzTags.FIRST_TAG_ID + 1, seen.size());
    }

    @Test
    public void nonMemberTagsAreRejected() {
        for (int tagId : new int[] {0, 20, 24, 29, 46, 583}) {
            assertFalse("Tag " + tagId + " should not be a hive tag", BiobuzzTags.isHiveTag(tagId));
            assertNull(BiobuzzTags.cellForTag(tagId));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void memberOffsetRejectsUnknownTag() {
        BiobuzzTags.memberOffset(29);
    }
}
