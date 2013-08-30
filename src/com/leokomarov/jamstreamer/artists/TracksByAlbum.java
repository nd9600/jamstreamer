package com.leokomarov.jamstreamer.artists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.leokomarov.jamstreamer.ComplexPreferences;
import com.leokomarov.jamstreamer.JSONParser;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.media_player.AudioPlayer;
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;
import com.leokomarov.jamstreamer.playlist.PlaylistAdapter;
import com.leokomarov.jamstreamer.playlist.PlaylistList;

public class TracksByAlbum extends ListActivity implements JSONParser.CallbackInterface, TracksByAlbumAdapter.CallbackInterface {
	private static final String TAG_RESULTS = "results";
	public static final String TAG_TRACK_ID = "id";
	public static final String TAG_TRACK_NAME = "name";
	public static final String TAG_TRACK_DURATION = "duration";
	JSONArray results = null;
	private ArrayList<HashMap<String, String>> trackList = new ArrayList<HashMap<String, String>>();
	private ImageButton button_playlist;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
		
		//if FIRSTRUN_PREFERENCE doesn't contain "firstrun" or "firstrun" == false
        if (! getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE).contains("firstrun") 
				|| ! getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true) == false) {
			Toast.makeText(getApplicationContext(), "Long-press on a track to add it to the playlist", Toast.LENGTH_LONG).show();
        	SharedPreferences firstrunPreference = getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE);
            Editor firstrunEditor = firstrunPreference.edit();
            firstrunEditor.putBoolean("firstrun", false);
            firstrunEditor.commit();
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
		
	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
			Log.e("TracksByAlbum", "JSONException: " + e.getMessage(), e);
		}
		
		if ( trackList.isEmpty() ) {
        	Toast.makeText(getApplicationContext(), "Please retry, there has been an error downloading the track list", Toast.LENGTH_SHORT).show();
        }
		else {
	    	final ListView tracksByAlbumLV = getListView();
	    	LayoutInflater inflater = getLayoutInflater();

	    	ViewGroup header = (ViewGroup)inflater.inflate(R.layout.artists_6header, tracksByAlbumLV, false);
	    	tracksByAlbumLV.addHeaderView(header, null, false);
	    	tracksByAlbumLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
	    	
	    	List<TracksByAlbumModel> TracksByAlbumModel = new ArrayList<TracksByAlbumModel>();
	    	for (HashMap<String, String> map : trackList) {
	    		TracksByAlbumModel.add(new TracksByAlbumModel(map));
	    	}
	    	
	    	ArrayAdapter<TracksByAlbumModel> tracksByAlbumAdapter = new TracksByAlbumAdapter(this, this, TracksByAlbumModel );	
	    	setListAdapter(tracksByAlbumAdapter);
	    	final ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
	    		getString(R.string.trackPreferencesFile), MODE_PRIVATE);;
	    		
	    	button_playlist = (ImageButton) findViewById(R.id.artists6_btnPlaylist);    	
	    	button_playlist.setOnClickListener(new View.OnClickListener() {
				@Override
	            public void onClick(View v) {
	                Intent button_playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
	                startActivityForResult(button_playlistIntent, 1);
	            }
			});
	    	
	    	tracksByAlbumLV.setOnItemClickListener(new OnItemClickListener() {
	    		@Override
	    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   			
	    			ArrayList<HashMap<String, String>> newTrackList = trackPreferences.getObject("tracks", PlaylistList.class).trackList;
	    			newTrackList.addAll(trackList);
	    			int indexPosition = trackPreferences.getObject("tracks", PlaylistList.class).trackList.size() + position - 1;
	    			
	    			PlaylistList trackPreferencesObject = new PlaylistList();
	    			trackPreferencesObject.setTrackList(newTrackList);
	    	    	trackPreferences.putObject("tracks", trackPreferencesObject);
	    	    	trackPreferences.commit();
	    	    	    				    	        
	    			SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
	    	    	SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
	    	    	indexPositionEditor.putInt("indexPosition", indexPosition);
	    	    	indexPositionEditor.commit();
	    	        
	    			Intent in = new Intent(getApplicationContext(), AudioPlayer.class);
	    			startActivityForResult(in, 2);
	    			
	    		}
	    	});
	    	
	        tracksByAlbumLV.setMultiChoiceModeListener(new MultiChoiceModeListener() {
	            @Override
	            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
	            	int numberOfCheckedItems = TracksByAlbumAdapter.TracksByAlbumCheckboxCount;
	                if (numberOfCheckedItems == 1){
	                	mode.setTitle(numberOfCheckedItems + " track selected");
	                }
	                else if(numberOfCheckedItems >= 2){
	                	mode.setTitle(numberOfCheckedItems + " tracks selected");
	                }
	            }
	            
	            @Override
	            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	                return false;
	            }

	            @Override
	            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	                MenuInflater inflater = mode.getMenuInflater();
	                inflater.inflate(R.menu.tracks_by_album_contextual_menu, menu);               
	                return true;
	            }
	            
	            @Override
	            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	                switch (item.getItemId()) {
	                	case R.id.addTrackToPlaylist:
	                		
	                		int tracksByAlbumLVLength = tracksByAlbumLV.getCount();
	                		SparseBooleanArray checkboxList = TracksByAlbumAdapter.TracksByAlbumCheckboxList;
	                		ArrayList<HashMap<String, String>> tracksToAddList = new ArrayList<HashMap<String, String>>();
             		
	                		for (int i = 0; i < tracksByAlbumLVLength; i++){
	                			if (checkboxList.get(i, false)) {
	                				tracksToAddList.add(trackList.get(i));
	                   			 }
	                		}
	                		PlaylistList trackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
	            	        ArrayList<HashMap<String, String>> newTrackList = trackPreferencesObject.trackList;
	                    	newTrackList.addAll(tracksToAddList);
	                    	trackPreferencesObject.setTrackList(newTrackList);    
	                    	trackPreferences.putObject("tracks", trackPreferencesObject);
	                    	trackPreferences.commit(); 
	                    	
	                    	if (tracksToAddList.size() == 1){
	                    		Toast.makeText(getApplicationContext(),"1 track added to the playlist", Toast.LENGTH_LONG).show();
	    	                } else if(tracksToAddList.size() >= 2){
	    	                	Toast.makeText(getApplicationContext(),tracksToAddList.size() + " tracks added to the playlist", Toast.LENGTH_LONG).show();
	    	                }
              		
	                        mode.finish();
	                        return true;
	                    default:
	                        return false;
	                }
	            }

	            @Override
	            public void onDestroyActionMode(ActionMode mode) {
	            }

	        });
		
		}
	}
	
	public void setListItemChecked(int position, boolean checked){
		ListView tracksByAlbumLV = getListView();
		tracksByAlbumLV.setItemChecked(position, checked);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		TracksByAlbumAdapter.TracksByAlbumCheckboxList.clear();
    	TracksByAlbumAdapter.TracksByAlbumCheckboxCount = 0;
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