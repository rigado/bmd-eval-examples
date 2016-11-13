package com.rigado.bmdeval.datasource;

import android.bluetooth.BluetoothProfile;
import android.support.annotation.NonNull;

import com.rigado.bmdeval.devicedata.otherdevices.DisconnectedDevice;
import com.rigado.bmdeval.devicedata.evaldemodevice.EvalDevice;
import com.rigado.rigablue.RigAvailableDeviceData;
import com.rigado.rigablue.RigCoreBluetooth;

public class DeviceRepository implements DeviceSource {

    private static DeviceRepository sSingleton = null;

    /**
     * Cached data
     */
    RigAvailableDeviceData data;
    EvalDevice evalDevice;

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
    public void saveConnectedDevice(@NonNull EvalDevice device) {
        this.evalDevice = device;
    }

    @Override
    public EvalDevice getConnectedDevice() {
        if (evalDevice == null) {
            evalDevice = new DisconnectedDevice();
        }
        return evalDevice;
    }

    @Override
    public boolean isDeviceConnected() {
        if (evalDevice == null
                || evalDevice instanceof DisconnectedDevice
                || evalDevice.getBaseDevice() == null) {
            return false;
        }

        return RigCoreBluetooth.getInstance()
                .getDeviceConnectionState(
                        evalDevice
                        .getBaseDevice()
                        .getBluetoothDevice()) == BluetoothProfile.STATE_CONNECTED;
    }
}