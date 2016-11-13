package com.rigado.bmdeval.devicedata.otherdevices;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;

import com.rigado.bmdeval.devicedata.IDeviceListener;
import com.rigado.rigablue.IRigFirmwareUpdateManagerObserver;
import com.rigado.rigablue.IRigLeBaseDeviceObserver;
import com.rigado.rigablue.IRigLeDescriptorObserver;
import com.rigado.rigablue.RigDfuError;
import com.rigado.rigablue.RigLeBaseDevice;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Base device methods
 */
public class BleDevice implements
        IBleDeviceActions,
        IRigLeBaseDeviceObserver,
        IRigLeDescriptorObserver,
        IRigFirmwareUpdateManagerObserver {

    private static final String TAG = BleDevice.class.getSimpleName();

    public static final String DIS_UUID_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final String DIS_UUID_MODEL_NUM = "00002a24-0000-1000-8000-00805f9b34fb";
    public static final String DIS_UUID_SERIAL_NUM = "00002a25-0000-1000-8000-00805f9b34fb";
    public static final String DIS_UUID_FIRMWARE_VER = "00002a26-0000-1000-8000-00805f9b34fb";
    public static final String DIS_UUID_MFG_NAME = "00002a29-0000-1000-8000-00805f9b34fb";

    private String [] SCAN_UUID_LIST = {
            "2413b33f-707f-90bd-0245-2ab8807571b7",
            "6e400001-b5a3-f393-e0a9-e50e24dcca9e",
            "50db1523-418d-4690-9589-ab7be9e22684",
            "0000180f-0000-1000-8000-00805f9b34fb"
    };


    private RigLeBaseDevice baseDevice;
    private boolean is200Device;

    private List<IDeviceListener.ReadWriteListener> readWriteListeners;
    private List<IDeviceListener.DiscoveryListener> discoveryListeners;
    private List<IDeviceListener.NotifyListener> notifyListeners;
    private List<IDeviceListener.FirmwareUpdateListener> firmwareUpdateListeners;

    public BleDevice(RigLeBaseDevice device) {
        if (device == null) {
            return;
        }

        //Assume we have a 200 board until notified otherwise
        this.is200Device = true;

        this.baseDevice = device;
        baseDevice.setDescriptorObserver(this);
        baseDevice.setObserver(this);

        readWriteListeners = new ArrayList<>();
        notifyListeners = new ArrayList<>();
        firmwareUpdateListeners = new ArrayList<>();
        discoveryListeners = new ArrayList<>();
    }

    //region DEVICE_OBSERVERS
    @Override
    public void addReadWriteListener(
            @NonNull IDeviceListener.ReadWriteListener readWriteListener) {
        if (!readWriteListeners.contains(readWriteListener)) {
            readWriteListeners.add(readWriteListener);
        }
    }

    @Override
    public void removeReadWriteListener(
            @NonNull IDeviceListener.ReadWriteListener readWriteListener) {
        if (readWriteListeners.contains(readWriteListener)) {
            readWriteListeners.remove(readWriteListener);
        }
    }

    @Override
    public void addNotifyListener(@NonNull IDeviceListener.NotifyListener notifyListener) {
        if (!notifyListeners.contains(notifyListener)) {
            notifyListeners.add(notifyListener);
        }
    }

    @Override
    public void removeNotifyListener(@NonNull IDeviceListener.NotifyListener notifyListener) {
        if (notifyListeners.contains(notifyListener)) {
            notifyListeners.remove(notifyListener);
        }
    }

    @Override
    public void addFirmwareUpdateListener(
            @NonNull IDeviceListener.FirmwareUpdateListener firmwareUpdateListener) {
        if (!firmwareUpdateListeners.contains(firmwareUpdateListener)) {
            firmwareUpdateListeners.add(firmwareUpdateListener);
        }
    }

    @Override
    public void removeFirmwareUpdateListener(
            @NonNull IDeviceListener.FirmwareUpdateListener firmwareUpdateListener) {
        if (firmwareUpdateListeners.contains(firmwareUpdateListener)) {
            firmwareUpdateListeners.remove(firmwareUpdateListener);
        }
    }

    @Override
    public void addDiscoveryListener(@NonNull IDeviceListener.DiscoveryListener listener) {
        if (!discoveryListeners.contains(listener)) {
            discoveryListeners.add(listener);
        }
    }

    @Override
    public void removeDiscoveryListener(@NonNull IDeviceListener.DiscoveryListener listener) {
        if (discoveryListeners.contains(listener)) {
            discoveryListeners.remove(listener);
        }
    }
    //endregion

    //region RIGLEBASEDEVICE_INHERITED_METHODS
    @Override
    public boolean readDescriptor(BluetoothGattDescriptor descriptor) {
        return baseDevice.readDescriptor(descriptor);
    }

    @Override
    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        return baseDevice.readCharacteristic(characteristic);
    }

    @Override
    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        return baseDevice.writeCharacteristic(characteristic, value);
    }

    @Override
    public void toggleNotifications(BluetoothGattCharacteristic characteristic, boolean enable) {
        baseDevice.setCharacteristicNotification(characteristic, enable);
    }

    @Override
    public boolean isDiscoveryComplete() {
        return baseDevice.isDiscoveryComplete();
    }

    @Override
    public void runDiscovery() {
        baseDevice.runDiscovery();
    }

    @Override
    public void setDiscoveryComplete(boolean isComplete) {
        baseDevice.setDiscoveryComplete();
    }

    @Override
    public RigLeBaseDevice getBaseDevice() {
        return baseDevice;
    }

    @Override
    public BluetoothDevice getBluetoothDevice() {
        return baseDevice.getBluetoothDevice();
    }

    @Override
    public String getName() {
        return baseDevice.getName();
    }

    @Override
    public List<BluetoothGattService> getServiceList() {
        return baseDevice.getServiceList();
    }

    @Override
    public byte[] getScanRecord() {
        return baseDevice.getScanRecord();
    }

    @Override
    public void setType200(boolean is200) {
        this.is200Device = is200;
    }

    @Override
    public boolean is200() {
        return this.is200Device;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public String[] getScanFilter() {
        return SCAN_UUID_LIST;
    }

    @Override
    public void requestBootloaderInformation() {

    }

    @Override
    public void startFirmwareUpdate(InputStream inputStream) {
        //TODO : FAILURE UNKNOWN DEVICE

    }

    @Override
    public void cancelFirmwareUpdate() {

    }

    @Override
    public void requestHardwareVersion() {

    }

    //endregion

    //region IRIG_CALLBACKS
    @Override
    public void didUpdateValue(RigLeBaseDevice device,
                               BluetoothGattCharacteristic characteristic) {
        for (IDeviceListener.ReadWriteListener listener : readWriteListeners) {
            listener.onCharacteristicUpdate(characteristic);
        }
    }

    @Override
    public void didUpdateNotifyState(RigLeBaseDevice device,
                                     BluetoothGattCharacteristic characteristic) {
        for (IDeviceListener.NotifyListener listener : notifyListeners) {
            listener.onCharacteristicStateChange(characteristic);
        }
    }

    @Override
    public void didWriteValue(RigLeBaseDevice device,
                              BluetoothGattCharacteristic characteristic) {
        for (IDeviceListener.ReadWriteListener listener : readWriteListeners) {
            listener.onCharacteristicWrite(characteristic);
        }
    }

    @Override
    public void discoveryDidComplete(RigLeBaseDevice device) {
        for (IDeviceListener.DiscoveryListener listener : discoveryListeners) {
            listener.onServicesDiscovered();
        }
    }

    @Override
    public void didReadDescriptor(RigLeBaseDevice device, BluetoothGattDescriptor descriptor) {
        for (IDeviceListener.ReadWriteListener listener : readWriteListeners) {
            listener.onDescriptorUpdate(descriptor);
        }
    }
    //endregion

    //region FIRMWARE_UPDATE
    @Override
    public void updateProgress(int progress) {
        for (IDeviceListener.FirmwareUpdateListener listener : firmwareUpdateListeners) {
            listener.onReceiveProgress(progress);
        }
    }

    @Override
    public void updateStatus(String status, int error) {
        for (IDeviceListener.FirmwareUpdateListener listener : firmwareUpdateListeners) {
            listener.onReceiveStatus(status);
        }
    }

    @Override
    public void didFinishUpdate() {
        for (IDeviceListener.FirmwareUpdateListener listener : firmwareUpdateListeners) {
            listener.onUpdateCompleted();
        }
    }

    @Override
    public void updateFailed(RigDfuError error) {
        for (IDeviceListener.FirmwareUpdateListener listener : firmwareUpdateListeners) {
            listener.onUpdateFailed(error);
        }
    }
    //endregion
}
