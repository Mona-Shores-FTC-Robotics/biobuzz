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

    // The robot is drawn as two outlines around its pose, which is the middle of the wheelbase
    // (the point the pods are measured from, and the one a mecanum drivetrain turns about).
    // Each is {back, front, right, left}: inches from that point. Panels' Rectangle cannot
    // rotate, so each is four lines turned to the heading.

    /**
     * The drivetrain, wheel to wheel (CAD, 29 Oct 2026): 15.43 in long, 13.79 in wide, centred on
     * the pose. What lines up on a seam and touches a wall.
     */
    public static final double[] DRIVETRAIN = {7.715, 7.715, 6.894, 6.894};

    /**
     * The robot's 16 × 16 in frame (taped on 20245, 31 Oct 2026): its back flush with the rear
     * wheels, so it reaches 8.28 in ahead of the pose; centred side to side. The 1.5 in at the
     * front middle is left out.
     */
    public static final double[] FRAME = {7.72, 8.28, 8.0, 8.0};

    private static final double[] CORNER_FORWARD_SIGN = {1, -1, -1, 1};
    private static final double[] CORNER_LEFT_SIGN = {1, 1, -1, -1};

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

    private FieldView(FieldManager field) {
        this.field = field;
    }

    /** A view on the shared Panels field, set to Pedro's coordinate frame. */
    public static FieldView pedroCoordinates() {
        FieldManager field = PanelsField.INSTANCE.getField();
        field.setOffsets(PEDRO_FRAME);
        return new FieldView(field);
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
     * Draws the robot: its drivetrain footprint and, around it, its frame ({@link #DRIVETRAIN},
     * {@link #FRAME}), turned to its heading, with a line from its centre to the front of the
     * drivetrain.
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
        double c = Math.cos(heading);
        double s = Math.sin(heading);
        field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), outlineColour, 1.0);
        drawOutline(x, y, c, s, DRIVETRAIN);
        if (FieldLayers.frame) {
            field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), outlineColour, 0.5);
            drawOutline(x, y, c, s, FRAME);
        }

        field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), PanelsField.INSTANCE.getWHITE(), 1.0);
        field.moveCursor(x, y);
        field.line(x + DRIVETRAIN[1] * c, y + DRIVETRAIN[1] * s);
    }

    /** One outline: front left, back left, back right, front right, each edge to the next. */
    private void drawOutline(double x, double y, double c, double s, double[] box) {
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) % 4;
            field.moveCursor(cornerX(x, c, s, box, i), cornerY(y, c, s, box, i));
            field.line(cornerX(x, c, s, box, j), cornerY(y, c, s, box, j));
        }
    }

    /** How far ahead (+) or behind (-) of the pose corner {@code i} of {@code box} is. */
    private static double forward(double[] box, int i) {
        return CORNER_FORWARD_SIGN[i] > 0 ? box[1] : -box[0];
    }

    /** How far left (+) or right (-) of the pose corner {@code i} of {@code box} is. */
    private static double left(double[] box, int i) {
        return CORNER_LEFT_SIGN[i] > 0 ? box[3] : -box[2];
    }

    /** Corner {@code i} (front left, back left, back right, front right) of {@code box}, field x. */
    static double cornerX(double x, double c, double s, double[] box, int i) {
        return x + forward(box, i) * c - left(box, i) * s;
    }

    /** As {@link #cornerX}, field y. */
    static double cornerY(double y, double c, double s, double[] box, int i) {
        return y + forward(box, i) * s + left(box, i) * c;
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
