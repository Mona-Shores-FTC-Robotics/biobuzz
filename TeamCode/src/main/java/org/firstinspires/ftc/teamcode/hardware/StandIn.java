package org.firstinspires.ftc.teamcode.hardware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * A do-nothing device that fills the slot of one that is missing, so the rest of the robot keeps
 * running.
 *
 * <p>This is how a robot with an unplugged drive motor still drives on three wheels instead of
 * sitting out the match. {@link HardwareCheck#prepare} registers one of these in the
 * {@code HardwareMap} under the missing device's name; every later
 * {@code hardwareMap.get(DcMotorEx.class, name)} — ours, or a library's such as Pedro's
 * {@code Mecanum} — then gets the stand-in instead of throwing. The missing device is reported by
 * name in telemetry, so it is never silent.
 *
 * <p><b>Only interfaces can be stood in for.</b> A {@link Proxy} implements interfaces, not
 * classes, so {@code DcMotorEx} and {@code Servo} work and the goBILDA Pinpoint (a concrete driver
 * class) does not. Code that needs such a device must check
 * {@link HardwareCheck#isMissing(String)} and choose its own fallback.
 *
 * <p>Behaviour of every method on a stand-in:
 * <ul>
 *   <li>{@code setX(value)} is remembered, and a later {@code getX()} returns it. Pedro sets a
 *       motor's direction and reads it back, and that should agree with itself.</li>
 *   <li>Anything else returns zero, {@code false}, the first constant of an enum, or {@code null}.
 *       A missing motor reports no current, no position and no power.</li>
 * </ul>
 *
 * <p>Deliberately free of Android types, so it is unit tested on the JVM.
 */
public final class StandIn {

    private StandIn() {}

    /** Creates a stand-in implementing {@code type}, labelled with the missing device's name. */
    public static <T> T create(Class<T> type, String deviceName) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException(type.getName() + " is a class, not an interface; "
                    + "a stand-in can only implement interfaces.");
        }
        Object proxy = Proxy.newProxyInstance(
                StandIn.class.getClassLoader(), new Class<?>[] {type}, new Handler(deviceName));
        return type.cast(proxy);
    }

    /** True if {@code device} is a stand-in rather than real hardware. */
    public static boolean is(Object device) {
        return device != null
                && Proxy.isProxyClass(device.getClass())
                && Proxy.getInvocationHandler(device) instanceof Handler;
    }

    private static final class Handler implements InvocationHandler {
        private final String deviceName;
        private final Map<String, Object> remembered = new HashMap<>();

        Handler(String deviceName) {
            this.deviceName = deviceName;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            int argCount = args == null ? 0 : args.length;

            // Object's methods: behave like an ordinary, identity-compared object.
            if (name.equals("equals") && argCount == 1) {
                return proxy == args[0];
            }
            if (name.equals("hashCode") && argCount == 0) {
                return System.identityHashCode(proxy);
            }
            if (name.equals("toString") && argCount == 0) {
                return "StandIn(" + deviceName + ")";
            }

            // HardwareDevice's descriptive methods: say plainly what this is.
            if (name.equals("getDeviceName")) {
                return "MISSING: " + deviceName;
            }
            if (name.equals("getConnectionInfo")) {
                return "not connected (stand-in)";
            }

            if (name.startsWith("set") && argCount == 1) {
                remembered.put(name.substring(3), args[0]);
            } else if (name.startsWith("get") && argCount == 0
                    && remembered.containsKey(name.substring(3))) {
                return remembered.get(name.substring(3));
            }
            return defaultFor(method.getReturnType());
        }

        private static Object defaultFor(Class<?> type) {
            if (type == void.class) return null;
            if (type == boolean.class) return false;
            if (type == int.class) return 0;
            if (type == double.class) return 0.0;
            if (type == float.class) return 0f;
            if (type == long.class) return 0L;
            if (type == short.class) return (short) 0;
            if (type == byte.class) return (byte) 0;
            if (type == char.class) return (char) 0;
            if (type == String.class) return "";
            if (type.isEnum()) {
                Object[] constants = type.getEnumConstants();
                return constants.length > 0 ? constants[0] : null;
            }
            return null;
        }
    }
}
