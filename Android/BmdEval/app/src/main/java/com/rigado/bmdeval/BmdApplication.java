package com.rigado.bmdeval;

import android.app.Application;
import android.util.Log;

import com.rigado.bmdeval.demodevice.BmdEvalManager;
import com.rigado.bmdeval.demodevice.IBmdEvalManagerListener;
import com.rigado.bmdeval.demodevice.BmdEvalDemoDevice;
import com.rigado.rigablue.RigCoreBluetooth;

/**
 * This class handles the long-term BmdEvalDemoDevice state since it's always available for the duration of the app
 * Fragments can query the state here and show whatever is necessary on the UI
 */
public class BmdApplication extends Application implements
        IBmdEvalManagerListener{
    // Constants
    final private String TAG = getClass().getSimpleName();

    private static BmdApplication sSingleton;

    // Member Variables
    private BmdEvalManager mBmdEvalManager;
    private BmdEvalDemoDevice mBmdEvalDemoDevice;//provides an interface to all of the functionality of the demo firmware
    private boolean mSearchingForDemoDevice;
    private IConnectionListener mIConnectionListener;

    public static BmdApplication getInstance() {
        return sSingleton;
    }

    // interface for fragments to use to be notified of a connection
    public interface IConnectionListener
    {
        void isNowConnected(BmdEvalDemoDevice device);
        void isNowDisconnected();
    }

    // once this callback is triggered, we have a context
    @Override
    public void onCreate()
    {
        super.onCreate();

        // Required initialization
        RigCoreBluetooth.initialize(this);

        sSingleton = this;

        mBmdEvalManager = BmdEvalManager.getInstance();
        mBmdEvalManager.registerBmdManagerListener(this);

    }

    /**
     * searchForDemoDevices() can result in a connection
     * and trigger IBmdEvalManagerListener.didConnectDevice()
     */
    public void searchForDemoDevices()
    {
        Log.d(TAG, "searchForDemoDevices");
        mSearchingForDemoDevice = mBmdEvalManager.maybeBeginScanning();
    }


    public BmdEvalManager getBmdManager() {
        return mBmdEvalManager;
    }

    public boolean isConnected()
    {
        return mBmdEvalManager.isConnected();
    }

    public boolean isSearching() {
        return mSearchingForDemoDevice;
    }

    public void setSearching(boolean isSearching) {
        this.mSearchingForDemoDevice = isSearching;
    }


    public void disconnectDevice()
    {
        mBmdEvalManager.disconnectDevice();
    }

    public BmdEvalDemoDevice getBMD200EvalDemoDevice()
    {
        return mBmdEvalDemoDevice;
    }

    public void setConnectionNotificationListener(IConnectionListener listener)
    {
        mIConnectionListener = listener;
    }


    // ************
    //  Concrete Implementation of IBmdEvalManagerListener
    // ************
    public void didConnectDevice(BmdEvalDemoDevice device)
    {
        mBmdEvalDemoDevice = device;
        mSearchingForDemoDevice = false;

        Log.d(TAG, "didConnectDevice "+device.getBaseDevice().getName());

        // alert a fragment that might be listening for this event
        if (mIConnectionListener != null)
        {
            Log.i(TAG, "isNowConnected");
            mIConnectionListener.isNowConnected(device);
        } else {
            Log.i(TAG, "Connection listener was null??");
        }
    }

    public void didDisconnectDevice()
    {
        mBmdEvalDemoDevice = null;
        mSearchingForDemoDevice = false;

        Log.d(TAG, "didDisconnectDevice");
        //searchForDemoDevices();

        if(mIConnectionListener != null) {
            mIConnectionListener.isNowDisconnected();
        }
    }

    public void bluetoothNotSupported()
    {
        mSearchingForDemoDevice = false;
        Log.d(TAG, "bluetoothNotSupported");
    }

    public void bluetoothPowerStateChanged(boolean enabled)
    {
        mSearchingForDemoDevice = false;
    }
}
