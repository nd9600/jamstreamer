package com.leokomarov.jamstreamer.util;

import android.os.AsyncTask;
import android.util.Log;

import com.leokomarov.jamstreamer.MainActivity;
import com.leokomarov.jamstreamer.playlist.PlaylistList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class TracklistUtils extends AsyncTask<Object, Integer, Void> {

    public static ArrayList<HashMap<String, String>> tracklist;
    public static int[] shufflelist;

    //set tracklist with parameter, or restore from memory if parameter is null
    public static void updateTracklist(ArrayList<HashMap<String, String>> tracklist) {
        if (tracklist == null){
            TracklistUtils.tracklist = restoreTracklist();
        } else {
            TracklistUtils.tracklist = tracklist;
        }

        updateShufflelist();
    }

    public static void updateShufflelist() {
        Random random = new Random();
        int min = 0;

        //creates a shuffled list of integers from 0 to tracklist.size()
        shufflelist = new int[tracklist.size()];
        for (int i = 0; i < tracklist.size(); i++){
            int j = random.nextInt((i - min) + 1) + min;
            if (j != i){
                shufflelist[i] = shufflelist[j];
            }
            shufflelist[j] = i;
        }

        //puts the current track at the front
        int indexPosition = MainActivity.sharedPreferences.getInt("indexPosition", 0);
        for (int i = 0; i < shufflelist.length; i++) {
            if (shufflelist[i] == indexPosition) {
                shufflelist[i] = shufflelist[0];
                shufflelist[0] = indexPosition;
                break;
            }
        }
    }

    //Restores the playlist from memory
    //See ComplexPreferences docs on Github
    @SuppressWarnings("unchecked")
    public static ArrayList<HashMap<String, String>> restoreTracklist(){
        Log.v("TracklistUtils", "restoreTracklist");
        ArrayList<HashMap<String, String>> tracklist = new ArrayList<>();
        PlaylistList playlistList = MainActivity.trackPreferences.getObject("tracks", PlaylistList.class);
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
        ArrayList<HashMap<String, String>> tracklist = (ArrayList<HashMap<String, String>>) objects[0];

        Log.v("TracklistUtils", "saveTracklist");
        PlaylistList playlistList = MainActivity.trackPreferences.getObject("tracks", PlaylistList.class);
        if (playlistList == null) {
            playlistList = new PlaylistList();
        }
        playlistList.setTrackList(tracklist);
        MainActivity.trackPreferences.putObject("tracks", playlistList);
        MainActivity.trackPreferences.commit();

        return null;
    }

    @Override
    protected void onPostExecute(Void nothing) {
    }

}
