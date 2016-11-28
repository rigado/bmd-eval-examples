package com.rigado.bmdeval.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rigado.bmdeval.R;
import com.rigado.bmdeval.interfaces.IFragmentLifecycleListener;

public class AboutFragment extends Fragment implements IFragmentLifecycleListener {

    public AboutFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onPauseFragment() {
        //Noop.
    }

    @Override
    public void onResumeFragment() {
        //Noop.
    }
}
