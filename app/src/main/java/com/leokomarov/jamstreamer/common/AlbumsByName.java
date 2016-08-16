package com.leokomarov.jamstreamer.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;
import com.leokomarov.jamstreamer.playlist.PlaylistAdapter;
import com.leokomarov.jamstreamer.playlist.PlaylistList;
import com.leokomarov.jamstreamer.searches.AlbumsSearch;
import com.leokomarov.jamstreamer.searches.ArtistsParser;

public class AlbumsByName extends SherlockListActivity implements JSONParser.CallbackInterface, AlbumsByNameAdapter.CallbackInterface,
		AlbumsByNameTrackParser.CallbackInterface {
	private String TAG_RESULTS = "results";
	private static String TAG_ARTIST_NAME = "artist_name";
	protected static String TAG_ALBUM_ID = "id";
	private String TAG_ALBUM_NAME = "name";
	private String TAG_TRACK_ID = "id";
	private String TAG_TRACK_NAME = "name";
	private String TAG_TRACK_DURATION = "duration";
	private ListView AlbumsByNameLV;
	private ArrayAdapter<AlbumsByNameModel>AlbumsByNameListAdapter;
	private List<AlbumsByNameModel> AlbumsByNameModel = new ArrayList<AlbumsByNameModel>();
	protected static ActionMode mActionMode;
	private JSONArray results;
	private ArrayList<HashMap<String, String>> albumList = new ArrayList<HashMap<String, String>>();
	private ArrayList<HashMap<String, String>> trackList = new ArrayList<HashMap<String, String>>();
	private ImageButton button_playlist;
	private int albumsToAddLoop = 0;
	private int onTrackRequestCompletedLoop = 0;
	protected static boolean selectAll;
	protected static boolean selectAllPressed;
	
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
		
		Intent intent = getIntent();
		SharedPreferences hierarchyPreference = getSharedPreferences(getString(R.string.hierarchyPreferences), 0);
		String hierarchy = hierarchyPreference.getString("hierarchy", "none");
		String searchTerm = "";
		String unformattedURL = "";

        switch (hierarchy) {
            case "artists":
                searchTerm = intent.getStringExtra(ArtistsParser.TAG_ARTIST_ID);
                unformattedURL = getResources().getString(R.string.albumsByArtistIDJSONURL);
                break;
            case "albums":
                searchTerm = intent.getStringExtra(AlbumsSearch.TAG_ALBUM_NAME);
                unformattedURL = getResources().getString(R.string.albumsByNameJSONURL);
                break;
            case "tracks":
                searchTerm = intent.getStringExtra(TracksByName.TAG_ALBUM_NAME);
                unformattedURL = getResources().getString(R.string.albumsByNameJSONURL);
                break;
            case "tracksFloatingMenuArtist":
                searchTerm = intent.getStringExtra(TracksByName.TAG_ARTIST_NAME);
                unformattedURL = getResources().getString(R.string.albumsByArtistNameJSONURL);
                break;
            case "albumsFloatingMenuArtist":
                searchTerm = intent.getStringExtra(AlbumsByName.TAG_ARTIST_NAME);
                unformattedURL = getResources().getString(R.string.albumsByArtistNameJSONURL);
                break;
            case "playlistFloatingMenuArtist":
                searchTerm = intent.getStringExtra(PlaylistActivity.TAG_ARTIST_NAME);
                unformattedURL = getResources().getString(R.string.albumsByArtistNameJSONURL);
                break;
        }
		
    	String url = String.format(unformattedURL, searchTerm).replace("&amp;", "&").replace(" ", "+");
		JSONParser jParser = new JSONParser(this);
		jParser.execute(url);
	}
		
	@Override
	public void onRequestCompleted(JSONObject json) {
		try {
			results = json.getJSONArray(TAG_RESULTS);
			SharedPreferences hierarchyPreference = getSharedPreferences(getString(R.string.hierarchyPreferences), 0);
			String hierarchy = hierarchyPreference.getString("hierarchy", "none");
			if (hierarchy.equals("artists") || hierarchy.equals("albumsFloatingMenuArtist") || hierarchy.equals("tracksFloatingMenuArtist") || hierarchy.equals("playlistFloatingMenuArtist")){
				for(int i = 0; i < results.length(); i++) {
					JSONArray albumsArray = results.getJSONObject(i).getJSONArray("albums");
					String artistName = results.getJSONObject(i).getString("name");
					for(int j = 0; j < albumsArray.length(); j++) {
						JSONObject albumInfo = albumsArray.getJSONObject(j);
						
						HashMap<String, String> map = new HashMap<String, String>();
						String albumName = albumInfo.getString(TAG_ALBUM_NAME);
						String albumID = albumInfo.getString(TAG_ALBUM_ID);
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
					
					HashMap<String, String> map = new HashMap<String, String>();
					String artistName = albumInfo.getString(TAG_ARTIST_NAME);
					String albumName = albumInfo.getString(TAG_ALBUM_NAME);
					String albumID = albumInfo.getString(TAG_ALBUM_ID);		
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
					Intent in = new Intent(getApplicationContext(), TracksByName.class);
					in.putExtra(TAG_ALBUM_ID, albumID);
					startActivityForResult(in, 2);
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
			artistsIntent.putExtra(TAG_ARTIST_NAME, artistName);
			startActivityForResult(artistsIntent, 3);
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
	        else if (selectAll){
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
              		
              		if (selectAll && ! AlbumsByNameAdapter.AlbumsByNameCheckboxList.get(indexPosition, false) ){
              			AlbumsByNameAdapter.AlbumsByNameCheckboxList.put(indexPosition, true);
              			AlbumsByNameAdapter.AlbumsByNameCheckboxCount++;
					}
              		else if (! selectAll && AlbumsByNameAdapter.AlbumsByNameCheckboxList.get(indexPosition, false) ){
              			AlbumsByNameAdapter.AlbumsByNameCheckboxList.put(indexPosition, false);
              			AlbumsByNameAdapter.AlbumsByNameCheckboxCount--;
					}
              	}
              	
              	if (AlbumsByNameAdapter.AlbumsByNameCheckboxCount == 0){
              		if (mActionMode != null){
              			mActionMode.finish();
              		}
                }
				else if (AlbumsByNameAdapter.AlbumsByNameCheckboxCount != 0){
					callActionBar();
					mActionMode.setTitle(AlbumsByNameAdapter.AlbumsByNameCheckboxCount + " selected");
                }
              	
               	return true;
            } else if (itemId == R.id.addAlbumToPlaylist) {
            	selectAllPressed = false;
				button_playlist.setClickable(false);
				int AlbumsByNameLVLength = AlbumsByNameLV.getCount();
				SparseBooleanArray checkboxList = AlbumsByNameAdapter.AlbumsByNameCheckboxList;
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
	
	@Override
	public void onTrackRequestCompleted(JSONObject json) {
		try {
			JSONArray results = json.getJSONArray(TAG_RESULTS);
			onTrackRequestCompletedLoop++;
			for(int i = 0; i < results.length(); i++) {
				JSONArray tracksArray = results.getJSONObject(i).getJSONArray("tracks");
				String artistName = results.getJSONObject(i).getString(TAG_ARTIST_NAME);
				String albumName = results.getJSONObject(i).getString(TAG_ALBUM_NAME);
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
    	    
    	    if (onTrackRequestCompletedLoop == albumsToAddLoop){
    	    	ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(AlbumsByName.this,
    	    		getString(R.string.trackPreferences), MODE_PRIVATE);

				ArrayList<HashMap<String, String>> newTrackList = new ArrayList<HashMap<String, String>>();
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
		if (requestCode == 1) {
	    	PlaylistAdapter.PlaylistCheckboxList.clear();
	    	PlaylistAdapter.PlaylistCheckboxCount = 0;
	    }
		if (requestCode == 2) {
	    	TracksByNameAdapter.TracksByNameCheckboxList.clear();
	    	TracksByNameAdapter.TracksByNameCheckboxCount = 0;
	    }
		if (requestCode == 3) {
			AlbumsByNameAdapter.AlbumsByNameCheckboxList.clear();
	    	AlbumsByNameAdapter.AlbumsByNameCheckboxCount = 0;
	    }
	}
	
	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) { 
	        switch (item.getItemId()) {
	        case android.R.id.home: 
	            onBackPressed();
	            return true;
	        }
	    return super.onOptionsItemSelected(item);
	}

}

class AlbumsByNameTrackParser extends AsyncTask<String, Void, JSONObject>  {
	Context context;
	
	public interface CallbackInterface {
        void onTrackRequestCompleted(JSONObject json);
    }
	
	private CallbackInterface mCallback;

    public AlbumsByNameTrackParser(CallbackInterface callback, Context context) {
        mCallback = callback;
        this.context = context;
    }
	
	@Override
    protected JSONObject doInBackground(String... urls) {
		JSONObject jObj = null;
        try {
            InputStream is;
            String json;
            String myURL = urls[0];
            URL url = new URL(myURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();
            json = sb.toString();
            jObj = new JSONObject(json);
        } catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
		}

        return jObj;
    }
			   
    @Override
    protected void onPostExecute(JSONObject jObj) {
    	mCallback.onTrackRequestCompleted(jObj);
     }  	
}