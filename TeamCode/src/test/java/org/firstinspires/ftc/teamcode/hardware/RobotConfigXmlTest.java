package org.firstinspires.ftc.teamcode.hardware;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Proves the bundled robot configuration XML and {@link DeviceNames} cannot
 * drift apart.
 *
 * <p>This is the cheap half of the whole design. Generating one side from the
 * other was considered and rejected: it needs a Gradle codegen task wired into
 * generated source or resource directories, against a build whose Sloth / Load
 * / AGP versions the README documents as delicately locked together. A plain
 * JVM test gets the same guarantee with no build-tooling risk and far better
 * error messages — and it runs in a {@code testDebugUnitTest} CI job that
 * already existed with nothing in it.
 *
 * <p>Every failure here is one a student would otherwise hit as a {@code null}
 * device or a "could not find device" crash, days later, on a robot.
 */
public class RobotConfigXmlTest {

    private static final String ROBOT_ROOT_TAG = "Robot";
    private static final String ROBOT_TYPE_ATTRIBUTE = "FirstInspires-FTC";

    /** Elements that group other devices rather than being devices themselves. */
    private static final Set<String> CONTAINER_TAGS = new HashSet<>(Arrays.asList(
            "Robot", "LynxUsbDevice", "LynxModule"));

    /**
     * The SDK's element for a USB-Ethernet device ({@code BuiltInConfigurationType
     * .ETHERNET_OVER_USB_DEVICE}). {@code ReadXMLFileHandler.parseRobot} only looks
     * for it as a direct child of {@code <Robot>}.
     */
    private static final String ETHERNET_TAG = "EthernetDevice";

    /**
     * What {@code SerialNumber.fromString} needs to see to build an
     * {@code EthernetOverUsbSerialNumber}. {@code Limelight3A} stores its serial
     * as that subclass, so any other prefix is a crash at hardware-map build.
     */
    private static final String ETHERNET_SERIAL_PREFIX = "EthernetOverUsb:";

    // ---------------------------------------------------------------- tests

    @Test
    public void everyRobotIdentityHasExactlyOneConfigFile() {
        Set<String> onDisk = new TreeSet<>();
        for (File file : configFiles()) {
            onDisk.add(stripXmlExtension(file.getName()));
        }

        Set<String> declared = new TreeSet<>();
        for (RobotIdentity identity : RobotIdentity.values()) {
            declared.add(identity.configName);
        }

        Set<String> missingFiles = new TreeSet<>(declared);
        missingFiles.removeAll(onDisk);
        Set<String> orphanFiles = new TreeSet<>(onDisk);
        orphanFiles.removeAll(declared);

        if (!missingFiles.isEmpty() || !orphanFiles.isEmpty()) {
            fail("RobotIdentity and res/xml/robot_*.xml disagree."
                    + "\n  RobotIdentity constants with no XML file: " + missingFiles
                    + "\n  XML files with no RobotIdentity constant: " + orphanFiles
                    + "\n  Adding a robot means both: a res/xml/robot_<name>.xml and a"
                    + " RobotIdentity constant whose configName is <name>.");
        }
    }

    /**
     * Each file is held to its own robot's list, {@link RobotIdentity#devices}.
     * Until 26 Sep 2026 every file was held to one shared list, which a bench rig
     * with a single motor could never satisfy — see {@link DeviceNames}.
     */
    @Test
    public void everyConfigDeclaresEveryDeviceItsRobotUses() {
        for (File file : configFiles()) {
            RobotIdentity identity = identityOf(file);
            Map<String, Element> devices = devicesIn(file);

            for (DeviceNames.Device expected : identity.devices) {
                Element element = devices.get(expected.name);
                if (element == null) {
                    fail(file.getName() + " does not declare \"" + expected.name + "\", which"
                            + " RobotIdentity." + identity + " requires. Add it to this file, or"
                            + " remove it from that robot's list in DeviceNames."
                            + "\n  Declared in this file: " + new TreeSet<>(devices.keySet()));
                }

                DeviceNames.Kind actualKind = kindOf(element);
                if (actualKind != expected.kind) {
                    fail(file.getName() + " declares \"" + expected.name + "\" as <"
                            + element.getTagName() + "> which is a " + actualKind + ", but"
                            + " DeviceNames says it is a " + expected.kind + ".");
                }
            }
        }
    }

    @Test
    public void noConfigDeclaresADeviceItsRobotDoesNotUse() {
        for (File file : configFiles()) {
            RobotIdentity identity = identityOf(file);
            Set<String> known = new HashSet<>();
            for (DeviceNames.Device expected : identity.devices) {
                known.add(expected.name);
            }

            Set<String> unknown = new TreeSet<>(devicesIn(file).keySet());
            unknown.removeAll(known);
            if (!unknown.isEmpty()) {
                fail(file.getName() + " declares device(s) RobotIdentity." + identity
                        + " does not list: " + unknown
                        + ".\n  Either add them to that robot's list in DeviceNames or delete"
                        + " them. Last season shipped configs containing devices nothing used"
                        + " and code referring to devices no config declared, in both"
                        + " directions at once.");
            }
        }
    }

    @Test
    public void everyConfigIsAValidFtcRobotConfiguration() {
        for (File file : configFiles()) {
            Element root = parse(file).getDocumentElement();

            assertTrue(file.getName() + " root element must be <" + ROBOT_ROOT_TAG + ">, found <"
                            + root.getTagName() + ">. The SDK only recognises a bundled resource"
                            + " as a robot configuration if the root matches.",
                    ROBOT_ROOT_TAG.equals(root.getTagName()));

            assertTrue(file.getName() + " root must be type=\"" + ROBOT_TYPE_ATTRIBUTE + "\","
                            + " found \"" + root.getAttribute("type") + "\". RobotConfigResFilter"
                            + " filters on exactly this attribute; get it wrong and the config"
                            + " silently never appears on the Driver Station.",
                    ROBOT_TYPE_ATTRIBUTE.equals(root.getAttribute("type")));
        }
    }

    /**
     * The {@code <Robot>} element must not carry a {@code name} attribute.
     *
     * <p>{@code RobotConfigFileManager.getXMLFiles()} names a bundled config
     * from that attribute, falling back to the resource entry name. Leaving it
     * off means the filename is the one and only identity string — the thing
     * {@link RobotIdentity} keys on. Adding it would create a second string
     * free to disagree with the first.
     */
    @Test
    public void noConfigOverridesItsNameWithARobotNameAttribute() {
        for (File file : configFiles()) {
            Element root = parse(file).getDocumentElement();
            assertFalse(file.getName() + " sets a name attribute on <Robot>. Remove it: the"
                            + " filename is the robot's identity, and a name attribute would"
                            + " override it and could disagree with it.",
                    root.hasAttribute("name"));
        }
    }

    /**
     * Last season's configs contained {@code //PORT 1 IS DEAD} and
     * {@code //hood center?} — C-style comments, which XML does not have. They
     * parsed as stray character data and happened to be harmless. Use
     * {@code <!-- -->}.
     */
    @Test
    public void noConfigUsesSlashSlashComments() {
        for (File file : configFiles()) {
            String contents = read(file);
            int line = 0;
            for (String text : contents.split("\n", -1)) {
                line++;
                String trimmed = text.trim();
                if (trimmed.startsWith("//") || trimmed.contains("/> //") || trimmed.contains("> //")) {
                    fail(file.getName() + " line " + line + " uses a // comment, which XML does"
                            + " not support: " + trimmed + "\n  Use <!-- --> instead.");
                }
            }
        }
    }

    @Test
    public void noTwoDevicesShareAPortOnTheSameHub() {
        for (File file : configFiles()) {
            for (Element module : childElements(parse(file).getDocumentElement(), true)) {
                if (!"LynxModule".equals(module.getTagName())) {
                    continue;
                }
                String hub = module.getAttribute("name");
                Map<String, String> occupied = new HashMap<>();

                for (Element device : childElements(module, false)) {
                    if (CONTAINER_TAGS.contains(device.getTagName())) {
                        continue;
                    }
                    DeviceNames.Kind kind = kindOf(device);
                    // I2C devices legitimately share a bus at different
                    // addresses, so a bus collision is not an error.
                    if (kind == DeviceNames.Kind.I2C) {
                        continue;
                    }
                    String slot = kind + " port " + device.getAttribute("port");
                    String previous = occupied.put(slot, device.getAttribute("name"));
                    if (previous != null) {
                        fail(file.getName() + ": on \"" + hub + "\", both \"" + previous
                                + "\" and \"" + device.getAttribute("name") + "\" claim " + slot
                                + ".");
                    }
                }
            }
        }
    }

    @Test
    public void everyPortNumberIsInRangeForItsKind() {
        for (File file : configFiles()) {
            for (Element device : allDeviceElements(parse(file).getDocumentElement())) {
                String name = device.getAttribute("name");
                DeviceNames.Kind kind = kindOf(device);

                if (kind == DeviceNames.Kind.ETHERNET) {
                    // No hub port to range-check; everyEthernetDeviceIsAddressable
                    // checks its address instead.
                    continue;
                }

                if (kind == DeviceNames.Kind.I2C) {
                    int bus = intAttribute(file, name, device, "bus");
                    assertTrue(file.getName() + ": \"" + name + "\" is on I2C bus " + bus
                            + "; a REV hub has buses 0-3.", bus >= 0 && bus <= 3);
                    continue;
                }

                int port = intAttribute(file, name, device, "port");
                int maxPort = kind == DeviceNames.Kind.MOTOR ? 3 : 5;
                assertTrue(file.getName() + ": \"" + name + "\" is a " + kind + " on port " + port
                                + "; a REV hub has " + kind + " ports 0-" + maxPort + ".",
                        port >= 0 && port <= maxPort);
            }
        }
    }

    /**
     * Hub devices belong inside a {@code <LynxModule>}; an Ethernet device belongs
     * directly under {@code <Robot>}. The SDK parser looks in exactly those places,
     * so an element anywhere else is skipped without a word and its device is
     * simply missing on the robot.
     */
    @Test
    public void everyDeviceSitsWhereTheSdkLooksForIt() {
        for (File file : configFiles()) {
            for (Element device : allDeviceElements(parse(file).getDocumentElement())) {
                String name = device.getAttribute("name");
                String parent = ((Element) device.getParentNode()).getTagName();
                if (kindOf(device) == DeviceNames.Kind.ETHERNET) {
                    assertTrue(file.getName() + ": <" + ETHERNET_TAG + "> \"" + name + "\" is inside <"
                                    + parent + ">. It must be a direct child of <" + ROBOT_ROOT_TAG
                                    + ">, beside <LynxUsbDevice>: it is not on a hub.",
                            ROBOT_ROOT_TAG.equals(parent));
                } else {
                    assertTrue(file.getName() + ": \"" + name + "\" is inside <" + parent + ">. A"
                                    + " hub device must be inside a <LynxModule>.",
                            "LynxModule".equals(parent));
                }
            }
        }
    }

    /**
     * An Ethernet device's equivalent of a port range check. The SDK builds a
     * {@code Limelight3A} from {@code name} and {@code ipAddress} alone
     * ({@code HardwareFactory.mapEthernetOverUsb}), so a bad address is a camera that
     * is in the hardware map and never answers.
     */
    @Test
    public void everyEthernetDeviceIsAddressable() {
        for (File file : configFiles()) {
            Set<String> addresses = new HashSet<>();
            for (Element device : allDeviceElements(parse(file).getDocumentElement())) {
                if (kindOf(device) != DeviceNames.Kind.ETHERNET) {
                    continue;
                }
                String name = device.getAttribute("name");

                String ip = device.getAttribute("ipAddress");
                assertTrue(file.getName() + ": \"" + name + "\" has ipAddress=\"" + ip + "\", which"
                                + " is not a dotted IPv4 address. The RC sends the camera's HTTP"
                                + " requests there.",
                        isIpv4(ip));
                assertTrue(file.getName() + ": \"" + name + "\" and another Ethernet device both"
                        + " claim " + ip + ".", addresses.add(ip));

                String serial = device.getAttribute("serialNumber");
                String rest = serial.startsWith(ETHERNET_SERIAL_PREFIX)
                        ? serial.substring(ETHERNET_SERIAL_PREFIX.length()) : "";
                int colon = rest.lastIndexOf(':');
                assertTrue(file.getName() + ": \"" + name + "\" has serialNumber=\"" + serial
                                + "\". It must read " + ETHERNET_SERIAL_PREFIX + "<interface>:<IPv4>,"
                                + " or the SDK cannot build the device.",
                        colon > 0 && isIpv4(rest.substring(colon + 1)));
            }
        }
    }

    // ------------------------------------------------------------- plumbing

    private static boolean isIpv4(String text) {
        String[] octets = text.split("\\.", -1);
        if (octets.length != 4) {
            return false;
        }
        for (String octet : octets) {
            if (!octet.matches("\\d{1,3}") || Integer.parseInt(octet) > 255) {
                return false;
            }
        }
        return true;
    }

    /**
     * The robot a file belongs to. A file with none already fails
     * {@link #everyRobotIdentityHasExactlyOneConfigFile}; this repeats the
     * message so the per-device tests do not fail with a bare null.
     */
    private static RobotIdentity identityOf(File file) {
        RobotIdentity identity = RobotIdentity.fromConfigName(stripXmlExtension(file.getName()));
        if (identity == null) {
            fail(file.getName() + " has no RobotIdentity constant, so there is no device list"
                    + " to check it against.");
        }
        return identity;
    }

    /**
     * Classifies a device element.
     *
     * <p>Attribute-driven rather than a hardcoded list of the SDK's device
     * tags, which run to hundreds and change between releases. This only has
     * to be right for devices we actually declare — anything else already
     * fails {@link #noConfigDeclaresADeviceTheCodeDoesNotKnowAbout}.
     */
    private static DeviceNames.Kind kindOf(Element device) {
        if (ETHERNET_TAG.equals(device.getTagName())) {
            return DeviceNames.Kind.ETHERNET;
        }
        if (device.hasAttribute("bus")) {
            return DeviceNames.Kind.I2C;
        }
        if (device.getTagName().contains("Servo")) {
            return DeviceNames.Kind.SERVO;
        }
        return DeviceNames.Kind.MOTOR;
    }

    private static Map<String, Element> devicesIn(File file) {
        Map<String, Element> devices = new HashMap<>();
        for (Element device : allDeviceElements(parse(file).getDocumentElement())) {
            String name = device.getAttribute("name");
            Element previous = devices.put(name, device);
            if (previous != null) {
                fail(file.getName() + " declares \"" + name + "\" more than once.");
            }
        }
        return devices;
    }

    private static List<Element> allDeviceElements(Element root) {
        List<Element> devices = new ArrayList<>();
        collectDevices(root, devices);
        return devices;
    }

    private static void collectDevices(Element element, List<Element> into) {
        for (Element child : childElements(element, false)) {
            if (CONTAINER_TAGS.contains(child.getTagName())) {
                collectDevices(child, into);
            } else if (child.hasAttribute("name")) {
                into.add(child);
            }
        }
    }

    private static List<Element> childElements(Element parent, boolean recursive) {
        List<Element> elements = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            elements.add((Element) child);
            if (recursive) {
                elements.addAll(childElements((Element) child, true));
            }
        }
        return elements;
    }

    private static int intAttribute(File file, String name, Element device, String attribute) {
        String raw = device.getAttribute(attribute);
        if (raw.isEmpty()) {
            fail(file.getName() + ": \"" + name + "\" has no " + attribute + " attribute.");
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            fail(file.getName() + ": \"" + name + "\" has " + attribute + "=\"" + raw
                    + "\", which is not a number.");
            return -1; // unreachable
        }
    }

    private static Document parse(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(file);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new AssertionError(file.getName() + " is not well-formed XML: " + e.getMessage(), e);
        }
    }

    private static String read(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Could not read " + file, e);
        }
    }

    private static String stripXmlExtension(String fileName) {
        return fileName.substring(0, fileName.length() - ".xml".length());
    }

    /** Every {@code robot_*.xml} under {@code src/main/res/xml}. */
    private static List<File> configFiles() {
        File directory = resXmlDirectory();
        File[] found = directory.listFiles((dir, name) ->
                name.startsWith("robot_") && name.endsWith(".xml"));

        if (found == null || found.length == 0) {
            throw new AssertionError("No robot_*.xml found in " + directory.getAbsolutePath());
        }
        List<File> files = new ArrayList<>(Arrays.asList(found));
        files.sort((a, b) -> a.getName().compareTo(b.getName()));
        return files;
    }

    /**
     * Locates {@code src/main/res/xml}. Gradle runs unit tests with the module
     * directory as the working directory, but walking up keeps this working
     * when a test is run straight from an IDE with the repository root instead.
     */
    private static File resXmlDirectory() {
        File candidate = new File(System.getProperty("user.dir"));
        for (int depth = 0; depth < 4 && candidate != null; depth++) {
            File direct = new File(candidate, "src/main/res/xml");
            if (direct.isDirectory()) {
                return direct;
            }
            File viaModule = new File(candidate, "TeamCode/src/main/res/xml");
            if (viaModule.isDirectory()) {
                return viaModule;
            }
            candidate = candidate.getParentFile();
        }
        throw new AssertionError(
                "Could not locate src/main/res/xml from " + System.getProperty("user.dir"));
    }
}
