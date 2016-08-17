package com.leokomarov.jamstreamer.playlist;

import java.util.HashMap;

public class PlaylistModel {
    private HashMap<String, String> trackMap;
    private boolean selected;

    public PlaylistModel(HashMap<String, String> trackMap) {
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
