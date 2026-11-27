package org.firstinspires.ftc.teamcode.subsystems;

/**
 * Master's corner-wheel policy for the simulator (test code only): whether the wheels rake, asked of
 * {@link CornerWheelSubsystem#stopFor} itself, which is package-private, so a rule changed there changes the
 * simulation and nothing is copied.
 */
public final class CornerWheelRules {

    private CornerWheelRules() {
    }

    /** Whether the robot has the wheels at all ({@link CornerWheelSubsystem#fitted}). */
    public static boolean fitted() {
        return CornerWheelSubsystem.fitted;
    }

    /** Whether the wheels rake now: after PLAY, with {@link CornerWheelSubsystem#stopFor} naming no reason to stop. */
    public static boolean raking(boolean takingIn, boolean laneFull, int held, boolean wrongNectar, boolean stickDown) {
        return CornerWheelSubsystem.stopFor(true, takingIn, laneFull, held, wrongNectar, stickDown)
                == CornerWheelSubsystem.Why.RAKING;
    }
}
