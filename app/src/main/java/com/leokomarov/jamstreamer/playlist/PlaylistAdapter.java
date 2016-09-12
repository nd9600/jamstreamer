package com.leokomarov.jamstreamer.playlist;

import android.view.LayoutInflater;

import com.leokomarov.jamstreamer.common.CustomListAdapter;
import com.leokomarov.jamstreamer.common.TrackModel;

import java.util.HashMap;
import java.util.List;

public class PlaylistAdapter extends CustomListAdapter {
    public PlaylistAdapter(PlaylistController playlistController, List<TrackModel> listData, LayoutInflater inflater) {
        super(playlistController, playlistController, listData, inflater);
    }

    //update the viewHolder for a track
    @Override
    public void updateViewHolder(CustomListAdapter.ViewHolder viewHolder, int position) {
        HashMap<String, String> trackMap = listData.get(position).getMap();
        String trackNameAndDuration = trackMap.get("trackName") + " - " + trackMap.get("trackDuration");
        String trackArtistAndAlbum = trackMap.get("trackArtist") + " - " + trackMap.get("trackAlbum");
        viewHolder.textView1.setText(trackNameAndDuration);
        viewHolder.textView2.setText(trackArtistAndAlbum);
        viewHolder.checkbox.setChecked(listOfCheckboxes.get(position, false));
    }
}