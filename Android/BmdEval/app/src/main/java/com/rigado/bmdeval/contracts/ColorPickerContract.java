package com.rigado.bmdeval.contracts;


import com.rigado.bmdeval.devicedata.evaldemodevice.RgbColor;

public interface ColorPickerContract {

    interface View {
        void updateStatusButton(boolean enabled);


    }

    interface UserActionsListener {
        void setLedColor(RgbColor color);
        void setLedEnabled(boolean enabled);
    }
}
