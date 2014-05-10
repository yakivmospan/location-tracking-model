package com.ym.model.location;

import com.defaultproject.R;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class LocationService6 extends Service {

    private int mId;

    // ...


    @Override
    public void onCreate() {
        super.onCreate();
        // ...
        sendNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void sendNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Location Tracking")
                        .setContentText("Location Service is running")
                        .setOngoing(true); // this is the important line;

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
    }
}
