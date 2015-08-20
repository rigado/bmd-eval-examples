package com.rigado.bmd200eval.demodevice;

/**
 * Created by stutzenbergere on 7/27/15.
 */
public interface BMD200EvalManagerObserver {
    void didConnectDevice(BMD200EvalDemoDevice device);
    void didDisconnectDevice();
    void bluetoothNotSupported();
    void bluetoothPowerStateChanged(boolean enabled);
}
