package com.leokomarov.jamstreamer.discography.albums;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Toast;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.ListInteractor;
import com.leokomarov.jamstreamer.common.TrackModel;
import com.leokomarov.jamstreamer.discography.tracks.TracksActivity;
import com.leokomarov.jamstreamer.playlist.PlaylistList;
import com.leokomarov.jamstreamer.searches.ArtistsParser;
import com.leokomarov.jamstreamer.utils.ComplexPreferences;
import com.leokomarov.jamstreamer.utils.JSONParser;
import com.leokomarov.jamstreamer.utils.generalUtils;
import com.leokomarov.jamstreamer.utils.tracklistUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AlbumsPresenter implements JSONParser.CallbackInterface, AlbumsByNameTrackParser.CallbackInterface {

    private Context context;
    private AlbumsActivity activity;
    private ListInteractor interactor;
    private ComplexPreferences trackPreferences;

    private JSONArray results;
    private ArrayList<HashMap<String, String>> albumList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> trackList = new ArrayList<>();

    private int albumsToAddLoop = 0;
    private int onTrackRequestCompletedLoop = 0;

    public AlbumsPresenter(Context context, AlbumsActivity activity, ListInteractor listInteractor){
        this.context = context;
        this.activity = activity;
        this.interactor = listInteractor;
        this.trackPreferences = ComplexPreferences.getComplexPreferences(context,
                context.getString(R.string.trackPreferences), Context.MODE_PRIVATE);
    }

    public List<TrackModel> getListData(){
        return interactor.getListData();
    }

    //Sets the data used to generate the listview
    public void setListData(ArrayList<HashMap<String, String>> listData) {
        interactor.setListData(listData);
    }

    public void populateList(Intent intent){
        SharedPreferences hierarchyPreference = context.getSharedPreferences(context.getString(R.string.hierarchyPreferences), 0);
        String hierarchy = hierarchyPreference.getString("hierarchy", "none");
        String searchTerm = "";
        String unformattedURL = "";

        switch (hierarchy) {
            case "artists":
                searchTerm = intent.getStringExtra(ArtistsParser.TAG_ARTIST_ID);
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

        String url = String.format(unformattedURL, searchTerm).replace("&amp;", "&").replace(" ", "+");
        JSONParser jParser = new JSONParser(this);
        jParser.execute(url);
    }

    public void listviewOnClick(int position){
        generalUtils.putHierarchy(context, "albums");
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
        if (menuID == R.id.albumsFloating_selectAlbum){
            activity.listAdapter.selectAllPressed = false;
            CheckBox checkbox = (CheckBox) viewClicked.findViewById(R.id.albums_by_name_checkBox);
            checkbox.setChecked(! checkbox.isChecked());

            activity.callActionBar(activity.listAdapter.tickedCheckboxCounter);
            return true;
        } else if (menuID == R.id.albumsFloating_viewArtist) {
            generalUtils.putHierarchy(context, "albumsFloatingMenuArtist");

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

                Toast.makeText(context, "Adding album, please wait", Toast.LENGTH_SHORT).show();
                AlbumsByNameTrackParser trackParser = new AlbumsByNameTrackParser(this, context);
                trackParser.execute(url);
            }
        }
    }

    @Override
    public void onRequestCompleted(JSONObject json) {
        try {
            results = json.getJSONArray(context.getString(R.string.TAG_RESULTS));
            SharedPreferences hierarchyPreference = context.getSharedPreferences(context.getString(R.string.hierarchyPreferences), 0);
            String hierarchy = hierarchyPreference.getString("hierarchy", "none");
            if (hierarchy.equals("artists") || hierarchy.equals("albumsFloatingMenuArtist") || hierarchy.equals("tracksFloatingMenuArtist") || hierarchy.equals("playlistFloatingMenuArtist")){
                for(int i = 0; i < results.length(); i++) {
                    JSONArray albumsArray = results.getJSONObject(i).getJSONArray("albums");
                    String artistName = results.getJSONObject(i).getString("name");
                    for(int j = 0; j < albumsArray.length(); j++) {
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
            }
            else if (hierarchy.equals("albums")){
                for(int i = 0; i < results.length(); i++) {
                    JSONObject albumInfo = results.getJSONObject(i);
                    HashMap<String, String> map = new HashMap<>();
                    String artistName = albumInfo.getString(context.getString(R.string.TAG_ARTIST_NAME));
                    String albumName = albumInfo.getString(context.getString(R.string.TAG_ALBUM_NAME));
                    String albumID = albumInfo.getString(context.getString(R.string.TAG_ALBUM_ID));

                    map.put("albumArtist", artistName);
                    map.put("albumName", albumName);
                    map.put("albumID", albumID);
                    albumList.add(map);
                }
            }
        } catch (Exception e) {
            Log.e("onRequestCompleted", "Exception: " + e.getMessage());
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

    @Override
    public void onTrackRequestCompleted(JSONObject json) {
        try {
            JSONArray results = json.getJSONArray(context.getString(R.string.TAG_RESULTS));
            onTrackRequestCompletedLoop++;
            for(int i = 0; i < results.length(); i++) {
                JSONArray tracksArray = results.getJSONObject(i).getJSONArray("tracks");
                String artistName = results.getJSONObject(i).getString(context.getString(R.string.TAG_ARTIST_NAME));
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

                    trackList.add(trackMap);
                }
            }

            if (onTrackRequestCompletedLoop == albumsToAddLoop){
                ArrayList<HashMap<String, String>> newTrackList = new ArrayList<>();
                if (trackPreferences.getObject("tracks", PlaylistList.class) != null){
                    newTrackList.addAll(trackPreferences.getObject("tracks", PlaylistList.class).trackList);
                }
                newTrackList.addAll(trackList);
                new tracklistUtils(activity).execute(trackPreferences, "saveAndShuffle", newTrackList);

                activity.setPlaylistButtonClickable(true);
                if (albumsToAddLoop == 1){
                    Toast.makeText(context,"1 album added to the playlist", Toast.LENGTH_LONG).show();
                } else if(albumsToAddLoop >= 2){
                    Toast.makeText(context,albumsToAddLoop + " albums added to the playlist", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e("onTrackRequestCompleted", "Exception: " + e.getMessage());
        }
    }
}
