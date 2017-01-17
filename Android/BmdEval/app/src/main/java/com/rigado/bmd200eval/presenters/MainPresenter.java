package com.rigado.bmd200eval.presenters;


import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.rigado.bmd200eval.contracts.MainContract;
import com.rigado.bmd200eval.datasource.DeviceRepository;
import com.rigado.bmd200eval.demodevice.DisconnectedDevice;
import com.rigado.bmd200eval.demodevice.DemoDevice;
import com.rigado.bmd200eval.interfaces.IDeviceListener;
import com.rigado.rigablue.IRigLeConnectionManagerObserver;
import com.rigado.rigablue.IRigLeDiscoveryManagerObserver;
import com.rigado.rigablue.RigAvailableDeviceData;
import com.rigado.rigablue.RigDeviceRequest;
import com.rigado.rigablue.RigLeBaseDevice;
import com.rigado.rigablue.RigLeConnectionManager;
import com.rigado.rigablue.RigLeDiscoveryManager;


public class MainPresenter extends BasePresenter implements
        MainContract.UserActionsListener,
        IRigLeDiscoveryManagerObserver,
        IRigLeConnectionManagerObserver,
        IDeviceListener.PasswordListener,
        IDeviceListener.DiscoveryListener {

    private static final String TAG = MainPresenter.class.getSimpleName();

    private MainContract.View mainView;
    private DemoDevice demoDevice;
    private Handler uiThreadHandler;
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
        connectionManager.setObserver(this);
        if (!DeviceRepository.getInstance().isDeviceConnected() && !demoDevice.isUpdating()) {
            maybeStartScanning();
        }
    }

    @Override
    public void onPause() {
        connectionManager.setObserver(null);
        if (discoveryManager.isDiscoveryRunning()) {
            stopScanning();
        }
    }

    private static final int CONNECTION_TIMEOUT = 20000;

    @Override
    public void didDiscoverDevice(final RigAvailableDeviceData device) {
        discoveryManager.setObserver(null);
        discoveryManager.stopDiscoveringDevices();
        discoveryManager.clearAvailableDevices();

        connectionManager.setObserver(MainPresenter.this);
        connectionManager.connectDevice(device, CONNECTION_TIMEOUT);
        updateDialog("Connecting to " + device.getUncachedName() + "...");
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
        updateDialog("Interrogating " + device.getName() + "...");
        DeviceRepository.getInstance().saveConnectedDevice(demoDevice);
        demoDevice.setDiscoveryListener(this);
        demoDevice.setPasswordListener(this);
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
    public void onServicesDiscovered(RigLeBaseDevice device) {
        //Noop
    }

    @Override
    public void onInterrogationCompleted(final DemoDevice demoDevice, boolean foundHwVersion) {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mainView.onInterrogationCompleted(demoDevice);
            }
        });
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
        updateDialog("Attempting to reconnect to " + data.getUncachedName());
    }

    @Override
    public void unlockDevice(String password) {
        demoDevice.unlockDevice(password);
    }

    private void updateDialog(final String title) {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mainView.updateDialog(title);
            }
        });
    }

    @Override
    public void onDeviceLocked() {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mainView.updateDeviceLocked("Device Locked!");
            }
        });
    }

    @Override
    public void onDeviceUnlocked() {
        demoDevice.requestBootloaderInformation();
    }

    @Override
    public void onDeviceUnlockFailed() {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mainView.updateDeviceLocked("Unlock Device Failed!");
            }
        });
    }
}
