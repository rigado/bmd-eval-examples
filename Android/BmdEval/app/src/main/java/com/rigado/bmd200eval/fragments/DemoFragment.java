package com.rigado.bmd200eval.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.rigado.bmd200eval.R;
import com.rigado.bmd200eval.adapters.SectionsPagerAdapter;
import com.rigado.bmd200eval.contracts.DemoContract;
import com.rigado.bmd200eval.demodevice.devicedata.AccelData;
import com.rigado.bmd200eval.demodevice.devicedata.AmbientLight;
import com.rigado.bmd200eval.demodevice.devicedata.ButtonStatus;
import com.rigado.bmd200eval.interfaces.IFragmentLifecycleListener;
import com.rigado.bmd200eval.presenters.DemoPresenter;

import java.util.Locale;

public class DemoFragment extends Fragment implements
        DemoContract.View,
        IFragmentLifecycleListener {

    private static final String TAG = DemoFragment.class.getSimpleName();
    private static final int MAX_ARRAY_SIZE = 30;


    private LineGraphSeries<DataPoint> mSeriesX;
    private LineGraphSeries<DataPoint> mSeriesY;
    private LineGraphSeries<DataPoint> mSeriesZ;
    private DataPoint[] dparrayX = new DataPoint[MAX_ARRAY_SIZE];
    private DataPoint[] dparrayY = new DataPoint[MAX_ARRAY_SIZE];
    private DataPoint[] dparrayZ = new DataPoint[MAX_ARRAY_SIZE];
    private int mDataIndex;//used to plot against horizontal axis

    private GraphView mGraph;
    private TextView mTextViewUser1;
    private TextView mTextViewUser2;
    private View mViewAmbientLight;
    private TextView mTextViewAmbientLight;

    private DemoPresenter demoPresenter;

    private boolean isConnected;

    public static DemoFragment newInstance(boolean isConnected) {
        DemoFragment demoFragment = new DemoFragment();
        Bundle args = new Bundle();
        args.putBoolean(SectionsPagerAdapter.CONNECTION_STATE, isConnected);
        demoFragment.setArguments(args);
        return demoFragment;
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        isConnected = getArguments()
                .getBoolean(SectionsPagerAdapter.CONNECTION_STATE, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(TAG, "DemoFragment " + isConnected);

        //inflate the necessary layout
        View rootView = inflater.inflate(R.layout.fragment_demo, container, false);

        //get references to all of the views
        mGraph = (GraphView) rootView.findViewById(R.id.graph);
        mTextViewUser1 = (TextView) rootView.findViewById(R.id.tv_user1);
        mTextViewUser2 = (TextView) rootView.findViewById(R.id.tv_user2);
        mViewAmbientLight = rootView.findViewById(R.id.view_ambient);
        mTextViewAmbientLight = (TextView) rootView.findViewById(R.id.textview_ambient_millivolt);

        // set User1/2 images to be partially see-through, indicating "not pressed"
        mTextViewUser1.setAlpha(0.5f);
        mTextViewUser2.setAlpha(0.5f);

        final int textColorRegular = ContextCompat.getColor(getActivity(), R.color.textColorRegular);
        final int colorPrimary = ContextCompat.getColor(getActivity(), R.color.colorPrimary);
        final int colorAccent = ContextCompat.getColor(getActivity(), R.color.colorAccent);
        final int colorSecondary = ContextCompat.getColor(getActivity(), R.color.colorSecondary);

        // set graph title
        mGraph.setTitle("Accelerometer Data");
        mGraph.setTitleColor(textColorRegular);

        // hide the x-axis labels
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(mGraph);
        staticLabelsFormatter.setHorizontalLabels(new String[]{" ", " "});
        mGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        mGraph.getGridLabelRenderer().setGridColor(textColorRegular);

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
        mSeriesX.setColor(colorAccent);
        mSeriesY.setColor(colorPrimary);
        mSeriesZ.setColor(colorSecondary);
        mGraph.addSeries(mSeriesX);
        mGraph.addSeries(mSeriesY);
        mGraph.addSeries(mSeriesZ);

        demoPresenter = new DemoPresenter(this);

        return rootView;

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i(TAG, "onHiddenChanged " +hidden);
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


    private void initializeDataArrays() {
        mDataIndex = 0;

        for(int i=0; i<MAX_ARRAY_SIZE; i++) {
            final DataPoint dpX = new DataPoint(mDataIndex, 0.0f);
            final DataPoint dpY = new DataPoint(mDataIndex, 0.0f);
            final DataPoint dpZ = new DataPoint(mDataIndex, 0.0f);

            dparrayX[i] = dpX;
            dparrayY[i] = dpY;
            dparrayZ[i] = dpZ;

            mDataIndex++;// this is important
        }
    }


    @Override
    public void onPauseFragment() {
        demoPresenter.onPause();
    }

    @Override
    public void onResumeFragment() {
        demoPresenter.onResume();
    }

    @Override
    public void updateButtonStatus(final ButtonStatus buttonStatus) {
        mTextViewUser1.post(new Runnable() {
            @Override
            public void run() {
                if (buttonStatus.isUser1Pressed()) {
                    // show "USER1 PRESSED" by darkening image
                    //mImageViewUser1.setColorFilter(Color.BLUE, PorterDuff.Mode.DARKEN);
                    mTextViewUser1.setAlpha(1.0f);//not see-through
                } else {
                    // show "USER1 IDLE"
                    //mImageViewUser1.setColorFilter(null);//null to remove the existing color filter
                    mTextViewUser1.setAlpha(0.5f);//partially see-through
                }

                if (buttonStatus.isUser2Pressed()) {
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
    public void updateAccelStream(AccelData accelData) {
        final int iArraySizeMinusOne = MAX_ARRAY_SIZE -1;//optimization

        //NOTE: at this point mDataIndex should always = MAX_ARRAY_SIZE -1
        for (int i = 0 ; i < iArraySizeMinusOne; i++) {
            // there's no way around the data shuffle because resetData() expects an array with values in sequence
            // NOTE: DataPoint class does *not* have setters, creating new objects is the only way to set data
            dparrayX[i] = new DataPoint(i, dparrayX[i+1].getY());
            dparrayY[i] = new DataPoint(i, dparrayY[i+1].getY());
            dparrayZ[i] = new DataPoint(i, dparrayZ[i+1].getY());
        }

        // store new data
        final DataPoint dpX = new DataPoint(mDataIndex, accelData.getX());
        final DataPoint dpY = new DataPoint(mDataIndex, accelData.getY());
        final DataPoint dpZ = new DataPoint(mDataIndex, accelData.getZ());
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

    @Override
    public void updateAmbientLight(final AmbientLight ambientLight) {
        mViewAmbientLight.post(new Runnable() {
            @Override
            public void run() {
                mViewAmbientLight.setAlpha(ambientLight.getAlphaLevel());
                mTextViewAmbientLight.setText(
                        String.format(Locale.getDefault(),
                                "%d mV", ambientLight.getLevel()));
            }
        });
    }
}
