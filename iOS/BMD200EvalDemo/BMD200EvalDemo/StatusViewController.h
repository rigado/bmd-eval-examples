//
//  FirstViewController.h
//  BMD200EvalDemo
//
//  Created by Eric P. Stutzenberger on 7/6/15.
//  Copyright (c) 2015 Rigado,LLC. All rights reserved.
//
//  Source code licensed under BMD-200 Software License Agreement.
//  You should have received a copy with purchase of BMD-200 product.
//  If not, contact info@rigado.com for for a copy.

//  This class handles all information display on the Status tab.

#import <UIKit/UIKit.h>
#import "HardwareButton.h"

@interface StatusViewController : UIViewController

@property (weak, nonatomic) IBOutlet UIImageView *ambientLightLevelImageView;
@property (weak, nonatomic) IBOutlet HardwareButton *userButtonTwo;
@property (weak, nonatomic) IBOutlet HardwareButton *userButtonOne;

@property (weak, nonatomic) IBOutlet UILabel *ambientLightLevelLabel;
@property (weak, nonatomic) IBOutlet UILabel *accelXLabel;
@property (weak, nonatomic) IBOutlet UILabel *accelYLabel;
@property (weak, nonatomic) IBOutlet UILabel *accelZLabel;
@property (weak, nonatomic) IBOutlet UIView *accelGraphView;

@end

