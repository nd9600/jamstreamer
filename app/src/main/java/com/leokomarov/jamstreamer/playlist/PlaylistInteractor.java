package com.leokomarov.jamstreamer.playlist;

import android.os.Bundle;

import com.leokomarov.jamstreamer.utils.ComplexPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlaylistInteractor {
    private final String TAG_TRACKLIST = "trackListSaved";
    private List<PlaylistTrackModel> playlistTrackModel = new ArrayList<>();

    public List<PlaylistTrackModel> getPlaylistTrackModel(){
        return playlistTrackModel;
    }

    public void clearPlaylistTrackModel(){
        playlistTrackModel.clear();
    }

    public void setPlaylistTrackModel(ArrayList<HashMap<String, String>> trackList){
        for (HashMap<String, String> map : trackList) {
            playlistTrackModel.add(new PlaylistTrackModel(map));
        }
    }

    public void saveTracklist(ComplexPreferences trackPreferences, ArrayList<HashMap<String, String>> trackList){
        PlaylistList trackListObject = new PlaylistList();
        trackListObject.setTrackList(trackList);
        trackPreferences.putObject("tracks", trackListObject);
        trackPreferences.commit();
    }

    //Restores the playlist from memory
    //See ComplexPreferences docs on Github
    public ArrayList<HashMap<String, String>> restoreTracklist(Bundle savedInstanceState, ComplexPreferences trackPreferences){
        if (savedInstanceState != null) {
            @SuppressWarnings("unchecked")
            ArrayList<HashMap<String, String>> trackList = (ArrayList<HashMap<String, String>>)savedInstanceState.getSerializable(TAG_TRACKLIST);
            return trackList;
        } else {
            PlaylistList trackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
            if (trackPreferencesObject != null){
                return trackPreferencesObject.trackList;
            }
            else {
                return null;
            }
        }
    }

    public void shuffleTrackList(ComplexPreferences trackPreferences){

        PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
        ArrayList<HashMap<String, String>> trackList = shuffledTrackPreferencesObject.trackList;

        Collections.shuffle(trackList);
        PlaylistList shuffledTrackListObject = new PlaylistList();
        shuffledTrackListObject.setTrackList(trackList);
        trackPreferences.putObject("shuffledTracks", shuffledTrackListObject);
        trackPreferences.commit();
    }
}
