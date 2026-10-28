package org.firstinspires.ftc.teamcode.controls;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

/**
 * Builds the printable cheat sheets, one per scheme, into {@code TeamCode/build/cheat-sheets/},
 * and checks each lists every input its scheme binds. {@code ./gradlew testDebugUnitTest}, then
 * open the file in a browser and print.
 */
public class CheatSheetTest {

    @Test
    public void everySchemeHasASheetListingEveryInput() throws IOException {
        File dir = new File(new File("build").isDirectory() && new File("src").isDirectory()
                ? "build" : "TeamCode/build", "cheat-sheets");
        assertTrue("cannot make " + dir, dir.isDirectory() || dir.mkdirs());
        for (ControlScheme scheme : ControlScheme.values()) {
            String html = CheatSheet.html(scheme);
            for (Map.Entry<Action, Input> entry : scheme.layout(ControlScheme.DEFAULT_FLOWER_PICK).entrySet()) {
                assertTrue(scheme + " sheet is missing " + entry.getKey(),
                        html.contains(entry.getKey().description.replace("&", "&amp;")));
            }
            Files.write(new File(dir, "controls-scheme-" + scheme.name() + ".html").toPath(),
                    html.getBytes(StandardCharsets.UTF_8));
        }
    }
}
