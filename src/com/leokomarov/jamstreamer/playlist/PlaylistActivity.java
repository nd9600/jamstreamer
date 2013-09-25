package com.leokomarov.jamstreamer.playlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.leokomarov.jamstreamer.ComplexPreferences;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.media_player.AudioPlayer;
import com.leokomarov.jamstreamer.media_player.AudioPlayerService;

public class PlaylistActivity extends SherlockListActivity implements PlaylistAdapter.CallbackInterface {
	private final String TAG_TRACKLIST = "trackListSaved";
	private ArrayList<HashMap<String, String>> trackList = new ArrayList<HashMap<String, String>>();
	private ListView PlaylistLV;
	private ArrayAdapter<PlaylistModel> PlaylistListAdapter;
	private List<PlaylistModel> PlaylistModel = new ArrayList<PlaylistModel>();
	protected static ActionMode mActionMode;
	
	//See ComplexPreferences docs on Github
    private ArrayList<HashMap<String, String>> restoreTracklist(Bundle savedInstanceState){
    	if (savedInstanceState != null) {
        	@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, String>> trackList = (ArrayList<HashMap<String,String>>)savedInstanceState.get(TAG_TRACKLIST);
        	return trackList;
        } 
        else {
        	ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
    	    		getString(R.string.trackPreferencesFile), MODE_PRIVATE);
        	PlaylistList trackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
        	if (trackPreferencesObject != null){
        		return trackPreferencesObject.trackList;
        	}
        	else {
        		return null;
        	}
        	
        }
    }
    
	private void shuffleTrackList(){
		ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
			getString(R.string.trackPreferencesFile), MODE_PRIVATE);	
		PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
		ArrayList<HashMap<String, String>> trackList = shuffledTrackPreferencesObject.trackList;
		
		Collections.shuffle(trackList);
		PlaylistList shuffledTrackListObject = new PlaylistList();
		shuffledTrackListObject.setTrackList(trackList);  
		trackPreferences.putObject("shuffledTracks", shuffledTrackListObject);
		trackPreferences.commit();
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);     
        setContentView(R.layout.original_empty_list);
        
        //if FIRSTRUN_PREFERENCE doesn't contain "firstrun" or "firstrun" == false
        if (! getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE).contains("firstrun") 
				|| ! getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true) == false) {
        	SharedPreferences firstrunPreference = getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE);
            Editor firstrunEditor = firstrunPreference.edit();
            firstrunEditor.putBoolean("firstrun", false);
            firstrunEditor.commit();
		}     
        
        if (restoreTracklist(savedInstanceState) != null ){
        	trackList = restoreTracklist(savedInstanceState);
        }
        
        PlaylistLV = getListView();
		LayoutInflater inflater = getLayoutInflater();
		ViewGroup header = (ViewGroup)inflater.inflate(R.layout.playlist_header, PlaylistLV, false);
		PlaylistLV.addHeaderView(header, null, false);
		
    	for (HashMap<String, String> map : trackList) {
    		PlaylistModel.add(new PlaylistModel(map));
    	}
    	
    	PlaylistListAdapter = new PlaylistAdapter(this, this, PlaylistModel);	
    	setListAdapter(PlaylistListAdapter);
    	
    	PlaylistLV.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int indexPosition = position - 1;
 
				SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
    	    	SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
    	    	indexPositionEditor.putInt("indexPosition", indexPosition );
    	    	indexPositionEditor.commit();
    	    	
    	    	if(AudioPlayerService.shuffleBoolean == true){
                	AudioPlayerService.shuffleBoolean = false;
                	AudioPlayer.button_shuffle.setImageResource(R.drawable.img_repeat_default);
    	    	}
    			Intent in = new Intent(getApplicationContext(), AudioPlayer.class);
    			in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    			in.putExtra("fromNotification", false);
    			startActivityForResult(in, 1);
                finish();
            }
        });
    	   	
    	PlaylistLV.setOnItemLongClickListener(new OnItemLongClickListener() {
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
				
				CheckBox checkbox = (CheckBox) view.findViewById(R.id.playlist_checkBox);
				checkbox.setChecked(! checkbox.isChecked());
				
				return returnValue;
			}
    	}); 
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
	        inflater.inflate(R.menu.playlist_contextual_menu, menu);
	        return true;
	        }
	    
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	    	ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(PlaylistActivity.this, 
        	    	getString(R.string.trackPreferencesFile), MODE_PRIVATE);;
        	    PlaylistList trackListObject = new PlaylistList();
                int itemId = item.getItemId();
				if (itemId == R.id.removePlaylistItem) {
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
					
					if (trackList != null && ! trackList.isEmpty()){
						shuffleTrackList();
					}
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
					PlaylistAdapter.PlaylistCheckboxList.clear();
			    	PlaylistAdapter.PlaylistCheckboxCount = 0;
					mActionMode.finish();
					mActionMode = null;
					mode.finish();
					mode = null;
					return true;
				} else if (itemId == R.id.deletePlaylist) {
					trackList.clear();
					trackListObject.setTrackList(trackList);
					trackPreferences.putObject("tracks", trackListObject);
					trackPreferences.commit();
					
					if (trackList != null && ! trackList.isEmpty()){
						shuffleTrackList();
					}
					PlaylistModel.clear();
					
					PlaylistAdapter.PlaylistCheckboxList.clear();
			    	PlaylistAdapter.PlaylistCheckboxCount = 0;
					PlaylistListAdapter.notifyDataSetChanged();
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
	}
	
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	ArrayList<HashMap<String, String>> trackList = restoreTracklist(savedInstanceState);
    	savedInstanceState.putSerializable(TAG_TRACKLIST, trackList); 
    }
    
	public boolean onOptionsItemSelected(MenuItem item) { 
	        int itemId = item.getItemId();
			if (itemId == android.R.id.home) {
				onBackPressed();
				return true;
			}
	    return super.onOptionsItemSelected((android.view.MenuItem) item);
	}

}