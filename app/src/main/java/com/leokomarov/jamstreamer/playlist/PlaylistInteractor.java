package com.leokomarov.jamstreamer.playlist;

import com.leokomarov.jamstreamer.common.TrackModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaylistInteractor {
    //playlistTrackData is the data used to generate the listview,
    //made up of individual tracks stored as PlaylistTrackModels
    private List<TrackModel> playlistTrackData = new ArrayList<>();

    //Returns the playlist track data
    public List<TrackModel> getPlaylistTrackData(){
        return playlistTrackData;
    }

    //Clears the playlist track data
    public void clearPlaylistTrackData(){
        playlistTrackData.clear();
    }

    //Sets the playlist track data
    public void setPlaylistTrackData(ArrayList<HashMap<String, String>> trackList){
        playlistTrackData.clear();
        for (HashMap<String, String> map : trackList) {
            playlistTrackData.add(new TrackModel(map));
        }
    }
}
