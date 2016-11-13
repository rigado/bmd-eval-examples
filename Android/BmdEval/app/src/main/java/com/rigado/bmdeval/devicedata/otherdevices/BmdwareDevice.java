package com.rigado.bmdeval.devicedata.otherdevices;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.rigado.bmdeval.devicedata.evaldemodevice.EvalDevice;
import com.rigado.bmdeval.devicedata.evaldemodevice.RgbColor;
import com.rigado.rigablue.RigFirmwareUpdateManager;
import com.rigado.rigablue.RigLeBaseDevice;

import java.io.InputStream;
import java.util.UUID;

public class BmdwareDevice extends EvalDevice {

    public static final String TAG = BmdwareDevice.class.getSimpleName();

    private RigFirmwareUpdateManager rigFirmwareUpdateManager;
    private BluetoothGattService bmdwareService;

    public static final String BMDWARE_SERVICE_UUID = "2413B33F-707F-90BD-2045-2AB8807571B7";
    public static final String BMDWARE_CTRL_POINT_UUID = "2413B43F-707F-90BD-2045-2AB8807571B7";

    public static final byte [] FIRMWARE_UPDATE_COMMAND = { 0x03, 0x56, 0x30, 0x57 };
    public static final byte [] BOOTLOADER_VERSION_COMMAND = { 0x60 };

    public BmdwareDevice(RigLeBaseDevice device) {
        super(device);

        for (BluetoothGattService service : device.getServiceList()) {
            if (service.getUuid().equals(UUID.fromString(BMDWARE_SERVICE_UUID))) {
                this.bmdwareService = service;
            }
        }

    }

    @Override
    public void startFirmwareUpdate(InputStream inputStream) {
        if (bmdwareService == null) {
            return;
        }

        final BluetoothGattCharacteristic activateChar =
                bmdwareService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));

        if (activateChar == null) {
            return;
        }

        rigFirmwareUpdateManager = new RigFirmwareUpdateManager();
        rigFirmwareUpdateManager.updateFirmware(
                getBaseDevice(),
                inputStream,
                activateChar,
                FIRMWARE_UPDATE_COMMAND);
    }

    @Override
    public void cancelFirmwareUpdate() {
        if (rigFirmwareUpdateManager == null) {
            return;
        }

        rigFirmwareUpdateManager.cancelUpdate();
    }

    @Override
    public void requestBootloaderInformation() {

    }

    @Override
    public void requestHardwareVersion() {

    }

    //TODO : return failure BMDWARE_DEVICE

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
}
