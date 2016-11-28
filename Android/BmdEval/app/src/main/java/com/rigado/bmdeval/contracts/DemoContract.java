package com.rigado.bmdeval.contracts;

import com.rigado.bmdeval.demodevice.devicedata.AccelData;
import com.rigado.bmdeval.demodevice.devicedata.AmbientLight;
import com.rigado.bmdeval.demodevice.devicedata.ButtonStatus;

public interface DemoContract {

    interface View {
        void updateButtonStatus(ButtonStatus status);
        void updateAccelStream(AccelData accelData);
        void updateAmbientLight(AmbientLight ambientLight);
    }

    interface UserActionsListener {

    }
}
