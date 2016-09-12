package com.leokomarov.jamstreamer.common;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListInteractor {
    //listData is the data used to generate the listview,
    //made up of individual records stored as TrackModels
    private List<TrackModel> listData = new ArrayList<>();

    public ListInteractor(){
        Log.v("listI", "created");
    }

    //Returns the playlist track data
    public List<TrackModel> getListData(){
        return listData;
    }

    //Clears the playlist track data
    public void clearListData(){
        listData.clear();
    }

    //Sets the playlist track data
    public void setListData(ArrayList<HashMap<String, String>> tracklist){
        clearListData();
        for (HashMap<String, String> map : tracklist) {
            listData.add(new TrackModel(map));
        }
    }
}
