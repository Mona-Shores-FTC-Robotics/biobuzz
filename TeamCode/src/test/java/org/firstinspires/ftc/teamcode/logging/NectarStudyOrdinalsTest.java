package org.firstinspires.ftc.teamcode.logging;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;
import static org.junit.Assert.*;

public final class NectarStudyOrdinalsTest {
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
