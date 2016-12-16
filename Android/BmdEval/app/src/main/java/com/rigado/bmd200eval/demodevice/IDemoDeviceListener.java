package com.rigado.bmd200eval.demodevice;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.rigado.rigablue.RigLeBaseDevice;

public interface IDemoDeviceListener {

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

    interface DiscoveryListener {
        void onServicesDiscovered(RigLeBaseDevice device);
    }
}