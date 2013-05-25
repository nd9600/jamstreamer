package com.leokomarov.jamstreamer.media_player;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.artists.TracksByAlbum;

//Shouldn't extend Activity
public class AudioPlayer extends Activity {
	private String DEBUG = "AudioPlayer";
	//private static final String TAG_TRACK_ID = "id";
	//private static final String TAG_TRACK_NAME = "name";
	//private static final String TAG_TRACK_DURATION = "duration";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Needs fixed, complains about API level if <11 in manifest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
		setContentView(R.layout.audio_player);
		Log.v(DEBUG, "Created");
		Intent intent = getIntent();
    	String trackID = intent.getStringExtra(TracksByAlbum.TAG_TRACK_ID); 
    	String unformattedURL = getResources().getString(R.string.trackByIDURL);
		String url = String.format(unformattedURL, trackID);
		url = url.replace("&amp;", "&");
		Log.v(DEBUG, "URL passed is " + url);
	
		MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		Log.v(DEBUG, "Created mediaPlayer");
		try {
		mediaPlayer.setDataSource(url);
		mediaPlayer.prepare(); // might take long! (for buffering, etc)
		Log.v(DEBUG, "Preparing mediaPlayer");
		} catch (IOException e) {
			e.printStackTrace();
		}
		mediaPlayer.start();
		
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