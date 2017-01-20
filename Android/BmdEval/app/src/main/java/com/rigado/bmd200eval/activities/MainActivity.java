package com.rigado.bmd200eval.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rigado.bmd200eval.adapters.SectionsPagerAdapter;
import com.rigado.bmd200eval.contracts.MainContract;
import com.rigado.bmd200eval.customviews.ControllableViewPager;
import com.rigado.bmd200eval.datasource.DeviceRepository;
import com.rigado.bmd200eval.demodevice.DemoDevice;
import com.rigado.bmd200eval.presenters.MainPresenter;
import com.rigado.bmd200eval.utilities.Utilities;
import com.rigado.bmd200eval.R;
import com.rigado.rigablue.RigCoreBluetooth;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SectionsPagerAdapter mSectionsPagerAdapter;
    public ControllableViewPager mViewPager;
    private ProgressDialog mDiscoveryDialog;

    public MainPresenter mainPresenter;

    private Toolbar toolbar;

    private static final String ACTION_INTENT_PROVIDERS_CHANGED =
            "android.location.PROVIDERS_CHANGED";

    private BroadcastReceiver gpsStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches(ACTION_INTENT_PROVIDERS_CHANGED)) {
                checkLocationPermissions();
                mainPresenter.onResume();

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLocationPermissions();

        mDiscoveryDialog = new ProgressDialog(this);
        mDiscoveryDialog.setIndeterminate(true);
        mDiscoveryDialog.setIndeterminateDrawable(
                ContextCompat.getDrawable(this, R.drawable.progressbar));
        mDiscoveryDialog.setCancelable(false);

        // Set up the toolbar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ControllableViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);// set to 3 to keep all 4 pages alive

        TabLayout tabLayout = (TabLayout) findViewById(R.id.activity_device_tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
        mBluetoothEnabled = true;
        mainPresenter = new MainPresenter(this);

    }

    /**
     * Uncomment to continue work on allowing scanning after a firmware update
     */

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.scan:
//                mainPresenter.maybeStartScanning();
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(gpsStateReceiver, new IntentFilter(ACTION_INTENT_PROVIDERS_CHANGED));
        checkLocationPermissions();
        mainPresenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(gpsStateReceiver);
        mainPresenter.onPause();
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
        } catch (NullPointerException e) {
            Log.d(TAG, "ViewPager NPE bug workaround");
        }
    }

    private void checkLocationPermissions() {
        boolean locationEnabled = false;
        if (Utilities.hasLocationPermission(this)
                && Utilities.isLocationEnabled(this)) {
            locationEnabled = true;
        }

        if(!locationEnabled) {
            goToPermissionsActivity();
        }
    }

    private void goToPermissionsActivity() {
        startActivity(new Intent(this, PermissionActivity.class));
        finish();
    }

    private boolean mBluetoothEnabled;

    @Override
    public void setBluetoothState(boolean enabled) {
        mBluetoothEnabled = enabled;
        if (enabled) {
            RigCoreBluetooth.initialize(getApplicationContext());
            mainPresenter.maybeStartScanning();
            toolbar.setSubtitle("");
        } else {
            toolbar.setSubtitle("Bluetooth Disabled");
            toolbar.setSubtitleTextColor(ContextCompat.getColor(this, android.R.color.white));
            dismissDiscoveryDialog();
        }
    }

    @Override
    public void onInterrogationCompleted(final DemoDevice deviceType) {
        Log.d(TAG, "onInterrogationCompleted");
        if (isDestroyed()) {
            return;
        }
            dismissDiscoveryDialog();
            mSectionsPagerAdapter.notifyDataSetChanged();
            if (deviceType.getFirmwareType() != DemoDevice.FirmwareType.EvalDemo) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Update to demo firmware?")
                        .setMessage("The device is currently running "
                                + deviceType.getFirmwareType().getDescription()
                                + " firmware. Would you like to update to the"
                                + " main demo firmware?")
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mViewPager.setCurrentItem(
                                        SectionsPagerAdapter.FIRMWARE_UPDATE_FRAGMENT);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Noop.
                            }
                        })
                        .show();
            }

    }

    @Override
    public void updateDialog(final String message) {
        Log.i(TAG, "updateDialog");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mDiscoveryDialog.isShowing() && mBluetoothEnabled) {
                    mDiscoveryDialog.show();
                }

                mDiscoveryDialog.setMessage(message);
            }

        });
    }

    @Override
    public void deviceDisconnected(final String reason) {
        if (isDestroyed()) {
            return;
        }

        if (DeviceRepository.getInstance().isDeviceConnected()) {
            return;
        }
        // Only allow reconnect attempts if a firmware update is not in progress or
        // has not been successfully completed.
        mSectionsPagerAdapter.notifyDataSetChanged();

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Device Disconnected")
                .setMessage("Try Reconnecting?")
                .setPositiveButton("Reconnect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mainPresenter.requestReconnect();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       mainPresenter.maybeStartScanning();
                    }
                })
                .setCancelable(false)
                .show();
        }

    private AlertDialog mDeviceLockedDialog;

    @Override
    public void updateDeviceLocked(String title) {
        if (mDeviceLockedDialog != null && mDeviceLockedDialog.isShowing()) {
            return;
        }

        dismissDiscoveryDialog();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_password, null);
        final TextInputEditText passwordEditText =
                (TextInputEditText) dialogView.findViewById(R.id.dialog_password_edit_text);
        mDeviceLockedDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton("Unlock", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String maybePassword = passwordEditText.getText().toString();
                        if (TextUtils.isEmpty(maybePassword)
                                || !Utilities.isValidPassword(maybePassword)) {
                            Log.i(TAG, "Invalid password!");
                            return;
                        }
                        mainPresenter.unlockDevice(maybePassword);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Noop
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void dismissDialogs() {
        dismissDiscoveryDialog();
    }

    private void dismissDiscoveryDialog() {
        if (mDiscoveryDialog != null && mDiscoveryDialog.isShowing()) {
            mDiscoveryDialog.dismiss();
        }

        if (mDeviceLockedDialog != null && mDeviceLockedDialog.isShowing()) {
            mDeviceLockedDialog.dismiss();
        }
    }

}