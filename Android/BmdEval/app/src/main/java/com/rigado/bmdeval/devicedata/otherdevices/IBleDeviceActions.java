package com.rigado.bmdeval.devicedata.otherdevices;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;

import com.rigado.bmdeval.devicedata.IDeviceListener;
import com.rigado.rigablue.RigLeBaseDevice;

import java.io.InputStream;
import java.util.List;

public interface IBleDeviceActions {
    void addReadWriteListener(@NonNull IDeviceListener.ReadWriteListener readWriteListener);
    void removeReadWriteListener(@NonNull IDeviceListener.ReadWriteListener readWriteListener);
    void addNotifyListener(@NonNull IDeviceListener.NotifyListener notifyListener);
    void removeNotifyListener(@NonNull IDeviceListener.NotifyListener notifyListener);
    void addFirmwareUpdateListener(
            @NonNull IDeviceListener.FirmwareUpdateListener firmwareUpdateListener);
    void removeFirmwareUpdateListener(
            @NonNull IDeviceListener.FirmwareUpdateListener firmwareUpdateListener);
    void addDiscoveryListener(@NonNull IDeviceListener.DiscoveryListener listener);
    void removeDiscoveryListener(@NonNull IDeviceListener.DiscoveryListener listener);

    boolean readDescriptor(BluetoothGattDescriptor descriptor);
    boolean readCharacteristic(BluetoothGattCharacteristic characteristic);
    boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte [] value);
    void toggleNotifications(BluetoothGattCharacteristic characteristic, boolean enable);

    boolean isDiscoveryComplete();
    void runDiscovery();
    void setDiscoveryComplete(boolean isComplete);

    RigLeBaseDevice getBaseDevice();
    BluetoothDevice getBluetoothDevice();
    String getName();
    List<BluetoothGattService> getServiceList();
    byte [] getScanRecord();
    boolean isConnected();

    void setType200(boolean is200);
    boolean is200();

    String [] getScanFilter();

    void requestBootloaderInformation();

    void startFirmwareUpdate(InputStream firmwareImage);
    void cancelFirmwareUpdate();

    void requestHardwareVersion();
}
