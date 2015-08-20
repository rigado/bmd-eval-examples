package com.rigado.bmd200eval.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rigado.bmd200eval.R;
import com.rigado.bmd200eval.interfaces.InterfaceFragmentLifecycle;

public class FragmentScreen4 extends Fragment implements InterfaceFragmentLifecycle {

    //Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
    public FragmentScreen4(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //inflate the necessary layout
        View rootView = inflater.inflate(R.layout.fragment_screen4, container, false);

        return rootView;
    }

    // ************
    //  Concrete Implementation of InterfaceFragmentLifecycle
    // ************
    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {

    }
}
