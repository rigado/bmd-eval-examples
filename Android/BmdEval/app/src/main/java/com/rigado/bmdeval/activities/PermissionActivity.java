package com.rigado.bmdeval.activities;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.rigado.bmdeval.R;
import com.rigado.bmdeval.utilities.Utilities;

public class PermissionActivity extends AppCompatActivity {
    private final static String TAG = PermissionActivity.class.getSimpleName();

    private final static int LOCATION_SETTINGS_REQUEST_CODE = 101;
    private final static int APPLICATION_SETTINGS_REQUEST_CODE = 100;
    private final static int LOCATION_PERMISSION_REQUEST_CODE = 0x0F;

    private Button mTryAgainButton;
    private int numberOfPermissionRequests;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_permission);

        numberOfPermissionRequests = 0;

        final ImageView infoIcon = (ImageView) findViewById(R.id.permission_info);
        infoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertUserPermissionRequestRationale();
            }
        });

        mTryAgainButton = (Button) findViewById(R.id.activity_permission_button);
        mTryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForPermissions();
            }
        });

        checkForPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //If permission was granted, check if Location is turned on before loading UI
                    checkLocationStatus();
                } else {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(PermissionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        //If permission was denied, explain why we need access to location
                        if(numberOfPermissionRequests<1) {
                            numberOfPermissionRequests += 1;
                            alertUserPermissionRequestRationale();
                        }

                    } else {
                        //The user has permanently denied access and will have to manually
                        //update their Settings to connect.
                        goToAppSettings();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult " );
        if(requestCode == LOCATION_SETTINGS_REQUEST_CODE
                || requestCode == APPLICATION_SETTINGS_REQUEST_CODE) {
            Log.i(TAG, "either code received");
            if(Utilities.hasLocationPermission(this)
                    && Utilities.isLocationEnabled(this)) {
                goToMainActivity();
            }
        }
    }

    private void checkForPermissions() {
        Log.i(TAG, "checkForPermissions");
        /**
         * Apps installed on Android devices with API 6.0+ require Location permissions and for
         * Location to be turned on to discover devices. Location is a dangerous permission,
         * and users can revoke permissions or turn it off at any time.
         * Apps should always check for permissions before beginning the discovery process.
         *
         * https://developer.android.com/training/permissions/requesting.html
         *
         * https://code.google.com/p/android/issues/detail?id=189090&q=ble%20android%206.0&colspec=ID%20Type%20Status%20Owner%20Summary%20Stars
         *
         * */
        if(Utilities.hasLocationPermission(this)) {
            Log.i(TAG, "hasLocationPermission");
            //If we already have permission, check if location is turned on
            checkLocationStatus();

        } else {
            Log.i(TAG, "!hasLocationPermission");
            //Else, if we do not have permission, & the user has previously denied our request,
            // show a dialog explaining why we need access to location.
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.i(TAG, "shouldShowRequest");
                if(numberOfPermissionRequests<1) {
                    Log.i(TAG, "alertUser");
                    numberOfPermissionRequests += 1;
                    alertUserPermissionRequestRationale();
                } else {
                    Log.i(TAG, "!alertUser");
                    ActivityCompat.requestPermissions(this, Utilities.locationPermission, LOCATION_PERMISSION_REQUEST_CODE);
                }
            } else {
                //Else, request access to location
                ActivityCompat.requestPermissions(this, Utilities.locationPermission, LOCATION_PERMISSION_REQUEST_CODE);
            }

        }
    }

    private void alertUserPermissionRequestRationale() {
        Log.i(TAG, "alertUserPermissionRequestRationale");
        new AlertDialog.Builder(this)
                .setTitle("BLE Scanning Unavailable")
                .setMessage("Marshmallow+ requires Location services to scan for BLE devices. Please enable location to continue.")
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //Once we have explained why we need access to Location, request it again
                        if(numberOfPermissionRequests >1) {
                            ActivityCompat.requestPermissions(PermissionActivity.this, Utilities.locationPermission, LOCATION_PERMISSION_REQUEST_CODE);
                        }
                    }
                })
                .show();
    }

    private void checkLocationStatus() {
        Log.i(TAG, "checkLocationStatus");
        if(Utilities.isLocationEnabled(this)) {
            goToMainActivity();
        } else {
            //If location is not enabled, send the user to Settings
            goToLocationSettings();
        }
    }

    private void goToLocationSettings() {
        Log.i(TAG, "goToLocationSettings");
        Toast.makeText(this, "Please turn on location to scan for devices.", Toast.LENGTH_LONG).show();
        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_SETTINGS_REQUEST_CODE);
    }

    private void goToAppSettings() {
        Log.i(TAG, "goToAppSettings");
        Toast.makeText(this, "Please enable location permissions to continue", Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        final Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, APPLICATION_SETTINGS_REQUEST_CODE);
    }


    private void goToMainActivity() {
        Toast.makeText(this, "Location enabled.", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}
