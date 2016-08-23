package com.leokomarov.jamstreamer.common;

import java.util.HashMap;

public class TrackModel {
    private HashMap<String, String> map;

    public TrackModel(HashMap<String, String> map){
        this.map = map;
    }

    public HashMap<String, String> getMap(){
        return map;
    }
}
