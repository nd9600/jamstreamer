package com.leokomarov.jamstreamer;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.leokomarov.jamstreamer.artists.ArtistsSearch;

public class MainMenu extends Activity {
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);       
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
	}
	
	public void artistsMainButton(View view){
		Intent artistIntent = new Intent(this, ArtistsSearch.class);
		startActivity(artistIntent);
	}
	
    public void albumsMainButton(View view){
		//Intent albumIntent = new Intent(this, AlbumsSearch.class);
		//startActivity(albumIntent);	
	}
    
    public void tracksMainButton(View view){
		//Intent trackIntent = new Intent(this, TracksSearch.class);
		//startActivity(trackIntent);	
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