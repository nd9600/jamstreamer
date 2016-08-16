package com.leokomarov.jamstreamer.playlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.leokomarov.jamstreamer.ComplexPreferences;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.AlbumsByName;
import com.leokomarov.jamstreamer.common.AlbumsByNameAdapter;
import com.leokomarov.jamstreamer.common.TracksByName;
import com.leokomarov.jamstreamer.common.TracksByNameAdapter;
import com.leokomarov.jamstreamer.media_player.AudioPlayer;
import com.leokomarov.jamstreamer.media_player.AudioPlayerService;

public class PlaylistActivity extends SherlockListActivity implements PlaylistAdapter.CallbackInterface {
	private final String TAG_TRACKLIST = "trackListSaved";
	public static String TAG_ARTIST_NAME = "artist_name";
	public static String TAG_ALBUM_NAME = "name";
	private ArrayList<HashMap<String, String>> trackList = new ArrayList<HashMap<String, String>>();
	protected static ListView PlaylistLV;
	private ArrayAdapter<PlaylistModel> PlaylistListAdapter;
	private List<PlaylistModel> PlaylistModel = new ArrayList<PlaylistModel>();
	protected static ActionMode mActionMode;
	protected static boolean selectAll;
	protected static boolean selectAllPressed;
	
	//See ComplexPreferences docs on Github
    private ArrayList<HashMap<String, String>> restoreTracklist(Bundle savedInstanceState){
    	if (savedInstanceState != null) {
        	@SuppressWarnings("unchecked")
			//ArrayList<HashMap<String, String>> trackList = (ArrayList<HashMap<String,String>>)savedInstanceState.get(TAG_TRACKLIST);
        	ArrayList<HashMap<String, String>> trackList = (ArrayList<HashMap<String, String>>)savedInstanceState.getSerializable(TAG_TRACKLIST);
        	return trackList;
        } 
        else {
        	ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
    	    		getString(R.string.trackPreferences), MODE_PRIVATE);
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
			getString(R.string.trackPreferences), MODE_PRIVATE);	
		PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
		ArrayList<HashMap<String, String>> trackList = shuffledTrackPreferencesObject.trackList;
		
		Collections.shuffle(trackList);
		PlaylistList shuffledTrackListObject = new PlaylistList();
		shuffledTrackListObject.setTrackList(trackList);  
		trackPreferences.putObject("shuffledTracks", shuffledTrackListObject);
		trackPreferences.commit();
	}
    
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
        selectAllPressed = false;
        selectAll = false;
        
        PlaylistAdapter.PlaylistCheckboxList.clear();
    	PlaylistAdapter.PlaylistCheckboxCount = 0;
        
        ArrayList<HashMap<String, String>> restoredTracklist = restoreTracklist(savedInstanceState);
        if (restoredTracklist != null ){
        	trackList = restoredTracklist;
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
    	registerForContextMenu(PlaylistLV);
    	
    	PlaylistLV.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int indexPosition = position - 1;
 
				SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
    	    	SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
    	    	indexPositionEditor.putInt("indexPosition", indexPosition );
    	    	indexPositionEditor.commit();
    	    	
    	    	if(AudioPlayerService.shuffleBoolean){
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
    }
	
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        getMenuInflater().inflate(R.menu.playlist_floating_menu , menu);
    }
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		trackList = restoreTracklist(null);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();   
        View viewClicked = info.targetView;
        int indexPosition = info.position - 1;
        
        int menuID = item.getItemId();
        if (menuID == R.id.playlistFloating_selectTrack){
        	selectAllPressed = false;
        	callActionBar();
        	CheckBox checkbox = (CheckBox) viewClicked.findViewById(R.id.playlist_checkBox);
			checkbox.setChecked(! checkbox.isChecked());
        	return true;
		} else if (menuID == R.id.playlistFloating_viewArtist) {
			putHierarchy("playlistFloatingMenuArtist");
			String artistName = trackList.get(indexPosition).get("trackArtist");
			Intent artistsIntent = new Intent(getApplicationContext(), AlbumsByName.class);
			artistsIntent.putExtra(TAG_ARTIST_NAME, artistName);
			startActivityForResult(artistsIntent, 2);
			return true;
		} else if (menuID == R.id.playlistFloating_viewAlbum) {
			putHierarchy("playlistFloatingMenuAlbum");
			String albumName = trackList.get(indexPosition).get("trackAlbum");
			Intent albumsIntent = new Intent(getApplicationContext(), TracksByName.class);
			albumsIntent.putExtra(TAG_ALBUM_NAME, albumName);
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
			mActionMode = startActionMode(mActionModeCallback);
		}
	}	
	
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){
		@Override 
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        return true;
	    }
	    
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			menu.clear();
	    	MenuInflater inflater = getSupportMenuInflater();
	        inflater.inflate(R.menu.playlist_contextual_menu, menu);  
	        if (! selectAll){
	        	menu.findItem(R.id.playlistSelectAllTracks).setTitle("Select all");
	        }
	        else if (selectAll){
	        	menu.findItem(R.id.playlistSelectAllTracks).setTitle("Select none");
	        }	
			return true;
		}

	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	    	ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(PlaylistActivity.this, 
        	    getString(R.string.trackPreferences), MODE_PRIVATE);
			PlaylistList trackListObject = new PlaylistList();
            int itemId = item.getItemId();
            if (itemId == R.id.playlistSelectAllTracks) {
            	selectAllPressed = true;
            	selectAll = !selectAll;
            	mActionMode.invalidate();
            	
              	for (int i = 1; i < PlaylistLV.getCount(); i++) {
              		View view = PlaylistLV.getChildAt(i);
              		int indexPosition = i - 1;
              		
              		if (view != null) {
              			CheckBox checkbox = (CheckBox) view.findViewById(R.id.playlist_checkBox);
              			
              			if (selectAll && ! checkbox.isChecked()){
              				checkbox.setChecked(true);
              			}
              			else if (! selectAll && checkbox.isChecked()){
              				checkbox.setChecked(false);
              			}
              		}
              		
              		if (selectAll && ! PlaylistAdapter.PlaylistCheckboxList.get(indexPosition, false) ){
              			PlaylistAdapter.PlaylistCheckboxList.put(indexPosition, true);
              			PlaylistAdapter.PlaylistCheckboxCount++;
					}
              		else if (! selectAll && PlaylistAdapter.PlaylistCheckboxList.get(indexPosition, false) ){
              			PlaylistAdapter.PlaylistCheckboxList.put(indexPosition, false);
              			PlaylistAdapter.PlaylistCheckboxCount--;
					}
              	}
              	
              	if (PlaylistAdapter.PlaylistCheckboxCount == 0){
              		if (mActionMode != null){
              			mActionMode.finish();
              		}
                }
				else if (PlaylistAdapter.PlaylistCheckboxCount != 0){
					callActionBar();
					mActionMode.setTitle(PlaylistAdapter.PlaylistCheckboxCount + " selected");
                }
              	
               	return true;
            } else if (itemId == R.id.removePlaylistItem) {
            	selectAllPressed = false;
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
				//mode = null;
				return true;
			} else if (itemId == R.id.deletePlaylist) {
				selectAllPressed = false;
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
				//mode = null;
				return true;
			} else {
				return false;
			}
	    }

	    @Override
        public void onDestroyActionMode(ActionMode mode) {
	    	selectAllPressed = false;
	    	if (mActionMode != null){
	    		mActionMode = null;
	    		//mode = null;
	    	}
        }
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
	    	PlaylistAdapter.PlaylistCheckboxList.clear();
	    	PlaylistAdapter.PlaylistCheckboxCount = 0;
	    }
		if (requestCode == 2) {
	    	AlbumsByNameAdapter.AlbumsByNameCheckboxList.clear();
	    	AlbumsByNameAdapter.AlbumsByNameCheckboxCount = 0;
	    }
	    if (requestCode == 3) {
	    	TracksByNameAdapter.TracksByNameCheckboxList.clear();
	    	TracksByNameAdapter.TracksByNameCheckboxCount = 0;
	    }
	}
	
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	trackList = restoreTracklist(null);
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