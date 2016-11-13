package com.rigado.bmdeval.contracts;

import android.content.Context;

import com.rigado.bmdeval.utilities.JsonFirmwareType;

public interface FirmwareContract {

    interface View {
        void updateProgressBar(int progress);
        void updateStatusText(String status);
        void setFirmwareUpdateCompleted();
        void setFirmwareUpdateFailed(String errorMessage);
    }

    interface UserActionsListener {
        void programFirmware(Context context, JsonFirmwareType firmwareRecord);
    }
}
