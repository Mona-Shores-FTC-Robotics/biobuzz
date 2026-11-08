package org.firstinspires.ftc.teamcode.logging;

import java.io.File;

/**
 * Cuts the HIVE field and the HIVE out of AdvantageScope's stock 2026-2027 field, exactly as
 * {@code HiveAssetsTest} does, without Gradle or the Android SDK: build-assets.sh compiles this with
 * HiveAssets, Glb, MiniJson and TeamCodeDir and runs it. Args: the stock field folder, the output folder.
 */
public final class BuildReplayAssets {
    private BuildReplayAssets() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("usage: BuildReplayAssets <stock field folder> <out folder>");
        HiveAssets.build(new File(args[0]), new File(args[1]));
        System.out.println("Wrote " + HiveAssets.FIELD_FOLDER + " and " + HiveAssets.ROBOT_FOLDER + " into " + args[1]);
    }
}
