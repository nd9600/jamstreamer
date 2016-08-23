package com.leokomarov.jamstreamer.playlist;

import android.util.Log;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.CustomListAdapter;
import com.leokomarov.jamstreamer.common.TrackModel;

import java.util.HashMap;
import java.util.List;

public class PlaylistAdapter extends CustomListAdapter {

    public interface CallbackInterface {
        void callActionBar();
    }

    //private PlaylistActivity playlistActivity;

    //mCallback is the instance of the interface with the callActionBar() method
    private CallbackInterface mCallback;

    private final List<TrackModel> playlistTrackData;

    protected PlaylistAdapter(CallbackInterface callback, PlaylistActivity playlistActivity, PlaylistPresenter presenter) {
		super(playlistActivity, presenter, presenter.getPlaylistTrackData(), R.layout.playlist_by_list_item, R.id.playlist_checkBox, R.id.playlist_trackNameAndDuration, R.id.playlist_trackArtistAndAlbum);
        //this.playlistActivity = playlistActivity;
        this.mCallback = callback;
        this.playlistTrackData = presenter.getPlaylistTrackData();
	}

    @Override
    public void callActionBarFromCustomAdapter() {

    }

    @Override
    public void updateViewHolder(ViewHolder viewHolder, int position) {
        HashMap<String, String> trackMap = playlistTrackData.get(position).getMap();
        String trackNameAndDuration = trackMap.get("trackName") + " - " + trackMap.get("trackDuration");
        String trackArtistAndAlbum = trackMap.get("trackArtist") + " - " + trackMap.get("trackAlbum");
        viewHolder.textView1.setText(trackNameAndDuration);
        viewHolder.textView2.setText(trackArtistAndAlbum);
        viewHolder.checkbox.setChecked(listOfCheckboxes.get(position, false));
    }

    @Override
    public void changeActionMode() {
        Log.v("callActionAdapter", ".");
        mCallback.callActionBar();
        if (tickedCheckboxCounter == 0){
            PlaylistActivity.mActionMode.finish();
        }
        else {
            PlaylistActivity.mActionMode.setTitle(tickedCheckboxCounter + " selected");
        }
    }
}