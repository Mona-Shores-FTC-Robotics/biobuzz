package org.firstinspires.ftc.teamcode.pedro;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Holds {@link Tuning} to the shape AutoTune's {@code TunerScanner} requires.
 *
 * <p>{@code Tuning}'s own javadoc spells out the contract and the cost of breaking
 * it: the scanner rejects a malformed entry with an {@code IllegalArgumentException}
 * naming the method, at OpMode-discovery time. That is robot startup. A student
 * adding a tuner and forgetting {@code static} does not get a compile error, a lint
 * warning, or a failing test — they get a robot that will not finish booting, at
 * whatever moment they next try to use it.
 *
 * <p>The contract is pure reflection over a class we own, so the JVM can check it
 * in milliseconds. This is the cheapest test in the suite and it guards the most
 * abrupt failure.
 *
 * <p><b>It deliberately does not invoke the methods.</b> Calling {@code mecanumTuner()}
 * constructs a {@code MecanumTuner}, and the {@code Tests} entry captures
 * {@code Constants::createAlgorithm}; both reach toward hardware and SDK types that
 * have no business being touched on a build machine. The signature is what the
 * scanner inspects, so the signature is what this checks.
 */
public class TuningContractTest {

    @Test
    public void tuningDeclaresAtLeastOneTuner() {
        if (annotatedMethods().isEmpty()) {
            fail("Tuning declares no @Tuner methods, so the AutoTune page would be empty."
                    + " Either this test is looking at the wrong class or the registrations"
                    + " were lost.");
        }
    }

    /**
     * The three rules {@code TunerScanner} enforces by reflection: static, no
     * arguments, returns a {@link Procedure}.
     */
    @Test
    public void everyTunerMethodMatchesWhatTheScannerRequires() {
        List<String> violations = new ArrayList<>();

        for (Method method : annotatedMethods()) {
            String where = method.getName() + "()";

            if (!Modifier.isStatic(method.getModifiers())) {
                violations.add(where + " is not static");
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                violations.add(where + " is not public");
            }
            if (method.getParameterCount() != 0) {
                violations.add(where + " takes " + method.getParameterCount()
                        + " argument(s); the scanner calls it with none");
            }
            if (!Procedure.class.isAssignableFrom(method.getReturnType())) {
                violations.add(where + " returns " + method.getReturnType().getName()
                        + ", not a Procedure");
            }
        }

        if (!violations.isEmpty()) {
            fail("Tuning has @Tuner method(s) the AutoTune scanner will reject at"
                    + " OpMode-discovery time, which breaks robot startup:\n  - "
                    + String.join("\n  - ", violations)
                    + "\n\nEach entry must be a public static no-argument method returning"
                    + " Procedure.");
        }
    }

    /**
     * The {@code name} is what appears on the AutoTune webpage, so a blank or
     * duplicated one produces a page you cannot navigate — and a duplicate is the
     * easy mistake when a tuner is added by copying the one above it.
     */
    @Test
    public void tunerNamesArePresentAndUnique() {
        Set<String> seen = new TreeSet<>();
        List<String> violations = new ArrayList<>();

        for (Method method : annotatedMethods()) {
            String name = method.getAnnotation(Tuner.class).name();
            if (name == null || name.trim().isEmpty()) {
                violations.add(method.getName() + "() has a blank @Tuner name");
            } else if (!seen.add(name)) {
                violations.add(method.getName() + "() reuses the @Tuner name \"" + name + "\"");
            }
        }

        if (!violations.isEmpty()) {
            fail("AutoTune entries are not distinctly named:\n  - "
                    + String.join("\n  - ", violations) + "\n\nNames seen: " + seen);
        }
    }

    /**
     * Registering a tuner for hardware the robot does not have puts a procedure on
     * the AutoTune page that cannot complete. {@code Tuning}'s javadoc records the
     * decision — mecanum drivetrain, Pinpoint localizer, and deliberately not the
     * OTOS, OctoQuad, two-wheel or three-wheel tuners the Quickstart also ships.
     *
     * <p>This asserts that decision rather than a count, so adding a tuner for
     * hardware we <em>do</em> have is a one-line edit here, while adding one for
     * hardware we do not fails and points at the note explaining why.
     */
    @Test
    public void onlyTunersForHardwareThisRobotHasAreRegistered() {
        // Matched on the @Tuner name rather than the return type: every one of these
        // methods is declared as returning Procedure, so the return type carries no
        // information about which tuner it is. The name is what reaches the page.
        Set<String> unexpected = new TreeSet<>();
        for (Method method : annotatedMethods()) {
            String name = method.getAnnotation(Tuner.class).name();
            for (String absent : new String[] {"OTOS", "OctoQuad", "Two Wheel", "Three Wheel"}) {
                if (name.contains(absent)) {
                    unexpected.add(name);
                }
            }
        }
        assertTrue("Tuning registers procedure(s) for hardware this robot does not have: "
                        + unexpected + ". See the note in Tuning about why the Quickstart's"
                        + " other tuners stay in procedures/ but off the AutoTune page.",
                unexpected.isEmpty());
    }

    private static List<Method> annotatedMethods() {
        List<Method> annotated = new ArrayList<>();
        for (Method method : Tuning.class.getDeclaredMethods()) {
            if (method.getAnnotation(Tuner.class) != null) {
                annotated.add(method);
            }
        }
        return annotated;
    }
}
