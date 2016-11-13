package com.rigado.bmdeval.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

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
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragmentList;
    private Context mContext;

    public final static int DEMO_STATUS_FRAGMENT = 0;
    public final static int COLOR_WHEEL_FRAGMENT = 1;
    public final static int FIRMWARE_UPDATE_FRAGMENT = 2;
    public final static int ABOUT_FRAGMENT = 3;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.mContext = context;
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

}
