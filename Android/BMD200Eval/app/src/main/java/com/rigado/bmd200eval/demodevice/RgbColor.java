package com.rigado.bmd200eval.demodevice;

/**
 * Created by stutzenbergere on 7/27/15.
 */
public class RgbColor {
    byte red;
    byte green;
    byte blue;

    public RgbColor() {
        this.red = 0;
        this.green = 0;
        this.blue = 0;
    }

    public RgbColor(int red, int green, int blue) {
        this.red = (byte)red;
        this.green = (byte)green;
        this.blue = (byte)blue;
    }

    public byte getRed() {
        return this.red;
    }

    public byte getGreen() {
        return this.green;
    }

    public byte getBlue() {
        return this.blue;
    }
}
