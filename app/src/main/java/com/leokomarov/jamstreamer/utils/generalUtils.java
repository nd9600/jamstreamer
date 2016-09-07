package com.leokomarov.jamstreamer.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.leokomarov.jamstreamer.R;

public class generalUtils {

    public static int notificationID = 46798;

    //Stores where you are going in the app hierarchy
    public static void putHierarchy(Context context, String hierarchy){
        SharedPreferences hierarchyPreference = context.getSharedPreferences(context.getString(R.string.hierarchyPreferences), 0);
        SharedPreferences.Editor hierarchyEditor = hierarchyPreference.edit();
        hierarchyEditor.putString("hierarchy", hierarchy);
        hierarchyEditor.apply();
    }

    public static void closeNotification(Context context){
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationID);
    }

}
