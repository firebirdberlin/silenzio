package com.firebirdberlin.silenzio;

import android.content.Context;
import android.content.SharedPreferences;

class Settings {
    private SharedPreferences settings;

    boolean FlipAction = false;
    boolean FlipActionNotification = false;
    boolean PullOutAction = false;
    boolean ShakeAction = false;
    boolean enabled = true;

    Settings(Context context){
        settings = context.getSharedPreferences(Silenzio.PREFS_KEY, 0);
        reload();
    }

    private void reload() {
        FlipAction = settings.getBoolean("FlipAction", true);
        FlipActionNotification = settings.getBoolean("FlipActionNotification", true);
        PullOutAction = settings.getBoolean("PullOutAction", true);
        ShakeAction = settings.getBoolean("ShakeAction", true);
        enabled = settings.getBoolean("enabled", true);
    }

    public static void setEnabled(Context context, boolean on) {
        SharedPreferences settings = context.getSharedPreferences(Silenzio.PREFS_KEY, 0);
        SharedPreferences.Editor edit = settings.edit();
        edit.putBoolean("enabled", on);
        edit.apply();
    }

    public static boolean getEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Silenzio.PREFS_KEY, 0);
        return settings.getBoolean("enabled", true);
    }

}
