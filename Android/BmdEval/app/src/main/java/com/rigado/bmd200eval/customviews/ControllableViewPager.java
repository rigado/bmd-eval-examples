package com.rigado.bmd200eval.customviews;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * ViewPager that allows disabling swiping
 * Extreme circumstances require extreme methods... the default ViewPager doesn't let one disable swiping
 */
public class ControllableViewPager extends ViewPager
{

    private boolean mSwipingEnabled;

    public ControllableViewPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mSwipingEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (mSwipingEnabled)
        {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event)
    {
        if (mSwipingEnabled)
        {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setPagingEnabled(boolean enabled)
    {
        mSwipingEnabled = enabled;
    }
}
