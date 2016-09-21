package com.rigado.bmd200eval.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.rigado.bmd200eval.BmdApplication;
import com.rigado.bmd200eval.activities.MainActivity;
import com.rigado.bmd200eval.R;
import com.rigado.bmd200eval.demodevice.AccelData;
import com.rigado.bmd200eval.demodevice.AmbientLight;
import com.rigado.bmd200eval.demodevice.IBmdEvalDemoDeviceListener;
import com.rigado.bmd200eval.demodevice.BmdEvalDemoDevice;
import com.rigado.bmd200eval.demodevice.ButtonStatus;
import com.rigado.bmd200eval.demodevice.RgbColor;
import com.rigado.bmd200eval.interfaces.InterfaceFragmentLifecycle;

public class DemoFragment extends Fragment implements
        IBmdEvalDemoDeviceListener,
        BmdApplication.ConnectionNotification,
        InterfaceFragmentLifecycle {

    private static final int MAX_ARRAY_SIZE = 30;
    private static final String TAG = DemoFragment.class.getSimpleName();

    private BmdApplication mBmdApplication;

    private LineGraphSeries<DataPoint> mSeriesX;
    private LineGraphSeries<DataPoint> mSeriesY;
    private LineGraphSeries<DataPoint> mSeriesZ;
    private DataPoint[] dparrayX = new DataPoint[MAX_ARRAY_SIZE];
    private DataPoint[] dparrayY = new DataPoint[MAX_ARRAY_SIZE];
    private DataPoint[] dparrayZ = new DataPoint[MAX_ARRAY_SIZE];
    private int mDataIndex;//used to plot against horizontal axis
    private boolean mIsConfigured;

    private GraphView mGraph;
    private TextView mTextViewUser1;
    private TextView mTextViewUser2;
    private RelativeLayout mLayoutProgressBar;
    private View mViewAmbientLight;
    private TextView mTextViewAmbientLight;

    //Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
    public DemoFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mBmdApplication = (BmdApplication) getActivity().getApplication();

        //inflate the necessary layout
        View rootView = inflater.inflate(R.layout.fragment_demo, container, false);

        //get references to all of the views
        mGraph = (GraphView) rootView.findViewById(R.id.graph);
        mTextViewUser1 = (TextView) rootView.findViewById(R.id.tv_user1);
        mTextViewUser2 = (TextView) rootView.findViewById(R.id.tv_user2);
        mLayoutProgressBar = (RelativeLayout) rootView.findViewById(R.id.layout_progress_bar);
        mViewAmbientLight = rootView.findViewById(R.id.view_ambient);
        mTextViewAmbientLight = (TextView) rootView.findViewById(R.id.textview_ambient_millivolt);

        // set User1/2 images to be partially see-through, indicating "not pressed"
        mTextViewUser1.setAlpha(0.5f);
        mTextViewUser2.setAlpha(0.5f);

        // set graph title
        mGraph.setTitle("Accelerometer Data");

        // hide the x-axis labels
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(mGraph);
        staticLabelsFormatter.setHorizontalLabels(new String[]{" ", " "});
        mGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        // set Y bounds
        mGraph.getViewport().setYAxisBoundsManual(true);
        mGraph.getViewport().setMinY(-1.5f);
        mGraph.getViewport().setMaxY(1.5f);

        // set X bounds
        mGraph.getViewport().setXAxisBoundsManual(true);
        mGraph.getViewport().setMinX(0);
        mGraph.getViewport().setMaxX(MAX_ARRAY_SIZE);

        // initialize data
        initializeDataArrays();
        mSeriesX = new LineGraphSeries<DataPoint>();
        mSeriesY = new LineGraphSeries<DataPoint>();
        mSeriesZ = new LineGraphSeries<DataPoint>();
        mSeriesX.setColor(Color.RED);
        mSeriesY.setColor(Color.BLUE);
        mSeriesZ.setColor(Color.GREEN);
        mGraph.addSeries(mSeriesX);
        mGraph.addSeries(mSeriesY);
        mGraph.addSeries(mSeriesZ);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        onResumeFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    // ************
    //  Concrete Implementation of IBmdEvalDemoDeviceListener
    // ************
    @Override
    public void didUpdateButtonData(final ButtonStatus status) {

        mTextViewUser1.post(new Runnable() {
            @Override
            public void run() {
                if (status.isUser1Pressed()) {
                    // show "USER1 PRESSED" by darkening image
                    //mImageViewUser1.setColorFilter(Color.BLUE, PorterDuff.Mode.DARKEN);
                    mTextViewUser1.setAlpha(1.0f);//not see-through
                } else {
                    // show "USER1 IDLE"
                    //mImageViewUser1.setColorFilter(null);//null to remove the existing color filter
                    mTextViewUser1.setAlpha(0.5f);//partially see-through
                }

                if (status.isUser2Pressed()) {
                    // show "USER2 PRESSED"
                    //mImageViewUser2.setColorFilter(Color.BLUE, PorterDuff.Mode.DARKEN);
                    mTextViewUser2.setAlpha(1.0f);//not see-through
                } else {
                    // show "USER2 IDLE"
                    //mImageViewUser2.setColorFilter(null);//null to remove the existing color filter
                    mTextViewUser2.setAlpha(0.5f);//partially see-through
                }
            }
        });

    }

    @Override
    public void didUpdateLedColor(RgbColor color) {
        // not used
    }

    @Override
    public void didUpdateAmbientLightData(final AmbientLight light) {
        Log.d(TAG, "didUpdateAmbientLightData alpha = " + light.getAlphaLevel());

        mViewAmbientLight.post(new Runnable() {
            @Override
            public void run() {
                mViewAmbientLight.setAlpha(light.getAlphaLevel());
                mTextViewAmbientLight.setText(light.getLevel() + " mV");
            }
        });

    }

    @Override
    public void didUpdateAccelData(final AccelData data) {

        final int iArraySizeMinusOne = MAX_ARRAY_SIZE -1;//optimization

        //NOTE: at this point mDataIndex should always = MAX_ARRAY_SIZE -1
        for(int i=0; i<iArraySizeMinusOne; i++)
        {
            // there's no way around the data shuffle because resetData() expects an array with values in sequence
            // NOTE: DataPoint class does *not* have setters, creating new objects is the only way to set data
            dparrayX[i] = new DataPoint(i, dparrayX[i+1].getY());
            dparrayY[i] = new DataPoint(i, dparrayY[i+1].getY());
            dparrayZ[i] = new DataPoint(i, dparrayZ[i+1].getY());
        }

        // store new data
        final DataPoint dpX = new DataPoint(mDataIndex, data.getX());
        final DataPoint dpY = new DataPoint(mDataIndex, data.getY());
        final DataPoint dpZ = new DataPoint(mDataIndex, data.getZ());
        dparrayX[iArraySizeMinusOne] = dpX;
        dparrayY[iArraySizeMinusOne] = dpY;
        dparrayZ[iArraySizeMinusOne] = dpZ;

        // update graphs (this is done on the UI thread)
        mGraph.post(new Runnable() {
            @Override
            public void run() {

                // redraw data
                mSeriesX.resetData(dparrayX);
                mSeriesY.resetData(dparrayY);
                mSeriesZ.resetData(dparrayZ);
            }
        });
    }

    private void initializeDataArrays()
    {
        mDataIndex = 0;

        for(int i=0; i<MAX_ARRAY_SIZE; i++)
        {
            final DataPoint dpX = new DataPoint(mDataIndex, 0.0f);
            final DataPoint dpY = new DataPoint(mDataIndex, 0.0f);
            final DataPoint dpZ = new DataPoint(mDataIndex, 0.0f);

            dparrayX[i] = dpX;
            dparrayY[i] = dpY;
            dparrayZ[i] = dpZ;

            mDataIndex++;// this is important
        }
    }

    private void configureDevice() {
        mBmdApplication.getBMD200EvalDemoDevice().setObserver(this);

        //Uncomment to enable accel streaming
        mBmdApplication.getBMD200EvalDemoDevice().startAccelerometerStream();

        //Uncomment to enable ambient light data sensing
        mBmdApplication.getBMD200EvalDemoDevice().startAmbientLightSensing();

        mIsConfigured = true;
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
        // otherwise, set up device for data streaming
        else {
            configureDevice();
        }
    }

    @Override
    public void isNowDisconnected() {

        // show the SEARCHING UI - note: BmdApplication.didDisconnectDevice() will have started searching already
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
    //  Concrete Implementation of InterfaceFragmentLifecycle
    // ************
    @Override
    public void onPauseFragment() {

        mBmdApplication.setConnectionNotificationListener(null);
        mLayoutProgressBar.setVisibility(View.GONE);
        if (mIsConfigured) {
            mIsConfigured = false;
            mBmdApplication.getBMD200EvalDemoDevice().setObserver(null);
            mBmdApplication.getBMD200EvalDemoDevice().stopAccelerometerStream();
            mBmdApplication.getBMD200EvalDemoDevice().stopAmbientLightSensing();
        }
    }


    @Override
    public void onResumeFragment() {
        Log.i(TAG, "onResumeFragment");
        // callback so we know when it's connected / disconnected
        mBmdApplication.setConnectionNotificationListener(this);

        if (mBmdApplication.isConnected()) {
            Log.i(TAG, "isConnected");
            // if the device is already connected, configure to stream data
            configureDevice();
            mLayoutProgressBar.setVisibility(View.INVISIBLE);

        } else if (!mBmdApplication.isSearching()) {
            Log.i(TAG, "!isSearching - begin search");
            // if device is not connected, and not searching, let's search !
            mBmdApplication.searchForDemoDevices();
            mLayoutProgressBar.setVisibility(View.VISIBLE);

        } else {
            Log.i(TAG, "isSearching already");
            // if device is still searching, simply show searching animation
            mLayoutProgressBar.setVisibility(View.VISIBLE);
        }
    }
}
