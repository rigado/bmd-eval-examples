//
//  FirstViewController.h
//  BMDEvalDemo
//
//  Created by Eric P. Stutzenberger on 7/13/15.
//  Copyright © 2017 Rigado, Inc. All rights reserved.
//
//  Source code licensed under Rigado Software License Agreement.
//  You should have received a copy with purchase of a Rigado product.
//  If not, contact info@rigado.com for a copy.

//  This class handles all information display on the Status tab.

#import <UIKit/UIKit.h>
#import "HardwareButton.h"

@interface StatusViewController : UIViewController

@property (weak, nonatomic) IBOutlet UIImageView *ambientLightLevelImageView;
@property (weak, nonatomic) IBOutlet UIButton *userButtonTwo;
@property (weak, nonatomic) IBOutlet UIButton *userButtonOne;

@property (weak, nonatomic) IBOutlet UILabel *ambientLightLevelLabel;
@property (weak, nonatomic) IBOutlet UILabel *accelXLabel;
@property (weak, nonatomic) IBOutlet UILabel *accelYLabel;
@property (weak, nonatomic) IBOutlet UILabel *accelZLabel;
@property (weak, nonatomic) IBOutlet UIView *accelGraphView;

@end

