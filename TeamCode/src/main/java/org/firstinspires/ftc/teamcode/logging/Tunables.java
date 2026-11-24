package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.localization.LocalizationTuning;
import org.firstinspires.ftc.teamcode.opmodes.FlowerPicker;
import org.firstinspires.ftc.teamcode.opmodes.FlowerVolley;
import org.firstinspires.ftc.teamcode.opmodes.GardenGather;
import org.firstinspires.ftc.teamcode.opmodes.LaunchSpotDrive;
import org.firstinspires.ftc.teamcode.opmodes.HiveAndPose;
import org.firstinspires.ftc.teamcode.opmodes.TeleOpConfig;
import org.firstinspires.ftc.teamcode.opmodes.auto.BuiltAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.LaunchCards;
import org.firstinspires.ftc.teamcode.opmodes.auto.PathEndSettle;
import org.firstinspires.ftc.teamcode.subsystems.BeamBreakSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.CornerWheelSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.ColorSensorSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeedGateSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.PrefeedSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FlowerStickSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IndicatorSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.LaunchSpeedTable;
import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.FieldLayers;
import org.firstinspires.ftc.teamcode.vision.CameraMount;
import org.firstinspires.ftc.teamcode.vision.PieceVisionSubsystem;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Every Panels tunable a match OpMode runs with, written to the match log at PLAY, so a log can
 * be reproduced: the feed times, intake powers, turret constants and the rest are
 * {@code @Configurable} and change from a browser, and a run whose numbers are not in its own log
 * cannot be repeated or compared.
 *
 * <p>{@link #CLASSES} is the list, kept in step with the {@code @Configurable} annotations by
 * {@code TunablesTest}: a class annotated in {@code subsystems}, {@code localization},
 * {@code vision}, {@code opmodes} or {@code opmodes/auto} must be here, and nothing else may be.
 * The benches in {@code opmodes/calibration} and the
 * {@code DriveMotorCheck} and {@code PartnerNudgeTest} diagnostics are not here: a bench's
 * knobs are not a match's. The nudge Utility logs its own captured settings at PLAY.
 *
 * <p>Reflection, once per run, at PLAY: a few dozen fields, well under a millisecond, on the loop
 * thread but not in the loop.
 */
public final class Tunables {

    public static final Class<?>[] CLASSES = {
            DriveSubsystem.class,
            ColorSensorSubsystem.class,
            BeamBreakSubsystem.class,
            CornerWheelSubsystem.class,
            FeederSubsystem.class,
            FeedGateSubsystem.class,
            PrefeedSubsystem.class,
            FlowerStickSubsystem.class,
            IndicatorSubsystem.class,
            IntakeSubsystem.class,
            LauncherSubsystem.class,
            LaunchSpeedTable.class,
            TurretSubsystem.class,
            LaunchCards.class,
            PathEndSettle.class,
            BuiltAuto.class,
            LocalizationTuning.class,
            CameraMount.class,
            PieceVisionSubsystem.class,
            TeleOpConfig.class,
            FieldLayers.class,
            FlowerPicker.class,
            FlowerVolley.class,
            GardenGather.class,
            LaunchSpotDrive.class,
            HiveAndPose.class,
    };

    /** Metadata key prefix: {@code Tunable/IntakeSubsystem.inPower}, {@code Tunable/GardenGather.driveIn.extraDepthIn} for a group's. */
    public static final String PREFIX = "Tunable/";

    private Tunables() {
    }

    /** Every tunable's current value into {@code log} as metadata. Never throws. */
    public static void snapshot(MatchLog log) {
        for (Class<?> cls : CLASSES) {
            for (String[] keyValue : values(cls)) {
                log.metadata(PREFIX + keyValue[0], keyValue[1]);
            }
        }
    }

    /**
     * Every tunable on {@code cls} as {key, value}: its own knobs ({@code GardenGather.useStick}),
     * and the knobs of each of its groups ({@link #isGroup}) under the group's name
     * ({@code GardenGather.driveIn.extraDepthIn}). Never throws: an unreadable value is written as such.
     */
    static List<String[]> values(Class<?> cls) {
        List<String[]> out = new ArrayList<>();
        for (Field field : cls.getDeclaredFields()) {
            if (isTunable(field)) {
                out.add(new String[] {cls.getSimpleName() + "." + field.getName(), read(field, null)});
            } else if (isGroup(cls, field)) {
                Object group;
                try {
                    group = field.get(null);
                } catch (RuntimeException | IllegalAccessException e) {
                    group = null;
                }
                for (Field knob : field.getType().getDeclaredFields()) {
                    if (!isGroupKnob(knob)) continue;
                    out.add(new String[] {cls.getSimpleName() + "." + field.getName() + "." + knob.getName(),
                            group == null ? "(no group)" : read(knob, group)});
                }
            }
        }
        return out;
    }

    private static String read(Field field, Object owner) {
        try {
            return String.valueOf(field.get(owner));
        } catch (RuntimeException | IllegalAccessException e) {
            return "(unreadable: " + e + ")";
        }
    }

    /**
     * What Panels edits: a public static, non-final field of a primitive, String or enum type.
     * Anything else on a {@code @Configurable} class (a constant, an object) is not a knob.
     */
    static boolean isTunable(Field field) {
        int mods = field.getModifiers();
        if (!Modifier.isPublic(mods) || !Modifier.isStatic(mods) || Modifier.isFinal(mods)) return false;
        return isScalar(field.getType());
    }

    /**
     * A group of knobs, which Panels shows as a folder: a public static, non-final field whose type
     * is a class nested in {@code cls} ({@code GardenGather.driveIn}, a {@code GardenGather.DriveIn}).
     * Its knobs are {@link #isGroupKnob}.
     */
    static boolean isGroup(Class<?> cls, Field field) {
        int mods = field.getModifiers();
        if (!Modifier.isPublic(mods) || !Modifier.isStatic(mods) || Modifier.isFinal(mods)) return false;
        return field.getType().getDeclaringClass() == cls;
    }

    /** A knob in a group: a public, non-static, non-final field of a primitive, String or enum type. */
    static boolean isGroupKnob(Field field) {
        int mods = field.getModifiers();
        if (!Modifier.isPublic(mods) || Modifier.isStatic(mods) || Modifier.isFinal(mods)) return false;
        return isScalar(field.getType());
    }

    private static boolean isScalar(Class<?> type) {
        return type.isPrimitive() || type == String.class || type.isEnum();
    }
}
