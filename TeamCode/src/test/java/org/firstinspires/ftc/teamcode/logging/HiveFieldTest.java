package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The Belding fields' tip points ({@link HiveCalibration#tipPointInBand}): Field 1 tips lighter than the guide's
 * middle and Field 2 heavier, and at either one every placed case of the Event Field Setup Guide §12 still holds or
 * tips as the guide says, since both sat inside its band. (A tossed piece at Field 1 may tip 7 POLLEN, as one did in
 * Q-31; that is the field, not a fault.)
 *
 * <p>Opt-in, like every simulator test but the smoke test: {@code BIOBUZZ_SIM_CHECKS=1}.
 */
public class HiveFieldTest {

    @Before
    public void optIn() {
        assumeTrue("set BIOBUZZ_SIM_CHECKS=1 to run the simulator's HIVE checks", "1".equals(System.getenv("BIOBUZZ_SIM_CHECKS")));
    }

    @After
    public void restore() {
        HiveCalibration.tipPointInBand = HiveCalibration.GUIDE_MIDDLE;
    }

    private static double holdAt(double tipPoint) {
        HiveCalibration.tipPointInBand = tipPoint;
        return HiveCalibration.current().fit().holdTorque;
    }

    @Test
    public void field1TipsLighterThanTheGuideAndField2Heavier() {
        double f1 = holdAt(HiveCalibration.BELDING_FIELD_1);
        double guide = holdAt(HiveCalibration.GUIDE_MIDDLE);
        double f2 = holdAt(HiveCalibration.BELDING_FIELD_2);
        assertTrue("Field 1 " + f1 + " < guide " + guide, f1 < guide);
        assertTrue("guide " + guide + " < Field 2 " + f2, guide < f2);
    }

    @Test
    public void namesParse() {
        assertEquals(HiveCalibration.BELDING_FIELD_1, HiveCalibration.tipPointInBand("belding1"), 0);
        assertEquals(HiveCalibration.BELDING_FIELD_2, HiveCalibration.tipPointInBand("Belding2"), 0);
        assertEquals(HiveCalibration.GUIDE_MIDDLE, HiveCalibration.tipPointInBand("guide"), 0);
        assertEquals(0.3, HiveCalibration.tipPointInBand("0.3"), 0);
    }

    @Test
    public void bothFieldsStillPassTheGuidesPlacedCases() {
        for (double field : new double[] {HiveCalibration.BELDING_FIELD_1, HiveCalibration.BELDING_FIELD_2}) {
            HiveCalibration.tipPointInBand = field;
            FieldSim.Physics physics = HiveCalibration.current().fit();
            placed(physics, field, 3, 2);  // 3 NECTAR + 2 POLLEN holds, the 3rd POLLEN tips
            placed(physics, field, 0, 7);  // 7 POLLEN holds, the 8th tips
        }
    }

    /** A CELL holding these pieces must not have tipped; one more POLLEN placed must tip it. */
    private static void placed(FieldSim.Physics physics, double field, int nectar, int pollen) {
        String what = "tip point " + field + ": " + nectar + " NECTAR + " + pollen + " POLLEN";
        FieldSim sim = HiveCalibration.upwardCell(physics, nectar, pollen, false);
        assertEquals(what + " tipped; it must hold", 0, sim.red.tips);
        assertNotNull(sim.placeInRaisedCell(sim.red, FieldSim.Kind.POLLEN));
        HiveCalibration.settleRocker(sim);
        assertEquals(what + ", then one more placed, did not tip", 1, sim.red.tips);
    }
}
