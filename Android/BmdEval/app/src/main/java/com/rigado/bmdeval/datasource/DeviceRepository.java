package com.rigado.bmdeval.datasource;

import android.support.annotation.NonNull;

import com.rigado.bmdeval.demodevice.DemoDevice;
import com.rigado.bmdeval.demodevice.DisconnectedDevice;
import com.rigado.rigablue.RigAvailableDeviceData;

public class DeviceRepository implements DeviceSource {

    private static DeviceRepository sSingleton = null;

    public String [] SCAN_UUID_LIST = {
            DemoDevice.BMDWARE_SERVICE_UUID,
            DemoDevice.BMDWARE_CONTROL_SERVICE_UUID,
            DemoDevice.BLINKY_SERVICE_UUID,
            DemoDevice.BMDEVAL_UUID_SERVICE,
            DemoDevice.BLINKY_ADVERTISING_SERVICE_UUID
    };

    /**
     * Cached data
     */
    RigAvailableDeviceData data;
    DemoDevice demoDevice;

    //Prevent direct instantiation
    private DeviceRepository() {
    }

    public static DeviceRepository getInstance() {
        if (sSingleton == null) {
            sSingleton = new DeviceRepository();
        }

        return sSingleton;
    }

    /**
     * Force a refresh
     */
    public void destroyCache() {
        sSingleton = null;
    }

    @Override
    public void saveAvailableData(@NonNull RigAvailableDeviceData data) {
        this.data = data;
    }

    @Override
    public RigAvailableDeviceData getAvailableData() {
        return data;
    }

    @Override
    public void saveConnectedDevice(@NonNull DemoDevice device) {
        this.demoDevice = device;
    }

    @Override
    public DemoDevice getConnectedDevice() {
        if (demoDevice == null) {
            demoDevice = new DisconnectedDevice();
        }
        return demoDevice;
    }
}