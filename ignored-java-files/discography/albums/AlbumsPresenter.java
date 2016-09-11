package com.leokomarov.jamstreamer.ignored.discography.albums;

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
import com.leokomarov.jamstreamer.common.ListInteractor;
import com.leokomarov.jamstreamer.common.TrackModel;
import com.leokomarov.jamstreamer.ignored.discography.tracks.TracksActivity;
import com.leokomarov.jamstreamer.util.ComplexPreferences;
import com.leokomarov.jamstreamer.util.GeneralUtils;
import com.leokomarov.jamstreamer.util.JSONParser;
import com.leokomarov.jamstreamer.util.TracklistUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AlbumsPresenter implements JSONParser.CallbackInterface {

    private Context context;
    private AlbumsActivity activity;
    private ListInteractor interactor;
    private ComplexPreferences trackPreferences;
    private SharedPreferences sharedPreferences;

    private JSONArray results;
    private ArrayList<HashMap<String, String>> albumList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> tracklist = new ArrayList<>();

    private int albumsToAddLoop = 0;
    private int onTrackRequestCompletedLoop = 0;

    private String requestType;

    public AlbumsPresenter(Context context, AlbumsActivity activity, ListInteractor listInteractor){
        this.context = context;
        this.activity = activity;
        this.interactor = listInteractor;
        this.trackPreferences = ComplexPreferences.getComplexPreferences(context,
                context.getString(R.string.trackPreferences), Context.MODE_PRIVATE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public List<TrackModel> getListData(){
        return interactor.getListData();
    }

    //Sets the data used to generate the listview
    public void setListData(ArrayList<HashMap<String, String>> listData) {
        interactor.setListData(listData);
    }

    public void populateList(Intent intent){
        String hierarchy = sharedPreferences.getString("hierarchy", "none");
        String searchTerm = "";
        String unformattedURL = "";

        switch (hierarchy) {
            case "artists":
                searchTerm = intent.getStringExtra(context.getString(R.string.TAG_ARTIST_ID));
                unformattedURL = context.getString(R.string.albumsByArtistIDJSONURL);
                break;
            case "albums":
            case "tracks":
                searchTerm = intent.getStringExtra(context.getString(R.string.TAG_ALBUM_NAME));
                unformattedURL = context.getString(R.string.albumsByNameJSONURL);
                break;
            case "tracksFloatingMenuArtist":
            case "albumsFloatingMenuArtist":
            case "playlistFloatingMenuArtist":
                searchTerm = intent.getStringExtra(context.getString(R.string.TAG_ARTIST_NAME));
                unformattedURL = context.getString(R.string.albumsByArtistNameJSONURL);
                break;
        }

        requestType = "album";
        String url = String.format(unformattedURL, searchTerm).replace("&amp;", "&").replace(" ", "+");
        JSONParser albumParser = new JSONParser(this);
        albumParser.execute(url);
    }

    public void listviewOnClick(int position){
        GeneralUtils.putHierarchy(context, "albums");
        String albumID = albumList.get(position - 1).get("albumID");
        Intent intent = new Intent(context, TracksActivity.class);
        intent.putExtra(context.getString(R.string.TAG_ALBUM_ID), albumID);
        activity.startNewActivity(intent, 2);
    }

    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        View viewClicked = info.targetView;
        int indexPosition = info.position - 1;

        int menuID = item.getItemId();
        if (menuID == R.id.albums_floating_menu_selectAlbum){
            activity.listAdapter.selectAllPressed = false;
            CheckBox checkbox = (CheckBox) viewClicked.findViewById(R.id.albums_checkbox);
            checkbox.setChecked(! checkbox.isChecked());

            activity.callActionBar(activity.listAdapter.tickedCheckboxCounter);
            return true;
        } else if (menuID == R.id.albums_floating_menu_viewArtist) {
            GeneralUtils.putHierarchy(context, "albumsFloatingMenuArtist");

            String artistName = albumList.get(indexPosition).get("albumArtist");
            Intent artistsIntent = new Intent(context, AlbumsActivity.class);
            artistsIntent.putExtra(context.getString(R.string.TAG_ARTIST_NAME), artistName);
            activity.startNewActivity(artistsIntent, 3);
            return true;
        } else {
            return false;
        }
    }

    public void addAlbumToPlaylist(int numberOfAlbums){
        activity.listAdapter.selectAllPressed = false;
        albumsToAddLoop = 0;
        onTrackRequestCompletedLoop = 0;
        for (int i = 0; i < numberOfAlbums; i++){
            if (activity.listAdapter.listOfCheckboxes.get(i, false)) {
                albumsToAddLoop++;
                String albumID = albumList.get(i).get("albumID");
                String unformattedURL = context.getString(R.string.tracksByAlbumIDJSONURL);
                String url = String.format(unformattedURL, albumID).replace("&amp;", "&");

                Toast.makeText(context, String.format("Adding album #%s", albumsToAddLoop), Toast.LENGTH_SHORT).show();

                requestType = "track";
                JSONParser trackParser = new JSONParser(this);
                trackParser.execute(url);
            }
        }
    }

    @Override
    public void onRequestCompleted(JSONObject json) {
        if (requestType.equals("album")){
            albumRequest(json);
        } else if (requestType.equals("track")){
            trackRequest(json);
        }
    }

    public void albumRequest(JSONObject json) {
        try {
            results = json.getJSONArray(context.getString(R.string.TAG_RESULTS));
            String hierarchy = sharedPreferences.getString("hierarchy", "none");
            switch (hierarchy) {
                case "artists":
                case "albumsFloatingMenuArtist":
                case "tracksFloatingMenuArtist":
                case "playlistFloatingMenuArtist":
                    for (int i = 0; i < results.length(); i++) {
                        JSONArray albumsArray = results.getJSONObject(i).getJSONArray(context.getString(R.string.TAG_ALBUMS));
                        String artistName = results.getJSONObject(i).getString(context.getString(R.string.TAG_ARTIST_NAME));
                        for (int j = 0; j < albumsArray.length(); j++) {
                            JSONObject albumInfo = albumsArray.getJSONObject(j);
                            HashMap<String, String> map = new HashMap<>();
                            String albumName = albumInfo.getString(context.getString(R.string.TAG_ALBUM_NAME));
                            String albumID = albumInfo.getString(context.getString(R.string.TAG_ALBUM_ID));

                            map.put("albumArtist", artistName);
                            map.put("albumName", albumName);
                            map.put("albumID", albumID);
                            albumList.add(map);
                        }
                    }
                    break;
                case "albums":
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject albumInfo = results.getJSONObject(i);
                        HashMap<String, String> map = new HashMap<>();
                        String artistName = albumInfo.getString(context.getString(R.string.TAG_ARTIST_NAME_LITERAL));
                        String albumName = albumInfo.getString(context.getString(R.string.TAG_ALBUM_NAME));
                        String albumID = albumInfo.getString(context.getString(R.string.TAG_ALBUM_ID));

                        map.put("albumArtist", artistName);
                        map.put("albumName", albumName);
                        map.put("albumID", albumID);
                        albumList.add(map);
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e("albumRequest", "Exception: " + e.getMessage());
        }

        if (json == null || json.isNull("results")) {
            Toast.makeText(context, "Please retry, there has been an error downloading the album list", Toast.LENGTH_SHORT).show();
        }
        else if (json.has("results") && albumList.isEmpty()){
            Toast.makeText(context, "There are no albums matching this search", Toast.LENGTH_SHORT).show();
        }
        else {
            setListData(albumList);
            activity.setUpListview();
        }
    }

    public void trackRequest(JSONObject json) {
        try {
            JSONArray results = json.getJSONArray(context.getString(R.string.TAG_RESULTS));
            onTrackRequestCompletedLoop++;
            for(int i = 0; i < results.length(); i++) {
                JSONArray tracksArray = results.getJSONObject(i).getJSONArray("tracks");
                String artistName = results.getJSONObject(i).getString(context.getString(R.string.TAG_ARTIST_NAME_LITERAL));
                String albumName = results.getJSONObject(i).getString(context.getString(R.string.TAG_ALBUM_NAME));
                for(int j = 0; j < tracksArray.length(); j++) {
                    JSONObject trackInfo = tracksArray.getJSONObject(j);

                    String trackID = trackInfo.getString(context.getString(R.string.TAG_TRACK_ID));
                    String trackName = trackInfo.getString(context.getString(R.string.TAG_TRACK_NAME));
                    long durationLong = Long.valueOf(trackInfo.getString(context.getString(R.string.TAG_TRACK_DURATION)));
                    String trackDuration = String.format(Locale.US, "%d:%02d", durationLong / 60,durationLong % 60);

                    HashMap<String, String> trackMap = new HashMap<>();
                    trackMap.put("trackID", trackID);
                    trackMap.put("trackName", trackName);
                    trackMap.put("trackDuration", trackDuration);
                    trackMap.put("trackArtist", artistName);
                    trackMap.put("trackAlbum", albumName);

                    tracklist.add(trackMap);
                }
            }

            if (onTrackRequestCompletedLoop == albumsToAddLoop){
                ArrayList<HashMap<String, String>> newTrackList = new ArrayList<>();
                newTrackList.addAll(TracklistUtils.restoreTracklist(trackPreferences));
                newTrackList.addAll(tracklist);

                new TracklistUtils().execute(trackPreferences, newTrackList);
                TracklistUtils.updateTracklist(trackPreferences, sharedPreferences, newTrackList);

                activity.setPlaylistButtonClickable(true);
                if (albumsToAddLoop == 1){
                    Toast.makeText(context,"1 album added to the playlist", Toast.LENGTH_LONG).show();
                } else if(albumsToAddLoop >= 2){
                    Toast.makeText(context,albumsToAddLoop + " albums added to the playlist", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e("trackRequest", "Exception: " + e.getMessage());
        }
    }
}
