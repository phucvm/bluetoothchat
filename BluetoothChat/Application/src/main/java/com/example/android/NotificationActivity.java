package com.example.android;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;

import com.example.android.bluetoothchat.R;

public class NotificationActivity extends Activity {

    Button btNotification;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        btNotification = (Button) findViewById(R.id.bt_notify);
        btNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduleNotification(NotificationActivity.this,1,0);
            }
        });
    }

    public static void scheduleNotification(Context context, long delay, int notificationId) {//delay is after how much time(in millis) from current time you want to schedule the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle("Notice")
                .setContentText("Times up!")
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_launcher)).getBitmap())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        Intent intent = new Intent(context, com.example.android.bluetoothchat.MyNotificationPublisher.class);
        PendingIntent activity = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(activity);

        Notification notification = builder.build();

        Intent notificationIntent = new Intent(context, com.example.android.bluetoothchat.MyNotificationPublisher.class);
        notificationIntent.putExtra(com.example.android.bluetoothchat.MyNotificationPublisher.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(com.example.android.bluetoothchat.MyNotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }
}
