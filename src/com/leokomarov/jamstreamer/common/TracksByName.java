package com.leokomarov.jamstreamer.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.leokomarov.jamstreamer.ComplexPreferences;
import com.leokomarov.jamstreamer.JSONParser;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.media_player.AudioPlayer;
import com.leokomarov.jamstreamer.media_player.AudioPlayerService;
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;
import com.leokomarov.jamstreamer.playlist.PlaylistAdapter;
import com.leokomarov.jamstreamer.playlist.PlaylistList;
import com.leokomarov.jamstreamer.searches.TracksSearch;

public class TracksByName extends SherlockListActivity implements JSONParser.CallbackInterface, TracksByNameAdapter.CallbackInterface {
	private String TAG_RESULTS = "results";
	private String TAG_ARTIST_NAME = "artist_name";
	private String TAG_TRACK_ID = "id";
	private String TAG_TRACK_NAME = "name";
	private String TAG_TRACK_DURATION = "duration";
	private ListView TracksByNameLV;
	private ArrayAdapter<TracksByNameModel>TracksByNameListAdapter;
	private List<TracksByNameModel> TracksByNameModel = new ArrayList<TracksByNameModel>();
	protected static ActionMode mActionMode;
	private JSONArray results;
	private ArrayList<HashMap<String, String>> trackList = new ArrayList<HashMap<String, String>>();
	private ImageButton button_playlist;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.original_empty_list);
		
		Intent intent = getIntent();
		SharedPreferences hierarchyPreference = getSharedPreferences(getString(R.string.hierarchyPreferences), 0);
		String hierarchy = hierarchyPreference.getString("hierarchy", "none");
		Log.v("TracksByName","hierarchy is " + hierarchy);
		String searchTerm = new String();
		String unformattedURL = new String();
		if (hierarchy.equals("artists") || hierarchy.equals("albums")){
			searchTerm = intent.getStringExtra(AlbumsByName.TAG_ALBUM_ID);
			unformattedURL = getResources().getString(R.string.tracksByAlbumIDJSONURL);
		}
		else if (hierarchy.equals("tracks")){
			searchTerm = intent.getStringExtra(TracksSearch.TAG_TRACK_NAME);
			unformattedURL = getResources().getString(R.string.tracksByNameJSONURL);
		}
		String url = String.format(unformattedURL, searchTerm).replace("&amp;", "&");
		Log.v("TracksByName","url is " + url);
		JSONParser jParser = new JSONParser(this);
		jParser.execute(url);
	}

	@Override
	public void onRequestCompleted(JSONObject json) {
		try {
			results = json.getJSONArray(TAG_RESULTS);
			SharedPreferences hierarchyPreference = getSharedPreferences(getString(R.string.hierarchyPreferences), 0);
			String hierarchy = hierarchyPreference.getString("hierarchy", "none");
			Log.v("TracksByName","hierarchy is " + hierarchy);
			if (hierarchy.equals("artists") || hierarchy.equals("albums")){
				for(int i = 0; i < results.length(); i++) {
					JSONArray tracksArray = results.getJSONObject(i).getJSONArray("tracks");
					String artistName = results.getJSONObject(i).getString(TAG_ARTIST_NAME);
					String albumName = results.getJSONObject(i).getString("name");
					for(int j = 0; j < tracksArray.length(); j++) {
						JSONObject trackInfo = tracksArray.getJSONObject(j);
					
						String trackID = trackInfo.getString(TAG_TRACK_ID);
						String trackName = trackInfo.getString(TAG_TRACK_NAME);
						long durationLong = Long.valueOf(trackInfo.getString(TAG_TRACK_DURATION));
						String trackDuration = String.format(Locale.US, "%d:%02d", durationLong / 60,durationLong % 60);
					
						HashMap<String, String> trackMap = new HashMap<String, String>();
						trackMap.put("trackID", trackID);
						trackMap.put("trackName", trackName);
						trackMap.put("trackDuration", trackDuration);
						trackMap.put("trackArtist", artistName);
						trackMap.put("trackAlbum", albumName);
						trackList.add(trackMap);
					}
				}
			}
			else if (hierarchy.equals("tracks")){
				for(int i = 0; i < results.length(); i++) {
					JSONObject trackInfo= results.getJSONObject(i);
					
					String trackID = trackInfo.getString(TAG_TRACK_ID);
					String trackName = trackInfo.getString(TAG_TRACK_NAME);
					long durationLong = Long.valueOf(trackInfo.getString(TAG_TRACK_DURATION));
					String trackDuration = String.format(Locale.US, "%d:%02d", durationLong / 60,durationLong % 60);
					String artistName = trackInfo.getString(TAG_ARTIST_NAME);
					String albumName = trackInfo.getString("album_name");
					
					HashMap<String, String> trackMap = new HashMap<String, String>();
					trackMap.put("trackID", trackID);
					trackMap.put("trackName", trackName);
					trackMap.put("trackDuration", trackDuration);
					trackMap.put("trackArtist", artistName);
					trackMap.put("trackAlbum", albumName);
					trackList.add(trackMap);
				}
			}
		} catch (NullPointerException e) {
		} catch (JSONException e) {
			Log.e("TracksByName","JSONException : " + e.getMessage(), e);
		}
		
		Log.v("TracksByName","results.length() is " + results.length());
		Log.v("TracksByName","json.isNull(TAG_RESULTS) is " + json.isNull(TAG_RESULTS));
		
		if (results.length() == 0){
			Toast.makeText(getApplicationContext(), "There are no tracks matching this search", Toast.LENGTH_SHORT).show();
		}
		else if (json.isNull(TAG_RESULTS) && trackList.isEmpty()) {
        	Toast.makeText(getApplicationContext(), "Please retry, there has been an error downloading the track list", Toast.LENGTH_SHORT).show();
        }
		else {
			TracksByNameLV = getListView();
	    		LayoutInflater inflater = getLayoutInflater();

	    		ViewGroup header = (ViewGroup)inflater.inflate(R.layout.tracks_by_name_header, TracksByNameLV, false);
	    		TracksByNameLV.addHeaderView(header, null, false);
	    	
	    		for (HashMap<String, String> map : trackList) {
	    			TracksByNameModel.add(new TracksByNameModel(map));
	    		}
	    	
	    		TracksByNameListAdapter = new TracksByNameAdapter(this, this, TracksByNameModel );	
	    		setListAdapter(TracksByNameListAdapter);
	    		final ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
	    			getString(R.string.trackPreferencesFile), MODE_PRIVATE);;
	    		
	    		button_playlist = (ImageButton) findViewById(R.id.tracks_by_name_btnPlaylist);    	
	    		button_playlist.setOnClickListener(new View.OnClickListener() {
				@Override
	            	public void onClick(View v) {
	                	Intent button_playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
	                	startActivityForResult(button_playlistIntent, 1);
	            	}
			});
	    	
	    		TracksByNameLV.setOnItemClickListener(new OnItemClickListener() {
	    			@Override
	    			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    				ArrayList<HashMap<String, String>> newTrackList = new ArrayList<HashMap<String, String>>();
	    				SharedPreferences hierarchyPreference = getSharedPreferences(getString(R.string.hierarchyPreferences), 0);
	    				String hierarchy = hierarchyPreference.getString("hierarchy", "none");
	    				int indexPosition = 0;
	    				if (trackPreferences.getObject("tracks", PlaylistList.class) != null){
	    					newTrackList.addAll(trackPreferences.getObject("tracks", PlaylistList.class).trackList);
	    					indexPosition = trackPreferences.getObject("tracks", PlaylistList.class).trackList.size();
	    				}
	    				if (hierarchy.equals("artists") || hierarchy.equals("albums")){
	    					newTrackList.addAll(trackList);
	    				}
	    				else if (hierarchy.equals("tracks")){
	    					newTrackList.add(trackList.get(position - 1));
	    				}
	    				indexPosition = indexPosition + position - 1;
	    			
	    				PlaylistList trackPreferencesObject = new PlaylistList();
	    				trackPreferencesObject.setTrackList(newTrackList);
	    	    		trackPreferences.putObject("tracks", trackPreferencesObject);
	    	    		trackPreferences.commit();
	    	    	    				    	        
	    				SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
	    	    		SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
	    	    		indexPositionEditor.putInt("indexPosition", indexPosition);
	    	    		indexPositionEditor.commit();
	    	    	
	    	    		if(AudioPlayerService.shuffleBoolean == true){
	                		AudioPlayerService.shuffleBoolean = false;
	    	    		}
	    	        
	    				Intent in = new Intent(getApplicationContext(), AudioPlayer.class);
	    				startActivityForResult(in, 2);	
	    			}
	    		});
	    	
	    		TracksByNameLV.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					view.setFocusable(false);
					boolean returnValue;
					if (mActionMode == null){
						mActionMode = startActionMode(mActionModeCallback);
						returnValue = false;
					}
					else{
						returnValue = true;
					}
					
					CheckBox checkbox = (CheckBox) view.findViewById(R.id.tracks_by_name_checkBox);
					checkbox.setChecked(! checkbox.isChecked());
					
					return returnValue;
				}
	    		}); 
	    	
		}
	}

	public void callActionBar(){
		if (mActionMode == null) {
			mActionMode = startActionMode(mActionModeCallback);
		}
	}

	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){
		@Override 
	    	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	    		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
	        	inflater.inflate(R.menu.tracks_contextual_menu, menu);
	        	return true;
	      }
	    
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
        	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			int itemId = item.getItemId();
			if (itemId == R.id.addTrackToPlaylist) {
				int tracksByNameLVLength = TracksByNameLV.getCount();
				SparseBooleanArray checkboxList = TracksByNameAdapter.TracksByNameCheckboxList;
				ArrayList<HashMap<String, String>> tracksToAddList = new ArrayList<HashMap<String, String>>();
				for (int i = 0; i < tracksByNameLVLength; i++){
					if (checkboxList.get(i, false)) {
						tracksToAddList.add(trackList.get(i));
					 }
				}
				ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(TracksByName.this,
	    	    		getString(R.string.trackPreferencesFile), MODE_PRIVATE);;
				PlaylistList trackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
				
				ArrayList<HashMap<String, String>> newTrackList = new ArrayList<HashMap<String, String>>();
    	    	if (trackPreferences.getObject("tracks", PlaylistList.class) != null){
    	    		newTrackList.addAll(trackPreferences.getObject("tracks", PlaylistList.class).trackList);
    	    	}
    	    	
    	    	newTrackList.addAll(tracksToAddList);
				trackPreferencesObject.setTrackList(newTrackList);
				trackPreferences.putObject("tracks", trackPreferencesObject);
				trackPreferences.commit();
				if (tracksToAddList.size() == 1){
					Toast.makeText(getApplicationContext(),"1 track added to the playlist", Toast.LENGTH_LONG).show();
				} else if(tracksToAddList.size() >= 2){
					Toast.makeText(getApplicationContext(),tracksToAddList.size() + " tracks added to the playlist", Toast.LENGTH_LONG).show();
				}
				mActionMode.finish();
				mActionMode = null;
				mode.finish();
				mode = null;
				return true;
			} else {
				return false;
			}
        }

	    @Override
        public void onDestroyActionMode(ActionMode mode) {
	    	if (mActionMode != null){
	    		mActionMode = null;
	    		mode = null;
	    	}
        }
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		TracksByNameAdapter.TracksByNameCheckboxList.clear();
    	TracksByNameAdapter.TracksByNameCheckboxCount = 0;
	    if (requestCode == 1) {
	    	PlaylistAdapter.PlaylistCheckboxList.clear();
	    	PlaylistAdapter.PlaylistCheckboxCount = 0;
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
