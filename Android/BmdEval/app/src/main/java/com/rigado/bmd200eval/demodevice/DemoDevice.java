package com.rigado.bmd200eval.demodevice;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;
import android.util.Log;

import com.rigado.bmd200eval.datasource.DeviceRepository;
import com.rigado.bmd200eval.demodevice.devicedata.BootloaderInfo;
import com.rigado.bmd200eval.demodevice.devicedata.RgbColor;
import com.rigado.bmd200eval.interfaces.IDeviceListener;
import com.rigado.rigablue.IRigFirmwareUpdateManagerObserver;
import com.rigado.rigablue.IRigLeBaseDeviceObserver;
import com.rigado.rigablue.IRigLeDescriptorObserver;
import com.rigado.rigablue.RigDfuError;
import com.rigado.rigablue.RigFirmwareUpdateManager;
import com.rigado.rigablue.RigLeBaseDevice;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Convenience object used to interact with the main eval demo firmware
 * as well as provide secure updates to {@link FirmwareType#Blinky} and
 * {@link FirmwareType#EvalDemo}. See {@link DisconnectedDevice} for
 * the Null Object Pattern implementation.
 */
public class DemoDevice implements
        IDemoDeviceActions {

    private static final String TAG = DemoDevice.class.getSimpleName();

    /**
     * There are three different firmwares used in this demo.
     * {@link #EvalDemo} is the main demo firmware.
     * {@link #Blinky} is a simple firmware that causes the RGB Led to blink
     * while advertising. Advertisement stops after 20 seconds.
     * {@link #Bmdware} is the firmware shipped on all Rigado boards. It is included
     * so that the user has the option to revert back after using the demo firmware.
     *
     * On connection, if the firmware is type {@link #Blinky} or type {@link #Bmdware},
     * the user is given the option to update to {@link #EvalDemo}
     */
    public enum FirmwareType {
        EvalDemo("BMD Eval Demo"),
        Blinky("Blinky Demo"),
        Bmdware("BMDware"),
        Disconnected("Disconnected");

        String description;

        FirmwareType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }

    /**
     * Device Information Service UUIDs
     */
    public static final String DIS_UUID_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final String DIS_UUID_MODEL_NUM = "00002a24-0000-1000-8000-00805f9b34fb";
    public static final String DIS_UUID_SERIAL_NUM = "00002a25-0000-1000-8000-00805f9b34fb";
    public static final String DIS_UUID_FIRMWARE_VER = "00002a26-0000-1000-8000-00805f9b34fb";
    public static final String DIS_UUID_MFG_NAME = "00002a29-0000-1000-8000-00805f9b34fb";

    /**
     * BMD Eval Demo UUIDs
     */
    public static final String BMDEVAL_UUID_SERVICE = "50db1523-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_BUTTON_CHAR = "50db1524-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_LED_CHAR = "50db1525-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_ADC_CHAR = "50db1526-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_CTRL_CHAR = "50db1527-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_ACCEL_CHAR = "50db1528-418d-4690-9589-ab7be9e22684";

    /**
     * Start and Stop Ambient Light Streaming by sending these commands to
     * {@link #BMDEVAL_UUID_CTRL_CHAR}
     */
    public static final byte ADC_STREAM_START = 0x01;
    public static final byte ADC_STREAM_STOP = 0x02;
    /**
     * Start and stop Accelerometer Streaming by sending these commands to
     * {@link #BMDEVAL_UUID_CTRL_CHAR}
     */
    public static final byte ACCEL_STREAM_START = 0x06;
    public static final byte ACCEL_STREAM_STOP = 0x09;

    /**
     * Receive the bootloader information by sending this command to
     * {@link #BMDEVAL_UUID_CTRL_CHAR}. See {@code requestBootloaderInformation()}
     */
    public static final byte [] EVAL_HARDDWARE_VERSION_COMMAND = { 0x0A };

    /**
     * Start the firmware update for {@link FirmwareType#EvalDemo} with this data.
     */
    public static final byte [] BMD_EVAL_ENTER_BOOTLOADER_COMMAND = { -95, -4, -42, -25 };

    /**
     * Blinky UUIDs
     */
    public static final String BLINKY_ADVERTISING_SERVICE_UUID = "0000180f-0000-1000-8000-00805f9b34fb";
    public static final String BLINKY_SERVICE_UUID = "6d580001-fc91-486b-82c4-86a1d2eb8f88";
    public static final String BLINKY_UUID_CTRL_CHAR = "6d580002-fc91-486b-82c4-86a1d2eb8f88";

    /**
     * Receive the bootloader information by sending this command to
     * {@link #BLINKY_UUID_CTRL_CHAR}
     */
    public static final byte[] BLINKY_HARDWARE_VERSION_COMMAND = { 0x0A };

    /**
     * Start the firmware update for {@link FirmwareType#Blinky} with this data.
     */
    private static final byte [] BLINKY_ENTER_BOOTLOADER_COMMAND = { -104, -74, 0x2f, 0x51 };

    /**
     * BMDware UUIDs
     */
    public static final String BMDWARE_CONTROL_SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String BMDWARE_SERVICE_UUID = "2413B33F-707F-90BD-2045-2AB8807571B7";
    public static final String BMDWARE_CTRL_POINT_UUID = "2413B43F-707F-90BD-2045-2AB8807571B7";

    /**
     * Receive the bootloader information by sending this command to
     * {@link #BMDWARE_CTRL_POINT_UUID}
     */
    public static final byte [] BMDWARE_HARDWARE_VERSION_COMMAND = { 0x60 };
    /**
     * Start the firmware update for {@link FirmwareType#Bmdware} with this data.
     */
    public static final byte [] BMDWARE_ENTER_BOOTLOADER_COMMAND = { 0x03, 0x56, 0x30, 0x57 };


    /**
     * {@link #BMDWARE_CTRL_POINT_UUID} Command Response Codes
     */
    public static final byte [] COMMAND_SUCCESS = { 0x00 };
    public static final byte [] DEVICE_LOCKED = { 0x01 };
    public static final byte [] COMMAND_INVALID_LENGTH = { 0x02 };
    public static final byte [] UNLOCK_FAILED = { 0x03 };
    public static final byte [] UPDATE_PIN_FAILED = { 0x04 };
    /**
     * Return value if BMDware protocol version is < 2.
     * See {@link #setType200(byte[])}
     */
    public static final byte [] INVALID_DATA = { 0x05 };
    public static final byte [] INVALID_STATE = { 0x06 };
    public static final byte [] INVALID_PARAMETER = { 0x07 };
    public static final byte [] INVALID_COMMAND = { 0x08 };


    private FirmwareType firmwareType;
    private RigLeBaseDevice baseDevice;
    private RigFirmwareUpdateManager rigFirmwareUpdateManager;
    private boolean is200Device;
    private boolean isUpdating;
    private boolean isConnected;

    /**
     * Store a reference after receiving device type
     */
    private BluetoothGattService gattService;
    private byte [] hardwareVersionCommand;
    private byte [] enterBootloaderCommand;
    private BluetoothGattCharacteristic controlPointCharacteristic;

    private int numberOfEnabledNotifs;

    private IDeviceListener.DemoData deviceListener;
    private IDeviceListener.DiscoveryListener discoveryListener;
    private IDeviceListener.PasswordListener passwordListener;

    private IRigLeBaseDeviceObserver baseDeviceObserver = new IRigLeBaseDeviceObserver() {

        @Override
        public void didUpdateValue(RigLeBaseDevice device,
                                   BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(UUID.fromString(BMDEVAL_UUID_BUTTON_CHAR))) {
                if (deviceListener != null) {
                    deviceListener.onReceiveButtonData(characteristic.getValue());
                }
            } else if (characteristic.getUuid().equals(UUID.fromString(BMDEVAL_UUID_ADC_CHAR))) {
                if (deviceListener != null) {
                    deviceListener.onReceiveAmbientLightData(characteristic.getValue());
                }
            } else if (characteristic.getUuid().equals(UUID.fromString(BMDEVAL_UUID_ACCEL_CHAR))) {
                if (deviceListener != null) {
                    deviceListener.onReceiveAccelerometerData(characteristic.getValue());
                }
            } else if (characteristic.getUuid().equals(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR))
                    || characteristic.getUuid().equals(UUID.fromString(BMDWARE_CTRL_POINT_UUID))
                    || characteristic.getUuid().equals(UUID.fromString(BLINKY_UUID_CTRL_CHAR))) {
                final byte [] maybeBootloaderInfo = characteristic.getValue();
                setType200(maybeBootloaderInfo);
                if (firmwareType == FirmwareType.EvalDemo) {
                    setButtonNotificationsEnabled(true);
                    setAccelNotificationsEnabled(true);
                    setAmbLightNotificationsEnabled(true);
                }
                if (discoveryListener != null) {
                    discoveryListener.onInterrogationCompleted(DemoDevice.this, true);
                }
            }
        }

        @Override
        public void didUpdateNotifyState(RigLeBaseDevice device,
                                         BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "didUpdateNotifyState");

            if (characteristic.getUuid().equals(UUID.fromString(BMDEVAL_UUID_BUTTON_CHAR))) {
                maybeStartDemo();
            } else if (characteristic.getUuid().equals(UUID.fromString(BMDEVAL_UUID_ADC_CHAR))) {
                maybeStartDemo();
            } else if (characteristic.getUuid().equals(UUID.fromString(BMDEVAL_UUID_ACCEL_CHAR))) {
                maybeStartDemo();
            } else if (characteristic.getUuid().equals(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR))
                    || characteristic.getUuid().equals(UUID.fromString(BMDWARE_CTRL_POINT_UUID))
                    || characteristic.getUuid().equals(UUID.fromString(BLINKY_UUID_CTRL_CHAR))) {
                requestBootloaderInformation();
            }
        }

        @Override
        public void didWriteValue(RigLeBaseDevice device,
                                  BluetoothGattCharacteristic characteristic) {
        }

        /**
         * Get the hardware version. See {@link DemoDevice#setType200(byte[])}
         *
         * @param device An instance of {@link RigLeBaseDevice} after services have been discovered.
         */
        @Override
        public void discoveryDidComplete(RigLeBaseDevice device) {
            Log.i(TAG, "discoveryDidComplete");

            for (BluetoothGattService service : device.getServiceList()) {
                Log.i(TAG, "Found Service : " + service.getUuid().toString());
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    Log.i(TAG, "Found Characteristic : " + characteristic.getUuid().toString());
                }
            }

            initServices();
            if (getFirmwareType() == DemoDevice.FirmwareType.Bmdware
                    || getFirmwareType() == DemoDevice.FirmwareType.EvalDemo) {
                setControlPointNotificationsEnabled(true);

                // If the control point does not have PROPERTY_NOTIFY,
                // it is a 200 board running Blinky firmware -->
                // set Interrogation completed!
            } else if (getFirmwareType()
                    == DemoDevice.FirmwareType.Blinky) {
                if (hasNotifyProperty()) {
                    setControlPointNotificationsEnabled(true);
                } else {
                    Log.i(TAG, "Found Blinky 200!");
                    if (discoveryListener != null) {
                        discoveryListener.onInterrogationCompleted(DemoDevice.this, true);
                    }
                }

            } else {
                Log.w(TAG, "Failed to find hardware version");
                if (discoveryListener != null) {
                    discoveryListener.onInterrogationCompleted(DemoDevice.this, false);
                }
            }

            DeviceRepository.getInstance().saveConnectedDevice(DemoDevice.this);
        }
    };

    private void maybeStartDemo() {
        Log.i(TAG, "maybeStartDemo");
        numberOfEnabledNotifs += 1;
        Log.i(TAG, "enabledNotifs " + numberOfEnabledNotifs);
        if (numberOfEnabledNotifs >= 3 && deviceListener != null) {
            deviceListener.onDemoInitialized();
        }
    }

    private IRigLeDescriptorObserver descriptorObserver = new IRigLeDescriptorObserver() {

        @Override
        public void didReadDescriptor(RigLeBaseDevice device, BluetoothGattDescriptor descriptor) {
            Log.i(TAG, "didReadDescriptor");
        }
    };

    public DemoDevice(RigLeBaseDevice device) {
        if (device == null) {
            return;
        }

        this.isConnected = false;
        //Assume we have a 200 board until notified otherwise
        this.is200Device = true;
        //Prevent reconnection attempts if we are in the middle of a firmware update
        this.isUpdating = false;

        this.baseDevice = device;
        baseDevice.setDescriptorObserver(descriptorObserver);
        baseDevice.setObserver(baseDeviceObserver);
    }

    @Override
    public void initServices() {
        if (baseDevice == null) {
            return;
        }

        for (BluetoothGattService service : baseDevice.getServiceList()) {
            if (service.getUuid().equals(UUID.fromString(BMDEVAL_UUID_SERVICE))) {
                Log.i(TAG, "Found type Eval Demo");
                gattService = service;
                hardwareVersionCommand = EVAL_HARDDWARE_VERSION_COMMAND;
                enterBootloaderCommand = BMD_EVAL_ENTER_BOOTLOADER_COMMAND;
                controlPointCharacteristic =
                        service.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));
                firmwareType = FirmwareType.EvalDemo;

            } else if (service.getUuid().equals(UUID.fromString(BLINKY_SERVICE_UUID))
                    || (service.getUuid().equals(UUID.fromString(BLINKY_ADVERTISING_SERVICE_UUID)))) {
                Log.i(TAG, "Found type Blinky");
                gattService = service;
                Log.i(TAG, "Found uuid " + gattService.getUuid().toString());
                hardwareVersionCommand = BLINKY_HARDWARE_VERSION_COMMAND;
                enterBootloaderCommand = BLINKY_ENTER_BOOTLOADER_COMMAND;
                controlPointCharacteristic =
                        service.getCharacteristic(UUID.fromString(BLINKY_UUID_CTRL_CHAR));
                firmwareType = FirmwareType.Blinky;

            } else if (service.getUuid().equals(UUID.fromString(BMDWARE_SERVICE_UUID))) {
                Log.i(TAG, "Found type Bmdware");
                gattService = service;
                hardwareVersionCommand = BMDWARE_HARDWARE_VERSION_COMMAND;
                enterBootloaderCommand = BMDWARE_ENTER_BOOTLOADER_COMMAND;
                controlPointCharacteristic =
                        service.getCharacteristic(UUID.fromString(BMDWARE_CTRL_POINT_UUID));
                firmwareType = FirmwareType.Bmdware;
            }
        }
    }

    @Override
    public void runDiscovery() {
        baseDevice.runDiscovery();
    }

    /**
     * Utility method to detect whether or not the Control Point Characteristic has
     * {@link BluetoothGattCharacteristic#PROPERTY_NOTIFY}. Necessary to detect type 200
     * boards running {@link FirmwareType#Blinky}.
     * See {@link com.rigado.bmd200eval.presenters.MainPresenter#onServicesDiscovered(RigLeBaseDevice)}
     *
     * @return True if it has PROPERTY_NOTIFY, false if not or the Characteristic is null.
     */
    @Override
    public boolean hasNotifyProperty() {
        if (controlPointCharacteristic == null) {
            return false;
        }

        final int properties = controlPointCharacteristic.getProperties();

        return (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    @Override
    public void setControlPointNotificationsEnabled(boolean enabled) {
        if (controlPointCharacteristic == null) {
            Log.e(TAG, "controlPointCharacteristic was null!");
            return;
        }

        setCharacteristicNotification(controlPointCharacteristic, enabled);
    }

    @Override
    public void requestBootloaderInformation() {
        Log.i(TAG, "requestBootloaderInformation");
        if (controlPointCharacteristic == null || hardwareVersionCommand == null) {
            Log.e(TAG, "controlPointCharacteristic was null!");
            return;
        }

        writeCharacteristic(controlPointCharacteristic, hardwareVersionCommand);
    }

    @Override
    public FirmwareType getFirmwareType() {
        return this.firmwareType;
    }

    @Override
    public void setDemoListener(IDeviceListener.DemoData listener) {
        this.deviceListener = listener;
    }

    @Override
    public void setDiscoveryListener(IDeviceListener.DiscoveryListener listener) {
        this.discoveryListener = listener;
    }

    @Override
    public void setPasswordListener(IDeviceListener.PasswordListener listener) {
        this.passwordListener = listener;
    }

    @Override
    public void startDemo() {
        Log.i(TAG, "startDemo");
        startAmbientLightSensing();
        startAccelerometerStream();
    }

    @Override
    public void stopDemo() {
        Log.i(TAG, "stopDemo");
        stopAmbientLightSensing();
        stopAccelerometerStream();
    }

    //region DEVICE
    @Override
    public synchronized void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (!DeviceRepository.getInstance().isDeviceConnected()) {
            Log.w(TAG, "Device is disconnected! Aborting read request for "
                    + characteristic.getUuid());
            return;
        }

        baseDevice.readCharacteristic(characteristic);
    }

    @Override
    public synchronized void writeCharacteristic(
            BluetoothGattCharacteristic characteristic, byte[] value) {

        if (!DeviceRepository.getInstance().isDeviceConnected()) {
            Log.w(TAG, "Device is disconnected! Aborting write request for "
                    + characteristic.getUuid().toString() + " " + Arrays.toString(value));
            return;
        }

        baseDevice.writeCharacteristic(characteristic, value);
    }

    @Override
    public synchronized void setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enable) {

        if (!DeviceRepository.getInstance().isDeviceConnected()) {
            Log.w(TAG, "Device is disconnected! Aborting notification request for "
                    + characteristic.getUuid().toString() + " " + enable);
            return;
        }

        baseDevice.setCharacteristicNotification(characteristic, enable);
    }
    //endregion

    @Override
    public RigLeBaseDevice getBaseDevice() {
        return baseDevice;
    }

    /**
     * Determines if the device is a 200 or 300 board by parsing the
     * {@link BootloaderInfo.HardwareSupport} version from the {@link BootloaderInfo} or
     * {@code byte []} value received from the {@code characteristic}. This information is
     * unavailable on certain devices, and is not guaranteed to follow the same structure.
     * All devices are created as type 200 by default.

     *
     * The following steps detail how to get the hardware support {@code value} by device type :
     *
     * {@link FirmwareType#EvalDemo} :
     * - Enable Notifications for {@link DemoDevice#BMDWARE_CTRL_POINT_UUID}
     * - After receiving the state change notification, Call {@link #requestBootloaderInformation()}
     * - Type 200 returns {@link DemoDevice#INVALID_DATA}
     * - Type 300 returns a {@code value} in the following format :
     *   [96, 3, 2, 1, 43, 0, 0, 0, 1, 4, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0] which can be parsed as
     *   {@link BootloaderInfo}.
     * - If {@link BootloaderInfo.HardwareSupport} is >= 2, it is a
     *   300 board.
     *
     * {@link FirmwareType#Blinky} :
     * - After {@link #initServices()}, check to see if {@link #BLINKY_UUID_CTRL_CHAR} has
     *   {@link BluetoothGattCharacteristic#PROPERTY_NOTIFY} by calling
     *   {@link #hasNotifyProperty()}. If not, it is type 200. Do nothing, type 200 is the default.
     *   Otherwise, enable notifications.
     * - After receiving the state change notification, Call {@link #requestBootloaderInformation()}
     * - Receive the value in {@code onCharacteristicUpdate}, and pass to
     *   {@code getHardwareVersion}.
     * - The {@code value} is returned in the following format :
     *   [3, 2, 0, 42, 0, 0, 0, 1, 1, 2, 3, 0]
     * - If position [9] is >= 2, it is a 300 board.
     *
     * {@link FirmwareType#Bmdware} :
     * - Enable notifications for {@link DemoDevice#BMDEVAL_UUID_CTRL_CHAR}
     * - After receiving the state change notification, Call {@link #requestBootloaderInformation()}
     * - Type 200 returns a value of [0]
     * - Type 300 returns a {@code value} in the following format :
     *   [3, 2, 0, 42, 0, 0, 0, 1, 1, 2, 3, 0]
     * - If position [9] is >= 2, it is a 300 board.
     *
     * @param value The bytes returned from the device's {@link #controlPointCharacteristic} after
     *                      a call to request the Hardware Version.
     */
    @Override
    public void setType200(byte [] value) {
        if (value == null) {
            return;
        }

        final byte [] maybeLockedCommand = { value[0] };
        boolean is200 = true;

        if (Arrays.equals(maybeLockedCommand, DEVICE_LOCKED)) {
            if (passwordListener != null) {
                passwordListener.onDeviceLocked();
            }
            Log.w(TAG, "Device Locked!");
            return;
        }

        // Legacy firmware reports the hardware version in a different structure
        if (value.length == BootloaderInfo.LEGACY_SIZE
                && value[BootloaderInfo.LEGACY_HARDWARE_INDEX]
                >= BootloaderInfo.HARDWARE_SUPPORT_NRF52) {
            is200 = false;

        } else if (value.length == BootloaderInfo.SIZE) {
            final BootloaderInfo info = new BootloaderInfo(value);
            if (info.getHardwareSupport().getType()
                    == BootloaderInfo.HardwareSupport.NRF52.getType()) {
                is200 = false;
            }
        }

        this.is200Device = is200;
    }

    @Override
    public boolean is200() {
        return this.is200Device;
    }

    @Override
    public boolean isConnected() {
        return this.isConnected;
    }

    @Override
    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    //region FIRMWARE_UPDATE
    /**
     * Performs a firmware update. Internally, Rigablue checks if any of these parameters are null.
     * If null, {@link IRigFirmwareUpdateManagerObserver#updateFailed(RigDfuError)} is
     * called with {@link RigDfuError#INVALID_PARAMETER}.
     *
     * @param manager A new instance of {@link RigFirmwareUpdateManager}. A new instance is
     *                required for each firmware update.
     * @param inputStream The firmware image
     */
    @Override
    public void startFirmwareUpdate(RigFirmwareUpdateManager manager, InputStream inputStream) {
        rigFirmwareUpdateManager = manager;
        rigFirmwareUpdateManager.updateFirmware(
                baseDevice,
                inputStream,
                controlPointCharacteristic,
                enterBootloaderCommand);
    }

    @Override
    public boolean isUpdating() {
        return this.isUpdating;
    }

    @Override
    public void setUpdatingStatus(boolean isUpdating) {
        this.isUpdating = isUpdating;
    }
    //endregion

    @Override
    public void setLedColor(RgbColor color) {
        if(gattService == null) {
            return;
        }

        BluetoothGattCharacteristic ledChar =
                gattService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_LED_CHAR));

        if(ledChar == null) {
            return;
        }

        byte [] data = new byte[] { color.getRed(), color.getGreen(), color.getBlue() };

        writeCharacteristic(ledChar, data);
    }

    @Override
    public RgbColor getLedColor() {
        if(gattService == null) {
            return null;
        }

        BluetoothGattCharacteristic ledChar =
                gattService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_LED_CHAR));

        if(ledChar == null) {
            return null;
        }

        byte [] value = ledChar.getValue();

        return new RgbColor(value[0], value[1], value[2]);
    }

    @Override
    public void setAmbLightNotificationsEnabled(boolean enabled) {
        if (gattService == null) {
            return;
        }

        final BluetoothGattCharacteristic abmChar =
                gattService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_ADC_CHAR));

        if (abmChar == null) {
            return;
        }

        setCharacteristicNotification(abmChar, enabled);
    }

    @Override
    public void startAmbientLightSensing() {
        if(gattService == null) {
            return;
        }

        final BluetoothGattCharacteristic adcChar =
                gattService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));

        if(adcChar == null) {
            return;
        }

        byte [] command = new byte[] { ADC_STREAM_START };
        writeCharacteristic(adcChar, command);
    }

    @Override
    public void stopAmbientLightSensing() {
        if(gattService == null) {
            return;
        }

        BluetoothGattCharacteristic adcChar =
                gattService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));

        if(adcChar == null) {
            return;
        }

        Log.i(TAG, "stopAmbientLightSensing");
        byte [] command = new byte[] { ADC_STREAM_STOP };
        writeCharacteristic(adcChar, command);
    }

    @Override
    public void setAccelNotificationsEnabled(boolean enabled) {
        if (gattService == null) {
            return;
        }

        final BluetoothGattCharacteristic accelChar =
                gattService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_ACCEL_CHAR));

        if (accelChar == null) {
            return;
        }

        setCharacteristicNotification(accelChar, enabled);
    }

    @Override
    public void startAccelerometerStream() {
        if(gattService == null) {
            return;
        }

        BluetoothGattCharacteristic accelChar =
                gattService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));

        if(accelChar == null) {
            return;
        }

        byte [] command = new byte[] { ACCEL_STREAM_START };
        writeCharacteristic(accelChar, command);
    }

    @Override
    public void stopAccelerometerStream() {
        if(gattService == null) {
            return;
        }

        BluetoothGattCharacteristic accelChar =
                gattService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_CTRL_CHAR));
        if(accelChar == null) {
            return;
        }

        Log.i(TAG, "stopAccelerometerStream");
        byte [] command = new byte[] { ACCEL_STREAM_STOP };
        writeCharacteristic(accelChar, command);
    }

    @Override
    public void setButtonNotificationsEnabled(boolean enable) {
        if (gattService == null) {
            return;
        }

        final BluetoothGattCharacteristic buttonChar =
                gattService.getCharacteristic(UUID.fromString(BMDEVAL_UUID_BUTTON_CHAR));

        if (buttonChar == null) {
            return;
        }

        setCharacteristicNotification(buttonChar, enable);
    }
}
