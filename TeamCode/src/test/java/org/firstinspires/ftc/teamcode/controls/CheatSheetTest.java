package org.firstinspires.ftc.teamcode.controls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.opmodes.MatchPageExport;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;

/**
 * Builds the printable cheat sheets, one per team, into {@code TeamCode/build/cheat-sheets/}, and
 * checks each lists every button the layout binds (the sticks are left off).
 * {@code ./gradlew testDebugUnitTest}, then open a file in a browser and print it (portrait).
 *
 * <p>It also keeps {@code TeamCode/cheat-sheets/controls.json} ({@link ControlsExport}), the same
 * layout as data for the Visualizer's driver drill: it rewrites the file and fails while the committed
 * copy was stale, so running the tests once and committing the file brings it up to date. The same
 * goes for {@code match-page.json}, the TeleOp's Match page.
 *
 * <p>A team's logo is {@code TeamCode/cheat-sheets/logo-<number>.jpg} (or {@code .png}, {@code .svg});
 * without one the sheet draws an emblem in the team's colours.
 */
public class CheatSheetTest {

    @Test
    public void everyTeamHasASheetListingEveryInput() throws IOException {
        File teamCode = new File("src").isDirectory() ? new File(".") : new File("TeamCode");
        File dir = new File(new File(teamCode, "build"), "cheat-sheets");
        assertTrue("cannot make " + dir, dir.isDirectory() || dir.mkdirs());
        for (CheatSheet.Team team : CheatSheet.Team.values()) {
            String logo = logo(new File(teamCode, "cheat-sheets"), team.number);
            String html = CheatSheet.html(team, logo);
            for (Map.Entry<Action, Input> entry : ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK,
                    TurretSubsystem.lockedForward).entrySet()) {
                if (entry.getValue().isStick()) continue; // the sheet leaves the sticks off
                assertTrue(team + " sheet is missing " + entry.getKey(),
                        html.contains(entry.getKey().description.replace("&", "&amp;")));
            }
            assertTrue(team + " sheet is missing the restart rule", html.contains(CheatSheet.RULES));
            String name = "controls-" + team.number + "-" + team.name.toLowerCase() + ".html";
            Files.write(new File(dir, name).toPath(), html.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void controlsExportIsCommittedUpToDate() throws IOException {
        File teamCode = new File("src").isDirectory() ? new File(".") : new File("TeamCode");
        File file = new File(new File(teamCode, "cheat-sheets"), "controls.json");
        String json = ControlsExport.json();
        for (Map.Entry<Action, Input> entry : ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, false).entrySet()) {
            assertTrue("controls.json is missing " + entry.getKey(),
                    json.contains("\"action\": \"" + entry.getKey().name() + "\", \"input\": \"" + entry.getValue().name() + "\""));
        }
        String committed = file.isFile() ? new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8) : "";
        if (!committed.equals(json)) {
            Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));
        }
        assertEquals("TeamCode/cheat-sheets/controls.json was stale and has been rewritten: commit it", json, committed);
    }

    /**
     * {@code TeamCode/cheat-sheets/match-page.json} ({@code MatchPageExport}), the TeleOp's Match page as
     * data for the drill's simulated Driver Station: rewritten here, and a failure while the committed
     * copy was stale. {@code MatchPageExportTest} checks the samples are the helpers' own lines.
     */
    @Test
    public void matchPageExportIsCommittedUpToDate() throws IOException {
        File teamCode = new File("src").isDirectory() ? new File(".") : new File("TeamCode");
        File file = new File(new File(teamCode, "cheat-sheets"), "match-page.json");
        String json = MatchPageExport.json();
        String committed = file.isFile() ? new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8) : "";
        if (!committed.equals(json)) {
            Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));
        }
        assertEquals("TeamCode/cheat-sheets/match-page.json was stale and has been rewritten: commit it", json, committed);
    }

    /** The team's logo as a data URI, or null when there is none in the folder. */
    private static String logo(File folder, int number) throws IOException {
        String[][] kinds = {{"jpg", "image/jpeg"}, {"png", "image/png"}, {"svg", "image/svg+xml"}};
        for (String[] kind : kinds) {
            File file = new File(folder, "logo-" + number + "." + kind[0]);
            if (file.isFile()) {
                return "data:" + kind[1] + ";base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
            }
        }
        return null;
    }
}
