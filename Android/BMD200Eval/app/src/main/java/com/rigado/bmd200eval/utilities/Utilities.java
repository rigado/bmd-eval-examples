package com.rigado.bmd200eval.utilities;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import com.rigado.rigablue.RigFirmwareUpdateManager;
import com.rigado.rigablue.RigLeBaseDevice;

import java.io.InputStream;

/**
 * BTLE Sample app useful methods and functions
 */
public class Utilities {

    // Constants
    private final String TAG = getClass().getSimpleName();

    public void startFirmwareUpdate(Context context, RigFirmwareUpdateManager fwManager, RigLeBaseDevice device, JsonFirmwareType firmwareRecord,
                                    BluetoothGattCharacteristic bootCharacteristic, byte [] bootCommand) {

        if (firmwareRecord != null){

            final String filename = firmwareRecord.getProperties().getFilename();

            // ensure that the filenames contain no extension
            String strFilenameNoExt1;
            if (filename.contains(".")) {
                strFilenameNoExt1 = filename.substring(0, filename.lastIndexOf('.'));
            } else {
                strFilenameNoExt1 = filename;
            }

            final int deviceFWid = context.getResources().getIdentifier(strFilenameNoExt1, "raw", context.getPackageName());

            InputStream fwImageInputStream = (deviceFWid != 0) ? context.getResources().openRawResource(deviceFWid) : null;

            fwManager.updateFirmware(device, fwImageInputStream, bootCharacteristic, bootCommand);
        } else {
            Log.e(TAG, "Firmware filenames are unknown - were the JSON values read correctly?");
        }
    }
}
