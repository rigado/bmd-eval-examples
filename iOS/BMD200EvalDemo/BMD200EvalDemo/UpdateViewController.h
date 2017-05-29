//
//  UpdateViewController.h
//  BMDEvalDemo
//
//  Created by Eric P. Stutzenberger on 7/13/15.
//  Copyright © 2017 Rigado, Inc. All rights reserved.
//
//  Source code licensed under Rigado Software License Agreement.
//  You should have received a copy with purchase of a Rigado product.
//  If not, contact info@rigado.com for a copy.

#import <UIKit/UIKit.h>

@interface UpdateViewController : UIViewController
@property (weak, nonatomic) IBOutlet UILabel *updateStatusLabel;
@property (weak, nonatomic) IBOutlet UIProgressView *updateProgressView;
- (IBAction)didTouchBeginUpdate:(id)sender;
@property (weak, nonatomic) IBOutlet UIPickerView *deploymentPicker;

@end
