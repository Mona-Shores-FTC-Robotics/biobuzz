package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A real {@link HardwareMap} (so libraries such as Pedro accept it) whose name lookups work on a
 * plain JVM.
 *
 * <p>Why it exists: the SDK's {@code HardwareMap.tryGet} ends by calling
 * {@code com.qualcomm.robotcore.util.Device.isRevControlHub()}, and {@code Device}'s static
 * initializer needs a real Android device — on the JVM it fails with
 * {@code NoClassDefFoundError}. That call only decides whether to log an IMU hint, so this
 * reproduces the lookup rule that matters (verified against the SDK 12.0.0 bytecode): devices
 * registered under a name are kept in registration order, and the <b>first</b> one that is an
 * instance of the requested type wins. {@code get(Class, String)} delegates to {@code tryGet}, so
 * overriding {@code tryGet} covers Pedro's {@code hardwareMap.get(DcMotorEx.class, name)} too.
 */
public class JvmHardwareMap extends HardwareMap {

    private final Map<String, List<HardwareDevice>> byName = new LinkedHashMap<>();

    public JvmHardwareMap() {
        super(null, null);
    }

    @Override
    public void put(String deviceName, HardwareDevice device) {
        List<HardwareDevice> devices = byName.get(deviceName.trim());
        if (devices == null) {
            devices = new ArrayList<>();
            byName.put(deviceName.trim(), devices);
        }
        devices.add(device);
    }

    @Override
    public <T> T tryGet(Class<? extends T> classOrInterface, String deviceName) {
        List<HardwareDevice> devices = byName.get(deviceName.trim());
        if (devices == null) {
            return null;
        }
        for (HardwareDevice device : devices) {
            if (classOrInterface.isInstance(device)) {
                return classOrInterface.cast(device);
            }
        }
        return null;
    }

    @Override
    public <T> List<T> getAll(Class<? extends T> classOrInterface) {
        List<T> all = new ArrayList<>();
        for (List<HardwareDevice> devices : byName.values()) {
            for (HardwareDevice device : devices) {
                if (classOrInterface.isInstance(device)) {
                    all.add(classOrInterface.cast(device));
                }
            }
        }
        return all;
    }

    @Override
    public SortedSet<String> getAllNames(Class<? extends HardwareDevice> classOrInterface) {
        SortedSet<String> names = new TreeSet<>();
        for (Map.Entry<String, List<HardwareDevice>> entry : byName.entrySet()) {
            for (HardwareDevice device : entry.getValue()) {
                if (classOrInterface.isInstance(device)) {
                    names.add(entry.getKey());
                }
            }
        }
        return names;
    }
}
