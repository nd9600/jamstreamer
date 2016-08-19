package com.leokomarov.jamstreamer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.discography.AlbumsByNameAdapter;
import com.leokomarov.jamstreamer.discography.TracksByNameAdapter;
import com.leokomarov.jamstreamer.playlist.PlaylistAdapter;

public class utils {

    //Stores where you are going in the app hierarchy
    public static void putHierarchy(Context context, String hierarchy){
        SharedPreferences hierarchyPreference = context.getSharedPreferences(context.getString(R.string.hierarchyPreferences), 0);
        SharedPreferences.Editor hierarchyEditor = hierarchyPreference.edit();
        hierarchyEditor.putString("hierarchy", hierarchy);
        hierarchyEditor.apply();
    }

    public static void clearCheckboxes(int requestCode){
        if (requestCode == 1) {
            PlaylistAdapter.playlistCheckboxList.clear();
            PlaylistAdapter.PlaylistCheckboxCount = 0;
        }
        if (requestCode == 2) {
            AlbumsByNameAdapter.AlbumsByNameCheckboxList.clear();
            AlbumsByNameAdapter.AlbumsByNameCheckboxCount = 0;
        }
        if (requestCode == 3) {
            TracksByNameAdapter.TracksByNameCheckboxList.clear();
            TracksByNameAdapter.TracksByNameCheckboxCount = 0;
        }
    }

}
