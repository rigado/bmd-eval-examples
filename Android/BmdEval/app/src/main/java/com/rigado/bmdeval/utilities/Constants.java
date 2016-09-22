package com.rigado.bmdeval.utilities;


public class Constants {

    public static final String BLINKY_RESET_SERVICE_UUID = "6d580001-fc91-486b-82c4-86a1d2eb8f88";
    public static final String BLINKY_UUID_CTRL_CHAR = "6d580002-fc91-486b-82c4-86a1d2eb8f88";

    public static final String BLINKY_DEMO_DEVICE_NAME = "Blinky";

    public static final String BMDEVAL_UUID_SERVICE = "50db1523-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_BUTTON_CHAR = "50db1524-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_LED_CHAR = "50db1525-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_ADC_CHAR = "50db1526-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_CTRL_CHAR = "50db1527-418d-4690-9589-ab7be9e22684";
    public static final String BMDEVAL_UUID_ACCEL_CHAR = "50db1528-418d-4690-9589-ab7be9e22684";

    public static final String DIS_UUID_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final String DIS_UUID_MODEL_NUM = "00002a24-0000-1000-8000-00805f9b34fb";
    public static final String DIS_UUID_SERIAL_NUM = "00002a25-0000-1000-8000-00805f9b34fb";
    public static final String DIS_UUID_FIRMWARE_VER = "00002a26-0000-1000-8000-00805f9b34fb";
    public static final String DIS_UUID_MFG_NAME = "00002a29-0000-1000-8000-00805f9b34fb";

    public static final byte EVAL_CMD_ADC_STREAM_START = 0x01;
    public static final byte EVAL_CMD_ADC_STREAM_STOP = 0x02;
    public static final byte EVAL_CMD_ACCEL_STREAM_START = 0x06;
    public static final byte EVAL_CMD_ACCEL_STREAM_STOP = 0x09;
    public static final byte[] CMD_HARDWARE_VERSION = { 0x0A };

    public final static int DEMO_STATUS_FRAGMENT = 0;
    public final static int COLOR_WHEEL_FRAGMENT = 1;
    public final static int FIRMWARE_UPDATE_FRAGMENT = 2;
    public final static int ABOUT_FRAGMENT = 3;

}
