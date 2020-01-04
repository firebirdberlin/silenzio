package com.firebirdberlin.silenzio;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.List;


public class SetRingerService extends Service implements SensorEventListener {
    private static String TAG = "SetRingerService";
    private static int NOT_ON_TABLE = 0;
    private static int DISPLAY_FACE_UP = 1;
    private static int DISPLAY_FACE_DOWN = 2;
    private static int SENSOR_DELAY = 50000; // us = 50 ms
    private static float MAX_POCKET_BRIGHTNESS = 10.f;
    PowerManager.WakeLock wakelock;
    private Settings settings = null;
    private mAudioManager audiomanager = null;
    private SensorManager sensorManager = null;
    private Sensor accelerometerSensor = null;
    private Sensor proximitySensor = null;
    private Sensor lightSensor = null;
    public static boolean running = false;
    private boolean DeviceIsCovered = false;
    private float ambientLight = 0;//SensorManager.LIGHT_SUNLIGHT_MAX;
    private int isOnTable = NOT_ON_TABLE;
    private Float firstProximityValue = null;

    public static void start(Context context) {
        Intent i =  new Intent(context, SetRingerService.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        } else {
            context.startService(i);
        }
    }

    public static void stop(Context context) {
        Intent i =  new Intent(context, SetRingerService.class);
        context.stopService(i);
    }

    private final BroadcastReceiver phoneStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if ( state != TelephonyManager.EXTRA_STATE_RINGING) {
                audiomanager.unmute();
                stopSelf();
            }
        }
    };
    private boolean DeviceUnCovered = false;
    private int shake_left = 0;
    private int shake_right = 0;
    @Override
    public void onCreate() {
        callStartForeground();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakelock.acquire();

        IntentFilter filter = new IntentFilter();
        //filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.addAction(android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(phoneStateReceiver, filter);

        settings = new Settings(this);
        audiomanager = new mAudioManager(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensorList.size() > 0) {
            accelerometerSensor = sensorList.get(0);
        }
    }

    void callStartForeground() {
        Utility.createNotificationChannels(getApplicationContext());
        Notification note = buildNotification(getString(R.string.notificationChannelNameRingerService));
        startForeground(Silenzio.NOTIFICATION_ID_RINGER_SERVICE, note);
    }

    private Notification buildNotification(String message) {
        NotificationCompat.Builder noteBuilder =
                Utility.buildNotification(this, Silenzio.NOTIFICATION_CHANNEL_ID_RINGER_SERVICE)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message)
                        .setSmallIcon(R.drawable.ic_hearing)
                        .setPriority(NotificationCompat.PRIORITY_MIN);

        Notification note = noteBuilder.build();

        note.flags |= Notification.FLAG_NO_CLEAR;
        note.flags |= Notification.FLAG_FOREGROUND_SERVICE;

        return note;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");

        unregisterReceiver(phoneStateReceiver);
        sensorManager.unregisterListener(this);

        if (wakelock != null && wakelock.isHeld()) {
            wakelock.release();
        }
        audiomanager.unmute();
        running = false;
    }


    private void registerListenerForSensor(Sensor sensor) {
        Log.i(TAG, "registerListener: " + sensor.getName());
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SENSOR_DELAY, SENSOR_DELAY / 2);
        }
    }

    private boolean isScreenOn() {
        if (Build.VERSION.SDK_INT >= 20) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            return pm.isInteractive();
        }
        return deprecated_isScreenOn();
    }

    @SuppressWarnings("deprecation")
    private boolean deprecated_isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    private boolean isCovered() {
        return (DeviceIsCovered || (ambientLight < MAX_POCKET_BRIGHTNESS));
    }

    private boolean shouldRing() {
        Log.i(TAG, "shouldRing()");
        Log.i(TAG, " settings.FlipAction = " + settings.FlipAction);
        Log.i(TAG, " isOnTable = " + isOnTable);
        Log.i(TAG, " ambientLight = " + ambientLight);
        Log.i(TAG, " DeviceIsCovered = " + DeviceIsCovered);
        return (!(settings.FlipAction
                && isOnTable == DISPLAY_FACE_DOWN
                && isCovered()));
        //&& ambientLight < MAX_POCKET_BRIGHTNESS));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // called when sensor value have changed
    @Override
    public void onSensorChanged(SensorEvent event) {
        // The Proximity sensor returns a single value either 0 or 5 (also 1 depends on Sensor manufacturer).
        // 0 for near and 5 for far
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (firstProximityValue == null) {
                firstProximityValue = event.values[0];
                DeviceIsCovered = (event.values[0] == 0);
                Log.i(TAG, "Device is covered");
            } else if (DeviceIsCovered) {
                DeviceUnCovered = (event.values[0] > 0.f);
            }

        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            ambientLight = event.values[0]; // simply log the value
            if ((settings.PullOutAction) && isCovered()) {
                // Attention do not choose the value to low,
                // noise produces values up to 12 lux on my GNex
                if (event.values[0] >= 15.f) { // uncovered
                    Log.i(TAG, "device is uncovered");
                    audiomanager.mute();
                    DeviceIsCovered = false;
                }
            }
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            handleAccelerometerActions(event);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() ( running = " + running + " )");
        callStartForeground();
        // no action needed
        if (audiomanager.isSilent() || audiomanager.isVibration() || (!settings.enabled)) {
            stopSelf();
            return Service.START_NOT_STICKY;
        }

        if (running) {
            // Don't bother the service while already running.
            // The volume is beeing set right now.
            Log.i(TAG, "Declined ! Service already running");
            return Service.START_NOT_STICKY;
        }
        running = true;

        DeviceIsCovered = false;

        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        registerListenerForSensor(proximitySensor);
        registerListenerForSensor(lightSensor);
        registerListenerForSensor(accelerometerSensor);
        audiomanager.mute();

        return Service.START_NOT_STICKY;
    }

    private void handleAccelerometerActions(SensorEvent event) {

        float x_value = event.values[0];
        float z_value = event.values[2];
        // if acceleration in x and y direction is too strong, device is moving
        if (settings.ShakeAction) {
            if (x_value < -12.) shake_left++;
            if (x_value > 12.) shake_right++;
            // shake to silence
            if ((shake_left >= 1 && shake_right >= 2) ||
                    (shake_left >= 2 && shake_right >= 1)) {
                audiomanager.mute();
                shake_left = shake_right = 0;
                Log.i(TAG, "shake detected");
            }
        }

        if (settings.FlipAction) {
            if (z_value > -10.3 && z_value < -9.3) { // display face down
                audiomanager.mute();
            }
        }
    }
}
