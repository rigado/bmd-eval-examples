package com.rigado.bmdeval.devicedata.evaldemodevice;


public interface IEvalDeviceActions {

    void setLedColor(RgbColor color);
    RgbColor getLedColor();

    void startAmbientLightSensing();
    void stopAmbientLightSensing();

    void startAccelerometerStream();
    void stopAccelerometerStream();

    void setButtonNotificationsEnabled(boolean enable);
}
