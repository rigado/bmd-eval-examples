package com.rigado.bmdeval.contracts;

import com.rigado.bmdeval.devicedata.otherdevices.BleDevice;

public interface MainContract {

    interface View {
        void setBluetoothState(boolean enabled);
        void onInterrogationCompleted(BleDevice device);
    }

    interface UserActionsListener {

    }
}
