package com.rigado.bmd200eval;

import android.app.Application;
import android.util.Log;

import com.rigado.bmd200eval.demodevice.BMD200EvalDemoDevice;
import com.rigado.bmd200eval.demodevice.BMD200EvalManager;
import com.rigado.bmd200eval.demodevice.BMD200EvalManagerObserver;

/**
 * This class handles the long-term BMD200EvalDemoDevice state since it's always available for the duration of the app
 * Fragments can query the state here and show whatever is necessary on the UI
 */
public class ApplicationMain extends Application implements BMD200EvalManagerObserver
{
    // Constants
    final private String TAG = getClass().getSimpleName();

    // Member Variables
    private BMD200EvalManager mBMD200EvalManager;
    private BMD200EvalDemoDevice mBMD200EvalDemoDevice;//provides an interface to all of the functionality of the demo firmware
    private boolean mSearchingForDemoDevice;
    private ConnectionNotification mConnectionNotification;

    // interface for fragments to use to be notified of a connection
    public interface ConnectionNotification
    {
        void isNowConnected(BMD200EvalDemoDevice device);
        void isNowDisconnected();
    }

    // once this callback is triggered, we have a context
    @Override
    public void onCreate()
    {
        super.onCreate();

        mBMD200EvalManager = BMD200EvalManager.getInstance();
        mBMD200EvalManager.setContext(this);
        mBMD200EvalManager.setObserver(this);

    }

    /**
     * searchForDemoDevices() can result in a connection
     * and trigger BMD200EvalManagerObserver.didConnectDevice()
     */
    public void searchForDemoDevices()
    {
        Log.d(TAG, "searchForDemoDevices");
        mBMD200EvalManager.searchForDemoDevices();
        mSearchingForDemoDevice = true;
    }

    public boolean isConnected()
    {
        return mBMD200EvalManager.isConnected();
    }

    public boolean isSearching()
    {
        return mSearchingForDemoDevice;
    }

    public void disconnectDevice()
    {
        mBMD200EvalManager.disconnectDevice();
    }

    public BMD200EvalDemoDevice getBMD200EvalDemoDevice()
    {
        return mBMD200EvalDemoDevice;
    }

    public void setConnectionNotificationListener(ConnectionNotification listener)
    {
        mConnectionNotification = listener;
    }


    // ************
    //  Concrete Implementation of BMD200EvalManagerObserver
    // ************
    public void didConnectDevice(BMD200EvalDemoDevice device)
    {
        mBMD200EvalDemoDevice = device;
        mSearchingForDemoDevice = false;

        Log.d(TAG, "didConnectDevice "+device.getBaseDevice().getName());

        // alert a fragment that might be listening for this event
        if (mConnectionNotification != null)
        {
            mConnectionNotification.isNowConnected(device);
        }
    }

    public void didDisconnectDevice()
    {
        mBMD200EvalDemoDevice = null;
        mSearchingForDemoDevice = false;

        Log.d(TAG, "didDisconnectDevice");
        //searchForDemoDevices();

        if(mConnectionNotification != null) {
            mConnectionNotification.isNowDisconnected();
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
