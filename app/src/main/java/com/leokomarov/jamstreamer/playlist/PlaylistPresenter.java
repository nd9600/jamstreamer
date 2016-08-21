package com.leokomarov.jamstreamer.playlist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.discography.AlbumsByName;
import com.leokomarov.jamstreamer.discography.TracksByName;
import com.leokomarov.jamstreamer.media_player.AudioPlayer;
import com.leokomarov.jamstreamer.media_player.AudioPlayerService;
import com.leokomarov.jamstreamer.utils.ComplexPreferences;
import com.leokomarov.jamstreamer.utils.generalUtils;
import com.leokomarov.jamstreamer.utils.tracklistUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlaylistPresenter {

    private Context context;
    private Bundle savedInstanceState;
    private PlaylistActivity view;
    private PlaylistInteractor interactor;
    private ComplexPreferences trackPreferences;
    public static boolean selectAllPressed;

    public PlaylistPresenter(Context context, PlaylistActivity view, Bundle savedInstanceState, PlaylistInteractor playlistInteractor){
        this.context = context;
        this.savedInstanceState = savedInstanceState;
        this.view = view;
        this.interactor = playlistInteractor;
        this.trackPreferences = ComplexPreferences.getComplexPreferences(context,
                context.getString(R.string.trackPreferences), Context.MODE_PRIVATE);
    }

    public List<PlaylistTrackModel> getPlaylistTrackData(){
        return interactor.getPlaylistTrackData();
    }

    public void clearPlaylistTrackData(){
        interactor.clearPlaylistTrackData();
    }

    //Sets the playlist track data used to generate the listview
    //If the passed tracklist is empty, the tracklist will be restored from memory
    public void setPlaylistTrackData(ArrayList<HashMap<String, String>> trackList) {
        if (trackList.isEmpty()) {
            ArrayList<HashMap<String, String>> restoredTracklist = tracklistUtils.restoreTracklist(savedInstanceState, trackPreferences);
            if (! restoredTracklist.isEmpty()) {
                trackList = restoredTracklist;
            }
        }
        interactor.setPlaylistTrackData(trackList);
    }

    public void deletePlaylist(){
        clearPlaylistTrackData();
        tracklistUtils.saveTracklist(trackPreferences, new ArrayList<HashMap<String, String>>());
        tracklistUtils.shuffleTrackList(trackPreferences);
    }

    //Starts the audio player
    public void startAudioPlayer(int indexPosition){
        SharedPreferences indexPositionPreference = context.getSharedPreferences(context.getString(R.string.indexPositionPreferences), 0);
        SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
        indexPositionEditor.putInt("indexPosition", indexPosition );
        indexPositionEditor.apply();

        if(AudioPlayerService.shuffleBoolean){
            AudioPlayerService.shuffleBoolean = false;
            AudioPlayer.button_shuffle.setImageResource(R.drawable.img_repeat_default);
        }

        Intent intent = new Intent(context, AudioPlayer.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("fromNotification", false);

        view.startNewActivity(intent, 1);
    }

    //Called when something in the floating menu is selected
    public boolean onContextItemSelected(MenuItem item){
        ArrayList<HashMap<String, String>> trackList = tracklistUtils.restoreTracklist(savedInstanceState, trackPreferences);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        View viewClicked = info.targetView;
        int indexPosition = info.position - 1;

        int menuID = item.getItemId();
        switch (menuID) {
            case R.id.playlistFloating_selectTrack:
                selectAllPressed = false;
                view.callActionBar();
                CheckBox checkbox = (CheckBox) viewClicked.findViewById(R.id.playlist_checkBox);
                boolean isCheckboxTicked = checkbox.isChecked();
                checkbox.setChecked(! isCheckboxTicked);
                PlaylistAdapter.tickCheckbox(indexPosition, isCheckboxTicked);

                return true;
            case R.id.playlistFloating_viewArtist:
                generalUtils.putHierarchy(context, "playlistFloatingMenuArtist");
                String artistName = trackList.get(indexPosition).get("trackArtist");
                Intent artistsIntent = new Intent(context, AlbumsByName.class);
                artistsIntent.putExtra(context.getString(R.string.TAG_ARTIST_NAME), artistName);

                view.startNewActivity(artistsIntent, 2);
                return true;
            case R.id.playlistFloating_viewAlbum:
                generalUtils.putHierarchy(context, "playlistFloatingMenuAlbum");
                String albumName = trackList.get(indexPosition).get("trackAlbum");
                Intent albumsIntent = new Intent(context, TracksByName.class);
                albumsIntent.putExtra(context.getString(R.string.TAG_ALBUM_NAME), albumName);

                view.startNewActivity(albumsIntent, 3);
                return true;
            default:
                return false;
        }
    }

    //called in the action mode callback,
    //removes the ticked tracks from the playlist
    public int removeTracksFromPlaylist(int numberOfTracks){
        ArrayList<HashMap<String, String>> tracklist = tracklistUtils.restoreTracklist(savedInstanceState, trackPreferences);
        PlaylistPresenter.selectAllPressed = false;

        ArrayList<Integer> tracksToDelete = new ArrayList<>();
        //add every track that is ticked to a list
        for (int i = 0; i < numberOfTracks; i++){
            if (PlaylistAdapter.listOfCheckboxes.get(i, false)) {
                tracksToDelete.add(i);
            }
        }

        //reverse that list then remove the corresponding tracks from the tracklist,
        //save the tracklist and shuffled tracklist,
        //and update the LV's data
        Collections.sort(tracksToDelete, Collections.reverseOrder());
        for (int i : tracksToDelete){
            tracklist.remove(i);
        }
        tracklistUtils.saveTracklist(trackPreferences, tracklist);
        setPlaylistTrackData(tracklist);

        if (! tracklist.isEmpty()){
            tracklistUtils.shuffleTrackList(trackPreferences);
        }

        generalUtils.clearCheckboxes(2);
        return tracksToDelete.size();
    }

}