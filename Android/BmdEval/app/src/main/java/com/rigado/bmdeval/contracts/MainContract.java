package com.rigado.bmdeval.contracts;

import com.rigado.bmdeval.demodevice.DemoDevice;

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
