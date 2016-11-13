package com.rigado.bmdeval.presenters;


import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

import com.rigado.bmdeval.contracts.MainContract;
import com.rigado.bmdeval.devicedata.otherdevices.BleDevice;
import com.rigado.rigablue.IRigLeConnectionManagerObserver;
import com.rigado.rigablue.IRigLeDiscoveryManagerObserver;
import com.rigado.rigablue.RigAvailableDeviceData;
import com.rigado.rigablue.RigDeviceRequest;
import com.rigado.rigablue.RigLeBaseDevice;
import com.rigado.rigablue.RigLeConnectionManager;
import com.rigado.rigablue.RigLeDiscoveryManager;

public class MainPresenter extends BasePresenter implements
        IRigLeDiscoveryManagerObserver,
        IRigLeConnectionManagerObserver {

    private MainContract.View mainView;
    private BleDevice bleDevice;
    private Handler uiThreadHandler;
    private boolean isConnected;
    private RigLeDiscoveryManager discoveryManager =
            RigLeDiscoveryManager.getInstance();
    private RigLeConnectionManager connectionManager =
            RigLeConnectionManager.getInstance();

    public MainPresenter(MainContract.View view) {
        this.mainView = view;
        uiThreadHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onResume() {
        if (bleDevice == null && !isConnected) {
            discoveryManager.setObserver(this);
            maybeStartScanning();
        } else {
            connectionManager.setObserver(this);
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
    public void didDiscoverDevice(RigAvailableDeviceData device) {
        discoveryManager.setObserver(null);
        discoveryManager.stopDiscoveringDevices();
        discoveryManager.clearAvailableDevices();
        connectionManager.connectDevice(device, CONNECTION_TIMEOUT);
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
    public void didConnectDevice(RigLeBaseDevice device) {
        //TODO : Interrogate
    }

    @Override
    public void didDisconnectDevice(BluetoothDevice btDevice) {

    }

    @Override
    public void deviceConnectionDidFail(RigAvailableDeviceData device) {

    }

    @Override
    public void deviceConnectionDidTimeout(RigAvailableDeviceData device) {

    }

    private RigDeviceRequest rigDeviceRequest;
    private static final int DISCOVERY_TIMEOUT = 0;

    private RigDeviceRequest getDeviceRequest() {
        if (rigDeviceRequest == null) {
            rigDeviceRequest = new RigDeviceRequest(
                    bleDevice.getScanFilter(),
                    DISCOVERY_TIMEOUT);
            rigDeviceRequest.setObserver(this);
        }

        return rigDeviceRequest;
    }

    private void maybeStartScanning() {
        discoveryManager.setObserver(this);
        discoveryManager.startDiscoverDevices(getDeviceRequest());
    }

    private void stopScanning() {
        discoveryManager.setObserver(null);
        discoveryManager.stopDiscoveringDevices();
        discoveryManager.clearAvailableDevices();
    }

}
