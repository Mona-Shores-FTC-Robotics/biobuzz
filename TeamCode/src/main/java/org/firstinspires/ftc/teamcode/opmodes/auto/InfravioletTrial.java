package org.firstinspires.ftc.teamcode.opmodes.auto;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LInfravioletAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RInfravioletAuto;

/** Runs the paired drafts from Utilities, with the same thirty-second stop as an AUTO period. */
public abstract class InfravioletTrial extends BuiltAuto {
    private long startedNs;

    protected InfravioletTrial(boolean left) {
        super(left ? LInfravioletAuto.SOURCE : RInfravioletAuto.SOURCE,
                left ? LInfravioletAuto.COMMANDS : RInfravioletAuto.COMMANDS,
                left ? LInfravioletAuto.TRIGGERS : RInfravioletAuto.TRIGGERS,
                left ? LInfravioletAuto.DRAWN_FOR : RInfravioletAuto.DRAWN_FOR,
                rotated -> left ? LInfravioletAuto.startPose(rotated) : RInfravioletAuto.startPose(rotated),
                (kit, rotated) -> left ? LInfravioletAuto.build(kit, rotated) : RInfravioletAuto.build(kit, rotated),
                left ? LInfravioletAuto.OPTIONS : RInfravioletAuto.OPTIONS);
    }

    @Override protected void onInitLoop() {
        super.onInitLoop();
        display.line("DRAFT PAIR: matching Infraviolet partner; automatic STOP after 30 s.");
    }

    @Override protected void onStart() {
        startedNs = System.nanoTime();
        super.onStart();
    }

    @Override protected void onLoop() {
        if (System.nanoTime() - startedNs >= 30_000_000_000L) {
            requestOpModeStop();
            return;
        }
        super.onLoop();
    }
}
