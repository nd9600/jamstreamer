package com.leokomarov.jamstreamer.searches;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.discography.albums.AlbumsByName;
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;
import com.leokomarov.jamstreamer.common.ActionBarListActivity;
import com.leokomarov.jamstreamer.utils.JSONParser;
import com.leokomarov.jamstreamer.utils.generalUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ArtistsParser extends ActionBarListActivity implements JSONParser.CallbackInterface  {
	
	private static final String TAG_RESULTS = "results";
	public static final String TAG_ARTIST_ID = "id";
	private static final String TAG_ARTIST_NAME = "name";
	JSONArray results = null;
	private ImageButton button_playlist;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getSupportActionBar();//.setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
        String artistName = intent.getStringExtra(ArtistsSearch.TAG_ARTIST_NAME); 
        String unformattedURL = getResources().getString(R.string.artistsByNameJSONURL);
    	String url = String.format(unformattedURL, artistName).replace("&amp;", "&").replace(" ", "+");
    		
		setContentView(R.layout.original_empty_list);
		JSONParser jParser = new JSONParser(this);
		jParser.execute(url);
	}
		
	@Override
	public void onRequestCompleted(JSONObject json) {
	    ArrayList<HashMap<String, String>> artistList = new ArrayList<>();
		try {
			results = json.getJSONArray(TAG_RESULTS);
				
			for(int i = 0; i < results.length(); i++) {
				JSONObject r = results.getJSONObject(i);
					
				String id = r.getString(TAG_ARTIST_ID);
				String name = r.getString(TAG_ARTIST_NAME);
				
				HashMap<String, String> map = new HashMap<>();
					
				map.put(TAG_ARTIST_ID, id);
				map.put(TAG_ARTIST_NAME, name);

				artistList.add(map);
			}
		} catch (Exception e) {
            System.out.println("Exception: " +  e);
		}
		
		if (json == null || json.isNull("results")) {
	        Toast.makeText(getApplicationContext(), "Please retry, there has been an error downloading the artist list", Toast.LENGTH_SHORT).show();
		}
		else if (json.has("results") && artistList.isEmpty()){
			Toast.makeText(getApplicationContext(), "There are no artists matching this search", Toast.LENGTH_SHORT).show();
        }
		else {		
			ListView lv = getListView();
			LayoutInflater inflater = getLayoutInflater();
		
			ViewGroup header = (ViewGroup)inflater.inflate(R.layout.artists_list_header, lv, false);
			lv.addHeaderView(header, null, false);
			ListAdapter adapter = new SimpleAdapter(this, artistList, R.layout.artists_list, 
				new String[] {TAG_ARTIST_NAME, TAG_ARTIST_ID}, new int[] {R.id.artists_list_artists_names, R.id.artists_list_artists_ids});
			setListAdapter(adapter);
		
			button_playlist = (ImageButton) findViewById(R.id.artists1_btnPlaylist);    	
	    	button_playlist.setOnClickListener(new View.OnClickListener() {
				@Override
	            public void onClick(View v) {
	                Intent button_playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
	                startActivityForResult(button_playlistIntent, 1);
	            }
			});
			
			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    generalUtils.putHierarchy(ArtistsParser.this, "artists");
                    String artistID = ((TextView) view.findViewById(R.id.artists_list_artists_ids)).getText().toString();

					Intent in = new Intent(ArtistsParser.this, AlbumsByName.class);
					in.putExtra(TAG_ARTIST_ID, artistID);
					startActivityForResult(in, 2);
				}
			});
			
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Todo: Possibly remove this
		//generalUtils.clearCheckboxes(null);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	        int itemId = item.getItemId();
			if (itemId == android.R.id.home) {
				onBackPressed();
				return true;
			}
	    return super.onOptionsItemSelected(item);
	}

}