package com.rigado.bmdeval.contracts;

import com.rigado.bmdeval.devicedata.evaldemodevice.AccelData;
import com.rigado.bmdeval.devicedata.evaldemodevice.AmbientLight;
import com.rigado.bmdeval.devicedata.evaldemodevice.ButtonStatus;

public interface DemoContract {

    interface View {
        void updateButtonStatus(ButtonStatus status);
        void updateAccelStream(AccelData accelData);
        void updateAmbientLight(AmbientLight ambientLight);
    }

    interface UserActionsListener {

    }
}
