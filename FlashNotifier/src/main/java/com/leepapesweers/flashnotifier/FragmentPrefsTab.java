package com.leepapesweers.flashnotifier;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * At some point, will replace the FragmentMainTab with a SettingsFragment based on these:
 * http://blog.fordemobile.com/2012/11/display-preference-fragment-compatible.html
 * http://www.cs.dartmouth.edu/~campbell/cs65/lecture12/lecture12.html
 * http://forum.xda-developers.com/showpost.php?p=19719977&postcount=1
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentPrefsTab extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }
}