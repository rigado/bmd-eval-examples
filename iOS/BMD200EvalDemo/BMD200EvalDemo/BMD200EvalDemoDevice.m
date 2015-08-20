//
//  BMD200EvalDemoDevice.m
//  BMD200EvalDemo
//
//  Created by Eric P. Stutzenberger on 7/6/15.
//  Copyright (c) 2015 Rigado,LLC. All rights reserved.
//
//  Source code licensed under BMD-200 Software License Agreement.
//  You should have received a copy with purchase of BMD-200 product.
//  If not, contact info@rigado.com for for a copy.

#import "BMD200EvalDemoDevice.h"
#import "CBUUID+UUIDHelperMethods.h"
#import "Rigablue.h"

#define EVAL_CMD_ADC_STREAM_START       0x01
#define EVAL_CMD_ADC_STREAM_STOP        0x02
#define EVAL_CMD_ACCEL_STREAM_START     0x06
#define EVAL_CMD_ACCEL_STREAM_STOP      0x09

#define BMDEVAL_UUID_BUTTON_CHAR        @"1524"
#define BMDEVAL_UUID_LED_CHAR           @"1525"
#define BMDEVAL_UUID_ADC_CHAR           @"1526"
#define BMDEVAL_UUID_CTRL_CHAR          @"1527"
#define BMDEVAL_UUID_ACCEL_CHAR         @"1528"

#define DIS_SERVICE_UUID                @"180A"
#define DIS_MFG_NAME_UUID               @"2A29"
#define DIS_MODEL_NUMBER_UUID           @"2A24"
#define DIS_SERIAL_NUMBER_UUID          @"2A25"
#define DIS_FIRMWARE_REVISION_UUID      @"2A26"

@interface BMD200EvalDemoDevice() <RigLeBaseDeviceDelegate>
{
    CBUUID *demoServiceUuid;
    CBUUID *demoButtonCharUuid;
    CBUUID *demoLedCharUuid;
    CBUUID *demoAdcCharUuid;
    CBUUID *demoCtrlCharUuid;
    CBUUID *demoAccelCharUuid;
    
    CBService *demoService;
    CBCharacteristic *demoButtonChar;
    CBCharacteristic *demoLedChar;
    CBCharacteristic *demoAdcChar;
    CBCharacteristic *demoCtrlChar;
    CBCharacteristic *demoAccelChar;
    
    CBUUID *disServiceUuid;
    CBUUID *disMfgNameUuid;
    CBUUID *disModelNumberUuid;
    CBUUID *disSerialNumberUuid;
    CBUUID *disFirmwareRevisionUuid;
    
    CBService *disService;
    CBCharacteristic *disMfgNameChar;
    CBCharacteristic *disModelNumberChar;
    CBCharacteristic *disSerialNumberChar;
    CBCharacteristic *disFirmwareRevisionChar;
    
    RigLeBaseDevice *baseDevice;
}
@end

@implementation BMD200EvalDemoDevice

- (id)initWithDevice:(RigLeBaseDevice*)device
{
    self = [super init];
    if (self) {
        baseDevice = device;
        baseDevice.delegate = self;
        [self initUuidObjs];
        [self initServiceObjects];
    }
    
    return self;
}

- (void)initServiceObjects
{
    for (CBService *service in [baseDevice getSerivceList]) {
        if ([service.UUID isEqual:demoServiceUuid]) {
            demoService = service;
            [self populateDemoServiceCharacteristics];
        } else if ([service.UUID isEqual:disServiceUuid]) {
            disService = service;
            [self populateDisServiceCharacteristics];
        }
    }
}

- (void)initUuidObjs
{
    demoServiceUuid = [CBUUID uuidForBaseUuidString:BMDEVAL_BASE_UUID withShortUuidString:BMDEVAL_UUID_SERVICE];
    demoButtonCharUuid = [CBUUID uuidForBaseUuidString:BMDEVAL_BASE_UUID withShortUuidString:BMDEVAL_UUID_BUTTON_CHAR];
    demoLedCharUuid = [CBUUID uuidForBaseUuidString:BMDEVAL_BASE_UUID withShortUuidString:BMDEVAL_UUID_LED_CHAR];
    demoAdcCharUuid = [CBUUID uuidForBaseUuidString:BMDEVAL_BASE_UUID withShortUuidString:BMDEVAL_UUID_ADC_CHAR];
    demoCtrlCharUuid = [CBUUID uuidForBaseUuidString:BMDEVAL_BASE_UUID withShortUuidString:BMDEVAL_UUID_CTRL_CHAR];
    demoAccelCharUuid = [CBUUID uuidForBaseUuidString:BMDEVAL_BASE_UUID withShortUuidString:BMDEVAL_UUID_ACCEL_CHAR];
    
    disServiceUuid = [CBUUID UUIDWithString:DIS_SERVICE_UUID];
    disMfgNameUuid = [CBUUID UUIDWithString:DIS_MFG_NAME_UUID];
    disModelNumberUuid = [CBUUID UUIDWithString:DIS_MODEL_NUMBER_UUID];
    disSerialNumberUuid = [CBUUID UUIDWithString:DIS_SERIAL_NUMBER_UUID];
    disFirmwareRevisionUuid = [CBUUID UUIDWithString:DIS_FIRMWARE_REVISION_UUID];
}

- (void)populateDemoServiceCharacteristics
{
    for (CBCharacteristic *characteristic in demoService.characteristics) {
        if ([characteristic.UUID isEqual:demoButtonCharUuid]) {
            _isButtonAvailable = YES;
            demoButtonChar = characteristic;
            [baseDevice.peripheral setNotifyValue:YES forCharacteristic:demoButtonChar];
        } else if([characteristic.UUID isEqual:demoLedCharUuid]) {
            _isLedAvailable = YES;
            demoLedChar = characteristic;
            [baseDevice.peripheral setNotifyValue:YES forCharacteristic:demoLedChar];
        } else if([characteristic.UUID isEqual:demoAdcCharUuid]) {
            _isAdcAvailable = YES;
            demoAdcChar = characteristic;
            [baseDevice.peripheral setNotifyValue:YES forCharacteristic:demoAdcChar];
        } else if([characteristic.UUID isEqual:demoCtrlCharUuid]) {
            demoCtrlChar = characteristic;
            //[baseDevice.peripheral setNotifyValue:YES forCharacteristic:demoCtrlChar];
        } else if([characteristic.UUID isEqual:demoAccelCharUuid]) {
            _isAccelAvailable = YES;
            demoAccelChar = characteristic;
            [baseDevice.peripheral setNotifyValue:YES forCharacteristic:demoAccelChar];
        }
    }
}

- (void)populateDisServiceCharacteristics
{
    for (CBCharacteristic *characteristic in disService.characteristics) {
        if ([characteristic.UUID isEqual:disMfgNameUuid]) {
            disMfgNameChar = characteristic;
        } else if([characteristic.UUID isEqual:disModelNumberUuid]) {
            disModelNumberChar = characteristic;
        } else if([characteristic.UUID isEqual:disSerialNumberUuid]) {
            disSerialNumberChar = characteristic;
        } else if([characteristic.UUID isEqual:disFirmwareRevisionUuid]) {
            disFirmwareRevisionChar = characteristic;
        }
    }
}

- (RigLeBaseDevice*)getBaseDevice
{
    return baseDevice;
}

- (void)setLedColor:(RgbColor_t)color
{
    if (!_isLedAvailable) {
        return;
    }
    
    uint8_t data[3] = { color.red, color.green, color.blue };
    
    [baseDevice.peripheral writeValue:[NSData dataWithBytes:data length:sizeof data] forCharacteristic:demoLedChar type:CBCharacteristicWriteWithoutResponse];
}

- (RgbColor_t)getLedColor
{
    uint8_t *ledData = (uint8_t*)demoLedChar.value.bytes;
    RgbColor_t color;
    
    color.red = ledData[0];
    color.green = ledData[1];
    color.blue = ledData[2];
    
    return color;
}

- (void)startAmbientLightSensing
{
    uint8_t cmd = EVAL_CMD_ADC_STREAM_START;
    [baseDevice.peripheral writeValue:[NSData dataWithBytes:&cmd length:sizeof cmd] forCharacteristic:demoCtrlChar type:CBCharacteristicWriteWithoutResponse];
}

- (void)stopAmbientLightSensing
{
    uint8_t cmd = EVAL_CMD_ADC_STREAM_STOP;
    [baseDevice.peripheral writeValue:[NSData dataWithBytes:&cmd length:sizeof cmd] forCharacteristic:demoCtrlChar type:CBCharacteristicWriteWithoutResponse];
}

- (void)startAccelerometer
{
    uint8_t cmd = EVAL_CMD_ACCEL_STREAM_START;
    [baseDevice.peripheral writeValue:[NSData dataWithBytes:&cmd length:sizeof cmd] forCharacteristic:demoCtrlChar type:CBCharacteristicWriteWithoutResponse];
}

- (void)stopAccelerometer
{
    uint8_t cmd = EVAL_CMD_ACCEL_STREAM_STOP;
    [baseDevice.peripheral writeValue:[NSData dataWithBytes:&cmd length:sizeof cmd] forCharacteristic:demoCtrlChar type:CBCharacteristicWriteWithoutResponse];
}

#pragma mark -
#pragma mark RigLeBaseDeviceDelegate methods
- (void)discoveryDidCompleteForDevice:(RigLeBaseDevice *)device
{
    
}

- (void)didUpdateNotifyStateForCharacteristic:(CBCharacteristic *)characteristic forDevice:(RigLeBaseDevice *)device
{
    
}

- (void)didWriteValueForCharacteristic:(CBCharacteristic *)characteristic forDevice:(RigLeBaseDevice *)device
{
    
}

- (void)didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic forDevice:(RigLeBaseDevice *)device
{
    if (characteristic == demoLedChar) {
        if (_delegate != nil) {
            RgbColor_t color;
            uint8_t * ledData = (uint8_t*)characteristic.value.bytes;
            color.red = ledData[0];
            color.green = ledData[1];
            color.blue = ledData[2];
            [_delegate didUpdateLedColor:color];
        }
    } else if(characteristic == demoButtonChar) {
        if (_delegate != nil) {
            uint8_t button_state = ((uint8_t*)characteristic.value.bytes)[0];
            [_delegate didUpdateButtonData:button_state];
        }
    } else if(characteristic == demoAdcChar) {
        if (_delegate != nil) {
            uint8_t ambient = ((uint8_t*)characteristic.value.bytes)[0];
            [_delegate didUpdateAdcData:ambient];
        }
    } else if(characteristic == demoAccelChar) {
        if (_delegate != nil) {
            AccelData_t accelData;
            uint8_t * data = (uint8_t*)characteristic.value.bytes;
            accelData.x = data[0];
            accelData.y = data[1];
            accelData.z = data[2];
            [_delegate didUpdateAccelData:accelData];
        }
    }
}
@end
