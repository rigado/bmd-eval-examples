package com.rigado.bmd200eval.interfaces;


public interface IDeviceListener {

    interface
    void onReceiveButtonData(byte [] data);
    void onReceiveAmbientLightData(byte [] data);
    void onReceiveAccelerometerData(byte [] data);

    void onButtonNotifsEnabled();
    void onAmbientLightNotifsEnabled();
    void onAccelerometerDataNotifsEnabled();
}
