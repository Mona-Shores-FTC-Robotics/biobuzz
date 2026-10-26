package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.BuildConfig;

/**
 * Which code is running: the git commit, branch and dirty flag the build stamped into
 * {@code BuildConfig} (see {@code TeamCode/build.gradle}). Every match log carries it as metadata,
 * so a log can be matched to a commit, and a robot installed from the wrong branch, or from a
 * tree with uncommitted edits, says so in its own log.
 */
public final class BuildStamp {

    private BuildStamp() {
    }

    /** The short commit, with {@code -dirty} when the tree had uncommitted changes; {@code unknown} without git. */
    public static String commit() {
        return BuildConfig.GIT_COMMIT + (BuildConfig.GIT_DIRTY ? "-dirty" : "");
    }

    /** The branch the build was made from; {@code HEAD} when detached, {@code unknown} without git. */
    public static String branch() {
        return BuildConfig.GIT_BRANCH;
    }

    /** {@code branch @ commit}, for a screen or a log line. */
    public static String describe() {
        return branch() + " @ " + commit();
    }
}
