package com.rigado.bmd200eval.interfaces;


import com.rigado.bmd200eval.demodevice.DemoDevice;
import com.rigado.rigablue.RigLeBaseDevice;

public interface IDeviceListener {

    interface DemoData {
        void onReceiveButtonData(byte[] data);

        void onReceiveAmbientLightData(byte[] data);

        void onReceiveAccelerometerData(byte[] data);

        void onDemoInitialized();
    }

    interface DiscoveryListener {
        void onServicesDiscovered(RigLeBaseDevice device);
        void onInterrogationCompleted(DemoDevice demoDevice, boolean foundHwVersion);
    }

    interface PasswordListener {
        void onDeviceLocked();
        void onDeviceUnlocked();
        void onDeviceUnlockFailed();
    }
}
