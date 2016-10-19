//
//  TabBarController.h
//  BMD200EvalDemo
//
//  Created by Eric P. Stutzenberger on 7/6/15.
//  Copyright (c) 2015 Rigado,LLC. All rights reserved.
//
//  Source code licensed under BMD-200 Software License Agreement.
//  You should have received a copy with purchase of BMD-200 product.
//  If not, contact info@rigado.com for for a copy.

//  This class handles searching for and connecting to demo devices.  Depending on the type of device
//  connection formed, it will take additoinal actions.  If the app connects to either the Blinky demo
//  or BMDware, this class will automatically load the Update tab.  The update tab will then display an
//  informative message to the user regarding their options and offer to flash the main demo firmware.

#import <UIKit/UIKit.h>
#import "Rigablue.h"
#import "BMD200EvalDemoDevice.h"

@protocol BMD200EvalDemoTabBarDelegate <NSObject>

@required
- (void)didConnectToDevice:(BMD200EvalDemoDevice*)device;
- (void)didDisconnectFromDevice;
@end

@interface BMD200EvalDemoTabBarController : UITabBarController

/**
 *  Retrieves the currently connected demo device object.
 *
 *  @return The connected demo device, nil if not connected
 */
- (BMD200EvalDemoDevice*)getDevice;

/**
 *  Returns the connected Blinky demo device if appropriate.
 *
 *  @return Returns the blink demo or BMDware device object if appropriate.  nil if not connected to either.
 */
- (RigLeBaseDevice*)getBlinkyDemoDevice;

/**
 *  Returns the current connection state.
 *
 *  @return YES if connected to a device, NO otherwise
 */
- (BOOL)isConnected;

/**
 *  @return YES if connected to the Blinky demo, NO otherwise
 */
- (BOOL)isConnectedToBlinkyDemo;

/**
 *  @return YES if connected to BMDware, NO otherwise
 */
- (BOOL)isConnectedToBmdWare;

/**
 *  @return YES if connected to 200Device, NO otherwise
 */
- (BOOL)isConnectedTo200;

/**
 *  @return YES if connected to 300Device, NO otherwise
 */
- (BOOL)isConnectedTo300;

/**
 *  Instructs ths object to begin searching for an inrage evauluation board running any of the following:
 *  Eval Demo Firmware
 *  Blinky Demo Firmware
 *  BMDware Firmware (firmware programmed during Evaluation Board manufacture)
 */
- (void)searchForDevice;

/**
 *  @return YES if currently searching for a device, NO otherwise
 */
- (BOOL)isSearching;

/**
 *  Stops device searching.
 */
- (void)stopSearchingForDevice;

/**
 *  Function to register a deletage object which wishes to receive protocol messages using the
 *  BMD200EvalDemoTabBarDelegate protocol.
 */
- (BOOL)registerListener:(id<BMD200EvalDemoTabBarDelegate>)delegate;

/**
 *  Function to unregister a deletage object from receiving protocol messages from the
 *  BMD200EvalDemoTabBarDelegate protocol.
 */
- (BOOL)unregiserListener:(id<BMD200EvalDemoTabBarDelegate>)delegate;

@end
