//
//  BMD200EvalDemoDevice.h
//  BMD200EvalDemo
//
//  Created by Eric P. Stutzenberger on 7/6/15.
//  Copyright (c) 2015 Rigado,LLC. All rights reserved.
//
//  Source code licensed under BMD-200 Software License Agreement.
//  You should have received a copy with purchase of BMD-200 product.
//  If not, contact info@rigado.com for for a copy.

//  This class handles all communications with the evaluation board demo firmware.

#import <Foundation/Foundation.h>
#import "Rigablue.h"

#define BMDEVAL_BASE_UUID       @"50db0000-418d-4690-9589-ab7be9e22684"
#define BMDEVAL_UUID_SERVICE    @"1523"
#define BLINKY_SERVICE          @"6d580001-fc91-486b-82c4-86a1d2eb8f88"
#define BLINKY_CHAR             @"6d580002-fc91-486b-82c4-86a1d2eb8f88"
#define BMDWARE_RESET_SERVICE   @"2413B33F-707F-90BD-2045-2AB8807571B7"
#define BMDWARE_RESET_CHAR      @"2413B43F-707F-90BD-2045-2AB8807571B7"

/**
 *  Structure defining an RGB color.
 */
typedef struct RgbColor_struct
{
    uint8_t red;
    uint8_t green;
    uint8_t blue;
} RgbColor_t;

/**
 *  Structure to hold data from the Accelerometer.
 */
typedef struct AccelData_struct
{
    uint8_t x;
    uint8_t y;
    uint8_t z;
} AccelData_t;

@protocol BMD200EvalDemoDeviceDelegate <NSObject>

@optional
/**
 *  This function is called when the state of either button changes.
 *
 *  @param data     The current button data.  See this function in StatusViewController.m for processing information.
 */
- (void)didUpdateButtonData:(uint8_t)data;

/**
 *  This function is called when the LED color changes state asynchrnously.  At this time, nothing in the firmware
 *  causes this function to be called.
 *
 *  @param color    The current color of the LED.
 */
- (void)didUpdateLedColor:(RgbColor_t)color;

/**
 *  This function is called when a new sample from the ADC arrives.  For this demonstration, the ADC is configured to
 *  gather readings from the ambient light sensor.
 *
 *  @param adc      The adc value reading.  See this function StatusViewController.m for processing information.
 */
- (void)didUpdateAdcData:(uint8_t)adc;

/**
 *  This function is called when a new sample from the Accelerometer arrives.  For this demonstration, only the raw
 *  X, Y, and Z axis data values are reported.  The data are reported as 8-bit signed integers.
 */
- (void)didUpdateAccelData:(AccelData_t)accelData;

/**
 *  This function is called when the app determines the hardware version.
 */
- (void)didDiscoverHardwareVersion;

/**
 *  This function is called if the app can not determin the hardware version.
 */
- (void)unableToDiscoverHardwareVersion;

@end

@interface BMD200EvalDemoDevice : NSObject

- (id)initWithDevice:(RigLeBaseDevice*)device;
- (RigLeBaseDevice*)getBaseDevice;
- (void)determineDeviceHardwareVersion;

/**
*  This property reports if the device is a 200.
*/
@property (assign, readonly) BOOL is200;

/**
 *  This property reports if the device is a 300.
 */
@property (assign, readonly) BOOL is300;

/**
 *  This property reports if the device type can not be determined.
 */
@property (assign, readonly) BOOL isIndeterminatableState;

/**
 *  This property reports if the Button is available on the demo firmware.
 */
@property (nonatomic, readonly) BOOL isButtonAvailable;

/**
 *  This property reports if the LED is available on the demo firmware.
 */
@property (nonatomic, readonly) BOOL isLedAvailable;

/**
 *  This property reports if ADC reports are available on the demo firmware.
 */
@property (nonatomic, readonly) BOOL isAdcAvailable;

/**
 *  This property reports if Accelerometer data is available on the demo firmware.
 */
@property (nonatomic, readonly) BOOL isAccelAvailable;

@property (nonatomic, weak) id<BMD200EvalDemoDeviceDelegate> delegate;

/**
 *  This method sets the LED color.  If the LED is not available, this method will do nothing.
 *
 *  @param color    The color to set.
 */
- (void)setLedColor:(RgbColor_t)color;

/**
 *  This method reads the current value of the LED.
 *
 *  @return The current LED color.
 */
- (RgbColor_t)getLedColor;

/**
 *  This method starts the streaming of ADC data from the ambient light sensor.  If the senor interface
 *  is not available, this method will do nothing.
 */
- (void)startAmbientLightSensing;

/**
 *  This method stops the streaming of ADC data from the ambient light sensor.  If the senor interface
 *  is not available, this method will do nothing.
 */
- (void)stopAmbientLightSensing;

/**
 *  This method starts the streaming of accelerometer data.  If the senor interface is not available,
 *  this method will do nothing.
 */
- (void)startAccelerometer;

/**
 *  This method stops the streaming of accelerometer data.  If the senor interface is not available,
 *  this method will do nothing.
 */
- (void)stopAccelerometer;
@end
