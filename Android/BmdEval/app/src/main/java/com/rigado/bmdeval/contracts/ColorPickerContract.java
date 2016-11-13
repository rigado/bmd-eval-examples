package com.rigado.bmdeval.contracts;


import com.rigado.bmdeval.demodevice.devicedata.RgbColor;

public interface ColorPickerContract {

    interface View {
    }

    interface UserActionsListener {
        void setLedColor(RgbColor color);
    }
}
