package com.rigado.bmd200eval.demodevice;

import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.NonNull;
import android.util.Log;

import com.rigado.bmd200eval.demodevice.devicedata.RgbColor;
import com.rigado.rigablue.RigFirmwareUpdateManager;
import com.rigado.rigablue.RigLeBaseDevice;

import java.io.InputStream;

/**
 * Implements Null Object Pattern for {@link DemoDevice}
 */
public class DisconnectedDevice extends DemoDevice implements IDemoDeviceActions {

    private static final String TAG = DisconnectedDevice.class.getSimpleName();

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
        Log.i(TAG, "startFirmwareUpdate");
    }

    @Override
    public boolean isUpdating() {
        return false;
    }

    @Override
    public void setUpdatingStatus(boolean isUpdating) {
        Log.i(TAG, "setUpdatingStatus " + isUpdating);
    }

    @Override
    public void setLedColor(RgbColor color) {
        Log.i(TAG, "setLedColor " + color);
    }

    @Override
    public RgbColor getLedColor() {
        return new RgbColor(0, 0, 0);
    }

    @Override
    public void setAmbLightNotificationsEnabled(boolean enabled) {
        Log.i(TAG, "setAmbientLightNotificationsEnabled " + enabled);
    }

    @Override
    public void startAmbientLightSensing() {
        Log.i(TAG, "startAmbientLightSensing");
    }

    @Override
    public void stopAmbientLightSensing() {
        Log.i(TAG, "stopAmbientLightSensing");
    }

    @Override
    public void setAccelNotificationsEnabled(boolean enabled) {
        Log.i(TAG, "setAccelNotificationsEnabled " + enabled);
    }

    @Override
    public void startAccelerometerStream() {
        Log.i(TAG, "startAccelerometerStream");
    }

    @Override
    public void stopAccelerometerStream() {
        Log.i(TAG, "stopAccelerometerStream");
    }

    @Override
    public void setButtonNotificationsEnabled(boolean enable) {
        Log.i(TAG, "setButtonNotificationsEnabled " + enable);
    }

}
