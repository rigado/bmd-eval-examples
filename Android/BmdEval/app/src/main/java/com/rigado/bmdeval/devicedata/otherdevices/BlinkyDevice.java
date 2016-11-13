package com.rigado.bmdeval.devicedata.otherdevices;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.rigado.bmdeval.devicedata.evaldemodevice.EvalDevice;
import com.rigado.bmdeval.devicedata.evaldemodevice.RgbColor;
import com.rigado.rigablue.RigFirmwareUpdateManager;
import com.rigado.rigablue.RigLeBaseDevice;

import java.io.InputStream;
import java.util.UUID;

public class BlinkyDevice extends EvalDevice {

    private final static byte [] FIRMWARE_UPDATE_COMMAND = { -104, -74, 0x2f, 0x51 };
    public static final String BLINKY_UUID_CTRL_CHAR = "6d580002-fc91-486b-82c4-86a1d2eb8f88";
    public static final String BLINKY_SERVICE_UUID = "6d580001-fc91-486b-82c4-86a1d2eb8f88";

    private RigFirmwareUpdateManager rigFirmwareUpdateManager;
    private BluetoothGattService blinkyService;

    public BlinkyDevice(RigLeBaseDevice device) {
        super(device);

        for (BluetoothGattService service : device.getServiceList()) {
            if (service.getUuid().equals(UUID.fromString(BLINKY_SERVICE_UUID))) {
                this.blinkyService = service;
            }
        }
    }

    @Override
    public void startFirmwareUpdate(InputStream inputStream) {
        if (blinkyService == null) {
            //TODO : FAILURE UNKNOWN DEVICE
            return;
        }

        final BluetoothGattCharacteristic activateChar =
                blinkyService.getCharacteristic(UUID.fromString(BLINKY_UUID_CTRL_CHAR));

        if (activateChar == null) {
            //TODO : FAILURE UNKNOWN DEVICE / CACHED CHARACTERISTICS
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

    //TODO : return failure BLINKY_DEVICE

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
