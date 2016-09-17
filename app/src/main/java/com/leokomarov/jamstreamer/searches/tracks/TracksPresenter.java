package com.leokomarov.jamstreamer.searches.tracks;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.leokomarov.jamstreamer.MainActivity;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.ListInteractor;
import com.leokomarov.jamstreamer.common.TrackModel;
import com.leokomarov.jamstreamer.playlist.PlaylistList;
import com.leokomarov.jamstreamer.util.GeneralUtils;
import com.leokomarov.jamstreamer.util.JSONParser;
import com.leokomarov.jamstreamer.util.TracklistUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TracksPresenter implements JSONParser.CallbackInterface {

    private Context context;
    private ListInteractor interactor;

    private TracksController listController;
    public TracksAdapter listAdapter;

    protected ArrayList<HashMap<String, String>> tracklist = new ArrayList<>();
    private ArrayList<HashMap<String, String>> albumIDList = new ArrayList<>();

    public TracksPresenter(Context context, TracksController listController, LayoutInflater inflater){
        this.context = context;
        this.listController = listController;
        this.interactor = new ListInteractor();
        this.listAdapter = new TracksAdapter(listController, getListData(), inflater);
        this.listAdapter.selectAll = true;
        this.listAdapter.clearCheckboxes();
    }

    public List<TrackModel> getListData(){
        return interactor.getListData();
    }

    //Sets the data used to generate the listview
    public void setListData(ArrayList<HashMap<String, String>> listData) {
        interactor.setListData(listData);
    }

    public void populateList(String searchTerm){
        String hierarchy = MainActivity.sharedPreferences.getString("hierarchy", "none");
        String unformattedURL = "";

        switch (hierarchy) {
            case "artists":
            case "albums":
            case "tracksFloatingMenuAlbum":
                //Gets the search term from the intent using the extra data put in with the TAG_ALBUM_ID tag
                unformattedURL = context.getString(R.string.tracksByAlbumIDJSONURL);
                break;
            case "tracks":
                unformattedURL = context.getString(R.string.tracksByNameJSONURL);
                break;
            case "topTracksPerWeek":
                unformattedURL = context.getString(R.string.tracksByPopularityPerWeek);
                break;
            case "playlistFloatingMenuAlbum":
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
            JSONArray results = json.getJSONArray(context.getString(R.string.TAG_RESULTS));
            String hierarchy = MainActivity.sharedPreferences.getString("hierarchy", "none");
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
            listAdapter.notifyDataSetChanged();
        }
    }

    public void recyclerViewOnClick(int position){
        ArrayList<HashMap<String, String>> newTrackList = new ArrayList<>();
        String hierarchy = MainActivity.sharedPreferences.getString("hierarchy", "none");

        int indexPosition = 0;
        int oldTrackListSize = 0;

        ArrayList<HashMap<String, String>> oldTrackList = MainActivity.trackPreferences.getObject("tracks", PlaylistList.class).getTracklist();

        if (MainActivity.trackPreferences.getObject("tracks", PlaylistList.class) != null && oldTrackList.size() != 0){
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

        new TracklistUtils().execute(MainActivity.trackPreferences, newTrackList);
        TracklistUtils.updateTracklist(newTrackList);

        SharedPreferences indexPositionPreference = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = indexPositionPreference.edit();
        editor.putInt("indexPosition", indexPosition);
        editor.apply();

        /*
        if (AudioPlayerService.shuffleBoolean){
            AudioPlayerService.shuffleBoolean = false;
        }

        Intent in = new Intent(context, AudioPlayer.class);
        in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        in.putExtra("fromNotification", false);
        activity.startNewActivity(in, 3);
        */
    }

    public int addTrackToPlaylist(int tracksByNameLVLength){
        ArrayList<HashMap<String, String>> tracksToAddList = new ArrayList<>();
        for (int i = 0; i < tracksByNameLVLength; i++){
            if (listAdapter.listOfCheckboxes.get(i, false)) {
                tracksToAddList.add(tracklist.get(i));
            }
        }

        //Creates a new tracklist made up of the old tracklist
        ArrayList<HashMap<String, String>> newTrackList = new ArrayList<>();
        newTrackList.addAll(TracklistUtils.restoreTracklist());

        //and the selected tracks, then saves it to memory
        //and returns the size to the act
        newTrackList.addAll(tracksToAddList);
        new TracklistUtils().execute(newTrackList);
        TracklistUtils.updateTracklist(newTrackList);

        return tracksToAddList.size();
    }
}
