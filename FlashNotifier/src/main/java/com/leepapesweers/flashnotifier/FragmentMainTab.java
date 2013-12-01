package com.leepapesweers.flashnotifier;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import org.jraf.android.backport.switchwidget.Switch;

import java.util.ArrayList;

public class FragmentMainTab extends SherlockFragment {

    private boolean mServiceRunning;
    private Switch mServiceSwitch;
    private SharedPreferences mPrefs;
    private Spinner mSpinner;
    private CheckBox mSMSCheckbox;
    private CheckBox mPhoneCheckbox;

    @Override
    public void onAttach(Activity activity) {
        mPrefs = getActivity().getSharedPreferences("com.leepapesweers.flashnotifier", Context.MODE_PRIVATE);
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_main_tab, container, false);
        setHasOptionsMenu(false);

        mServiceSwitch = (Switch) (view != null ? view.findViewById(R.id.service_switch) : null);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            Typeface tf = Typeface.createFromAsset(
                    getActivity().getAssets(), "fonts/Roboto-Light.ttf");
            mServiceSwitch.setTypeface(tf);
        }

        mServiceRunning = isServiceRunning();

        updateServiceStatus();


        mSpinner = (Spinner) (view != null ? view.findViewById(R.id.spinner) : null);
        mSMSCheckbox = (CheckBox) (view != null ? view.findViewById(R.id.smsCheckBox) : null);
        mPhoneCheckbox = (CheckBox) (view != null ? view.findViewById(R.id.callCheckBox) : null);

        // Set up spinner
        int spinnerPos = (mPrefs.getBoolean("apiDefault", false)) ? 0 : 1;
        mSpinner.setSelection((mPrefs.getBoolean("apiDefault", false)) ? 0 : 1);
        mSpinner.refreshDrawableState();
        new APIDefaultItemSelectedListener();

        initializeListeners(view);

        return view;
    }

    /**
     * Initialize the onClickListeners for the respective views
     * @param view the view containing the views we're updating
     */
    public void initializeListeners(final View view){

        mSpinner.setOnItemSelectedListener(new APIDefaultItemSelectedListener());

        mServiceSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleService();
            }
        });

        mPhoneCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserPref(mPhoneCheckbox);
            }
        });

        mSMSCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserPref(mSMSCheckbox);
            }
        });

        view.findViewById(R.id.tv_calls).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // Test button for API
        view.findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isServiceRunning()) {
                    ArrayList<Integer> pattern = new ArrayList<Integer>();
                    pattern.add(50);
                    pattern.add(50);
                    Intent i = new Intent("com.leepapesweers.flashnotifier.API");
                    i.putIntegerArrayListExtra("flash_pattern", pattern);
                    i.putExtra("calling_application", getActivity().getPackageName());
                    getActivity().sendBroadcast(i);
                } else {
                    Toast.makeText(getActivity(), "Service isn't running!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        setUserVisibleHint(true);
    }

    /**
     * Toggle the service if the checkboxview is clicked
     */
    public void toggleService() {
        if (mServiceSwitch.isChecked()) {
            mServiceRunning = true;
            getActivity().startService(new Intent(getActivity(), SMSCallListener.class));
            updateServiceStatus();
        } else {
            mServiceRunning = false;
            getActivity().stopService(new Intent(getActivity(), SMSCallListener.class));
            updateServiceStatus();
        }
    }

    /**
     * Updates the contents of the switch view
     */
    public void updateServiceStatus() {
        if (mServiceRunning) {
            mServiceSwitch.setChecked(true);
            mServiceSwitch.setText("Service is running!");
        } else {
            mServiceSwitch.setChecked(false);
            mServiceSwitch.setText("Service isn't running");
        }
    }

    /**
     * Borrowed from http://stackoverflow.com/a/5921190
     * @return true or false, depending on if service is running
     */
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SMSCallListener.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void updateUserPref(CheckBox checkBox) {

        boolean val = checkBox.isChecked();

        if (checkBox.getId() == R.id.callCheckBox) {
            mPrefs.edit().putBoolean("callNotifications", val).commit();
        } else {
            // Must be SMS
            mPrefs.edit().putBoolean("smsNotifications", val).commit();
        }
    }

    public class APIDefaultItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            switch (pos) {
                case 0:
//                    Toast.makeText(getActivity(), "Changed default", Toast.LENGTH_SHORT).show();
                    mPrefs.edit().putBoolean("apiDefault", true).commit();
                    break;
                case 1:
//                    Toast.makeText(getActivity(), "Changed default", Toast.LENGTH_SHORT).show();
                    mPrefs.edit().putBoolean("apiDefault", false).commit();
                    break;
                default:
                    break;
            }
        }

        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }
    }
}