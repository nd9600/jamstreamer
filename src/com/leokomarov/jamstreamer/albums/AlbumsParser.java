package com.leokomarov.jamstreamer.albums;

import com.leokomarov.jamstreamer.JSONParser;
import com.leokomarov.jamstreamer.R;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**Retrieves JSON file, parses and populates a clickable ListView, which opens another 
 *  activity - SingleMenuItemActivity - with the name shown to the user
 * @author LeoKomarov
 */
public class AlbumsParser extends ListActivity implements JSONParser.MyCallbackInterface  {
	//private String url = "http://api.jamendo.com/v3.0/albums/?client_id=b6747d04&format=jsonpretty&artist_name=professor+kliq";
	private String DEBUG = "ArtistsParser";
	
	// JSON Node names
	private static final String TAG_RESULTS = "results";
	private static final String TAG_ARTISTID = "id";
	private static final String TAG_NAME = "name";
	
	JSONArray results = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
        String artistName = intent.getStringExtra(AlbumsSearch.INTENT_TAG); 
        String unformattedURL = getResources().getString(R.string.artistsByNameJSONURL);
    	String url = String.format(unformattedURL, artistName);
    	url = url.replace("&amp;", "&").replace(" ", "+");
    	//url = url.replace(" ", "+");
    	Log.v(DEBUG, "Url passed is " + url);
    		
		//Log.v(DEBUG, "Created");
		setContentView(R.layout.main_menu);
		//Log.v(DEBUG, "Content view set to main.xml");
			
		// Creating JSON Parser instance
		JSONParser jParser = new JSONParser(this);
		Log.v(DEBUG, "New JSONParser created");

		// getting JSON string from URL
		jParser.execute(url);
	}
		
	@Override
	public void onRequestCompleted(JSONObject json) {
		//Hashmap for ListView
	    ArrayList<HashMap<String, String>> resultList = new ArrayList<HashMap<String, String>>();
		try {
			//Getting array of Contacts
			results = json.getJSONArray(TAG_RESULTS);
				
			//looping through all Contacts
			for(int i = 0; i < results.length(); i++) {
				JSONObject r = results.getJSONObject(i);
					
				// Storing each json item in variable
				String id = r.getString(TAG_ARTISTID);
				String name = r.getString(TAG_NAME);
				
				// creating new HashMap
				HashMap<String, String> map = new HashMap<String, String>();
					
				// adding each child node to HashMap key => value
				map.put(TAG_ARTISTID, id);
				map.put(TAG_NAME, name);

				// adding HashList to ArrayList
				resultList.add(map);
				//Contains all fields, like "id" and "name"
				Log.v(DEBUG,"Result list is " + resultList);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
			
		Log.v(DEBUG, "Got JSON array, adding into ListView");
			
		/**
		 * Updating parsed JSON data into ListView
		 * */
		ListAdapter adapter = new SimpleAdapter(this, resultList, R.layout.artists_2artist_names_list,new String[] {TAG_NAME}, new int[] {R.id.artists_names});
		ListAdapter IDadapter = new SimpleAdapter(this, resultList, R.layout.artists_2artist_names_list,new String[] {TAG_ARTISTID}, new int[] {R.id.artists_ids});
		setListAdapter(adapter);
		setListAdapter(IDadapter);

		// selecting single ListView item
		ListView lv = getListView();
		
		// Launching new screen on Selecting Single ListItem
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// getting values from selected ListItem
				String name = ((TextView) view.findViewById(R.id.artists_names)).getText().toString();
				String artistID = ((TextView) view.findViewById(R.id.artists_ids)).getText().toString();
								
				Log.v(DEBUG,"Artist name is " + name);	
				Log.v(DEBUG,"Artist ID is " + artistID);
				
				// Starting new intent
				//Pass to AlbumsByArtistName
				Intent in = new Intent(getApplicationContext(), AlbumsSingleItem.class);
				in.putExtra(TAG_NAME, name);
				in.putExtra(TAG_ARTISTID, artistID);
				startActivity(in);
			}
		});
	    }

}