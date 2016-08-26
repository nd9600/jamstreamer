package com.leokomarov.jamstreamer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.discography.albums.AlbumsByNameAdapter;
import com.leokomarov.jamstreamer.discography.tracks.TracksByNameAdapter;
import com.leokomarov.jamstreamer.playlist.PlaylistAdapter;

public class generalUtils {

    //Stores where you are going in the app hierarchy
    public static void putHierarchy(Context context, String hierarchy){
        SharedPreferences hierarchyPreference = context.getSharedPreferences(context.getString(R.string.hierarchyPreferences), 0);
        SharedPreferences.Editor hierarchyEditor = hierarchyPreference.edit();
        hierarchyEditor.putString("hierarchy", hierarchy);
        hierarchyEditor.apply();
    }

    //Clears the checkbox list and counter for the relevant adapter
    public static void clearCheckboxes(int requestCode){
        System.out.println("");
        System.out.println("Clearing checkboxes, requestCode: " + requestCode);
        if (requestCode == 1) {
            PlaylistAdapter.listOfCheckboxes.clear();
            PlaylistAdapter.tickedCheckboxCounter = 0;
        }
        if (requestCode == 2) {
            AlbumsByNameAdapter.listOfCheckboxes.clear();
            AlbumsByNameAdapter.tickedCheckboxCounter = 0;
        }
        if (requestCode == 3) {
            TracksByNameAdapter.listOfCheckboxes.clear();
            TracksByNameAdapter.tickedCheckboxCounter = 0;
        }
    }

}
