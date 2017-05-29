//
//  CBUUID+UUIDHelperMethods.h
//  BMDEvalDemo
//
//  Created by Eric P. Stutzenberger on 7/13/15.
//  Copyright © 2017 Rigado, Inc. All rights reserved.
//
//  Source code licensed under Rigado Software License Agreement.
//  You should have received a copy with purchase of a Rigado product.
//  If not, contact info@rigado.com for a copy.

//  This category class provides a few helper methods for generating UUIDs.

#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface CBUUID (UUIDHelperMethods)

/**
 *  This function builds a full 128-bit UUID from a base UUID and a 16-bit UUID.
 *
 *  @param baseUuidString      The base UUID string for a full 128-bit UUID.  This string should ideally have
 *                             bytes 2 and 3 set to 0x00 and 0x00.
 *  @param shortUuidString     A 16-bit UUID string.  This string is filled in to base UUID string to create a
 *                             full 128-bit UUID string.
 *
 *  @return New 128-bit CBUUID if successful, nil otherwise
 */
+ (CBUUID*)uuidForBaseUuidString:(NSString*)baseUuidString withShortUuidString:(NSString*)shortUuidString;

/**
 *  This function builds a CBUUID object from hexadecimal input array wrapped in an NSData object.
 *
 *  @param data                 The UUID data array
 *
 *  @return New 128-bit CBUUID if successful, nil otherwise
 */
+ (CBUUID*)uuidFromHexData:(NSData*)data;

@end
