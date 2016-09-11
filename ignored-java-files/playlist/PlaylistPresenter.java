package com.leokomarov.jamstreamer.ignored.playlist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.ignored.audio_player.AudioPlayer;
import com.leokomarov.jamstreamer.ignored.audio_player.AudioPlayerService;
import com.leokomarov.jamstreamer.common.ListInteractor;
import com.leokomarov.jamstreamer.common.TrackModel;
import com.leokomarov.jamstreamer.ignored.discography.albums.AlbumsActivity;
import com.leokomarov.jamstreamer.ignored.discography.tracks.TracksActivity;
import com.leokomarov.jamstreamer.util.ComplexPreferences;
import com.leokomarov.jamstreamer.util.GeneralUtils;
import com.leokomarov.jamstreamer.util.TracklistUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlaylistPresenter {

    private Context context;
    private PlaylistActivity activity;
    private ListInteractor interactor;
    private ComplexPreferences trackPreferences;
    private SharedPreferences sharedPreferences;

    public PlaylistPresenter(Context context, PlaylistActivity activity, ListInteractor listInteractor){
        this.context = context;
        this.activity = activity;
        this.interactor = listInteractor;
        this.trackPreferences = ComplexPreferences.getComplexPreferences(context,
                context.getString(R.string.trackPreferences), Context.MODE_PRIVATE);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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
            ArrayList<HashMap<String, String>> restoredTracklist = TracklistUtils.restoreTracklist(trackPreferences);
            if (! restoredTracklist.isEmpty()) {
                tracklist = restoredTracklist;
            }
        }
        interactor.setListData(tracklist);
    }

    public void deletePlaylist(){
        clearListData();

        TracklistUtils.updateTracklist(trackPreferences, sharedPreferences, new ArrayList<HashMap<String, String>>());
        new TracklistUtils().execute(trackPreferences, new ArrayList<HashMap<String, String>>());
    }

    //Starts the audio player
    public void startAudioPlayer(int indexPosition){
        SharedPreferences.Editor editor = sharedPreferences.edit();
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

    //Called when something in the floating menu is selected
    public boolean onContextItemSelected(MenuItem item){
        ArrayList<HashMap<String, String>> tracklist = TracklistUtils.restoreTracklist(trackPreferences);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        View viewClicked = info.targetView;
        int indexPosition = info.position - 1;

        int menuID = item.getItemId();
        switch (menuID) {
            case R.id.playlist_floating_menu_selectTrack:
                activity.listAdapter.selectAllPressed = false;

                //un/tick the checkbox, then call the action bar
                CheckBox checkbox = (CheckBox) viewClicked.findViewById(R.id.playlist_checkBox);
                boolean isCheckboxTicked = checkbox.isChecked();
                checkbox.setChecked(! isCheckboxTicked);
                activity.listAdapter.tickCheckbox(indexPosition, ! isCheckboxTicked);

                activity.callActionBar(activity.listAdapter.tickedCheckboxCounter);

                return true;
            case R.id.playlist_floating_menu_viewArtist:
                //put the correct string in the hierarchy,
                //then start AlbumsActivity
                GeneralUtils.putHierarchy(context, "playlistFloatingMenuArtist");
                String artistName = tracklist.get(indexPosition).get("trackArtist");
                Intent albumsIntent = new Intent(context, AlbumsActivity.class);
                albumsIntent.putExtra(context.getString(R.string.TAG_ARTIST_NAME), artistName);

                activity.startNewActivity(albumsIntent);
                return true;
            case R.id.playlist_floating_menu_viewAlbum:
                //put the correct string in the hierarchy,
                //then start TracksActivity
                GeneralUtils.putHierarchy(context, "playlistFloatingMenuAlbum");
                String albumName = tracklist.get(indexPosition).get("trackAlbum");
                Intent tracksIntent = new Intent(context, TracksActivity.class);
                tracksIntent.putExtra(context.getString(R.string.TAG_ALBUM_NAME), albumName);

                activity.startNewActivity(tracksIntent);
                return true;
            default:
                return false;
        }
    }

    //called in the action mode callback,
    //removes the ticked tracks from the playlist
    public int removeTracksFromPlaylist(int numberOfTracks){
        ArrayList<HashMap<String, String>> tracklist = TracklistUtils.restoreTracklist(trackPreferences);
        activity.listAdapter.selectAllPressed = false;

        ArrayList<Integer> tracksToDelete = new ArrayList<>();
        //add every track that is ticked to a list
        for (int i = 0; i < numberOfTracks; i++){
            if (activity.listAdapter.listOfCheckboxes.get(i, false)) {
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

        new TracklistUtils().execute(trackPreferences, tracklist);
        TracklistUtils.updateTracklist(trackPreferences, sharedPreferences, tracklist);

        activity.listAdapter.clearCheckboxes();
        return tracksToDelete.size();
    }

}