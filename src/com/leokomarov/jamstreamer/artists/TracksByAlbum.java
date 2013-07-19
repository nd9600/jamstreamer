package com.leokomarov.jamstreamer.artists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.leokomarov.jamstreamer.ComplexPreferences;
import com.leokomarov.jamstreamer.JSONParser;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.media_player.AudioPlayer;
import com.leokomarov.jamstreamer.media_player.PlaylistList;

public class TracksByAlbum extends ListActivity implements JSONParser.MyCallbackInterface {
	private static final String DEBUG = "TracksByAlbum";
	private ArrayList<HashMap<String, String>> trackList = new ArrayList<HashMap<String, String>>();
	private static final String TAG_RESULTS = "results";
	public static final String TAG_TRACK_ID = "id";
	public static final String TAG_TRACK_NAME = "name";
	public static final String TAG_TRACK_DURATION = "duration";
	JSONArray results = null;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
		
		Intent intent = getIntent();
        String albumID = intent.getStringExtra(AlbumsByArtist.TAG_ALBUM_ID); 
        String unformattedURL = getResources().getString(R.string.tracksByAlbumIDJSONURL);
    	String url = String.format(unformattedURL, albumID);
    	url = url.replace("&amp;", "&");
    	
    	setContentView(R.layout.artists_5_tracks_in_album_original_empty_list);
		JSONParser jParser = new JSONParser(this);
		jParser.execute(url);
	}
		
	@Override
	public void onRequestCompleted(JSONObject json) {
		try {
			results = json.getJSONArray(TAG_RESULTS);

			for(int i = 0; i < results.length(); i++) {
				JSONArray tracksArray = results.getJSONObject(i).getJSONArray("tracks");
				for(int j = 0; j < tracksArray.length(); j++) {
					JSONObject t = tracksArray.getJSONObject(j);
					
					String id = t.getString(TAG_TRACK_ID);
					String name = t.getString(TAG_TRACK_NAME);
					String duration = t.getString(TAG_TRACK_DURATION);
					long durationLong = Long.valueOf(duration);
					
					String timeString = String.format(Locale.US, "%d:%02d", TimeUnit.SECONDS.toMinutes(durationLong),durationLong % 60);
				
					HashMap<String, String> trackMap = new HashMap<String, String>();
					trackMap.put(TAG_TRACK_ID, id);
					trackMap.put(TAG_TRACK_NAME, name);
					trackMap.put(TAG_TRACK_DURATION, timeString);

					trackList.add(trackMap);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if ( trackList.isEmpty() ) {
        	Toast.makeText(getApplicationContext(), "Please retry, there has been an error downloading the track list", Toast.LENGTH_SHORT).show();
        }
		else {
			PlaylistList trackPreferencesObject = new PlaylistList();
			trackPreferencesObject.setTrackList(trackList);
			ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
		    		getString(R.string.trackPreferencesFile), MODE_PRIVATE);;
	    	trackPreferences.putObject("tracks", trackPreferencesObject);
	    	trackPreferences.commit();
		
	    	ListView lv = getListView();
	    	LayoutInflater inflater = getLayoutInflater();

	    	ViewGroup header = (ViewGroup)inflater.inflate(R.layout.artists_6header, lv, false);
	    	lv.addHeaderView(header, null, false);
	    	ListAdapter adapter = new SimpleAdapter(this, trackList, 
	    			R.layout.artists_6tracks_in_album_list_item,new String[] {TAG_TRACK_NAME, TAG_TRACK_DURATION, TAG_TRACK_ID}, 
	    			new int[] {R.id.track_name, R.id.track_duration, R.id.track_id});
	    	setListAdapter(adapter);
		
	    	lv.setOnItemClickListener(new OnItemClickListener() {
	    		@Override
	    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    			Log.v(DEBUG, "Starting AudioPlayer");
	    			SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
	    	    	SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
	    	    	indexPositionEditor.putInt("indexPosition", position - 1);
	    	    	indexPositionEditor.commit();
	    	        
	    			Intent in = new Intent(getApplicationContext(), AudioPlayer.class);
	    			startActivity(in);
	    		}
	    	});
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 
	        switch (item.getItemId()) {
	        case android.R.id.home: 
	            onBackPressed();
	            return true;
	        }
	    return super.onOptionsItemSelected(item);
	}

}