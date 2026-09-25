package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * A subsystem with nothing in it. <b>Copy this file, rename it, and fill it in.</b>
 *
 * <p>This is a starting point, not a working mechanism. It talks to no hardware on purpose — there
 * is no motor here to guess wrong about, because when this was written BIOBUZZ had no intake, no
 * turret and no launcher designed yet. Adding a fake one would hand you decisions somebody else
 * made about last season's robot.
 *
 * <h2>How to turn this into a real subsystem</h2>
 *
 * <ol>
 *   <li><b>Copy this file</b> into this package and give it a name that says what the mechanism is —
 *       {@code IntakeSubsystem}, {@code TurretSubsystem}. Rename the class to match the file.</li>
 *   <li><b>Add the hardware as fields</b> and look it up once in the constructor. Names come from
 *       {@link org.firstinspires.ftc.teamcode.hardware.DeviceNames} and nowhere else — never type a
 *       device name as a string here, because a name written twice is a name that can disagree with
 *       itself, and a unit test enforces that rule.</li>
 *   <li><b>Write {@link #update()}</b>. That is the one method {@link Subsystem} requires.</li>
 *   <li><b>Add a field for it</b> in {@link org.firstinspires.ftc.teamcode.Robot} and build it in
 *       that constructor, so every OpMode can reach it.</li>
 * </ol>
 *
 * <h2>Things worth knowing before you start</h2>
 *
 * <ul>
 *   <li><b>A missing device must not stop the robot.</b> If {@code hardwareMap.get(...)} throws
 *       because the part is not plugged in or not in the configuration, catch it, remember why, and
 *       let the rest of the robot run. Half a working robot beats an OpMode that will not start —
 *       see {@code LimelightVisionSubsystem} for how it reports being unavailable.</li>
 *   <li><b>{@code update()} runs every loop.</b> Do not {@code sleep()}, do not wait for anything,
 *       and try not to create new objects in it. Whatever time it takes is time the drivetrain is
 *       not being updated.</li>
 *   <li><b>Keep the driver's buttons out of here.</b> A subsystem exposes what the mechanism can do
 *       ({@code open()}, {@code setSpeed(...)}); which button triggers it belongs in the bindings.
 *       That way two drivers can disagree about the controls without anyone touching this file.</li>
 * </ul>
 */
public class ExampleSubsystem implements Subsystem {

    // TODO: your hardware goes here, for example:
    //   private final DcMotorEx motor;

    /**
     * Look your hardware up once, here — not in {@link #update()}. A {@code hardwareMap} lookup is
     * slow, and doing it every loop is one of the easier ways to make a robot feel sluggish.
     *
     * @param hardwareMap passed down from the OpMode by {@link org.firstinspires.ftc.teamcode.Robot}
     */
    public ExampleSubsystem(HardwareMap hardwareMap) {
        // TODO: look up your hardware, using a name from DeviceNames. For example:
        //   motor = hardwareMap.get(DcMotorEx.class, DeviceNames.SOME_MOTOR);
    }

    /**
     * One step of this mechanism's work, called once per OpMode loop.
     *
     * <p>Empty is correct for now — this subsystem has no mechanism. Yours will read a sensor,
     * decide something, and set a motor or servo.
     */
    @Override
    public void update() {
        // TODO: one step of the work. Read, decide, act. Then return.
    }
}
