package com.rigado.bmdeval.devicedata.otherdevices;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;

import com.rigado.bmdeval.devicedata.IDeviceListener;
import com.rigado.bmdeval.devicedata.evaldemodevice.EvalDevice;
import com.rigado.bmdeval.devicedata.evaldemodevice.RgbColor;
import com.rigado.rigablue.RigLeBaseDevice;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DisconnectedDevice extends EvalDevice {

    public DisconnectedDevice(RigLeBaseDevice device) {
        super(device);
    }

    /**
     * Convenience method to instantiate a new {@code DisconnectedDevice} object
     */
    public DisconnectedDevice() {
        this(null);
    }

    @Override
    public void addReadWriteListener(
            @NonNull IDeviceListener.ReadWriteListener readWriteListener) {
    }

    @Override
    public void removeReadWriteListener(
            @NonNull IDeviceListener.ReadWriteListener readWriteListener) {
    }

    @Override
    public void addNotifyListener(@NonNull IDeviceListener.NotifyListener notifyListener) {

    }

    @Override
    public void removeNotifyListener(@NonNull IDeviceListener.NotifyListener notifyListener) {

    }

    @Override
    public void addFirmwareUpdateListener(
            @NonNull IDeviceListener.FirmwareUpdateListener firmwareUpdateListener) {

    }

    @Override
    public void removeFirmwareUpdateListener(
            @NonNull IDeviceListener.FirmwareUpdateListener firmwareUpdateListener) {

    }

    @Override
    public void addDiscoveryListener(@NonNull IDeviceListener.DiscoveryListener listener) {

    }

    @Override
    public void removeDiscoveryListener(@NonNull IDeviceListener.DiscoveryListener listener) {

    }

    //region RIGLEBASEDEVICE_INHERITED_METHODS
    @Override
    public boolean readDescriptor(BluetoothGattDescriptor descriptor) {
        return false;
   }

    @Override
    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        return false;
    }

    @Override
    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        return false;
    }

    @Override
    public void toggleNotifications(BluetoothGattCharacteristic characteristic, boolean enable) {
    }

    @Override
    public boolean isDiscoveryComplete() {
        return true;
    }

    @Override
    public void runDiscovery() {
    }

    @Override
    public void setDiscoveryComplete(boolean isComplete) {

    }

    @Override
    public RigLeBaseDevice getBaseDevice() {
        return null;
    }

    @Override
    public BluetoothDevice getBluetoothDevice() {
        return null;
    }

    @Override
    public String getName() {
        return "Unknown Device";
    }

    @Override
    public List<BluetoothGattService> getServiceList() {
        return new ArrayList<>();
    }

    @Override
    public byte[] getScanRecord() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public String[] getScanFilter() {
        return new String[0];
    }

    @Override
    public void requestBootloaderInformation() {

    }

    @Override
    public void startFirmwareUpdate(InputStream inputStream) {

    }

    @Override
    public void cancelFirmwareUpdate() {

    }

    @Override
    public void requestHardwareVersion() {

    }

    @Override
    public void setLedColor(RgbColor color) {

    }

    @Override
    public RgbColor getLedColor() {
        return null;
    }

    @Override
    public void startAmbientLightSensing() {

    }

    @Override
    public void stopAmbientLightSensing() {

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

    //endregion
}
