package org.firstinspires.ftc.teamcode.opmodes;

/**
 * The chassis aim's turn (Circle with the turret LOCKED): proportional to the aim's error, with a
 * floor, so the robot keeps turning onto the CELL instead of stalling just short of it. Pure, so a
 * test drives it.
 *
 * <p>Without a floor it stalled: on 19429 (19 Nov 2026 logs) a turn command of 0.14 or less did not
 * rotate the robot at all, 0.28 always did, and at {@code chassisAimKp} 1.0 the aim asks under 0.14
 * until the CELL is about 8° off, so a LOCKED Circle press sat 3 to 6° off and its 3° aim gate never
 * opened (mentor, 20 Nov 2026: "when I locked the turret I couldn't shoot").
 *
 * <p>Inside {@code stopDeg} it asks for nothing, so the floor cannot rock it back and forth across
 * the CELL; {@code stopDeg} sits inside the aim gate's tolerance so it stops where the gate opens.
 */
final class ChassisAimTurn {

    private ChassisAimTurn() {}

    /**
     * @param errorDeg aim error, degrees, + to turn left (counter-clockwise); NaN gives 0
     * @param kp       turn power per radian of error, negative counts as 0
     * @param minTurn  the least turn that rotates the robot, applied outside {@code stopDeg}
     * @param maxTurn  the most turn it may ask for
     * @param stopDeg  inside this, no turn
     */
    static double power(double errorDeg, double kp, double minTurn, double maxTurn, double stopDeg) {
        if (Double.isNaN(errorDeg)) return 0.0;
        double max = unit(maxTurn);
        double min = Math.min(unit(minTurn), max);
        if (Math.abs(errorDeg) <= Math.max(0.0, Double.isNaN(stopDeg) ? 0.0 : stopDeg)) return 0.0;
        double turn = Math.abs((kp > 0.0 ? kp : 0.0) * Math.toRadians(errorDeg));
        return Math.copySign(Math.min(max, Math.max(min, turn)), errorDeg);
    }

    private static double unit(double v) {
        return v > 0.0 ? Math.min(v, 1.0) : 0.0;
    }
}
