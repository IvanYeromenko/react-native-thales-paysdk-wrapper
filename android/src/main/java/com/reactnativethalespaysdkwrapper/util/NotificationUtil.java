package com.reactnativethalespaysdkwrapper.util;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;


public class NotificationUtil {


    public static Notification getNotification(final Context context,
                                               String contentMessage,
                                               String channelId) {
        //AppLogger.d("TAG", "Notification is initialized in foreground");

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = showChannel(context, channelId);
        } else {
            builder = new Notification.Builder(context);
        }
        return builder.setAutoCancel(false)
                .setOngoing(true)
               // .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentTitle("Wrapper")
                .setContentText(contentMessage)
                .setPriority(Notification.PRIORITY_MAX)
                .build();
    }

    @TargetApi(26)
    private static Notification.Builder showChannel(final Context context,
                                                    String channelId) {

        NotificationChannel channel = new NotificationChannel(channelId,
                channelId, NotificationManager.IMPORTANCE_LOW);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .createNotificationChannel(channel);
        //For android 8 and above device
        return new Notification.Builder(context, channelId);
    }

}
