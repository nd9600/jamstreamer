package com.leokomarov.jamstreamer.searches.tracks;

import android.view.LayoutInflater;

import com.leokomarov.jamstreamer.common.CustomListAdapter;
import com.leokomarov.jamstreamer.common.TrackModel;

import java.util.HashMap;
import java.util.List;

public class TracksAdapter extends CustomListAdapter {
    public TracksAdapter(TracksController tracksController, List<TrackModel> listData, LayoutInflater inflater) {
        super(tracksController, tracksController, listData, inflater);
    }

    //update the viewHolder for a track
    @Override
    public void updateViewHolder(ViewHolder viewHolder, int position) {
        HashMap<String, String> trackMap = listData.get(position).getMap();
        String trackNameAndDuration = trackMap.get("trackName") + " - " + trackMap.get("trackDuration");
        String trackArtistAndAlbum = trackMap.get("trackArtist") + " - " + trackMap.get("trackAlbum");
        viewHolder.textView1.setText(trackNameAndDuration);
        viewHolder.textView2.setText(trackArtistAndAlbum);
        viewHolder.checkbox.setChecked(listOfCheckboxes.get(position, false));
    }
}
