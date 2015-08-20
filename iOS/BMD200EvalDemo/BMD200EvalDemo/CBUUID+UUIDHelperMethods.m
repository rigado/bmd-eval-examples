//
//  CBUUID+UUIDHelperMethods.m
//  RigadoToolbox
//
//  Created by Eric P. Stutzenberger on 12/9/14.
//  Copyright (c) 2014 Rigado,LLC. All rights reserved.
//
//  Source code licensed under BMD-200 Software License Agreement.
//  You should have received a copy with purchase of BMD-200 product.
//  If not, contact info@rigado.com for for a copy.

#import "CBUUID+UUIDHelperMethods.h"

@implementation CBUUID (UUIDHelperMethods)

+ (CBUUID*)uuidForBaseUuidString:(NSString*)baseUuidString withShortUuidString:(NSString*)shortUuidString
{
    if (baseUuidString == nil || shortUuidString == nil) {
        return nil;
    }
    
    NSMutableString *uuidString = [[NSMutableString alloc] initWithString:baseUuidString];
    [uuidString replaceCharactersInRange:NSMakeRange(4, 4) withString:shortUuidString];
    return [CBUUID UUIDWithString:uuidString];
}


+ (CBUUID*)uuidFromHexData:(NSData*)data
{
    char ascii_to_hex[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    
    if (data == nil) {
        return nil;
    }
    
    NSUInteger valueLen = data.length;
    NSMutableString *valueStr = [[NSMutableString alloc] init];
    if (valueLen == 0) {
        return nil;
    }
    
    for (NSUInteger i = 0; i < valueLen; i++) {
        uint8_t temp = ((uint8_t*)data.bytes)[i];
        char upper = (ascii_to_hex[temp >> 4]);
        char lower = (ascii_to_hex[temp & 0x0F]);
        [valueStr appendFormat:@"%c%c", upper, lower];
        
        if (i == 3 || i == 5 | i == 7 || i == 9) {
            [valueStr appendFormat:@"%c", '-'];
        }
    }
    
    return [CBUUID UUIDWithString:valueStr];
}
@end
