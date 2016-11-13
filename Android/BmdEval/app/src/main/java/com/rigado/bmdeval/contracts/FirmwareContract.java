package com.rigado.bmdeval.contracts;

import android.content.Context;

import com.rigado.bmdeval.demodevice.DemoDevice;
import com.rigado.bmdeval.utilities.JsonFirmwareType;

public interface FirmwareContract {

    interface View {
        void updateProgressBar(int progress);
        void updateStatusText(String status);
        void setFirmwareUpdateCompleted(DemoDevice demoDevice);
        void setFirmwareUpdateFailed(String errorMessage);
        void setButtonEnabled(boolean enabled);
    }

    interface UserActionsListener {
        void programFirmware(Context context, JsonFirmwareType firmwareRecord);
    }
}
