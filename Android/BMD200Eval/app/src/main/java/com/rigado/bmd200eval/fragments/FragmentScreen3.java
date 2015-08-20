package com.rigado.bmd200eval.fragments;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rigado.bmd200eval.ActivityMain;
import com.rigado.bmd200eval.ApplicationMain;
import com.rigado.bmd200eval.R;
import com.rigado.bmd200eval.demodevice.BMD200EvalBootloaderInfo;
import com.rigado.bmd200eval.demodevice.BMD200EvalDemoDevice;
import com.rigado.bmd200eval.interfaces.InterfaceFragmentLifecycle;
import com.rigado.bmd200eval.utilities.JsonFirmwareReader;
import com.rigado.bmd200eval.utilities.JsonFirmwareType;
import com.rigado.bmd200eval.utilities.Utilities;
import com.rigado.rigablue.IRigFirmwareUpdateManagerObserver;
import com.rigado.rigablue.RigFirmwareUpdateManager;
import com.rigado.rigablue.RigLeBaseDevice;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class FragmentScreen3 extends Fragment implements InterfaceFragmentLifecycle, View.OnClickListener, ApplicationMain.ConnectionNotification, IRigFirmwareUpdateManagerObserver {

    // Constants
    private final String BMD_EVAL_DEMO_NAME_SUBSET = "BMD200Eval";// string contained within the demo firmware name
    public static final String BLINKY_DEMO_NAME_SUBSET = "Blinky";// string contained within the Blinky firmware name
    public static final String BMDWARE_NAME_SUBSET = "RigCom";// string contained within the BMDware firmware name

    // UI References
    private RelativeLayout mLayoutProgressBar;
    private NumberPicker mFirmwarePicker;
    private Button mButtonDeploy;
    private ProgressBar mProgressBar;
    private TextView mTextViewStatus;

    // General Member Variables
    private ApplicationMain mApplicationMain;
    private Utilities mUtilities;
    private boolean mIsUpdateInProgress;
    private JsonFirmwareReader mJsonFirmwareReader;
    private ArrayList<JsonFirmwareType> mJsonFirmwareTypeList;
    private RigFirmwareUpdateManager mRigFirmwareUpdateManager;
    private int mLastProgressIndication = -1;
    private String mSelectedFirmwareName;
    private AlertDialog alertDialog0;
    private AlertDialog alertDialog1;

    //Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
    public FragmentScreen3(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mApplicationMain = (ApplicationMain) getActivity().getApplication();

        //inflate the necessary layout
        View rootView = inflater.inflate(R.layout.fragment_screen3, container, false);

        // UI references
        mFirmwarePicker = (NumberPicker) rootView.findViewById(R.id.id_picker_firmware);
        mButtonDeploy = (Button) rootView.findViewById(R.id.id_btn_begin_deploy);
        mLayoutProgressBar = (RelativeLayout) rootView.findViewById(R.id.layout_progress_bar);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.id_progress_deployment);
        mTextViewStatus = (TextView) rootView.findViewById(R.id.id_tv_status);

        mButtonDeploy.setEnabled(false);// disabled until we know it's connected
        mButtonDeploy.setOnClickListener(this);
        mIsUpdateInProgress = false;

        // read list of available firmwares - these are listed in res/raw/firmware_descriptions.json
        /* TODO: Update raw folder to contain your binary and update firmware_descriptions.json to have
           the necessary information regarding the update binary.  Note: Update binaries must be
           binary files generated using the genimage.py Python script.  See Getting Started with the
           Rigado Secure Bootlaoder for more details.
         */
        mJsonFirmwareReader = new JsonFirmwareReader();
        mJsonFirmwareTypeList = mJsonFirmwareReader.getFirmwareList(getActivity());

        // show firmware in Picker
        final String[] arrayFirmwareNames = new String[mJsonFirmwareTypeList.size()];
        for(int index=0; index<mJsonFirmwareTypeList.size(); index++) {
            final JsonFirmwareType firmwareRecord = mJsonFirmwareTypeList.get(index);
            arrayFirmwareNames[index] = firmwareRecord.getFwname() + " v" + firmwareRecord.getProperties().getVersion();
        }
        mFirmwarePicker.setMinValue(0);
        mFirmwarePicker.setMaxValue(mJsonFirmwareTypeList.size() - 1);
        mFirmwarePicker.setDisplayedValues(arrayFirmwareNames);
        mFirmwarePicker.post(new Runnable() {
            @Override
            public void run() {
                if (mJsonFirmwareTypeList.size() > 1) {
                    mFirmwarePicker.setValue(1);// auto select the 2nd item in the list to make the UI more identifiable to the user
                }
            }
        });
        setNumberPickerTxtColor(mFirmwarePicker, Color.WHITE);

        // initialization
        mUtilities = new Utilities();

        return rootView;
    }

    // ************
    //  Concrete Implementation of InterfaceFragmentLifecycle
    // ************
    @Override
    public void onPauseFragment() {
        mApplicationMain.setConnectionNotificationListener(null);
    }

    @Override
    public void onResumeFragment() {

        // callback so we know when it's connected / disconnected
        mApplicationMain.setConnectionNotificationListener(this);

        if (mApplicationMain.isConnected())
        {
            // if the device is already connected, enable Deploy button
            mButtonDeploy.setEnabled(true);
            checkFirmwareInstalled(mApplicationMain.getBMD200EvalDemoDevice());
            mLayoutProgressBar.setVisibility(View.INVISIBLE);
        }
        else if (mApplicationMain.isSearching() == false)
        {
            // if device is not connected, and not searching, let's search !
            mApplicationMain.searchForDemoDevices();
            mLayoutProgressBar.setVisibility(View.VISIBLE);
        }
        else if (mApplicationMain.isSearching() == true)
        {
            // if device is still searching, simply show searching animation
            mLayoutProgressBar.setVisibility(View.VISIBLE);
        }
    }

    // ************
    //  Concrete Implementation of View.OnClickListener
    //  this code only runs when the button is enabled - which should only happen when device is connected
    // ************
    @Override
    public void onClick(View v) {

        // get selected firmware from picker
        final int selectedIndex = mFirmwarePicker.getValue();
        final JsonFirmwareType firmwareRecord = mJsonFirmwareTypeList.get(selectedIndex);

        // save the name for later, see didFinishUpdate()
        mSelectedFirmwareName = firmwareRecord.getFwname();

        programFirmware(firmwareRecord);
    }

    private void programFirmware(JsonFirmwareType firmwareRecord) {
        mButtonDeploy.setEnabled(false);
        ((ActivityMain) getActivity()).setAllowTabsAndViewpagerSwitching(false);

        powerKeepScreenOn();

        BMD200EvalBootloaderInfo bmd200EvalBootloaderInfo = mApplicationMain.getBMD200EvalDemoDevice().getBootloaderInfo();

        BluetoothGattCharacteristic resetChar = bmd200EvalBootloaderInfo.getBootloaderCharacteristic();
        RigLeBaseDevice rigLeBaseDevice = mApplicationMain.getBMD200EvalDemoDevice().getBaseDevice();

        // initialize FW Manager
        mRigFirmwareUpdateManager = new RigFirmwareUpdateManager();
        mRigFirmwareUpdateManager.setObserver(this);

        mUtilities.startFirmwareUpdate(getActivity(), mRigFirmwareUpdateManager, rigLeBaseDevice, firmwareRecord, resetChar, bmd200EvalBootloaderInfo.getBootloaderCommand());
        mIsUpdateInProgress = true;
    }

    // ************
    //  Concrete Implementation of ApplicationMain.ConnectionNotification
    // ************
    @Override
    public void isNowConnected(BMD200EvalDemoDevice device) {

        if (mIsUpdateInProgress == false) {

            // hide the SEARCHING UI
            mLayoutProgressBar.post(new Runnable() {
                @Override
                public void run() {
                    mButtonDeploy.setEnabled(true);
                    mLayoutProgressBar.setVisibility(View.GONE);
                }
            });

            checkFirmwareInstalled(device);
        }
    }

    @Override
    public void isNowDisconnected() {

        // show the SEARCHING UI if not currently programming
        if (mIsUpdateInProgress == false) {
            mLayoutProgressBar.post(new Runnable() {
                @Override
                public void run() {
                    mButtonDeploy.setEnabled(false);
                    //mLayoutProgressBar.setVisibility(View.VISIBLE);
                    //mApplicationMain.searchForDemoDevices();
                }
            });
        }
    }

    // ************
    //  Concrete Implementation of IRigFirmwareUpdateManagerObserver
    // ************
    @Override
    public void updateProgress(final int progress) {
        //only update progress if there really was visible progress
        if (mLastProgressIndication != progress) {
            mLastProgressIndication = progress;

            // UI widgets must be updated from UI thread
            mProgressBar.post(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setProgress(progress);
                }
            });
        }
    }

    @Override
    public void updateStatus(String status, int error) {
        showStatus(status);
    }

    @Override
    public void didFinishUpdate() {

        mButtonDeploy.post(new Runnable() {
            @Override
            public void run() {
                mButtonDeploy.setEnabled(true);
                powerScreenNormal();
                ((ActivityMain) getActivity()).setAllowTabsAndViewpagerSwitching(true);
            }
        });

        // reset the UI and state for next firmware programming
        mLastProgressIndication = -1;
        mIsUpdateInProgress = false;
        showStatus("Idle");
        mProgressBar.post(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setProgress(mLastProgressIndication);
            }
        });

        mApplicationMain.disconnectDevice(); //Note: this will cause the searching to begin fresh

        // if the Main Demo Firmware was programmed just now, switch to Fragment 1
        if (mSelectedFirmwareName.contains(BMD_EVAL_DEMO_NAME_SUBSET)) {

            ((ActivityMain)getActivity()).mViewPager.post(new Runnable() {
                @Override
                public void run() {
                    ((ActivityMain) getActivity()).mViewPager.setCurrentItem(0);
                }
            });

        }
    }

    /**
     * Utility function: update the text on the status field (R.id.id_tv_status)
     * @param status
     */
    private void showStatus(final String status) {
        // UI widgets must be updated from UI thread
        mTextViewStatus.post(new Runnable() {
            @Override
            public void run() {
                mTextViewStatus.setText(status);
            }
        });
    }

    // Utility function: Set window to keep screen on. Benefit: does not require a permission
    // NOTE: call from UI thread
    protected void powerKeepScreenOn() {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Utility function: Set window to resume normal power operation
    // NOTE: call from UI thread
    protected void powerScreenNormal() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Utility function to change the text color.
     * Needed because android:textColor="@android:color/white" in XML has no effect !
     * @param numberPicker view object
     * @param color the desired color
     * @return true if successfully changed the color
     */
    public boolean setNumberPickerTxtColor(NumberPicker numberPicker, int color)
    {
        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText){
                try{
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                }
                catch(NoSuchFieldException e){
                    Log.w("setNumberPickerTxtColor", e);
                }
                catch(IllegalAccessException e){
                    Log.w("setNumberPickerTxtColor", e);
                }
                catch(IllegalArgumentException e){
                    Log.w("setNumberPickerTxtColor", e);
                }
            }
        }
        return false;
    }

    /**
     * Utility function to check currently installed fw and show dialog if necessary
     * Requires connected device
     */
    private void checkFirmwareInstalled(BMD200EvalDemoDevice device)
    {
        // if the Blinky Demo fw is programmed, show message to the user
        if (device.getBaseDevice().getName().contains(BLINKY_DEMO_NAME_SUBSET))
        {
            showFirmwareUpdateDialog(R.string.title_blinky_dialog, R.string.message_blinky);
        }
        // if the BMD Eval fw is programmed, show message to the user
        else if (device.getBaseDevice().getName().contains(BMDWARE_NAME_SUBSET))
        {
            showFirmwareUpdateDialog(R.string.title_bmdware, R.string.message_bmdware);
        }
    }

    /**
     * Utility function to show dialog
     */
    private void showFirmwareUpdateDialog(int idTitle, int idMessage)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(idTitle);
        alertDialogBuilder.setMessage(idMessage);
        alertDialogBuilder.setPositiveButton(R.string.txt_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // YES BUTTON

                // find the main demo firmware in the available list
                int index;
                for(index=0; index<mJsonFirmwareTypeList.size(); index++) {
                    final JsonFirmwareType firmwareRecord = mJsonFirmwareTypeList.get(index);

                    String strname = firmwareRecord.getFwname();

                    // check whether it's the end of the list and we still haven't found it
                    if ((index == mJsonFirmwareTypeList.size()-1) && (!strname.contains(BMD_EVAL_DEMO_NAME_SUBSET)) )
                    {
                        index = -1;//impossible value indicates error
                        break;
                    }

                    // found a match in the list - take the index and program the fw
                    if (strname.contains(BMD_EVAL_DEMO_NAME_SUBSET))
                    {
                        break;
                    }
                }

                if (index == -1)
                {
                    // error
                    dialog.cancel();
                    alertDialog0 = null;
                    showFirmwareErrorDialog("Error", "Main Demo firmware does not exist");
                }
                else
                {
                    final JsonFirmwareType firmwareRecord = mJsonFirmwareTypeList.get(index);
                    mSelectedFirmwareName = firmwareRecord.getFwname();

                    // program the main demo (evaluation) firmware
                    programFirmware(firmwareRecord);
                }
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.txt_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // NO BUTTON
                dialog.cancel();
                alertDialog0 = null;
            }
        });
        alertDialog0 = alertDialogBuilder.create();
        alertDialog0.show();
    }

    private void showFirmwareErrorDialog(String strTitle, String strMessage)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(strTitle);
        alertDialogBuilder.setMessage(strMessage);
        alertDialogBuilder.setNeutralButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                alertDialog1 = null;
            }
        });
        alertDialog1 = alertDialogBuilder.create();
        alertDialog1.show();
    }

}
