package com.rigado.bmd200eval.demodevice;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.rigado.rigablue.*;

import java.util.UUID;

/**
 * Created by stutzenbergere on 7/23/15.
 */


public class BmdEvalDemoDevice implements IRigLeBaseDeviceObserver {
    private static final String BMDWARE_DEVICE_NAME = "RigCom";
    private static final String BLINKY_DEMO_DEVICE_NAME = "BMD200-Blinky";

    private static final String BMDEVAL_UUID_SERVICE = "50db1523-418d-4690-9589-ab7be9e22684";
    private static final String BMDEVAL_UUID_BUTTON_CHAR = "50db1524-418d-4690-9589-ab7be9e22684";
    private static final String BMDEVAL_UUID_LED_CHAR = "50db1525-418d-4690-9589-ab7be9e22684";
    private static final String BMDEVAL_UUID_ADC_CHAR = "50db1526-418d-4690-9589-ab7be9e22684";
    private static final String BMDEVAL_UUID_CTRL_CHAR = "50db1527-418d-4690-9589-ab7be9e22684";
    private static final String BMDEVAL_UUID_ACCEL_CHAR = "50db1528-418d-4690-9589-ab7be9e22684";

    private static final String DIS_UUID_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb";
    private static final String DIS_UUID_MODEL_NUM = "00002a24-0000-1000-8000-00805f9b34fb";
    private static final String DIS_UUID_SERIAL_NUM = "00002a25-0000-1000-8000-00805f9b34fb";
    private static final String DIS_UUID_FIRMWARE_VER = "00002a26-0000-1000-8000-00805f9b34fb";
    private static final String DIS_UUID_MFG_NAME = "00002a29-0000-1000-8000-00805f9b34fb";

    private static final byte EVAL_CMD_ADC_STREAM_START = 0x01;
    private static final byte EVAL_CMD_ADC_STREAM_STOP = 0x02;
    private static final byte EVAL_CMD_ACCEL_STREAM_START = 0x06;
    private static final byte EVAL_CMD_ACCEL_STREAM_STOP = 0x09;

    BluetoothGattService bmdEvalService;
    BluetoothGattService disService;

    public enum DemoDeviceType
    {
        MainEvalDemo,
        BlinkyDemo,
        BMDware
    }

    RigLeBaseDevice baseDevice;
    DemoDeviceType type;
    IBmdEvalDemoDeviceListener observer;

    public BmdEvalDemoDevice(RigLeBaseDevice device) {
        baseDevice = device;
        type = DemoDeviceType.MainEvalDemo;

        String name = device.getName();
        baseDevice.setObserver(this);

        if(name.equals(BMDWARE_DEVICE_NAME)) {
            type = DemoDeviceType.BMDware;
        } else if(name.equals(BLINKY_DEMO_DEVICE_NAME)) {
            type = DemoDeviceType.BlinkyDemo;
        }

        if(type == DemoDeviceType.MainEvalDemo) {
            initServices();
        }
        //TODO: Verify available characteristics
    }

    private void initServices() {
        for(BluetoothGattService service : baseDevice.getServiceList()) {
            if(service.getUuid().equals(UUID.fromString(BMDEVAL_UUID_SERVICE))) {
                bmdEvalService = service;
            } else if(service.getUuid().equals(UUID.fromString(DIS_UUID_SERVICE))) {
                disService = service;
            }
        }

        if(bmdEvalService != null) {
            BluetoothGattCharacteristic characteristic = bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_ADC_CHAR));
            baseDevice.setCharacteristicNotification(characteristic, true);
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

        BluetoothGattCharacteristic ledChar = bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_LED_CHAR));
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

        BluetoothGattCharacteristic ledChar = bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_LED_CHAR));
        if(ledChar == null) {
            return null;
        }

        byte [] value = ledChar.getValue();

        RgbColor color = new RgbColor(value[0], value[1], value[2]);
        return color;
    }

    public void startAmbientLightSensing() {
        if(bmdEvalService == null) {
            return;
        }

        BluetoothGattCharacteristic adcChar = bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));
        if(adcChar == null) {
            return;
        }

        byte [] command = new byte[] { EVAL_CMD_ADC_STREAM_START };
        baseDevice.writeCharacteristic(adcChar, command);
    }

    public void stopAmbientLightSensing() {
        if(bmdEvalService == null) {
            return;
        }

        BluetoothGattCharacteristic adcChar = bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));
        if(adcChar == null) {
            return;
        }

        byte [] command = new byte[] { EVAL_CMD_ADC_STREAM_STOP };
        baseDevice.writeCharacteristic(adcChar, command);
    }

    public void startAccelerometerStream() {
        if(bmdEvalService == null) {
            return;
        }

        BluetoothGattCharacteristic accelChar = bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));
        if(accelChar == null) {
            return;
        }

        byte [] command = new byte[] { EVAL_CMD_ACCEL_STREAM_START };
        baseDevice.writeCharacteristic(accelChar, command);
    }

    public void stopAccelerometerStream() {
        if(bmdEvalService == null) {
            return;
        }

        BluetoothGattCharacteristic accelChar = bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));
        if(accelChar == null) {
            return;
        }

        byte [] command = new byte[] { EVAL_CMD_ACCEL_STREAM_STOP };
        baseDevice.writeCharacteristic(accelChar, command);
    }

    @Override
    public void didUpdateValue(RigLeBaseDevice device, BluetoothGattCharacteristic characteristic) {
        UUID uuid = characteristic.getUuid();
        byte [] value = characteristic.getValue();
        if(value == null) {
            //TODO: Log bad value data
            return;
        }

        if(uuid.equals(UUID.fromString(BMDEVAL_UUID_LED_CHAR))) {
            RgbColor color = new RgbColor(value[0], value[1], value[2]);
            if(observer != null) {
                observer.didUpdateLedColor(color);
            }
        } else if(uuid.equals(UUID.fromString(BMDEVAL_UUID_BUTTON_CHAR))) {
            if(observer != null) {
                ButtonStatus status = new ButtonStatus(value[0]);
                observer.didUpdateButtonData(status);
            }
        } else if(uuid.equals(UUID.fromString(BMDEVAL_UUID_ADC_CHAR))) {
            if(observer != null) {
                AmbientLight lightLevel = new AmbientLight(value[0]);
                observer.didUpdateAmbientLightData(lightLevel);
            }
        } else if(uuid.equals(UUID.fromString(BMDEVAL_UUID_ACCEL_CHAR))) {
            AccelData data = new AccelData(value[0], value[1], value[2]);
            if(observer != null) {
                observer.didUpdateAccelData(data);
            }
        }
    }

    @Override
    public void didUpdateNotifyState(RigLeBaseDevice device, BluetoothGattCharacteristic characteristic) {
        if(characteristic.getUuid().equals(UUID.fromString(BMDEVAL_UUID_ADC_CHAR))) {
            BluetoothGattCharacteristic tempChar = bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_ACCEL_CHAR));
            baseDevice.setCharacteristicNotification(tempChar, true);
        } else if(characteristic.getUuid().equals(UUID.fromString(BMDEVAL_UUID_ACCEL_CHAR))) {
            BluetoothGattCharacteristic tempChar = bmdEvalService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_BUTTON_CHAR));
            baseDevice.setCharacteristicNotification(tempChar, true);
        }
    }

    @Override
    public void didWriteValue(RigLeBaseDevice device, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void discoveryDidComplete(RigLeBaseDevice device) {

    }
}
