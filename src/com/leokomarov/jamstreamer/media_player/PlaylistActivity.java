package com.leokomarov.jamstreamer.media_player;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.leokomarov.jamstreamer.ComplexPreferences;
import com.leokomarov.jamstreamer.R;
 
public class PlaylistActivity extends ListActivity {
	private String DEBUG = "PlaylistActivity";
	private String TAG_TRACKLIST = "trackListSaved";
	private final String TAG_TRACK_ID = "id";
	private final String TAG_TRACK_NAME = "name";
	private final String TAG_TRACK_DURATION = "duration";
	protected static ArrayList<HashMap<String, String>> trackListData = new ArrayList<HashMap<String, String>>();
    
	//See ComplexPreferences docs on Github
    protected ArrayList<HashMap<String, String>> restoreTracklist(Bundle savedInstanceState){
    	if (savedInstanceState != null) {
        	@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, String>> trackList = (ArrayList<HashMap<String,String>>)savedInstanceState.get(TAG_TRACKLIST);
        	Log.v(DEBUG, "Restoring trackList");
        	return trackList;
        } 
        else {
        	Log.v(DEBUG, "Getting trackList from ComplexPreferences");
        	ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
    	    		getString(R.string.trackPreferencesFile), MODE_PRIVATE);
        	PlaylistList trackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
        	return trackPreferencesObject.trackList;
        }
    }
    
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        setContentView(R.layout.playlist_original_empty_list);
        
        if (! getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE).contains("firstrun") 
				|| ! getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true) == false) {
			Toast.makeText(getApplicationContext(), "Long-press on an entry to edit the playlist", Toast.LENGTH_LONG).show();
        	SharedPreferences firstrunPreference = getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE);
            Editor firstrunEditor = firstrunPreference.edit();
            firstrunEditor.putBoolean("firstrun", false);
            firstrunEditor.commit();
		}
        
        ArrayList<HashMap<String, String>> trackList = restoreTracklist(savedInstanceState);
        Log.v(DEBUG, "tracklist in PlaylistActivity is " + trackList);
        Log.v(DEBUG, "tracklist size in PlaylistActivity is " + trackList.size() );

        Log.v(DEBUG, "tracklistData before possible deletion is " + trackListData);
        if (! trackListData.isEmpty() ){
        	trackListData.clear();
        }
        Log.v(DEBUG, "tracklistData after possible deletion is " + trackListData);
        for (int i = 0; i < trackList.size(); i++) {
            HashMap<String, String> song = trackList.get(i);
             trackListData.add(song);
        }
        Log.v(DEBUG, "tracklistData after addition is " + trackListData);

 
        ListView playlistLV = getListView();
		LayoutInflater inflater = getLayoutInflater();
		ViewGroup header = (ViewGroup)inflater.inflate(R.layout.playlist_header, playlistLV, false);
		playlistLV.addHeaderView(header, null, false);
		
        ListAdapter playlistAdapter = new SimpleAdapter(this, trackListData, 
        		R.layout.playlist_by_list_item, new String[] {TAG_TRACK_NAME, TAG_TRACK_DURATION, TAG_TRACK_ID},
        		new int[] {R.id.playlist_track_name, R.id.playlist_track_duration, R.id.playlist_track_id });
        setListAdapter(playlistAdapter);
 
        playlistLV.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String trackName = ((TextView) view.findViewById(R.id.playlist_track_name)).getText().toString();
				String trackDuration = ((TextView) view.findViewById(R.id.playlist_track_duration)).getText().toString();
				String trackID = ((TextView) view.findViewById(R.id.playlist_track_id)).getText().toString();
				int indexPosition = position - 1;
				Log.v(DEBUG,"Track name is " + trackName);
				Log.v(DEBUG,"Track duration is " + trackDuration);
				Log.v(DEBUG,"Track ID is " + trackID);
				Log.v(DEBUG,"position is " + position);
				Log.v(DEBUG,"indexPosition is " + indexPosition);
 
				SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
    	    	SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
    	    	indexPositionEditor.putInt("indexPosition", indexPosition );
    	    	indexPositionEditor.commit();
    	        
    			Intent in = new Intent(getApplicationContext(), AudioPlayer.class);
    			startActivity(in);
                finish();
            }
        });
        
        playlistLV.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            	//Dialog goes here, see Rainmeter notes
            	Log.v(DEBUG, "Long-pressed item");
            	return true;
            }
        });
        
    }
	
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	Log.v(DEBUG, "Saving trackList from savedInstanceState to savedInstanceState");
    	ArrayList<HashMap<String, String>> trackList = restoreTracklist(savedInstanceState);
    	savedInstanceState.putSerializable(TAG_TRACKLIST, trackList); 
    }
}