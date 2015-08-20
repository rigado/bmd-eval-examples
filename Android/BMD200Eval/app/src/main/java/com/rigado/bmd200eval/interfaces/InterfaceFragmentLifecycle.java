package com.rigado.bmd200eval.interfaces;

/**
 * Fragment lifecycle is changed by the ViewPager due to the updated fragment creation
 * This interface is intended to indicate when the fragment is truly resumed and paused
 */
public interface InterfaceFragmentLifecycle {
    void onPauseFragment();
    void onResumeFragment();
}
