package com.firebirdberlin.silenzio;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;



public class mNotificationListener extends NotificationListenerService
implements SensorEventListener {

    private String TAG = this.getClass().getSimpleName();
    private SharedPreferences settings;
    public static boolean isRunning = false;
    private mAudioManager audioManager;

    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];

    Handler handler = new Handler();
    Runnable checkOrientation = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(checkOrientation);
            registerReceiver();
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = new mAudioManager(this);
        settings = getSharedPreferences(Silenzio.PREFS_KEY, 0);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(checkOrientation);
        super.onDestroy();
    }


    @Override
    public void onListenerConnected() {
        Log.i(TAG, "onListenerConnected");
        isRunning = true;
    }

    @Override
    public void onListenerDisconnected() {
        Log.i(TAG, "onListenerDisconnected");
        isRunning = false;
    }

    private void registerReceiver() {
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unregisterReceiver() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.i(TAG, "onNotificationPosted");
        if (!isEnabled()) return;

        registerReceiver();
    }

    boolean isEnabled() {
        return (
                settings.getBoolean("enabled", true)
                && settings.getBoolean("FlipActionNotification", true)
        );
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        if (!isEnabled()) return;

        registerReceiver();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(
                    event.values, 0, accelerometerReading, 0, accelerometerReading.length
            );
            unregisterReceiver();
            manageDnd();
        }
    }

    private boolean wasSetToSilent = false;
    private void manageDnd() {
        if (accelerometerReading[2] < -9.) {
            if (!audioManager.isSilent()) {
                audioManager.setRingerModeSilent();
                wasSetToSilent = true;
            }
        } else {
            if (wasSetToSilent) {
                wasSetToSilent = false;
                audioManager.restoreRingerMode();
            }
        }

        if (wasSetToSilent) {
            handler.postDelayed(checkOrientation, 120000);
        }
    }
}
