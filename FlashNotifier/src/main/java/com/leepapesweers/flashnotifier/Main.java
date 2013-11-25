package com.leepapesweers.flashnotifier;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class Main extends SherlockFragmentActivity {

    private boolean mServiceRunning;
    private Switch mServiceSwitch;
    private SharedPreferences mPrefs;
    ActionBar mActionBar;
    ViewPager mPager;
    ActionBar.Tab tab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = this.getSharedPreferences(
                "com.leepapesweers.flashnotifier", Context.MODE_PRIVATE);

        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mPager = (ViewPager) findViewById(R.id.pager);
        FragmentManager fm = getSupportFragmentManager();

        // Get swipes
        ViewPager.SimpleOnPageChangeListener ViewPagerListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Find the ViewPager Position
                mActionBar.setSelectedNavigationItem(position);
            }
        };

        mPager.setOnPageChangeListener(ViewPagerListener);

        // Locate the adapter class called ViewPagerAdapter.java
        ViewPagerAdapter viewpageradapter = new ViewPagerAdapter(fm);

        // Set the View Pager Adapter into ViewPager
        mPager.setAdapter(viewpageradapter);

        // Capture tab button clicks
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {

            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // Pass the position on tab click to ViewPager
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // TODO Auto-generated method stub
            }
        };

        // Create first Tab
        tab = mActionBar.newTab().setText("Settings").setTabListener(tabListener);
        mActionBar.addTab(tab);

        // Create second Tab
        tab = mActionBar.newTab().setText("Access").setTabListener(tabListener);
        mActionBar.addTab(tab);

        SharedPreferences.OnSharedPreferenceChangeListener prefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs,String key) {
                    if (key.equals("apiDefault"))
                    {
                        Toast.makeText(getBaseContext(), "default changed", Toast.LENGTH_SHORT).show();
                    }
                }
           };
        mPrefs.registerOnSharedPreferenceChangeListener(prefListener);
    }



    /**
     * Toggle the service if the checkboxview is clicked
     * @param v the view parameter passed in from onClick
     */
    public void toggleService(View v) {
        Switch chkBox = (Switch) v;
        if (chkBox.isChecked()) {
            mServiceRunning = true;
            startService(new Intent(this, SMSCallListener.class));
            updateServiceStatus(chkBox);
        } else {
            mServiceRunning = false;
            stopService(new Intent(this, SMSCallListener.class));
            updateServiceStatus(chkBox);
        }
    }

    /**
     * Updates the contents of the switch view
     */
    public void updateServiceStatus(Switch chkBox) {
        if (mServiceRunning) {
            chkBox.setChecked(true);
            chkBox.setText("Service is running!");
        } else {
            chkBox.setChecked(false);
            chkBox.setText("Service isn't running");
        }
    }

    public void updateUserPref(View v) {

        CheckBox checkBox = (CheckBox) v;
        boolean val = checkBox.isChecked();

        if (v.getId() == R.id.callCheckBox) {
            mPrefs.edit().putBoolean("callNotifications", val).commit();
        } else {
            // Must be SMS
            mPrefs.edit().putBoolean("smsNotifications", val).commit();
        }
    }

    public void APIDialog(View v) {
        startActivity(new Intent(this, APIAccess.class));
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    /**
     * Borrowed from http://stackoverflow.com/a/5921190
     * @return true or false, depending on if service is running
     */
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SMSCallListener.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}