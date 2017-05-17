//
//  UpdateViewController.m
//  BMD200EvalDemo
//
//  Created by Eric P. Stutzenberger on 7/13/15.
//  Copyright (c) 2015 Rigado,LLC. All rights reserved.
//
//  Source code licensed under BMD-200 Software License Agreement.
//  You should have received a copy with purchase of BMD-200 product.
//  If not, contact info@rigado.com for for a copy.

#import "UpdateViewController.h"
#import "Rigablue.h"
#import "BMDEvalDemoTabBarController.h"
#import "BMDEvalDemoDevice.h"
#import "SVProgressHUD.h"
#import "RigFirmwareUpdateManager.h"

#define BOOTLOADER_COMMAND_LEN          0x04

/* TODO: Update the defineS below for your product's service and characteristic UUID */
#define RESET_SERVICE           @"50db1523-418d-4690-9589-ab7be9e22684"
/* TODO: Ensure the UUID for this characteristic is the correct UUID for the charracteristic that will accept
 * a command to reset the device in to the bootloader.  If it doesn't not, this application will not work properly. */
#define RESET_CHAR              @"50db1527-418d-4690-9589-ab7be9e22684"

#define BLINKY_RESET_SERVICE    @"6d580001-fc91-486b-82c4-86a1d2eb8f88"
#define BLINKY_RESET_CHAR       @"6d580002-fc91-486b-82c4-86a1d2eb8f88"

#define BMDWARE_RESET_SERVICE   @"2413B33F-707F-90BD-2045-2AB8807571B7"
#define BMDWARE_RESET_CHAR      @"2413B43F-707F-90BD-2045-2AB8807571B7"

static uint8_t bootloader_command[] = { 0xa1, 0xfc, 0xd6, 0xe7 };
static uint8_t blinky_boot_command[] = { 0x98, 0xb6, 0x2f, 0x51 };
static uint8_t bmdware_boot_command[] = { 0x03, 0x56, 0x30, 0x57 };

@interface UpdateViewController () <UIPickerViewDataSource, UIPickerViewDelegate, RigFirmwareUpdateManagerDelegate, BMDEvalDemoTabBarDelegate, BMDEvalDemoDeviceDelegate>
{
    RigFirmwareUpdateManager *updateManager;
    RigLeBaseDevice *updateDevice;
    BMDEvalDemoDevice *demoDevice;
    BMDEvalDemoTabBarController *tbc;
    
    NSArray *firmwareList;
    NSArray *firmwareBinaryList;
    
    BOOL isUpdateInProgress;
    BOOL isAlreadyBootloader;
    BOOL didUpdateThroughBootloader;
    BOOL didCompleteAnUpdate;
    
    BOOL isBlinkyDemo;
    BOOL isBmdWare;
    
    __weak IBOutlet UIButton *firmwareButton;
    
    id<RigLeConnectionManagerDelegate> connectionManagerDelegate;
}
@end

@implementation UpdateViewController

- (void)viewDidLoad {
    tbc = (BMDEvalDemoTabBarController*)self.tabBarController;

    [super viewDidLoad];

    self.view.backgroundColor = [UIColor colorWithPatternImage: [UIImage imageNamed:@"row-background-blue-grid.png"]];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(appWillResignActive:) name:UIApplicationWillResignActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(appWillTerminate:) name:UIApplicationWillTerminateNotification object:nil];
    connectionManagerDelegate = [RigLeConnectionManager sharedInstance].delegate;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    _updateStatusLabel.text = @"Idle";
    _updateProgressView.progress = 0.0f;
    
    [tbc registerListener:self];
    if ([tbc isConnected]) {
        [self configureDevice];
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    
//    BMDEvalDemoTabBarController *tbc = (BMDEvalDemoTabBarController*)self.tabBarController;
    [tbc unregiserListener:self];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
//    BMDEvalDemoTabBarController *tbc = (BMDEvalDemoTabBarController*)self.tabBarController;
    if (![tbc isSearching] && ![tbc isConnected]) {
        [tbc searchForDevice];
        [SVProgressHUD showWithStatus:NSLocalizedString(@"Searching for BMD Device", nil) maskType:SVProgressHUDMaskTypeGradient];
    }
}

- (UIStatusBarStyle)preferredStatusBarStyle{
    return UIStatusBarStyleLightContent;
}

- (void)appWillResignActive:(NSNotification*)note
{
    isUpdateInProgress = NO;
    self.tabBarController.tabBar.userInteractionEnabled = YES;
    [RigLeConnectionManager sharedInstance].delegate = connectionManagerDelegate;
}

- (void)appWillTerminate:(NSNotification*)note
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationWillResignActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationWillTerminateNotification object:nil];
}

- (void)configureDevice
{
//    BMDEvalDemoTabBarController *tbc = (BMDEvalDemoTabBarController*)self.tabBarController;
    demoDevice = [tbc getDevice];
    updateDevice = [demoDevice getBaseDevice];
    demoDevice.delegate = self;
    [demoDevice determineDeviceHardwareVersion];
}

- (void)displayAppropriateMessage
{
    if ([tbc isConnectedToBlinkyDemo] && !demoDevice.isIndeterminatableState) {
        isBlinkyDemo = YES;
        UIAlertController *ac = [UIAlertController alertControllerWithTitle:@"Blinky Connected" message:@"The Blinky demo is currently programmed.  Would you like to revert to the main demo firmware?" preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *aaYes = [UIAlertAction actionWithTitle:@"Yes" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
            [_deploymentPicker selectRow:0 inComponent:0 animated:YES];
            [self didTouchBeginUpdate:nil];
        }];
        UIAlertAction *aaNo = [UIAlertAction actionWithTitle:@"No" style:UIAlertActionStyleDefault handler:nil];
        
        updateDevice = [tbc getBlinkyDemoDevice];
        [ac addAction:aaYes];
        [ac addAction:aaNo];
        [self presentViewController:ac animated:NO completion:nil];
    } else if([tbc isConnectedToBmdWare] && !demoDevice.isIndeterminatableState && !demoDevice.isVS132_3_0) {
        isBmdWare = YES;
        UIAlertController *ac = [UIAlertController alertControllerWithTitle:@"BMDWare Installed" message:@"BMDWare is installed to this evaluation board.  If you would like to use its features, download the Rigado Toolbox app from the app store.  Would you like to program the Evaluation demo firmware?" preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *aaYes = [UIAlertAction actionWithTitle:@"Yes" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
            [_deploymentPicker selectRow:0 inComponent:0 animated:YES];
            [self didTouchBeginUpdate:nil];
        }];
        UIAlertAction *aaNo = [UIAlertAction actionWithTitle:@"No" style:UIAlertActionStyleDefault handler:nil];
        
        updateDevice = [tbc getBlinkyDemoDevice];
        [ac addAction:aaYes];
        [ac addAction:aaNo];
        [self presentViewController:ac animated:NO completion:nil];
    } else if (demoDevice.isIndeterminatableState) {
        UIAlertController *ac = [UIAlertController alertControllerWithTitle:[NSString stringWithFormat:NSLocalizedString(@"Reset Bluetooth", nil)] message:[NSString stringWithFormat:NSLocalizedString(@"Reset Bluetooth Message", nil)] preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *OK = [UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleDefault handler:nil];
        [ac addAction:OK];
        [self presentViewController:ac animated:NO completion:nil];
    } else if (demoDevice.isVS132_3_0) {
        UIAlertController *ac = [UIAlertController alertControllerWithTitle:@"Unsupported Bluetooth Version" message:@"The Bluetooth version on the connected device is not compatible with the BMD Eval Demo software" preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *ok = [UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleDefault handler:nil];
        [ac addAction:ok];
        [self presentViewController:ac animated:YES completion:nil];
    }
    [self configureDeploymentPicker];
}

- (void)configureDeploymentPicker {
    firmwareButton.enabled = YES;
    if ([tbc isConnectedTo200]) {
        firmwareList = [NSArray arrayWithObjects:@"BMD200 Eval Demo", @"BMD200 Eval Blinky Demo", @"BMDWare 200", nil];
        firmwareBinaryList = [NSArray arrayWithObjects:@"eval_demo_1_0_0_ota", @"bmd200_blinky_demo_ota", @"bmdware_rel_nrf51_s110_3_1_1_ota", nil];
    } else if ([tbc isConnectedTo300]) {
        firmwareList = [NSArray arrayWithObjects:@"BMD300 Eval Demo", @"BMD300 Eval Blinky Demo", @"BMDWare 300", nil];
        firmwareBinaryList = [NSArray arrayWithObjects:@"bmd-300-demo-shield-rel_1_0_4_ota", @"bmd_blinky_demo_nrf52_s132_1_0_1_ota",  @"bmdware_rel_nrf52_s132_3_1_1_ota", nil];
    } else if ([tbc isConnectedToVS132_3_0]) {
        firmwareList = nil;
        firmwareBinaryList = nil;
        firmwareButton.enabled = NO;        
    } else {
        firmwareList = nil;
        firmwareBinaryList = nil;
        firmwareButton.enabled = NO;
    }
    [self.deploymentPicker reloadAllComponents];
}

- (IBAction)didTouchBeginUpdate:(id)sender
{
    if (updateDevice == nil || isUpdateInProgress) {
        //Disable button when device is not available!!
        return;
    }
    
    [[RigLeDiscoveryManager sharedInstance] stopDiscoveringDevices];
    
    isUpdateInProgress = YES;
    self.tabBarController.tabBar.userInteractionEnabled = NO;
    
    NSData *firmwareImageData;
    NSString *filePath;
    
    NSUInteger row = [_deploymentPicker selectedRowInComponent:0];
    NSString *firmwareFile = [firmwareBinaryList objectAtIndex:row];

    /* Load firmware image in to local memory */
    filePath = [[NSBundle mainBundle] pathForResource:firmwareFile ofType:@"bin"];
    firmwareImageData = [NSData dataWithContentsOfFile:filePath];
    NSLog(@"Firmware file: %@", filePath);
    
    updateManager = [[RigFirmwareUpdateManager alloc] init];
    updateManager.delegate = self;
    
    if (isAlreadyBootloader) {
        /* This path is for when only the bootloader is present on the device. */
        /* Invoke bootloader here with pointer to binary image of firmware. */
        [updateManager updateFirmware:updateDevice image:firmwareImageData activateChar:nil activateCommand:nil activateCommandLen:0];
        isUpdateInProgress = YES;
        return;
    }
    
    CBService *service = nil;
    CBCharacteristic *controlPoint = nil;
    
    /* TODO: Update to use your service and characteristic UUIDs */
    CBUUID *serviceUuid;
    CBUUID *controlPointUuid = [CBUUID UUIDWithString:RESET_CHAR];
    uint8_t *boot_command;
    if (isBlinkyDemo) {
        serviceUuid = [CBUUID UUIDWithString:BLINKY_RESET_SERVICE];
        controlPointUuid = [CBUUID UUIDWithString:BLINKY_RESET_CHAR];
        boot_command = blinky_boot_command;
    } else if (isBmdWare) {
        serviceUuid = [CBUUID UUIDWithString:BMDWARE_RESET_SERVICE];
        controlPointUuid = [CBUUID UUIDWithString:BMDWARE_RESET_CHAR];
        boot_command = bmdware_boot_command;
    } else {
        serviceUuid = [CBUUID UUIDWithString:RESET_SERVICE];
        controlPointUuid = [CBUUID UUIDWithString:RESET_CHAR];
        boot_command = bootloader_command;
    }
    
    for (CBService *svc in [updateDevice getServiceList]) {
        if ([svc.UUID isEqual:serviceUuid]) {
            service = svc;
            break;
        }
    }
    
    if (service != nil) {
        for (CBCharacteristic *characteristic in service.characteristics) {
            if ([characteristic.UUID isEqual:controlPointUuid]) {
                controlPoint = characteristic;
                break;
            }
        }
    }
    
    if (controlPoint != nil) {
        /* Invoke bootloader here with pointer to binary image of firmware. */
        [updateManager updateFirmware:updateDevice image:firmwareImageData activateChar:controlPoint activateCommand:boot_command activateCommandLen:BOOTLOADER_COMMAND_LEN];
        isUpdateInProgress = YES;
    } else {
        _updateStatusLabel.text = @"Characteristic for Reset not found!";
        isUpdateInProgress = NO;
        self.tabBarController.tabBar.userInteractionEnabled = YES;
    }
}

#pragma mark -
#pragma mark - BMD200EvalDemoTabBarDelegate methods
- (void)didConnectToDevice:(BMDEvalDemoDevice *)device
{
    void (^update)(void) = ^void(void) {
        [self configureDevice];
    };
    if (![NSThread isMainThread]) dispatch_sync(dispatch_get_main_queue(), update);
    else update();
}

- (void)didDisconnectFromDevice
{
    
}

#pragma mark -
#pragma mark - UIPickerViewDelegate methods
- (NSAttributedString *)pickerView:(UIPickerView *)pickerView attributedTitleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    NSAttributedString *attString;
    NSString *title;
    
    //if (updateDevice == nil) {
    //    title = @"";
    //} else {
        title = [firmwareList objectAtIndex:row];
    //}
    
    attString = [[NSAttributedString alloc] initWithString:title attributes:@{NSForegroundColorAttributeName:[UIColor whiteColor]}];
    
    return attString;
}

#pragma mark - UIPickerViewDataSource methods
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
    //if (updateDevice == nil) {
    //    return 0;
    //}
    
    return firmwareList.count;
}

#pragma mark -
#pragma mark - RigFirmwareUpdateManagerDelegate methods
- (void)updateProgress:(float)progress
{
    void (^update)(void) = ^void(void) {
        _updateProgressView.progress = progress;
    };
    if (![NSThread isMainThread]) dispatch_sync(dispatch_get_main_queue(), update);
    else update();
}

- (void)updateStatus:(NSString *)status errorCode:(RigDfuError_t)error
{
    void (^update)(void) = ^void(void) {
        _updateStatusLabel.text = status;
    };
    if (![NSThread isMainThread]) dispatch_sync(dispatch_get_main_queue(), update);
    else update();
}

- (void)didFinishUpdate
{
    isUpdateInProgress = NO;
    if (isAlreadyBootloader) {
        didUpdateThroughBootloader = YES;
    }
    isAlreadyBootloader = NO;
    didCompleteAnUpdate = YES;
    
    void (^update)(void) = ^void(void) {
        _updateStatusLabel.text = @"Update Complete";
        self.tabBarController.tabBar.userInteractionEnabled = YES;
        if (isBlinkyDemo || isBmdWare) {
            isBlinkyDemo = NO;
            isBmdWare = NO;
            [self.tabBarController setSelectedIndex:0];
        }
    };
    if (![NSThread isMainThread]) dispatch_sync(dispatch_get_main_queue(), update);
    else update();
}

- (void)updateFailed:(NSString*)status errorCode:(RigDfuError_t)error
{
    void (^update)(void) = ^void(void) {
        isUpdateInProgress = NO;
        didCompleteAnUpdate = NO;
        self.tabBarController.tabBar.userInteractionEnabled = YES;
        
        UIAlertController *ac = [UIAlertController alertControllerWithTitle:@"Update Failed" message:@"The update failed.  Please restart the app." preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *aaYes = [UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleDefault handler:nil];
        
        [ac addAction:aaYes];
        [self presentViewController:ac animated:NO completion:nil];
    };
    if (![NSThread isMainThread]) dispatch_sync(dispatch_get_main_queue(), update);
    else update();
}

#pragma mark -
#pragma mark - BMD200EvalDemoDeviceDelegate methods

- (void)didDiscoverHardwareVersion {
    dispatch_sync(dispatch_get_main_queue(), ^{
        [self displayAppropriateMessage];
        [self configureDeploymentPicker];
    });
}

- (void)unableToDiscoverHardwareVersion {
    dispatch_sync(dispatch_get_main_queue(), ^{
        [self displayAppropriateMessage];
        [self configureDeploymentPicker];
    });
}

@end
