package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The partner wait: the partner's last shot as their team said it, entered in INIT
 * ({@link InitOptions}, tested in {@code InitOptionsTest}), plus the TIP lag, which is written in
 * two places that must agree ({@code TeamCode/autos/timing.json} and
 * {@link AutoRegistration#PARTNER_TIP_LAG_S}).
 */
public class PartnerWaitTest {

    @Test
    public void theLagInTimingJsonIsTheRobots() throws Exception {
        String text = new String(Files.readAllBytes(timingFile().toPath()), StandardCharsets.UTF_8);
        Matcher m = Pattern.compile("\"partnerTipLagS\"\\s*:\\s*\\{\\s*\"value\"\\s*:\\s*([0-9.]+)").matcher(text);
        assertTrue("timing.json has no partnerTipLagS fact", m.find());
        assertEquals("timing.json's partnerTipLagS and AutoRegistration.PARTNER_TIP_LAG_S differ: change both",
                AutoRegistration.PARTNER_TIP_LAG_S, Double.parseDouble(m.group(1)), 1e-9);
    }

    @Test
    public void theWaitIsTheLastShotPlusTheLag() {
        assertEquals(8 + AutoRegistration.PARTNER_TIP_LAG_S, AutoRegistration.partnerWaitUntilS(8), 1e-9);
        assertTrue(Double.isNaN(AutoRegistration.partnerWaitUntilS(Double.NaN)));
    }

    /** TeamCode/autos/timing.json, from the module dir under Gradle or the repo root in an IDE. */
    private static File timingFile() {
        File candidate = new File(System.getProperty("user.dir"));
        for (int depth = 0; depth < 4 && candidate != null; depth++) {
            for (String path : new String[]{"autos/timing.json", "TeamCode/autos/timing.json"}) {
                File file = new File(candidate, path);
                if (file.isFile()) return file.getAbsoluteFile();
            }
            candidate = candidate.getParentFile();
        }
        throw new AssertionError("TeamCode/autos/timing.json not found from " + System.getProperty("user.dir"));
    }
}
