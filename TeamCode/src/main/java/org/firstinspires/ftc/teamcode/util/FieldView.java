package org.firstinspires.ftc.teamcode.util;

import com.bylazar.field.FieldManager;
import com.bylazar.field.FieldPresets;
import com.bylazar.field.PanelsField;

/**
 * Draws the robot on the Panels Field view, in Pedro coordinates.
 *
 * <p>Open it at {@code http://192.168.43.1:8001} and pick the Field plugin.
 *
 * <h2>Why this exists rather than calling Pedro's drawing helper</h2>
 *
 * <p>Pedro's {@code Drawing} class, and the {@code Follower.telemetryDebug()} that uses it, are
 * <b>not in any artifact this project resolves</b>. They ship in {@code com.pedropathing:telemetry},
 * which is on the deliberately-excluded list — it is still published at {@code 1.0.0} and was never
 * updated for Pedro 3. Verified 25 Sep 2026 by listing the classes in {@code core-3.0.1},
 * {@code revhub-3.0.1}, {@code tuning-1.0.1} and both Ivy jars: no {@code Drawing}, no Panels
 * reference anywhere in the set.
 *
 * <p>That turns out not to matter, because Panels already knows about Pedro's coordinate frame:
 * {@code FieldPresets.PEDRO_PATHING} is one of its four built-in presets, alongside the default FTC
 * and Road Runner frames. So the conversion Pedro's helper would have done is done by the library
 * we already have, and we draw the two shapes ourselves.
 *
 * <h2>The throttle, which will bite you if you ignore it</h2>
 *
 * <p>{@code FieldManager.update()} only sends when at least {@code canvasUpdateInterval}
 * milliseconds have passed — 100 ms by default, so 10 Hz. When it is not time yet, it returns
 * having done <b>nothing at all</b>, and that includes not clearing the canvas. Shapes added since
 * the last send stay in the list. So a 200 Hz loop that draws unconditionally piles twenty copies
 * of the robot into one canvas and ships them together, which costs bandwidth and looks like a
 * smear on the field.
 *
 * <p>{@link #shouldDraw()} is the guard. The three-line shape to copy:
 *
 * <pre>{@code
 * if (fieldView.shouldDraw()) {
 *     fieldView.drawRobot(pose.x(), pose.y(), pose.heading());
 *     fieldView.send();
 * }
 * }</pre>
 *
 * <p>Drawing is skipped entirely on most loops, which is also why field view costs almost nothing
 * in {@code LoopTimeBaseline}'s numbers — the expensive loop is one in twenty.
 */
public final class FieldView {

    /**
     * Half the robot's footprint, in inches.
     *
     * <p>9 in is half of the 18 in starting-size limit, so the circle drawn is the largest the
     * robot could legally be. It is a sighting aid, not a collision model — Panels'
     * {@code Rectangle} has no rotation parameter, so an oriented chassis outline is not available
     * and a circle plus a heading line is the honest thing to draw instead.
     */
    public static final double ROBOT_RADIUS_INCHES = 9.0;

    private final FieldManager field;
    private final double robotRadius;

    private FieldView(FieldManager field, double robotRadius) {
        this.field = field;
        this.robotRadius = robotRadius;
    }

    /** A view on the shared Panels field, set to Pedro's coordinate frame. */
    public static FieldView pedroCoordinates() {
        return pedroCoordinates(ROBOT_RADIUS_INCHES);
    }

    public static FieldView pedroCoordinates(double robotRadiusInches) {
        FieldManager field = PanelsField.INSTANCE.getField();
        field.setOffsets(FieldPresets.INSTANCE.getPEDRO_PATHING());
        return new FieldView(field, robotRadiusInches);
    }

    /**
     * Whether the next {@link #send()} would actually transmit.
     *
     * <p>Gate all drawing on this. See the class comment for what happens if you do not.
     */
    public boolean shouldDraw() {
        return field.getShouldUpdateCanvas();
    }

    /**
     * Draws the robot as a circle with a line from its centre out along its heading.
     *
     * @param x       field X in inches, Pedro's frame
     * @param y       field Y in inches, Pedro's frame
     * @param heading field heading in radians, Pedro's frame
     */
    public void drawRobot(double x, double y, double heading) {
        field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), PanelsField.INSTANCE.getBLUE(), 1.0);
        field.moveCursor(x, y);
        field.circle(robotRadius);

        field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), PanelsField.INSTANCE.getWHITE(), 1.0);
        field.moveCursor(x, y);
        field.line(x + robotRadius * Math.cos(heading), y + robotRadius * Math.sin(heading));
    }

    /** Marks a point — a path target, a detected game element, wherever you were aiming. */
    public void drawMarker(double x, double y, double radius, String outlineColour) {
        field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), outlineColour, 1.0);
        field.moveCursor(x, y);
        field.circle(radius);
    }

    /** Sends the canvas and clears it, if the throttle allows. */
    public void send() {
        field.update();
    }

    /**
     * The underlying Panels field, for drawing this class does not wrap.
     *
     * <p>Anything drawn through here is subject to the same throttle: check {@link #shouldDraw()}
     * first, and let {@link #send()} do the transmitting.
     */
    public FieldManager manager() {
        return field;
    }
}
