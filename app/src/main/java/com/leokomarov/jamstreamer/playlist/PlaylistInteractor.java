package com.leokomarov.jamstreamer.playlist;

import android.os.Bundle;

import com.leokomarov.jamstreamer.ComplexPreferences;
import com.leokomarov.jamstreamer.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PlaylistInteractor {
    private final String TAG_TRACKLIST = "trackListSaved";

    //Restores the playlist from memory
    //See ComplexPreferences docs on Github
    private ArrayList<HashMap<String, String>> restoreTracklist(Bundle savedInstanceState){
        if (savedInstanceState != null) {
            @SuppressWarnings("unchecked")
            //ArrayList<HashMap<String, String>> trackList = (ArrayList<HashMap<String,String>>)savedInstanceState.get(TAG_TRACKLIST);
                    ArrayList<HashMap<String, String>> trackList = (ArrayList<HashMap<String, String>>)savedInstanceState.getSerializable(TAG_TRACKLIST);
            return trackList;
        }
        else {
            ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
                    getString(R.string.trackPreferences), MODE_PRIVATE);
            PlaylistList trackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
            if (trackPreferencesObject != null){
                return trackPreferencesObject.trackList;
            }
            else {
                return null;
            }

        }
    }

    private void shuffleTrackList(){

        PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
        ArrayList<HashMap<String, String>> trackList = shuffledTrackPreferencesObject.trackList;

        Collections.shuffle(trackList);
        PlaylistList shuffledTrackListObject = new PlaylistList();
        shuffledTrackListObject.setTrackList(trackList);
        trackPreferences.putObject("shuffledTracks", shuffledTrackListObject);
        trackPreferences.commit();
    }
}
