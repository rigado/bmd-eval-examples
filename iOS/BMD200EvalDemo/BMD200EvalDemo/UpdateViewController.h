//
//  UpdateViewController.h
//  BMD200EvalDemo
//
//  Created by Eric P. Stutzenberger on 7/13/15.
//  Copyright (c) 2015 Rigado,LLC. All rights reserved.
//
//  Source code licensed under BMD-200 Software License Agreement.
//  You should have received a copy with purchase of BMD-200 product.
//  If not, contact info@rigado.com for for a copy.

#import <UIKit/UIKit.h>

@interface UpdateViewController : UIViewController
@property (weak, nonatomic) IBOutlet UILabel *updateStatusLabel;
@property (weak, nonatomic) IBOutlet UIProgressView *updateProgressView;
- (IBAction)didTouchBeginUpdate:(id)sender;
@property (weak, nonatomic) IBOutlet UIPickerView *deploymentPicker;

@end
