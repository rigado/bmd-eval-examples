package com.rigado.bmd200eval.demodevice;


import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.NonNull;

import com.rigado.bmd200eval.demodevice.devicedata.RgbColor;
import com.rigado.bmd200eval.interfaces.IDeviceListener;
import com.rigado.rigablue.RigFirmwareUpdateManager;
import com.rigado.rigablue.RigLeBaseDevice;

import java.io.InputStream;

public interface IDemoDeviceActions {
    void setDemoListener(IDeviceListener.DemoData listener);
    void setDiscoveryListener(IDeviceListener.DiscoveryListener listener);
    void setPasswordListener(IDeviceListener.PasswordListener listener);

    void startDemo();
    void stopDemo();

    void readCharacteristic(BluetoothGattCharacteristic characteristic);
    void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte [] value);
    void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable);

    void initServices();
    void runDiscovery();
    boolean hasNotifyProperty();
    void setControlPointNotificationsEnabled(boolean enabled);
    void requestBootloaderInformation();

    DemoDevice.FirmwareType getFirmwareType();
    RigLeBaseDevice getBaseDevice();
    boolean isConnected();
    void setConnected(boolean isConnected);
    void setType200(byte [] value);
    boolean is200();

    void startFirmwareUpdate(RigFirmwareUpdateManager manager, InputStream firmwareImage);
    boolean isUpdating();
    void setUpdatingStatus(boolean isUpdating);

    void setLedColor(RgbColor color);
    RgbColor getLedColor();

    void setAmbLightNotificationsEnabled(boolean enabled);
    void startAmbientLightSensing();
    void stopAmbientLightSensing();

    void setAccelNotificationsEnabled(boolean enabled);
    void startAccelerometerStream();
    void stopAccelerometerStream();

    void setButtonNotificationsEnabled(boolean enable);
}
