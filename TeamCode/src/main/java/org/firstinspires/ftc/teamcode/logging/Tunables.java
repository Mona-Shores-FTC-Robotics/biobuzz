package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.localization.LocalizationTuning;
import org.firstinspires.ftc.teamcode.opmodes.FlowerPicker;
import org.firstinspires.ftc.teamcode.opmodes.GardenGather;
import org.firstinspires.ftc.teamcode.opmodes.HiveAndPose;
import org.firstinspires.ftc.teamcode.opmodes.TeleOpConfig;
import org.firstinspires.ftc.teamcode.opmodes.auto.LaunchCards;
import org.firstinspires.ftc.teamcode.opmodes.auto.PathEndSettle;
import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeedGateSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.PrefeedSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FlowerStickSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IndicatorSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.FieldLayers;
import org.firstinspires.ftc.teamcode.vision.CameraMount;
import org.firstinspires.ftc.teamcode.vision.PieceVisionSubsystem;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Every Panels tunable a match OpMode runs with, written to the match log at PLAY, so a log can
 * be reproduced: the feed times, intake powers, turret constants and the rest are
 * {@code @Configurable} and change from a browser, and a run whose numbers are not in its own log
 * cannot be repeated or compared.
 *
 * <p>{@link #CLASSES} is the list, kept in step with the {@code @Configurable} annotations by
 * {@code TunablesTest}: a class annotated in {@code subsystems}, {@code localization},
 * {@code vision}, {@code opmodes} or {@code opmodes/auto} must be here, and nothing else may be.
 * The benches in {@code opmodes/calibration}, the {@code launcher2} rig and the
 * {@code DriveMotorCheck} diagnostic are not here: a bench's knobs are not a match's.
 *
 * <p>Reflection, once per run, at PLAY: a few dozen fields, well under a millisecond, on the loop
 * thread but not in the loop.
 */
public final class Tunables {

    public static final Class<?>[] CLASSES = {
            DriveSubsystem.class,
            FeederSubsystem.class,
            FeedGateSubsystem.class,
            PrefeedSubsystem.class,
            FlowerStickSubsystem.class,
            IndicatorSubsystem.class,
            IntakeSubsystem.class,
            LauncherSubsystem.class,
            TurretSubsystem.class,
            LaunchCards.class,
            PathEndSettle.class,
            LocalizationTuning.class,
            CameraMount.class,
            PieceVisionSubsystem.class,
            TeleOpConfig.class,
            FieldLayers.class,
            FlowerPicker.class,
            GardenGather.class,
            HiveAndPose.class,
    };

    /** Metadata key prefix: {@code Tunable/IntakeSubsystem.inPower}. */
    public static final String PREFIX = "Tunable/";

    private Tunables() {
    }

    /** Every tunable's current value into {@code log} as metadata. Never throws. */
    public static void snapshot(MatchLog log) {
        for (Class<?> cls : CLASSES) {
            for (Field field : cls.getDeclaredFields()) {
                if (!isTunable(field)) continue;
                String value;
                try {
                    value = String.valueOf(field.get(null));
                } catch (RuntimeException | IllegalAccessException e) {
                    value = "(unreadable: " + e + ")";
                }
                log.metadata(PREFIX + cls.getSimpleName() + "." + field.getName(), value);
            }
        }
    }

    /**
     * What Panels edits: a public static, non-final field of a primitive, String or enum type.
     * Anything else on a {@code @Configurable} class (a constant, an object) is not a knob.
     */
    static boolean isTunable(Field field) {
        int mods = field.getModifiers();
        if (!Modifier.isPublic(mods) || !Modifier.isStatic(mods) || Modifier.isFinal(mods)) return false;
        Class<?> type = field.getType();
        return type.isPrimitive() || type == String.class || type.isEnum();
    }
}
