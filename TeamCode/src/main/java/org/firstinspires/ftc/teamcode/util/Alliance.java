package org.firstinspires.ftc.teamcode.util;

/** Which alliance the robot is playing for. */
public enum Alliance {
    BLUE,
    RED,
    /** No alliance selected yet, or a detection that belongs to neither. */
    UNKNOWN;

    /** The other alliance: the opponents' (UNKNOWN stays UNKNOWN). */
    public Alliance other() {
        return this == RED ? BLUE : this == BLUE ? RED : UNKNOWN;
    }
}
