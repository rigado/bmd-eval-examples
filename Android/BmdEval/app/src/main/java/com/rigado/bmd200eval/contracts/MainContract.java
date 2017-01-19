package com.rigado.bmd200eval.contracts;

import com.rigado.bmd200eval.demodevice.DemoDevice;

public interface MainContract {

    interface View {
        void setBluetoothState(boolean enabled);
        void onInterrogationCompleted(DemoDevice device);
        void updateDialog(String message);
        void deviceDisconnected(final String reason);
        void updateDeviceLocked(final String title);
        void dismissDialogs();
    }

    interface UserActionsListener {
        void requestReconnect();
        void unlockDevice(final String password);
    }
}
