package com.rigado.bmdeval.fragments;

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

import com.rigado.bmdeval.R;
import com.rigado.bmdeval.contracts.ColorPickerContract;
import com.rigado.bmdeval.customviews.CircleView;
import com.rigado.bmdeval.devicedata.evaldemodevice.RgbColor;
import com.rigado.bmdeval.interfaces.IFragmentLifecycleListener;
import com.rigado.bmdeval.presenters.ColorPickerPresenter;

public class ColorPickerFragment extends Fragment implements
        OnTouchListener,
        ColorPickerContract.View,
        IFragmentLifecycleListener {

    private final String TAG = getClass().getSimpleName();

    private ImageView mImageWheel;
    private CircleView mImageSelected;
    private ToggleButton mToggleButton;

    private ColorPickerPresenter colorPickerPresenter;

    public ColorPickerFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate the necessary layout
        View rootView = inflater.inflate(R.layout.fragment_color_picker, container, false);

        mImageWheel = (ImageView) rootView.findViewById(R.id.imageView1);
        mImageWheel.setOnTouchListener(this);
        mImageSelected = (CircleView) rootView.findViewById(R.id.imageColorSelected);
        mImageSelected.setFillColor(Color.WHITE);
        mToggleButton = (ToggleButton) rootView.findViewById(R.id.toggleButton);

        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "toggle button enabled " + mToggleButton.isChecked());
                colorPickerPresenter.setLedEnabled(mToggleButton.isChecked());
                //TODO : If off & turning on, getSelectedColor & set LED
            }
        });

        colorPickerPresenter = new ColorPickerPresenter(this);

        return rootView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                //TODO : Prevent viewpager touch events
                return maybeSetLedColor(v, event);
            case MotionEvent.ACTION_CANCEL:
                //TODO: Allow viewpager touch events
                return false;
            default:
                return false;
        }
    }

    private boolean maybeSetLedColor(View view, MotionEvent event) {
        if (view == mImageWheel && mImageWheel.isEnabled()) {
            //get the x and y where imageview was touched
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

    @Override
    public void updateStatusButton(boolean enabled) {

    }

    private RgbColor getSelectedColor() {
        int selectedColor = mImageSelected.getFillColor();
        RgbColor rgbcolor = new RgbColor(
                Color.red(selectedColor),
                Color.green(selectedColor),
                Color.blue(selectedColor));
        return rgbcolor;
    }
}
