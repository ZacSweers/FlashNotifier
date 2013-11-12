package com.leepapesweers.flashnotifier;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Switch;

import com.actionbarsherlock.app.SherlockFragment;

public class FragmentMainTab extends SherlockFragment {

    Activity parent;
    private boolean mServiceRunning;
    private Switch mServiceSwitch;
    private SharedPreferences mPrefs;

    @Override
    public void onAttach(Activity activity) {
        parent = activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_tab, container, false);
//        mPrefs = this.getActivity().getSharedPreferences(
//                "com.leepapesweers.flashnotifier", Context.MODE_PRIVATE);
//
//        mServiceSwitch = (Switch) view.findViewById(R.id.chkbox);
//
//        mServiceRunning = isServiceRunning();
//
//        // Check pref values
//        ((CheckBox) view.findViewById(R.id.smsCheckBox)).setChecked(mPrefs.getBoolean("smsNotifications", false));
//        ((CheckBox) view.findViewById(R.id.callCheckBox)).setChecked(mPrefs.getBoolean("callNotifications", false));
//
//        updateServiceStatus();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        setUserVisibleHint(true);
    }
}