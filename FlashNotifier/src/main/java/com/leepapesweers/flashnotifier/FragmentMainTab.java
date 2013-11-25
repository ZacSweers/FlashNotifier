package com.leepapesweers.flashnotifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
        mPrefs = parent.getSharedPreferences("com.leepapesweers.flashnotifier", Context.MODE_PRIVATE);
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("FragTab", "here");
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_main_tab, container, false);

        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new APIDefaultItemSelectedListener());
        int spinnerPos = (mPrefs.getBoolean("apiDefault", false)) ? 0 : 1;
        Log.d("FragTab", ""+spinnerPos);
        spinner.setSelection((mPrefs.getBoolean("apiDefault", false)) ? 0 : 1);
        spinner.refreshDrawableState();
        new APIDefaultItemSelectedListener();

        view.findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent("com.leepapesweers.flashnotifier.API");
                getActivity().sendBroadcast(i);
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        setUserVisibleHint(true);
    }

    public class APIDefaultItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            Log.d("itemSelected", "" + pos);

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