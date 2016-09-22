package com.rigado.bmdeval.demodevice;

/**
 * Created by stutzenbergere on 7/27/15.
 */
public class AmbientLight {
    private int level;

    public AmbientLight() {
        level = 0;
    }

    public AmbientLight(byte adcReading) {
        level = (((adcReading * 1200) / 255) * 3) / 2;
    }

    public int getLevel() {
        return level;
    }

    public float getAlphaLevel() {
        return level / 400.0f;
    }
}
