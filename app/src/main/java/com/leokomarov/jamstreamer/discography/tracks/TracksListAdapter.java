package com.leokomarov.jamstreamer.discography.tracks;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.CustomListAdapter;
import com.leokomarov.jamstreamer.common.TrackModel;

import java.util.HashMap;
import java.util.List;


public class TracksListAdapter extends CustomListAdapter {
    private final List<TrackModel> listData;

    protected TracksListAdapter(TracksActivity tracksActivity, TracksByNamePresenter presenter) {
        super(tracksActivity, tracksActivity, presenter.getListData(), R.layout.tracks_by_name, R.id.tracks_by_name_checkBox, R.id.tracks_by_name_trackNameAndDuration, R.id.tracks_by_name_trackArtistAndAlbum);
        this.listData = presenter.getListData();
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