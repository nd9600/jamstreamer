package com.leokomarov.jamstreamer.ignored.discography.albums;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.CustomListAdapter;
import com.leokomarov.jamstreamer.common.TrackModel;

import java.util.HashMap;
import java.util.List;

public class AlbumsAdapter extends CustomListAdapter {

    private final List<TrackModel> listData;

    protected AlbumsAdapter(AlbumsActivity playlistActivity, AlbumsPresenter presenter) {
        super(playlistActivity, playlistActivity, presenter.getListData(), R.layout.albums_list, R.id.albums_checkbox, R.id.albums_album_name, R.id.albums_album_artist);
        this.listData = presenter.getListData();
    }

    //update the viewHolder for a track
    @Override
    public void updateViewHolder(ViewHolder viewHolder, int position) {
        HashMap<String, String> trackMap = listData.get(position).getMap();
        String albumName = trackMap.get("albumName");
        String albumArtist = trackMap.get("albumArtist");
        viewHolder.textView1.setText(albumName);
        viewHolder.textView2.setText(albumArtist);
        viewHolder.checkbox.setChecked(listOfCheckboxes.get(position, false));
    }
}