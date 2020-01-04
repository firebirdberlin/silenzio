package com.firebirdberlin.silenzio;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

class Utility {

    private static final String TAG = "Utility";

    static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);

        if (notificationManager == null) {
            return;
        }
        NotificationChannel channelRingerService = prepareNotificationChannel(
                context,
                Silenzio.NOTIFICATION_CHANNEL_ID_RINGER_SERVICE,
                R.string.notificationChannelNameRingerService,
                R.string.notificationChannelDescRingerService,
                NotificationManager.IMPORTANCE_MIN
        );
        notificationManager.createNotificationChannel(channelRingerService);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static NotificationChannel prepareNotificationChannel(
            Context context, String channelName, int idName, int idDesc, int importance) {
        String name = context.getString(idName);
        String description = context.getString(idDesc);
        NotificationChannel mChannel = new NotificationChannel(channelName, name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(false);
        mChannel.enableVibration(false);
        mChannel.setSound(null, null);
        mChannel.setShowBadge(false);
        return mChannel;
    }

    static NotificationCompat.Builder buildNotification(Context context, String channel_id) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return new NotificationCompat.Builder(context);
        } else {
            return new NotificationCompat.Builder(context, channel_id);
        }
    }
}

