//
//  FirstViewController.m
//  BMD200EvalDemo
//
//  Created by Eric P. Stutzenberger on 7/6/15.
//  Copyright (c) 2015 Rigado,LLC. All rights reserved.
//
//  Source code licensed under BMD-200 Software License Agreement.
//  You should have received a copy with purchase of BMD-200 product.
//  If not, contact info@rigado.com for for a copy.

#import "StatusViewController.h"
#import "BMD200EvalDemoTabBarController.h"
#import "BMD200EvalDemoDevice.h"
#import "Rigablue.h"
#import "AccelPlotManager.h"
#import "CorePlot-CocoaTouch.h"
#import "SVProgressHUD.h"

#define USER_BUTTON_2_MASK      (1 << 0)
#define USER_BUTTON_1_MASK      (1 << 4)

@interface StatusViewController () <BMD200EvalDemoTabBarDelegate, BMD200EvalDemoDeviceDelegate>
{
    BMD200EvalDemoDevice *baseDevice;
    
    AccelPlotManager *plotManager;
    
    NSMutableArray *accelDataList;
    uint32_t total_samples;
    uint32_t delayMs;
    
    uint32_t ambientLightLevel;
}
@end

@implementation StatusViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    accelDataList = [[NSMutableArray alloc] init];
    self.view.backgroundColor = [UIColor colorWithPatternImage: [UIImage imageNamed:@"row-background-blue-grid.png"]];
    _userButtonOne.layer.cornerRadius = 5;
    _userButtonTwo.layer.cornerRadius = 5;
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self deconfigureDevice];
    BMD200EvalDemoTabBarController *tbc = (BMD200EvalDemoTabBarController*)self.tabBarController;
    [tbc unregiserListener:self];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    plotManager = [[AccelPlotManager alloc] initWithFrame:_accelGraphView.bounds];
    [_accelGraphView addSubview:plotManager.hostView];
    BMD200EvalDemoTabBarController *tbc = (BMD200EvalDemoTabBarController*)self.tabBarController;
    [tbc registerListener:self];
    if ([tbc isConnected]) {
        [self configureDevice];
    } else {
        if (![tbc isSearching]) {
            [tbc searchForDevice];
            [SVProgressHUD showWithStatus:NSLocalizedString(@"Searching for BMD Device", nil) maskType:SVProgressHUDMaskTypeGradient];
        }
    }
}

-(UIStatusBarStyle)preferredStatusBarStyle{
    return UIStatusBarStyleLightContent;
}

- (void)configureDevice
{
    BMD200EvalDemoTabBarController *tbc = (BMD200EvalDemoTabBarController*)self.tabBarController;
    baseDevice = [tbc getDevice];
    baseDevice.delegate = self;
    [baseDevice determineDeviceHardwareVersion];
    [baseDevice startAmbientLightSensing];
    [NSThread sleepForTimeInterval:0.3f];
    
    [baseDevice startAccelerometer];
    [self updateView];
}

- (void)deconfigureDevice
{
    if (baseDevice == nil) {
        return;
    }
    
    [baseDevice stopAmbientLightSensing];
    [NSThread sleepForTimeInterval:0.3f];
    [baseDevice stopAccelerometer];
    if (baseDevice.delegate == self) {
        baseDevice.delegate = nil;
    }
    baseDevice = nil;
    [plotManager resetSampleCount];
}

- (void)updateView
{
    _ambientLightLevelImageView.backgroundColor = [UIColor colorWithRed:0.0f green:0.0f blue:0.0f alpha:1.0f];
    _ambientLightLevelImageView.layer.cornerRadius = 2;
}

#pragma mark -
#pragma mark BMD200EvalDemoTabBarDelegate methods
- (void)didConnectToDevice:(BMD200EvalDemoDevice *)device
{
    //Update view using device
    void (^update)(void) = ^void(void) {
        [self configureDevice];
        [SVProgressHUD showSuccessWithStatus:@"Connected!" maskType:SVProgressHUDMaskTypeGradient];
    };
    if (![NSThread isMainThread]) dispatch_sync(dispatch_get_main_queue(), update);
    else update();
}

- (void)didDisconnectFromDevice
{
    BMD200EvalDemoTabBarController *tbc = (BMD200EvalDemoTabBarController*)self.tabBarController;
    [tbc searchForDevice];
    void (^update)(void) = ^void(void) {
            [SVProgressHUD showWithStatus:@"Searching..." maskType:SVProgressHUDMaskTypeGradient];
    };
    if (![NSThread isMainThread]) dispatch_sync(dispatch_get_main_queue(), update);
    else update();
}

#pragma mark -
#pragma mark BMD200EvalDemoDeviceDelegate methods
- (void)didUpdateLedColor:(RgbColor_t)color
{
    //Not Used
}

- (void)didUpdateButtonData:(uint8_t)data
{
    /**
     *  Button data is reported as a single byte.  Each button is assigned a bit within this byte.  The
     *  User2 button uses bit 0 and the User1 button uses bit 4.  If either bit is 1, that button
     *  is currently being pressed.  If the bit is 0, then the button is not pressed.
     */
    void (^update)(void) = ^void(void) {
        if ((data & USER_BUTTON_2_MASK) == USER_BUTTON_2_MASK) {
            _userButtonTwo.alpha = 0.5;
            _userButtonTwo.backgroundColor = [UIColor colorWithWhite:100 alpha:.5];
//            [_userButtonTwo setNeedsDisplay];
        } else {
            _userButtonTwo.alpha = 1;
            _userButtonTwo.backgroundColor = [UIColor clearColor];
//            [_userButtonTwo setNeedsDisplay];
        }
        
        if ((data & USER_BUTTON_1_MASK) == USER_BUTTON_1_MASK) {
            _userButtonOne.alpha = 0.5;
            _userButtonOne.backgroundColor = [UIColor colorWithWhite:100 alpha:.5];
        } else {
            _userButtonOne.alpha = 1;
            _userButtonOne.backgroundColor = [UIColor clearColor];
        }
    };
    if (![NSThread isMainThread]) dispatch_sync(dispatch_get_main_queue(), update);
    else update();
}

- (void)didUpdateAdcData:(uint8_t)adc
{
    void (^update)(void) = ^void(void) {
        /**
         *  The ADC peripheral is configured to use the Internal 1.2V reference with 1/3 voltage scaling.
         *  Here, we apply a scaling factor and correct for the 1/3 voltage scaling.  For the alpha calculation,
         *  the value is constrained to 400 mV.  Depending on the ambient light sensor, this value may need
         *  to be slightly adjusted.
         */
        ambientLightLevel = (((adc * 1200)/255) * 3)/2;
        _ambientLightLevelLabel.text = [NSString stringWithFormat:@"%d mV", ambientLightLevel];
        _ambientLightLevelImageView.backgroundColor = [UIColor colorWithRed:1.0f green:1.0f blue:1.0f alpha:(float)ambientLightLevel/400.0f];
    };
    if (![NSThread isMainThread]) dispatch_sync(dispatch_get_main_queue(), update);
    else update();
}

- (void)didUpdateAccelData:(AccelData_t)accelData
{
    [plotManager addSample:accelData];
}

- (void)unableToDiscoverHardwareVersion {
    dispatch_sync(dispatch_get_main_queue(), ^{
        UIAlertController *ac = [UIAlertController alertControllerWithTitle:[NSString stringWithFormat:NSLocalizedString(@"Reset Bluetooth", nil)] message:[NSString stringWithFormat:NSLocalizedString(@"Reset Bluetooth Message", nil)] preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *OK = [UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleDefault handler:nil];
        [ac addAction:OK];
        [self presentViewController:ac animated:NO completion:nil];
    });
}

@end
