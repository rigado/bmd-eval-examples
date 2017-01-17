package com.rigado.bmd200eval.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import com.rigado.bmd200eval.fragments.AboutFragment;
import com.rigado.bmd200eval.fragments.ColorPickerFragment;
import com.rigado.bmd200eval.fragments.DemoFragment;
import com.rigado.bmd200eval.fragments.FirmwareUpdateFragment;

/**
 * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding
 * to one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    private final String[] tabTitles = {
            DemoFragment.TITLE,
            ColorPickerFragment.TITLE,
            FirmwareUpdateFragment.TITLE,
            AboutFragment.TITLE
    };

    public static final int DEMO_FRAGMENT = 0;
    public static final int COLOR_PICKER_FRAGMENT = 1;
    public static final int FIRMWARE_UPDATE_FRAGMENT = 2;
    public static final int ABOUT_FRAGMENT = 3;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case DEMO_FRAGMENT:
                return DemoFragment.newInstance();
            case COLOR_PICKER_FRAGMENT:
                return ColorPickerFragment.newInstance();
            case FIRMWARE_UPDATE_FRAGMENT:
                return FirmwareUpdateFragment.newInstance();
            case ABOUT_FRAGMENT:
                return new AboutFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return tabTitles.length;
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

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}
