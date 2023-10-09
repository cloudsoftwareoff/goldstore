package com.cloudsoftware.goldstorex;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class PushNotificationSender {

    public static void sendLocalNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "order_channel"; // Replace with your channel ID
            String channelName = "Gold Store"; // Replace with your channel name
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, "order_channel"); // Replace with your channel ID
        } else {
            builder = new Notification.Builder(context);
        }

        builder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.logo) // Replace with your notification icon
                .setAutoCancel(true);

        int notificationId=72000;
        notificationManager.notify(notificationId, builder.build());
    }
}


