package com.leokomarov.jamstreamer.playlist;

import java.util.HashMap;

public class PlaylistTrackModel {
    //Models every individual track with a hashmap and whether it is selected
    //Hashmap consists of a dictionary with track name, artist, album and duration
    private HashMap<String, String> trackMap;
    private boolean selected;

    public PlaylistTrackModel(HashMap<String, String> trackMap) {
        this.trackMap = trackMap;
        //selected = false;
    }

    public String getTrackNameAndDuration(){
        return trackMap.get("trackName") + " - " + trackMap.get("trackDuration");
    }

    public String getTrackArtistAndAlbum(){
        return trackMap.get("trackArtist") + " - " + trackMap.get("trackAlbum");
    }

    ///*
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    //*/


}
