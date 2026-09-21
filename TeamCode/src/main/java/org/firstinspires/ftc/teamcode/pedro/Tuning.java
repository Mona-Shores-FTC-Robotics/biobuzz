package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.StandIn;
import org.firstinspires.ftc.teamcode.pedro.procedures.ForesightTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.MecanumTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.PinpointTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.Tests;

/**
 * The procedures AutoTune offers on its webpage.
 *
 * <p>{@code TunerScanner} finds these by reflection at OpMode-discovery time. Each entry must be a
 * <b>static, no-argument method returning {@link Procedure}</b> — the scanner rejects anything else
 * with an {@code IllegalArgumentException} naming the offending method, so a mistake here breaks
 * robot startup rather than failing quietly.
 *
 * <p>Only the procedures matching our hardware are registered: mecanum drivetrain, Pinpoint
 * localizer. The Quickstart also ships OTOS, OctoQuad, two-wheel and three-wheel tuners; those
 * files stay in {@code procedures/} untouched so the package remains a verbatim upstream copy, but
 * listing them here would put tuners for hardware we don't have on the AutoTune page. Add one back
 * if the localizer changes.
 *
 * <p>See {@link Constants} for the order to run these in and where each one's output goes.
 */
public class Tuning {
    @Tuner(name = "Mecanum Tuner")
    public static Procedure mecanumTuner() {
        return new MecanumTuner();
    }

    @Tuner(name = "Pinpoint Tuner")
    public static Procedure pinpointTuner() {
        return new PinpointTuner();
    }

    @Tuner(name = "Foresight Tuner")
    public static Procedure foresightTuner() {
        return new ForesightTuner(Constants::createLocalizer, Tuning::realDrivetrain);
    }

    /**
     * The localization, odometry, pose and driving tests need only the drivetrain and localizer, so
     * they are usable before Foresight is tuned. The hold, line and curve tests build a
     * {@link com.pedropathing.follower.Follower} and will report Foresight as untuned until it is.
     */
    @Tuner(name = "Tests")
    public static Procedure tests() {
        return new Tests(Tuning::realDrivetrain, Constants::createLocalizer, Constants::createAlgorithm);
    }

    /**
     * {@link Constants#createDrivetrain}, but refusing to run on a {@link StandIn}.
     *
     * <p>Match OpModes put do-nothing stand-ins in the {@code HardwareMap} for missing motors so
     * the robot keeps playing (see {@code HardwareCheck}), and the SDK keeps that map until the
     * robot restarts. A tuner run afterwards would otherwise measure a robot with a motor that
     * does nothing and hand you numbers to paste. The Mecanum Tuner needs no such guard: it looks
     * motors up through {@code hardwareMap.dcMotor}, which stand-ins are never added to, so it
     * already fails with "could not find device".
     */
    static Mecanum realDrivetrain(HardwareMap hardwareMap) {
        for (String name : new String[] {DeviceNames.FRONT_LEFT, DeviceNames.FRONT_RIGHT,
                DeviceNames.BACK_LEFT, DeviceNames.BACK_RIGHT}) {
            if (StandIn.is(hardwareMap.tryGet(DcMotorEx.class, name))) {
                throw new IllegalStateException("Drive motor \"" + name + "\" is missing; a "
                        + "stand-in is filling in for it. Tuning would measure a motor that does "
                        + "nothing. Fix the wiring, restart the robot (DS menu -> Restart Robot), "
                        + "then tune.");
            }
        }
        return Constants.createDrivetrain(hardwareMap);
    }
}
