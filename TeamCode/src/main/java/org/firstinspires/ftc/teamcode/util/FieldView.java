package org.firstinspires.ftc.teamcode.util;

import com.bylazar.field.CanvasRotation;
import com.bylazar.field.FieldManager;
import com.bylazar.field.FieldPresetParams;
import com.bylazar.field.PanelsField;

/**
 * Draws the robot on the Panels Field view, in Pedro coordinates.
 *
 * <p>Open it at {@code http://192.168.43.1:8001} and pick the Field plugin.
 *
 * <p>Every OpMode on {@code RobotOpMode} already draws the robot through this, once per throttle
 * interval, after its subsystems update. Create your own only to draw more than the robot (a
 * second pose, a target, a path), and draw it in {@code onLoop()} with the gated shape below.
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
 * <p>We use a copy of that preset rather than the preset itself, because the built-in one centres
 * on (72, 72) — a 144 in field — and ours is {@link FieldFrame#FIELD_SIZE_INCHES}. Same rotation
 * and flip; only the centre differs, by 1.25 in. Without this the drawn robot sits that far off
 * where the robot is, which is exactly the kind of small frame disagreement nobody spots.
 *
 * <h2>The throttle, and why every draw is gated on it</h2>
 *
 * <p>{@code FieldManager.update()} only sends when at least {@code canvasUpdateInterval}
 * milliseconds have passed — 100 ms by default, so 10 Hz. Whether or not it sends, it then clears
 * the canvas: {@code update()} is "send if it is time, then reset". So a loop that draws every
 * time and calls {@code update()} every time still shows one robot, the one from whichever loop
 * the send landed on. What it wastes is the drawing: at 200 Hz, nineteen canvases in twenty are
 * built, cleared and never seen.
 *
 * <p>That is the reason for the gate. CLAUDE.md's loop-time rule is that nothing is done per loop
 * that does not need doing, and building a canvas that will not transmit is exactly that. The
 * other reason the gate matters is that it is how the base class and an OpMode's own drawing share
 * one canvas: see {@code RobotOpMode#drawField()}.
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
 * <p>Drawing is skipped entirely on most loops, which is why field view costs almost nothing in
 * {@code LoopTimeBaseline}'s numbers — the expensive loop is one in twenty.
 *
 * <p><b>History note, 25 Oct 2026.</b> This section used to say that a skipped {@code update()}
 * "does nothing at all, and that includes not clearing the canvas", so an ungated draw piled
 * twenty robots into one canvas and shipped a smear. The library does not do that. Checked by
 * disassembling {@code com.bylazar:field:1.0.10}, the module {@code fullpanels:1.0.19} pulls in
 * ({@code javap -p -c com/bylazar/field/FieldManager.class}): {@code update()} sends, stamps
 * {@code lastUpdate} and copies the canvas into {@code lastCanvas} only when
 * {@code getShouldUpdateCanvas()} is true, and then unconditionally calls {@code canvas.reset()}
 * and zeroes the cursor. The Sloth fork {@code 0.3.2+1.0.7} the old text was written against
 * disassembles the same way. The gate was right; the stated reason was not.
 *
 * <h2>The background is already the right one</h2>
 *
 * <p>Panels ships a BIOBUZZ field image and, as of {@code field 0.3.2+1.0.7}, uses its dark variant
 * as the default background. Nothing needs setting. ({@code FieldImages} also still carries last
 * season's DECODE field, if a rig ever wants it.)
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

    /**
     * Pedro's frame as Panels needs it: shift the field centre to Panels' origin, rotate 90° and
     * flip Y.
     *
     * <p>The rotation and flip are copied from Panels' built-in {@code PEDRO_PATHING} preset
     * ({@code field} 1.0.7); the offset is ours, from {@link FieldFrame#FIELD_CENTRE_INCHES}, where
     * the built-in uses 72. Poses go in as Pedro reports them; nothing outside this class converts.
     */
    private static final FieldPresetParams PEDRO_FRAME = new FieldPresetParams(
            "Pedro Pathing",
            -FieldFrame.FIELD_CENTRE_INCHES,
            -FieldFrame.FIELD_CENTRE_INCHES,
            CanvasRotation.DEG_90,
            false,
            true,
            false);

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
        field.setOffsets(PEDRO_FRAME);
        return new FieldView(field, robotRadiusInches);
    }

    /**
     * Whether the next {@link #send()} would actually transmit.
     *
     * <p>Gate all drawing on this, so the loops that will not transmit do no drawing work. See the
     * class comment.
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
        drawRobot(x, y, heading, PanelsField.INSTANCE.getBLUE());
    }

    /**
     * {@link #drawRobot(double, double, double)} in another colour, for a second opinion of where
     * the robot is (the camera's, say) drawn over the first.
     *
     * @param outlineColour a CSS colour, e.g. {@code "#FFB300"}
     */
    public void drawRobot(double x, double y, double heading, String outlineColour) {
        field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), outlineColour, 1.0);
        field.moveCursor(x, y);
        field.circle(robotRadius);

        field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), PanelsField.INSTANCE.getWHITE(), 1.0);
        field.moveCursor(x, y);
        field.line(x + robotRadius * Math.cos(heading), y + robotRadius * Math.sin(heading));
    }

    /**
     * A fixed cross at field centre, for telling "the field view is broken" from "the pose is
     * wrong".
     *
     * <p>Those two look identical when the pose is stuck at the origin, which is exactly what
     * happens with no odometry pod fitted. If this cross is on screen, the canvas, the background
     * and the coordinate frame are all working and the problem is upstream in the pose.
     */
    public void drawCentreReference() {
        field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), PanelsField.INSTANCE.getWHITE(), 0.5);
        field.moveCursor(FieldFrame.FIELD_CENTRE_INCHES - 4.0, FieldFrame.FIELD_CENTRE_INCHES);
        field.line(FieldFrame.FIELD_CENTRE_INCHES + 4.0, FieldFrame.FIELD_CENTRE_INCHES);
        field.moveCursor(FieldFrame.FIELD_CENTRE_INCHES, FieldFrame.FIELD_CENTRE_INCHES - 4.0);
        field.line(FieldFrame.FIELD_CENTRE_INCHES, FieldFrame.FIELD_CENTRE_INCHES + 4.0);
    }

    /** A line between two field points, for a bearing, a ray or a path segment. */
    public void drawLine(double x1, double y1, double x2, double y2, String colour) {
        field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), colour, 1.0);
        field.moveCursor(x1, y1);
        field.line(x2, y2);
    }

    /** Marks a point — a path target, a detected game element, wherever you were aiming. */
    public void drawMarker(double x, double y, double radius, String outlineColour) {
        field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), outlineColour, 1.0);
        field.moveCursor(x, y);
        field.circle(radius);
    }

    /** Sends the canvas if the throttle allows, then clears it either way. */
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
