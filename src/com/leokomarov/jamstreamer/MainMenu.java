package com.leokomarov.jamstreamer;

import com.leokomarov.jamstreamer.artists.ArtistsSearch;
import com.leokomarov.jamstreamer.albums.AlbumsSearch;
import com.leokomarov.jamstreamer.tracks.TracksSearch;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

public class MainMenu extends Activity {
	private String DEBUG = "MainMenu";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);       
        
        //Needs fixed, complains about API level if <11 in manifest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
        	ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
	}
	
	public void artistsMainButton(View view){
		Log.v(DEBUG,"Clicked artists");
		Intent artistIntent = new Intent(this, ArtistsSearch.class);
		startActivity(artistIntent);
	}
	
    public void albumsMainButton(View view){
    	Log.v(DEBUG,"Clicked albums");
		Intent albumIntent = new Intent(this, AlbumsSearch.class);
		startActivity(albumIntent);	
	}
    
    public void tracksMainButton(View view){
    	Log.v(DEBUG,"Clicked tracks");
		Intent trackIntent = new Intent(this, TracksSearch.class);
		startActivity(trackIntent);	
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