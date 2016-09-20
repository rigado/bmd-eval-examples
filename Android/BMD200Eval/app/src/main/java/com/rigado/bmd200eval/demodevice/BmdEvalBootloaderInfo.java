package com.rigado.bmd200eval.demodevice;

import com.rigado.rigablue.RigLeBaseDevice;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

/**
 * Created by stutzenbergere on 7/23/15.
 */
public class BmdEvalBootloaderInfo {

    private static final String RESET_SERVICE_UUID = "50db1523-418d-4690-9589-ab7be9e22684";
    private static final String RESET_CHAR_UUID = "50db1527-418d-4690-9589-ab7be9e22684";

    private static final String BLINKY_RESET_SERVICE_UUID = "6d580001-fc91-486b-82c4-86a1d2eb8f88";
    private static final String BLINKY_RESET_CHAR_UUID = "6d580002-fc91-486b-82c4-86a1d2eb8f88";

    private static final String BMDWARE_RESET_SERVICE_UUID = "2413B33F-707F-90BD-2045-2AB8807571B7";
    private static final String BMDWARE_RESET_CHAR_UUID = "2413B43F-707F-90BD-2045-2AB8807571B7";

    /* Note: Java is pretty annoying when it comes to dealing with raw bytes.  Because the type
     * 'byte' is not unsigned, values like 0xfc and 0xd6 are considered integers by the compiler
     * instead of bytes.  As such, the don't fite in to the value of a 'byte' type.  However, the
     * value range of a 'byte' type is -128 to 127.  So, negative numbers are used below where
     * appropriate to get around this issue.
     */
    private final static byte [] bmdware_bootloader_command = { 0x03, 0x56, 0x30, 0x57 };
    private final static byte [] bmdeval_bootloader_command = { -95, -4, -42, -25 };
    private final static byte [] blinky_bootloader_command = { -104, -74, 0x2f, 0x51 };

    RigLeBaseDevice baseDevice;
    BluetoothGattService bootloader_service;
    BluetoothGattCharacteristic bootloader_char;
    byte [] bootloader_command;

    public BmdEvalBootloaderInfo(BmdEvalDemoDevice device) {

        UUID serviceUuid;
        UUID charUuid;

        baseDevice = device.getBaseDevice();
        bootloader_command = new byte[4];
        if(device.getType() == com.rigado.bmd200eval.demodevice.BmdEvalDemoDevice.DemoDeviceType.BlinkyDemo) {
            serviceUuid = UUID.fromString(BLINKY_RESET_SERVICE_UUID);
            charUuid = UUID.fromString(BLINKY_RESET_CHAR_UUID);
            System.arraycopy(blinky_bootloader_command, 0, bootloader_command, 0, 4);
        } else if(device.getType() == com.rigado.bmd200eval.demodevice.BmdEvalDemoDevice.DemoDeviceType.BMDware) {
            serviceUuid = UUID.fromString(BMDWARE_RESET_SERVICE_UUID);
            charUuid = UUID.fromString(BMDWARE_RESET_CHAR_UUID);
            System.arraycopy(bmdware_bootloader_command, 0, bootloader_command, 0, 4);
        } else {
            serviceUuid = UUID.fromString(RESET_SERVICE_UUID);
            charUuid = UUID.fromString(RESET_CHAR_UUID);
            System.arraycopy(bmdeval_bootloader_command, 0, bootloader_command, 0, 4);
        }

        for(BluetoothGattService svc : baseDevice.getServiceList()) {
            if(svc.getUuid().equals(serviceUuid)) {
                bootloader_service = svc;
                break;
            }
        }

        bootloader_char = bootloader_service.getCharacteristic(charUuid);
    }

    public BluetoothGattService getBootloaderService() {
        return bootloader_service;
    }

    public BluetoothGattCharacteristic getBootloaderCharacteristic() {
        return bootloader_char;
    }

    public byte[] getBootloaderCommand() {
        return bootloader_command;
    }
}
