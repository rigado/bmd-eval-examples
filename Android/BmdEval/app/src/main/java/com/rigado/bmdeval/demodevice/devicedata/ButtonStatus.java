package com.rigado.bmdeval.demodevice.devicedata;

/**
 * Created by stutzenbergere on 7/27/15.
 */
public class ButtonStatus {
    private static final byte USER_1_BUTTON_MASK = 0x01;
    private static final byte USER_2_BUTTON_MASK = 0x10;

    boolean isUser1Pressed;
    boolean isUser2Pressed;

    public ButtonStatus() {
        isUser1Pressed = false;
        isUser2Pressed = false;
    }

    public ButtonStatus(byte status) {
        isUser1Pressed = false;
        isUser2Pressed = false;

        if((status & USER_1_BUTTON_MASK) == USER_1_BUTTON_MASK) {
            isUser1Pressed = true;
        }

        if((status & USER_2_BUTTON_MASK) == USER_2_BUTTON_MASK) {
            isUser2Pressed = true;
        }
    }

    public boolean isUser1Pressed() {
        return this.isUser1Pressed;
    }

    public boolean isUser2Pressed() {
        return this.isUser2Pressed;
    }
}
