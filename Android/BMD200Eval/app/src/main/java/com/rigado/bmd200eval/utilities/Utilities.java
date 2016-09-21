package com.rigado.bmd200eval.utilities;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.rigado.bmd200eval.BmdApplication;
import com.rigado.rigablue.RigCoreBluetooth;
import com.rigado.rigablue.RigFirmwareUpdateManager;
import com.rigado.rigablue.RigLeBaseDevice;
import com.rigado.rigablue.RigLeConnectionManager;

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

            final String filename = firmwareRecord.getProperties().getFilename200();
            Log.i(TAG, "filename " + filename);

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


    //If Marshmallow or above, check if permission has been granted
    public static boolean hasPermission(Context context, String permission) {
        return Build.VERSION.SDK_INT<23 ||
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static final String[] locationPermission = {Manifest.permission.ACCESS_COARSE_LOCATION};

    public static boolean hasLocationPermission(Context context) {
        boolean hasPermissions = false;

        if(hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                || hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            hasPermissions = true;
        }

        return hasPermissions;
    }

    public static boolean isLocationEnabled(Context context) {
        boolean isLocationEnabled = false;
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            isLocationEnabled = true;
        }
        return isLocationEnabled;
    }

    /**
     *
     * @return true if the current device is found in the list of connected devices.
     */
    public static boolean isDeviceConnected(BluetoothDevice device) {
        return RigCoreBluetooth.getInstance().getDeviceConnectionState(device) == BluetoothProfile.STATE_CONNECTED;
    }
}
