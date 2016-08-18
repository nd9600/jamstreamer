package com.leokomarov.jamstreamer.playlist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.leokomarov.jamstreamer.utils.ComplexPreferences;
import com.leokomarov.jamstreamer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaylistPresenter {

    private ListView view;
    private PlaylistInteractor interactor;
    private ComplexPreferences trackPreferences;
    private Bundle savedInstanceState;

    private List<PlaylistTrackModel> playlistTrackModel = new ArrayList<>();

    public PlaylistPresenter(Context context, Bundle savedInstanceState, ListView playlistView, PlaylistInteractor playlistInteractor){
        this.savedInstanceState = savedInstanceState;
        this.view = playlistView;
        this.interactor = playlistInteractor;
        this.trackPreferences = ComplexPreferences.getComplexPreferences(context,
                context.getString(R.string.trackPreferences), context.MODE_PRIVATE);
    }

    public List<PlaylistTrackModel> getPlaylistTrackModel(){
        return playlistTrackModel;
    }

    public void clearPlaylistTrackModel(){
        playlistTrackModel.clear();
    }

    public void setPlaylistTrackModel(ArrayList<HashMap<String, String>> trackList){

        if (trackList == null) {
            ArrayList<HashMap<String, String>> restoredTracklist = restoreTracklist();
            if (restoredTracklist != null) {
                trackList = restoredTracklist;
            }
        }

        for (HashMap<String, String> map : trackList) {
            playlistTrackModel.add(new PlaylistTrackModel(map));
        }
    }

    public void saveTracklist(ArrayList<HashMap<String, String>> trackList){
        interactor.saveTracklist(trackPreferences, trackList);
    }

    public ArrayList<HashMap<String, String>> restoreTracklist(){
        return interactor.restoreTracklist(savedInstanceState, trackPreferences);
    }

    public void shuffleTracklist(){
        interactor.shuffleTrackList(trackPreferences);
    }

}
