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

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;
import com.leokomarov.jamstreamer.playlist.PlaylistAdapter;
import com.leokomarov.jamstreamer.playlist.PlaylistList;
import com.leokomarov.jamstreamer.searches.AlbumsSearch;
import com.leokomarov.jamstreamer.searches.ArtistsParser;

public class AlbumsByName extends SherlockListActivity implements JSONParser.CallbackInterface, AlbumsByNameAdapter.CallbackInterface,
		AlbumsByNameTrackParser.CallbackInterface {
	private String TAG_RESULTS = "results";
	private String TAG_ARTIST_NAME = "artist_name";
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
		if (hierarchy.equals("artists")){
			searchTerm = intent.getStringExtra(ArtistsParser.TAG_ARTIST_ID);
			unformattedURL = getResources().getString(R.string.albumsByArtistIDJSONURL);
		}
		else if (hierarchy.equals("albums")){
			searchTerm = intent.getStringExtra(AlbumsSearch.TAG_ALBUM_NAME);
			unformattedURL = getResources().getString(R.string.albumsByNameJSONURL);
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
			if (hierarchy.equals("artists")){
				for(int i = 0; i < results.length(); i++) {
					JSONArray albumsArray = results.getJSONObject(i).getJSONArray("albums");
					String artistName = results.getJSONObject(i).getString("name");
					for(int j = 0; j < albumsArray.length(); j++) {
						JSONObject albumInfo = albumsArray.getJSONObject(j);
						
						HashMap<String, String> map = new HashMap<String, String>();
						String albumName = albumInfo.getString(TAG_ALBUM_NAME);
						String albumID = albumInfo.getString(TAG_ALBUM_ID);
						map.put("albumArtist", artistName);
						map.put(TAG_ALBUM_NAME, albumName);
						map.put(TAG_ALBUM_ID, albumID);
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
					map.put(TAG_ALBUM_NAME, albumName);
					map.put(TAG_ALBUM_ID, albumID);
					albumList.add(map);
				}
			}
		} catch (NullPointerException e) {
		} catch (JSONException e) {
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
					String albumID = albumList.get(position - 1).get(TAG_ALBUM_ID);
					Intent in = new Intent(getApplicationContext(), TracksByName.class);
					in.putExtra(TAG_ALBUM_ID, albumID);
					startActivityForResult(in, 2);
				}
			});
	    	
	    	AlbumsByNameLV.setOnItemLongClickListener(new OnItemLongClickListener() {
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
					
					CheckBox checkbox = (CheckBox) view.findViewById(R.id.albums_by_name_checkBox);
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
	        inflater.inflate(R.menu.albums_contextual_menu, menu);
	        return true;
	        }
	    
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        	int itemId = item.getItemId();
			if (itemId == R.id.addAlbumToPlaylist) {
				button_playlist.setClickable(false);
				int AlbumsByNameLVLength = AlbumsByNameLV.getCount();
				SparseBooleanArray checkboxList = AlbumsByNameAdapter.AlbumsByNameCheckboxList;
				albumsToAddLoop = 0;
				onTrackRequestCompletedLoop = 0;
				for (int i = 0; i < AlbumsByNameLVLength; i++){
					if (checkboxList.get(i, false)) {
						albumsToAddLoop++;
						String albumID = albumList.get(i).get(TAG_ALBUM_ID);
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
    	    		getString(R.string.trackPreferencesFile), MODE_PRIVATE);;
    	    		
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
		} catch (NullPointerException e) {		
		} catch (JSONException e) {		
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		AlbumsByNameAdapter.AlbumsByNameCheckboxList.clear();
		AlbumsByNameAdapter.AlbumsByNameCheckboxCount = 0;
		if (requestCode == 1) {
	    	PlaylistAdapter.PlaylistCheckboxList.clear();
	    	PlaylistAdapter.PlaylistCheckboxCount = 0;
	    }
		if (requestCode == 2) {
	    	TracksByNameAdapter.TracksByNameCheckboxList.clear();
	    	TracksByNameAdapter.TracksByNameCheckboxCount = 0;
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
        public void onTrackRequestCompleted(JSONObject json);
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
        	InputStream is = null;
        	String json = "";
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
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();
            jObj = new JSONObject(json);
        } catch (SocketTimeoutException e) {
        	Toast.makeText(this.context, "Jamendo has timed out, please retry", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
		} catch (UnsupportedEncodingException e) {
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		} catch (Exception e) {
		}       
        return jObj;
    }
			   
    @Override
    protected void onPostExecute(JSONObject jObj) {
    	mCallback.onTrackRequestCompleted(jObj);
     }  	
}