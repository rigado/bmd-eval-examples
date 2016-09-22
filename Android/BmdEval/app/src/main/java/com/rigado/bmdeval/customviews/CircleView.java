package com.rigado.bmdeval.customviews;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewDebug.ExportedProperty;

import com.rigado.bmdeval.R;

import java.util.HashMap;

/**
 * Purdy little circles :-)
 *
 * study material
 * see http://stackoverflow.com/questions/18693721/oval-shape-clipped-when-created-programmatically
 *
 */
public class CircleView extends View implements OnTouchListener, OnDragListener {

	private final int SOME_LARGE_HEIGHT = 200;
	private final float MOVE_DISTANCE_UNTIL_DRAG = 10;
	
	private Paint circlePaint;
	private Paint circleStrokePaint;
	private RectF circleArc;
	
	private float mScreenDensity;//see: http://stackoverflow.com/a/10948031/550471

	// Attrs
	private int circleRadius;
	private int circleFillColor;
	private int circleStrokeColor;
	private int circleStartAngle;
	private int circleEndAngle;
	private int circleDragEnabled;
	private int circleDropEnabled;
	
	// touch
	private float mTouchX;
	private float mTouchY;

	//programmatic instantiation
	public CircleView(Context context, HashMap<String, Integer> attrib) {

		super(context);
		// Read all attributes
		circleRadius = attrib.get("circleview_cRadius");
		circleFillColor = attrib.get("circleview_cFillColor");
		circleStrokeColor = attrib.get("circleview_cStrokeColor");
		circleStartAngle = attrib.get("circleview_cAngleStart");
		circleEndAngle = attrib.get("circleview_cAngleEnd");
		
		//enable dragging this view
		if (attrib.containsKey("circleview_cDragEnabled"))
		{
			circleDragEnabled = attrib.get("circleview_cDragEnabled");
		}
		else
		{
			circleDragEnabled = 0;
		}
		
		//enable reception of dropped data
		if (attrib.containsKey("circleview_cDropEnabled"))
		{
			circleDropEnabled = attrib.get("circleview_cDropEnabled");
		}
		else
		{
			circleDropEnabled = 0;
		}
		
		mScreenDensity = context.getResources().getDisplayMetrics().density;

		//init
		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setStyle(Paint.Style.FILL);
		circleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circleStrokePaint.setStyle(Paint.Style.STROKE);
		circleStrokePaint.setStrokeWidth(2);
		circleStrokePaint.setColor(circleStrokeColor);
		
		//touch listener to initiate dragging
		setOnTouchListener(this);

		//listener for drag drop
		setOnDragListener(this);
	}

	//XML Inflation
	public CircleView(Context context, AttributeSet attrs) {

		super(context, attrs);
		init(attrs); // Read all attributes
		
		mScreenDensity = context.getResources().getDisplayMetrics().density;

		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setStyle(Paint.Style.FILL);
		circleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circleStrokePaint.setStyle(Paint.Style.STROKE);
		circleStrokePaint.setStrokeWidth(2);
		circleStrokePaint.setColor(circleStrokeColor);
		
		//touch listener to initiate dragging
		setOnTouchListener(this);
		
		//listener for drag drop
		setOnDragListener(this);
	}

	public void init(AttributeSet attrs)
	{
		// Go through all custom attrs.
		TypedArray attrsArray = getContext().obtainStyledAttributes(attrs, R.styleable.CircleView);
		circleRadius = attrsArray.getInteger(R.styleable.CircleView_cRadius, 0);
		circleFillColor = attrsArray.getColor(R.styleable.CircleView_cFillColor, 16777215);
		circleStrokeColor = attrsArray.getColor(R.styleable.CircleView_cStrokeColor, -1);
		circleStartAngle = attrsArray.getInteger(R.styleable.CircleView_cAngleStart, 0);
		circleEndAngle = attrsArray.getInteger(R.styleable.CircleView_cAngleEnd, 360);
		circleDragEnabled = attrsArray.getBoolean(R.styleable.CircleView_cDragEnabled, false) ? 1 : 0;

		// Google tells us to call recycle.
		attrsArray.recycle();
	}
	
	public void setFillColor(int color)
	{
		circleFillColor = color;
		invalidate();
	}
	
	public int getFillColor()
	{
		return circleFillColor;
	}

	//used to return the color of this object when this object is dropped on another object
	@Override
	@ExportedProperty
	public Object getTag()
	{
		return String.valueOf(circleFillColor);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Move canvas down and right 1 pixel, otherwise the stroke gets cut off.
		canvas.translate(1,1);
		circlePaint.setColor(circleFillColor);
		canvas.drawArc(circleArc, circleStartAngle, circleEndAngle, true, circlePaint);
		canvas.drawArc(circleArc, circleStartAngle, circleEndAngle, true, circleStrokePaint);
	}

	private int chooseWidth(int mode, int size){
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY)
		{
			//Log.i("chooseWidth()", "Mode = AT_MOST or EXACTLY");
			return size;
		} else { // (mode == MeasureSpec.UNSPECIFIED)
			//Log.i("chooseWidth()", "Mode = UNSPECIFIED");
			return getPrefferedWidth();
		}
	}

	private int chooseHeight(int mode, int size){
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY)
		{
			//Log.i("chooseHeight()", "Mode = AT_MOST or EXACTLY");
			return size;
		} else { // (mode == MeasureSpec.UNSPECIFIED)
			//Log.i("chooseHeight()", "Mode = UNSPECIFIED");
			return getPrefferedHeight();
		}
	}

	private int getPrefferedWidth()
	{
		int width = getPrefferedHeight();

		return width;
	}

	private int getPrefferedHeight()
	{
		int ipadTop=getPaddingTop();
		int ipadBottom=getPaddingBottom();
		
		int height = (int)(SOME_LARGE_HEIGHT * mScreenDensity) - (ipadTop + ipadBottom);

		return height;
	}
	
	//specify how big this view is going to be
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		
		int widthAllowed = MeasureSpec.getSize(widthMeasureSpec);
		int heightAllowed = MeasureSpec.getSize(heightMeasureSpec);
		
		widthAllowed = chooseWidth(widthMode, widthAllowed);
		heightAllowed = chooseHeight(heightMode, heightAllowed);
		
		//best fit to make it square sides
		int equal_side_length = Math.min(widthAllowed, heightAllowed);
		
		//Log.i("onMeasure()", "equal_side_length ="+equal_side_length);

		setMeasuredDimension(equal_side_length, equal_side_length);
	}
	
	@Override protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		
		int ipadTop=getPaddingTop();
		int ipadBottom=getPaddingBottom();
		int ipadLeft=getPaddingLeft();
		int ipadRight=getPaddingRight();

		int measuredWidth = w-ipadLeft-ipadRight;
		int measuredHeight = h-ipadTop-ipadBottom;
		
		int intendedDiameter = Math.min(measuredWidth, measuredHeight);
		
		if(circleRadius == 0) // No radius specified - Let's see what we can make.
		{
			// Check width size. Make radius half of available.
			circleRadius = intendedDiameter / 2;
		}
		
		// Remove 2 pixels for the stroke.
		int circleDiameter = circleRadius * 2 - 2;

		circleArc = new RectF(0, 0, circleDiameter, circleDiameter);
		circleArc.offsetTo(ipadLeft, ipadTop);

		//Log.d("onSizeChanged()", "measuredHeight =>" + String.valueOf(measuredHeight) + "px measuredWidth => " + String.valueOf(measuredWidth) + "px");
	}

	/**
	 * OnTouchListener Interface - used to initiate dragging
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event)
	{

		boolean bTouchEventHandled = super.onTouchEvent(event);//needed for click events

        int action = event.getAction();
        switch (action)
        {

        case MotionEvent.ACTION_DOWN:
        {
        	//get coordinates of initial touch event
        	mTouchX = event.getX();
        	mTouchY = event.getY();

        	bTouchEventHandled = true;
        	break;
        }
        
        case MotionEvent.ACTION_MOVE:
        {
        	final float currentX = event.getX();
        	final float currentY = event.getY();
        	
        	final float deltaX = Math.abs(mTouchX - currentX);
        	final float deltaY = Math.abs(mTouchY - currentY);
        	
        	//test whether the user wants to drag, if so then init drag mode
        	if ((deltaX > MOVE_DISTANCE_UNTIL_DRAG) | (deltaY > MOVE_DISTANCE_UNTIL_DRAG))
        	{
        		//only drag if enabled
        		if (circleDragEnabled > 0)
        		{
        			// setup drag
        			ClipData.Item item = new ClipData.Item((CharSequence)view.getTag());
        			String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        			ClipData dragData = new ClipData((CharSequence) view.getTag(), mimeTypes, item);

        			// shadow
        			View.DragShadowBuilder myShadow = new DragShadowBuilder(view);

        			// Start the drag
        			view.startDrag(dragData,  // the data to be dragged
        					myShadow,  // the drag shadow builder
        					null,      // no need to use local data
        					0          // flags (not currently used, set to 0)
        					);
        		}
        	}
        	
        	bTouchEventHandled = true;
        }
        }

		
		return bTouchEventHandled;
	}

	/**
	 * OnDragListener Interface - used to respond to drag events
	 */
	@Override
	public boolean onDrag(View viewdroptarget, DragEvent event)
	{
		
		switch (event.getAction())
		{
		case DragEvent.ACTION_DRAG_STARTED:
			boolean valueToReturn = false;
			// Determines if this View can accept the dragged data
			if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
			{
				valueToReturn = true;
			}
			
			return valueToReturn;
			//break;
			
		case DragEvent.ACTION_DRAG_ENTERED:
			//no action necessary
			break;
			
		case DragEvent.ACTION_DRAG_EXITED:
			//no action necessary
			break;
			
		case DragEvent.ACTION_DROP:
			//handle the drop event - some data is being dropped on this view
			if(circleDropEnabled > 0)
			{
				//get data that's being dropped
				ClipData.Item item = event.getClipData().getItemAt(0);

				this.setFillColor(Integer.valueOf((String) item.getText()));

				return true;
			}
			
			//break;
			
		case DragEvent.ACTION_DRAG_ENDED:
			//no action necessary
			break;
			
		default:
			break;
		}

		return false;
	}

}