package com.leokomarov.jamstreamer.artists;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Build;
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
import android.widget.Toast;

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
		
	@Override
	public void onRequestCompleted(JSONObject json) {
	    ArrayList<HashMap<String, String>> albumList = new ArrayList<HashMap<String, String>>();
		try {
			results = json.getJSONArray(TAG_RESULTS);
			for(int i = 0; i < results.length(); i++) {
				JSONArray albumsArray = results.getJSONObject(i).getJSONArray("albums");
				for(int j = 0; j < albumsArray.length(); j++) {
					JSONObject a = albumsArray.getJSONObject(j);
					
					String id = a.getString(TAG_ALBUM_ID);
					String name = a.getString(TAG_ALBUM_NAME);
				
					HashMap<String, String> map = new HashMap<String, String>();
					
					map.put(TAG_ALBUM_ID, id);
					map.put(TAG_ALBUM_NAME, name);

					albumList.add(map);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if (albumList.isEmpty()){
			Toast.makeText(getApplicationContext(), "Please retry, there has been an error downloading the album list", Toast.LENGTH_SHORT).show();
		}
		else {	
			ListView lv = getListView();
			LayoutInflater inflater = getLayoutInflater();
			ViewGroup header = (ViewGroup)inflater.inflate(R.layout.artists_4header, lv, false);
			lv.addHeaderView(header, null, false);
			ListAdapter adapter = new SimpleAdapter(this, albumList, 
				R.layout.artists_4albums_by_list_item,new String[] {TAG_ALBUM_NAME, TAG_ALBUM_ID}, 
				new int[] {R.id.artist_album_name, R.id.artist_album_id});
			setListAdapter(adapter);

			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String albumName = ((TextView) view.findViewById(R.id.artist_album_name)).getText().toString();
					String albumID = ((TextView) view.findViewById(R.id.artist_album_id)).getText().toString();	

					Intent in = new Intent(getApplicationContext(), TracksByAlbum.class);
					in.putExtra(TAG_ALBUM_NAME, albumName);
					in.putExtra(TAG_ALBUM_ID, albumID);
					startActivity(in);
				}
			});
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