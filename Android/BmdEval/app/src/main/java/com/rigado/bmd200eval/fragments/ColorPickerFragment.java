package com.rigado.bmd200eval.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.rigado.bmd200eval.R;
import com.rigado.bmd200eval.activities.MainActivity;
import com.rigado.bmd200eval.adapters.SectionsPagerAdapter;
import com.rigado.bmd200eval.contracts.ColorPickerContract;
import com.rigado.bmd200eval.customviews.CircleView;
import com.rigado.bmd200eval.customviews.ControllableViewPager;
import com.rigado.bmd200eval.demodevice.devicedata.RgbColor;
import com.rigado.bmd200eval.interfaces.IFragmentLifecycleListener;
import com.rigado.bmd200eval.presenters.ColorPickerPresenter;

public class ColorPickerFragment extends Fragment implements
        OnTouchListener,
        ColorPickerContract.View,
        IFragmentLifecycleListener {

    private final String TAG = getClass().getSimpleName();

    private ImageView mImageWheel;
    private CircleView mImageSelected;
    private ToggleButton mToggleButton;
    private ControllableViewPager mViewPager;

    private ColorPickerPresenter colorPickerPresenter;

    private boolean isConnected;

    public static ColorPickerFragment newInstance(boolean isConnected) {
        ColorPickerFragment colorPickerFragment = new ColorPickerFragment();
        Bundle args = new Bundle();
        args.putBoolean(SectionsPagerAdapter.CONNECTION_STATE, isConnected);
        colorPickerFragment.setArguments(args);
        return colorPickerFragment;
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        isConnected = getArguments()
                .getBoolean(SectionsPagerAdapter.CONNECTION_STATE, false);
    }

    //TODO : Refactor legacy code
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mViewPager = ((MainActivity) getActivity()).mViewPager;
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_color_picker, container, false);

        mImageWheel = (ImageView) rootView.findViewById(R.id.fragment_color_picker_wheel_image);
        mImageWheel.setOnTouchListener(this);
        mImageSelected = (CircleView) rootView.findViewById(R.id.fragment_color_picker_circleview);
        mImageSelected.setFillColor(Color.WHITE);
        mToggleButton = (ToggleButton) rootView.findViewById(R.id.fragment_color_picker_button);

        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RgbColor rgbColor;

                if (mToggleButton.isChecked()) {
                    final int selectedColor = mImageSelected.getFillColor();
                    rgbColor = new RgbColor(
                        Color.red(selectedColor),
                        Color.green(selectedColor),
                        Color.blue(selectedColor));
                } else {
                    //Sending RgbColor with all values set to 0 turns off the LED
                    rgbColor = new RgbColor(0, 0, 0);
                }

                colorPickerPresenter.setLedColor(rgbColor);
            }
        });

        colorPickerPresenter = new ColorPickerPresenter(this);

        if (!isConnected) {
            mToggleButton.setEnabled(false);
            mImageWheel.setEnabled(false);
            mImageSelected.setEnabled(false);
        }

        return rootView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mViewPager.requestDisallowInterceptTouchEvent(true);
                return maybeSetLedColor(v, event);
            case MotionEvent.ACTION_CANCEL:
                mViewPager.requestDisallowInterceptTouchEvent(false);
                return false;
            default:
                return false;
        }
    }

    private boolean maybeSetLedColor(View view, MotionEvent event) {
        if (view == mImageWheel && mImageWheel.isEnabled()) {
            /**
             * Get positions X and Y where {@code mImageWheel} was touched
             */
            Matrix inverse = new Matrix();
            mImageWheel.getImageMatrix().invert(inverse);
            float[] touchPoint = new float[] {event.getX(), event.getY()};
            inverse.mapPoints(touchPoint);
            int x = (int) touchPoint[0];
            int y = (int) touchPoint[1];

            //x and y must be within dimensions of image
            final Bitmap bitmap = ((BitmapDrawable)mImageWheel.getDrawable()).getBitmap();
            if (y < bitmap.getHeight() && y >= 0 && x < bitmap.getWidth() && x >= 0) {
                // get chosen color
                int selectedColor = bitmap.getPixel(x, y);
                // ignore the transparent corners
                if (selectedColor != 0) {
                    Log.d("TouchEvent",
                            "Touch event at " + x + ", " + y + ", color= #"
                                    + Integer.toHexString(selectedColor));

                    // strip transparency from the selected color before
                    // showing it in the selection circle
                    int newcolor = Color.rgb(
                            Color.red(selectedColor),
                            Color.green(selectedColor),
                            Color.blue(selectedColor));
                    mImageSelected.setFillColor(newcolor);

                    // send the chosen color to the device
                    RgbColor rgbcolor = new RgbColor(
                            Color.red(selectedColor),
                            Color.green(selectedColor),
                            Color.blue(selectedColor));

                    colorPickerPresenter.setLedColor(rgbcolor);

                    if (!mToggleButton.isChecked()) {
                        mToggleButton.setChecked(true);
                    }

                    return true;
                }
            }

        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        onResumeFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
        onPauseFragment();
    }

    @Override
    public void onResumeFragment() {
        colorPickerPresenter.onResume();
    }

    @Override
    public void onPauseFragment() {
        colorPickerPresenter.onPause();
    }
}
