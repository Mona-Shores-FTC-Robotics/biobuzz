package org.firstinspires.ftc.teamcode.controls;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** {@link Display#visibleLength} against the regexes it replaced, on the shapes the pages write. */
public class DisplayRowsTest {

    private static int byRegex(String html) {
        return html.replaceAll("<[^>]+>", "").replaceAll("&[a-zA-Z#0-9]+;", "x").length();
    }

    @Test
    public void countsWhatTheRegexesCounted() {
        String[] samples = {
                "", "plain text", "<b>Odometry</b> · (55.6, 7.7) facing 90°",
                "<small>Cross (A) relocalize · D-pad left reset</small>",
                "Tag 35: ACCEPTED · height +1.6 · 51 in · field -0.6, -1.0",
                "a &amp; b &lt; c &#176; d", "<big><b>RED</b></big> alliance", "x < y and 3 > 2",
                "&broken entity", "trailing &", "<tt>mono</tt><br>next", "<br><br>",
                "<span style=\"color:#AB47BC\">shadow</span>",
        };
        for (String s : samples) {
            assertEquals(s, byRegex(s), Display.visibleLength(s));
        }
    }
}
