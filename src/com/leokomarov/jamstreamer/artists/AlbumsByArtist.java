package com.leokomarov.jamstreamer.artists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;
import com.leokomarov.jamstreamer.playlist.PlaylistAdapter;
import com.leokomarov.jamstreamer.playlist.PlaylistList;

/**Retrieves JSON file, parses and populates a clickable ListView, which opens another 
 *  activity - TracksByAlbum - with the tracks shown to the user
 * @author LeoKomarov
 */
public class AlbumsByArtist extends ListActivity implements JSONParser.CallbackInterface, AlbumsByArtistAdapter.CallbackInterface,
		AlbumsByArtistTrackParser.CallbackInterface  {
	private static final String TAG_RESULTS = "results";
	protected static final String TAG_ALBUM_ID = "id";
	private static final String TAG_ALBUM_NAME = "name";
	private static final String TAG_TRACK_ID = "id";
	private static final String TAG_TRACK_NAME = "name";
	private static final String TAG_TRACK_DURATION = "duration";
	private ArrayList<HashMap<String, String>> albumList = new ArrayList<HashMap<String, String>>();
	private ArrayList<HashMap<String, String>> trackList = new ArrayList<HashMap<String, String>>();
	private ImageButton button_playlist;
	private int albumsToAddLoop = 0;
	private int onTrackRequestCompletedLoop = 0;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
		
		Intent intent = getIntent();
        String artistID = intent.getStringExtra(ArtistsParser.TAG_ARTIST_ID); 
        String unformattedURL = getResources().getString(R.string.albumsByArtistIDJSONURL);
    	String url = String.format(unformattedURL, artistID);
    	url = url.replace("&amp;", "&");
    	
    	setContentView(R.layout.artists_3albums_by_original_empty_list);
		JSONParser jParser = new JSONParser(this);
		jParser.execute(url);
	}
		
	@SuppressLint({"NewApi"})
	@Override
	public void onRequestCompleted(JSONObject json) {
		try {
			JSONArray results = json.getJSONArray(TAG_RESULTS);
			for(int i = 0; i < results.length(); i++) {
				JSONArray albumsArray = results.getJSONObject(i).getJSONArray("albums");
				for(int j = 0; j < albumsArray.length(); j++) {
					JSONObject a = albumsArray.getJSONObject(j);
					
					String name = a.getString(TAG_ALBUM_NAME);
					String id = a.getString(TAG_ALBUM_ID);					
				
					HashMap<String, String> map = new HashMap<String, String>();
					
					map.put(TAG_ALBUM_NAME, name);
					map.put(TAG_ALBUM_ID, id);


					albumList.add(map);
				}
			}
		} catch (JSONException e) {
			Log.e("AlbumsByArtist onRequestCompleted", "JSONException: " + e.getMessage(), e);
		}
		
		if (albumList.isEmpty()){
			Toast.makeText(getApplicationContext(), "Please retry, there has been an error downloading the album list", Toast.LENGTH_SHORT).show();
		}
		else {	
			final ListView AlbumsByArtistLV = getListView();
			LayoutInflater inflater = getLayoutInflater();
			ViewGroup header = (ViewGroup)inflater.inflate(R.layout.artists_4header, AlbumsByArtistLV, false);
			AlbumsByArtistLV.addHeaderView(header, null, false);
			AlbumsByArtistLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			
			List<AlbumsByArtistModel> AlbumsByArtistModel = new ArrayList<AlbumsByArtistModel>();
	    	for (HashMap<String, String> map : albumList) {
	    		AlbumsByArtistModel.add(new AlbumsByArtistModel(map));
	    	}
	    	
	    	ArrayAdapter<AlbumsByArtistModel> AlbumsByArtistListAdapter = new AlbumsByArtistAdapter(this, this, AlbumsByArtistModel );	
	    	setListAdapter(AlbumsByArtistListAdapter);
			
			button_playlist = (ImageButton) findViewById(R.id.artists4_btnPlaylist);    	
	    	button_playlist.setOnClickListener(new View.OnClickListener() {
				@Override
	            public void onClick(View v) {
	                Intent button_playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
	                startActivityForResult(button_playlistIntent, 1);
	            }
			});

	    	AlbumsByArtistLV.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String albumID = albumList.get(position - 1).get(TAG_ALBUM_ID);
					Intent in = new Intent(getApplicationContext(), TracksByAlbum.class);
					in.putExtra(TAG_ALBUM_ID, albumID);
					startActivityForResult(in, 2);
				}
			});
	    	
	    	AlbumsByArtistLV.setMultiChoiceModeListener(new MultiChoiceModeListener() {
	            @Override
	            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
	            	int numberOfCheckedItems = AlbumsByArtistAdapter.AlbumsByArtistCheckboxCount;
	                if (numberOfCheckedItems == 1){
	                	mode.setTitle(numberOfCheckedItems + " album selected");
	                }
	                else if(numberOfCheckedItems >= 2){
	                	mode.setTitle(numberOfCheckedItems + " albums selected");
	                }
	            }
	            
	            @Override
	            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	                return false;
	            }

	            @Override
	            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	                MenuInflater inflater = mode.getMenuInflater();
	                inflater.inflate(R.menu.albums_by_artist_contextual_menu, menu);               
	                return true;
	            }
	            
	            @Override
	            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	            	switch (item.getItemId()) {
	                	case R.id.addAlbumToPlaylist:
	                		
	                		button_playlist.setClickable(false);
	                		int AlbumsByArtistLVLength = AlbumsByArtistLV.getCount();
	                		SparseBooleanArray checkboxList = AlbumsByArtistAdapter.AlbumsByArtistCheckboxList;
	                		albumsToAddLoop = 0;
	                		onTrackRequestCompletedLoop = 0;

	                		for (int i = 0; i < AlbumsByArtistLVLength; i++){
	                			if (checkboxList.get(i, false)) {
	                				albumsToAddLoop++;
	                				String albumID = albumList.get(i).get(TAG_ALBUM_ID);
	                				String unformattedURL = getResources().getString(R.string.tracksByAlbumIDJSONURL);
	                		    	String url = String.format(unformattedURL, albumID);
	                		    	url = url.replace("&amp;", "&");
	                		    	
	                		    	AlbumsByArtistTrackParser trackParser = new AlbumsByArtistTrackParser(AlbumsByArtist.this);
	                				trackParser.execute(url);     		    		                				
	                   			 }
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
	
	@Override
	public void onTrackRequestCompleted(JSONObject json) {
		try {
			JSONArray results = json.getJSONArray(TAG_RESULTS);
			//ArrayList<HashMap<String, String>> trackList = new ArrayList<HashMap<String, String>>();
			onTrackRequestCompletedLoop++;
			Log.v("","onTrackRequestCompletedLoop 2 is " + onTrackRequestCompletedLoop);
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
    	    
    	    if (onTrackRequestCompletedLoop == albumsToAddLoop){
    	    	ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(AlbumsByArtist.this,
    	        	    getString(R.string.trackPreferencesFile), MODE_PRIVATE);;
    	        ArrayList<HashMap<String, String>> newTrackList = trackPreferences.getObject("tracks", PlaylistList.class).trackList;
    	        
    	    	newTrackList.addAll(trackList);
    	    	Log.v("onTrackRequestCompleted","newTrackList is " + newTrackList);
    	    	
    	    	PlaylistList trackPreferencesObject = new PlaylistList();
        		trackPreferencesObject.setTrackList(newTrackList);
        	    trackPreferences.putObject("tracks", trackPreferencesObject);
        	    trackPreferences.commit();
    	    	button_playlist.setClickable(true);
    	    	if (albumsToAddLoop == 1){
            		Toast.makeText(getApplicationContext(),"1 album added to the playlist", Toast.LENGTH_LONG).show();
                } else if(albumsToAddLoop >= 2){
                	Toast.makeText(getApplicationContext(),albumsToAddLoop + " albums added to the playlist", Toast.LENGTH_LONG).show();
                }
    	    }
		} catch (JSONException e) {
			Log.e("AlbumsByArtist", "JSONException: " + e.getMessage(), e);
		}
	}
	
	@Override
	public void setListItemChecked(int position, boolean checked) {
		ListView AlbumsByArtistLV = getListView();
		AlbumsByArtistLV.setItemChecked(position, checked);		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		AlbumsByArtistAdapter.AlbumsByArtistCheckboxList.clear();
		AlbumsByArtistAdapter.AlbumsByArtistCheckboxCount = 0;
		if (requestCode == 1) {
	    	PlaylistAdapter.PlaylistCheckboxList.clear();
	    	PlaylistAdapter.PlaylistCheckboxCount = 0;
	    }
		if (requestCode == 2) {
	    	TracksByAlbumAdapter.TracksByAlbumCheckboxList.clear();
	    	TracksByAlbumAdapter.TracksByAlbumCheckboxCount = 0;
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

class AlbumsByArtistTrackParser extends AsyncTask<String, Void, JSONObject>  {

    static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";
	
	public JSONObject getJSONFromUrl(String myurl) {
		try {
	        URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000 /* milliseconds */);
	        conn.setConnectTimeout(15000 /* milliseconds */);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        conn.connect();
	        is = conn.getInputStream();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();    
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
			Log.e("AlbumsByArtistTrackParser", "JSONException: " + e.getMessage(), e);
		}
	
		return jObj;
	}

	@Override
    protected JSONObject doInBackground(String... urls) {
    	String url = urls[0];            
        return getJSONFromUrl(url);
    }
	
	public interface CallbackInterface {
        public void onTrackRequestCompleted(JSONObject json);
    }
	
	private CallbackInterface mCallback;

    public AlbumsByArtistTrackParser(CallbackInterface callback) {
        mCallback = callback;
    }
			   
    @Override
    protected void onPostExecute(JSONObject jObj) {
    	mCallback.onTrackRequestCompleted(jObj);
     }
    	
}