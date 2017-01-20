package com.rigado.bmd200eval.contracts;

import com.rigado.bmd200eval.demodevice.devicedata.AccelData;
import com.rigado.bmd200eval.demodevice.devicedata.AmbientLight;
import com.rigado.bmd200eval.demodevice.devicedata.ButtonStatus;

public interface DemoContract {

    interface View {
        void updateButtonStatus(ButtonStatus status);
        void updateAccelStream(AccelData accelData);
        void updateAmbientLight(AmbientLight ambientLight);
    }

    interface UserActionsListener {

    }
}
