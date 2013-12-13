package com.pandanomic.flashnotifier;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListFragmentAccessTab extends SherlockListFragment {

    private final String PERMISSION = "android.permission.ACCESS_FLASHNOTIFIER";
    private SharedPreferences mWhitelistPrefs;
    private SharedPreferences mBlacklistPrefs;
    private AppsArrayAdapter mNewAdapter;
    private static List<Map<String, String>> mAppListItems = new ArrayList<Map<String, String>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWhitelistPrefs = getActivity().getSharedPreferences(
                "com.leepapesweers.flashnotifier.whitelistprefs", Context.MODE_PRIVATE);

        mBlacklistPrefs = getActivity().getSharedPreferences(
                "com.leepapesweers.flashnotifier.blacklistprefs", Context.MODE_PRIVATE);

        if (savedInstanceState == null) {
            mNewAdapter = new AppsArrayAdapter(getActivity(), R.layout.api_access_listitem, mAppListItems);
            updateACList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_access_tab, container, false);
        setRetainInstance(true);

        setListAdapter(mNewAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final MenuItem refresh = menu.getItem(R.id.refresh);
        refresh.setVisible(true);
        refresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                refresh.setIcon(R.layout.progressbar);
                updateACList();
                refresh.setIcon(R.drawable.ic_action_reload);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Method for sniffing apps that use a certain permission.
     * Based on here: http://stackoverflow.com/a/13028631/3034339
     */
    private void updateACList() {
        mAppListItems.clear();
        mNewAdapter.notifyDataSetChanged();

        PackageManager packageManager = getActivity().getPackageManager();
        List<PackageInfo> applist = packageManager.getInstalledPackages(0);

        for (PackageInfo pk : applist) {
            if (PackageManager.PERMISSION_GRANTED == packageManager.checkPermission(PERMISSION, pk.packageName)) {
                HashMap<String, String> data = new HashMap<String, String>();
                data.put("package_name", pk.packageName);
                data.put("app_name", (String) pk.applicationInfo.loadLabel(packageManager));
                mNewAdapter.add(data);
            }
        }

        // Sort the apps
        Collections.sort(mAppListItems, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> lhs, Map<String, String> rhs) {
                return lhs.get("app_name").compareTo(rhs.get("app_name"));
            }
        });

        mNewAdapter.notifyDataSetChanged();
    }

    /**
     * Custom ArrayAdapter class used for maintaining the list of apps
     */
    public class AppsArrayAdapter extends ArrayAdapter<Map<String, String>> {

        private Context mContext;

        public AppsArrayAdapter(Context context, int resourceId, List<Map<String, String>> objects) {
            super(context, resourceId, objects);
            mContext = context;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.api_access_listitem, null, false);
            }

            // Initialize local vars for view control
            final Map<String, String> data = getItem(position);
            final String appNameString = (data.get("app_name") != null) ? data.get("app_name") : "(unknown)";
            String packageString = data.get("package_name");

            // Set the image
            ImageView appImage = (ImageView) convertView.findViewById(R.id.ic_appicon);
            try {
                appImage.setImageDrawable(mContext.getPackageManager().getApplicationIcon(packageString));
            } catch (PackageManager.NameNotFoundException e) {
                appImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_generic));
            }

            // Set the app name text
            final TextView appNameTextView = (TextView) convertView.findViewById(R.id.tv_appname);
            appNameTextView.setText(appNameString);

            // Set the name color based on user's AC pref
            if (mWhitelistPrefs.contains(appNameString)) {
                appNameTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
            else if (mBlacklistPrefs.contains(appNameString)) {
                appNameTextView.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }
            else {
                appNameTextView.setTextColor(getResources().getColor(android.R.color.black));
            }

            // Set onClickListener for when user presses the "info" button
            ImageView infoImg = (ImageView) convertView.findViewById(R.id.ic_info);
            infoImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Make a dialog
                    new AlertDialog.Builder(mContext)
                            .setTitle(appNameTextView.getText() + " access")
                            .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mWhitelistPrefs.edit().putBoolean(appNameString, true).commit();
                                    if (mBlacklistPrefs.contains(appNameString))
                                        mBlacklistPrefs.edit().remove(appNameString).commit();
                                    appNameTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                                }
                            })
                            .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mBlacklistPrefs.edit().putBoolean(appNameString, true).commit();
                                    if (mWhitelistPrefs.contains(appNameString))
                                        mWhitelistPrefs.edit().remove(appNameString).commit();
                                    appNameTextView.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                }
                            })
                            .setNeutralButton("Clear Setting", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mWhitelistPrefs.contains(appNameString))
                                        mWhitelistPrefs.edit().remove(appNameString).commit();
                                    if (mBlacklistPrefs.contains(appNameString))
                                        mBlacklistPrefs.edit().remove(appNameString).commit();
                                    appNameTextView.setTextColor(getResources().getColor(android.R.color.black));
                                }
                            })
                            .show();
                }
            });

            return convertView;
        }
    }
}