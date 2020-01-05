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
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private boolean accelerometerRead = false;
    private boolean magnetometerRead = false;

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
        accelerometerRead = false;
        magnetometerRead = false;
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_NORMAL);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unregisterReceiver() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.i(TAG, "onNotificationPosted");
        if (!settings.getBoolean("enabled", true)) return;

        registerReceiver();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        if (!settings.getBoolean("enabled", true)) return;

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
            accelerometerRead = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(
                    event.values, 0, magnetometerReading, 0, magnetometerReading.length
            );
            magnetometerRead = true;
        }
        if (accelerometerRead && magnetometerRead) {
            unregisterReceiver();
            updateOrientationAngles();
            manageDnd();
        }
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
                rotationMatrix, null, accelerometerReading, magnetometerReading
        );

        // "mRotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        Log.d(
                TAG,
                String.format(
                        "%3.2f %3.2f %3.2f",
                        accelerometerReading[0],
                        accelerometerReading[1],
                        accelerometerReading[2]
                )
        );
    }

    private boolean wasSetToSilent = false;
    private void manageDnd() {
        if (accelerometerReading[2] < -9.) {
            if (!audioManager.isSilent()) {
                audioManager.setRingerModeSilent();
                wasSetToSilent = true;
            }
        } else {
            wasSetToSilent = false;
            audioManager.restoreRingerMode();
        }
        if (wasSetToSilent) {
            handler.postDelayed(checkOrientation, 120000);
        }
    }
}
