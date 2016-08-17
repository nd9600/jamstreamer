package com.leokomarov.jamstreamer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.leokomarov.jamstreamer.common.TracksByName;
import com.leokomarov.jamstreamer.common.TracksByNameAdapter;
import com.leokomarov.jamstreamer.media_player.AudioPlayerService;
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;
import com.leokomarov.jamstreamer.playlist.PlaylistAdapter;
import com.leokomarov.jamstreamer.searches.AlbumsSearch;
import com.leokomarov.jamstreamer.searches.ArtistsSearch;
import com.leokomarov.jamstreamer.searches.TracksSearch;

public class MainMenu extends SherlockActivity {
	private ImageButton button_playlist;
	private TextView textView_artists;
	private TextView textView_albums;
	private TextView textView_tracks;
	private TextView textView_topTracksThisWeek;

    //Stores whether you are going into the artists, albums, tracks, or top tracks this week activity
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
        textView_artists = (TextView) findViewById(R.id.mainMenu_artists);
        textView_albums = (TextView) findViewById(R.id.mainMenu_albums);
        textView_tracks = (TextView) findViewById(R.id.mainMenu_tracks);
        textView_topTracksThisWeek = (TextView) findViewById(R.id.mainMenu_topTracksThisWeek);

        button_playlist.setOnClickListener(new View.OnClickListener() {
    		@Override
            public void onClick(View v) {
                Intent playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
                startActivityForResult(playlistIntent, 1);
            }
    	});

        //If the artists &c button is clicked
        //put the appropriate string in the hierarchy, and start the activity
    	textView_artists.setOnClickListener(new View.OnClickListener() {
    		@Override
            public void onClick(View v) {
    			putHierarchy("artists");
    			Intent artistIntent = new Intent(getApplicationContext(), ArtistsSearch.class);
    			startActivity(artistIntent);
    		}
    	});

    	textView_albums.setOnClickListener(new View.OnClickListener() {
    		@Override
            public void onClick(View v) {
    			putHierarchy("albums");
    			Intent albumIntent = new Intent(getApplicationContext(), AlbumsSearch.class);
    			startActivity(albumIntent);	
    		}
    	});
    	
    	textView_tracks.setOnClickListener(new View.OnClickListener() {
    		@Override
            public void onClick(View v) {
    			putHierarchy("tracks");
    			Intent trackIntent = new Intent(getApplicationContext(), TracksSearch.class);
    			startActivity(trackIntent);	
    		}
    	});
    	
    	textView_topTracksThisWeek.setOnClickListener(new View.OnClickListener() {
    		@Override
            public void onClick(View v) {
    			putHierarchy("topTracksPerWeek");
    			Intent topTracksWeekIntent = new Intent(getApplicationContext(), TracksByName.class);
    			startActivityForResult(topTracksWeekIntent, 2);	
    		}
    	});

    	//if the app hasn't been ran before, show the toasts
		// ie FIRSTRUN_PREFERENCE doesn't contain "firstrun" or "firstrun" == true
        if (! getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE).contains("firstrun") 
				|| getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true)) {
        	Toast.makeText(getApplicationContext(),"Long-press on an album or track for options", Toast.LENGTH_LONG).show();
        	Toast.makeText(getApplicationContext(),"I'd appreciate it if you rate this app. Thanks!", Toast.LENGTH_LONG).show();
        	SharedPreferences firstrunPreference = getSharedPreferences("FIRSTRUN_PREFERENCE", MODE_PRIVATE);
            Editor firstrunEditor = firstrunPreference.edit();
            firstrunEditor.putBoolean("firstrun", false);
            firstrunEditor.commit();
		}   
	}

    //Resets the checkboxes once you've left the playlist or top tracks activities
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                PlaylistAdapter.PlaylistCheckboxList.clear();
                PlaylistAdapter.PlaylistCheckboxCount = 0;
                break;
            case 2:
                TracksByNameAdapter.TracksByNameCheckboxList.clear();
                TracksByNameAdapter.TracksByNameCheckboxCount = 0;
                break;
        }
	}

    //Brings up the exit dialog when you press the back button
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

    //When you press the app home button, bring up the exit dialog
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) { 
	        int itemId = item.getItemId();
			if (itemId == android.R.id.home) {
				onBackPressed();
				return true;
			}
	    return super.onOptionsItemSelected(item);
	}
	
}