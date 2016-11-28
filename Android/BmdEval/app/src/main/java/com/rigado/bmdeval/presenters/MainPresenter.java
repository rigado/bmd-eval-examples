package com.rigado.bmdeval.presenters;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.rigado.bmdeval.contracts.MainContract;
import com.rigado.bmdeval.datasource.DeviceRepository;
import com.rigado.bmdeval.demodevice.DisconnectedDevice;
import com.rigado.bmdeval.demodevice.IDemoDeviceListener;
import com.rigado.bmdeval.demodevice.DemoDevice;
import com.rigado.rigablue.IRigLeConnectionManagerObserver;
import com.rigado.rigablue.IRigLeDiscoveryManagerObserver;
import com.rigado.rigablue.RigAvailableDeviceData;
import com.rigado.rigablue.RigDeviceRequest;
import com.rigado.rigablue.RigLeBaseDevice;
import com.rigado.rigablue.RigLeConnectionManager;
import com.rigado.rigablue.RigLeDiscoveryManager;

import java.util.UUID;

public class MainPresenter extends BasePresenter implements
        MainContract.UserActionsListener,
        IRigLeDiscoveryManagerObserver,
        IRigLeConnectionManagerObserver,
        IDemoDeviceListener.NotifyListener,
        IDemoDeviceListener.ReadWriteListener,
        IDemoDeviceListener.DiscoveryListener {

    private static final String TAG = MainPresenter.class.getSimpleName();

    private MainContract.View mainView;
    private DemoDevice demoDevice;
    private Handler uiThreadHandler;
    private boolean isConnected;
    private RigLeDiscoveryManager discoveryManager =
            RigLeDiscoveryManager.getInstance();
    private RigLeConnectionManager connectionManager =
            RigLeConnectionManager.getInstance();

    public MainPresenter(MainContract.View view) {
        this.mainView = view;
        this.demoDevice = DeviceRepository
                .getInstance()
                .getConnectedDevice();
        this.uiThreadHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onResume() {
        discoveryManager.setObserver(this);
        connectionManager.setObserver(this);
        if (!demoDevice.isConnected()) {
            maybeStartScanning();
        }
    }

    @Override
    public void onPause() {
        discoveryManager.setObserver(null);
        connectionManager.setObserver(null);
        if (discoveryManager.isDiscoveryRunning()) {
            stopScanning();
        }
    }

    private static final int CONNECTION_TIMEOUT = 8000;

    @Override
    public void didDiscoverDevice(final RigAvailableDeviceData device) {
        discoveryManager.setObserver(null);
        discoveryManager.stopDiscoveringDevices();
        discoveryManager.clearAvailableDevices();

        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                connectionManager.setObserver(MainPresenter.this);
                connectionManager.connectDevice(device, CONNECTION_TIMEOUT);
                mainView.updateDialog("Connecting to " + device.getUncachedName() + "...");
            }
        };

        handler.postDelayed(runnable, 1000);
    }

    @Override
    public void discoveryDidTimeout() {
        //Noop.
    }

    @Override
    public void bluetoothPowerStateChanged(boolean enabled) {
        mainView.setBluetoothState(enabled);
    }

    @Override
    public void bluetoothDoesNotSupported() {
        //TODO: Alert user bluetooth is not supported
    }

    @Override
    public void didConnectDevice(final RigLeBaseDevice device) {
        Log.i(TAG, "didConnectDevice " + device.getBluetoothDevice().getAddress());
        demoDevice.setConnected(true);
        demoDevice = new DemoDevice(device);
        demoDevice.addDiscoveryListener(this);
        demoDevice.addReadWriteListener(this);
        demoDevice.addNotifyListener(this);
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mainView.updateDialog("Interrogating " + device.getName() + "...");
            }
        });
        demoDevice.runDiscovery();
    }

    @Override
    public void didDisconnectDevice(BluetoothDevice btDevice) {
        Log.i(TAG, "didDisconnectDevice");
        cleanUp( "Device Disconnected");
    }

    @Override
    public void deviceConnectionDidFail(RigAvailableDeviceData device) {
        Log.i(TAG, "deviceConnectionDidFail");
        cleanUp( "Device Connection Failed");
    }

    @Override
    public void deviceConnectionDidTimeout(RigAvailableDeviceData device) {
        Log.i(TAG, "deviceConnectionDidTimeout");
        cleanUp("Device Connection Timeout");
    }

    private void cleanUp(final String reason) {
        if (!demoDevice.isUpdating()) {
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    demoDevice = new DisconnectedDevice();
                    mainView.deviceDisconnected(reason);
                }
            });
        }
    }

    private RigDeviceRequest rigDeviceRequest;
    private static final int DISCOVERY_TIMEOUT = 0;

    private RigDeviceRequest getDeviceRequest() {
        if (rigDeviceRequest == null) {
            rigDeviceRequest = new RigDeviceRequest(
                    DeviceRepository.getInstance().SCAN_UUID_LIST,
                    DISCOVERY_TIMEOUT);
            rigDeviceRequest.setObserver(this);
        }

        return rigDeviceRequest;
    }

    public void maybeStartScanning() {
        demoDevice.setUpdatingStatus(false);
        discoveryManager.setObserver(this);
        discoveryManager.startDiscoverDevices(getDeviceRequest());
        mainView.updateDialog("Searching for devices...");
    }

    public void stopScanning() {
        discoveryManager.setObserver(null);
        discoveryManager.stopDiscoveringDevices();
        discoveryManager.clearAvailableDevices();
    }

    @Override
    public void onCharacteristicStateChange(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(
                UUID.fromString(DemoDevice.BMDEVAL_UUID_CTRL_CHAR))
                || characteristic.getUuid().equals(
                UUID.fromString(DemoDevice.BMDWARE_CTRL_POINT_UUID))
                || characteristic.getUuid().equals(
                UUID.fromString(DemoDevice.BLINKY_UUID_CTRL_CHAR))) {
            Log.i(TAG, "enabled control point notifications for " + characteristic.getUuid().toString());
            demoDevice.requestBootloaderInformation();
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onCharacteristicUpdate(BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, "onCharacteristicUpdate " + characteristic.getUuid().toString());
        if (characteristic.getUuid().equals(
                UUID.fromString(DemoDevice.BMDWARE_CTRL_POINT_UUID))
            || (characteristic.getUuid().equals(
                UUID.fromString(DemoDevice.BLINKY_UUID_CTRL_CHAR)))
            || (characteristic.getUuid().equals(
                UUID.fromString(DemoDevice.BMDEVAL_UUID_CTRL_CHAR)))) {

            final byte [] maybeBootloaderInfo = characteristic.getValue();

            demoDevice.setType200(maybeBootloaderInfo);

            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "onInterrogationCompleted called");
                    mainView.onInterrogationCompleted(demoDevice);
                }
            });

        }
    }

    @Override
    public void onDescriptorUpdate(BluetoothGattDescriptor descriptor) {

    }

    /**
     * Get the hardware version. See {@link DemoDevice#setType200(byte[])}
     *
     * @param device An instance of {@link RigLeBaseDevice} after services have been discovered.
     */
    @Override
    public void onServicesDiscovered(RigLeBaseDevice device) {
        Log.i(TAG, "onServicesDiscovered");

        for (BluetoothGattService service : device.getServiceList()) {
            Log.i(TAG, "Found Service : " + service.getUuid().toString());
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                Log.i(TAG, "Found Characteristic : " + characteristic.getUuid().toString());
            }
        }

        demoDevice.initServices();
        if (demoDevice.getFirmwareType() == DemoDevice.FirmwareType.Bmdware
                || demoDevice.getFirmwareType() == DemoDevice.FirmwareType.EvalDemo) {
            demoDevice.setControlPointNotificationsEnabled(true);

            // If the control point does not have PROPERTY_NOTIFY,
            // it is a 200 board running Blinky firmware -->
            // set Interrogation completed!
        } else if (demoDevice.getFirmwareType()
                == DemoDevice.FirmwareType.Blinky) {
            if (demoDevice.hasNotifyProperty()) {
                demoDevice.setControlPointNotificationsEnabled(true);
            } else {
                Log.i(TAG, "Found Blinky 200!");

                uiThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "onInterrogationCompleted called");
                        mainView.onInterrogationCompleted(demoDevice);
                    }
                });
            }

        } else {
            Log.w(TAG, "Failed to find hardware version");
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mainView.onInterrogationFailed(demoDevice);
                }
            });
        }

        DeviceRepository.getInstance().saveConnectedDevice(demoDevice);
    }

    @Override
    public void requestReconnect() {
        connectionManager.setObserver(this);
        final RigAvailableDeviceData data =
                DeviceRepository
                .getInstance()
                .getAvailableData();

        if (data == null) {
            maybeStartScanning();
            return;
        }

        connectionManager.connectDevice(data, CONNECTION_TIMEOUT);
        mainView.updateDialog("Attempting to reconnect to " + data.getUncachedName());
    }
}
