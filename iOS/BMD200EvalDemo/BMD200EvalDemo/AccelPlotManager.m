//
//  AccelPlotManager.m
//  BMDEvalDemo
//
//  Created by Eric P. Stutzenberger on 7/13/15.
//  Copyright © 2017 Rigado, Inc. All rights reserved.
//
//  Source code licensed under Rigado Software License Agreement.
//  You should have received a copy with purchase of a Rigado product.
//  If not, contact info@rigado.com for a copy.

//  See CorePlot-License.txt for CorePlot license.

#import "AccelPlotManager.h"

#define GRAPH_PLOT_AREA_PADDING_LEFT    30.0f
#define GRAPH_PLOT_AREA_PADDING_BOTTOM  1.0f
#define GRAPH_PLOT_AREA_PADDING_TOP     20.0f
#define PLOT_MAX_SAMPLES                50

@interface AccelPlotManager() <CPTPlotDataSource>
{
    CPTTheme *selectedTheme;
    CPTGraph *graph;
    
    int total_samples;
    NSMutableArray *sampleList;
}
@end

@implementation AccelPlotManager
- (id)initWithFrame:(CGRect)frame
{
    self = [super init];
    if (self) {
        [self initPlotWithFrame:frame];
        sampleList = [[NSMutableArray alloc] init];
    }
    return self;
}

-(void)initPlotWithFrame:(CGRect)frame
{
    total_samples = 0;
    [self configureHostWithFrame:frame];
    [self configureGraph];
    [self configureChart];
    //[self configureLegend];
    [self configureAxes];
}

- (void)resetSampleCount
{
    total_samples = 0;
}

-(void)configureHostWithFrame:(CGRect)frame
{
    CGRect initFrame;
    
    initFrame.origin.x = 0;
    initFrame.origin.y = 0;
    initFrame.size = frame.size;
    _hostView = [(CPTGraphHostingView *) [CPTGraphHostingView alloc] initWithFrame:initFrame];
    _hostView.allowPinchScaling = YES;
}

-(void)configureGraph
{
    // 1 - Create the graph
    CGRect initFrame = _hostView.bounds;
    graph = [[CPTXYGraph alloc] initWithFrame:initFrame];
    [graph applyTheme:[CPTTheme themeNamed:kCPTPlainWhiteTheme]];
    
    _hostView.hostedGraph = graph;
    [graph setPaddingLeft:0.0f];
    [graph setPaddingBottom:0.0f];
    [graph setPaddingRight:0.0f];
    [graph setPaddingTop:0.0f];
    // 2 - Set graph title
    NSString *title = @"Accelerometer Data";
    graph.title = title;
    
    // 3 - Create and set text style
    CPTMutableTextStyle *titleStyle = [CPTMutableTextStyle textStyle];
    //titleStyle.color = [CPTColor whiteColor];
    titleStyle.fontName = @"Helvetica-Bold";
    titleStyle.fontSize = 16.0f;
    graph.titleTextStyle = titleStyle;
    
    // 4 - Set padding for plot area
    [graph.plotAreaFrame setPaddingLeft:GRAPH_PLOT_AREA_PADDING_LEFT];
    [graph.plotAreaFrame setPaddingBottom:GRAPH_PLOT_AREA_PADDING_BOTTOM];
    [graph.plotAreaFrame setPaddingTop:GRAPH_PLOT_AREA_PADDING_TOP];
    
    // 5 - Enable user interactions for plot space
    CPTXYPlotSpace *plotSpace = (CPTXYPlotSpace *) graph.defaultPlotSpace;
    plotSpace.allowsUserInteraction = NO;
}

-(void)configureChart
{
    CPTXYPlotSpace *plotSpace = (CPTXYPlotSpace *) graph.defaultPlotSpace;
    NSMutableArray *plotList = [[NSMutableArray alloc] init];
    CPTPlot *plot = nil;
    
    plot = [self setupAccelPlot:@"AccelX" withLineColor:[CPTColor colorWithComponentRed: 0.7333 green: 0.0078 blue: 0.1882 alpha: 1.0]];
    [plotList addObject:plot];
    
    plot = [self setupAccelPlot:@"AccelY" withLineColor:[CPTColor colorWithComponentRed: 0.0039 green: 0.5294 blue: 0.7451 alpha: 1.0]];
    [plotList addObject:plot];
    
    plot = [self setupAccelPlot:@"AccelZ" withLineColor:[CPTColor colorWithComponentRed: 0.0078 green: 0.6118 blue: 0.3882 alpha: 1.0]];
    [plotList addObject:plot];
    
    // 3 - Set up plot space
    [plotSpace scaleToFitPlots:plotList];
    
    CPTMutablePlotRange *yRange = [plotSpace.yRange mutableCopy];
    [yRange expandRangeByFactor:CPTDecimalFromCGFloat(1.0f)];
    plotSpace.yRange = [CPTPlotRange plotRangeWithLocation: CPTDecimalFromDouble(-1.5f) length: CPTDecimalFromDouble(4.0f)];
}

- (CPTPlot*)setupAccelPlot:(NSString*)identifier withLineColor:(CPTColor*)lineColor
{
    CPTXYPlotSpace *plotSpace = (CPTXYPlotSpace *) graph.defaultPlotSpace;
    // 2 - Create the three plots
    CPTScatterPlot *plot = [[CPTScatterPlot alloc] init];
    plot.dataSource = self;
    plot.identifier = identifier;
    [graph addPlot:plot toPlotSpace:plotSpace];
    
    CPTMutableLineStyle *lineStyle = [plot.dataLineStyle mutableCopy];
    lineStyle.lineWidth = 2.5;
    lineStyle.lineColor = lineColor;
    plot.dataLineStyle = lineStyle;
    
    return plot;
}

-(void)configureAxes
{
    // 1 - Create styles
    CPTMutableTextStyle *axisTitleStyle = [CPTMutableTextStyle textStyle];
    //axisTitleStyle.color = [CPTColor colorWithCGColor:_temperatureLabel.textColor.CGColor];
    axisTitleStyle.fontName = @"Helvetica-Bold";
    axisTitleStyle.fontSize = 12.0f;
    CPTMutableLineStyle *axisLineStyle = [CPTMutableLineStyle lineStyle];
    axisLineStyle.lineWidth = 2.0f;
    axisLineStyle.lineColor = [CPTColor blackColor];
    CPTMutableTextStyle *axisTextStyle = [[CPTMutableTextStyle alloc] init];
    //axisTextStyle.color = [CPTColor colorWithCGColor:_temperatureLabel.textColor.CGColor];
    axisTextStyle.fontName = @"Helvetica-Bold";
    axisTextStyle.fontSize = 11.0f;
    CPTMutableLineStyle *tickLineStyle = [CPTMutableLineStyle lineStyle];
    tickLineStyle.lineColor = [CPTColor whiteColor];
    tickLineStyle.lineWidth = 2.0f;
    CPTMutableLineStyle *gridLineStyle = [CPTMutableLineStyle lineStyle];
    tickLineStyle.lineColor = [CPTColor blackColor];
    tickLineStyle.lineWidth = 1.0f;
    
    // 2 - Get axis set
    CPTXYAxisSet *axisSet = (CPTXYAxisSet *)graph.axisSet;
    axisSet.xAxis.axisConstraints = [CPTConstraints constraintWithLowerOffset:0.0];
    
    // 3 - Configure x-axis
    CPTAxis *x = axisSet.xAxis;
    x.hidden = YES;
    x.labelingPolicy = CPTAxisLabelingPolicyNone;
    for (CPTAxisLabel *axisLabel in x.axisLabels) {
        axisLabel.contentLayer.hidden = YES;
    }
    
    // 4 - Configure y-axis
    CPTAxis *y = axisSet.yAxis;
    axisSet.yAxis.axisConstraints = [CPTConstraints constraintWithLowerOffset:0.0];
    y.title = @"Acceleration (g)";
    y.titleTextStyle = axisTitleStyle;
    y.titleOffset = -25.0f;
    y.axisLineStyle = axisLineStyle;
    y.majorGridLineStyle = gridLineStyle;
    y.labelingPolicy = CPTAxisLabelingPolicyAutomatic;
    y.labelTextStyle = axisTextStyle;
    y.labelOffset = 5.0f;
    y.majorTickLineStyle = axisLineStyle;
    y.majorTickLength = 4.0f;
    y.minorTickLength = 2.0f;
    y.tickDirection = CPTSignPositive;
    //CGFloat majorIncrement = 1.0;
    CGFloat minorIncrement = 0.1;
    CGFloat yMax = 2.0f;  // should determine dynamically based on max price
    NSMutableSet *yLabels = [NSMutableSet set];
    NSMutableSet *yMajorLocations = [NSMutableSet set];
    NSMutableSet *yMinorLocations = [NSMutableSet set];
    for (CGFloat j = -2.0f; j <= yMax; j += minorIncrement) {
        if (j == -2.0 || j == -1.0 || j == 0.0 || j == 1.0 || j == 2.0) {
            CPTAxisLabel *label = [[CPTAxisLabel alloc] initWithText:[NSString stringWithFormat:@"%02f", j] textStyle:y.labelTextStyle];
            NSDecimal location = CPTDecimalFromInteger(j);
            label.tickLocation = location;
            label.offset = -y.majorTickLength - y.labelOffset;
            if (label) {
                [yLabels addObject:label];
            }
            [yMajorLocations addObject:[NSDecimalNumber decimalNumberWithDecimal:location]];
        } else {
            [yMinorLocations addObject:[NSDecimalNumber decimalNumberWithDecimal:CPTDecimalFromInteger(j)]];
        }
    }
    y.axisLabels = yLabels;
    y.majorTickLocations = yMajorLocations;
    y.minorTickLocations = yMinorLocations;
}

- (void)addSample:(AccelData_t)sample
{
    //  Once started, the accelerometer data stream does not stop until the stop message is sent to the device or
    //  a disconnection occurs.  Since the incoming data rate is 12.5 Hz, a decently large number of objects can
    //  be created in a short amount of time.  To manage this, only 50 samples are kept and graphed at a time.  Once
    //  50 samples have been collected, the oldest sample is dropped and the newest sample is added.  The plot is
    //  then redrawn with the newest sample.
    NSValue *accelValue = [NSValue value:&sample withObjCType:@encode(AccelData_t)];
    if (sampleList.count == 50) {
        NSRange range = { 1, 49 };
        sampleList = [NSMutableArray arrayWithArray:[sampleList subarrayWithRange:range]];
    }
    [sampleList addObject:accelValue];
    total_samples++;
    
    void (^reload)(void) = ^void(void)
    {
        CPTXYPlotSpace *plotSpace = (CPTXYPlotSpace *) graph.defaultPlotSpace;
        float plotMin = 0.0f;
        if (total_samples > PLOT_MAX_SAMPLES) {
            plotMin = total_samples - PLOT_MAX_SAMPLES;
        }
        plotSpace.xRange = [CPTPlotRange plotRangeWithLocation: CPTDecimalFromDouble(plotMin) length: CPTDecimalFromDouble(50.0)];
        
        [graph reloadData];
    };
    
    if (![NSThread isMainThread]) dispatch_sync(dispatch_get_main_queue(), reload);
    else reload();
}

#pragma mark -
#pragma mark - CPTPlotDataSource methods
-(NSUInteger)numberOfRecordsForPlot:(CPTPlot *)plot
{
    if (total_samples < PLOT_MAX_SAMPLES) {
        return total_samples;
    }
    return PLOT_MAX_SAMPLES;
}

-(NSNumber *)numberForPlot:(CPTPlot *)plot field:(NSUInteger)fieldEnum recordIndex:(NSUInteger)index
{
    switch (fieldEnum) {
        case CPTScatterPlotFieldX:
        {
            NSUInteger plot_index = index;
            if (total_samples > PLOT_MAX_SAMPLES) {
                plot_index = (total_samples - PLOT_MAX_SAMPLES) + index;
            }
            return [NSNumber numberWithUnsignedInteger:plot_index];
        }
            break;
            
        case CPTScatterPlotFieldY:
        {
            NSArray *accelDataListCopy = [sampleList copy];
            NSValue *accelValue = [accelDataListCopy objectAtIndex:index];
            AccelData_t accelData;
            [accelValue getValue:&accelData];
            
            float plotValue = 0.0f;
            //  Accelerometer is configured for 8 G maximum so samples are scaled down by a factor of 16 where
            //  a value of 127 (or -128) is a value of 8 G.  This means that +/- 16 is the value reported for 1G.
            if ([plot.identifier isEqual:@"AccelX"]) {
                plotValue = (((int8_t)accelData.x) * 8) / 128.0f;
            } else if([plot.identifier isEqual:@"AccelY"]) {
                plotValue = (((int8_t)accelData.y) * 8) / 128.0f;
            } else if([plot.identifier isEqual:@"AccelZ"]) {
                plotValue = (((int8_t)accelData.z) * 8) / 128.0f;
            }
            return [NSNumber numberWithFloat:plotValue];
        }
            break;
    }
    return [NSDecimalNumber zero];
}

-(CPTLayer *)dataLabelForPlot:(CPTPlot *)plot recordIndex:(NSUInteger)index
{
    return nil;
}

@end
