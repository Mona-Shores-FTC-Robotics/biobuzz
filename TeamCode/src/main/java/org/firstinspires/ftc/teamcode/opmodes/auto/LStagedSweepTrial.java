package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LStagedSweepAuto;

/**
 * Runs the {@link LStagedSweepAuto} draft from the Utility menu: the robot starts empty; see
 * {@code doc/drive-coach-checklist.md} for where its 4 preloads go on the tiles (manual 10.3.4, G304.G). It picks
 * the SCORING FLOWER first, fires those 4 at LEFT from LEFT_AUTO_HIVE once TIP 1 is up, sweeps the staged row,
 * fires again behind a CELL check and parks at LEFT_PARK. A student's idea (Brady, 23 Nov 2026).
 */
@Utility(name = "Drive: L-StagedSweep " + LStagedSweepAuto.SAVED,
        description = "Draft: preloads staged on the tiles; FLOWER first, LEFT volley, sweep the row, LEFT volley, park")
public final class LStagedSweepTrial extends BuiltAuto {
    public LStagedSweepTrial() {
        super(LStagedSweepAuto.SOURCE, LStagedSweepAuto.COMMANDS, LStagedSweepAuto.TRIGGERS,
                LStagedSweepAuto.DRAWN_FOR, LStagedSweepAuto::startPose, LStagedSweepAuto::build,
                LStagedSweepAuto.OPTIONS);
    }
}
