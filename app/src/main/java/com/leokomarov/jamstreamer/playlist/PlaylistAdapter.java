package com.leokomarov.jamstreamer.playlist;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.CustomListAdapter;
import com.leokomarov.jamstreamer.common.TrackModel;

import java.util.HashMap;
import java.util.List;

public class PlaylistAdapter extends CustomListAdapter {

    private final List<TrackModel> playlistTrackData;

    protected PlaylistAdapter(PlaylistActivity playlistActivity, PlaylistPresenter presenter) {
		super(playlistActivity, playlistActivity, presenter, presenter.getPlaylistTrackData(), R.layout.playlist_by_list_item, R.id.playlist_checkBox, R.id.playlist_trackNameAndDuration, R.id.playlist_trackArtistAndAlbum);
        this.playlistTrackData = presenter.getPlaylistTrackData();
	}

    //update the viewHolder for a track
    @Override
    public void updateViewHolder(ViewHolder viewHolder, int position) {
        HashMap<String, String> trackMap = playlistTrackData.get(position).getMap();
        String trackNameAndDuration = trackMap.get("trackName") + " - " + trackMap.get("trackDuration");
        String trackArtistAndAlbum = trackMap.get("trackArtist") + " - " + trackMap.get("trackAlbum");
        viewHolder.textView1.setText(trackNameAndDuration);
        viewHolder.textView2.setText(trackArtistAndAlbum);
        viewHolder.checkbox.setChecked(listOfCheckboxes.get(position, false));
    }
}