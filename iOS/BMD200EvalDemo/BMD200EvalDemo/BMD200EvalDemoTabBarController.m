//
//  TabBarController.m
//  BMD200EvalDemo
//
//  Created by Eric P. Stutzenberger on 7/6/15.
//  Copyright (c) 2015 Rigado,LLC. All rights reserved.
//
//  Source code licensed under BMD-200 Software License Agreement.
//  You should have received a copy with purchase of BMD-200 product.
//  If not, contact info@rigado.com for for a copy.

#import "BMD200EvalDemoTabBarController.h"
#import "Rigablue.h"
#import "CBUUID+UUIDHelperMethods.h"
#import "SVProgressHUD.h"

@interface BMD200EvalDemoTabBarController () <RigLeDiscoveryManagerDelegate, RigLeConnectionManagerDelegate, RigLeBaseDeviceDelegate>
{
    BOOL isConnected;
    RigLeBaseDevice *baseDevice;
    BMD200EvalDemoDevice *demoDevice;
    NSMutableArray *delegateList;
    BOOL isBlinkyDemo;
    BOOL isBmdWare;
    BOOL is200;
    BOOL is300;
}
@end

@implementation BMD200EvalDemoTabBarController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    //[self startDiscovery];
    [RigLeConnectionManager sharedInstance].delegate = self;
    delegateList = [[NSMutableArray alloc] init];
    baseDevice = nil;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)startDiscovery
{
    CBUUID *demoServiceUuid = [CBUUID uuidForBaseUuidString:BMDEVAL_BASE_UUID withShortUuidString:BMDEVAL_UUID_SERVICE];
    CBUUID *blinkyServiceUuid = [CBUUID UUIDWithString:@"180F"];
    CBUUID *bmdWareServiceUuid = [CBUUID UUIDWithString:@"2413b33f-707f-90bd-0245-2ab8807571b7"];
    CBUUID *bmdWareServiceUuid2 = [CBUUID UUIDWithString:@"6e400001-b5a3-f393-e0a9-e50e24dcca9e"];
    RigDeviceRequest *dr = [RigDeviceRequest deviceRequestWithUuidList:[NSArray arrayWithObjects:demoServiceUuid, blinkyServiceUuid, bmdWareServiceUuid, bmdWareServiceUuid2, nil] timeout:0.0f delegate:self allowDuplicates:YES];
    
    [RigLeConnectionManager sharedInstance].delegate = self;
    [[RigLeDiscoveryManager sharedInstance] discoverDevices:dr];
}

- (BOOL)isConnected
{
    return baseDevice.peripheral.state == CBPeripheralStateConnected;
}

- (BOOL)isConnectedToBlinkyDemo
{
    return isBlinkyDemo;
}

- (BOOL)isConnectedToBmdWare
{
    return isBmdWare;
}

- (BOOL)isConnectedTo200
{
    return demoDevice.is200;
}

- (BOOL)isConnectedTo300
{
    return demoDevice.is300;
}

- (BMD200EvalDemoDevice*)getDevice
{
    return demoDevice;
}

- (RigLeBaseDevice *)getBlinkyDemoDevice
{
    if (!isBlinkyDemo && !isBmdWare) {
        return nil;
    }
    
    return baseDevice;
}

- (void)searchForDevice
{
    [self startDiscovery];
}

- (void)stopSearchingForDevice
{
    [[RigLeDiscoveryManager sharedInstance] stopDiscoveringDevices];
    [SVProgressHUD dismiss];
}

- (BOOL)isSearching
{
    return [[RigLeDiscoveryManager sharedInstance] isDiscoveryRunning];
}

- (BOOL)registerListener:(id<BMD200EvalDemoTabBarDelegate>)delegate
{
    if ([delegateList indexOfObject:delegate] == NSNotFound) {
        [delegateList addObject:delegate];
        return YES;
    }
    
    return NO;
}

- (BOOL)unregiserListener:(id<BMD200EvalDemoTabBarDelegate>)delegate
{
    if ([delegateList indexOfObject:delegate] == NSNotFound) {
        return NO;
    }
    
    [delegateList removeObject:delegate];
    return YES;
}

-(UIStatusBarStyle)preferredStatusBarStyle{
    return UIStatusBarStyleLightContent;
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

#pragma mark -
#pragma mark RigLeDiscoveryManagerDelegate methods
- (void)didDiscoverDevice:(RigAvailableDeviceData *)device
{
    NSLog(@"Discovered device: %@", device.peripheral.name);
    if (device.rssi.intValue < -60) {
        NSLog(@"RSSI Not low enough: %@", device.peripheral.name);
        return;
    }
    
    
    NSDictionary *advData = [device advertisementData];
//    NSArray *mfgData = [advData objectForKey:CBAdvertisementDataManufacturerDataKey];
    NSString *advName = [advData objectForKey:CBAdvertisementDataLocalNameKey];
    
    if ([advName isEqualToString:@"BMD Blinky"] || [advName isEqualToString:@"BMD200-Blinky"]) {
        isBlinkyDemo = YES;
        
    } else if([advName isEqualToString:@"RigCom"]) {
        isBmdWare = YES;
    }
    
    if (isBmdWare || isBlinkyDemo || [advName isEqualToString:@"EvalDemo"]) {
        [[RigLeConnectionManager sharedInstance] connectDevice:device connectionTimeout:10.0f];
        [[RigLeDiscoveryManager sharedInstance] stopDiscoveringDevices];
    }
}

- (void)discoveryDidTimeout
{
    
}

- (void)bluetoothNotPowered {}

- (void)didUpdateDeviceData:(RigAvailableDeviceData *)device deviceIndex:(NSUInteger)index {}

#pragma mark -
#pragma mark RigLeConnectionManagerDelegate methods
- (void)didConnectDevice:(RigLeBaseDevice *)device
{
    NSLog(@"Connected");
    baseDevice = device;
    baseDevice.delegate = self;
    isConnected = YES;
    [device runDiscovery];
}

- (void)didDisconnectPeripheral:(CBPeripheral *)peripheral
{
    NSLog(@"Disconnected");
    isConnected = NO;
    demoDevice = nil;
    baseDevice = nil;
    isBlinkyDemo = NO;
    isBmdWare = NO;
    is200 = NO;
    is300 = NO;
    for (id<BMD200EvalDemoTabBarDelegate> delegate in delegateList) {
        [delegate didDisconnectFromDevice];
    }
}

- (void)deviceConnectionDidFail:(RigAvailableDeviceData *)device
{
    [self startDiscovery];
}

- (void)deviceConnectionDidTimeout:(RigAvailableDeviceData *)device
{
    [self startDiscovery];
}

#pragma mark -
#pragma mark RigLeBaseDeviceDelegate methods
- (void)discoveryDidCompleteForDevice:(RigLeBaseDevice *)device
{
    NSLog(@"Discovery complete");
    demoDevice = [[BMD200EvalDemoDevice alloc] initWithDevice:device];
    for (id<BMD200EvalDemoTabBarDelegate> delegate in delegateList) {
        [delegate didConnectToDevice:demoDevice];
    }
    dispatch_sync(dispatch_get_main_queue(), ^{
        if (isBlinkyDemo || isBmdWare) {
            //switch to update tab and show alert view
            [self setSelectedIndex:2];
        }
        [SVProgressHUD dismiss];
    });
}

- (void)didUpdateNotifyStateForCharacteristic:(CBCharacteristic *)characteristic forDevice:(RigLeBaseDevice *)device {}

- (void)didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic forDevice:(RigLeBaseDevice *)device {}

- (void)didWriteValueForCharacteristic:(CBCharacteristic *)characteristic forDevice:(RigLeBaseDevice *)device {}
@end
