package com.leepapesweers.flashnotifier;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SMSCallListener extends Service {

    private final String PERMISSION = "android.permission.ACCESS_FLASHNOTIFIER";
    private Camera mCamera;
    private boolean mLightOn;
    private boolean mFlashing;
    private SharedPreferences mPrefs;
    private SharedPreferences mAPIPrefs;
    private HashMap<String, Boolean> mMap;
    private SharedPreferences mWhitelistPrefs;
    private SharedPreferences mBlacklistPrefs;

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {

        Toast.makeText(getApplicationContext(), "Started service", Toast.LENGTH_SHORT).show();

        mPrefs = this.getSharedPreferences(
                "com.leepapesweers.flashnotifier", Context.MODE_PRIVATE);

        mAPIPrefs = getApplicationContext().getSharedPreferences(
                "com.leepapesweers.flashnotifier.apiaccess", Context.MODE_PRIVATE);

        mWhitelistPrefs = getApplicationContext().getSharedPreferences(
                "com.leepapesweers.flashnotifier.whitelistprefs", Context.MODE_PRIVATE);

        mBlacklistPrefs = getApplicationContext().getSharedPreferences(
                "com.leepapesweers.flashnotifier.blacklistprefs", Context.MODE_PRIVATE);

        mLightOn = false;
        mFlashing = false;

        // Register SMS listener
        IntentFilter smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mSMSListener, smsFilter);

        // Register call listener
        IntentFilter callFilter = new IntentFilter("android.intent.action.PHONE_STATE");
        registerReceiver(mCallListener, callFilter);

        // Register SMS listener
        IntentFilter apiFilter = new IntentFilter("com.leepapesweers.flashnotifier.API");
        registerReceiver(mAPIListener, apiFilter);

        return Service.START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Task for passing in custom lists, probably what we should just use for predefined flash
     * patterns too. Pass in a list.
     */
    public class CustomFlashTask extends AsyncTask<List<Integer>, Void, Void> {

        @Override
        protected void onPreExecute(){
            mFlashing = true;
        }

        @Override
        protected Void doInBackground(List<Integer>... params) {
            List<Integer> list = params[0];

            // Make sure it goes off at the end and isn't unreasonable
            if (list.size() % 2 != 0 || list.size() > 10) {
                Log.e("SERVICE", "Invalid flash parameters");
                return null;
            }

            // Flash on evens (0, 2, 4...)
            boolean flash = true;

            // Iterate over on/off durations.
            for (Integer i : list) {
                if (flash) {
                    flashOn();
                    flash = false;
                } else {
                    flashOff();
                    flash = true;
                }

                // Run for as long as interval passed
                try {
                    Thread.sleep(i);
                } catch (InterruptedException e) {
                    Log.e("FLASH", e.getMessage());
                    flashOff();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mFlashing = false;
        }
    }

    /**
     * Toggle method for continuous flashing
     * This controls a thread that flashes the LED on an interval until the mFlashing
     * boolean is flipped back to false
     */
    public class ContinuousFlashTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected Void doInBackground(Void... params) {

            // Timeout after 25secs, just in case
            int counter = 0;

            while (mFlashing) {
                ++counter;
                if (counter == 25) break;

                try {
                    flashOn();
                    Thread.sleep(50);
                    flashOff();
                    Thread.sleep(50);
                    if (!mFlashing)
                        break;
                    flashOn();
                    Thread.sleep(50);
                    flashOff();
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    Log.v("Continuousflashing", "Flashing stopped");
                }
            }

            return null;
        }
    }

    private void flashOn() {
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(parameters);
        mLightOn = true;
    }

    private void flashOff() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(parameters);
        mCamera.release();
        mLightOn = false;
    }

    ////////////////// Set up broadcast receivers //////////////////
    private BroadcastReceiver mSMSListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mFlashing && mPrefs.getBoolean("smsNotifications", false)) {
                mFlashing = true;
                List<Integer> flashes = Arrays.asList(50, 50, 50, 50);
                new CustomFlashTask().execute(flashes);
            }
        }
    };

    private BroadcastReceiver mCallListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPrefs.getBoolean("callNotifications", false)) {
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

                if(!mFlashing && state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    // Ringing
                    mFlashing = true;
                    new ContinuousFlashTask().execute();
                } else {
                    // Disable on anything else (answered, end of call, etc)
                    mFlashing = false;
                }

            }
        }
    };

    /**
     * API listener, anonymous inner class
     */
    private BroadcastReceiver mAPIListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            // First verify the API call
            String appName = verifyAPICall(intent);
            if (appName != null) {

                // Check user prefs
                // IF it's on the whitelist OR (not on the blacklist AND default access is "allow")
                if (!mFlashing && (mWhitelistPrefs.contains(appName) ||
                        (mPrefs.getBoolean("apiDefault", false) && !mBlacklistPrefs.contains(appName)))) {
                    mFlashing = true;
                    List<Integer> flashes;
                    flashes = intent.getIntegerArrayListExtra("flash_pattern");
                    if (flashes == null) {
                        flashes = Arrays.asList(50, 50, 50, 50);
                    }
                    new CustomFlashTask().execute(flashes);
                }
            }
        }

        /**
         * Method for verifying that the API call is allowed, based on the calling app's permissions
         * @param intent sending intent to use the API
         * @return String null if it failed, app name if it worked
         */
        private String verifyAPICall(Intent intent) {
            PackageManager pm = getPackageManager();
            String packageName = intent.getStringExtra("calling_application");

            // Check to make sure it included the package name
            if (packageName == null) {
                Toast.makeText(getBaseContext(), "App has malformed API call (missing package name)", Toast.LENGTH_SHORT).show();
                return null;
            }

            // Get the package name
            ApplicationInfo ai;
            try {
                ai = pm.getApplicationInfo(packageName, 0);
            } catch (final PackageManager.NameNotFoundException e) {
                ai = null;
            }
            final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : null);

            // Check the manifest permissions
            if (PackageManager.PERMISSION_DENIED == pm.checkPermission(PERMISSION, packageName)) {
                Toast.makeText(getBaseContext(), applicationName + " hasn't declared the permission in the manifest", Toast.LENGTH_SHORT).show();
                return null;
            }

            return applicationName;
        }
    };

    @Override
    public void onDestroy() {
        // Need to unregister listeners
        Toast.makeText(getApplicationContext(), "Service stopped", Toast.LENGTH_SHORT).show();
        unregisterReceiver(mCallListener);
        unregisterReceiver(mSMSListener);
        unregisterReceiver(mAPIListener);
    }
}
