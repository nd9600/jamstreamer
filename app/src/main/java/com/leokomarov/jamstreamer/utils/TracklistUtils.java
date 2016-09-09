package com.leokomarov.jamstreamer.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.leokomarov.jamstreamer.playlist.PlaylistList;

import java.util.ArrayList;
import java.util.HashMap;

public class TracklistUtils extends AsyncTask<Object, Integer, Void> {

    //Restores the playlist from memory
    //See ComplexPreferences docs on Github
    @SuppressWarnings("unchecked")
    public static ArrayList<HashMap<String, String>> restoreTracklist(ComplexPreferences trackPreferences){
        Log.v("TracklistUtils", "restoreTracklist");
        ArrayList<HashMap<String, String>> tracklist = new ArrayList<>();
        PlaylistList playlistList = trackPreferences.getObject("tracks", PlaylistList.class);
        if (playlistList != null){
            tracklist = playlistList.getTracklist();
        }
        return tracklist;
    }

    //Saves the tracklist to memory
    //playlistList is a class with the tracklist as a variable inside it
    @SuppressWarnings("unchecked")
    @Override
    protected Void doInBackground(Object... objects) {
        ComplexPreferences trackPreferences = (ComplexPreferences) objects[0];
        ArrayList<HashMap<String, String>> tracklist = (ArrayList<HashMap<String, String>>) objects[1];

        Log.v("TracklistUtils", "saveTracklist");
        PlaylistList playlistList = trackPreferences.getObject("tracks", PlaylistList.class);
        if (playlistList == null) {
            playlistList = new PlaylistList();
        }
        playlistList.setTrackList(tracklist);
        trackPreferences.putObject("tracks", playlistList);
        trackPreferences.commit();

        return null;
    }

    @Override
    protected void onPostExecute(Void nothing) {
    }

}
