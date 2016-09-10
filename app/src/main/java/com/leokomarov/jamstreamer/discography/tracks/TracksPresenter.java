package com.leokomarov.jamstreamer.discography.tracks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Toast;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.audio_player.AudioPlayer;
import com.leokomarov.jamstreamer.audio_player.AudioPlayerService;
import com.leokomarov.jamstreamer.common.ListInteractor;
import com.leokomarov.jamstreamer.common.TrackModel;
import com.leokomarov.jamstreamer.discography.albums.AlbumsActivity;
import com.leokomarov.jamstreamer.playlist.PlaylistList;
import com.leokomarov.jamstreamer.utils.ComplexPreferences;
import com.leokomarov.jamstreamer.utils.GeneralUtils;
import com.leokomarov.jamstreamer.utils.JSONParser;
import com.leokomarov.jamstreamer.utils.TracklistUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TracksPresenter implements JSONParser.CallbackInterface {
    private Context context;
    private TracksActivity activity;
    private ListInteractor interactor;
    private ComplexPreferences trackPreferences;
    private SharedPreferences sharedPreferences;

    private JSONArray results;
    private ArrayList<HashMap<String, String>> tracklist = new ArrayList<>();
    private ArrayList<HashMap<String, String>> albumIDList = new ArrayList<>();

    public TracksPresenter(Context context, TracksActivity activity, ListInteractor listInteractor){
        this.context = context;
        this.activity = activity;
        this.interactor = listInteractor;
        this.trackPreferences = ComplexPreferences.getComplexPreferences(context,
                context.getString(R.string.trackPreferences), Context.MODE_PRIVATE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //Sets the data used to generate the listview
    public void setListData(ArrayList<HashMap<String, String>> listData) {
        interactor.setListData(listData);
    }

    public List<TrackModel> getListData(){
        return interactor.getListData();
    }

    public void populateList(Intent intent){
        String hierarchy = sharedPreferences.getString("hierarchy", "none");
        String searchTerm = "";
        String unformattedURL = "";

        switch (hierarchy) {
            case "artists":
            case "albums":
            case "tracksFloatingMenuAlbum":
                //Gets the search term from the intent using the extra data put in with the TAG_ALBUM_ID tag
                searchTerm = intent.getStringExtra(context.getString(R.string.TAG_ALBUM_ID));
                unformattedURL = context.getString(R.string.tracksByAlbumIDJSONURL);
                break;
            case "tracks":
                searchTerm = intent.getStringExtra(context.getString(R.string.TAG_TRACK_NAME)).replace(" ", "+");
                unformattedURL = context.getString(R.string.tracksByNameJSONURL);
                break;
            case "topTracksPerWeek":
                unformattedURL = context.getString(R.string.tracksByPopularityPerWeek);
                break;
            case "playlistFloatingMenuAlbum":
                searchTerm = intent.getStringExtra(context.getString(R.string.TAG_ALBUM_NAME)).replace(" ", "+");
                unformattedURL = context.getString(R.string.tracksByAlbumNameJSONURL);
                break;
        }

        String url = String.format(unformattedURL, searchTerm).replace("&amp;", "&");
        JSONParser jParser = new JSONParser(this);
        jParser.execute(url);
    }

    @Override
    public void onRequestCompleted(JSONObject json) {
        try {
            results = json.getJSONArray(context.getString(R.string.TAG_RESULTS));
            String hierarchy = sharedPreferences.getString("hierarchy", "none");
            switch (hierarchy) {
                case "artists":
                case "albums":
                case "tracksFloatingMenuAlbum":
                case "playlistFloatingMenuAlbum":
                    for (int i = 0; i < results.length(); i++) {
                        JSONArray tracksArray = results.getJSONObject(i).getJSONArray("tracks");
                        String artistName = results.getJSONObject(i).getString(context.getString(R.string.TAG_ARTIST_NAME));
                        String albumName = results.getJSONObject(i).getString("name");
                        String albumID = results.getJSONObject(i).getString("id");
                        for (int j = 0; j < tracksArray.length(); j++) {
                            JSONObject trackInfo = tracksArray.getJSONObject(j);

                            String trackID = trackInfo.getString(context.getString(R.string.TAG_TRACK_ID));
                            String trackName = trackInfo.getString(context.getString(R.string.TAG_TRACK_NAME));
                            long durationLong = Long.valueOf(trackInfo.getString(context.getString(R.string.TAG_TRACK_DURATION)));
                            String trackDuration = String.format(Locale.US, "%d:%02d", durationLong / 60, durationLong % 60);

                            HashMap<String, String> albumIDMap = new HashMap<>();
                            albumIDMap.put("albumID", albumID);
                            albumIDList.add(albumIDMap);

                            HashMap<String, String> trackMap = new HashMap<>();
                            trackMap.put("trackID", trackID);
                            trackMap.put("trackName", trackName);
                            trackMap.put("trackDuration", trackDuration);
                            trackMap.put("trackArtist", artistName);
                            trackMap.put("trackAlbum", albumName);
                            tracklist.add(trackMap);
                        }
                    }
                    break;
                case "tracks":
                case "topTracksPerWeek":
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject trackInfo = results.getJSONObject(i);

                        String trackID = trackInfo.getString(context.getString(R.string.TAG_TRACK_ID));
                        String trackName = trackInfo.getString(context.getString(R.string.TAG_TRACK_NAME));
                        long durationLong = Long.valueOf(trackInfo.getString(context.getString(R.string.TAG_TRACK_DURATION)));
                        String trackDuration = String.format(Locale.US, "%d:%02d", durationLong / 60, durationLong % 60);
                        String artistName = trackInfo.getString(context.getString(R.string.TAG_ARTIST_NAME_LITERAL));
                        String albumName = trackInfo.getString(context.getString(R.string.TAG_ALBUM_NAME_LITERAL));
                        String albumID = trackInfo.getString("album_id");

                        HashMap<String, String> albumIDMap = new HashMap<>();
                        albumIDMap.put("albumID", albumID);
                        albumIDList.add(albumIDMap);

                        HashMap<String, String> trackMap = new HashMap<>();
                        trackMap.put("trackID", trackID);
                        trackMap.put("trackName", trackName);
                        trackMap.put("trackDuration", trackDuration);
                        trackMap.put("trackArtist", artistName);
                        trackMap.put("trackAlbum", albumName);
                        tracklist.add(trackMap);
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e("TracksPresenter", "Exception: " + e.getMessage());
        }

        if (json == null || json.isNull("results")) {
            Toast.makeText(context, "Please retry, there has been an error downloading the track list", Toast.LENGTH_LONG).show();
        }
        else if (json.has("results") && tracklist.isEmpty()){
            Toast.makeText(context, "There are no tracks matching this search", Toast.LENGTH_LONG).show();
        }
        else {
            setListData(tracklist);
            activity.setUpListview();
        }
    }

     public void listviewOnClick(int position){
         ArrayList<HashMap<String, String>> newTrackList = new ArrayList<>();
         String hierarchy = sharedPreferences.getString("hierarchy", "none");

         int indexPosition = 0;
         int oldTrackListSize = 0;

         ArrayList<HashMap<String, String>> oldTrackList = trackPreferences.getObject("tracks", PlaylistList.class).getTracklist();

         if (trackPreferences.getObject("tracks", PlaylistList.class) != null && oldTrackList.size() != 0){
             newTrackList.addAll(oldTrackList);
             oldTrackListSize = oldTrackList.size();
         }

         switch (hierarchy) {
             case "artists":
             case "albums":
             case "tracksFloatingMenuAlbum":
             case "playlistFloatingMenuAlbum":
                 newTrackList.addAll(tracklist);
                 indexPosition = oldTrackListSize + position - 1;
                 break;
             case "tracks":
             case "topTracksPerWeek":
                 newTrackList.add(tracklist.get(position - 1));
                 indexPosition = oldTrackListSize;
                 break;
         }

         GeneralUtils.putHierarchy(context, "tracks");

         new TracklistUtils().execute(trackPreferences, newTrackList);
         TracklistUtils.updateTracklist(trackPreferences, sharedPreferences, newTrackList);

         SharedPreferences indexPositionPreference = PreferenceManager.getDefaultSharedPreferences(context);
         SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
         indexPositionEditor.putInt("indexPosition", indexPosition);
         indexPositionEditor.apply();

         if (AudioPlayerService.shuffleBoolean){
             AudioPlayerService.shuffleBoolean = false;
         }

         Intent in = new Intent(context, AudioPlayer.class);
         in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
         in.putExtra("fromNotification", false);
         activity.startNewActivity(in, 3);
     }

    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        View viewClicked = info.targetView;
        int indexPosition = info.position - 1;

        int menuID = item.getItemId();
        if (menuID == R.id.tracks_floating_menu_selectTrack){
            activity.listAdapter.selectAllPressed = false;
            CheckBox checkbox = (CheckBox) viewClicked.findViewById(R.id.tracks_by_name_checkBox);
            checkbox.setChecked(! checkbox.isChecked());
            return true;
        } else if (menuID == R.id.tracks_floating_menu_viewArtist) {
            GeneralUtils.putHierarchy(context, "tracksFloatingMenuArtist");

            String artistName = tracklist.get(indexPosition).get("trackArtist");
            Intent artistsIntent = new Intent(context.getApplicationContext(), AlbumsActivity.class);
            artistsIntent.putExtra(context.getString(R.string.TAG_ARTIST_NAME), artistName);
            activity.startNewActivity(artistsIntent, 2);
            return true;
        } else if (menuID == R.id.tracks_floating_menu_viewAlbum) {
            GeneralUtils.putHierarchy(context, "tracksFloatingMenuAlbum");
            String albumID = albumIDList.get(indexPosition).get("albumID");
            Intent albumsIntent = new Intent(context, TracksActivity.class);
            albumsIntent.putExtra(context.getString(R.string.TAG_ALBUM_ID), albumID);
            activity.startNewActivity(albumsIntent, 3);
            return true;
        } else {
            return false;
        }
    }

    public int addTrackToPlaylist(int tracksByNameLVLength){
        activity.listAdapter.selectAllPressed = false;

        ArrayList<HashMap<String, String>> tracksToAddList = new ArrayList<>();
        for (int i = 0; i < tracksByNameLVLength; i++){
            if (activity.listAdapter.listOfCheckboxes.get(i, false)) {
                tracksToAddList.add(tracklist.get(i));
            }
        }

        //Creates a new tracklist made up of the old tracklist
        ArrayList<HashMap<String, String>> newTrackList = new ArrayList<>();
        newTrackList.addAll(TracklistUtils.restoreTracklist(trackPreferences));

        //and the selected tracks, then saves it to memory
        //and returns the size to the act
        newTrackList.addAll(tracksToAddList);
        new TracklistUtils().execute(trackPreferences, newTrackList);
        TracklistUtils.updateTracklist(trackPreferences, sharedPreferences, newTrackList);

        return tracksToAddList.size();
    }
}
