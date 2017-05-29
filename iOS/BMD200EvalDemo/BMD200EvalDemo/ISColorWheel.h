//
//  ISColorWheel.h
//  
//
//  BMDEvalDemo
//
//  Created by Eric P. Stutzenberger on 7/13/15.
//  Copyright © 2017 Rigado, Inc. All rights reserved.
//
//  Source code licensed under Rigado Software License Agreement.
//  You should have received a copy with purchase of a Rigado product.
//  If not, contact info@rigado.com for a copy.
//

/*
 Copyright (c) 2012 Inline Studios
 Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

#import <UIKit/UIKit.h>

@class ISColorWheel;

typedef struct
{
    unsigned char r;
    unsigned char g;
    unsigned char b;
    
} PixelRGB;

@class ISColorKnob;

float ISColorWheel_PointDistance (CGPoint p1, CGPoint p2);
PixelRGB ISColorWheel_HSBToRGB (float h, float s, float v);

@protocol ISColorWheelDelegate <NSObject>
@required
- (void)colorWheelDidChangeColor:(ISColorWheel*)colorWheel;
@end


@interface ISColorWheel : UIView
{
    CGImageRef _radialImage;
    ISColorKnob* _knobView;
    float _radius;
    CGSize _knobSize;
    CGPoint _touchPoint;
    float _brightness;
    bool _continuous;
    id <ISColorWheelDelegate> _delegate;
}

@property(nonatomic, strong)UIView* knobView;
@property(nonatomic, assign)CGSize knobSize;
@property(nonatomic, assign)float brightness;
@property(nonatomic, assign)bool continuous;
@property(nonatomic, strong)id <ISColorWheelDelegate> delegate;

- (void)initWheelWithFrame:(CGRect)frame;

- (void)updateImage;

- (void)setCurrentColor:(UIColor*)color;
- (UIColor*)currentColor;

- (PixelRGB)currentRGBColor;
- (void)setTouchPoint:(CGPoint)point;

@end
