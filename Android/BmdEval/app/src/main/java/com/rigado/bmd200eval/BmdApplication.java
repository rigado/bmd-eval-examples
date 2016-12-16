package com.rigado.bmd200eval;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import com.rigado.rigablue.RigCoreBluetooth;

import io.fabric.sdk.android.Fabric;


public class BmdApplication extends Application  {

    @Override
    public void onCreate() {
        super.onCreate();

        // Required initialization
        RigCoreBluetooth.initialize(this);

        /*Prevent Crashlytics from sending crash reports while in development*/
        CrashlyticsCore core = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());
    }
}
