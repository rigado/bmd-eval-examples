package com.rigado.bmd200eval.demodevice;

/**
 * Created by stutzenbergere on 7/27/15.
 */
public interface BMD200EvalDemoDeviceObserver {
    void didUpdateButtonData(ButtonStatus status);
    void didUpdateLedColor(RgbColor color);
    void didUpdateAmbientLightData(AmbientLight light);
    void didUpdateAccelData(AccelData data);
}
