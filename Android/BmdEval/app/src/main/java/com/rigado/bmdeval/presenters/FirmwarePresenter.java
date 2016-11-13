package com.rigado.bmdeval.presenters;


import android.content.Context;
import android.util.Log;

import com.rigado.bmdeval.contracts.FirmwareContract;
import com.rigado.bmdeval.datasource.DeviceRepository;
import com.rigado.bmdeval.devicedata.IDeviceListener;
import com.rigado.bmdeval.devicedata.otherdevices.BleDevice;
import com.rigado.bmdeval.utilities.JsonFirmwareType;
import com.rigado.rigablue.RigDfuError;
import com.rigado.rigablue.RigFirmwareUpdateManager;

import java.io.InputStream;

public class FirmwarePresenter extends BasePresenter implements
        FirmwareContract.UserActionsListener,
        IDeviceListener.FirmwareUpdateListener {

    private static final String TAG = FirmwarePresenter.class.getSimpleName();

    private FirmwareContract.View firmwareView;
    private BleDevice bleDevice;

    public FirmwarePresenter(FirmwareContract.View view) {
        this.firmwareView = view;
        this.bleDevice = DeviceRepository
                .getInstance()
                .getConnectedDevice();
    }

    @Override
    public void onResume() {
        bleDevice.addFirmwareUpdateListener(this);

    }

    @Override
    public void onPause() {
        bleDevice.removeFirmwareUpdateListener(this);
    }

    @Override
    public void onReceiveProgress(int progress) {
        firmwareView.updateProgressBar(progress);
    }

    @Override
    public void onReceiveStatus(String status) {
        firmwareView.updateStatusText(status);
    }

    @Override
    public void onUpdateCompleted() {
        firmwareView.setFirmwareUpdateCompleted();
    }

    @Override
    public void onUpdateFailed(RigDfuError error) {
        firmwareView.setFirmwareUpdateFailed(error.getErrorMessage());
    }

    private RigFirmwareUpdateManager rigFirmwareUpdateManager;

    @Override
    public void programFirmware(Context context, JsonFirmwareType firmwareRecord) {
        Log.i(TAG, "firmware record name " + firmwareRecord.getFwname());

        if (firmwareRecord == null) {
            return;
        }

        String fileName = bleDevice.is200() ? firmwareRecord.getProperties().getFilename200()
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

        bleDevice.startFirmwareUpdate(inputStream);
    }
}
