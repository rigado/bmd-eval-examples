package com.rigado.bmdeval.demodevice;

/**
 * Created by stutzenbergere on 7/27/15.
 */
public interface IBmdEvalManagerListener {
    void didConnectDevice(BmdEvalDemoDevice device);
    void didDisconnectDevice();
    void bluetoothNotSupported();
    void bluetoothPowerStateChanged(boolean enabled);
}
