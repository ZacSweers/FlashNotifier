package com.leepapesweers.flashnotifier;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMSCallListener extends Service {

    private Camera mCamera;
    private boolean mLightOn;
    private boolean mFlashing;
    private SharedPreferences mPrefs;
    private SharedPreferences mAPIPrefs;
    private Thread flashingThread;
    private HashMap<String, Boolean> mMap;

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {

        Toast.makeText(getApplicationContext(), "Started service", Toast.LENGTH_SHORT).show();
        Log.i("SERVICE", "Service started");

        mPrefs = this.getSharedPreferences(
                "com.leepapesweers.flashnotifier", Context.MODE_PRIVATE);

        mAPIPrefs = getApplicationContext().getSharedPreferences(
                "com.leepapesweers.flashnotifier.apiaccess", Context.MODE_PRIVATE);

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
     * Done in a task because can't use sleep() on main thread
     */
    public class FlashTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute(){
            mFlashing = true;
        }

        @Override
        protected Void doInBackground(Void... params) {

            for (int i = 0; i < 2; ++i) {
                flashOn();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Log.e("FLASH", e.getMessage());
                    flashOff();     // Turn it off if something went wrong
                }
                flashOff();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.e("FLASH", e.getMessage());
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
     * This controls a thread that flashes the LED on an interval until the thread is stopped
     * Used for phone calls
     */
    public void toggleContinuousFlash(boolean b) {
        if (b && !mFlashing) {
            flashingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    mFlashing = true;
                    while (mFlashing) {
                        try {
                            flashOn();
                            Thread.sleep(100);
                            flashOff();
                            Thread.sleep(100);
                            flashOn();
                            Thread.sleep(100);
                            flashOff();
                            Thread.sleep(400);
                        } catch (InterruptedException e) {
                            Log.v("Continuous flashing", "Flashing stopped");
                        }

                    }
                }
            });
            flashingThread.run();
        }
        else {
            mFlashing = false;
            if (flashingThread != null) {
                flashingThread.stop();
            }
            flashingThread = null;
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

    private BroadcastReceiver mSMSListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPrefs.getBoolean("smsNotifications", false)) {
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

                if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    // Ringing
//                    new FlashTask().execute();
                    toggleContinuousFlash(false);
                }

                if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    // Detect call answered
                    toggleContinuousFlash(false);
                }

                if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    // Detect end of call, probably don't need this
                    toggleContinuousFlash(false);
                }
            }

        }
    };

    private BroadcastReceiver mAPIListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPrefs.getBoolean("apiDefault", false)) {
                List<Integer> flashes = Arrays.asList(50, 50, 50, 50);
                new CustomFlashTask().execute(flashes);
            }
//            toggleContinuousFlash(true);
        }
    };

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "Service stopped", Toast.LENGTH_SHORT).show();
        Log.i("SERVICE", "Service stopped");
        unregisterReceiver(mCallListener);
        unregisterReceiver(mSMSListener);
        unregisterReceiver(mAPIListener);
    }


    /**
     * Loads the preferences into a hashmap
     * Loosely based on http://stackoverflow.com/a/4955428
     */
    private void refreshACList() {
        mMap.clear();
        Map<String, Boolean> map = (Map<String, Boolean>) mAPIPrefs.getAll();
        if(!map.isEmpty()){
            for (Map.Entry<String, Boolean> stringBooleanEntry : map.entrySet()) {
                Map.Entry pairs = (Map.Entry) stringBooleanEntry;
                mMap.put((String) pairs.getKey(), (Boolean) pairs.getValue());
            }
        }
    }

    private void updateACList(String appName, boolean b) {

        if (mAPIPrefs.contains(appName)) {
            SharedPreferences.Editor editor = mAPIPrefs.edit();
            editor.putBoolean(appName, b).commit();
        }

        refreshACList();

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
}
