package com.rigado.bmdeval.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.rigado.bmdeval.R;
import com.rigado.bmdeval.adapters.SectionsPagerAdapter;
import com.rigado.bmdeval.contracts.MainContract;
import com.rigado.bmdeval.customviews.ControllableViewPager;
import com.rigado.bmdeval.demodevice.DemoDevice;
import com.rigado.bmdeval.interfaces.IFragmentLifecycleListener;
import com.rigado.bmdeval.presenters.MainPresenter;
import com.rigado.bmdeval.utilities.Utilities;
import com.rigado.rigablue.RigCoreBluetooth;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SectionsPagerAdapter mSectionsPagerAdapter;
    public ControllableViewPager mViewPager;
    private ProgressDialog mDiscoveryDialog;

    private int oldPosition = 0;//by default the first tab

    public MainPresenter mainPresenter;

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
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                this,
                getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ControllableViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);// set to 3 to keep all 4 pages alive

        /** Resume and Pause fragments in {@link #onPageSelected} */
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // NOTE: onPauseFragment must always be called before onResumeFragment
                IFragmentLifecycleListener fragmentToHide =
                        (IFragmentLifecycleListener)mSectionsPagerAdapter.getItem(oldPosition);
                fragmentToHide.onPauseFragment();

                IFragmentLifecycleListener fragmentToShow =
                        (IFragmentLifecycleListener)mSectionsPagerAdapter.getItem(position);
                fragmentToShow.onResumeFragment();

                oldPosition = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageScrolled(int position,
                                       float positionOffset,
                                       int positionOffsetPixels) {}
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.activity_device_tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        mainPresenter = new MainPresenter(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLocationPermissions();
        mainPresenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mainPresenter.onPause();
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

    @Override
    public void setBluetoothState(boolean enabled) {
        if (enabled) {
            RigCoreBluetooth.initialize(getApplicationContext());
        } else {
            //TODO: Set disabled state
        }
    }

    @Override
    public void onInterrogationCompleted(final DemoDevice deviceType) {
        Log.d(TAG, "onInterrogationCompleted");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDiscoveryDialog.dismiss();
                mSectionsPagerAdapter.setConnected(true);
                mSectionsPagerAdapter.destroyCache();
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
        });
    }

    @Override
    public void onInterrogationFailed(DemoDevice demoDevice) {
        new AlertDialog.Builder(this)
                .setTitle("Interrogation Failed!")
                .setMessage("Failed to find device hardware version. Try resetting your bluetooth and restarting the app.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mDiscoveryDialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void updateDialog(final String message) {
        Log.i(TAG, "updateDialog");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mDiscoveryDialog.isShowing()) {
                    mDiscoveryDialog.show();
                }

                mDiscoveryDialog.setMessage(message);
            }

        });
    }

    @Override
    public void deviceDisconnected(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            // Only allow reconnect attempts if a firmware update is not in progress or
            // has not been successfully completed.
            mSectionsPagerAdapter.setConnected(false);
            mSectionsPagerAdapter.destroyCache();
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

        });
    }

}
