package com.fireninjas.firebasehack.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.fireninjas.firebasehack.MainActivity;
import com.fireninjas.firebasehack.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;


/**
 * Created by ashok.kumar on 28/06/17.
 */

public class WaterReminderReceiver extends BroadcastReceiver {
    public static final String CONTENT_KEY = "contentText";

    int count = 0;

    GoogleApiClient mGoogleApiClient;


    @Override
    public void onReceive(Context context, Intent intent) {

        Intent intent2 = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        count++;
        intent.putExtra("count",count);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent2,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        if (mGoogleApiClient == null)
            return;


        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setAutoCancel(true)   //Automatically delete the notification
                .setSmallIcon(R.drawable.water_bottle_flat) //Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle("Time to hydrate")
                .setContentText("Drink a glass of water now\n " +
                        "Todays water drink count: "+ count)
                .setCategory(Notification.CATEGORY_REMINDER)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(defaultSoundUri);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, notificationBuilder.build());
        Toast.makeText(context, "Repeating Alarm Received", Toast.LENGTH_SHORT).show();
    }
}