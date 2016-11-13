package com.rigado.bmdeval.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.rigado.bmdeval.R;
import com.rigado.bmdeval.adapters.SectionsPagerAdapter;
import com.rigado.bmdeval.contracts.MainContract;
import com.rigado.bmdeval.customviews.ControllableViewPager;
import com.rigado.bmdeval.devicedata.otherdevices.BleDevice;
import com.rigado.bmdeval.devicedata.evaldemodevice.EvalDevice;
import com.rigado.bmdeval.devicedata.otherdevices.BlinkyDevice;
import com.rigado.bmdeval.devicedata.otherdevices.BmdwareDevice;
import com.rigado.bmdeval.interfaces.IFragmentLifecycleListener;
import com.rigado.bmdeval.utilities.Utilities;
import com.rigado.rigablue.RigCoreBluetooth;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ControllableViewPager mViewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLocationPermissions();

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

        // Resume and Pause fragments #onPageSelected
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private int oldPosition = 0;//by default the first tab

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
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLocationPermissions();
    }

    @Override
    public void onPause() {
        super.onPause();
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
    public void onInterrogationCompleted(BleDevice deviceType) {
        if (deviceType instanceof BlinkyDevice
                || deviceType instanceof BmdwareDevice) {

        } else if (deviceType instanceof EvalDevice) {

        } else {
            //TODO : Handle odd device connections
        }
    }
}
