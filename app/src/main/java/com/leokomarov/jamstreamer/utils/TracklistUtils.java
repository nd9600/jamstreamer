package com.leokomarov.jamstreamer.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.leokomarov.jamstreamer.playlist.PlaylistList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class TracklistUtils extends AsyncTask<Object, Integer, Void> {

    //Saves the tracklist to memory
    //tracklistObject is a class with the tracklist as a variable inside it
    public static void saveTracklist(ComplexPreferences trackPreferences, ArrayList<HashMap<String, String>> tracklist){
        Log.v("TracklistUtils", "saveTracklist");
        PlaylistList tracklistObject = new PlaylistList();
        tracklistObject.setTrackList(tracklist);
        trackPreferences.putObject("tracks", tracklistObject);
        trackPreferences.commit();
    }

    //creates a shuffled list of integers the same size as the tracklist
    //and stores it in memory
    public static void shuffleTrackList(ComplexPreferences trackPreferences, int size){
        Log.v("TracklistUtils", "shuffleTrackList");

        Random random = new Random();
        int min = 0;

        int[] listOfInts = new int[size];
        for (int i = 0; i < size; i++){
            int j = random.nextInt((i - min) + 1) + min;
            if (j != i){
                listOfInts[i] = listOfInts[j];
            }
            listOfInts[j] = i;
        }

        PlaylistList playlistList = new PlaylistList();
        playlistList.setShuffleList(listOfInts);
        trackPreferences.putObject("shuffledListOfInts", playlistList);
    }

    //Restores the playlist from memory
    //See ComplexPreferences docs on Github
    @SuppressWarnings("unchecked")
    public static ArrayList<HashMap<String, String>> restoreTracklist(ComplexPreferences trackPreferences){
        Log.v("TracklistUtils", "restoreTracklist");
        ArrayList<HashMap<String, String>> tracklist = new ArrayList<>();
        PlaylistList trackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
        if (trackPreferencesObject != null){
            tracklist = trackPreferencesObject.getTracklist();
        }
        return tracklist;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Void doInBackground(Object... objects) {
        ComplexPreferences trackPreferences = (ComplexPreferences) objects[0];
        String operation = (String) objects[1];
        switch (operation) {
            case "save":
                Log.v("doInBackground", "save");
                ArrayList<HashMap<String, String>> tracklist = (ArrayList<HashMap<String, String>>) objects[2];
                saveTracklist(trackPreferences, tracklist);
                break;
            case "shuffle":
                Log.v("doInBackground", "shuffle");
                int size = (int) objects[2];
                shuffleTrackList(trackPreferences, size);
                break;
            case "saveAndShuffle":
                Log.v("doInBackground", "saveAndShuffle");
                tracklist = (ArrayList<HashMap<String, String>>) objects[2];
                saveTracklist(trackPreferences, tracklist);
                shuffleTrackList(trackPreferences, tracklist.size());
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void nothing) {
    }

}
