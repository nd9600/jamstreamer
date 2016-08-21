package com.leokomarov.jamstreamer.utils;

import android.os.Bundle;

import com.leokomarov.jamstreamer.playlist.PlaylistList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class tracklistUtils {
    private static final String TAG_TRACKLIST = "tracklist_saved";

    //Saves the tracklist to memory
    //trackListObject is a class with the trackList as a variable inside it
    public static void saveTracklist(ComplexPreferences trackPreferences, ArrayList<HashMap<String, String>> trackList){
        PlaylistList trackListObject = new PlaylistList();
        trackListObject.setTrackList(trackList);
        trackPreferences.putObject("tracks", trackListObject);
        trackPreferences.commit();
    }

    //Restores the playlist from memory
    //See ComplexPreferences docs on Github
    @SuppressWarnings("unchecked")
    public static ArrayList<HashMap<String, String>> restoreTracklist(Bundle savedInstanceState, ComplexPreferences trackPreferences){
        ArrayList<HashMap<String, String>> tracklist = new ArrayList<>();
        if (savedInstanceState != null) {
            tracklist = (ArrayList<HashMap<String, String>>)savedInstanceState.getSerializable(TAG_TRACKLIST);
        } else {
            PlaylistList trackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
            if (trackPreferencesObject != null){
                tracklist = trackPreferencesObject.trackList;
            }
        }
        return tracklist;
    }

    //Shuffles the tracklist and stores it in memory separate from the unshuffled list
    public static void shuffleTrackList(ComplexPreferences trackPreferences){
        PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
        ArrayList<HashMap<String, String>> trackList = shuffledTrackPreferencesObject.trackList;

        Collections.shuffle(trackList);
        PlaylistList shuffledTrackListObject = new PlaylistList();
        shuffledTrackListObject.setTrackList(trackList);
        trackPreferences.putObject("shuffledTracks", shuffledTrackListObject);
        trackPreferences.commit();
    }
}
