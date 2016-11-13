package com.rigado.bmdeval.presenters;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.rigado.bmdeval.contracts.FirmwareContract;
import com.rigado.bmdeval.datasource.DeviceRepository;
import com.rigado.bmdeval.demodevice.DemoDevice;
import com.rigado.bmdeval.utilities.JsonFirmwareType;
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

    }

    @Override
    public void onPause() {

    }

    //Keep reference if we decide to cancel firmware update
    private RigFirmwareUpdateManager rigFirmwareUpdateManager;

    @Override
    public void programFirmware(Context context, JsonFirmwareType firmwareRecord) {
        Log.i(TAG, "firmware record name " + firmwareRecord.getFwname());

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
                firmwareView.setButtonEnabled(true);
                firmwareView.setFirmwareUpdateFailed(error.getErrorMessage());
            }
        });
    }
}
