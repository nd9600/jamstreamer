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
import com.leokomarov.jamstreamer.utils.utils;

import java.util.ArrayList;
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

    public List<PlaylistTrackModel> getPlaylistTrackModel(){
        return interactor.getPlaylistTrackModel();
    }

    public void clearPlaylistTrackModel(){
        interactor.clearPlaylistTrackModel();
    }

    //Sets the playlist track model
    //If the passed tracklist is null, the tracklist will be restored from memory
    public void setPlaylistTrackModel(ArrayList<HashMap<String, String>> trackList) {
        if (trackList == null) {
            ArrayList<HashMap<String, String>> restoredTracklist = restoreTracklist();
            if (restoredTracklist != null) {
                trackList = restoredTracklist;
            }
        }
        interactor.setPlaylistTrackModel(trackList);
    }

    public void saveTracklist(ArrayList<HashMap<String, String>> trackList){
        interactor.saveTracklist(trackPreferences, trackList);
    }

    public ArrayList<HashMap<String, String>> restoreTracklist(){
        return interactor.restoreTracklist(savedInstanceState, trackPreferences);
    }

    public void shuffleTracklist(){
        interactor.shuffleTrackList(trackPreferences);
    }

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

    public boolean onContextItemSelected(MenuItem item){
        ArrayList<HashMap<String, String>> trackList = restoreTracklist();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        View viewClicked = info.targetView;
        int indexPosition = info.position - 1;

        int menuID = item.getItemId();
        switch (menuID) {
            case R.id.playlistFloating_selectTrack:
                selectAllPressed = false;
                view.callActionBar();
                CheckBox checkbox = (CheckBox) viewClicked.findViewById(R.id.playlist_checkBox);
                checkbox.setChecked(! checkbox.isChecked());

                return true;
            case R.id.playlistFloating_viewArtist:
                utils.putHierarchy(context, "playlistFloatingMenuArtist");
                String artistName = trackList.get(indexPosition).get("trackArtist");
                Intent artistsIntent = new Intent(context, AlbumsByName.class);
                artistsIntent.putExtra(context.getString(R.string.TAG_ARTIST_NAME), artistName);

                view.startNewActivity(artistsIntent, 2);
                return true;
            case R.id.playlistFloating_viewAlbum:
                utils.putHierarchy(context, "playlistFloatingMenuAlbum");
                String albumName = trackList.get(indexPosition).get("trackAlbum");
                Intent albumsIntent = new Intent(context, TracksByName.class);
                albumsIntent.putExtra(context.getString(R.string.TAG_ALBUM_NAME), albumName);

                view.startNewActivity(albumsIntent, 3);
                return true;
            default:
                return false;
        }
    }

}