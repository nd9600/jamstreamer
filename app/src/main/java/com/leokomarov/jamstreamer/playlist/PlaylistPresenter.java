package com.leokomarov.jamstreamer.playlist;

import android.os.Bundle;
import android.widget.ListView;

import com.leokomarov.jamstreamer.ComplexPreferences;
import com.leokomarov.jamstreamer.R;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistPresenter {

    private ListView view;
    private PlaylistInteractor interactor;
    private ComplexPreferences trackPreferences;
    private Bundle savedInstanceState;

    public PlaylistPresenter(ListView playlistView, Bundle savedInstanceState, PlaylistInteractor playlistInteractor, ComplexPreferences trackPreferences){
        this.view = playlistView;
        this.savedInstanceState = savedInstanceState;
        this.interactor = playlistInteractor;
        this.trackPreferences = trackPreferences;
    }

    public ArrayList<HashMap<String, String>> restoreTracklist(){
        return interactor.restoreTracklist(savedInstanceState, trackPreferences);
    }

    public void shuffleTracklist(){
        interactor.shuffleTrackList(trackPreferences);
    }


}
