//
//  TabBarController.h
//  BMDEvalDemo
//
//  Created by Eric P. Stutzenberger on 7/13/15.
//  Copyright © 2017 Rigado, Inc. All rights reserved.
//
//  Source code licensed under Rigado Software License Agreement.
//  You should have received a copy with purchase of a Rigado product.
//  If not, contact info@rigado.com for a copy.

//  This class handles searching for and connecting to demo devices.  Depending on the type of device
//  connection formed, it will take additoinal actions.  If the app connects to either the Blinky demo
//  or BMDware, this class will automatically load the Update tab.  The update tab will then display an
//  informative message to the user regarding their options and offer to flash the main demo firmware.

#import <UIKit/UIKit.h>
#import "Rigablue.h"
#import "BMDEvalDemoDevice.h"

@protocol BMDEvalDemoTabBarDelegate <NSObject>

@required
- (void)didConnectToDevice:(BMDEvalDemoDevice*)device;
- (void)didDisconnectFromDevice;
@end

@interface BMDEvalDemoTabBarController : UITabBarController

/**
 *  Retrieves the currently connected demo device object.
 *
 *  @return The connected demo device, nil if not connected
 */
- (BMDEvalDemoDevice*)getDevice;

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
 *  @return YES if connected to VS132_3_0zSoftDevice, NO otherwise
 */
- (BOOL)isConnectedToVS132_3_0;

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
 *  BMDEvalDemoTabBarDelegate protocol.
 */
- (BOOL)registerListener:(id<BMDEvalDemoTabBarDelegate>)delegate;

/**
 *  Function to unregister a deletage object from receiving protocol messages from the
 *  BMDEvalDemoTabBarDelegate protocol.
 */
- (BOOL)unregiserListener:(id<BMDEvalDemoTabBarDelegate>)delegate;

@end
