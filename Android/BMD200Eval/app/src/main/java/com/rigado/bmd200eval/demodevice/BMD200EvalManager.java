package com.rigado.bmd200eval.demodevice;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.rigado.rigablue.IRigLeBaseDeviceObserver;
import com.rigado.rigablue.IRigLeConnectionManagerObserver;
import com.rigado.rigablue.IRigLeDiscoveryManagerObserver;
import com.rigado.rigablue.RigAvailableDeviceData;
import com.rigado.rigablue.RigDeviceRequest;
import com.rigado.rigablue.RigLeBaseDevice;
import com.rigado.rigablue.RigLeConnectionManager;
import com.rigado.rigablue.RigLeDiscoveryManager;
/**
 * Created by stutzenbergere on 7/23/15.
 */
public class BMD200EvalManager implements IRigLeBaseDeviceObserver, IRigLeConnectionManagerObserver, IRigLeDiscoveryManagerObserver {
    BMD200EvalDemoDevice demoDevice;
    boolean is_connected;
    BMD200EvalManagerObserver observer;
    Context mContext;

    static BMD200EvalManager instance;

    private BMD200EvalManager() {
        is_connected = false;
        demoDevice = null;
        observer = null;
    }

    public static BMD200EvalManager getInstance() {
        if(instance == null) {
            instance = new BMD200EvalManager();
        }

        return instance;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void searchForDemoDevices() {
        String [] uuidList = new String[] {
                "2413b33f-707f-90bd-0245-2ab8807571b7",
                "6e400001-b5a3-f393-e0a9-e50e24dcca9e",
                "50db1523-418d-4690-9589-ab7be9e22684",
                "0000180f-0000-1000-8000-00805f9b34fb" // Blinky

        };

        RigLeConnectionManager.setContext(mContext);
        final RigLeConnectionManager rigLeConnectionManager = RigLeConnectionManager.getInstance();
        rigLeConnectionManager.setObserver(this);

        RigDeviceRequest dr = new RigDeviceRequest(uuidList, 0);
        dr.setObserver(this);
        RigLeDiscoveryManager.getInstance().startDiscoverDevices(dr);
    }

    public boolean isConnected() {
        return is_connected;
    }

    public BMD200EvalDemoDevice getDemoDevice() { return demoDevice; }

    public void disconnectDevice() {
        if(!is_connected) {
            return;
        }

        RigLeConnectionManager.getInstance().disconnectDevice(demoDevice.getBaseDevice());
    }

    public void setObserver(BMD200EvalManagerObserver o) {
        observer = o;
    }

    @Override
    public void didUpdateValue(RigLeBaseDevice device, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void didUpdateNotifyState(RigLeBaseDevice device, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void didWriteValue(RigLeBaseDevice device, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void discoveryDidComplete(RigLeBaseDevice device) {
        demoDevice = new BMD200EvalDemoDevice(device);
        if(observer != null) {
            observer.didConnectDevice(demoDevice);
        }
    }

    @Override
    public void didConnectDevice(RigLeBaseDevice device) {
        //TODO: Log connection event
        is_connected = true;
        device.setObserver(this);
        device.runDiscovery();
    }

    @Override
    public void didDisconnectDevice(BluetoothDevice btDevice) {
        is_connected = false;
        if(observer != null) {
            observer.didDisconnectDevice();
        }
    }

    @Override
    public void deviceConnectionDidFail(RigAvailableDeviceData device) {
        is_connected = false;
    }

    @Override
    public void deviceConnectionDidTimeout(RigAvailableDeviceData device) {
        this.searchForDemoDevices();
    }

    @Override
    public void didDiscoverDevice(RigAvailableDeviceData device) {
        if(device.getRssi() > -60) {
            RigLeDiscoveryManager.getInstance().stopDiscoveringDevices();
            RigLeConnectionManager.getInstance().connectDevice(device, 10000);
        } else {
            //TODO: Output to log??
        }
    }

    @Override
    public void discoveryDidTimeout() {

    }

    @Override
    public void bluetoothPowerStateChanged(boolean enabled) {
        if(observer != null) {
            observer.bluetoothPowerStateChanged(enabled);
        }
    }

    @Override
    public void bluetoothDoesNotSupported() {
        if(observer != null) {
            observer.bluetoothNotSupported();;
        }
    }
}
