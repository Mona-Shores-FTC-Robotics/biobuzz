package org.firstinspires.ftc.teamcode.pedro;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Pins the untuned-Foresight guard in {@link Constants#createAlgorithm()}.
 *
 * <p>Twelve of {@code ForesightConfig}'s variables are {@code required} with no
 * default, and reading an unset one throws
 * {@code IllegalStateException("Config variable has not been set")} — no field
 * name, no hint, and it surfaces partway through following a path rather than at
 * init. {@code createAlgorithm()} exists to turn that into a sentence naming the
 * tuner to run and the field to paste into.
 *
 * <p>That guard is exactly the kind of code a later reader deletes as redundant:
 * it looks like a try/catch that rethrows the same exception type. This test
 * records that the <em>message</em> is the point, so removing it fails the build
 * instead of quietly costing someone an afternoon at a meeting.
 *
 * <p>It also encodes the current state of the robot — Foresight is untuned on
 * {@code master}. When the Foresight Tuner's output is finally pasted into
 * {@code Constants.foresightConfig}, this test starts failing, and the failure
 * message says so and tells you to invert it. That is the intended signal, not a
 * bug: "the robot has been tuned" is a real event worth noticing in the build.
 */
public class ConstantsTest {

    @Test
    public void createAlgorithmRefusesToBuildAnUntunedForesight() {
        try {
            Constants.createAlgorithm();
        } catch (IllegalStateException expected) {
            String message = String.valueOf(expected.getMessage());

            assertTrue("The guard's whole purpose is naming the tuner to run, but the"
                            + " message was: " + message,
                    message.contains("Foresight Tuner"));
            assertTrue("The message must name the field to paste into, but it was: " + message,
                    message.contains("Constants.foresightConfig"));
            assertTrue("The original cause must be kept, or the underlying config error is"
                            + " lost from the stack trace",
                    expected.getCause() instanceof IllegalStateException);
            return;
        }

        fail("Constants.createAlgorithm() built a Foresight without throwing, which means"
                + " foresightConfig now has its twelve required values set."
                + "\n\nIf the Foresight Tuner has been run and its block pasted in, that is"
                + " good news — replace this test with one asserting the tuned values are"
                + " present and sane."
                + "\n\nIf it has not, the guard in createAlgorithm() has stopped working and"
                + " an untuned robot will now fail mid-path instead of at init.");
    }
}
