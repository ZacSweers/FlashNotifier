package com.leepapesweers.flashnotifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.SherlockListFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListFragmentAccessTab extends SherlockListFragment {

    private HashMap<String, Boolean> mMap;
    private SharedPreferences mPrefs;
    private SimpleAdapter mAdapter;
    private static ArrayList<HashMap<String, Object>> mURLListItems = new ArrayList<HashMap<String, Object>>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the view from fragmenttab2.xml
        View view = inflater.inflate(R.layout.fragment_access_tab, container, false);

        mPrefs = getActivity().getSharedPreferences(
                "com.leepapesweers.flashnotifier.apiaccess", Context.MODE_PRIVATE);

//        ListView listView = getListView();
//        String[] from = new String[] {"icon", "appname", "tick", "x"};
//        int[] to = new int[] { R.id.ic_generic, R.id.tv_appname, R.id.ic_tick, R.id.ic_x};
        String[] from = new String[] {"appname"};
        int[] to = new int[] { R.id.tv_appname};
        mAdapter = new SimpleAdapter(getActivity(), mURLListItems, R.layout.api_access_listitem,
                from, to);
        setListAdapter(mAdapter);

        for (int i = 0; i < 10; ++i) {
            example();
        }

//        loadACList();

        return view;
    }

    public void example() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("appname", "Some example app");
        mURLListItems.add(map);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Do something when a list item is clicked
        HashMap<String, Object> map = mURLListItems.get(position);



//        if (!mRefreshing) {
//            HashMap<String, Object> map = mURLListItems.get(position);
//            String shortURL = (String) map.get("title");
//            Log.i("itemClicked", Boolean.toString(mRefreshing));
//            refreshMetrics task = new refreshMetrics();
//            task.execute(shortURL);
//        }
    }

    /**
     * Loads the preferences into a hashmap
     * Loosely based on http://stackoverflow.com/a/4955428
     */
    private void loadACList() {
        mMap.clear();
        Map<String, Boolean> map = (Map<String, Boolean>) mPrefs.getAll();
        if(!map.isEmpty()){
            for (Map.Entry<String, Boolean> stringBooleanEntry : map.entrySet()) {
                Map.Entry pairs = (Map.Entry) stringBooleanEntry;
                mMap.put((String) pairs.getKey(), (Boolean) pairs.getValue());
            }
        }
    }

    private void updateACList(String appName, boolean b) {

        if (mPrefs.contains(appName)) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(appName, b).commit();
        }

//        if (value.equals("")) {
//
//            boolean storedPreference = preferences.contains(app);
//            if (storedPreference) {
//                SharedPreferences.Editor editor = preferences.edit();
//                editor.remove(key); // value to store
//                Log.d("KEY", key);
//                editor.commit();
//            }
//        }else{
//
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putString(key, value); // value to store
//            Log.d("KEY", key);
//            editor.commit();
//        }
    }

    private void removeAppFromAC(String appName) {
        if (mPrefs.contains(appName)) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.remove(appName).commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }
}