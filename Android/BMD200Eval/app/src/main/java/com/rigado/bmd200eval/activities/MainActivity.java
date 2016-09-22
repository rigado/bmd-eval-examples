package com.rigado.bmd200eval.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.rigado.bmd200eval.BmdApplication;
import com.rigado.bmd200eval.R;
import com.rigado.bmd200eval.customviews.ControllableViewPager;
import com.rigado.bmd200eval.fragments.DemoFragment;
import com.rigado.bmd200eval.fragments.ColorPickerFragment;
import com.rigado.bmd200eval.fragments.FirmwareUpdateFragment;
import com.rigado.bmd200eval.fragments.AboutFragment;
import com.rigado.bmd200eval.interfaces.IFragmentLifecycleListener;
import com.rigado.bmd200eval.interfaces.IPermissionsRequestListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements
        ActionBar.TabListener,
        IPermissionsRequestListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    ActionBar mActionBar;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public ControllableViewPager mViewPager;
    private boolean mAllowTabsClicking = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BmdApplication.getInstance().getBmdManager().setContext(this);

        // Set up the action bar.
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);//hide app title
        mActionBar.setDisplayShowHomeEnabled(false);//hide app title
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ControllableViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);// set to 3 to keep all 4 pages alive

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private int oldPosition = 0;//by default the first tab

            @Override
            public void onPageSelected(int position) {
                mActionBar.setSelectedNavigationItem(position);

                // NOTE: onPauseFragment must always be called before onResumeFragment
                IFragmentLifecycleListener fragmentToHide = (IFragmentLifecycleListener)mSectionsPagerAdapter.getItem(oldPosition);
                fragmentToHide.onPauseFragment();

                IFragmentLifecycleListener fragmentToShow = (IFragmentLifecycleListener)mSectionsPagerAdapter.getItem(position);
                fragmentToShow.onResumeFragment();

                oldPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            mActionBar.addTab(
                    mActionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        BmdApplication.getInstance().getBmdManager().registerPermissionsListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        BmdApplication.getInstance().getBmdManager().registerPermissionsListener(null);

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        if (mAllowTabsClicking) {
            mViewPager.setCurrentItem(tab.getPosition());
        }
    }

    @Override
    public void onTabUnselected(final ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void setAllowTabsAndViewpagerSwitching(boolean toggle)
    {
        mViewPager.setPagingEnabled(toggle);
        mAllowTabsClicking = toggle;
    }

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    public void onPermissionsRequested() {
        Log.i(TAG, "onPermissionsRequested");
        startActivity(new Intent(this, PermissionActivity.class));
        finish();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> mFragmentList;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentList = new ArrayList<Fragment>();
            mFragmentList.add(new DemoFragment());
            mFragmentList.add(new ColorPickerFragment());
            mFragmentList.add(new FirmwareUpdateFragment());
            mFragmentList.add(new AboutFragment());
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);
            }
            return null;
        }
    }


}
