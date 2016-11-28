package com.rigado.bmdeval.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import com.rigado.bmdeval.R;
import com.rigado.bmdeval.fragments.AboutFragment;
import com.rigado.bmdeval.fragments.ColorPickerFragment;
import com.rigado.bmdeval.fragments.DemoFragment;
import com.rigado.bmdeval.fragments.FirmwareUpdateFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding
 * to one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    private Context mContext;
    private boolean isConnected;

    public final static int DEMO_STATUS_FRAGMENT = 0;
    public final static int COLOR_WHEEL_FRAGMENT = 1;
    public final static int FIRMWARE_UPDATE_FRAGMENT = 2;
    public final static int ABOUT_FRAGMENT = 3;

    private List<Fragment> mFragmentList;

    public static final String CONNECTION_STATE =
            "com.rigado.bmdeval.SectionsPagerAdapter.CONNECT_STATE";

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.mContext = context;
        this.isConnected = false;
        mFragmentList = new ArrayList<>();
        createFragments();
    }

    public void destroyCache() {
        mFragmentList.clear();
        createFragments();
    }

    public void createFragments() {
        mFragmentList.add(DemoFragment.newInstance(isConnected));
        mFragmentList.add(ColorPickerFragment.newInstance(isConnected));
        mFragmentList.add(FirmwareUpdateFragment.newInstance(isConnected));
        mFragmentList.add(new AboutFragment());
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case DEMO_STATUS_FRAGMENT:
                return mContext.getString(R.string.title_demo_fragment).toUpperCase(l);
            case COLOR_WHEEL_FRAGMENT:
                return mContext.getString(R.string.title_color_fragment).toUpperCase(l);
            case FIRMWARE_UPDATE_FRAGMENT:
                return mContext.getString(R.string.title_firmware_fragment).toUpperCase(l);
            case ABOUT_FRAGMENT:
                return mContext.getString(R.string.title_about_fragment).toUpperCase(l);
        }
        return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (position >= getCount()) {
            FragmentManager fragmentManager = ((Fragment) object).getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove((Fragment) object);
            fragmentTransaction.commitNow();
        }

        super.destroyItem(container, position, object);
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}
