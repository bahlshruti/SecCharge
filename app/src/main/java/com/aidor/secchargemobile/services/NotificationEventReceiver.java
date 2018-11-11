package com.aidor.secchargemobile.services;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import com.aidor.secchargemobile.model.UpdatedSocModel;
import com.aidor.secchargemobile.rest.RestClientReservation;

import java.util.Calendar;
import java.util.Date;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NotificationEventReceiver extends WakefulBroadcastReceiver {

    private static final String ACTION_START_NOTIFICATION_SERVICE = "ACTION_START_NOTIFICATION_SERVICE";
    private static final String ACTION_DELETE_NOTIFICATION = "ACTION_DELETE_NOTIFICATION";
    private static final int NOTIFICATIONS_INTERVAL_IN_HOURS = 20000;

    static String userId;
    static String userAction;

    public static void setupAlarm(Context context,Intent intent) {
        userId = intent.getStringExtra("USER_ID");
        userAction = intent.getStringExtra("ACTION");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = getStartPendingIntent(context);
        if(userAction.equals("start")) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    getTriggerAt(new Date()),
                    NOTIFICATIONS_INTERVAL_IN_HOURS,
                    alarmIntent);

        } else {
            alarmManager.cancel(alarmIntent);
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Intent serviceIntent = null;
        if (ACTION_START_NOTIFICATION_SERVICE.equals(action)) {
            Log.i(getClass().getSimpleName(), "onReceive from alarm, starting notification service");


            // TODO code to be written here for webservice
            RestClientReservation.get().getUpdatedSOC(userId, new Callback<UpdatedSocModel>() {
                @Override
                public void success(UpdatedSocModel updatedSocModel, Response response) {
                    int checkSoc = Integer.parseInt(updatedSocModel.getStatus());
                    if (checkSoc<100) {
                       // Toast.makeText(context, "Battery Charged : " + checkSoc, Toast.LENGTH_LONG).show();
                    } else {
                       // Toast.makeText(context,"Fully Charged", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                   // Toast.makeText(context,"Error try again",Toast.LENGTH_SHORT).show();
                }
            });


        } else if (ACTION_DELETE_NOTIFICATION.equals(action)) {
            Log.i(getClass().getSimpleName(), "onReceive delete notification action, starting notification service to handle delete");
            serviceIntent = NotificationIntentService.createIntentDeleteNotification(context);
        }

        if (serviceIntent != null) {
            startWakefulService(context, serviceIntent);
        }
    }

    private static long getTriggerAt(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        //calendar.add(Calendar.HOUR, NOTIFICATIONS_INTERVAL_IN_HOURS);
        return calendar.getTimeInMillis();
    }

    private static PendingIntent getStartPendingIntent(Context context) {
        Intent intent = new Intent(context, NotificationEventReceiver.class);
        intent.setAction(ACTION_START_NOTIFICATION_SERVICE);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getDeleteIntent(Context context) {
        Intent intent = new Intent(context, NotificationEventReceiver.class);
        intent.setAction(ACTION_DELETE_NOTIFICATION);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
