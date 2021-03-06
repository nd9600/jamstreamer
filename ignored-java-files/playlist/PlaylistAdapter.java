package com.leokomarov.jamstreamer.ignored.playlist;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.CustomListAdapter;
import com.leokomarov.jamstreamer.common.TrackModel;

import java.util.HashMap;
import java.util.List;

public class PlaylistAdapter extends CustomListAdapter {

    private final List<TrackModel> listData;

    protected PlaylistAdapter(PlaylistActivity playlistActivity, PlaylistPresenter presenter) {
		super(playlistActivity, playlistActivity, presenter.getListData(), R.layout.playlist_list, R.id.playlist_checkBox, R.id.playlist_trackNameAndDuration, R.id.playlist_trackArtistAndAlbum);
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