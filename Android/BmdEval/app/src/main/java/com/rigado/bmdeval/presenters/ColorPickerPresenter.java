package com.rigado.bmdeval.presenters;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.rigado.bmdeval.contracts.ColorPickerContract;
import com.rigado.bmdeval.datasource.DeviceRepository;
import com.rigado.bmdeval.demodevice.DemoDevice;
import com.rigado.bmdeval.demodevice.IDemoDeviceListener;
import com.rigado.bmdeval.demodevice.devicedata.RgbColor;

public class ColorPickerPresenter extends BasePresenter implements
        ColorPickerContract.UserActionsListener,
        IDemoDeviceListener.ReadWriteListener {

    private ColorPickerContract.View colorPickerView;
    private DemoDevice demoDevice;

    public ColorPickerPresenter(ColorPickerContract.View view) {
        this.colorPickerView = view;
        demoDevice =
                DeviceRepository
                        .getInstance()
                        .getConnectedDevice();
    }


    @Override
    public void onResume() {
        demoDevice.addReadWriteListener(this);
    }

    @Override
    public void onPause() {
        demoDevice.removeReadWriteListener(this);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onCharacteristicUpdate(BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onDescriptorUpdate(BluetoothGattDescriptor descriptor) {

    }

    @Override
    public void setLedColor(RgbColor color) {
        demoDevice.setLedColor(color);
    }
}
