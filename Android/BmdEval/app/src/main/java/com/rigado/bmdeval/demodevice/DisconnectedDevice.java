package com.rigado.bmdeval.demodevice;

import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.NonNull;
import android.util.Log;

import com.rigado.bmdeval.demodevice.devicedata.RgbColor;
import com.rigado.rigablue.RigFirmwareUpdateManager;
import com.rigado.rigablue.RigLeBaseDevice;

import java.io.InputStream;

/**
 * Implements Null Object Pattern for {@link DemoDevice}
 */
public class DisconnectedDevice extends DemoDevice implements IDemoDeviceActions {

    public DisconnectedDevice(RigLeBaseDevice device) {
        super(device);
    }

    /**
     * Convenience method to instantiate a new {@code DisconnectedDevice} object
     */
    public DisconnectedDevice() {
        this(null);

        Log.i("DisconnectedDevice", "new DisconnectedDevice");
    }

    //region DEVICE_LISTENERS
    @Override
    public void addReadWriteListener(
            @NonNull IDemoDeviceListener.ReadWriteListener readWriteListener) {
    }

    @Override
    public void removeReadWriteListener(
            @NonNull IDemoDeviceListener.ReadWriteListener readWriteListener) {
    }

    @Override
    public void addNotifyListener(@NonNull IDemoDeviceListener.NotifyListener notifyListener) {

    }

    @Override
    public void removeNotifyListener(@NonNull IDemoDeviceListener.NotifyListener notifyListener) {

    }

    @Override
    public void addDiscoveryListener(@NonNull IDemoDeviceListener.DiscoveryListener listener) {

    }

    @Override
    public void removeDiscoveryListener(@NonNull IDemoDeviceListener.DiscoveryListener listener) {

    }
    //endregion


    @Override
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void initServices() {

    }

    @Override
    public void runDiscovery() {

    }

    @Override
    public void setControlPointNotificationsEnabled(boolean enabled) {

    }

    @Override
    public void requestBootloaderInformation() {

    }

    @Override
    public DemoDevice.FirmwareType getFirmwareType() {
        return FirmwareType.Disconnected;
    }
    public RigLeBaseDevice getBaseDevice() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void setConnected(boolean isConnected) {

    }

    @Override
    public void setType200(byte [] value) {

    }

    @Override
    public boolean is200() {
        return false;
    }

    @Override
    public void startFirmwareUpdate(RigFirmwareUpdateManager manager, InputStream inputStream) {

    }

    @Override
    public boolean isUpdating() {
        return false;
    }

    @Override
    public void setUpdatingStatus(boolean isUpdating) {

    }

    @Override
    public void setLedColor(RgbColor color) {

    }

    @Override
    public RgbColor getLedColor() {
        return new RgbColor(0, 0, 0);
    }

    @Override
    public void setAmbLightNotificationsEnabled(boolean enabled) {

    }

    @Override
    public void startAmbientLightSensing() {

    }

    @Override
    public void stopAmbientLightSensing() {

    }

    @Override
    public void setAccelNotificationsEnabled(boolean enabled) {

    }

    @Override
    public void startAccelerometerStream() {

    }

    @Override
    public void stopAccelerometerStream() {

    }

    @Override
    public void setButtonNotificationsEnabled(boolean enable) {

    }

}
