package com.leokomarov.jamstreamer.ignored.playlist;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistList {
    //tracklistObject is a class with the tracklist as a variable inside it
	private ArrayList<HashMap<String, String>> tracklist;

    public void setTrackList(ArrayList<HashMap<String, String>> tracklist) {
        this.tracklist = tracklist;
    }

    public ArrayList<HashMap<String, String>> getTracklist(){
        return tracklist;
    }
}