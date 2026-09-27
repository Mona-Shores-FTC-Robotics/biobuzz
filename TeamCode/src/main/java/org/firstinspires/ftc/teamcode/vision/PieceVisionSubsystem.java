package org.firstinspires.ftc.teamcode.vision;

import android.util.Size;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.util.SortOrder;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.subsystems.Subsystem;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.opencv.ColorBlobLocatorProcessor;
import org.firstinspires.ftc.vision.opencv.ColorRange;
import org.firstinspires.ftc.vision.opencv.ColorSpace;
import org.firstinspires.ftc.vision.opencv.ImageRegion;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;

import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.List;

/**
 * Game pieces, by colour, from the webcam — the SDK's {@link ColorBlobLocatorProcessor} running on
 * the Control Hub. The Limelight is kept for AprilTags; see {@code TeamCode/README.md}.
 *
 * <h2>Cheap unless asked</h2>
 *
 * <p>Blob finding costs Control Hub CPU, so it runs only while enabled: an OpMode calls
 * {@link #setEnabled(boolean)} when intaking and turns it off after. Low resolution, no live
 * preview. Measure the loop with it on and off ({@code LoopTimeBaseline}).
 *
 * <h2>Colours are measured, not guessed</h2>
 *
 * <p>Each {@link Piece}'s HSV range starts at -1 — unmeasured — and a piece with no range gets no
 * processor. With no piece measured, the camera is never opened at all. OpenCV HSV: hue 0–180,
 * saturation and value 0–255. Set the ranges in Panels under a real field's lighting, then write
 * them back here.
 *
 * <h2>Fallback</h2>
 *
 * <p>The webcam is optional. Missing, it reports UNAVAILABLE with the reason and everything reading
 * it sees "no piece"; intaking is then manual.
 */
@Configurable
public class PieceVisionSubsystem implements Subsystem {

    /**
     * Not in {@code DeviceNames}, for the same reason as the Limelight: a webcam's config element
     * needs a per-robot serial number and {@code DeviceNames.Kind} has no camera kind. See
     * {@code DeviceNameLiteralTest}.
     */
    public static final String DEVICE_NAME = "webcam";

    public enum Piece { POLLEN, NECTAR }

    public enum State { NOT_CONFIGURED, IDLE, RUNNING, UNAVAILABLE }

    /** POLLEN's HSV range. -1 means not measured yet. */
    public static class Pollen {
        public static double hMin = -1, hMax = -1, sMin = -1, sMax = -1, vMin = -1, vMax = -1;
    }

    /** NECTAR's HSV range. -1 means not measured yet. */
    public static class Nectar {
        public static double hMin = -1, hMax = -1, sMin = -1, sMax = -1, vMin = -1, vMax = -1;
    }

    /** Blobs smaller than this, in pixels at 320×240, are noise. */
    public static double minBlobAreaPx = 50;

    private static final Piece[] PIECES = Piece.values();

    private final ColorBlobLocatorProcessor[] processors = new ColorBlobLocatorProcessor[PIECES.length];
    private final VisionPortal portal;
    private final String unavailableReason;
    private State state;
    private boolean enabled;

    // Latest largest blob per piece: centre as a fraction of the frame (-1 left .. +1 right,
    // -1 top .. +1 bottom), and area. area 0 = not seen.
    private final double[] centreX = new double[PIECES.length];
    private final double[] centreY = new double[PIECES.length];
    private final int[] areaPx = new int[PIECES.length];

    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;

    public PieceVisionSubsystem(HardwareMap hardwareMap) {
        WebcamName webcam = null;
        String reason = null;
        try {
            webcam = hardwareMap.get(WebcamName.class, DEVICE_NAME);
        } catch (RuntimeException e) {
            reason = e.getMessage() == null ? e.toString() : e.getMessage();
        }

        int built = 0;
        if (webcam != null) {
            built += build(Piece.POLLEN, Pollen.hMin, Pollen.hMax, Pollen.sMin, Pollen.sMax, Pollen.vMin, Pollen.vMax);
            built += build(Piece.NECTAR, Nectar.hMin, Nectar.hMax, Nectar.sMin, Nectar.sMax, Nectar.vMin, Nectar.vMax);
        }

        VisionPortal opened = null;
        if (webcam != null && built > 0) {
            try {
                VisionPortal.Builder builder = new VisionPortal.Builder()
                        .setCamera(webcam)
                        .setCameraResolution(new Size(WIDTH, HEIGHT))
                        .enableLiveView(false);
                for (ColorBlobLocatorProcessor processor : processors) {
                    if (processor != null) builder.addProcessor(processor);
                }
                opened = builder.build();
            } catch (RuntimeException e) {
                reason = e.getMessage() == null ? e.toString() : e.getMessage();
            }
        }
        portal = opened;
        unavailableReason = reason;
        state = webcam == null || (built > 0 && opened == null) ? State.UNAVAILABLE
                : built == 0 ? State.NOT_CONFIGURED : State.IDLE;
    }

    private int build(Piece piece, double hMin, double hMax, double sMin, double sMax,
                      double vMin, double vMax) {
        if (hMin < 0 || hMax < 0 || sMin < 0 || sMax < 0 || vMin < 0 || vMax < 0) {
            return 0;
        }
        ColorBlobLocatorProcessor processor = new ColorBlobLocatorProcessor.Builder()
                .setTargetColorRange(new ColorRange(ColorSpace.HSV,
                        new Scalar(hMin, sMin, vMin), new Scalar(hMax, sMax, vMax)))
                .setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)
                .setRoi(ImageRegion.entireFrame())
                .setBlurSize(5)
                .setDrawContours(false)
                .build();
        processor.addFilter(new ColorBlobLocatorProcessor.BlobFilter(
                ColorBlobLocatorProcessor.BlobCriteria.BY_CONTOUR_AREA, minBlobAreaPx, Double.MAX_VALUE));
        processor.setSort(new ColorBlobLocatorProcessor.BlobSort(
                ColorBlobLocatorProcessor.BlobCriteria.BY_CONTOUR_AREA, SortOrder.DESCENDING));
        processors[piece.ordinal()] = processor;
        return 1;
    }

    // -------------------------------------------------------------- commands

    /** Run blob finding (true) or leave the CPU alone (false). Starts off. */
    public void setEnabled(boolean enabled) {
        if (portal == null || this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        for (ColorBlobLocatorProcessor processor : processors) {
            if (processor != null) portal.setProcessorEnabled(processor, enabled);
        }
        state = enabled ? State.RUNNING : State.IDLE;
        if (!enabled) clearSightings();
    }

    // ----------------------------------------------------------------- state

    /** Whether {@code piece} is in view right now. */
    public boolean sees(Piece piece) {
        return areaPx[piece.ordinal()] > 0;
    }

    /** Horizontal position of the largest {@code piece} blob, -1 (left edge) to +1 (right). */
    public double centreX(Piece piece) {
        return centreX[piece.ordinal()];
    }

    /** Vertical position, -1 (top) to +1 (bottom). Lower in the frame usually means closer. */
    public double centreY(Piece piece) {
        return centreY[piece.ordinal()];
    }

    public int areaPx(Piece piece) {
        return areaPx[piece.ordinal()];
    }

    public State state() {
        return state;
    }

    // ------------------------------------------------------------- lifecycle

    @Override
    public void initialize() {
        if (portal != null) {
            for (ColorBlobLocatorProcessor processor : processors) {
                if (processor != null) portal.setProcessorEnabled(processor, false);
            }
        }
    }

    @Override
    public void update() {
        if (!enabled) {
            return;
        }
        for (int i = 0; i < PIECES.length; i++) {
            ColorBlobLocatorProcessor processor = processors[i];
            if (processor == null) continue;
            List<ColorBlobLocatorProcessor.Blob> blobs = processor.getBlobs();
            if (blobs.isEmpty()) {
                areaPx[i] = 0;
                continue;
            }
            ColorBlobLocatorProcessor.Blob largest = blobs.get(0);
            RotatedRect box = largest.getBoxFit();
            centreX[i] = box.center.x / (WIDTH / 2.0) - 1.0;
            centreY[i] = box.center.y / (HEIGHT / 2.0) - 1.0;
            areaPx[i] = largest.getContourArea();
        }
    }

    @Override
    public void stop() {
        try {
            if (portal != null) portal.close();
        } catch (RuntimeException ignored) {
            // Tearing down; a failure to close must not mask why the OpMode ended.
        }
        clearSightings();
    }

    @Override
    public void describe(Display display) {
        switch (state) {
            case UNAVAILABLE:
                display.status("Webcam", Display.Level.WARN, "UNAVAILABLE — " + unavailableReason);
                return;
            case NOT_CONFIGURED:
                display.status("Webcam", Display.Level.WARN, "no piece colours measured — camera off");
                return;
            default:
                display.status("Webcam", Display.Level.OK, state == State.RUNNING
                        ? String.format(java.util.Locale.US, "running, %.0f fps", portal.getFps())
                        : "idle (enable while intaking)");
        }
        for (Piece piece : PIECES) {
            if (processors[piece.ordinal()] == null) {
                display.line(piece + ": colour not measured");
            } else if (sees(piece)) {
                display.line(String.format(java.util.Locale.US, "%s: x %.2f, y %.2f, %d px",
                        piece, centreX(piece), centreY(piece), areaPx(piece)));
            } else {
                display.line(piece + ": none");
            }
        }
    }

    private void clearSightings() {
        for (int i = 0; i < PIECES.length; i++) areaPx[i] = 0;
    }
}
