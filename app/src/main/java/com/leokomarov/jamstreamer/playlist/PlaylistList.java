package com.leokomarov.jamstreamer.playlist;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistList {
    //trackListObject is a class with the trackList as a variable inside it
	public ArrayList<HashMap<String, String>> trackList;
	
    public void setTrackList(ArrayList<HashMap<String, String>> trackList) {
        this.trackList = trackList;
    }
}