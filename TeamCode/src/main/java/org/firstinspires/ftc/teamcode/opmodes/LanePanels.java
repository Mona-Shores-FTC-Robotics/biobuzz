package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.telemetry.TelemetryManager;

import org.firstinspires.ftc.teamcode.subsystems.LaneTracker;

/**
 * The lane tracker's guess to Panels, under the same keys in every OpMode that publishes:
 * {@code lane_count}, {@code lane_present}, and {@code slot_0} (the front, next to fire) to
 * {@code slot_3} (the entrance, under the sensor). A slot is a number so Panels can graph it:
 * 0 empty, 1 POLLEN, 2 red NECTAR, 3 blue NECTAR, 4 unknown, the same codes as the match log's
 * {@code /Lane/Slot1..4}. The caller's try/catch covers it, like the rest of its publishing.
 */
public final class LanePanels {

    private static final String[] SLOT_KEYS = {"slot_0", "slot_1", "slot_2", "slot_3"};

    private LanePanels() {}

    public static void publish(TelemetryManager panels, LaneTracker lane) {
        panels.addData("lane_count", lane.count());
        panels.addData("lane_present", lane.present() ? 1.0 : 0.0);
        for (int i = 0; i < SLOT_KEYS.length; i++) {
            panels.addData(SLOT_KEYS[i], code(lane.slot(i)));
        }
    }

    /** 0 empty, 1 POLLEN, 2 red NECTAR, 3 blue NECTAR, 4 unknown. */
    static int code(LaneTracker.Piece piece) {
        return piece == null ? 0 : piece.ordinal() + 1;
    }
}
