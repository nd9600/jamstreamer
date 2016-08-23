package com.leokomarov.jamstreamer.discography;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.media_player.AudioPlayer;
import com.leokomarov.jamstreamer.media_player.AudioPlayerService;
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;
import com.leokomarov.jamstreamer.playlist.PlaylistList;
import com.leokomarov.jamstreamer.common.ActionBarListActivity;
import com.leokomarov.jamstreamer.utils.ComplexPreferences;
import com.leokomarov.jamstreamer.utils.JSONParser;
import com.leokomarov.jamstreamer.utils.generalUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TracksByName extends ActionBarListActivity implements JSONParser.CallbackInterface, TracksByNameAdapter.CallbackInterface {
	private ListView TracksByNameLV;
	private ArrayAdapter<TracksByNameModel>TracksByNameListAdapter;
	private List<TracksByNameModel> TracksByNameModel = new ArrayList<>();
	protected static ActionMode mActionMode;
	protected static boolean selectAll;
	protected static boolean selectAllPressed;
	private JSONArray results;
	private ArrayList<HashMap<String, String>> trackList = new ArrayList<>();
	private ArrayList<HashMap<String, String>> albumIDList = new ArrayList<>();
	private ImageButton button_playlist;

    private void putHierarchy(String hierarchy){
		SharedPreferences hierarchyPreference = getSharedPreferences(getString(R.string.hierarchyPreferences), 0);
    	SharedPreferences.Editor hierarchyEditor = hierarchyPreference.edit();
    	hierarchyEditor.putString("hierarchy", hierarchy);
		hierarchyEditor.apply();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getSupportActionBar();//.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.original_empty_list);
        selectAllPressed = false;
        selectAll = false;
		
		Intent intent = getIntent();
		SharedPreferences hierarchyPreference = getSharedPreferences(getString(R.string.hierarchyPreferences), 0);
		String hierarchy = hierarchyPreference.getString("hierarchy", "none");
		String searchTerm = "";
		String unformattedURL = "";

        switch (hierarchy) {
            case "artists":
            case "albums":
            case "tracksFloatingMenuAlbum":
                //Gets the search term from the intent using the extra data put in with the TAG_ALBUM_ID tag
                searchTerm = intent.getStringExtra(getString(R.string.TAG_ALBUM_ID));
                unformattedURL = getResources().getString(R.string.tracksByAlbumIDJSONURL);
                break;
            case "tracks":
                searchTerm = intent.getStringExtra(getString(R.string.TAG_TRACK_NAME)).replace(" ", "+");
                unformattedURL = getString(R.string.tracksByNameJSONURL);
                break;
            case "topTracksPerWeek":
                unformattedURL = getString(R.string.tracksByPopularityPerWeek);
                break;
            case "playlistFloatingMenuAlbum":
                searchTerm = intent.getStringExtra(getString(R.string.TAG_ALBUM_NAME)).replace(" ", "+");
                unformattedURL = getString(R.string.tracksByAlbumNameJSONURL);
                break;
        }

		String url = String.format(unformattedURL, searchTerm).replace("&amp;", "&");		
		JSONParser jParser = new JSONParser(this);
		jParser.execute(url);
	}

	@Override
	public void onRequestCompleted(JSONObject json) {
		try {
			results = json.getJSONArray(getString(R.string.TAG_RESULTS));
			SharedPreferences hierarchyPreference = getSharedPreferences(getString(R.string.hierarchyPreferences), 0);
			String hierarchy = hierarchyPreference.getString("hierarchy", "none");
			if (hierarchy.equals("artists") || hierarchy.equals("albums") || hierarchy.equals("tracksFloatingMenuAlbum") || hierarchy.equals("playlistFloatingMenuAlbum")){
				for(int i = 0; i < results.length(); i++) {
					JSONArray tracksArray = results.getJSONObject(i).getJSONArray("tracks");
					String artistName = results.getJSONObject(i).getString(getString(R.string.TAG_ARTIST_NAME));
					String albumName = results.getJSONObject(i).getString("name");
					String albumID = results.getJSONObject(i).getString("id");
					for(int j = 0; j < tracksArray.length(); j++) {
						JSONObject trackInfo = tracksArray.getJSONObject(j);
					
						String trackID = trackInfo.getString(getString(R.string.TAG_TRACK_ID));
						String trackName = trackInfo.getString(getString(R.string.TAG_TRACK_NAME));
						long durationLong = Long.valueOf(trackInfo.getString(getString(R.string.TAG_TRACK_DURATION)));
						String trackDuration = String.format(Locale.US, "%d:%02d", durationLong / 60,durationLong % 60);
						
						HashMap<String, String> albumIDMap = new HashMap<>();
						albumIDMap.put("albumID", albumID);
						albumIDList.add(albumIDMap);
					
						HashMap<String, String> trackMap = new HashMap<>();
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
					
					String trackID = trackInfo.getString(getString(R.string.TAG_TRACK_ID));
					String trackName = trackInfo.getString(getString(R.string.TAG_TRACK_NAME));
					long durationLong = Long.valueOf(trackInfo.getString(getString(R.string.TAG_TRACK_DURATION)));
					String trackDuration = String.format(Locale.US, "%d:%02d", durationLong / 60,durationLong % 60);
					String artistName = trackInfo.getString(getString(R.string.TAG_ARTIST_NAME));
					String albumName = trackInfo.getString(getString(R.string.TAG_ALBUM_NAME));
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
					trackList.add(trackMap);
				}
			}
		} catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
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
	    		getString(R.string.trackPreferences), MODE_PRIVATE);

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
	    			ArrayList<HashMap<String, String>> newTrackList = new ArrayList<>();
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
	    	    	indexPositionEditor.apply();
	    	    	
	    	    	if(AudioPlayerService.shuffleBoolean){
	                	AudioPlayerService.shuffleBoolean = false;
	    	    	}
	    	        
	    			Intent in = new Intent(getApplicationContext(), AudioPlayer.class);
	    			in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    			in.putExtra("fromNotification", false);
	    			startActivityForResult(in, 3);
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
        	selectAllPressed = false;
        	if (mActionMode == null){
                System.out.print("Started action bar");
        	}
        	CheckBox checkbox = (CheckBox) viewClicked.findViewById(R.id.tracks_by_name_checkBox);
			checkbox.setChecked(! checkbox.isChecked());
        	return true;
		} else if (menuID == R.id.tracksFloating_viewArtist) {
			putHierarchy("tracksFloatingMenuArtist");
			String artistName = trackList.get(indexPosition).get("trackArtist");
			Intent artistsIntent = new Intent(getApplicationContext(), AlbumsByName.class);
			artistsIntent.putExtra(getString(R.string.TAG_ARTIST_NAME), artistName);
			startActivityForResult(artistsIntent, 2);
			return true;
		} else if (menuID == R.id.tracksFloating_viewAlbum) {
			putHierarchy("tracksFloatingMenuAlbum");
			String albumID = albumIDList.get(indexPosition).get("albumID");
			Intent albumsIntent = new Intent(getApplicationContext(), TracksByName.class);
			albumsIntent.putExtra(getString(R.string.TAG_ALBUM_ID), albumID);
			startActivityForResult(albumsIntent, 3);
			return true;
		} else {
			return false;
		}
		
    }
	
	public void checkboxTicked(View view){
    	selectAllPressed = false;
    }
	
	public void callActionBar(){
		if (mActionMode == null) {
            System.out.print("Called action bar");
			//mActionMode = startActionMode(mActionModeCallback);
		}
	}

    /*
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){
		@Override 
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	       	return true;
	    }
	    
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			menu.clear();
			MenuInflater inflater = getSupportMenuInflater();
        	inflater.inflate(R.menu.tracks_contextual_menu, menu);
        	if (! selectAll){
	        	menu.findItem(R.id.tracksSelectAllTracks).setTitle("Select all");
	        }
	        else {
	        	menu.findItem(R.id.tracksSelectAllTracks).setTitle("Select none");
	        }	
			return true;
		}

		@Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			int itemId = item.getItemId();
			if (itemId == R.id.tracksSelectAllTracks) {
            	selectAllPressed = true;
            	selectAll = !selectAll;
            	mActionMode.invalidate();
            	
              	for (int i = 1; i < TracksByNameLV.getCount(); i++) {
              		View view = TracksByNameLV.getChildAt(i);
              		int indexPosition = i - 1;
              		
              		if (view != null) {
              			CheckBox checkbox = (CheckBox) view.findViewById(R.id.tracks_by_name_checkBox);
              			
              			if (selectAll && ! checkbox.isChecked()){
              				checkbox.setChecked(true);
              			}
              			else if (! selectAll && checkbox.isChecked()){
              				checkbox.setChecked(false);
              			}
              		}
              		
              		if (selectAll && ! TracksByNameAdapter.listOfCheckboxes.get(indexPosition, false) ){
              			TracksByNameAdapter.listOfCheckboxes.put(indexPosition, true);
              			TracksByNameAdapter.tickedCheckboxCounter++;
					}
              		else if (! selectAll && TracksByNameAdapter.listOfCheckboxes.get(indexPosition, false) ){
              			TracksByNameAdapter.listOfCheckboxes.put(indexPosition, false);
              			TracksByNameAdapter.tickedCheckboxCounter--;
					}
              	}
              	if (TracksByNameAdapter.tickedCheckboxCounter == 0){
              		if (mActionMode != null){
              			mActionMode.finish();
              		}
                }
				else if (TracksByNameAdapter.tickedCheckboxCounter != 0){
					callActionBar();
					mActionMode.setTitle(TracksByNameAdapter.tickedCheckboxCounter + " selected");
                }
              	
               	return true;
            } else if (itemId == R.id.addTrackToPlaylist) {
            	selectAllPressed = false;
				int tracksByNameLVLength = TracksByNameLV.getCount();
				SparseBooleanArray checkboxList = TracksByNameAdapter.listOfCheckboxes;
				ArrayList<HashMap<String, String>> tracksToAddList = new ArrayList<>();
				for (int i = 0; i < tracksByNameLVLength; i++){
					if (checkboxList.get(i, false)) {
						tracksToAddList.add(trackList.get(i));
					 }
				}
				ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(TracksByName.this,
	    	    	getString(R.string.trackPreferences), MODE_PRIVATE);

				ArrayList<HashMap<String, String>> newTrackList = new ArrayList<>();
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
				//mode = null;
				return true;
			} else {
				return false;
			}
        }

	    @Override
        public void onDestroyActionMode(ActionMode mode) {
	    	if (mActionMode != null){
	    		mActionMode = null;
	    		//mode = null;
	    	}
        }
	};
	*/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        generalUtils.clearCheckboxes(requestCode);
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
