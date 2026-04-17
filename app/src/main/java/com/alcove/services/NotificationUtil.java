package com.alcove.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.alcove.MainActivity;
import com.alcove.R;

public class NotificationUtil {

    private static final String CHANNEL_ID = "reading_reminders_channel";
    private static final String CHANNEL_NAME = "Reading Reminders";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Reminds you to keep reading your books");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showReadingReminder(Context context, int booksCount) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                // Use a default icon since we can't be sure of the vector icons you have
                .setSmallIcon(android.R.drawable.ic_dialog_info) 
                .setContentTitle("Time to Read! \uD83D\uDCDA")
                .setContentText("You have " + booksCount + " book(s) waiting for you on your shelf!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(1001, builder.build());
        } catch (SecurityException e) {
            // Missing POST_NOTIFICATIONS permission on Android 13+
            e.printStackTrace();
        }
    }
}

