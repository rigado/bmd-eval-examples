package com.rigado.bmd200eval.contracts;

import android.content.Context;

import com.rigado.bmd200eval.demodevice.DemoDevice;
import com.rigado.bmd200eval.utilities.JsonFirmwareType;

public interface FirmwareContract {

    interface View {
        void updateProgressBar(int progress);
        void updateStatusText(String status);
        void setFirmwareUpdateCompleted(DemoDevice demoDevice);
        void setFirmwareUpdateFailed(String errorMessage);
        void setButtonEnabled(boolean enabled);
        void setWindowFlagEnabled(boolean enabled);
    }

    interface UserActionsListener {
        void programFirmware(Context context, JsonFirmwareType firmwareRecord);
    }
}
