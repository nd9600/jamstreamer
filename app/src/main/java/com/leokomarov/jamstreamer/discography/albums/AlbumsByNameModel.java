package com.leokomarov.jamstreamer.discography.albums;

import java.util.HashMap;

public class AlbumsByNameModel {
    private HashMap<String, String> albumMap;
    private boolean selected;

    public AlbumsByNameModel(HashMap<String, String> albumMap) {
        this.albumMap = albumMap;
        selected = false;
    }

    public String getAlbumName(){
        return albumMap.get("albumName");
    }

    public String getAlbumArtist(){
        return albumMap.get("albumArtist");
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}