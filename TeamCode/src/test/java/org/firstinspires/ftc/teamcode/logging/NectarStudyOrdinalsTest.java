package org.firstinspires.ftc.teamcode.logging;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;
import static org.junit.Assert.*;

public final class NectarStudyOrdinalsTest {
    @Test public void lateFirstRollUsesOnlyTheObservedFirstTipAndPreservesTheHumanFault() {
        Map<String, Object> first = new HashMap<>();
        first.put("spot", "40.2,120.8,215");
        first.put("x", 8);
        first.put("y", 98.3);
        first.put("speed", 16);
        first.put("delay", 6);
        first.put("earliest", 9);
        first.put("aimSpreadDeg", 2);
        first.put("aimBiasDeg", -4);
        first.put("speedSpread", 1.5);
        Map<String, Object> alternate = new HashMap<>();
        alternate.put("afterTipTime", 12);
        alternate.put("receiver", "R");
        alternate.put("spot", "56.3,122.4,206.5");
        alternate.put("x", 8);
        alternate.put("y", 114.5);
        alternate.put("speed", 24);
        alternate.put("extraDelay", 0.75);
        Map<String, Object> request = new HashMap<>();
        request.put("roll", first);
        request.put("secondRoll", first);
        assertNull(NectarStudy.from(request).lateFirstRoll);
        request.put("lateFirstRoll", alternate);
        NectarStudy study = NectarStudy.from(request);
        NectarStudy.LateFirstRoll late = study.lateFirstRoll;
        assertFalse(late.applies(0, Double.NaN));
        assertFalse(late.applies(0, 12));
        assertTrue(late.applies(0, 12.02));
        assertFalse(late.applies(1, 18));
        assertTrue(late.receiverIsRight);
        assertEquals(6.75, late.roll.delay, 0);
        assertEquals(9, late.roll.earliest, 0);
        assertEquals(2, late.roll.aimSpreadDeg, 0);
        assertEquals(-4, late.roll.headingErrorDeg, 0);
        assertEquals(1.5, late.roll.speedSpread, 0);
        assertEquals(6, study.secondRoll.delay, 0);
        request.put("roll", null);
        assertNull(NectarStudy.from(request).lateFirstRoll);
        assertNotNull(NectarStudy.from(request).secondRoll);
    }

    @Test public void withholdingTheFirstDoesNotChangeWhichTipEarnsTheSecond() {
        Map<String, Object> second = new HashMap<>();
        second.put("spot", "59.2,18.5,95.7");
        second.put("x", 5.5);
        second.put("y", 95.0);
        second.put("speed", 24.0);
        second.put("delay", 0.5);
        second.put("earliest", 0);
        Map<String, Object> request = new HashMap<>();
        request.put("secondRoll", second);
        request.put("secondReceiver", "R");
        NectarStudy study = NectarStudy.from(request);
        assertNull(study.roll);
        assertTrue(study.secondRollToRight);
        assertEquals(0.5, study.secondRoll.delay, 0);

        AutoSim.Result result = new AutoSim.Result("r-right-under-hive", Alliance.RED,
                Collections.singletonList("r-right-under-hive"));
        result.secondHumanRoll.retainedAtTip = 3;
        result.settledLoads.add(new int[]{8, 1, 0, 1});
        Map<String, Object> row = new HashMap<>();
        study.addLedger(row, result);
        assertEquals(0, ((Map<?, ?>) row.get("human")).get("retainedAtTip"));
        assertEquals(3, ((Map<?, ?>) row.get("secondHuman")).get("retainedAtTip"));
        Map<?, ?> load = (Map<?, ?>) ((List<?>) row.get("settledLoads")).get(0);
        assertEquals(false, load.get("human"));
        assertEquals(true, load.get("secondHuman"));
    }
}
