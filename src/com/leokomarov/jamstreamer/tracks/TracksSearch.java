package com.leokomarov.jamstreamer.tracks;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.leokomarov.jamstreamer.R;

public class TracksSearch extends Activity {
	public final static String INTENT_TAG = "com.leokomarov.jamstreamer.artists.NAME";
	private final String DEBUG = "ArtistsSearch";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artists_0main);
               
        //Needs fixed, complains about API level if <11 in manifest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
	}
	
	public void sendArtistName(View view){
		Intent intent = new Intent(this, TracksParser.class);
        EditText editText = (EditText) findViewById(R.id.artistsByNameField);
        String artistName = editText.getText().toString();
        Log.v(DEBUG, "Artists name is " + artistName);
        intent.putExtra(INTENT_TAG, artistName);
        startActivity(intent);
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