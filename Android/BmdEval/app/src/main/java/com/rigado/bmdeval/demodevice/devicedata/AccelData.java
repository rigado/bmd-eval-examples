package com.rigado.bmdeval.demodevice.devicedata;

/**
 * Created by stutzenbergere on 7/27/15.
 */
public class AccelData {
    float x;
    float y;
    float z;

    public AccelData() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public AccelData(byte x, byte y, byte z) {
        this.x = x/16.0f;
        this.y = y/16.0f;
        this.z = z/16.0f;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }
}
