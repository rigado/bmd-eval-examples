//
//  ISColorWheel.h
//  
//
//  Created by Eric Stutzenberger on 3/25/13.
//  Copyright (c) 2013 Rigado, LLC. All rights reserved.
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
