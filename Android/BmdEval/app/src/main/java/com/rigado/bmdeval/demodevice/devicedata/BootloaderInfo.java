package com.rigado.bmdeval.demodevice.devicedata;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;

/**
 *
 * See BMDWare-Data-Sheet.pdf
 * Section 7. Appendix A: Bootloader version information
 *
 * typedef enum version_type_e {
 * VERSION_TYPE_RELEASE = 1,
 * VERSION_TYPE_DEBUG = 2
 * }
 *
 * version_type_t;
 * typedef enum softdevice_support_e {
 * SOFTDEVICE_SUPPORT_S110 = 1,
 * SOFTDEVICE_SUPPORT_S120 = 2,
 * SOFTDEVICE_SUPPORT_S130 = 3,
 * SOFTDEVICE_SUPPORT_S132 = 4,
 * SOFTDEVICE_SUPPORT_RESERVED2,
 * SOFTDEVICE_SUPPORT_RESERVED3,
 * SOFTDEVICE_SUPPORT_RESERVED4,
 * SOFTDEVICE_SUPPORT_RESERVED5
 * } softdevice_support_t;
 *
 * typedef enum hardware_support_e {
 * HARDWARE_SUPPORT_NRF51 = 1,
 * HARDWARE_SUPPORT_NRF52 = 2,
 * HARDWARE_SUPPORT_RESERVED1,
 * HARDWARE_SUPPORT_RESERVED2,
 * HARDWARE_SUPPORT_RESERVED3,
 * HARDWARE_SUPPORT_RESERVED4,
 * HARDWARE_SUPPORT_RESERVED5
 * } hardware_support_t;
 *
 * typedef struct rig_firmware_info_s {
 * uint32_t magic_number_a;
 * uint32_t info_size;
 * uint8_t version_major;
 * uint8_t version_minor;
 * uint8_t version_rev;
 * uint32_t build_number;
 * version_type_t version_type;
 * softdevice_support_t sd_support;
 * hardware_support_t hw_support;
 * uint16_t protocol_version;
 * uint32_t magic_number_b; //Always 0x49B0784C
 * } rig_firmware_info_t;
 *
 */

public class BootloaderInfo {

    // These values correspond directly to the byte values
    // received from the bootloader. Do not! Change!

    // Version Type
    private static final int VERSION_TYPE_RELEASE = 1;
    private static final int VERSION_TYPE_DEBUG = 2;

    //Soft Device Support
    private static final int SOFTDEVICE_SUPPORT_S110 = 1;
    private static final int SOFTDEVICE_SUPPORT_S120 = 2;
    private static final int SOFTDEVICE_SUPPORT_S130 = 3;
    private static final int SOFTDEVICE_SUPPORT_S132 = 4;

    //Hardware Support
    private static final int HARDWARE_SUPPORT_NRF51 = 1;
    public static final int HARDWARE_SUPPORT_NRF52 = 2;

    //Bootloader value indices
    private static final int INFO_INDEX = 0;
    private static final int VERSION_MAJOR_INDEX = 1;
    private static final int VERSION_MINOR_INDEX = 2;
    private static final int VERSION_REVISION_INDEX = 3;
    private static final int BUILD_NUMBER_START_INDEX = 4;
    private static final int VERSION_TYPE_INDEX = 8;
    private static final int SOFT_DEVICE_INDEX = 9;
    private static final int HARDWARE_INDEX = 10;
    private static final int PROTOCOL_INDEX = 11;

    private static final int BUILD_NUMBER_DATA_SIZE = 4;

    public static final int SIZE = 20;
    public static final int LEGACY_SIZE = 12;
    public static final int LEGACY_HARDWARE_INDEX = 9;


    public enum VersionType {
        Release (VERSION_TYPE_RELEASE, "Release"),
        Debug   (VERSION_TYPE_DEBUG, "Debug");

        private final int valType;
        private final String valDescription;

        VersionType(int valType, String valDescription) {
            this.valType = valType;
            this.valDescription = valDescription;
        }

        int getType() {
            return this.valType;
        }

        public String getDescription() {
            return this.valDescription;
        }

        static VersionType versionTypeFromByte(int value) {
            for (VersionType versionType : VersionType.values()) {
                if (versionType.getType() == value) {
                    return versionType;
                }
            }
            return null;
        }
    }

    public enum SoftDeviceSupport {
        S110    (SOFTDEVICE_SUPPORT_S110, "S110"),
        S120    (SOFTDEVICE_SUPPORT_S120, "S120"),
        S130    (SOFTDEVICE_SUPPORT_S130, "S130"),
        S132    (SOFTDEVICE_SUPPORT_S132, "S132");

        private final int valType;
        private final String valDescription;

        SoftDeviceSupport(int valType, String valDescription) {
            this.valType = valType;
            this.valDescription = valDescription;
        }

        public int getType() {
            return this.valType;
        }

        public String getDescription() {
            return this.valDescription;
        }

        static SoftDeviceSupport softDeviceSupportFromByte(int value) {
            for (SoftDeviceSupport softDevice : SoftDeviceSupport.values()) {
                if (softDevice.getType() == value) {
                    return softDevice;
                }
            }
            return null;
        }
    }

    public enum HardwareSupport {
        NRF51   (HARDWARE_SUPPORT_NRF51, "NRF51"),
        NRF52   (HARDWARE_SUPPORT_NRF52, "NRF52");

        private final int valType;
        private final String valDescription;

        HardwareSupport(int valType, String valDescription) {
            this.valType = valType;
            this.valDescription = valDescription;
        }

        public int getType() {
            return this.valType;
        }

        public String getDescription() {
            return this.valDescription;
        }

        static HardwareSupport hardwareTypeFromByte(int value) {
            for (HardwareSupport hardwareType : HardwareSupport.values()) {
                if (hardwareType.getType() == value) {
                    return hardwareType;
                }
            }
            return null;
        }
    }

    private int infoSize;
    private int versionMajor;
    private int versionMinor;
    private int versionRevision;
    private int buildNumber;
    private VersionType versionType;
    private SoftDeviceSupport softDeviceSupport;
    private HardwareSupport hardwareSupport;
    private int bootloaderProtocolVer;


    public BootloaderInfo(byte [] bytes) {
        this.infoSize = parseInfoSize(bytes);
        this.versionMajor = parseVersionMajor(bytes);
        this.versionMinor = parseVersionMinor(bytes);
        this.versionRevision = parseVersionRevision(bytes);
        this.buildNumber = parseBuildNumber(bytes);
        this.versionType = parseVersionType(bytes);
        this.softDeviceSupport = parseSoftDeviceSupport(bytes);
        this.hardwareSupport = parseHardwareSupport(bytes);
        this.bootloaderProtocolVer = parseProtocolVer(bytes);
    }

    private int parseInfoSize(@NonNull byte [] bytes) {
        return bytes[INFO_INDEX];
    }

    private int parseVersionMajor(@NonNull byte [] bytes) {
        return bytes[VERSION_MAJOR_INDEX];
    }

    private int parseVersionMinor(@NonNull byte [] bytes) {
        return bytes[VERSION_MINOR_INDEX];
    }

    private int parseVersionRevision(@NonNull byte [] bytes) {
        return bytes[VERSION_REVISION_INDEX];
    }

    private int parseBuildNumber(@NonNull byte [] bytes) {
        if (bytes.length < (BUILD_NUMBER_START_INDEX + 1 + BUILD_NUMBER_DATA_SIZE)) {
            return 0;
        }

        final byte[] data = new byte[BUILD_NUMBER_DATA_SIZE];
        System.arraycopy(bytes, BUILD_NUMBER_START_INDEX, data, 0, BUILD_NUMBER_DATA_SIZE);
        int value = ByteBuffer.wrap(data).getInt();
        value = Integer.reverseBytes(value);
        return value;
    }

    private VersionType parseVersionType(@NonNull byte[] bytes) {
        if (bytes.length < (VERSION_TYPE_INDEX + 1)) {
            return null;
        }

        return VersionType.versionTypeFromByte(bytes[VERSION_TYPE_INDEX]);
    }

    private SoftDeviceSupport parseSoftDeviceSupport(@NonNull byte [] bytes) {
        if (bytes.length < (SOFT_DEVICE_INDEX + 1)) {
            return null;
        }

        return SoftDeviceSupport.softDeviceSupportFromByte(bytes[SOFT_DEVICE_INDEX]);
    }

    private HardwareSupport parseHardwareSupport(@NonNull byte [] bytes) {
        if (bytes.length < (HARDWARE_INDEX + 1)) {
            return null;
        }

        return HardwareSupport.hardwareTypeFromByte(bytes[HARDWARE_INDEX]);
    }

    // RigDfu has its own Protocol Version. This is different from
    // BMDware's protocol version.
    private int parseProtocolVer(@NonNull byte [] bytes) {
        if (bytes.length < (PROTOCOL_INDEX + 1)) {
            return 0;
        }

        return bytes[PROTOCOL_INDEX];
    }

    public int getInfoSize() {
        return infoSize;
    }

    public int getVersionMajor() {
        return versionMajor;
    }

    public int getVersionMinor() {
        return versionMinor;
    }

    public int getVersionRevision() {
        return versionRevision;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public VersionType getVersionType() {
        return versionType;
    }

    public SoftDeviceSupport getSoftDeviceSupport() {
        return softDeviceSupport;
    }

    public HardwareSupport getHardwareSupport() {
        return hardwareSupport;
    }

    public int getBootloaderProtocolVer() {
        return bootloaderProtocolVer;
    }

}
