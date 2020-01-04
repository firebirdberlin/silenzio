package com.firebirdberlin.silenzio;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;


public class mAudioManager {
    private static String TAG = "mAudioManager";
    private Context mContext;
    private AudioManager audiomanage;
    private int currentRingerMode = -1;
    private int maxRingerVolume;


    // constructor
    public mAudioManager(Context context){
        this.mContext = context;
        audiomanage = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        maxRingerVolume  = audiomanage.getStreamMaxVolume(AudioManager.STREAM_RING);
    }

    public void setMode(int stream) {
        audiomanage.setMode(stream);
    }

    public boolean isBluetoothA2dpOn() {
        return audiomanage.isBluetoothA2dpOn();
    }

    public int getMaxRingerVolume() {return maxRingerVolume;}

    public void setRingerMode(int mode){
        currentRingerMode = audiomanage.getRingerMode(); // ringer mode to restore
        audiomanage.setRingerMode(mode);
    }

    public void setRingerModeSilent(){
        Log.i(TAG, "setRingerModeSilent()");
        currentRingerMode = audiomanage.getRingerMode(); // ringer mode to restore
        Log.i(TAG, "currentRingerMode:" + currentRingerMode);
        audiomanage.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    public boolean isSilent(){
        return (audiomanage.getRingerMode() == AudioManager.RINGER_MODE_SILENT);
    }

    public boolean isVibration(){
        return (audiomanage.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE);
    }

    public void setRingerVolume(int value){
        currentRingerMode = audiomanage.getRingerMode(); // ringer mode to restore
        if (currentRingerMode == AudioManager.RINGER_MODE_SILENT) return;
        if (currentRingerMode == AudioManager.RINGER_MODE_VIBRATE) return;

        if (value > maxRingerVolume) value = maxRingerVolume;
        if (value < 0 ) value = 0;

        audiomanage.setStreamVolume(AudioManager.STREAM_RING, value,  0);
//        audiomanage.setStreamVolume(AudioManager.STREAM_RING, value,  AudioManager.FLAG_SHOW_UI);
        Log.i(TAG, "new ringer volume : " + String.valueOf(value));
    }

    public int getRingerMode(){
        return audiomanage.getRingerMode();
    }

    public int getRingerVolume(){
        return audiomanage.getStreamVolume(AudioManager.STREAM_RING);
    }

    public void restoreRingerMode(){
        Log.i(TAG, "restoreRingerMode(" + currentRingerMode + ")");
        // no previous ringer mode
        if (currentRingerMode == -1) return;
        // initial ringer mode was silent, don't have to do anything
        if (currentRingerMode == AudioManager.RINGER_MODE_SILENT) return;

        // The expected ringer mode is silent. Is it still valid ?
        // If not, another app may have changed it. R-E-S-P-E-C-T this setting.
        if (audiomanage.getRingerMode() != AudioManager.RINGER_MODE_SILENT) return;

        // otherwise we will reset the ringer mode
        audiomanage.setRingerMode(currentRingerMode);
        currentRingerMode = -1;
    }

    protected void muteMusic(boolean on){
        audiomanage.setStreamMute(AudioManager.STREAM_MUSIC, on);
    }

    private void muteNotifications(boolean on){
        audiomanage.setStreamMute(AudioManager.STREAM_NOTIFICATION, on);
    }

    private void muteRinger(boolean on){
        audiomanage.setStreamMute(AudioManager.STREAM_RING, on);
    }

    private boolean isMuted = false;
    public void mute(){
        if ( isMuted == false ) {
            Log.d(TAG,"mute ringer volume");
            isMuted = true;
            muteRinger(true);
            muteNotifications(true);
        }
    }

    public void unmute(){
        if (isMuted){
            Log.i(TAG,"unmute ringer volume");
            muteRinger(false);
            muteNotifications(false);
            isMuted = false;
        }
    }

    public void setSpeakerphoneOn(boolean on){
        audiomanage.setSpeakerphoneOn(on);
    }

    @SuppressWarnings("deprecation")
    public boolean isWiredHeadsetOn(){
        return audiomanage.isWiredHeadsetOn();
    }

    @SuppressWarnings("deprecation")
    public static boolean isWiredHeadsetOn(Context context){
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return am.isWiredHeadsetOn();
    }
}
