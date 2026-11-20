package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.autokit.AutoOption;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A draft Auto with a loaded leg driven backward, run from the Utility menu as a 30 s trial. Only a robot that keeps
 * its load in driving backward ({@link IntakeSubsystem#holdsLoadDrivingBackward}, on in {@code RobotDifferences} for a
 * robot with a bottom plate under its intake: 19429 now, 20245 once its plate is on) runs it; on any other robot it
 * says so in INIT and does not move at PLAY. The backward drive in
 * {@code doc/robot-todo.md} has to pass before one of these runs.
 */
abstract class BackwardLoadTrial extends BuiltAuto {
    private final String note;
    private boolean refused;
    private long startedNs;

    BackwardLoadTrial(String source, String[] commands, String[] triggers, String drawnFor,
                      Function<Boolean, Pose> startPose, BiFunction<AutoKit, Boolean, Command> build,
                      AutoOption[] options, String note) {
        super(source, commands, triggers, drawnFor, startPose, build, options);
        this.note = note;
    }

    private static final String REFUSAL = "This robot does not hold its load driving backward"
            + " (IntakeSubsystem.holdsLoadDrivingBackward off): a robot with a bottom plate only. Not moving.";

    @Override protected void onInitLoop() {
        super.onInitLoop();
        if (!IntakeSubsystem.holdsLoadDrivingBackward) {
            display.status("Backward legs", Display.Level.FAULT, REFUSAL);
            return;
        }
        display.line("DRAFT, bottom plate only: " + note + " Run only after the backward drive in robot-todo passed;"
                + " automatic STOP after 30 s.");
    }

    @Override protected void onStart() {
        startedNs = System.nanoTime();
        refused = !IntakeSubsystem.holdsLoadDrivingBackward;
        if (refused) {
            log.event("Backward-leg trial refused: " + REFUSAL);
            return;
        }
        super.onStart();
    }

    @Override protected void onLoop() {
        if (refused) {
            display.status("Backward legs", Display.Level.FAULT, REFUSAL);
            return;
        }
        if (System.nanoTime() - startedNs >= 30_000_000_000L) {
            requestOpModeStop();
            return;
        }
        super.onLoop();
    }
}
