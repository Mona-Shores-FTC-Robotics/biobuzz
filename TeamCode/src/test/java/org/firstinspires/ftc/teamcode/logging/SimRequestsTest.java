package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Every Auto's simulation request ({@code TeamCode/sim-requests/<name>.json}) is one the "Simulate Auto"
 * workflow can run: it belongs to an Auto we run, names a generated class and a robot design the simulator
 * knows, and each {@code partners} entry names a stand-in from {@code TeamCode/autos/partners/} (the curated
 * set the mentor picks per Auto, 3 Nov 2026). A typo would otherwise fail only in the workflow, after the
 * save. The format is {@link SimRunTest}'s; what a run does with the entries is its business.
 */
public class SimRequestsTest {

    @Test
    @SuppressWarnings("unchecked")
    public void everyRequestNamesAnAutoItsDesignAndRealStandIns() throws Exception {
        File teamCode = TeamCodeDir.get();
        File autos = new File(teamCode, "autos");
        File partners = new File(autos, "partners");
        File[] files = new File(teamCode, "sim-requests").listFiles((d, n) -> n.endsWith(".json"));
        assertTrue("TeamCode/sim-requests/ is missing or empty", files != null && files.length > 0);

        Set<String> designs = AutoStudyTest.designs().keySet();
        List<String> problems = new ArrayList<>();
        for (File file : files) {
            String name = file.getName().substring(0, file.getName().length() - ".json".length());
            Map<String, Object> request;
            try {
                request = (Map<String, Object>) MiniJson.parse(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
            } catch (RuntimeException e) {
                problems.add(file.getName() + ": not JSON (" + e.getMessage() + ")");
                continue;
            }
            // The request is the Auto's own: its .pp beside it in TeamCode/autos/, under the same name.
            if (!("TeamCode/autos/" + name + ".pp").equals(request.get("pp"))) {
                problems.add(file.getName() + ": pp should be TeamCode/autos/" + name + ".pp, not " + request.get("pp"));
            }
            if (!new File(autos, name + ".pp").isFile()) {
                problems.add(file.getName() + ": there is no TeamCode/autos/" + name + ".pp; a request belongs to an Auto we run");
            }
            String spec = (String) request.get("spec");
            if (spec == null || spec.isEmpty()) {
                problems.add(file.getName() + ": no spec");
                continue;
            }
            // Our Auto, then the drive profile; a comma names a partner the old way, which still runs, but the
            // curated partners belong in "partners".
            String ours = spec.split("@")[0].split("\\|")[0];
            if (ours.contains(",")) {
                problems.add(file.getName() + ": spec \"" + spec + "\" names a partner; the curated partners go in \"partners\"");
                ours = ours.split(",")[0];
            }
            if (!generatedExists(ours)) {
                problems.add(file.getName() + ": spec names " + ours + ", which is not a generated Auto");
            }
            checkDesign(problems, file, request.get("design"), designs);
            Object list = request.get("partners");
            if (list == null) continue;  // a request with no partner runs is fine: the Auto alone
            if (!(list instanceof List)) {
                problems.add(file.getName() + ": \"partners\" should be a list of entries");
                continue;
            }
            for (Object o : (List<Object>) list) {
                if (!(o instanceof Map)) {
                    problems.add(file.getName() + ": a partners entry is not an object: " + o);
                    continue;
                }
                Map<String, Object> entry = (Map<String, Object>) o;
                Object partner = entry.get("partner");
                if (!(partner instanceof String) || ((String) partner).isEmpty()) {
                    problems.add(file.getName() + ": a partners entry has no \"partner\" class: " + o);
                    continue;
                }
                String partnerClass = (String) partner;
                if (!generatedExists(partnerClass)) {
                    problems.add(file.getName() + ": partner " + partnerClass + " is not a generated Auto");
                    continue;
                }
                // A partner is a stand-in from TeamCode/autos/partners/, never one of the Autos we run.
                String source = (String) Class.forName(AutoStudyTest.PKG + partnerClass).getField("SOURCE").get(null);
                if (!new File(partners, source).isFile()) {
                    problems.add(file.getName() + ": partner " + partnerClass + " comes from " + source
                            + ", which is not in TeamCode/autos/partners/ (a partner is a stand-in, not an Auto we run)");
                }
                checkDesign(problems, file, entry.get("design"), designs);
            }
        }
        if (!problems.isEmpty()) fail(String.join("\n", problems));
    }

    private static void checkDesign(List<String> problems, File file, Object design, Set<String> designs) {
        if (design != null && !designs.contains(String.valueOf(design))) {
            problems.add(file.getName() + ": design \"" + design + "\" is not one the simulator knows " + designs);
        }
    }

    private static boolean generatedExists(String className) {
        try {
            Class.forName(AutoStudyTest.PKG + className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
