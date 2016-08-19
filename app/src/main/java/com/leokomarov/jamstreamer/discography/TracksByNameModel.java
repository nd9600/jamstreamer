package com.leokomarov.jamstreamer.discography;

import java.util.HashMap;

public class TracksByNameModel {
    private HashMap<String, String> trackMap;
    private boolean selected;

    public TracksByNameModel(HashMap<String, String> trackMap) {
        this.trackMap = trackMap;
        selected = false;
    }

    public String getTrackNameAndDuration(){
        return trackMap.get("trackName") + " - " + trackMap.get("trackDuration");
    }

    public String getTrackArtistAndAlbum(){
        return trackMap.get("trackArtist") + " - " + trackMap.get("trackAlbum");
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
