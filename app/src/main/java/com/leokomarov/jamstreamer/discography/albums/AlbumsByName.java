package com.leokomarov.jamstreamer.discography.albums;

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
import com.leokomarov.jamstreamer.discography.tracks.TracksByName;
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;
import com.leokomarov.jamstreamer.playlist.PlaylistList;
import com.leokomarov.jamstreamer.searches.ArtistsParser;
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

public class AlbumsByName extends ActionBarListActivity implements JSONParser.CallbackInterface, AlbumsByNameAdapter.CallbackInterface,
		AlbumsByNameTrackParser.CallbackInterface {
	private ListView AlbumsByNameLV;
	private ArrayAdapter<AlbumsByNameModel>AlbumsByNameListAdapter;
	private List<AlbumsByNameModel> AlbumsByNameModel = new ArrayList<>();
	protected static ActionMode mActionMode;
	private JSONArray results;
	private ArrayList<HashMap<String, String>> albumList = new ArrayList<>();
	private ArrayList<HashMap<String, String>> trackList = new ArrayList<>();
	private ImageButton button_playlist;
	private int albumsToAddLoop = 0;
	private int onTrackRequestCompletedLoop = 0;
	protected static boolean selectAll;
	protected static boolean selectAllPressed;
	
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
                searchTerm = intent.getStringExtra(ArtistsParser.TAG_ARTIST_ID);
                unformattedURL = getString(R.string.albumsByArtistIDJSONURL);
                break;
            case "albums":
            case "tracks":
                searchTerm = intent.getStringExtra(getString(R.string.TAG_ALBUM_NAME));
                unformattedURL = getString(R.string.albumsByNameJSONURL);
                break;
            case "tracksFloatingMenuArtist":
            case "albumsFloatingMenuArtist":
            case "playlistFloatingMenuArtist":
                searchTerm = intent.getStringExtra(getString(R.string.TAG_ARTIST_NAME));
                unformattedURL = getString(R.string.albumsByArtistNameJSONURL);
                break;
        }
		
    	String url = String.format(unformattedURL, searchTerm).replace("&amp;", "&").replace(" ", "+");
		JSONParser jParser = new JSONParser(this);
		jParser.execute(url);
	}
		
	@Override
	public void onRequestCompleted(JSONObject json) {
		try {
			results = json.getJSONArray(getString(R.string.TAG_RESULTS));
			SharedPreferences hierarchyPreference = getSharedPreferences(getString(R.string.hierarchyPreferences), 0);
			String hierarchy = hierarchyPreference.getString("hierarchy", "none");
			if (hierarchy.equals("artists") || hierarchy.equals("albumsFloatingMenuArtist") || hierarchy.equals("tracksFloatingMenuArtist") || hierarchy.equals("playlistFloatingMenuArtist")){
				for(int i = 0; i < results.length(); i++) {
					JSONArray albumsArray = results.getJSONObject(i).getJSONArray("albums");
					String artistName = results.getJSONObject(i).getString("name");
					for(int j = 0; j < albumsArray.length(); j++) {
						JSONObject albumInfo = albumsArray.getJSONObject(j);
						
						HashMap<String, String> map = new HashMap<>();
						String albumName = albumInfo.getString(getString(R.string.TAG_ALBUM_NAME));
						String albumID = albumInfo.getString(getString(R.string.TAG_ALBUM_ID));
						map.put("albumArtist", artistName);
						map.put("albumName", albumName);
						map.put("albumID", albumID);
						albumList.add(map);
					}
				}
			}
			else if (hierarchy.equals("albums")){
				for(int i = 0; i < results.length(); i++) {
					JSONObject albumInfo= results.getJSONObject(i);
					
					HashMap<String, String> map = new HashMap<>();
					String artistName = albumInfo.getString(getString(R.string.TAG_ARTIST_NAME));
					String albumName = albumInfo.getString(getString(R.string.TAG_ALBUM_NAME));
					String albumID = albumInfo.getString(getString(R.string.TAG_ALBUM_ID));
					map.put("albumArtist", artistName);
					map.put("albumName", albumName);
					map.put("albumID", albumID);
					albumList.add(map);
				}
			}
		} catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
		}
		
		if (json == null || json.isNull("results")) {
	        Toast.makeText(getApplicationContext(), "Please retry, there has been an error downloading the album list", Toast.LENGTH_SHORT).show();
		}
		else if (json.has("results") && albumList.isEmpty()){
			Toast.makeText(getApplicationContext(), "There are no albums matching this search", Toast.LENGTH_SHORT).show();
        }
		else {	
			AlbumsByNameLV = getListView();
			LayoutInflater inflater = getLayoutInflater();
			ViewGroup header = (ViewGroup)inflater.inflate(R.layout.albums_by_name_header, AlbumsByNameLV, false);
			AlbumsByNameLV.addHeaderView(header, null, false);
			
	    	for (HashMap<String, String> map : albumList) {
	    		AlbumsByNameModel.add(new AlbumsByNameModel(map));
	    	}
	    	
	    	AlbumsByNameListAdapter = new AlbumsByNameAdapter(this, this, AlbumsByNameModel );	
	    	setListAdapter(AlbumsByNameListAdapter);
	    	registerForContextMenu(AlbumsByNameLV);
			
			button_playlist = (ImageButton) findViewById(R.id.albums_by_name_btnPlaylist);    	
	    	button_playlist.setOnClickListener(new View.OnClickListener() {
				@Override
	            public void onClick(View v) {
	                Intent button_playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
	                startActivityForResult(button_playlistIntent, 1);
	            }
			});

	    	AlbumsByNameLV.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					putHierarchy("albums");
					String albumID = albumList.get(position - 1).get("albumID");
					Intent intent = new Intent(getApplicationContext(), TracksByName.class);
					intent.putExtra(getString(R.string.TAG_ALBUM_ID), albumID);
					startActivityForResult(intent, 2);
				}
			});
	    		    	
		}
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        getMenuInflater().inflate(R.menu.albums_floating_menu , menu);
    }
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        View viewClicked = info.targetView;
        int indexPosition = info.position - 1;
 
        int menuID = item.getItemId();
        if (menuID == R.id.albumsFloating_selectAlbum){
        	selectAllPressed = false;
        	callActionBar();
        	CheckBox checkbox = (CheckBox) viewClicked.findViewById(R.id.albums_by_name_checkBox);
			checkbox.setChecked(! checkbox.isChecked());
        	return true;
		} else if (menuID == R.id.albumsFloating_viewArtist) {
			putHierarchy("albumsFloatingMenuArtist");
			
			String artistName = albumList.get(indexPosition).get("albumArtist");
			Intent artistsIntent = new Intent(getApplicationContext(), AlbumsByName.class);
			artistsIntent.putExtra(getString(R.string.TAG_ARTIST_NAME), artistName);
			startActivityForResult(artistsIntent, 3);
			return true;
		} else {
			return false;
		}
		
    }
	
	public void callActionBar(){
		if (mActionMode == null) {
            System.out.println("Called action bar");
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
	    	inflater.inflate(R.menu.albums_contextual_menu, menu);  
	        if (! selectAll){
	        	menu.findItem(R.id.albumsSelectAllTracks).setTitle("Select all");
	        }
	        else {
	        	menu.findItem(R.id.albumsSelectAllTracks).setTitle("Select none");
	        }	
			return true;
		}

		@Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        	int itemId = item.getItemId();
        	if (itemId == R.id.albumsSelectAllTracks) {
            	selectAllPressed = true;
            	selectAll = !selectAll;
            	mActionMode.invalidate();
            	
              	for (int i = 1; i < AlbumsByNameLV.getCount(); i++) {
              		View view = AlbumsByNameLV.getChildAt(i);
              		int indexPosition = i - 1;
              		
              		if (view != null) {
              			CheckBox checkbox = (CheckBox) view.findViewById(R.id.albums_by_name_checkBox);
              			
              			if (selectAll && ! checkbox.isChecked()){
              				checkbox.setChecked(true);
              			}
              			else if (! selectAll && checkbox.isChecked()){
              				checkbox.setChecked(false);
              			}
              		}
              		
              		if (selectAll && ! AlbumsByNameAdapter.listOfCheckboxes.get(indexPosition, false) ){
              			AlbumsByNameAdapter.listOfCheckboxes.put(indexPosition, true);
              			AlbumsByNameAdapter.tickedCheckboxCounter++;
					}
              		else if (! selectAll && AlbumsByNameAdapter.listOfCheckboxes.get(indexPosition, false) ){
              			AlbumsByNameAdapter.listOfCheckboxes.put(indexPosition, false);
              			AlbumsByNameAdapter.tickedCheckboxCounter--;
					}
              	}
              	
              	if (AlbumsByNameAdapter.tickedCheckboxCounter == 0){
              		if (mActionMode != null){
              			mActionMode.finish();
              		}
                }
				else if (AlbumsByNameAdapter.tickedCheckboxCounter != 0){
					callActionBar();
					mActionMode.setTitle(AlbumsByNameAdapter.tickedCheckboxCounter + " selected");
                }
              	
               	return true;
            } else if (itemId == R.id.addAlbumToPlaylist) {
            	selectAllPressed = false;
				button_playlist.setClickable(false);
				int AlbumsByNameLVLength = AlbumsByNameLV.getCount();
				SparseBooleanArray checkboxList = AlbumsByNameAdapter.listOfCheckboxes;
				albumsToAddLoop = 0;
				onTrackRequestCompletedLoop = 0;
				for (int i = 0; i < AlbumsByNameLVLength; i++){
					if (checkboxList.get(i, false)) {
						albumsToAddLoop++;
						String albumID = albumList.get(i).get("albumID");
						String unformattedURL = getResources().getString(R.string.tracksByAlbumIDJSONURL);
				    	String url = String.format(unformattedURL, albumID).replace("&amp;", "&");
				    	
				    	Toast.makeText(getApplicationContext(), "Adding album, please wait", Toast.LENGTH_SHORT).show();
				    	AlbumsByNameTrackParser trackParser = new AlbumsByNameTrackParser(AlbumsByName.this, getApplicationContext());
						trackParser.execute(url);     		    		                				
					 }
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
	public void onTrackRequestCompleted(JSONObject json) {
		try {
			JSONArray results = json.getJSONArray(getString(R.string.TAG_RESULTS));
			onTrackRequestCompletedLoop++;
			for(int i = 0; i < results.length(); i++) {
				JSONArray tracksArray = results.getJSONObject(i).getJSONArray("tracks");
				String artistName = results.getJSONObject(i).getString(getString(R.string.TAG_ARTIST_NAME));
				String albumName = results.getJSONObject(i).getString(getString(R.string.TAG_ALBUM_NAME));
				for(int j = 0; j < tracksArray.length(); j++) {
					JSONObject trackInfo = tracksArray.getJSONObject(j);
					
					String trackID = trackInfo.getString(getString(R.string.TAG_TRACK_ID));
					String trackName = trackInfo.getString(getString(R.string.TAG_TRACK_NAME));
					long durationLong = Long.valueOf(trackInfo.getString(getString(R.string.TAG_TRACK_DURATION)));
					String trackDuration = String.format(Locale.US, "%d:%02d", durationLong / 60,durationLong % 60);
					
					HashMap<String, String> trackMap = new HashMap<>();
					trackMap.put("trackID", trackID);
					trackMap.put("trackName", trackName);
					trackMap.put("trackDuration", trackDuration);
					trackMap.put("trackArtist", artistName);
					trackMap.put("trackAlbum", albumName);

					trackList.add(trackMap);				
				}
			}
    	    
    	    if (onTrackRequestCompletedLoop == albumsToAddLoop){
    	    	ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(AlbumsByName.this,
    	    		getString(R.string.trackPreferences), MODE_PRIVATE);

				ArrayList<HashMap<String, String>> newTrackList = new ArrayList<>();
    	    	if (trackPreferences.getObject("tracks", PlaylistList.class) != null){
    	    		newTrackList.addAll(trackPreferences.getObject("tracks", PlaylistList.class).trackList);
    	    	}       
    	    	newTrackList.addAll(trackList);

    	    	PlaylistList trackPreferencesObject = new PlaylistList();
        		trackPreferencesObject.setTrackList(newTrackList);
        	    trackPreferences.putObject("tracks", trackPreferencesObject);
        	    trackPreferences.commit();   
        		
        		Collections.shuffle(newTrackList);
        		PlaylistList shuffledTrackListObject = new PlaylistList();
        		shuffledTrackListObject.setTrackList(newTrackList);  
        		trackPreferences.putObject("shuffledTracks", shuffledTrackListObject);
        		trackPreferences.commit();
    	    
    	    	button_playlist.setClickable(true);
    	    	if (albumsToAddLoop == 1){
            		Toast.makeText(getApplicationContext(),"1 album added to the playlist", Toast.LENGTH_LONG).show();
                } else if(albumsToAddLoop >= 2){
                	Toast.makeText(getApplicationContext(),albumsToAddLoop + " albums added to the playlist", Toast.LENGTH_LONG).show();
                }
    	    }
		} catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        generalUtils.clearCheckboxes(requestCode);
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