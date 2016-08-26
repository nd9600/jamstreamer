package com.leokomarov.jamstreamer.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListInteractor {
    //listData is the data used to generate the listview,
    //made up of individual records stored as TrackModels
    private List<TrackModel> listData = new ArrayList<>();

    //Returns the playlist track data
    public List<TrackModel> getListData(){
        return listData;
    }

    //Clears the playlist track data
    public void listData(){
        listData.clear();
    }

    //Sets the playlist track data
    public void setListData(ArrayList<HashMap<String, String>> trackList){
        listData.clear();
        for (HashMap<String, String> map : trackList) {
            listData.add(new TrackModel(map));
        }
    }
}
