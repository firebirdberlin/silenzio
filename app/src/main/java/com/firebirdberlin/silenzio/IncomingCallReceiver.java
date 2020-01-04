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
            SetRingerService.start(context);
        } else { // OFFHOOK or IDLE

        }
    }
}
