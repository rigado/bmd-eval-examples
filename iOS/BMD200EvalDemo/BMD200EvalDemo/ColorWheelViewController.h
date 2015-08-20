//
//  SecondViewController.h
//  BMD200EvalDemo
//
//  Created by Eric P. Stutzenberger on 7/6/15.
//  Copyright (c) 2015 Rigado,LLC. All rights reserved.
//
//  Source code licensed under BMD-200 Software License Agreement.
//  You should have received a copy with purchase of BMD-200 product.
//  If not, contact info@rigado.com for for a copy.

#import <UIKit/UIKit.h>
#import "ISColorWheel.h"

@interface ColorWheelViewController : UIViewController

@property (weak, nonatomic) IBOutlet ISColorWheel *wheelView;
@property (weak, nonatomic) IBOutlet UISwitch *ledOnOffSwitch;
- (IBAction)didChangeLedOnOffSwitch:(id)sender;

@end

