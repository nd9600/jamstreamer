package com.leokomarov.jamstreamer.searches.albums;

import android.view.LayoutInflater;

import com.leokomarov.jamstreamer.common.CustomListAdapter;
import com.leokomarov.jamstreamer.common.TrackModel;

import java.util.HashMap;
import java.util.List;

public class AlbumsAdapter extends CustomListAdapter {
    public AlbumsAdapter(AlbumsController albumsController, List<TrackModel> listData, LayoutInflater inflater) {
        super(albumsController, albumsController, listData, inflater);
    }

    //update the viewHolder for a track
    @Override
    public void updateViewHolder(CustomListAdapter.ViewHolder viewHolder, int position) {
        HashMap<String, String> trackMap = listData.get(position).getMap();
        String albumName = trackMap.get("albumName");
        String albumArtist = trackMap.get("albumArtist");
        viewHolder.textView1.setText(albumName);
        viewHolder.textView2.setText(albumArtist);
        viewHolder.checkbox.setChecked(listOfCheckboxes.get(position, false));
    }
}
