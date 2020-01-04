package com.firebirdberlin.silenzio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;


public class IncomingCallReceiver extends BroadcastReceiver {
    private final static String TAG = "IncomingCallReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        SharedPreferences settings = context.getSharedPreferences(Silenzio.PREFS_KEY, 0);
        if (!settings.getBoolean("enabled", true)) return;

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        Log.i(TAG, "Phone state changed to " + state);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {

            Intent i =  new Intent(context, SetRingerService.class);
            i.putExtra("PHONE_STATE", state);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i);
            } else {
                context.startService(i);
            }

        } else { // OFFHOOK or IDLE

        }
    }
}
