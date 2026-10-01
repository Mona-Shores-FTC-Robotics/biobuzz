package org.firstinspires.ftc.teamcode.logging;

import java.util.Locale;

/**
 * What happens to a POLLEN or a NECTAR inside a flywheel launcher, so launcher designs can be
 * compared before anyone builds one. It answers: at this wheel speed, how fast, at what angle and
 * with how much spin does each piece leave, does it slip, jam or get crushed, and how much does the
 * wheel slow down.
 *
 * <p><b>The physics.</b> The ball is squeezed between a driven wheel and either a hood (a single
 * wheel launcher) or a second driven wheel. The squeeze is set by the gap and shared between the
 * ball's own stiffness, the wheel tread's and, on a spring-loaded hood or wheel, the spring's. That
 * normal force {@code N} decides how hard friction can push: the wheel side drives the ball at up
 * to {@code μ N}, the hood side drags it at up to {@code μ_hood N}. The ball is a thin shell
 * ({@code I = ⅔ m r²}), so its speed and spin are integrated together until it reaches the end of
 * the contact arc. Squeezing a ball also costs rolling resistance, which grows with how far it is
 * squeezed. The wheel loses what the ball gains and its motor pulls it back up along a straight
 * torque-speed line. On a single wheel against a grippy hood the ball ends up rolling at half the
 * wheel's surface speed; against a slippery hood, nearer 0.4 of it; between two wheels, at the
 * wheels' speed. That matches the rules of thumb FRC and FTC teams quote.
 *
 * <p><b>What is measured and what is guessed.</b> The pieces' sizes and masses are AndyMark's. The
 * stiffness of the pieces, the friction and the rolling resistance are placeholders, marked as
 * such. A launcher's real behaviour hangs on them, and they are quick to measure; see
 * TeamCode/README.md, "Choosing a launcher".
 */
final class LauncherModel {

    enum Kind {
        /** One driven wheel; the ball rolls along a fixed hood. */
        HOOD,
        /** One driven wheel; the hood is spring-loaded off a hard stop, so a bigger ball pushes it open. */
        HOOD_SPRING,
        /** Two driven wheels facing each other at a fixed gap. */
        DOUBLE,
        /** Two driven wheels; one is spring-loaded off a hard stop. */
        DOUBLE_SPRING
    }

    /** A game piece as the launcher sees it. */
    static final class Ball {
        final String name;
        final double radiusM;
        final double massKg;
        /** How hard it is to squeeze, N/m. */
        double stiffness;

        Ball(String name, double diameterIn, double massKg, double stiffness) {
            this.name = name;
            this.radiusM = diameterIn * 0.0254 / 2;
            this.massKg = massKg;
            this.stiffness = stiffness;
        }

        Ball scaled(double sizeFactor) {
            Ball b = new Ball(name, 2 * radiusM / 0.0254 * sizeFactor, massKg, stiffness);
            return b;
        }
    }

    /**
     * Placeholder: a pickleball must take under 43 lbf to squeeze by 0.25 in (USA Pickleball, ASTM
     * F1888). These are guessed at 25 lbf for POLLEN; NECTAR's bigger shell is guessed a little
     * softer. Measure both: a bathroom scale and a ruler will do.
     */
    static final double PLACEHOLDER_POLLEN_STIFFNESS = 25 * 4.448 / 0.00635;
    static final double PLACEHOLDER_NECTAR_STIFFNESS = 0.8 * PLACEHOLDER_POLLEN_STIFFNESS;

    static Ball pollen() {
        return new Ball("POLLEN", 2.80, FieldSim.POLLEN_MASS_KG, PLACEHOLDER_POLLEN_STIFFNESS);
    }

    static Ball nectar() {
        return new Ball("NECTAR", 3.62, FieldSim.NECTAR_MASS_KG, PLACEHOLDER_NECTAR_STIFFNESS);
    }

    /** goBILDA 5203 Yellow Jacket, 1:1: 6000 rpm free, 1.47 kg·cm stall (goBILDA's figures). */
    static final double MOTOR_FREE_RPM = 6000;
    static final double MOTOR_STALL_NM = 1.47 * 0.0981;

    // ---- The design ------------------------------------------------------------------------------

    Kind kind = Kind.HOOD;
    double wheelDiameterIn = 3.78; // goBILDA 96 mm
    /** Wheel tread stiffness, N/m: a hard rubber wheel ~50 000, a soft compliant wheel ~8 000. */
    double wheelStiffness = 50_000;
    /** Rubber on the pieces' plastic. Placeholder. */
    double wheelFriction = 0.9;
    /** Polycarbonate hood on the plastic; grip tape on the hood raises it. Placeholder. */
    double hoodFriction = 0.25;
    /** Wheel surface to hood (or to the other wheel) at the hard stop, inches. */
    double gapIn = 2.5;
    /** A spring-loaded hood or wheel: preload against the hard stop, N, and rate, N/m. */
    double springPreloadN = 10;
    double springRate = 2_000;
    double springTravelIn = 1.2;
    /** How far the ball travels in contact, inches (a quarter of the way round a 96 mm wheel ≈ 3). */
    double contactIn = 3.5;
    /** Launch angle the hood or wheels set, degrees above horizontal. */
    double exitAngleDeg = 60;
    /** Everything spinning with the wheel, kg·m²: the wheel itself plus any added flywheel. */
    double inertia = 1.5e-4;
    /** Motor-to-wheel ratio: wheel rpm = motor rpm / gear. */
    double gear = 1.0;
    /** Motors driving each wheel. */
    int motors = 1;
    /** On a double: the second wheel's surface speed as a fraction of the first (spin). */
    double secondWheelSpeed = 1.0;
    /**
     * Placeholder: rolling resistance per unit of squeeze, as a fraction of {@code N} per fraction
     * of the diameter squeezed. FRC teams find more compression gives slower shots.
     */
    double rollingLoss = 1.5;
    /** A piece squeezed more than this fraction of its diameter is taken to jam or crack. */
    static final double MAX_SQUEEZE = 0.18;

    LauncherModel copy() {
        LauncherModel m = new LauncherModel();
        m.kind = kind;
        m.wheelDiameterIn = wheelDiameterIn;
        m.wheelStiffness = wheelStiffness;
        m.wheelFriction = wheelFriction;
        m.hoodFriction = hoodFriction;
        m.gapIn = gapIn;
        m.springPreloadN = springPreloadN;
        m.springRate = springRate;
        m.springTravelIn = springTravelIn;
        m.contactIn = contactIn;
        m.exitAngleDeg = exitAngleDeg;
        m.inertia = inertia;
        m.gear = gear;
        m.motors = motors;
        m.secondWheelSpeed = secondWheelSpeed;
        m.rollingLoss = rollingLoss;
        return m;
    }

    boolean isDouble() {
        return kind == Kind.DOUBLE || kind == Kind.DOUBLE_SPRING;
    }

    boolean isSprung() {
        return kind == Kind.HOOD_SPRING || kind == Kind.DOUBLE_SPRING;
    }

    double wheelRadiusM() {
        return wheelDiameterIn * 0.0254 / 2;
    }

    /** The wheel's top speed, rad/s. */
    double maxWheelRadPerS() {
        return MOTOR_FREE_RPM / gear * 2 * Math.PI / 60;
    }

    // ---- One shot --------------------------------------------------------------------------------

    /** What leaves the launcher. */
    static final class Shot {
        double exitSpeed;
        /** Backspin positive, rad/s. */
        double spin;
        /** r·ω/v. */
        double spinNumber;
        double normalForce;
        /** How far the ball is squeezed, as a fraction of its diameter. */
        double squeeze;
        boolean touches;
        boolean jammed;
        boolean crushed;
        /** Wheel speed after the shot, rad/s. */
        double wheelAfter;
        double secondWheelAfter;

        boolean ok() {
            return touches && !jammed && !crushed;
        }

        @Override
        public String toString() {
            if (!touches) return "does not touch the wheel";
            if (jammed) return "jams";
            return String.format(Locale.ROOT, "%.2f m/s, spin number %+.2f, squeezed %.0f%% (N %.0f N)%s",
                    exitSpeed, spinNumber, squeeze * 100, normalForce, crushed ? ", CRUSHED" : "");
        }
    }

    /** The force squeezing {@code ball}, N, and how far the ball itself is squeezed, m. */
    double[] squeeze(Ball ball) {
        double interference = 2 * ball.radiusM - gapIn * 0.0254;
        if (interference <= 0) return new double[] {0, 0};
        double wheels = isDouble() ? 2 : 1;
        double kSeries = 1 / (1 / ball.stiffness + wheels / wheelStiffness);
        double n = kSeries * interference;
        if (isSprung() && n > springPreloadN) {
            double lift = (kSeries * interference - springPreloadN) / (kSeries + springRate);
            double travel = springTravelIn * 0.0254;
            if (lift > travel) {
                n = kSeries * (interference - travel);
            } else {
                n = springPreloadN + springRate * lift;
            }
        }
        return new double[] {n, n / ball.stiffness};
    }

    /**
     * Fires {@code ball} with the wheel at {@code wheelRadPerS} (and a double's second wheel at
     * {@code secondRadPerS}), integrating the contact.
     */
    Shot fire(Ball ball, double wheelRadPerS, double secondRadPerS) {
        Shot shot = new Shot();
        double[] sq = squeeze(ball);
        double n = sq[0];
        shot.normalForce = n;
        shot.squeeze = sq[1] / (2 * ball.radiusM);
        shot.touches = n > 0.5;
        shot.crushed = shot.squeeze > MAX_SQUEEZE;
        shot.wheelAfter = wheelRadPerS;
        shot.secondWheelAfter = secondRadPerS;
        if (!shot.touches || shot.crushed) return shot;

        double r = ball.radiusM, m = ball.massKg, i = 2.0 / 3 * m * r * r;
        double rw = wheelRadiusM();
        double mu1 = wheelFriction, mu2 = isDouble() ? wheelFriction : hoodFriction;
        double rr = rollingLoss * shot.squeeze * n;
        double stallNm = MOTOR_STALL_NM * motors / 1.0 * gear, freeRad = maxWheelRadPerS();
        double v = 0.5, w = 0, x = 0, om1 = wheelRadPerS, om2 = secondRadPerS;
        double length = contactIn * 0.0254, dt = 4e-6, t = 0;
        double eps = 0.05;
        while (x < length) {
            double u1 = v + w * r; // the ball's surface against the driven wheel
            double u2 = v - w * r; // against the hood or the second wheel
            double s1 = om1 * rw - u1;
            double s2 = (isDouble() ? om2 * rw : 0) - u2;
            double f1 = mu1 * n * Math.max(-1, Math.min(1, s1 / eps));
            double f2 = mu2 * n * Math.max(-1, Math.min(1, s2 / eps));
            double resist = v > 0 ? rr : 0;
            double a = (f1 + f2 - resist) / m;
            double alpha = (f1 - f2) * r / i;
            v += a * dt;
            w += alpha * dt;
            x += Math.max(v, 0) * dt;
            // The wheels: the reaction, and the motor pulling back toward free speed.
            double motor1 = stallNm * Math.max(0, 1 - om1 / freeRad);
            om1 += (motor1 - f1 * rw) / inertia * dt;
            if (isDouble()) {
                double motor2 = stallNm * Math.max(0, 1 - om2 / freeRad);
                om2 += (motor2 - f2 * rw) / inertia * dt;
            }
            t += dt;
            if (t > 0.25) {
                shot.jammed = true;
                return shot;
            }
        }
        shot.exitSpeed = v;
        shot.spin = w;
        shot.spinNumber = v > 1e-6 ? w * r / v : 0;
        shot.wheelAfter = om1;
        shot.secondWheelAfter = om2;
        return shot;
    }

    /** The wheel speed after {@code seconds} of the motor pulling it back up toward {@code target}. */
    double recover(double from, double target, double seconds) {
        double stallNm = MOTOR_STALL_NM * motors * gear, freeRad = maxWheelRadPerS();
        double om = from, dt = 1e-4;
        for (double t = 0; t < seconds && om < target; t += dt) {
            om += stallNm * Math.max(0, 1 - om / freeRad) / inertia * dt;
        }
        return Math.min(om, Math.max(from, target));
    }

    String describe() {
        StringBuilder sb = new StringBuilder();
        switch (kind) {
            case HOOD: sb.append("single wheel, fixed hood"); break;
            case HOOD_SPRING: sb.append("single wheel, spring-loaded hood"); break;
            case DOUBLE: sb.append("two wheels, fixed gap"); break;
            default: sb.append("two wheels, one spring-loaded"); break;
        }
        sb.append(String.format(Locale.ROOT, "; %.2f in wheels, tread %s (%.0f N/mm)", wheelDiameterIn,
                wheelStiffness < 20_000 ? "soft/compliant" : "firm", wheelStiffness / 1000));
        sb.append(String.format(Locale.ROOT, "; gap %.2f in", gapIn));
        if (isSprung()) {
            sb.append(String.format(Locale.ROOT, " at the stop, spring preload %.0f N (%.1f lbf), rate %.1f N/mm",
                    springPreloadN, springPreloadN / 4.448, springRate / 1000));
        }
        if (!isDouble()) sb.append(String.format(Locale.ROOT, "; hood friction %.2f", hoodFriction));
        sb.append(String.format(Locale.ROOT, "; %.0f° launch; flywheel %.1e kg·m²; %d motor%s, %s",
                exitAngleDeg, inertia, motors, motors > 1 ? "s" : "",
                gear == 1 ? "1:1" : String.format(Locale.ROOT, "gear %.2f", gear)));
        return sb.toString();
    }
}
