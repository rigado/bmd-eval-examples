package com.rigado.bmd200eval.demodevice;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;
import android.util.Log;

import com.rigado.bmd200eval.interfaces.IBmdHardwareListener;
import com.rigado.bmd200eval.utilities.Constants;
import com.rigado.rigablue.*;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by stutzenbergere on 7/23/15.
 */


public class BmdEvalDemoDevice implements IRigLeBaseDeviceObserver {

    private static final String TAG = BmdEvalDemoDevice.class.getSimpleName();

    BluetoothGattService bmdEvalService;
    BluetoothGattService disService;
    BluetoothGattService blinkyService;

    IBmdHardwareListener mListener;

    public enum DemoDeviceType {
        MainEvalDemo,
        BlinkyDemo,
        BMDware
    }

    private boolean is200;
    private boolean hardwareVersionFound;

    RigLeBaseDevice baseDevice;
    DemoDeviceType type;
    IBmdEvalDemoDeviceListener observer;

    public BmdEvalDemoDevice(RigLeBaseDevice device) {
        baseDevice = device;
        type = DemoDeviceType.MainEvalDemo;
        hardwareVersionFound = false;
        is200 = true;

        String name = device.getName();
        baseDevice.setObserver(this);

        if(name.contains(Constants.BMDWARE_DEVICE_NAME)) {
            Log.i(TAG, "Type Bmdware");
            type = DemoDeviceType.BMDware;
        } else if(name.contains(Constants.BLINKY_DEMO_DEVICE_NAME)) {
            Log.i(TAG, "Type Blinky");
            type = DemoDeviceType.BlinkyDemo;
        }

        if(type == DemoDeviceType.BlinkyDemo || type == DemoDeviceType.MainEvalDemo) {
            initServices();
        }

    }

    public void registerHardwareListener(IBmdHardwareListener listener) {
        mListener = listener;
    }

    public boolean isBmd200() {
        return this.is200;
    }

    public void setBmd200Status(boolean isBmd200) {
        this.is200 = isBmd200;
    }

    private void initServices() {
        for(BluetoothGattService service : baseDevice.getServiceList()) {
            if(service.getUuid().equals(UUID.fromString(Constants.BMDEVAL_UUID_SERVICE))) {
                Log.i(TAG, "Found eval service!");
                bmdEvalService = service;
            } else if(service.getUuid().equals(UUID.fromString(Constants.DIS_UUID_SERVICE))) {
                Log.i(TAG, "Found dis service!");
                disService = service;
            } else if(service.getUuid().equals(UUID.fromString(Constants.BLINKY_RESET_SERVICE_UUID))) {
                Log.i(TAG, "Found blinky service!");
                blinkyService = service;
            }
        }

        //enable hardware version notification
        if(bmdEvalService != null) {
            BluetoothGattCharacteristic characteristic =
                    bmdEvalService.getCharacteristic(UUID.fromString(Constants.BMDEVAL_UUID_CTRL_CHAR));

            baseDevice.setCharacteristicNotification(characteristic, true);
        }

        if(blinkyService != null) {
            BluetoothGattCharacteristic characteristic =
                    blinkyService.getCharacteristic(UUID.fromString(Constants.BLINKY_UUID_CTRL_CHAR));

            baseDevice.writeCharacteristic(characteristic, Constants.CMD_HARDWARE_VERSION);
        }
    }

    public DemoDeviceType getType() {
        return type;
    }

    public RigLeBaseDevice getBaseDevice() {
        return baseDevice;
    }

    public BmdEvalBootloaderInfo getBootloaderInfo() {
        BmdEvalBootloaderInfo bi = new BmdEvalBootloaderInfo(this);
        return bi;
    }

    public void setObserver(IBmdEvalDemoDeviceListener o) {
        observer = o;
    }

    public void setLedColor(RgbColor color) {
        if(bmdEvalService == null) {
            return;
        }

        BluetoothGattCharacteristic ledChar = bmdEvalService.getCharacteristic(UUID.fromString(Constants.BMDEVAL_UUID_LED_CHAR));
        if(ledChar == null) {
            return;
        }

        byte [] data = new byte[] { color.getRed(), color.getGreen(), color.getBlue() };

        baseDevice.writeCharacteristic(ledChar, data);
    }

    public RgbColor getLedColor() {
        if(bmdEvalService == null) {
            return null;
        }

        BluetoothGattCharacteristic ledChar = bmdEvalService.getCharacteristic(UUID.fromString(Constants.BMDEVAL_UUID_LED_CHAR));
        if(ledChar == null) {
            return null;
        }

        byte [] value = ledChar.getValue();

        RgbColor color = new RgbColor(value[0], value[1], value[2]);
        return color;
    }

    public void startAmbientLightSensing() {
        if(!hardwareVersionFound) {
            return;
        }

        if(bmdEvalService == null) {
            return;
        }

        BluetoothGattCharacteristic adcChar = bmdEvalService.getCharacteristic(UUID.fromString(Constants.BMDEVAL_UUID_CTRL_CHAR));
        if(adcChar == null) {
            return;
        }

        byte [] command = new byte[] { Constants.EVAL_CMD_ADC_STREAM_START };
        baseDevice.writeCharacteristic(adcChar, command);
    }

    public void stopAmbientLightSensing() {
        if(bmdEvalService == null) {
            return;
        }

        BluetoothGattCharacteristic adcChar = bmdEvalService.getCharacteristic(UUID.fromString(Constants.BMDEVAL_UUID_CTRL_CHAR));
        if(adcChar == null) {
            return;
        }

        byte [] command = new byte[] { Constants.EVAL_CMD_ADC_STREAM_STOP };
        baseDevice.writeCharacteristic(adcChar, command);
    }

    public void startAccelerometerStream() {
        if(!hardwareVersionFound) {
            return;
        }
        if(bmdEvalService == null) {
            return;
        }

        BluetoothGattCharacteristic accelChar = bmdEvalService.getCharacteristic(UUID.fromString(Constants.BMDEVAL_UUID_CTRL_CHAR));
        if(accelChar == null) {
            return;
        }

        byte [] command = new byte[] { Constants.EVAL_CMD_ACCEL_STREAM_START };
        baseDevice.writeCharacteristic(accelChar, command);
    }

    public void stopAccelerometerStream() {
        if(bmdEvalService == null) {
            return;
        }

        BluetoothGattCharacteristic accelChar = bmdEvalService.getCharacteristic(UUID.fromString(Constants.BMDEVAL_UUID_CTRL_CHAR));
        if(accelChar == null) {
            return;
        }

        byte [] command = new byte[] { Constants.EVAL_CMD_ACCEL_STREAM_STOP };
        baseDevice.writeCharacteristic(accelChar, command);
    }

    @Override
    public void didUpdateValue(RigLeBaseDevice device, BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, String.format("didUpdateValue %s %s", characteristic.getUuid().toString(), Arrays.toString(characteristic.getValue())));
        UUID uuid = characteristic.getUuid();
        byte [] value = characteristic.getValue();

        if(value == null) {
            Log.e(TAG, "didUpdateValue returned null!");
            return;
        }

        if(uuid.equals(UUID.fromString(Constants.BMDEVAL_UUID_LED_CHAR))) {
            RgbColor color = new RgbColor(value[0], value[1], value[2]);
            if(observer != null) {
                observer.didUpdateLedColor(color);
            }
        } else if(uuid.equals(UUID.fromString(Constants.BMDEVAL_UUID_BUTTON_CHAR))) {
            if(observer != null) {
                ButtonStatus status = new ButtonStatus(value[0]);
                observer.didUpdateButtonData(status);
            }
        } else if(uuid.equals(UUID.fromString(Constants.BMDEVAL_UUID_ADC_CHAR))) {
            if(observer != null) {
                AmbientLight lightLevel = new AmbientLight(value[0]);
                observer.didUpdateAmbientLightData(lightLevel);
            }
        } else if(uuid.equals(UUID.fromString(Constants.BMDEVAL_UUID_ACCEL_CHAR))) {
            AccelData data = new AccelData(value[0], value[1], value[2]);
            if(observer != null) {
                observer.didUpdateAccelData(data);
            }
        } else if(uuid.equals(UUID.fromString(Constants.BMDEVAL_UUID_CTRL_CHAR))) {
            Log.i(TAG, String.format("bmd eval ctrl char value %s", Arrays.toString(characteristic.getValue())));

            determineHardwareVersion(value);

            beginStatusUpdates();


        } else if(uuid.equals(UUID.fromString(Constants.BLINKY_UUID_CTRL_CHAR))) {
            Log.i(TAG, String.format("blinky reset char %s", Arrays.toString(characteristic.getValue())));

            determineHardwareVersion(characteristic.getValue());

        }
    }

    // A return value of [0] indicates a BMD 200 board.
    // A 300+Shield will return [3, 2, 0, 42, 0, 0, 0, 1, 1, 2, 3, 0] - calling getValue[9] returns the version
    // if version == 2, we have a bmd300
    // After receiving hardware version, enable status updates
    private void determineHardwareVersion(final byte[] value) {

        if(value.length > 1 && value[9] == 2) {
            setBmd200Status(false);
        }

        hardwareVersionFound = true;

        if(mListener!=null) {

            mListener.onHardwareVersionReceived();
        }
    }

    private void beginStatusUpdates() {
        BluetoothGattCharacteristic adcChar = bmdEvalService.getCharacteristic(UUID.fromString(Constants.BMDEVAL_UUID_ADC_CHAR));
        baseDevice.setCharacteristicNotification(adcChar, true);

        startAmbientLightSensing();
        startAccelerometerStream();
    }

    @Override
    public void didUpdateNotifyState(RigLeBaseDevice device, BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, "didUpdateNotifyState " + characteristic.getUuid().toString());
        if(characteristic.getUuid().equals(UUID.fromString(Constants.BMDEVAL_UUID_ADC_CHAR))) {
            BluetoothGattCharacteristic tempChar = bmdEvalService.getCharacteristic(UUID.fromString(Constants.BMDEVAL_UUID_ACCEL_CHAR));
            baseDevice.setCharacteristicNotification(tempChar, true);
        } else if(characteristic.getUuid().equals(UUID.fromString(Constants.BMDEVAL_UUID_ACCEL_CHAR))) {
            BluetoothGattCharacteristic tempChar = bmdEvalService.getCharacteristic(UUID.fromString(Constants.BMDEVAL_UUID_BUTTON_CHAR));
            baseDevice.setCharacteristicNotification(tempChar, true);
        } else if(characteristic.getUuid().equals(UUID.fromString(Constants.BMDEVAL_UUID_CTRL_CHAR))) {
            if(bmdEvalService!=null) {
                BluetoothGattCharacteristic tempChar = bmdEvalService.getCharacteristic(UUID.fromString(Constants.BMDEVAL_UUID_CTRL_CHAR));
                baseDevice.writeCharacteristic(tempChar, Constants.CMD_HARDWARE_VERSION);
            }
        }
    }

    @Override
    public void didWriteValue(RigLeBaseDevice device, BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, String.format("didWriteValue %s %s", characteristic.getUuid().toString(), Arrays.toString(characteristic.getValue())));
        if(characteristic.getUuid().equals(UUID.fromString(Constants.BLINKY_UUID_CTRL_CHAR))) {
            baseDevice.readCharacteristic(characteristic);
        }
    }

    @Override
    public void discoveryDidComplete(RigLeBaseDevice device) {

    }
}
