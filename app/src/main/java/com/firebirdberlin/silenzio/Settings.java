package com.firebirdberlin.silenzio;

import android.content.Context;
import android.content.SharedPreferences;

class Settings {
    private SharedPreferences settings;

    boolean FlipAction = false;
    boolean PullOutAction = false;
    boolean ShakeAction = false;
    boolean enabled = true;

    Settings(Context context){
        settings = context.getSharedPreferences(Silenzio.PREFS_KEY, 0);
        reload();
    }

    private void reload() {
        FlipAction = settings.getBoolean("FlipAction", true);
        PullOutAction = settings.getBoolean("PullOutAction", true);
        ShakeAction = settings.getBoolean("ShakeAction", true);
        enabled = settings.getBoolean("enabled", true);
    }
}
