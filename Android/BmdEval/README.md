# Working with the Eval Demo Device
The demo device has four main operational elements: LED, Button, Ambient Light, and Accelerometer.  Each element is described below. In addition, you can choose to update the firmware to the Blinky Demo or BMDware, the default firmware shipped with the Rigado evaluation kit.

**Note :**
BMDware's default connectable advertising interval is set to 2000ms. This makes it difficult to connect on most Android devices.

## Button
The button element provides the status of the buttons when either button’s state changes.  Only changes are represented. If a button is being held down, there will not be any further indication it has changed state until the button is released. `ButtonStatus` contains two state variables, `isUser1Pressed`, and `isUser2Pressed` (these numbers match the button labels on the Evaluation board).
If the value is set to true, the button is pressed.  If it is false, the button is not pressed.

## LED
The LED element controls the on board RGB (Red, Green, Blue) LED.  The LED data is represented by `RbgColor`. This class contains red, green, and blue variables.  To set the current LED color, call `DemoDevice#setLedColor(RgbColor color)`  To get the current color, call `DemoDevice#getLedColor`.

## Ambient Light
The ambient light element provides a read back of the ambient light level.  This functionality shows off the ADC of the nrf51 IC. To receive ambient light level readings, call `DemoDevice#startAmbientLightSensing`. Once started, ambient light level readings will be sent by the demo device until  `DemoDevice#stopAmbientLightSensing` is called or a disconnect occurs.

The ambient light data is supplied via the `AmbientLight` helper class. In addition to the raw ambient light level (which is 0 to 1800 mV), a scaled alpha value is provided to fade the ambient light sensing box in and out. As the light intensity increases, the box becomes more opaque.
As the light intensity decreases, the box becomes more transparent.  The value of the current reading should also be displayed in millivolts.
This reading is provided by the `AmbientLight#getLevel`.

## Accelerometer
The accelerometer element provides X, Y, and Z axis data from the on board accelerometer.
Once enabled, accelerometer data packets are transmitted to the connected app at 12.5 Hz (or ~every 80 ms).
The X, Y, and Z data points should be graphed.  The following colors are assigned to the graph lines:
X - Red
Y - Blue
Z - Green

Accelerometer data is supplied by the helper class `AccelData`. This class has methods for retrieving the value of each axis.  To start receiving accelerometer data, call the `DemoDevice#startAccelerometer`. Once started, accelerometer data will continue to be sent until `DemoDevice#stopAccelerometer` is called or a disconnect occurs.

## Firmware Update
In addition to all of the above, the `DemoDevice` assigns the bootloader control point characteristic and bootloader command during `initServices`. Simply call `DemoDevice#startFirmwareUpdate(RigFirmwareUpdateManager manager, InputStream inputStream)` to begin a secure firmware update.

## Other types of Demo Devices
The app will also connect to devices running two other types of firmwares. This is because evaluation kits are shipped with BMDware, which does not work with the demo app.  In addition, there is also the Blinky Demo example firmware that can be programmed from the demo app. This firmware is included to provide the ability to demonstrate the Rigado firmware update process.
