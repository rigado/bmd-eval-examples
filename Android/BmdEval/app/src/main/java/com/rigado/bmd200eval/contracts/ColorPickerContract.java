package com.rigado.bmd200eval.contracts;


import com.rigado.bmd200eval.demodevice.devicedata.RgbColor;

public interface ColorPickerContract {

    interface View {
    }

    interface UserActionsListener {
        void setLedColor(RgbColor color);
    }
}
