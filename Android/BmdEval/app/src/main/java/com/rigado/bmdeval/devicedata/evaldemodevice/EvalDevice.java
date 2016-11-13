package com.rigado.bmdeval.devicedata.evaldemodevice;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.rigado.bmdeval.devicedata.otherdevices.BleDevice;
import com.rigado.rigablue.*;

import java.util.UUID;

public class EvalDevice extends BleDevice implements IEvalDeviceActions {

    private static final String TAG = EvalDevice.class.getSimpleName();

    public static final String BMDEVAL_UUID_SERVICE = "50db1523-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_BUTTON_CHAR = "50db1524-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_LED_CHAR = "50db1525-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_ADC_CHAR = "50db1526-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_CTRL_CHAR = "50db1527-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_ACCEL_CHAR = "50db1528-418d-4690-9589-ab7be9e22684";

    public static final byte ADC_STREAM_START = 0x01;
    public static final byte ADC_STREAM_STOP = 0x02;
    public static final byte ACCEL_STREAM_START = 0x06;
    public static final byte ACCEL_STREAM_STOP = 0x09;
    public static final byte [] HARDDWARE_VERSION_COMMAND = { 0x0A };
    public static final byte [] BOOTLOADER_COMMAND = { -95, -4, -42, -25 };


    RigLeBaseDevice baseDevice;
    BluetoothGattService bmdEvalService;

    public EvalDevice(RigLeBaseDevice device) {
        super(device);
        baseDevice = device;
        //initServices();
    }

    private void initServices() {
        for (BluetoothGattService service : baseDevice.getServiceList()) {
            if (service.getUuid().equals(UUID.fromString(BMDEVAL_UUID_SERVICE))) {
                Log.i(TAG, "Found Eval Demo Service!");
                bmdEvalService = service;
            }
        }

        //enable hardware version notification
        if(bmdEvalService != null) {
            BluetoothGattCharacteristic characteristic =
                    bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));

            baseDevice.setCharacteristicNotification(characteristic, true);
        }
    }

    @Override
    public void setLedColor(RgbColor color) {
        if(bmdEvalService == null) {
            return;
        }

        BluetoothGattCharacteristic ledChar =
                bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_LED_CHAR));

        if(ledChar == null) {
            return;
        }

        byte [] data = new byte[] { color.getRed(), color.getGreen(), color.getBlue() };

        baseDevice.writeCharacteristic(ledChar, data);
    }

    @Override
    public RgbColor getLedColor() {
        if(bmdEvalService == null) {
            return null;
        }

        BluetoothGattCharacteristic ledChar =
                bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_LED_CHAR));

        if(ledChar == null) {
            return null;
        }

        byte [] value = ledChar.getValue();

        RgbColor color = new RgbColor(value[0], value[1], value[2]);
        return color;
    }

    @Override
    public void startAmbientLightSensing() {
        if(bmdEvalService == null) {
            return;
        }

        BluetoothGattCharacteristic adcChar =
                bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));

        if(adcChar == null) {
            return;
        }

        byte [] command = new byte[] { ADC_STREAM_START };
        baseDevice.writeCharacteristic(adcChar, command);
    }

    @Override
    public void stopAmbientLightSensing() {
        if(bmdEvalService == null) {
            return;
        }

        BluetoothGattCharacteristic adcChar =
                bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));

        if(adcChar == null) {
            return;
        }

        byte [] command = new byte[] { ADC_STREAM_STOP };
        baseDevice.writeCharacteristic(adcChar, command);
    }

    @Override
    public void startAccelerometerStream() {
        if(bmdEvalService == null) {
            return;
        }

        BluetoothGattCharacteristic accelChar =
                bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));

        if(accelChar == null) {
            return;
        }

        byte [] command = new byte[] { ACCEL_STREAM_START };
        baseDevice.writeCharacteristic(accelChar, command);
    }

    @Override
    public void stopAccelerometerStream() {
        if(bmdEvalService == null) {
            return;
        }

        BluetoothGattCharacteristic accelChar =
                bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));
        if(accelChar == null) {
            return;
        }

        byte [] command = new byte[] { ACCEL_STREAM_STOP };
        baseDevice.writeCharacteristic(accelChar, command);
    }

    @Override
    public void setButtonNotificationsEnabled(boolean enable) {
        if (bmdEvalService == null) {
            return;
        }

        final BluetoothGattCharacteristic buttonChar =
                bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_BUTTON_CHAR));

        if (buttonChar == null) {
            return;
        }

        baseDevice.setCharacteristicNotification(buttonChar, true);
    }

    @Override
    public void discoveryDidComplete(RigLeBaseDevice device) {

    }

    @Override
    public void requestHardwareVersion() {
        if (bmdEvalService == null) {
            return;
        }

        final BluetoothGattCharacteristic controlChar =
                bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));

        if (controlChar == null) {
            return;
        }

        baseDevice.writeCharacteristic(controlChar, HARDDWARE_VERSION_COMMAND);
    }
}
