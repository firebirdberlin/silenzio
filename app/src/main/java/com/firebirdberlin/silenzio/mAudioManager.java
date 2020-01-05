package com.firebirdberlin.silenzio;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;


public class mAudioManager {
    private static String TAG = "mAudioManager";
    private Context context;
    private AudioManager audioManager;
    private int currentRingerMode = -1;
    private int maxRingerVolume;


    mAudioManager(Context context){
        this.context = context;
        audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        maxRingerVolume  = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
    }

    public void setMode(int stream) {
        audioManager.setMode(stream);
    }

    public boolean isBluetoothA2dpOn() {
        return audioManager.isBluetoothA2dpOn();
    }

    public int getMaxRingerVolume() {return maxRingerVolume;}

    public void setRingerMode(int mode){
        currentRingerMode = audioManager.getRingerMode(); // ringer mode to restore
        audioManager.setRingerMode(mode);
    }

    public void setRingerModeSilent(){
        Log.i(TAG, "setRingerModeSilent()");
        currentRingerMode = audioManager.getRingerMode(); // ringer mode to restore
        Log.i(TAG, "currentRingerMode:" + currentRingerMode);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    public boolean isSilent(){
        return (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT);
    }

    public boolean isVibration(){
        return (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE);
    }

    public void setRingerVolume(int value){
        currentRingerMode = audioManager.getRingerMode(); // ringer mode to restore
        if (currentRingerMode == AudioManager.RINGER_MODE_SILENT) return;
        if (currentRingerMode == AudioManager.RINGER_MODE_VIBRATE) return;

        if (value > maxRingerVolume) value = maxRingerVolume;
        if (value < 0 ) value = 0;

        audioManager.setStreamVolume(AudioManager.STREAM_RING, value,  0);
//        audioManager.setStreamVolume(AudioManager.STREAM_RING, value,  AudioManager.FLAG_SHOW_UI);
        Log.i(TAG, "new ringer volume : " + String.valueOf(value));
    }

    public int getRingerMode(){
        return audioManager.getRingerMode();
    }

    public int getRingerVolume(){
        return audioManager.getStreamVolume(AudioManager.STREAM_RING);
    }

    public void restoreRingerMode(){
        Log.i(TAG, "restoreRingerMode(" + currentRingerMode + ")");
        // no previous ringer mode
        if (currentRingerMode == -1) return;
        // initial ringer mode was silent, don't have to do anything
        if (currentRingerMode == AudioManager.RINGER_MODE_SILENT) return;

        // The expected ringer mode is silent. Is it still valid ?
        // If not, another app may have changed it. R-E-S-P-E-C-T this setting.
        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) return;

        // otherwise we will reset the ringer mode
        audioManager.setRingerMode(currentRingerMode);
        currentRingerMode = -1;
    }

    protected void muteMusic(boolean on){
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, on);
    }

    private void muteNotifications(boolean on){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, on);
            return;
        }
        audioManager.adjustStreamVolume(
                AudioManager.STREAM_NOTIFICATION,
                on ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE,
                0
        );
    }

    private void muteRinger(boolean on){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            audioManager.setStreamMute(AudioManager.STREAM_RING, on);
            return;
        }
        audioManager.adjustStreamVolume(
                AudioManager.STREAM_RING,
                on ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE,
                0
        );

    }

    private boolean isMuted = false;
    void mute(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null
                    && !notificationManager.isNotificationPolicyAccessGranted()){
                return;
            }
        }
        if (!isMuted) {
            Log.d(TAG,"mute ringer volume");
            isMuted = true;
            muteRinger(true);
            muteNotifications(true);
        }
    }

    void unmute(){
        if (isMuted){
            Log.i(TAG,"unmute ringer volume");
            muteRinger(false);
            muteNotifications(false);
            isMuted = false;
        }
    }

    public void setSpeakerphoneOn(boolean on){
        audioManager.setSpeakerphoneOn(on);
    }

    @SuppressWarnings("deprecation")
    public boolean isWiredHeadsetOn(){
        return audioManager.isWiredHeadsetOn();
    }

    @SuppressWarnings("deprecation")
    public static boolean isWiredHeadsetOn(Context context){
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return am.isWiredHeadsetOn();
    }
}
