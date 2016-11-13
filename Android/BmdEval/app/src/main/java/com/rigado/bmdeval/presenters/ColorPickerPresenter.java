package com.rigado.bmdeval.presenters;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.rigado.bmdeval.contracts.ColorPickerContract;
import com.rigado.bmdeval.datasource.DeviceRepository;
import com.rigado.bmdeval.devicedata.IDeviceListener;
import com.rigado.bmdeval.devicedata.evaldemodevice.EvalDevice;
import com.rigado.bmdeval.devicedata.evaldemodevice.RgbColor;

public class ColorPickerPresenter extends BasePresenter implements
        ColorPickerContract.UserActionsListener,
        IDeviceListener.ReadWriteListener {

    private ColorPickerContract.View colorPickerView;
    private EvalDevice evalDevice;

    public ColorPickerPresenter(ColorPickerContract.View view) {
        this.colorPickerView = view;
        evalDevice =
                DeviceRepository
                        .getInstance()
                        .getConnectedDevice();
    }


    @Override
    public void onResume() {
        evalDevice.addReadWriteListener(this);
    }

    @Override
    public void onPause() {
        evalDevice.removeReadWriteListener(this);
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
        //Turn LED on if off & set color
    }

    @Override
    public void setLedEnabled(boolean enabled) {
        //Turn LED on or off
    }
}
