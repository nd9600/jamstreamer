package com.leokomarov.jamstreamer.playlist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.media_player.AudioPlayer;
import com.leokomarov.jamstreamer.media_player.AudioPlayerService;
import com.leokomarov.jamstreamer.utils.ComplexPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaylistPresenter {

    private Context context;
    private Bundle savedInstanceState;
    private PlaylistInterface view;
    private PlaylistInteractor interactor;
    private ComplexPreferences trackPreferences;

    public PlaylistPresenter(Context context, PlaylistInterface view, Bundle savedInstanceState, PlaylistInteractor playlistInteractor){
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
        indexPositionEditor.commit();

        if(AudioPlayerService.shuffleBoolean){
            AudioPlayerService.shuffleBoolean = false;
            AudioPlayer.button_shuffle.setImageResource(R.drawable.img_repeat_default);
        }

        Intent intent = new Intent(context, AudioPlayer.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("fromNotification", false);

        view.startNewActivity(intent, 1);
    }

}