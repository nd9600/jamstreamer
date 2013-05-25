package com.leokomarov.jamstreamer.artists;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.leokomarov.jamstreamer.JSONParser;
import com.leokomarov.jamstreamer.R;

/**Retrieves JSON file, parses and populates a clickable ListView, which opens another 
 *  activity - TracksByAlbum - with the tracks shown to the user
 * @author LeoKomarov
 */
public class AlbumsByArtist extends ListActivity implements JSONParser.MyCallbackInterface  {
	private static final String TAG_RESULTS = "results";
	public static final String TAG_ALBUM_ID = "id";
	private static final String TAG_ALBUM_NAME = "name";
	//private static final String TAG_ALBUMIMAGE = "image";
	JSONArray results = null;
	JSONArray albumsArray = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
        String artistID = intent.getStringExtra(ArtistsParser.TAG_ARTIST_ID); 
        String unformattedURL = getResources().getString(R.string.albumsByArtistIDJSONURL);
    	String url = String.format(unformattedURL, artistID);
    	url = url.replace("&amp;", "&");
    	
    	setContentView(R.layout.artists_3albums_by_original_empty_list);
		JSONParser jParser = new JSONParser(this);
		jParser.execute(url);
	}
		
	@Override
	public void onRequestCompleted(JSONObject json) {
		//Hashmap for ListView
	    ArrayList<HashMap<String, String>> albumList = new ArrayList<HashMap<String, String>>();
		try {
			//Getting array of Contacts
			results = json.getJSONArray(TAG_RESULTS);
			//Looping through all results
			for(int i = 0; i < results.length(); i++) {
				JSONArray albumsArray = results.getJSONObject(i).getJSONArray("albums");
				for(int j = 0; j < albumsArray.length(); j++) {
					JSONObject a = albumsArray.getJSONObject(j);
					
					// Storing each json item in variable
					String id = a.getString(TAG_ALBUM_ID);
					String name = a.getString(TAG_ALBUM_NAME);
				
					// creating new HashMap
					HashMap<String, String> map = new HashMap<String, String>();
					
					// adding each child node to HashMap key => value
					map.put(TAG_ALBUM_ID, id);
					map.put(TAG_ALBUM_NAME, name);

					// adding HashList to ArrayList
					albumList.add(map);
					//Contains all fields, like "id" and "name"
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
				
		//Selecting single ListView item
		ListView lv = getListView();
		LayoutInflater inflater = getLayoutInflater();
		//Adding section header
		ViewGroup header = (ViewGroup)inflater.inflate(R.layout.artists_4header, lv, false);
		lv.addHeaderView(header, null, false);
		ListAdapter adapter = new SimpleAdapter(this, albumList, 
				R.layout.artists_4albums_by_list_item,new String[] {TAG_ALBUM_NAME, TAG_ALBUM_ID}, 
				new int[] {R.id.artist_album_name, R.id.artist_album_id});
		setListAdapter(adapter);

		// Launching new screen on Selecting Single ListItem
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// getting values from selected ListItem
				String albumName = ((TextView) view.findViewById(R.id.artist_album_name)).getText().toString();
				String albumID = ((TextView) view.findViewById(R.id.artist_album_id)).getText().toString();	
				// Starting new intent
				Intent in = new Intent(getApplicationContext(), TracksByAlbum.class);
				in.putExtra(TAG_ALBUM_NAME, albumName);
				in.putExtra(TAG_ALBUM_ID, albumID);
				startActivity(in);
			}
		});
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