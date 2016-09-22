package com.rigado.bmd200eval.demodevice;

import com.rigado.bmd200eval.utilities.Constants;
import com.rigado.rigablue.RigLeBaseDevice;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.UUID;

/**
 * Created by stutzenbergere on 7/23/15.
 */
public class BmdEvalBootloaderInfo {

    /* Note: Java is pretty annoying when it comes to dealing with raw bytes.  Because the type
     * 'byte' is not unsigned, values like 0xfc and 0xd6 are considered integers by the compiler
     * instead of bytes.  As such, the don't fite in to the value of a 'byte' type.  However, the
     * value range of a 'byte' type is -128 to 127.  So, negative numbers are used below where
     * appropriate to get around this issue.
     */
    private final static byte [] bmdware_bootloader_command = { 0x03, 0x56, 0x30, 0x57 };
    private final static byte [] bmdeval_bootloader_command = { -95, -4, -42, -25 };
    private final static byte [] blinky_bootloader_command = { -104, -74, 0x2f, 0x51 };

    private final static String TAG = BmdEvalBootloaderInfo.class.getSimpleName();

    RigLeBaseDevice baseDevice;
    BluetoothGattService bootloader_service;
    BluetoothGattCharacteristic bootloader_char;
    byte [] bootloader_command;

    public BmdEvalBootloaderInfo(BmdEvalDemoDevice device) {

        UUID serviceUuid;
        UUID charUuid;

        baseDevice = device.getBaseDevice();
        bootloader_command = new byte[4];
        if(device.getType() == BmdEvalDemoDevice.DemoDeviceType.BlinkyDemo) {
            Log.i(TAG, "found blinky demo");
            serviceUuid = UUID.fromString(Constants.BLINKY_RESET_SERVICE_UUID);
            charUuid = UUID.fromString(Constants.BLINKY_UUID_CTRL_CHAR);
            System.arraycopy(blinky_bootloader_command, 0, bootloader_command, 0, 4);
        } else if(device.getType() == BmdEvalDemoDevice.DemoDeviceType.BMDware) {
            Log.i(TAG, "found bmdware");
            serviceUuid = UUID.fromString(Constants.BMDWARE_RESET_SERVICE_UUID);
            charUuid = UUID.fromString(Constants.BMDWARE_RESET_CHAR_UUID);
            System.arraycopy(bmdware_bootloader_command, 0, bootloader_command, 0, 4);
        } else {
            serviceUuid = UUID.fromString(Constants.BMDEVAL_UUID_SERVICE);
            Log.i(TAG, "found eval demo " + serviceUuid.toString());
            charUuid = UUID.fromString(Constants.BMDEVAL_UUID_CTRL_CHAR);
            System.arraycopy(bmdeval_bootloader_command, 0, bootloader_command, 0, 4);
        }

        for(BluetoothGattService svc : baseDevice.getServiceList()) {
            Log.i(TAG, "Service uuid " + svc.getUuid().toString());
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
