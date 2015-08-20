//
//  SecondViewController.m
//  BMD200EvalDemo
//
//  Created by Eric P. Stutzenberger on 7/6/15.
//  Copyright (c) 2015 Rigado,LLC. All rights reserved.
//
//  Source code licensed under BMD-200 Software License Agreement.
//  You should have received a copy with purchase of BMD-200 product.
//  If not, contact info@rigado.com for for a copy.

#import "ColorWheelViewController.h"
#import "BMD200EvalDemoDevice.h"
#import "BMD200EvalDemoTabBarController.h"
#import "SVProgressHUD.h"

@interface ColorWheelViewController () <ISColorWheelDelegate, BMD200EvalDemoDeviceDelegate, BMD200EvalDemoTabBarDelegate>
{
    BMD200EvalDemoDevice *baseDevice;
}
@end

@implementation ColorWheelViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    CGRect frame;
    
    frame.origin = _wheelView.bounds.origin;
    frame.size = _wheelView.bounds.size;
    
    [_wheelView initWheelWithFrame:frame];
    
    BMD200EvalDemoTabBarController *tbc = (BMD200EvalDemoTabBarController*)self.tabBarController;
    [tbc registerListener:self];
    
    _wheelView.delegate = self;
    _wheelView.continuous = true;
    
    self.view.backgroundColor = [UIColor colorWithPatternImage: [UIImage imageNamed:@"row-background-blue-grid.png"]];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    BMD200EvalDemoTabBarController *tbc = (BMD200EvalDemoTabBarController*)self.tabBarController;
    if ([tbc isConnected]) {
        [self configureDevice];
    } else {
        if (![tbc isSearching]) {
            [tbc searchForDevice];
        }
    }
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    BMD200EvalDemoTabBarController *tbc = (BMD200EvalDemoTabBarController*)self.tabBarController;
    if ([tbc isSearching]) {
        [SVProgressHUD showWithStatus:@"Searching..." maskType:SVProgressHUDMaskTypeGradient];
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    BMD200EvalDemoTabBarController *tbc = (BMD200EvalDemoTabBarController*)self.tabBarController;
    [tbc unregiserListener:self];
}

-(UIStatusBarStyle)preferredStatusBarStyle{
    return UIStatusBarStyleLightContent;
}

- (void)setDeviceColor:(RgbColor_t)color
{
    if (baseDevice == nil) {
        return;
    }
    
    NSLog(@"Setting color: %d %d %d", color.red, color.green, color.blue);
    [baseDevice setLedColor:color];
    [_ledOnOffSwitch setOn:YES animated:YES];
}

- (void)configureDevice
{
    BMD200EvalDemoTabBarController *tbc = (BMD200EvalDemoTabBarController*)self.tabBarController;
    baseDevice = [tbc getDevice];
    baseDevice.delegate = self;
    PixelRGB c = [_wheelView currentRGBColor];
    RgbColor_t color = { c.r, c.g, c.b };
    [self setDeviceColor:color];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

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

- (void)didUpdateLedColor:(RgbColor_t)color {}

- (void)didUpdateAccelData:(AccelData_t)accelData {}

- (void)didUpdateAdcData:(uint8_t)adc {}

- (void)didUpdateButtonData:(uint8_t)data {}

- (void)colorWheelDidChangeColor:(ISColorWheel *)colorWheel
{
    PixelRGB temp = [colorWheel currentRGBColor];
    RgbColor_t color = { temp.r, temp.g, temp.b };
    
    [self setDeviceColor:color];
}

- (IBAction)didChangeLedOnOffSwitch:(id)sender
{
    BMD200EvalDemoTabBarController *tbc = (BMD200EvalDemoTabBarController*)self.tabBarController;
    if (![tbc isConnected]) {
        return;
    }
    
    if (_ledOnOffSwitch.isOn) {
        [self colorWheelDidChangeColor:_wheelView];
    } else {
        RgbColor_t off = { 0, 0, 0 };
        [baseDevice setLedColor:off];
    }
}
@end
