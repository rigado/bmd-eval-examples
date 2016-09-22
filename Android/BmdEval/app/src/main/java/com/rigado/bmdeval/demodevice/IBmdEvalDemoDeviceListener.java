package com.rigado.bmdeval.demodevice;

/**
 * Created by stutzenbergere on 7/27/15.
 */
public interface IBmdEvalDemoDeviceListener {
    void didUpdateButtonData(ButtonStatus status);
    void didUpdateLedColor(RgbColor color);
    void didUpdateAmbientLightData(AmbientLight light);
    void didUpdateAccelData(AccelData data);
}
