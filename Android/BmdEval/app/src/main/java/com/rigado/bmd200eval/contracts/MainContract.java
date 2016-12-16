package com.rigado.bmd200eval.contracts;

import com.rigado.bmd200eval.demodevice.DemoDevice;

public interface MainContract {

    interface View {
        void setBluetoothState(boolean enabled);
        void onInterrogationCompleted(DemoDevice device);
        void onInterrogationFailed(DemoDevice demoDevice);
        void updateDialog(String message);
        void deviceDisconnected(final String reason);
    }

    interface UserActionsListener {
        void requestReconnect();
    }
}
