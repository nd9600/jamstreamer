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
 *  activity - SingleMenuItemActivity - with the name shown to the user
 * @author LeoKomarov
 */
public class ArtistsParser extends ListActivity implements JSONParser.MyCallbackInterface  {
	
	// JSON Node names
	private static final String TAG_RESULTS = "results";
	public static final String TAG_ARTIST_ID = "id";
	private static final String TAG_ARTIST_NAME = "name";
	
	JSONArray results = null;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
		
		Intent intent = getIntent();
        String artistName = intent.getStringExtra(ArtistsSearch.INTENT_TAG); 
        String unformattedURL = getResources().getString(R.string.artistsByNameJSONURL);
    	String url = String.format(unformattedURL, artistName);
    	url = url.replace("&amp;", "&").replace(" ", "+");
    		
		setContentView(R.layout.artists_1artist_names_original_empty_list);
		JSONParser jParser = new JSONParser(this);
		jParser.execute(url);
	}
		
	@Override
	public void onRequestCompleted(JSONObject json) {
	    ArrayList<HashMap<String, String>> artistList = new ArrayList<HashMap<String, String>>();
		try {
			results = json.getJSONArray(TAG_RESULTS);
				
			for(int i = 0; i < results.length(); i++) {
				JSONObject r = results.getJSONObject(i);
					
				String id = r.getString(TAG_ARTIST_ID);
				String name = r.getString(TAG_ARTIST_NAME);
				
				HashMap<String, String> map = new HashMap<String, String>();
					
				map.put(TAG_ARTIST_ID, id);
				map.put(TAG_ARTIST_NAME, name);

				artistList.add(map);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if ( artistList.isEmpty() ) {
        	Toast.makeText(getApplicationContext(), "Please retry, there has been an error downloading the artist list", Toast.LENGTH_SHORT).show();
        }
		else {		
			ListView lv = getListView();
			LayoutInflater inflater = getLayoutInflater();
		
			ViewGroup header = (ViewGroup)inflater.inflate(R.layout.artists_2header, lv, false);
			lv.addHeaderView(header, null, false);
			ListAdapter adapter = new SimpleAdapter(this, artistList, R.layout.artists_2artist_names_list, 
				new String[] {TAG_ARTIST_NAME, TAG_ARTIST_ID}, new int[] {R.id.artists_names, R.id.artists_ids});
			setListAdapter(adapter);
		
			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String artistName = ((TextView) view.findViewById(R.id.artists_names)).getText().toString();
					String artistID = ((TextView) view.findViewById(R.id.artists_ids)).getText().toString();

					Intent in = new Intent(ArtistsParser.this, AlbumsByArtist.class);
					in.putExtra(TAG_ARTIST_NAME, artistName);
					in.putExtra(TAG_ARTIST_ID, artistID);
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