package org.firstinspires.ftc.teamcode.controls;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;

/**
 * Builds the printable cheat sheets, one per team and scheme, into
 * {@code TeamCode/build/cheat-sheets/}, and checks each lists every input its scheme binds.
 * {@code ./gradlew testDebugUnitTest}, then open a file in a browser and print it landscape.
 *
 * <p>A team logo, if someone has added one, is {@code TeamCode/cheat-sheets/logo-<number>.png}
 * (or {@code .svg}); without one the sheet draws an emblem in the team's colours.
 */
public class CheatSheetTest {

    @Test
    public void everyTeamAndSchemeHasASheetListingEveryInput() throws IOException {
        File teamCode = new File("src").isDirectory() ? new File(".") : new File("TeamCode");
        File dir = new File(new File(teamCode, "build"), "cheat-sheets");
        assertTrue("cannot make " + dir, dir.isDirectory() || dir.mkdirs());
        for (CheatSheet.Team team : CheatSheet.Team.values()) {
            String logo = logo(new File(teamCode, "cheat-sheets"), team.number);
            for (ControlScheme scheme : ControlScheme.values()) {
                String html = CheatSheet.html(scheme, team, logo);
                for (Map.Entry<Action, Input> entry : scheme.layout(ControlScheme.DEFAULT_FLOWER_PICK).entrySet()) {
                    assertTrue(team + " " + scheme + " sheet is missing " + entry.getKey(),
                            html.contains(entry.getKey().description.replace("&", "&amp;")));
                }
                String name = "controls-" + team.number + "-" + team.name.toLowerCase() + "-scheme-" + scheme.name() + ".html";
                Files.write(new File(dir, name).toPath(), html.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    /** The team's logo as a data URI, or null when there is none in the folder. */
    private static String logo(File folder, int number) throws IOException {
        File png = new File(folder, "logo-" + number + ".png");
        File svg = new File(folder, "logo-" + number + ".svg");
        File file = png.isFile() ? png : svg.isFile() ? svg : null;
        if (file == null) return null;
        String type = file == png ? "image/png" : "image/svg+xml";
        return "data:" + type + ";base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
    }
}
