package com.leokomarov.jamstreamer.playlist;

import android.view.LayoutInflater;

import com.leokomarov.jamstreamer.common.ListInteractor;
import com.leokomarov.jamstreamer.common.TrackModel;
import com.leokomarov.jamstreamer.util.TracklistUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlaylistPresenter {

    private ListInteractor interactor;

    public PlaylistAdapter listAdapter;


    public PlaylistPresenter(PlaylistController listController, LayoutInflater inflater){
        this.interactor = new ListInteractor();
        this.listAdapter = new PlaylistAdapter(listController, getListData(), inflater);
        this.listAdapter.selectAllPressed = false;
        this.listAdapter.selectAll = true;
        this.listAdapter.clearCheckboxes();
    }

    public List<TrackModel> getListData(){
        return interactor.getListData();
    }

    public void clearListData(){
        interactor.clearListData();
    }

    //Sets the playlist track data used to generate the listview
    //If fromMemory, the tracklist will be restored from memory
    public void setListData(boolean restoreTracklistFromMemory, ArrayList<HashMap<String, String>> tracklist) {
        if (restoreTracklistFromMemory) {
            ArrayList<HashMap<String, String>> restoredTracklist = TracklistUtils.restoreTracklist();
            if (! restoredTracklist.isEmpty()) {
                tracklist = restoredTracklist;
            }
        }
        interactor.setListData(tracklist);
    }

    public void deletePlaylist(){
        clearListData();

        new TracklistUtils().execute(new ArrayList<HashMap<String, String>>());
        TracklistUtils.updateTracklist(new ArrayList<HashMap<String, String>>());
    }

    /*
    //Starts the audio player
    public void startAudioPlayer(int indexPosition){
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        editor.putInt("indexPosition", indexPosition);
        editor.apply();

        Log.v("startAudioPlayer", "clicked: " + indexPosition);

        if(AudioPlayerService.shuffleBoolean){
            AudioPlayerService.shuffleBoolean = false;
            AudioPlayer.button_shuffle.setImageResource(R.drawable.img_shuffle_default);
        }

        Intent intent = new Intent(context, AudioPlayer.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("fromNotification", false);
        activity.startNewActivity(intent);
    }
    */

    //called in the action mode callback,
    //removes the ticked tracks from the playlist
    public int removeTracksFromPlaylist(int numberOfTracks){
        ArrayList<HashMap<String, String>> tracklist = TracklistUtils.restoreTracklist();
        listAdapter.selectAllPressed = false;

        ArrayList<Integer> tracksToDelete = new ArrayList<>();
        //add every track that is ticked to a list
        for (int i = 0; i < numberOfTracks; i++){
            if (listAdapter.listOfCheckboxes.get(i, false)) {
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

        setListData(false, tracklist);

        new TracklistUtils().execute(tracklist);
        TracklistUtils.updateTracklist(tracklist);

        listAdapter.clearCheckboxes();
        return tracksToDelete.size();
    }

}