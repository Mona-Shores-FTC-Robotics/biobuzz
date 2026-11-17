package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.generated.TeamSpots;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

/**
 * The spots an Auto launches from, besides its own start: a HIVE spot and an OFFSET spot at each
 * end of our HIVE, drawn for red (mentor, 16 Nov 2026). They are team spots under the same names
 * in {@code TeamCode/autos/spots.json}, which is what the Visualizer and so every generated Auto
 * reads; each pose here is read from the generated {@link TeamSpots}, which the Visualizer writes
 * with it, so a spot is moved in the Visualizer only. The drivers' Square has its own twelve
 * ({@link LaunchSpots}), which an Auto does not use.
 *
 * <p><b>Why not Square's.</b> Square's CLOSE row stops 4 in back from where it is drawn, for the
 * HIVE's lip, and there every CLOSE spot overlaps a robot still on that end's start. These are
 * placed so two of our robots can hold HIVE and OFFSET at one end together:
 * <ul>
 *   <li>{@link Spot#hive HIVE}: in front of the CELL, 40 in from its raised row (Square's CLOSE
 *       range), slid toward the centre line and turned back to aim: half an inch short of the line
 *       on the left, where the turret's side leaves 2 in to spare; 6 degrees round on the right.</li>
 *   <li>OFFSET: on our wall's side of it, about 29 degrees off square. On the left, 44 in out and
 *       2 in from a robot on HIVE. On the right, 52 in out: 4 in from a robot on the fix-TIP spot
 *       below (2 in was not enough for a robot arriving there in the simulator) and further still
 *       from one on HIVE. It also clears a robot left on that end's start, which HIVE does not.</li>
 * </ul>
 *
 * <p><b>One more, for one job:</b> {@code RIGHT_AUTO_FIX_TIP}, where a left-start Auto that has
 * crossed under the HIVE fires its preloads at the RIGHT CELL when the partner's TIP did not come
 * (mentor, 16 Nov 2026). It is square to the wall and in line with the CELL, its back edge 18 in
 * from the audience wall: no robot may start more than 18 in deep, so it clears whatever is still
 * on {@code RIGHT_START}, and it keeps the robot out of the lanes a partner drives to the WALL
 * FLOWER and the GARDEN. That puts it 35 in from the row, where volleys have hit the HIVE's lip:
 * the price of standing clear.
 *
 * <p><b>Which one a volley uses</b> is each Auto's to work out. HIVE is preferred; a pair uses one
 * each; a robot that comes under the HIVE shoots from HIVE, one that comes round the outside lane
 * from OFFSET. A left-start Auto moves up from {@code LEFT_START} to {@code LEFT_AUTO_HIVE} for
 * its first volley; a right-start Auto fires its preloads from {@code RIGHT_START}, where it is
 * placed, so that volley waits on no drive. {@code AutoVolleySpotsTest} fails an Auto on master
 * that launches from anywhere but a start or one of these.
 *
 * <p>Each heading points the turret's axis ({@code TurretSubsystem}: 3.33 in behind the centre,
 * 2.68 in right of it) at the CELL's raised row from where the spot is; move the turret's numbers
 * and the headings in {@code spots.json} move with them. Geometry only: none has been
 * launched from (16 Nov 2026; {@code doc/robot-todo.md}).
 */
public final class AutoLaunchSpots {

    private AutoLaunchSpots() {
    }

    /** A spot, drawn for red: its team spot of the same name, whose heading aims the turret from there. */
    public enum Spot {
        LEFT_AUTO_HIVE(true, TeamSpots.LEFT_AUTO_HIVE),
        LEFT_AUTO_OFFSET(false, TeamSpots.LEFT_AUTO_OFFSET),
        RIGHT_AUTO_HIVE(true, TeamSpots.RIGHT_AUTO_HIVE),
        RIGHT_AUTO_OFFSET(false, TeamSpots.RIGHT_AUTO_OFFSET),
        /** Square, in line with the RIGHT CELL, its back 18 in from the wall: see the class comment. */
        RIGHT_AUTO_FIX_TIP(false, TeamSpots.RIGHT_AUTO_FIX_TIP);

        /** As drawn, for red. */
        public final Pose red;
        /** The HIVE spot of a pair (else its OFFSET one, or the fix-TIP spot, which pairs with nothing). */
        public final boolean hive;
        /** Serves our LEFT CELL (else the RIGHT one): which half of the axle line it is on. */
        public final boolean left;

        Spot(boolean hive, TeamSpot spot) {
            this.red = spot.pose();
            this.hive = hive;
            this.left = LaunchSpots.leftHalf(Alliance.RED, spot.y);
        }

        /** The spot for {@code alliance}: blue's is red's turned half a turn about the field centre. */
        public Pose pose(Alliance alliance) {
            return alliance == Alliance.BLUE ? LaunchSpots.turned(red) : red;
        }

        /** The raised row of the CELL this spot serves, for {@code alliance}; null with no alliance. */
        public Vec3 cellRow(Alliance alliance) {
            HiveCell cell = left ? HiveCell.leftCell(alliance) : HiveCell.rightCell(alliance);
            return cell == null ? null : HiveFieldPoints.rowCentre(cell, HiveCellState.UP);
        }
    }
}
