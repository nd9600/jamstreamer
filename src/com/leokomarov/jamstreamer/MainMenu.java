package com.leokomarov.jamstreamer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.leokomarov.jamstreamer.common.TracksByName;
import com.leokomarov.jamstreamer.common.TracksByNameAdapter;
import com.leokomarov.jamstreamer.media_player.AudioPlayerService;
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
    	
    	//if FIRSTRUN_PREFERENCE doesn't contain "firstrun" or "firstrun" == false
        if (! getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE).contains("firstrun") 
				|| ! getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true) == false) {
        	Toast.makeText(getApplicationContext(),"Long-press on an album or track for options", Toast.LENGTH_LONG).show();
        	Toast.makeText(getApplicationContext(),"I'd appreciate it if you rate this app. Thanks!", Toast.LENGTH_LONG).show();
        	SharedPreferences firstrunPreference = getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE);
            Editor firstrunEditor = firstrunPreference.edit();
            firstrunEditor.putBoolean("firstrun", false);
            firstrunEditor.commit();
		}   
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
    
    public void topTracksWeekMainButton(View view){
    	putHierarchy("topTracksPerWeek");
		Intent topTracksWeekIntent = new Intent(this, TracksByName.class);
		startActivityForResult(topTracksWeekIntent, 2);	
	}
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == 2) {
	    	TracksByNameAdapter.TracksByNameCheckboxList.clear();
	    	TracksByNameAdapter.TracksByNameCheckboxCount = 0;
	    }
	}
    
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
        	.setMessage("Are you sure you want to exit?")
        	.setCancelable(false)
        	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int id) {
        			stopService(new Intent(MainMenu.this, AudioPlayerService.class));
        			MainMenu.this.finish();
        		}
        })
        .setNegativeButton("No", null)
        .show();
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