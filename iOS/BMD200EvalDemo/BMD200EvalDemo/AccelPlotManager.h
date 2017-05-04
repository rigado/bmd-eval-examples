//
//  AccelPlotManager.h
//  BMD200EvalDemo
//
//  Created by Eric P. Stutzenberger on 7/10/15.
//  Copyright (c) 2015 Rigado,LLC. All rights reserved.
//
//  Source code licensed under BMD-200 Software License Agreement.
//  You should have received a copy with purchase of BMD-200 product.
//  If not, contact info@rigado.com for for a copy.

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
