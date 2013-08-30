package com.leokomarov.jamstreamer.media_player;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;
//import java.util.Random;
//import android.media.MediaPlayer.OnCompletionListener;

public class AudioPlayer extends Activity {
//public class AudioPlayer extends Activity implements OnCompletionListener, SeekBar.OnSeekBarChangeListener {
	private String DEBUG = "AudioPlayer";
	
	protected static ImageButton button_play;
	protected static ImageButton button_forward;
	protected static ImageButton button_backward;
	private ImageButton button_next;
	private ImageButton button_previous;
	private ImageButton button_playlist;
	//private ImageButton button_repeat;
	//private ImageButton button_shuffle;
	//private SeekBar songProgressBar;
	protected static TextView songTitleLabel;
	//protected static TextView artistLabel;
	protected static TextView albumLabel;
	protected static ImageView songThumbnailImageView;
	//private TextView songCurrentDurationLabel;
    protected static TextView songTotalDurationLabel;
	
	//Media Player
	protected MediaPlayer mp;
	
	//Handler to update UI timer, progress bar etc,.
	//private Handler mHandler = new Handler();;
	//private SongsManager songManager;
	//private Utilities utils;
    
	private int seekForwardTime = 5000; // 5000 milliseconds
	private int seekBackwardTime = 5000; // 5000 milliseconds
	//private int currentSongIndex = 0; 
	//private boolean isShuffle = false;
	//private boolean isRepeat = false;
	
	/*
	private ArrayList<HashMap<String, String>> getTrackListFromPreferences(){
    	ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
	    		getString(R.string.trackPreferencesFile), MODE_PRIVATE);
	    PlaylistList trackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
	    return trackPreferencesObject.trackList;
    }
    */
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        setContentView(R.layout.audio_player);
		Log.v(DEBUG, "Created AudioPlayer");
		
		button_play = (ImageButton) findViewById(R.id.btnPlay);
		button_forward = (ImageButton) findViewById(R.id.btnForward);
		button_backward = (ImageButton) findViewById(R.id.btnBackward);
		button_next = (ImageButton) findViewById(R.id.btnNext);
		button_previous = (ImageButton) findViewById(R.id.btnPrevious);
		button_playlist = (ImageButton) findViewById(R.id.btnPlaylist);
		//button_repeat = (ImageButton) findViewById(R.id.btnRepeat);
		//button_shuffle = (ImageButton) findViewById(R.id.btnShuffle);
		//songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		//artistLabel = (TextView) findViewById(R.id.artistTitle);
		albumLabel = (TextView) findViewById(R.id.albumTitle);
		songThumbnailImageView = (ImageView)findViewById(R.id.songThumbnailImage);
		//songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
		
		button_play.setImageResource(R.drawable.button_play);
		button_play.setClickable(false);
    	button_forward.setClickable(false);
    	button_backward.setClickable(false);
		
		Intent audioService = new Intent(getApplicationContext(),AudioPlayerService.class);
		Log.v(DEBUG, "Starting AudioPlayerService");
		startService(audioService);
		
		button_play.setImageResource(R.drawable.button_pause);
	    
		button_play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( AudioPlayerService.mp.isPlaying() ) {
					if(AudioPlayerService.mp != null){
						AudioPlayerService.mp.pause();
						button_play.setImageResource(R.drawable.button_play);
					}
				}
				else {
					if(AudioPlayerService.mp != null) {
						AudioPlayerService.mp.start();
						button_play.setImageResource(R.drawable.button_pause);
					}
				}		
			}
		});
		
		button_forward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int currentPosition = AudioPlayerService.mp.getCurrentPosition();
				if(currentPosition + seekForwardTime <= AudioPlayerService.mp.getDuration()) {
					AudioPlayerService.mp.seekTo(currentPosition + seekForwardTime);
				}
				else {
					AudioPlayerService.mp.seekTo(AudioPlayerService.mp.getDuration());
				}
			}
		});
		
		button_backward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int currentPosition = AudioPlayerService.mp.getCurrentPosition();
				if(currentPosition - seekBackwardTime >= 0) {
					AudioPlayerService.mp.seekTo(currentPosition - seekBackwardTime);
				}
				else {
					AudioPlayerService.mp.seekTo(0);
				}	
			}
		});
		
		button_next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//if(trackList.){
					
				//}
			}
		
		});
		
		button_previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		
		});
		
		button_playlist.setOnClickListener(new View.OnClickListener() {
			@Override
            public void onClick(View v) {
                Intent button_playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
                startActivity(button_playlistIntent);
            }
		});
	}	
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    
	    if (AudioPlayerService.mp != null) {
	        if( AudioPlayerService.mp.isPlaying() ) {
	        	AudioPlayerService.mp.stop();
	        }
	        AudioPlayerService.mp.release();
	        AudioPlayerService.mp = null;
	        Log.v(DEBUG, "Destroyed mp");
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