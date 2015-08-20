//
//  RigDfuError.h
//  Rigablue Library
//
//  Created by Eric Stutzenberger on 7/10/14.
//  @copyright (c) 2014 Rigado, LLC. All rights reserved.
//
//  Source code licensed under BMD-200 Software License Agreement.
//  You should have received a copy with purchase of BMD-200 product.
//  If not, contact info@rigado.com for for a copy.

#ifndef Rigablue_RigDfuError_h
#define Rigablue_RigDfuError_h

//Error code for potential failure points within the firmware update manager
typedef enum DfuErrorEnum
{
    DfuError_None = 0,
    DfuError_BadPeripheral = -1,
    DfuError_ControlPointCharacteristicMissing = -2,
    DfuError_BadDevice = -3,
    DfuError_PeripheralNotSet = -4,
    DfuError_DiscoveryTimetout = -5,
    
} RigDfuError_t;

#endif
