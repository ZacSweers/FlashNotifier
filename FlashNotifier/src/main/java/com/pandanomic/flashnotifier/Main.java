package com.pandanomic.flashnotifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

public class Main extends SherlockFragmentActivity {

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
                mActionBar.getTabAt(position).select();
                invalidateOptionsMenu();
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
                invalidateOptionsMenu();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int pageNum = mActionBar.getSelectedTab().getPosition();
        if(pageNum == 0){
            menu.findItem(R.id.refresh).setVisible(false);
        }else{
            menu.findItem(R.id.refresh).setVisible(true);
        }

        return true;
    }
}