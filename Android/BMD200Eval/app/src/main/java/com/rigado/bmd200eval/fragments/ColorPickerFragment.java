package com.rigado.bmd200eval.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.rigado.bmd200eval.BmdApplication;
import com.rigado.bmd200eval.MainActivity;
import com.rigado.bmd200eval.R;
import com.rigado.bmd200eval.customviews.CircleView;
import com.rigado.bmd200eval.demodevice.BmdEvalDemoDevice;
import com.rigado.bmd200eval.demodevice.RgbColor;
import com.rigado.bmd200eval.interfaces.InterfaceFragmentLifecycle;

public class ColorPickerFragment extends Fragment implements
        OnTouchListener,
        BmdApplication.ConnectionNotification,
        View.OnClickListener,
        InterfaceFragmentLifecycle
{
    private final String TAG = getClass().getSimpleName();
    private BmdApplication mBmdApplication;

    private ImageView mImageWheel;
    private CircleView mImageSelected;
    private ViewPager mViewPager;
    private RelativeLayout mLayoutProgressBar;
    private ToggleButton mToggleButton;

    //Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
    public ColorPickerFragment()
    {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mBmdApplication = (BmdApplication) getActivity().getApplication();

        //inflate the necessary layout
        View rootView = inflater.inflate(R.layout.fragment_color_picker, container, false);

        MainActivity mMainActivity = (MainActivity) getActivity();
        mViewPager = mMainActivity.mViewPager;

        mImageWheel = (ImageView) rootView.findViewById(R.id.imageView1);
        mImageWheel.setOnTouchListener(this);
        mImageSelected = (CircleView) rootView.findViewById(R.id.imageColorSelected);
        mImageSelected.setFillColor(Color.WHITE);
        mLayoutProgressBar = (RelativeLayout) rootView.findViewById(R.id.layout_progress_bar);
        mToggleButton = (ToggleButton) rootView.findViewById(R.id.toggleButton);

        mToggleButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        boolean bTouchEventHandled = false;

        if((event.getAction() == MotionEvent.ACTION_DOWN) || (event.getAction() == MotionEvent.ACTION_MOVE))
        {
            //prevent the ViewPager from receiving these onTouch events or the screen will still switch tabs!!
            mViewPager.requestDisallowInterceptTouchEvent(true);

            if (v.getId() == mImageWheel.getId())
            {
                //get the x and y where imageview was touched
                Matrix inverse = new Matrix();
                mImageWheel.getImageMatrix().invert(inverse);
                float[] touchPoint = new float[] {event.getX(), event.getY()};
                inverse.mapPoints(touchPoint);
                int x = Integer.valueOf((int)touchPoint[0]);
                int y = Integer.valueOf((int)touchPoint[1]);

                //x and y must be within dimensions of image
                Bitmap bitmap = ((BitmapDrawable)mImageWheel.getDrawable()).getBitmap();
                if ((y < bitmap.getHeight()) && y >= 0)
                {
                    if ((x < bitmap.getWidth()) && (x >= 0))
                    {
                        // if toggle button is enabled, echo selected color to screen and device
                        if(mBmdApplication.getBMD200EvalDemoDevice() != null) {

                            // get chosen color
                            int selectedColor = bitmap.getPixel(x, y);

                            // ignore the transparent corners
                            if (selectedColor != 0) {

                                bTouchEventHandled = true;

                                Log.d("TouchEvent", "Touch event at " + x + ", " + y + ", color= #" + Integer.toHexString(selectedColor));

                                // strip transparency from the selected color before showing it in the selection circle
                                int newcolor = Color.rgb(Color.red(selectedColor), Color.green(selectedColor), Color.blue(selectedColor));
                                mImageSelected.setFillColor(newcolor);

                                // send the chosen color to the device
                                RgbColor rgbcolor = new RgbColor(
                                        Color.red(selectedColor),
                                        Color.green(selectedColor),
                                        Color.blue(selectedColor));



                                mBmdApplication.getBMD200EvalDemoDevice().setLedColor(rgbcolor);//send selected color to device

                                if(!mToggleButton.isChecked()) {
                                    mToggleButton.setChecked(true);
                                }
                            }
                        }
                    }
                }
            }
        }
        else if (event.getAction() == MotionEvent.ACTION_CANCEL)
        {
            mViewPager.requestDisallowInterceptTouchEvent(false);
        }

        return bTouchEventHandled;
    }

    // ************
    //  Concrete Implementation of BmdApplication.ConnectionNotification
    // ************
    @Override
    public void isNowConnected(BmdEvalDemoDevice device) {

        // hide the SEARCHING UI
        mLayoutProgressBar.post(new Runnable() {
            @Override
            public void run() {
                mLayoutProgressBar.setVisibility(View.GONE);
            }
        });

        // if the Blinky Demo fw is programmed, or the BMDware fw, show "UPDATE" fragment
        final String fwname = device.getBaseDevice().getName();
        if ((fwname.contains(FirmwareUpdateFragment.BLINKY_DEMO_NAME_SUBSET)) ||
                (fwname.contains(FirmwareUpdateFragment.BMDWARE_NAME_SUBSET)))
        {
            ((MainActivity)getActivity()).mViewPager.post(new Runnable() {
                @Override
                public void run() {
                    ((MainActivity) getActivity()).mViewPager.setCurrentItem(2);
                }
            });
        }
    }

    @Override
    public void isNowDisconnected() {
        // show the SEARCHING UI
        if(this.getUserVisibleHint() == true) {
            mLayoutProgressBar.post(new Runnable() {
                @Override
                public void run() {
                    mLayoutProgressBar.setVisibility(View.VISIBLE);
                }
            });
            mBmdApplication.searchForDemoDevices();
        }

    }

    // ************
    //  Concrete Implementation of View.OnClickListener
    // ************
    @Override
    public void onClick(View v) {
        if (v == mToggleButton) {
            if(mToggleButton.isChecked()) {

                // if there is no connection to device, immediately deselect the device
                if (mBmdApplication.getBMD200EvalDemoDevice() == null)
                {
                    mToggleButton.setChecked(false);
                }

                int selectedColor = mImageSelected.getFillColor();
                // send the chosen color to the device
                RgbColor rgbcolor = new RgbColor(
                        Color.red(selectedColor),
                        Color.green(selectedColor),
                        Color.blue(selectedColor));
                mBmdApplication.getBMD200EvalDemoDevice().setLedColor(rgbcolor);//send selected color to device
            } else {
                if (mBmdApplication.getBMD200EvalDemoDevice() == null) {
                    return;
                }

                RgbColor rgbcolor = new RgbColor(0, 0, 0);
                mBmdApplication.getBMD200EvalDemoDevice().setLedColor(rgbcolor);
            }

        }
    }

    // ************
    //  Concrete Implementation of InterfaceFragmentLifecycle
    // ************
    @Override
    public void onPauseFragment() {
        mBmdApplication.setConnectionNotificationListener(null);
    }

    @Override
    public void onResumeFragment() {

        // callback so we know when it's connected / disconnected
        mBmdApplication.setConnectionNotificationListener(this);

        if (mBmdApplication.isConnected())
        {
            // nothing to configure
            mLayoutProgressBar.setVisibility(View.INVISIBLE);
        }
        else if (mBmdApplication.isSearching() == false)
        {
            // if device is not connected, and not searching, let's search !
            mBmdApplication.searchForDemoDevices();
            mLayoutProgressBar.setVisibility(View.VISIBLE);
        }
        else if (mBmdApplication.isSearching() == true)
        {
            // if device is still searching, simply show searching animation
            mLayoutProgressBar.setVisibility(View.VISIBLE);
        }
    }
}
