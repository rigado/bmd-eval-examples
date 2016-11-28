package com.rigado.bmdeval.presenters;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import com.rigado.bmdeval.contracts.DemoContract;
import com.rigado.bmdeval.datasource.DeviceRepository;
import com.rigado.bmdeval.demodevice.DemoDevice;
import com.rigado.bmdeval.demodevice.IDemoDeviceListener;
import com.rigado.bmdeval.demodevice.devicedata.AccelData;
import com.rigado.bmdeval.demodevice.devicedata.AmbientLight;
import com.rigado.bmdeval.demodevice.devicedata.ButtonStatus;

import java.util.UUID;

public class DemoPresenter extends BasePresenter implements
        IDemoDeviceListener.ReadWriteListener,
        IDemoDeviceListener.NotifyListener {

    private static final String TAG = DemoPresenter.class.getSimpleName();

    private DemoContract.View demoView;
    private DemoDevice demoDevice;

    public DemoPresenter(DemoContract.View view) {
        this.demoView = view;
        demoDevice =
                DeviceRepository
                .getInstance()
                .getConnectedDevice();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        demoDevice.addReadWriteListener(this);
        demoDevice.addNotifyListener(this);
        demoDevice.setButtonNotificationsEnabled(true);
        demoDevice.setAccelNotificationsEnabled(true);
        demoDevice.setAmbLightNotificationsEnabled(true);
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        demoDevice.removeReadWriteListener(this);
        demoDevice.removeNotifyListener(this);
        demoDevice.setButtonNotificationsEnabled(false);
        demoDevice.setAccelNotificationsEnabled(false);
        demoDevice.setAmbLightNotificationsEnabled(false);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic) {
    }

    @Override
    public void onCharacteristicUpdate(BluetoothGattCharacteristic characteristic) {
        final byte [] value = characteristic.getValue();

        if (value == null) {
            return;
        }

        if (characteristic.getUuid().equals(UUID.fromString(
                DemoDevice.BMDEVAL_UUID_BUTTON_CHAR))) {
            ButtonStatus status = new ButtonStatus(value[0]);
            demoView.updateButtonStatus(status);

        } else if (characteristic.getUuid().equals(UUID.fromString(
                DemoDevice.BMDEVAL_UUID_ADC_CHAR))) {
            AmbientLight lightLevel = new AmbientLight(value[0]);
            demoView.updateAmbientLight(lightLevel);

        } else if (characteristic.getUuid().equals(UUID.fromString(
                DemoDevice.BMDEVAL_UUID_ACCEL_CHAR))) {
            AccelData accelData = new AccelData(value[0], value[1], value[2]);
            demoView.updateAccelStream(accelData);
        } else if (characteristic.getUuid().equals(UUID.fromString(
                DemoDevice.BMDEVAL_UUID_LED_CHAR))) {
            //RgbColor color = new RgbColor(value)
        }
    }

    @Override
    public void onDescriptorUpdate(BluetoothGattDescriptor descriptor) {

    }

    @Override
    public void onCharacteristicStateChange(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUID.fromString(
                DemoDevice.BMDEVAL_UUID_BUTTON_CHAR))) {
            Log.i(TAG, "Button notifications enabled");

        } else if (characteristic.getUuid().equals(UUID.fromString(
                DemoDevice.BMDEVAL_UUID_ACCEL_CHAR))) {
            Log.i(TAG, "Accel notifications enabled");
            demoDevice.startAccelerometerStream();

        } else if (characteristic.getUuid().equals(UUID.fromString(
                DemoDevice.BMDEVAL_UUID_ADC_CHAR))) {
            Log.i(TAG, "Ambient Light notifications enabled");
            demoDevice.startAmbientLightSensing();
        }
    }
}
