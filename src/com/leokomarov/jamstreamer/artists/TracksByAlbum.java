package com.leokomarov.jamstreamer.artists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
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
import android.widget.TextView;

import com.leokomarov.jamstreamer.JSONParser;
import com.leokomarov.jamstreamer.media_player.AudioPlayer;
import com.leokomarov.jamstreamer.R;

public class TracksByAlbum extends ListActivity implements JSONParser.MyCallbackInterface {
	private String DEBUG = "TracksByAlbum";
	private static final String TAG_RESULTS = "results";
	public static final String TAG_TRACK_ID = "id";
	public static final String TAG_TRACK_NAME = "name";
	public static final String TAG_TRACK_DURATION = "duration";
	JSONArray results = null;
	JSONArray tracksArray = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
		//Hashmap for ListView
	    ArrayList<HashMap<String, String>> trackList = new ArrayList<HashMap<String, String>>();
		try {
			//Getting array of Contacts
			results = json.getJSONArray(TAG_RESULTS);
			//looping through all Contacts
			for(int i = 0; i < results.length(); i++) {
				JSONArray tracksArray = results.getJSONObject(i).getJSONArray("tracks");
				for(int j = 0; j < tracksArray.length(); j++) {
					JSONObject t = tracksArray.getJSONObject(j);
					
					// Storing each json item in variable
					String id = t.getString(TAG_TRACK_ID);
					String name = t.getString(TAG_TRACK_NAME);
					String duration = t.getString(TAG_TRACK_DURATION);
					long durationLong = Long.valueOf(duration);
					
					String timeString = String.format(Locale.US, "%d:%02d", TimeUnit.SECONDS.toMinutes(durationLong),durationLong % 60);
				
					// creating new HashMap
					HashMap<String, String> map = new HashMap<String, String>();
					
					// adding each child node to HashMap key => value
					map.put(TAG_TRACK_ID, id);
					map.put(TAG_TRACK_NAME, name);
					map.put(TAG_TRACK_DURATION, timeString);

					// adding HashList to ArrayList
					trackList.add(map);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
					
		//Selecting single ListView item
		ListView lv = getListView();
		LayoutInflater inflater = getLayoutInflater();
		//Adding section header
		ViewGroup header = (ViewGroup)inflater.inflate(R.layout.artists_6header, lv, false);
		lv.addHeaderView(header, null, false);
		ListAdapter adapter = new SimpleAdapter(this, trackList, 
				R.layout.artists_6tracks_in_album_list_item,new String[] {TAG_TRACK_NAME, TAG_TRACK_DURATION, TAG_TRACK_ID}, 
				new int[] {R.id.track_name, R.id.track_duration, R.id.track_id});
		setListAdapter(adapter);

		// Launching new screen on Selecting Single ListItem
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// getting values from selected ListItem
				String trackName = ((TextView) view.findViewById(R.id.track_name)).getText().toString();
				String trackDuration = ((TextView) view.findViewById(R.id.track_duration)).getText().toString();
				String trackID = ((TextView) view.findViewById(R.id.track_id)).getText().toString();
				Log.v(DEBUG,"Track name is " + trackName);
				Log.v(DEBUG,"Track duration is " + trackDuration);
				Log.v(DEBUG,"Track ID is " + trackID);	
				
				//Starting new intent
				Intent in = new Intent(getApplicationContext(), AudioPlayer.class);
				in.putExtra(TAG_TRACK_NAME, trackName);
				in.putExtra(TAG_TRACK_DURATION, trackDuration);
				in.putExtra(TAG_TRACK_ID, trackID);
				startActivity(in);
			}
		});
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