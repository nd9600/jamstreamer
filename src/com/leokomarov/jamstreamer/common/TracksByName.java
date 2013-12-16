package com.leokomarov.jamstreamer.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
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
	protected static String TAG_ARTIST_NAME = "artist_name";
	protected static String TAG_ALBUM_NAME = "name";
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
	
	private void putHierarchy(String hierarchy){
		SharedPreferences hierarchyPreference = getSharedPreferences(getString(R.string.hierarchyPreferences), 0);
    	SharedPreferences.Editor hierarchyEditor = hierarchyPreference.edit();
    	hierarchyEditor.putString("hierarchy", hierarchy);
		hierarchyEditor.commit();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.original_empty_list);
		
		Intent intent = getIntent();
		SharedPreferences hierarchyPreference = getSharedPreferences(getString(R.string.hierarchyPreferences), 0);
		String hierarchy = hierarchyPreference.getString("hierarchy", "none");
		String searchTerm = new String();
		String unformattedURL = new String();
		if (hierarchy.equals("artists") || hierarchy.equals("albums")){
			searchTerm = intent.getStringExtra(AlbumsByName.TAG_ALBUM_ID);
			unformattedURL = getResources().getString(R.string.tracksByAlbumIDJSONURL);
		}
		else if (hierarchy.equals("tracks")){
			searchTerm = intent.getStringExtra(TracksSearch.TAG_TRACK_NAME).replace(" ", "+");
			unformattedURL = getResources().getString(R.string.tracksByNameJSONURL);
		}
		else if (hierarchy.equals("topTracksPerWeek")){
			unformattedURL = getResources().getString(R.string.tracksByPopularityPerWeek);
		}
		else if (hierarchy.equals("tracksFloatingMenuAlbum")){
			searchTerm = intent.getStringExtra(TracksByName.TAG_ALBUM_NAME).replace(" ", "+");
			unformattedURL = getResources().getString(R.string.tracksByAlbumNameJSONURL);
		}
		else if (hierarchy.equals("playlistFloatingMenuAlbum")){
			searchTerm = intent.getStringExtra(PlaylistActivity.TAG_ALBUM_NAME).replace(" ", "+");
			unformattedURL = getResources().getString(R.string.tracksByAlbumNameJSONURL);
		}
		String url = String.format(unformattedURL, searchTerm).replace("&amp;", "&");
		JSONParser jParser = new JSONParser(this);
		jParser.execute(url);
	}

	@Override
	public void onRequestCompleted(JSONObject json) {
		try {
			results = json.getJSONArray(TAG_RESULTS);
			SharedPreferences hierarchyPreference = getSharedPreferences(getString(R.string.hierarchyPreferences), 0);
			String hierarchy = hierarchyPreference.getString("hierarchy", "none");
			if (hierarchy.equals("artists") || hierarchy.equals("albums") || hierarchy.equals("tracksFloatingMenuAlbum") || hierarchy.equals("playlistFloatingMenuAlbum")){
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
			else if (hierarchy.equals("tracks") || hierarchy.equals("topTracksPerWeek")){
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
		}
			
		if (json == null || json.isNull("results")) {
	        Toast.makeText(getApplicationContext(), "Please retry, there has been an error downloading the track list", Toast.LENGTH_SHORT).show();
		}
		else if (json.has("results") && trackList.isEmpty()){
			Toast.makeText(getApplicationContext(), "There are no tracks matching this search", Toast.LENGTH_SHORT).show();
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
	    	registerForContextMenu(TracksByNameLV);
	    	final ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
	    		getString(R.string.trackPreferences), MODE_PRIVATE);;
	    		
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
	    			SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
	    			SharedPreferences hierarchyPreference = getSharedPreferences(getString(R.string.hierarchyPreferences), 0);
	    			String hierarchy = hierarchyPreference.getString("hierarchy", "none");
    				
	    			int indexPosition = 0;
	    			int oldTrackListSize = 0;
	    			if (trackPreferences.getObject("tracks", PlaylistList.class) != null && trackPreferences.getObject("tracks", PlaylistList.class).trackList.size() != 0){
	    				newTrackList.addAll(trackPreferences.getObject("tracks", PlaylistList.class).trackList);
	    				oldTrackListSize = trackPreferences.getObject("tracks", PlaylistList.class).trackList.size();
	    			}
	    				
	    			if (hierarchy.equals("artists") || hierarchy.equals("albums") || hierarchy.equals("tracksFloatingMenuAlbum") || hierarchy.equals("playlistFloatingMenuAlbum")){
	    				newTrackList.addAll(trackList);
	    				indexPosition = oldTrackListSize + position - 1;
	    			}
	    			else if (hierarchy.equals("tracks") || hierarchy.equals("topTracksPerWeek")){
	    				newTrackList.add(trackList.get(position - 1));
	    				indexPosition = oldTrackListSize;
	    			}
	    			
	    			putHierarchy("tracks");
	    			PlaylistList trackPreferencesObject = new PlaylistList();
	    			trackPreferencesObject.setTrackList(newTrackList);
	    	    	trackPreferences.putObject("tracks", trackPreferencesObject);
	    	    	trackPreferences.commit();
	    	    			
	            	Collections.shuffle(newTrackList);
	            	PlaylistList shuffledTrackListObject = new PlaylistList();
	            	shuffledTrackListObject.setTrackList(trackList);  
	            	trackPreferences.putObject("shuffledTracks", shuffledTrackListObject);
	            	trackPreferences.commit();
	    	    	    				    	        
	    	    	SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
	    	    	indexPositionEditor.putInt("indexPosition", indexPosition);
	    	    	indexPositionEditor.commit();
	    	    	
	    	    	if(AudioPlayerService.shuffleBoolean == true){
	                	AudioPlayerService.shuffleBoolean = false;
	    	    	}
	    	        
	    			Intent in = new Intent(getApplicationContext(), AudioPlayer.class);
	    			in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    			in.putExtra("fromNotification", false);
	    			startActivityForResult(in, 2);	
	    		}
	    	});
    	
		}
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        getMenuInflater().inflate(R.menu.tracks_floating_menu , menu);       
    }
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        View viewClicked = info.targetView;
        int indexPosition = info.position - 1;
 
        int menuID = item.getItemId();
        if (menuID == R.id.tracksFloating_selectTrack){
        	if (mActionMode == null){
				mActionMode = startActionMode(mActionModeCallback);
        	}
        	CheckBox checkbox = (CheckBox) viewClicked.findViewById(R.id.tracks_by_name_checkBox);
			checkbox.setChecked(! checkbox.isChecked());
        	return true;
		} else if (menuID == R.id.tracksFloating_viewArtist) {
			putHierarchy("tracksFloatingMenuArtist");
			String artistName = trackList.get(indexPosition).get("trackArtist");
			Intent artistsIntent = new Intent(getApplicationContext(), AlbumsByName.class);
			artistsIntent.putExtra(TAG_ARTIST_NAME, artistName);
			startActivityForResult(artistsIntent, 3);
			return true;
		} else if (menuID == R.id.tracksFloating_viewAlbum) {
			putHierarchy("tracksFloatingMenuAlbum");
			String albumName = trackList.get(indexPosition).get("trackAlbum");
			Intent albumsIntent = new Intent(getApplicationContext(), TracksByName.class);
			albumsIntent.putExtra(TAG_ALBUM_NAME, albumName);
			startActivityForResult(albumsIntent, 4);
			return true;
		} else {
			return false;
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
	    		MenuInflater inflater = getSupportMenuInflater();
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
	    	    	getString(R.string.trackPreferences), MODE_PRIVATE);;
				
				ArrayList<HashMap<String, String>> newTrackList = new ArrayList<HashMap<String, String>>();
    	    	if (trackPreferences.getObject("tracks", PlaylistList.class) != null){
    	    		newTrackList.addAll(trackPreferences.getObject("tracks", PlaylistList.class).trackList);
    	    	}
    	    	newTrackList.addAll(tracksToAddList);
    	    	
    	    	PlaylistList trackPreferencesObject = new PlaylistList(); 
				trackPreferencesObject.setTrackList(newTrackList);
				trackPreferences.putObject("tracks", trackPreferencesObject);
				trackPreferences.commit();
				
				Collections.shuffle(newTrackList);
        		PlaylistList shuffledTrackListObject = new PlaylistList();
        		shuffledTrackListObject.setTrackList(newTrackList);  
        		trackPreferences.putObject("shuffledTracks", shuffledTrackListObject);
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
	    if (requestCode == 1) {
	    	PlaylistAdapter.PlaylistCheckboxList.clear();
	    	PlaylistAdapter.PlaylistCheckboxCount = 0;
	    }
	    if (requestCode == 3) {
	    	AlbumsByNameAdapter.AlbumsByNameCheckboxList.clear();
	    	AlbumsByNameAdapter.AlbumsByNameCheckboxCount = 0;
	    }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 
	        int itemId = item.getItemId();
			if (itemId == android.R.id.home) {
				onBackPressed();
				return true;
			}
	    return super.onOptionsItemSelected(item);
	}

}
