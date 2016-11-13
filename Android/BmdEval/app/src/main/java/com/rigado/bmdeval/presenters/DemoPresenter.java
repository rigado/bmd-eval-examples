package com.rigado.bmdeval.presenters;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.rigado.bmdeval.contracts.DemoContract;
import com.rigado.bmdeval.datasource.DeviceRepository;
import com.rigado.bmdeval.devicedata.IDeviceListener;
import com.rigado.bmdeval.devicedata.evaldemodevice.AccelData;
import com.rigado.bmdeval.devicedata.evaldemodevice.AmbientLight;
import com.rigado.bmdeval.devicedata.evaldemodevice.ButtonStatus;
import com.rigado.bmdeval.devicedata.evaldemodevice.EvalDevice;

import java.util.UUID;

public class DemoPresenter extends BasePresenter implements
        IDeviceListener.ReadWriteListener,
        IDeviceListener.NotifyListener {

    private DemoContract.View demoView;
    private EvalDevice evalDevice;

    public DemoPresenter(DemoContract.View view) {
        this.demoView = view;
        evalDevice =
                DeviceRepository
                .getInstance()
                .getConnectedDevice();
    }

    @Override
    public void onResume() {
        evalDevice.setButtonNotificationsEnabled(true);
        evalDevice.startAccelerometerStream();
        evalDevice.startAmbientLightSensing();
        evalDevice.addReadWriteListener(this);
        evalDevice.addNotifyListener(this);
    }

    @Override
    public void onPause() {
        evalDevice.setButtonNotificationsEnabled(false);
        evalDevice.stopAccelerometerStream();
        evalDevice.stopAmbientLightSensing();
        evalDevice.removeReadWriteListener(this);
        evalDevice.removeNotifyListener(this);
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
                EvalDevice.BMDEVAL_UUID_BUTTON_CHAR))) {
            ButtonStatus status = new ButtonStatus(value[0]);
            demoView.updateButtonStatus(status);

        } else if (characteristic.getUuid().equals(UUID.fromString(
                EvalDevice.BMDEVAL_UUID_ADC_CHAR))) {
            AmbientLight lightLevel = new AmbientLight(value[0]);
            demoView.updateAmbientLight(lightLevel);

        } else if (characteristic.getUuid().equals(UUID.fromString(
                EvalDevice.BMDEVAL_UUID_ACCEL_CHAR))) {
            AccelData accelData = new AccelData(value[0], value[1], value[2]);
            demoView.updateAccelStream(accelData);
        }
    }

    @Override
    public void onDescriptorUpdate(BluetoothGattDescriptor descriptor) {

    }

    @Override
    public void onCharacteristicStateChange(BluetoothGattCharacteristic characteristic) {

    }
}
