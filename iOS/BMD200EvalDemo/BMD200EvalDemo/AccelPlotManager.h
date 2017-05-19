//
//  AccelPlotManager.h
//  BMDEvalDemo
//
//  Created by Eric P. Stutzenberger on 7/13/15.
//  Copyright © 2017 Rigado, Inc. All rights reserved.
//
//  Source code licensed under Rigado Software License Agreement.
//  You should have received a copy with purchase of a Rigado product.
//  If not, contact info@rigado.com for a copy.

//  This class interfaces with the CorePlot library to graph incoming accelerometer data.

#import <Foundation/Foundation.h>
#import "CorePlot-CocoaTouch.h"
#import "BMDEvalDemoDevice.h"

@interface AccelPlotManager : NSObject
- (id)initWithFrame:(CGRect)frame;

@property (nonatomic, strong) CPTGraphHostingView *hostView;

- (void)addSample:(AccelData_t)sample;
- (void)resetSampleCount;

@end
