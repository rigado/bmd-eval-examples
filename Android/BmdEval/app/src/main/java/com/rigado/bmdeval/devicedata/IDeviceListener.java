package com.rigado.bmdeval.devicedata;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.rigado.rigablue.RigDfuError;

public interface IDeviceListener {

    interface BluetoothStateListener {
        void onBluetoothPowerStateChange(boolean enabled);
    }

    interface ReadWriteListener {
        void onCharacteristicWrite(BluetoothGattCharacteristic characteristic);

        void onCharacteristicUpdate(BluetoothGattCharacteristic characteristic);

        void onDescriptorUpdate(BluetoothGattDescriptor descriptor);

    }

    interface NotifyListener {
        void onCharacteristicStateChange(BluetoothGattCharacteristic characteristic);
    }

    interface FirmwareUpdateListener {
        void onReceiveProgress(int progress);
        void onReceiveStatus(String status);
        void onUpdateCompleted();
        void onUpdateFailed(RigDfuError error);

    }

    interface DiscoveryListener {
        void onServicesDiscovered();
    }
}