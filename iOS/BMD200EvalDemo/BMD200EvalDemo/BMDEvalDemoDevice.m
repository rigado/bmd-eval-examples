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

#import "BMDEvalDemoDevice.h"
#import "CBUUID+UUIDHelperMethods.h"
#import "Rigablue.h"

#define EVAL_CMD_HARDWARE_VERSION       0x0A

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

@interface BMDEvalDemoDevice() <RigLeBaseDeviceDelegate>
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

    CBUUID *blinkyServiceUuid;
    CBUUID *blinkyCtrlCharUuid;
    
    CBService *blinkyService;
    CBCharacteristic *blinkyCtrlChar;

    CBUUID *bmdwareServiceUuid;
    CBUUID *bmdwareCtrlCharUuid;
    
    CBService *bmdwareService;
    CBCharacteristic *bmdwareCtrlChar;
    
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

@implementation BMDEvalDemoDevice

- (id)initWithDevice:(RigLeBaseDevice*)device
{
    self = [super init];
    if (self) {
        baseDevice = device;
        baseDevice.delegate = self;
        [self initUuidObjs];
        [self initServiceObjects];
        [self determineDeviceHardwareVersion];
    }
    
    return self;
}

- (void)initServiceObjects
{
    for (CBService *service in [baseDevice getServiceList]) {
        if ([service.UUID isEqual:demoServiceUuid]) {
            demoService = service;
            [self populateDemoServiceCharacteristics];
        } else if ([service.UUID isEqual:disServiceUuid]) {
            disService = service;
            [self populateDisServiceCharacteristics];
        } else if ([service.UUID isEqual:blinkyServiceUuid]) {
            blinkyService = service;
            [self populateDemoServiceCharacteristics];
        } else if ([service.UUID isEqual:bmdwareServiceUuid]) {
            bmdwareService = service;
            [self populateDemoServiceCharacteristics];
        }
    }
}

- (void)initUuidObjs
{
    blinkyServiceUuid = [CBUUID UUIDWithString:BLINKY_SERVICE];
    blinkyCtrlCharUuid = [CBUUID UUIDWithString:BLINKY_CHAR];
    
    bmdwareServiceUuid = [CBUUID UUIDWithString:BMDWARE_RESET_SERVICE];
    bmdwareCtrlCharUuid = [CBUUID UUIDWithString:BMDWARE_RESET_CHAR];
    
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
            [baseDevice.peripheral setNotifyValue:YES forCharacteristic:demoCtrlChar];
        } else if([characteristic.UUID isEqual:demoAccelCharUuid]) {
            _isAccelAvailable = YES;
            demoAccelChar = characteristic;
            [baseDevice.peripheral setNotifyValue:YES forCharacteristic:demoAccelChar];
        }
    }
    for (CBCharacteristic *characteristic in blinkyService.characteristics) {
        if([characteristic.UUID isEqual:blinkyCtrlCharUuid]) {
            blinkyCtrlChar = characteristic;
            [baseDevice.peripheral setNotifyValue:YES forCharacteristic:blinkyCtrlChar];
        }
    }
    for (CBCharacteristic *characteristic in bmdwareService.characteristics) {
        if([characteristic.UUID isEqual:bmdwareCtrlCharUuid]) {
            bmdwareCtrlChar = characteristic;
            [baseDevice.peripheral setNotifyValue:YES forCharacteristic:bmdwareCtrlChar];
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

- (void)determineDeviceHardwareVersion {
    uint8_t cmd = EVAL_CMD_HARDWARE_VERSION;
    CBCharacteristic *characteristic;
    if (demoCtrlChar) {
        characteristic = demoCtrlChar;
    } else if (blinkyCtrlChar) {
        characteristic = blinkyCtrlChar;
    }
    if (bmdwareCtrlChar) {
        cmd = 0x60;
        characteristic = bmdwareCtrlChar;
    }
    NSData *data = [NSData dataWithBytes:&cmd length: 1];
    if (characteristic) {
        [baseDevice.peripheral setNotifyValue:YES forCharacteristic:characteristic];
        [baseDevice.peripheral writeValue:data forCharacteristic:characteristic type:CBCharacteristicWriteWithResponse];
    }

}

- (void)startAmbientLightSensing
{
    if (demoCtrlChar) {
        uint8_t cmd = EVAL_CMD_ADC_STREAM_START;
        [baseDevice.peripheral writeValue:[NSData dataWithBytes:&cmd length:sizeof cmd] forCharacteristic:demoCtrlChar type:CBCharacteristicWriteWithoutResponse];
    }
}

- (void)stopAmbientLightSensing
{
    if (demoCtrlChar) {
    uint8_t cmd = EVAL_CMD_ADC_STREAM_STOP;
    [baseDevice.peripheral writeValue:[NSData dataWithBytes:&cmd length:sizeof cmd] forCharacteristic:demoCtrlChar type:CBCharacteristicWriteWithoutResponse];
    }
}

- (void)startAccelerometer
{
    if (demoCtrlChar) {
        uint8_t cmd = EVAL_CMD_ACCEL_STREAM_START;
    [baseDevice.peripheral writeValue:[NSData dataWithBytes:&cmd length:sizeof cmd] forCharacteristic:demoCtrlChar type:CBCharacteristicWriteWithoutResponse];
    }
}

- (void)stopAccelerometer
{
    if (demoCtrlChar) {
    uint8_t cmd = EVAL_CMD_ACCEL_STREAM_STOP;
    [baseDevice.peripheral writeValue:[NSData dataWithBytes:&cmd length:sizeof cmd] forCharacteristic:demoCtrlChar type:CBCharacteristicWriteWithoutResponse];
    }
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
    if ((!_is300 && blinkyCtrlChar) || (!_is300 && bmdwareCtrlChar)) {
        [device.peripheral readValueForCharacteristic:characteristic];
    }
}

- (void)didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic forDevice:(RigLeBaseDevice *)device
{
    if (characteristic == demoLedChar) {
        if ([_delegate respondsToSelector:@selector(didUpdateLedColor:)]) {
            RgbColor_t color;
            uint8_t * ledData = (uint8_t*)characteristic.value.bytes;
            color.red = ledData[0];
            color.green = ledData[1];
            color.blue = ledData[2];
            [_delegate didUpdateLedColor:color];
        }
    } else if(characteristic == demoButtonChar) {
        if ([_delegate respondsToSelector:@selector(didUpdateButtonData:)]) {
            uint8_t button_state = ((uint8_t*)characteristic.value.bytes)[0];
            [_delegate didUpdateButtonData:button_state];
        }
    } else if(characteristic == demoAdcChar) {
        if ([_delegate respondsToSelector:@selector(didUpdateAdcData:)]) {
            uint8_t ambient = ((uint8_t*)characteristic.value.bytes)[0];
            [_delegate didUpdateAdcData:ambient];
        }
    } else if(characteristic == demoAccelChar) {
        if ([_delegate respondsToSelector:@selector(didUpdateAccelData:)]) {
            AccelData_t accelData;
            uint8_t * data = (uint8_t*)characteristic.value.bytes;
            accelData.x = data[0];
            accelData.y = data[1];
            accelData.z = data[2];
            [_delegate didUpdateAccelData:accelData];
        }
    } else if (characteristic == demoCtrlChar || characteristic == blinkyCtrlChar) {
        uint8_t * data = (uint8_t*)characteristic.value.bytes;
        // check the 9th value of the data,
        // this is the hardware version number, if it equals 2, then it's a 300
        // is this check enough? would there be a time where the 9th number is 2 just by chance?
        // 1 in 16
        if (data[9] == 02) {
            _is300 = YES;
            _is200 = NO;
        } else {
            _is300 = NO;
            _is200 = YES;
        }
        if ([_delegate respondsToSelector:@selector(didDiscoverHardwareVersion)]) {
            [_delegate didDiscoverHardwareVersion];
        }
        NSLog(@"%@ %@ is 300? %d is 200? %d", characteristic.description, characteristic.value, self.is300, self.is200);
        [baseDevice.peripheral setNotifyValue:NO forCharacteristic:characteristic];
    } else if (characteristic == bmdwareCtrlChar) {
        uint8_t * data = (uint8_t*)characteristic.value.bytes;
        // check the 9th value of the data,
        // this is the hardware version number, if it equals 2, then it's a 300
        // is this check enough? would there be a time where the 9th number is 2 just by chance?
        // 1 in 16
        if (data[10] == 02) {
            _is300 = YES;
            _is200 = NO;
            _isIndeterminatableState = NO;
        } else if (data[10] == 01) {
            _is300 = NO;
            _is200 = YES;
            _isIndeterminatableState = NO;
        } else {
            NSLog(@"Issue determining device. Must Reset Bluetooth");
            _is300 = NO;
            _is200 = NO;
            _isIndeterminatableState = YES;
            //unableToDiscoverHardwareVersion
            if ([_delegate respondsToSelector:@selector(unableToDiscoverHardwareVersion)]) {
                [_delegate unableToDiscoverHardwareVersion];
            }
        }
        if ([_delegate respondsToSelector:@selector(didDiscoverHardwareVersion)] && !self.isIndeterminatableState) {
            [_delegate didDiscoverHardwareVersion];
        }
        NSLog(@"%@ %@ is 300? %d is 200? %d", characteristic.description, characteristic.value, self.is300, self.is200);
        [baseDevice.peripheral setNotifyValue:NO forCharacteristic:characteristic];
    }
}

@end
