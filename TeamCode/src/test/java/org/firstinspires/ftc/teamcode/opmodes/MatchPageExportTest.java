package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.localization.PinpointTrust;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link MatchPageExport}: the Match page's helpers write what {@code onLoop} wrote before they were
 * pulled out, and every line they can write on the drill's grid is among the export's samples.
 */
public class MatchPageExportTest {

    /** The lines exactly as {@code onLoop} built them inline before the extract (c3a6327). */
    @Test
    public void extractedLinesAreTheOldOnes() {
        assertEquals(Display.huge("AIM: Circle fires", Display.Level.OK), BasicDriveTeleOp.turretModeLine(true));
        assertEquals(Display.huge("LOCKED: Circle turns, then fires", Display.Level.WARN), BasicDriveTeleOp.turretModeLine(false));
        assertEquals(Display.huge("IDLE", (Display.Level) null), BasicDriveTeleOp.launcherLine(false, true));
        assertEquals(Display.huge("SPIN", Display.Level.WARN), BasicDriveTeleOp.launcherLine(true, false));
        assertEquals(Display.huge("READY", Display.Level.OK), BasicDriveTeleOp.launcherLine(true, true));
        assertEquals(Display.statusLine(Display.Level.FAULT,
                "Fire: turn the robot: CELL behind the turret's firing arc (or LOCK and hold Circle)"), BasicDriveTeleOp.arcBlockedLine(true));
        assertEquals(Display.statusLine(Display.Level.FAULT,
                "Fire: turret outside its firing arc: wait for it to come round to the front"), BasicDriveTeleOp.arcBlockedLine(false));
        assertEquals(Display.statusLine(Display.Level.WARN, "Fire: outside the turret's firing arc: shots may hit the robot"),
                BasicDriveTeleOp.outsideArcLine());
        assertEquals(Display.statusLine(Display.Level.FAULT, "Fire: WRONG NECTAR may be in the lane: gate shut. EJECT to clear"),
                BasicDriveTeleOp.wrongNectarLine());
        assertEquals(Display.huge("FIRE: WAITING FOR 4", Display.Level.WARN), BasicDriveTeleOp.fullHoldLine());
        assertEquals(Display.huge("FIRE: OUR CELL IS DOWN", Display.Level.WARN), BasicDriveTeleOp.cellHoldLine());
        assertEquals(Display.huge("SQUARE REFUSED", Display.Level.WARN), BasicDriveTeleOp.matchAlertLine("SQUARE REFUSED"));
        String[] alerts = {"FEED STUCK: UNJAM", "TURRET DROPPED: TOGGLE AIM", "NO PINPOINT", "INTAKE OFF",
                "ROBOT-CENTRIC", "SQUARE REFUSED", "FLOWER STICK OUT"};
        assertEquals(java.util.Arrays.asList(alerts), java.util.Arrays.asList(BasicDriveTeleOp.MATCH_ALERTS));
        assertEquals("Keep holding (0.5 s) to reset…", BasicDriveTeleOp.KEEP_HOLDING);
        assertEquals("<b>MATCH</b>  <small>(1/3 · Share (Back) for next page)</small>", Display.headerLine(Display.Page.MATCH));
    }

    @Test
    public void everyHelperLineIsASample() {
        Set<String> samples = new HashSet<>();
        for (MatchPageExport.Row row : MatchPageExport.rows()) {
            for (Object v : row.samples.values()) {
                if (v instanceof String) samples.add((String) v);
            }
        }
        List<String> lines = new ArrayList<>();
        for (boolean b : new boolean[]{true, false}) {
            lines.add(BasicDriveTeleOp.turretModeLine(b));
            lines.add(BasicDriveTeleOp.arcBlockedLine(b));
            for (boolean c : new boolean[]{true, false}) lines.add(BasicDriveTeleOp.launcherLine(b, c));
            for (int n = 0; n <= 4; n++) lines.add(BasicDriveTeleOp.piecesLine(n, b));
        }
        lines.add(BasicDriveTeleOp.outsideArcLine());
        lines.add(BasicDriveTeleOp.wrongNectarLine());
        lines.add(BasicDriveTeleOp.fullHoldLine());
        lines.add(BasicDriveTeleOp.cellHoldLine());
        lines.add(BasicDriveTeleOp.KEEP_HOLDING);
        for (String a : BasicDriveTeleOp.MATCH_ALERTS) lines.add(BasicDriveTeleOp.matchAlertLine(a));
        for (int t = -150; t <= 150; t += 5) lines.add(BasicDriveTeleOp.launchTrimLine(t));
        for (double d : MatchPageExport.aimTrims()) {
            lines.add(BasicDriveTeleOp.aimTrimLine(d));
            String hint = BasicDriveTeleOp.aimTrimHint(d);
            if (hint != null) lines.add(hint);
        }
        for (int i = -20; i <= 20; i++) lines.add(BasicDriveTeleOp.aimTrimLine(i * 0.5));
        lines.add(BasicDriveTeleOp.aimTrimLine(-180.0));
        lines.add(BasicDriveTeleOp.aimTrimLine(180.0));
        lines.add(BasicDriveTeleOp.loopLine(Double.NaN));
        lines.add(BasicDriveTeleOp.pinpointLine(false, PinpointTrust.State.TRUSTED, 0));
        for (PinpointTrust.State s : PinpointTrust.State.values()) lines.add(BasicDriveTeleOp.pinpointLine(true, s, 0));
        lines.add(BasicDriveTeleOp.pinpointLine(true, PinpointTrust.State.TRUSTED, 1));
        for (OurCell.Reading now : OurCell.Reading.values()) {
            for (OurCell.Reading last : OurCell.Reading.values()) {
                for (int age = 0; age <= 8; age++) lines.add(OurCell.line(now, last, age));
            }
        }
        lines.add(OurCell.line(OurCell.Reading.UNKNOWN, OurCell.Reading.UNKNOWN, Double.NaN));
        for (String line : lines) assertTrue("match-page.json has no sample " + line, samples.contains(line));
    }

    @Test
    public void practiceReadoutTimesBothCycles() {
        MatchPageExport.Row readout = null;
        for (MatchPageExport.Row row : MatchPageExport.rows()) if (row.id.equals("practiceReadout")) readout = row;
        assertTrue(readout != null && readout.samples.containsKey("cycle2"));
        String two = String.valueOf(readout.samples.get("cycle2"));
        assertTrue(two, two.contains("CYCLE") || two.contains("cycle best"));
    }
}
