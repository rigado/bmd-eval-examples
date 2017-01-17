package com.rigado.bmd200eval.utilities;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.rigado.rigablue.RigCoreBluetooth;

/**
 * BTLE Sample app useful methods and functions
 */
public class Utilities {

    // Constants
    private static final String TAG = Utilities.class.getSimpleName();

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

    public static boolean isValidPassword(String input) {
        return input.matches("[!-~]+");
    }
}
