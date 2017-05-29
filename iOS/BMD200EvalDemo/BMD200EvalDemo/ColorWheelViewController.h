//
//  SecondViewController.h
//  BMDEvalDemo
//
//  Created by Eric P. Stutzenberger on 7/13/15.
//  Copyright © 2017 Rigado, Inc. All rights reserved.
//
//  Source code licensed under Rigado Software License Agreement.
//  You should have received a copy with purchase of a Rigado product.
//  If not, contact info@rigado.com for a copy.


#import <UIKit/UIKit.h>
#import "ISColorWheel.h"

@interface ColorWheelViewController : UIViewController

@property (weak, nonatomic) IBOutlet ISColorWheel *wheelView;
@property (weak, nonatomic) IBOutlet UISwitch *ledOnOffSwitch;
- (IBAction)didChangeLedOnOffSwitch:(id)sender;

@end

