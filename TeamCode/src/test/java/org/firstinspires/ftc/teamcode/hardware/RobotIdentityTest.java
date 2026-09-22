package org.firstinspires.ftc.teamcode.hardware;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

/**
 * Covers {@link RobotIdentity}, whose whole job is refusing to guess.
 *
 * <p>{@code RobotConfigXmlTest} already checks that the constants and the XML files
 * correspond one-to-one. It never calls {@link RobotIdentity#fromConfigName}, so the
 * behaviour the class was actually written for — returning {@code null} rather than
 * falling back to a default — had no coverage at all.
 *
 * <p>That is the expensive one to get wrong. The class javadoc records what happened
 * when it was: last season had four disagreeing fallbacks, and a robot could run one
 * robot's tuning while telling you it was using the other's. A regression here would
 * not crash. It would quietly pick a robot.
 *
 * <p>This runs on a plain JVM with no stubbing, which is itself the point of the
 * split between this enum and {@link ActiveConfig} — every SDK type lives on the
 * other side of it.
 */
public class RobotIdentityTest {

    @Test
    public void everyConstantResolvesFromItsOwnConfigName() {
        for (RobotIdentity identity : RobotIdentity.values()) {
            assertSame("\"" + identity.configName + "\" should resolve to " + identity,
                    identity, RobotIdentity.fromConfigName(identity.configName));
        }
    }

    /**
     * The anti-footgun. A name that is not ours resolves to nothing, so a caller has
     * to decide what to do about it — which {@link ActiveConfig#requireIdentity()}
     * does by throwing with instructions.
     */
    @Test
    public void anUnknownConfigNameResolvesToNullRatherThanADefault() {
        for (String unknown : new String[] {
                "", " ", "robot", "robot_", "robot_00000", "Robot_19429", "ROBOT_19429",
                "robot_19429 ", " robot_19429", "robot_19429.xml", "Untitled", "default"}) {
            assertNull("\"" + unknown + "\" is not one of ours and must not resolve",
                    RobotIdentity.fromConfigName(unknown));
        }
    }

    /** A missing active config arrives here as null; it must not blow up on the way. */
    @Test
    public void nullResolvesToNull() {
        assertNull(RobotIdentity.fromConfigName(null));
    }

    /**
     * Matching is exact, including case. The Driver Station shows the name the
     * resource entry has, so a near-miss means the wrong robot, not a typo to be
     * helpfully corrected.
     */
    @Test
    public void matchingIsCaseSensitive() {
        for (RobotIdentity identity : RobotIdentity.values()) {
            String upper = identity.configName.toUpperCase();
            if (!upper.equals(identity.configName)) {
                assertNull(upper + " must not resolve", RobotIdentity.fromConfigName(upper));
            }
        }
    }

    /**
     * "Exactly one string identifies a robot," per the class javadoc. Two constants
     * sharing a name would make {@code fromConfigName} return whichever came first
     * in declaration order, which is not a decision anyone would have made on
     * purpose.
     */
    @Test
    public void configNamesAreUnique() {
        Set<String> seen = new TreeSet<>();
        for (RobotIdentity identity : RobotIdentity.values()) {
            if (!seen.add(identity.configName)) {
                fail("Two RobotIdentity constants share the configName \""
                        + identity.configName + "\". One robot, one string.");
            }
        }
        assertEquals(RobotIdentity.values().length, seen.size());
    }

    /**
     * A {@code configName} is also an Android resource entry name, which constrains
     * it to lowercase letters, digits and underscores, starting with a letter.
     *
     * <p>Worth checking here rather than discovering it later: a constant like
     * {@code "Robot-3"} compiles and reads fine, and only fails when someone creates
     * the matching {@code res/xml} file and aapt rejects the filename — at which
     * point the error names the resource system, not this enum. The javadoc's claim
     * that the constant "is also its resource entry name, which is also its filename
     * without {@code .xml}" is only true while this holds.
     */
    @Test
    public void configNamesAreUsableAsResourceEntryNames() {
        for (RobotIdentity identity : RobotIdentity.values()) {
            String name = identity.configName;
            if (!name.matches("[a-z][a-z0-9_]*")) {
                fail("RobotIdentity." + identity + " has configName \"" + name + "\","
                        + " which cannot be an Android resource entry name. It must be"
                        + " lowercase letters, digits and underscores, starting with a"
                        + " letter — because it is also the name of a file in res/xml.");
            }
        }
    }
}
