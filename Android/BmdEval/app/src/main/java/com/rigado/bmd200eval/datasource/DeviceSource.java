package com.rigado.bmd200eval.datasource;

import android.support.annotation.NonNull;

import com.rigado.bmd200eval.demodevice.DemoDevice;
import com.rigado.rigablue.RigAvailableDeviceData;

public interface DeviceSource {

    void saveAvailableData(@NonNull RigAvailableDeviceData data);
    RigAvailableDeviceData getAvailableData();

    void saveConnectedDevice(@NonNull DemoDevice device);
    DemoDevice getConnectedDevice();
}