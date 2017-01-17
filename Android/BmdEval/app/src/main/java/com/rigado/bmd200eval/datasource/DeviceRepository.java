package com.rigado.bmd200eval.datasource;

import android.bluetooth.BluetoothProfile;
import android.support.annotation.NonNull;

import com.rigado.bmd200eval.demodevice.DemoDevice;
import com.rigado.bmd200eval.demodevice.DisconnectedDevice;
import com.rigado.rigablue.RigAvailableDeviceData;
import com.rigado.rigablue.RigCoreBluetooth;

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

    @Override
    public boolean isDeviceConnected() {
        if (demoDevice == null
                || demoDevice instanceof DisconnectedDevice
                || demoDevice.getBaseDevice() == null) {
            return false;
        }

        return RigCoreBluetooth.getInstance()
                .getDeviceConnectionState(demoDevice.getBaseDevice().getBluetoothDevice())
                == BluetoothProfile.STATE_CONNECTED;
    }
}