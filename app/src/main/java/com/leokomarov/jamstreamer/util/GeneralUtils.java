package com.leokomarov.jamstreamer.util;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GeneralUtils {

    public static int notificationID = 46798;

    //Stores where you are going in the app hierarchy
    public static void putHierarchy(Context context, String hierarchy){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor hierarchyEditor = sharedPreferences.edit();
        hierarchyEditor.putString("hierarchy", hierarchy);
        hierarchyEditor.apply();
    }

    public static void closeNotification(Context context){
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationID);
    }

}
