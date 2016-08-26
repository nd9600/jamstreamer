package com.leokomarov.jamstreamer.playlist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.ListInteractor;
import com.leokomarov.jamstreamer.common.Presenter;
import com.leokomarov.jamstreamer.common.TrackModel;
import com.leokomarov.jamstreamer.discography.albums.AlbumsByName;
import com.leokomarov.jamstreamer.discography.tracks.TracksByName;
import com.leokomarov.jamstreamer.media_player.AudioPlayer;
import com.leokomarov.jamstreamer.media_player.AudioPlayerService;
import com.leokomarov.jamstreamer.utils.ComplexPreferences;
import com.leokomarov.jamstreamer.utils.generalUtils;
import com.leokomarov.jamstreamer.utils.tracklistUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlaylistPresenter extends Presenter {

    private Context context;
    private PlaylistActivity view;
    private ListInteractor interactor;
    private ComplexPreferences trackPreferences;
    public boolean selectAllPressed;

    public PlaylistPresenter(Context context, PlaylistActivity view, ListInteractor listInteractor){
        this.context = context;
        this.view = view;
        this.interactor = listInteractor;
        this.trackPreferences = ComplexPreferences.getComplexPreferences(context,
                context.getString(R.string.trackPreferences), Context.MODE_PRIVATE);
    }

    public List<TrackModel> getListData(){
        return interactor.getListData();
    }

    public void clearListData(){
        interactor.listData();
    }

    //Sets the playlist track data used to generate the listview
    //If fromMemory, the tracklist will be restored from memory
    public void setListData(boolean restoreTracklistFromMemory, ArrayList<HashMap<String, String>> trackList) {
        if (restoreTracklistFromMemory) {
            ArrayList<HashMap<String, String>> restoredTracklist = tracklistUtils.restoreTracklist(trackPreferences);
            if (! restoredTracklist.isEmpty()) {
                trackList = restoredTracklist;
            }
        }
        interactor.setListData(trackList);
    }

    public void deletePlaylist(){
        clearListData();

        new tracklistUtils(view).execute(trackPreferences, "saveAndShuffle", new ArrayList<HashMap<String, String>>());
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
        ArrayList<HashMap<String, String>> trackList = tracklistUtils.restoreTracklist(trackPreferences);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        View viewClicked = info.targetView;
        int indexPosition = info.position - 1;

        int menuID = item.getItemId();
        switch (menuID) {
            case R.id.playlistFloating_selectTrack:
                selectAllPressed = false;

                //un/tick the checkbox, then call the action bar
                CheckBox checkbox = (CheckBox) viewClicked.findViewById(R.id.playlist_checkBox);
                boolean isCheckboxTicked = checkbox.isChecked();
                checkbox.setChecked(! isCheckboxTicked);
                PlaylistAdapter.tickCheckbox(indexPosition, ! isCheckboxTicked);

                view.callActionBar(PlaylistAdapter.tickedCheckboxCounter);

                return true;
            case R.id.playlistFloating_viewArtist:
                //put the correct string in the hierarchy,
                //then start AlbumsByName
                generalUtils.putHierarchy(context, "playlistFloatingMenuArtist");
                String artistName = trackList.get(indexPosition).get("trackArtist");
                Intent albumsIntent = new Intent(context, AlbumsByName.class);
                albumsIntent.putExtra(context.getString(R.string.TAG_ARTIST_NAME), artistName);

                view.startNewActivity(albumsIntent, 2);
                return true;
            case R.id.playlistFloating_viewAlbum:
                //put the correct string in the hierarchy,
                //then start TracksByName
                generalUtils.putHierarchy(context, "playlistFloatingMenuAlbum");
                String albumName = trackList.get(indexPosition).get("trackAlbum");
                Intent tracksIntent = new Intent(context, TracksByName.class);
                tracksIntent.putExtra(context.getString(R.string.TAG_ALBUM_NAME), albumName);

                view.startNewActivity(tracksIntent, 3);
                return true;
            default:
                return false;
        }
    }

    //called in the action mode callback,
    //removes the ticked tracks from the playlist
    public int removeTracksFromPlaylist(int numberOfTracks){
        ArrayList<HashMap<String, String>> tracklist = tracklistUtils.restoreTracklist(trackPreferences);
        selectAllPressed = false;

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

        setListData(false, tracklist);

        if (! tracklist.isEmpty()){
            new tracklistUtils(view).execute(trackPreferences, "saveAndShuffle", tracklist);
        } else {
            new tracklistUtils(view).execute(trackPreferences, "save", tracklist);
        }

        generalUtils.clearCheckboxes(2);
        return tracksToDelete.size();
    }

}