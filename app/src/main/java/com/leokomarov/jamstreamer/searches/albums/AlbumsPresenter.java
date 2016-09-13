package com.leokomarov.jamstreamer.searches.albums;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.leokomarov.jamstreamer.MainActivity;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.ListInteractor;
import com.leokomarov.jamstreamer.common.TrackModel;
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
    private ListInteractor interactor;

    private AlbumsController listController;
    public AlbumsAdapter listAdapter;

    protected ArrayList<HashMap<String, String>> tracklist = new ArrayList<>();
    protected ArrayList<HashMap<String, String>> albumList = new ArrayList<>();

    private int albumsToAddLoop;
    private int onTrackRequestCompletedLoop;

    private String requestType;

    public AlbumsPresenter(Context context, AlbumsController listController, LayoutInflater inflater){
        this.context = context;
        this.listController = listController;
        this.interactor = new ListInteractor();
        this.listAdapter = new AlbumsAdapter(listController, getListData(), inflater);
        this.listAdapter.selectAll = true;
        this.listAdapter.clearCheckboxes();

        albumsToAddLoop = 0;
        onTrackRequestCompletedLoop = 0;
    }

    public List<TrackModel> getListData(){
        return interactor.getListData();
    }

    //Sets the data used to generate the listview
    public void setListData() {
        interactor.setListData(albumList);
    }

    public void populateList(String searchTerm){
        String hierarchy =  MainActivity.sharedPreferences.getString("hierarchy", "none");
        String unformattedURL = "";

        switch (hierarchy) {
            case "artists":
                unformattedURL = context.getString(R.string.albumsByArtistIDJSONURL);
                break;
            case "albums":
            case "tracks":
                unformattedURL = context.getString(R.string.albumsByNameJSONURL);
                break;
            case "tracksFloatingMenuArtist":
            case "albumsFloatingMenuArtist":
            case "playlistFloatingMenuArtist":
                unformattedURL = context.getString(R.string.albumsByArtistNameJSONURL);
                break;
        }

        requestType = "album";
        String url = String.format(unformattedURL, searchTerm).replace("&amp;", "&").replace(" ", "+");
        JSONParser albumParser = new JSONParser(this);
        albumParser.execute(url);
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
            JSONArray results = json.getJSONArray(context.getString(R.string.TAG_RESULTS));
            String hierarchy = MainActivity.sharedPreferences.getString("hierarchy", "none");
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
            setListData();
            listAdapter.notifyDataSetChanged();
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
                newTrackList.addAll(TracklistUtils.restoreTracklist());
                newTrackList.addAll(tracklist);

                new TracklistUtils().execute(newTrackList);
                TracklistUtils.updateTracklist(newTrackList);

                listController.setPlaylistButtonClickable(true);
                if (albumsToAddLoop == 1){
                    Toast.makeText(context,"1 album added to the playlist", Toast.LENGTH_LONG).show();
                } else if(albumsToAddLoop >= 2){
                    Toast.makeText(context, albumsToAddLoop + " albums added to the playlist", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e("trackRequest", "Exception: " + e.getMessage());
        }
    }

    public void addAlbumToPlaylist(int numberOfAlbums){
        albumsToAddLoop = 0;
        onTrackRequestCompletedLoop = 0;
        for (int i = 0; i < numberOfAlbums; i++){
            if (listAdapter.listOfCheckboxes.get(i, false)) {
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
}
