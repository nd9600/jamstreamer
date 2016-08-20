package com.leokomarov.jamstreamer.playlist;

import android.os.Bundle;

import com.leokomarov.jamstreamer.utils.ComplexPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlaylistInteractor {
    private final String TAG_TRACKLIST = "tracklist_saved";

    //playlistTrackData is the data used to generate the listview,
    //made up of individual tracks stored as PlaylistTrackModels
    private List<PlaylistTrackModel> playlistTrackData = new ArrayList<>();

    //Returns the playlist track data
    public List<PlaylistTrackModel> getPlaylistTrackData(){
        return playlistTrackData;
    }

    //Clears the playlist track data
    public void clearPlaylistTrackData(){
        playlistTrackData.clear();
    }

    //Sets the playlist track data
    public void setPlaylistTrackData(ArrayList<HashMap<String, String>> trackList){
        for (HashMap<String, String> map : trackList) {
            playlistTrackData.add(new PlaylistTrackModel(map));
        }
    }

    //Saves the tracklist to memory
    //trackListObject is a class with the trackList as a variable inside it
    public void saveTracklist(ComplexPreferences trackPreferences, ArrayList<HashMap<String, String>> trackList){
        PlaylistList trackListObject = new PlaylistList();
        trackListObject.setTrackList(trackList);
        trackPreferences.putObject("tracks", trackListObject);
        trackPreferences.commit();
    }

    //Restores the playlist from memory
    //See ComplexPreferences docs on Github
    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, String>> restoreTracklist(Bundle savedInstanceState, ComplexPreferences trackPreferences){
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
