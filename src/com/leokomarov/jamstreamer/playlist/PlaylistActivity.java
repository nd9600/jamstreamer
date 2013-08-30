package com.leokomarov.jamstreamer.playlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
import android.widget.ListView;
import android.widget.Toast;

import com.leokomarov.jamstreamer.ComplexPreferences;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.media_player.AudioPlayer;
 
public class PlaylistActivity extends ListActivity implements PlaylistAdapter.CallbackInterface {
	private final String DEBUG = "PlaylistActivity";
	private final String TAG_TRACKLIST = "trackListSaved";
	private ArrayList<HashMap<String, String>> trackList = new ArrayList<HashMap<String, String>>();
	
	//See ComplexPreferences docs on Github
    protected ArrayList<HashMap<String, String>> restoreTracklist(Bundle savedInstanceState){
    	if (savedInstanceState != null) {
        	@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, String>> trackList = (ArrayList<HashMap<String,String>>)savedInstanceState.get(TAG_TRACKLIST);
        	Log.v(DEBUG, "Restoring trackList from savedInstanceState");
        	return trackList;
        } 
        else {
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
        
        //if FIRSTRUN_PREFERENCE doesn't contain "firstrun" or "firstrun" == false
        if (! getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE).contains("firstrun") 
				|| ! getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true) == false) {
			Toast.makeText(getApplicationContext(), "Long-press on an entry to edit the playlist", Toast.LENGTH_LONG).show();
        	SharedPreferences firstrunPreference = getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE);
            Editor firstrunEditor = firstrunPreference.edit();
            firstrunEditor.putBoolean("firstrun", false);
            firstrunEditor.commit();
		}     
        
        trackList = restoreTracklist(savedInstanceState);

        final ListView PlaylistLV = getListView();
		LayoutInflater inflater = getLayoutInflater();
		ViewGroup header = (ViewGroup)inflater.inflate(R.layout.playlist_header, PlaylistLV, false);
		PlaylistLV.addHeaderView(header, null, false);
		PlaylistLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		
		final List<PlaylistModel> PlaylistModel = new ArrayList<PlaylistModel>();
    	for (HashMap<String, String> map : trackList) {
    		PlaylistModel.add(new PlaylistModel(map));
    	}
    	
    	final ArrayAdapter<PlaylistModel> PlaylistListAdapter = new PlaylistAdapter(this, this, PlaylistModel);	
    	setListAdapter(PlaylistListAdapter);
    	
    	PlaylistLV.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int indexPosition = position - 1;
 
				SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
    	    	SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
    	    	indexPositionEditor.putInt("indexPosition", indexPosition );
    	    	indexPositionEditor.commit();
    	        
    			Intent in = new Intent(getApplicationContext(), AudioPlayer.class);
    			startActivityForResult(in, 1);
                finish();
            }
        });     
    		
    	PlaylistLV.setMultiChoiceModeListener(new MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            	int numberOfCheckedItems = PlaylistAdapter.PlaylistCheckboxCount;
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
                inflater.inflate(R.menu.playlist_contextual_menu, menu);               
                return true;
            }
            
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(PlaylistActivity.this, 
            	    	getString(R.string.trackPreferencesFile), MODE_PRIVATE);;
            	    PlaylistList trackListObject = new PlaylistList();
                    switch (item.getItemId()) {
                    	case R.id.removePlaylistItem:                   		
                    		int PlaylistLVLength = PlaylistLV.getCount();
	                		SparseBooleanArray checkboxList = PlaylistAdapter.PlaylistCheckboxList;
	                		ArrayList<Integer> tracksToDelete = new ArrayList<Integer>();
	                		
	                		for (int i = 0; i < PlaylistLVLength; i++){
	                			if (checkboxList.get(i, false)) {
	                				tracksToDelete.add(i);
	                			}
	                		}
	                		
	                		Collections.sort(tracksToDelete, Collections.reverseOrder());
	                		for (int i : tracksToDelete){
	                		    trackList.remove(i);
	                		}
	                		
	                		trackListObject.setTrackList(trackList);    
                        	trackPreferences.putObject("tracks", trackListObject);
                        	trackPreferences.commit();
                        	
                        	PlaylistModel.clear();
                        	for (HashMap<String, String> map : trackList) {
                        		PlaylistModel.add(new PlaylistModel(map));
                        	}
                        	
                        	if (tracksToDelete.size() == 1){
	                    		Toast.makeText(getApplicationContext(),"1 track removed from the playlist", Toast.LENGTH_LONG).show();
	    	                } else if(tracksToDelete.size() >= 2){
	    	                	Toast.makeText(getApplicationContext(),tracksToDelete.size() + " tracks removed from the playlist", Toast.LENGTH_LONG).show();
	    	                }
                        	
                        	PlaylistListAdapter.notifyDataSetChanged();
                        	
                            mode.finish();
                            return true;
                    	case R.id.deletePlaylist:
                    		trackList.clear();
                            trackListObject.setTrackList(trackList);    
                        	trackPreferences.putObject("tracks", trackListObject);
                        	trackPreferences.commit();
                        	
                        	PlaylistModel.clear();
                        	PlaylistListAdapter.notifyDataSetChanged();
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
	
	public void setListItemChecked(int position, boolean checked){
		ListView PlaylistLV = getListView();
		PlaylistLV.setItemChecked(position, checked);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
	    	PlaylistAdapter.PlaylistCheckboxList.clear();
	    	PlaylistAdapter.PlaylistCheckboxCount = 0;
	    }
	}
	
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	Log.v(DEBUG, "Saving trackList from savedInstanceState to savedInstanceState");
    	ArrayList<HashMap<String, String>> trackList = restoreTracklist(savedInstanceState);
    	savedInstanceState.putSerializable(TAG_TRACKLIST, trackList); 
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