package org.firstinspires.ftc.teamcode.hardware;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.junit.Test;

/** A {@link StandIn} must never throw, and must be consistent with itself. */
public class StandInTest {

    @Test
    public void everyMotorCallAPedroDrivetrainMakesIsHarmless() {
        DcMotorEx motor = StandIn.create(DcMotorEx.class, "frontLeft");

        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setPower(0.8);

        assertEquals(0, motor.getCurrentPosition());
        assertEquals(0.0, motor.getCurrent(CurrentUnit.AMPS), 0.0);
    }

    @Test
    public void aValueThatWasSetIsReadBack() {
        DcMotorEx motor = StandIn.create(DcMotorEx.class, "frontLeft");
        motor.setDirection(DcMotorSimple.Direction.REVERSE);
        assertEquals(DcMotorSimple.Direction.REVERSE, motor.getDirection());
    }

    @Test
    public void servosWorkToo() {
        Servo servo = StandIn.create(Servo.class, "gate");
        servo.setPosition(0.4);
        assertEquals(0.4, servo.getPosition(), 0.0);
    }

    @Test
    public void itSaysWhatItIs() {
        DcMotorEx motor = StandIn.create(DcMotorEx.class, "backLeft");
        assertTrue(motor.getDeviceName().contains("MISSING"));
        assertTrue(motor.toString().contains("backLeft"));
    }

    @Test
    public void isRecognisesOnlyStandIns() {
        assertTrue(StandIn.is(StandIn.create(DcMotorEx.class, "x")));
        assertFalse(StandIn.is(null));
        assertFalse(StandIn.is("not a device"));
    }

    @Test
    public void standInsCompareByIdentity() {
        DcMotorEx a = StandIn.create(DcMotorEx.class, "a");
        DcMotorEx b = StandIn.create(DcMotorEx.class, "a");
        assertEquals(a, a);
        assertNotEquals(a, b);
    }

    @Test(expected = IllegalArgumentException.class)
    public void aClassCannotBeStoodInFor() {
        StandIn.create(String.class, "pinpoint");
    }
}
