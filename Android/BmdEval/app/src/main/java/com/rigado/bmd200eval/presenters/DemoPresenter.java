package com.rigado.bmd200eval.presenters;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.rigado.bmd200eval.contracts.DemoContract;
import com.rigado.bmd200eval.datasource.DeviceRepository;
import com.rigado.bmd200eval.demodevice.DemoDevice;
import com.rigado.bmd200eval.demodevice.devicedata.AccelData;
import com.rigado.bmd200eval.demodevice.devicedata.AmbientLight;
import com.rigado.bmd200eval.demodevice.devicedata.ButtonStatus;
import com.rigado.bmd200eval.interfaces.IDeviceListener;

public class DemoPresenter extends BasePresenter implements
        IDeviceListener.DemoData {

    private static final String TAG = DemoPresenter.class.getSimpleName();

    private DemoContract.View demoView;
    private DemoDevice demoDevice;
    private Handler uiThreadHandler;

    public DemoPresenter(DemoContract.View view) {
        this.demoView = view;
        demoDevice =
                DeviceRepository
                .getInstance()
                .getConnectedDevice();
        uiThreadHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onResume() {
        demoDevice.setDemoListener(this);
        demoDevice.startDemo();
    }

    @Override
    public void onPause() {
        demoDevice.setDemoListener(null);
        demoDevice.stopDemo();
    }

    @Override
    public void onReceiveButtonData(byte[] data) {
        final ButtonStatus status = new ButtonStatus(data[0]);
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                demoView.updateButtonStatus(status);
            }
        });
    }

    @Override
    public void onReceiveAmbientLightData(byte[] data) {
        final AmbientLight lightLevel = new AmbientLight(data[0]);
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                demoView.updateAmbientLight(lightLevel);
            }
        });
    }

    @Override
    public void onReceiveAccelerometerData(byte[] data) {
        final AccelData accelData = new AccelData(data[0], data[1], data[2]);
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                demoView.updateAccelStream(accelData);
            }
        });
    }

    //don't actually need this if enabling notifs becomes part of interrogation.
    //on interrogation completed, refresh all views
    // and on resume will call startDemo();
    @Override
    public void onDemoInitialized() {
        Log.i(TAG, "onDemoInitialized");
        demoDevice.startDemo();
    }
}
