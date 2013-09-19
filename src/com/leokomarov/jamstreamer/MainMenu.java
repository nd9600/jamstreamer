package com.leokomarov.jamstreamer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockActivity;
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;
import com.leokomarov.jamstreamer.searches.AlbumsSearch;
import com.leokomarov.jamstreamer.searches.ArtistsSearch;
import com.leokomarov.jamstreamer.searches.TracksSearch;

public class MainMenu extends SherlockActivity {
	private ImageButton button_playlist;
	
	private void putHierarchy(String hierarchy){
		SharedPreferences hierarchyPreference = getSharedPreferences(getString(R.string.hierarchyPreferences), 0);
    	SharedPreferences.Editor hierarchyEditor = hierarchyPreference.edit();
    	hierarchyEditor.putString("hierarchy", hierarchy);
		hierarchyEditor.commit();
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);       
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        
        button_playlist = (ImageButton) findViewById(R.id.mainMenu_btnPlaylist);    	
    	button_playlist.setOnClickListener(new View.OnClickListener() {
    		@Override
            public void onClick(View v) {
                Intent button_playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
                startActivityForResult(button_playlistIntent, 1);
            }
    	});
	}	
	
	public void artistsMainButton(View view){
		putHierarchy("artists");
		Intent artistIntent = new Intent(this, ArtistsSearch.class);
		startActivity(artistIntent);
	}
	
    public void albumsMainButton(View view){
    	putHierarchy("albums");
		Intent albumIntent = new Intent(this, AlbumsSearch.class);
		startActivity(albumIntent);	
	}
    
    public void tracksMainButton(View view){
    	putHierarchy("tracks");
		Intent trackIntent = new Intent(this, TracksSearch.class);
		startActivity(trackIntent);	
	}
    
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) { 
	        int itemId = item.getItemId();
			if (itemId == android.R.id.home) {
				onBackPressed();
				return true;
			}
	    return super.onOptionsItemSelected(item);
	}
	
}