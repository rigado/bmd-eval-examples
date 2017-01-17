package com.rigado.bmd200eval.presenters;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.rigado.bmd200eval.contracts.FirmwareContract;
import com.rigado.bmd200eval.datasource.DeviceRepository;
import com.rigado.bmd200eval.demodevice.DemoDevice;
import com.rigado.bmd200eval.utilities.JsonFirmwareType;
import com.rigado.rigablue.IRigFirmwareUpdateManagerObserver;
import com.rigado.rigablue.RigDfuError;
import com.rigado.rigablue.RigFirmwareUpdateManager;

import java.io.InputStream;

public class FirmwarePresenter extends BasePresenter implements
        FirmwareContract.UserActionsListener,
        IRigFirmwareUpdateManagerObserver {

    private static final String TAG = FirmwarePresenter.class.getSimpleName();

    private FirmwareContract.View firmwareView;
    private DemoDevice demoDevice;
    private Handler uiThreadHandler;

    public FirmwarePresenter(FirmwareContract.View view) {
        this.firmwareView = view;
        this.demoDevice = DeviceRepository
                .getInstance()
                .getConnectedDevice();

        uiThreadHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onResume() {
        if (rigFirmwareUpdateManager != null) {
            rigFirmwareUpdateManager.setObserver(this);
        }

        if (!demoDevice.isUpdating()) {
            firmwareView.reset();
        }
    }

    @Override
    public void onPause() {
        if (rigFirmwareUpdateManager != null) {
            Log.i(TAG, "onPause");
            rigFirmwareUpdateManager.setObserver(null);
            demoDevice.setUpdatingStatus(false);
            rigFirmwareUpdateManager.cancelUpdate();
        }
    }

    //Keep reference if we decide to cancel firmware update
    private RigFirmwareUpdateManager rigFirmwareUpdateManager;

    @Override
    public void programFirmware(Context context, JsonFirmwareType firmwareRecord) {
        if (firmwareRecord == null) {
            return;
        }

        String fileName = demoDevice.is200() ? firmwareRecord.getProperties().getFilename200()
                : firmwareRecord.getProperties().getFilename300();
        Log.i(TAG, "filename " + fileName);

        //ensure filenames contain no extension
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        final int fileId = context.getResources()
                .getIdentifier(fileName, "raw", context.getPackageName());
        final InputStream inputStream =
                (fileId != 0) ?
                        context.getResources().openRawResource(fileId) : null;

        if (inputStream == null) {
            return;
        }

        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                firmwareView.setWindowFlagEnabled(true);
                firmwareView.setButtonEnabled(false);
            }
        });


        rigFirmwareUpdateManager = new RigFirmwareUpdateManager();
        rigFirmwareUpdateManager.setObserver(this);
        demoDevice.setUpdatingStatus(true);
        demoDevice.startFirmwareUpdate(rigFirmwareUpdateManager, inputStream);
    }

    @Override
    public void updateProgress(final int progress) {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                firmwareView.updateProgressBar(progress);
            }
        });
    }

    @Override
    public void updateStatus(final String status, int error) {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                firmwareView.updateStatusText(status);
            }
        });
    }

    @Override
    public void didFinishUpdate() {
        Log.i(TAG, "didFinishUpdate");
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                firmwareView.updateStatusText("To complete the firmware update, please reset your bluetooth and restart the app.");
                firmwareView.setWindowFlagEnabled(false);
                firmwareView.setFirmwareUpdateCompleted(demoDevice);
            }
        });

    }

    @Override
    public void updateFailed(final RigDfuError error) {
        Log.i(TAG, "updateFailed");
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                firmwareView.setWindowFlagEnabled(false);
                firmwareView.setButtonEnabled(true);
                firmwareView.setFirmwareUpdateFailed(error.getErrorMessage());
            }
        });
    }
}
