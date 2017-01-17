package com.rigado.bmd200eval.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rigado.bmd200eval.R;
import com.rigado.bmd200eval.activities.MainActivity;
import com.rigado.bmd200eval.adapters.SectionsPagerAdapter;
import com.rigado.bmd200eval.contracts.FirmwareContract;
import com.rigado.bmd200eval.datasource.DeviceRepository;
import com.rigado.bmd200eval.demodevice.DemoDevice;
import com.rigado.bmd200eval.presenters.FirmwarePresenter;
import com.rigado.bmd200eval.utilities.JsonFirmwareReader;
import com.rigado.bmd200eval.utilities.JsonFirmwareType;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class FirmwareUpdateFragment extends Fragment implements
        FirmwareContract.View {

    private static final String TAG = FirmwareUpdateFragment.class.getSimpleName();
    public static final String TITLE = "Firmware Update";

    // UI References
    private NumberPicker mFirmwarePicker;
    private Button mButtonDeploy;
    private ProgressBar mProgressBar;
    private TextView mTextViewStatus;

    private JsonFirmwareReader mJsonFirmwareReader;
    private ArrayList<JsonFirmwareType> mJsonFirmwareTypeList;
    private int mLastProgressIndication = -1;

    private FirmwarePresenter firmwarePresenter;

    public static FirmwareUpdateFragment newInstance() {
        FirmwareUpdateFragment firmwareUpdateFragment = new FirmwareUpdateFragment();
        return firmwareUpdateFragment;
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate the necessary layout
        View rootView = inflater.inflate(R.layout.fragment_firmware_update, container, false);

        // UI references
        mFirmwarePicker = (NumberPicker) rootView.findViewById(R.id.id_picker_firmware);
        mButtonDeploy = (Button) rootView.findViewById(R.id.id_btn_begin_deploy);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.id_progress_deployment);
        mTextViewStatus = (TextView) rootView.findViewById(R.id.id_tv_status);


        /**
         * {@link JsonFirmwareReader} reads the list of available firmwares located in
         * res/raw/firmware_descriptions.json. Update the raw folder to contain your binary and
         * update firmware_descriptions.json with the necessary information regarding the
         * update binary.
         *
         * Note: Update binaries must be binary files generated using the genimage.py Python script.
         * See Getting Started with the Rigado Secure Bootloader for more details.
         */
        mJsonFirmwareReader = new JsonFirmwareReader();
        mJsonFirmwareTypeList = mJsonFirmwareReader.getFirmwareList(getActivity());

        // show firmware in Picker
        final String[] arrayFirmwareNames = new String[mJsonFirmwareTypeList.size()];
        for(int index=0; index<mJsonFirmwareTypeList.size(); index++) {
            final JsonFirmwareType firmwareRecord = mJsonFirmwareTypeList.get(index);
            arrayFirmwareNames[index] = firmwareRecord.getFwname();
        }
        mFirmwarePicker.setMinValue(0);
        mFirmwarePicker.setMaxValue(mJsonFirmwareTypeList.size() - 1);
        mFirmwarePicker.setDisplayedValues(arrayFirmwareNames);
        mFirmwarePicker.post(new Runnable() {
            @Override
            public void run() {
                if (mJsonFirmwareTypeList.size() > 1) {
                    // auto select the 2nd item in the list to
                    // make the UI more identifiable to the user
                    mFirmwarePicker.setValue(1);
                }
            }
        });

        final int rigadoBlue = ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark);

        setNumberPickerTxtColor(mFirmwarePicker, rigadoBlue);

        mButtonDeploy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get selected firmware from picker
                final int selectedIndex = mFirmwarePicker.getValue();
                final JsonFirmwareType firmwareRecord = mJsonFirmwareTypeList.get(selectedIndex);

                showFirmwareUpdateDialog(firmwareRecord);
            }
        });

        firmwarePresenter = new FirmwarePresenter(this);

        if (!DeviceRepository.getInstance().isDeviceConnected()) {
            mButtonDeploy.setEnabled(false);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        firmwarePresenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        firmwarePresenter.onPause();
    }

    @Override
    public void updateProgressBar(final int progress) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });

    }

    @Override
    public void updateStatusText(final String status) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextViewStatus.setText(status);
            }
        });
    }

    @Override
    public void setFirmwareUpdateCompleted(final DemoDevice demoDevice) {
        mLastProgressIndication = -1;
        mProgressBar.setProgress(mLastProgressIndication);

        new AlertDialog.Builder(getActivity())
                .setTitle("Firmware Update Completed")
                .setMessage("To complete the firmware update, please reset your bluetooth and restart the app.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void setFirmwareUpdateFailed(final String errorMessage) {
        mLastProgressIndication = -1;
        mProgressBar.setProgress(mLastProgressIndication);
        mTextViewStatus.setText(errorMessage);

        new AlertDialog.Builder(getActivity())
                .setTitle("Firmware Update Failed")
                .setMessage(errorMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((MainActivity) getActivity()).mainPresenter.maybeStartScanning();
                    }
                })
                .setCancelable(false)
                .show();


    }

    @Override
    public void setButtonEnabled(boolean enabled) {
        mButtonDeploy.setEnabled(enabled);
    }

    @Override
    public void setWindowFlagEnabled(boolean enabled) {
        if (enabled) {
            getActivity().getWindow()
                    .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getActivity().getWindow()
                    .clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void reset() {
        mLastProgressIndication = -1;
        mProgressBar.setProgress(mLastProgressIndication);
        mTextViewStatus.setText("Idle");
    }

    /**
     * Utility function to change the text color.
     * Needed because android:textColor="@android:color/white" in XML has no effect !
     * @param numberPicker view object
     * @param color the desired color
     * @return true if successfully changed the color
     */
    public boolean setNumberPickerTxtColor(NumberPicker numberPicker, int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                } catch (NoSuchFieldException
                        | IllegalAccessException
                        | IllegalArgumentException e){
                    Log.w("setNumberPickerTxtColor", e);
                }
            }
        }
        return false;
    }

    private void showFirmwareUpdateDialog(final JsonFirmwareType firmwareType) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Firmware Update")
                .setMessage(String.format(
                        "Program the device with %s firmware?", firmwareType.getFwname()))
                .setPositiveButton(R.string.txt_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        firmwarePresenter.programFirmware(getActivity(), firmwareType);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }
}
