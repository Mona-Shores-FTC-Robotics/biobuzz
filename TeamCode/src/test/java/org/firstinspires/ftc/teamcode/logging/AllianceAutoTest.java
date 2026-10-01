package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.DuoNorthAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.DuoSouthAuto;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;

/**
 * Both of an alliance's robots, each running its own Auto on one field: duo-south loads the HIVE's
 * south CELL and duo-north the north one, each firing when the other's TIP brings its CELL up, and
 * both end parked in the LOADING ZONE. Writes
 * {@code TeamCode/build/sim-logs/alliance-duo-<alliance>.wpilog}; TeamCode/README.md, "Two robots",
 * says how to watch both.
 */
public class AllianceAutoTest {

    @Test
    public void duoTipsThreeTimesAndBothRobotsLeaveAndPark() throws Exception {
        for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
            AutoSim.Result r = new AutoSim(DuoSouthAuto.class, alliance, 3572L).speed(60, 55)
                    .alsoRun(DuoNorthAuto.class).speed(60, 55)
                    .write(new File(TeamCodeDir.simLogs(), "alliance-duo-" + alliance.name().toLowerCase() + ".wpilog"));
            System.out.println(r);
            assertTrue(r.toString(), r.autoTips() >= 3);
            for (AutoSim.RobotResult robot : r.robots) {
                assertTrue(r.toString(), robot.leave && robot.park);
                assertTrue(r.toString(), Double.isNaN(robot.crossedAt));
            }
            assertTrue(r.toString(), Double.isNaN(r.robotsCollidedAt));
        }
    }
}
