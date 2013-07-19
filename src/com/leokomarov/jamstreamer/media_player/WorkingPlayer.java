package com.leokomarov.jamstreamer.media_player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
//import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.artists.TracksByAlbum;
//import android.widget.SeekBar;

//Shouldn't extend Activity
//public class AudioPlayer extends Activity implements OnCompletionListener {
public class WorkingPlayer extends Activity{
//public class AudioPlayer extends Activity implements OnCompletionListener, SeekBar.OnSeekBarChangeListener {
//public class AudioPlayer extends Activity {
	private String DEBUG = "AudioPlayer";
	//private static final String TAG_TRACK_ID = "id";
	//private static final String TAG_TRACK_NAME = "name";
	//private static final String TAG_TRACK_DURATION = "duration";
	
	private ImageButton button_play;
	private ImageButton button_forward;
	private ImageButton button_backward;
	//private ImageButton button_next;
	//private ImageButton button_previous;
	private ImageButton button_playlist;
	//private ImageButton button_repeat;
	//private ImageButton button_shuffle;
	//private SeekBar songProgressBar;
	private TextView songTitleLabel;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	
	//Media Player
	public MediaPlayer mp;
	
	//Handler to update UI timer, progress bar etc,.
	private Handler mHandler = new Handler();;
	//private SongsManager songManager;
	private Utilities utils;
    
	private int seekForwardTime = 5000; // 5000 milliseconds
	private int seekBackwardTime = 5000; // 5000 milliseconds
	//private int currentSongIndex = 0; 
	//private boolean isShuffle = false;
	//private boolean isRepeat = false;
	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Needs fixed, complains about API level if <11 in manifest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        setContentView(R.layout.audio_player);
		Log.v(DEBUG, "Created AudioPlayer");
		
		button_play = (ImageButton) findViewById(R.id.btnPlay);
		button_forward = (ImageButton) findViewById(R.id.btnForward);
		button_backward = (ImageButton) findViewById(R.id.btnBackward);
		//button_next = (ImageButton) findViewById(R.id.btnNext);
		//button_previous = (ImageButton) findViewById(R.id.btnPrevious);
		button_playlist = (ImageButton) findViewById(R.id.btnPlaylist);
		//button_repeat = (ImageButton) findViewById(R.id.btnRepeat);
		//button_shuffle = (ImageButton) findViewById(R.id.btnShuffle);
		//songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
		
		// Mediaplayer
		if (mp == null) {
			Log.v(DEBUG, "mp is null");
		    mp = new MediaPlayer();
		}
		else {
		    if(mp.isPlaying()) {
		    	Log.v(DEBUG, "Stopping mp");
		        mp.stop();
		    }
		    Log.v(DEBUG, "Not releasing mp");
		    mp.release();
		    mp = null;
		}
		if(mp.isPlaying()) {
	    	Log.v(DEBUG, "Stopping mp");
	        mp.stop();
	    }
		//songManager = new SongsManager();
		utils = new Utilities();
				
		// Listeners
		//songProgressBar.setOnSeekBarChangeListener(this); // Important
		//mp.setOnCompletionListener(this); // Important
		
		//songsList = songManager.getPlayList();
		
		// By default play first song
		//playSong(0);   
			
		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
		Intent intent = getIntent();
    	String trackID = intent.getStringExtra(TracksByAlbum.TAG_TRACK_ID);
    	String unformattedURL = getResources().getString(R.string.trackByIDURL);
		String url = String.format(unformattedURL, trackID);
		url = url.replace("&amp;", "&");
	    try {
        	mp.setDataSource(url);
	        Log.v(DEBUG, "Preparing async");
	        mp.prepareAsync();
	        Log.v(DEBUG, "Prepared async");
	    } catch(FileNotFoundException e){
	    	Log.e(DEBUG, e.getMessage(), e);
	    } catch (IllegalArgumentException e) {
	    	Log.e(DEBUG, e.getMessage(), e);
	    } catch (IllegalStateException e) {
	    	Log.e(DEBUG, e.getMessage(), e);
	    } catch (IOException e) {
	    	Log.e(DEBUG, e.getMessage(), e);
	    } catch (Exception e) {
	    	Log.e(DEBUG, e.getMessage(), e);
		}

		mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
	    	@Override
	        public void onPrepared(MediaPlayer mp) {
	    		Intent intent = getIntent();
		    	String trackName = intent.getStringExtra(TracksByAlbum.TAG_TRACK_NAME);
		    	button_play.setImageResource(R.drawable.button_pause);
	        	songTitleLabel.setText(trackName);
	            Log.v(DEBUG, "Prepared mp");
	            mp.start();
	        }
	    });
		
		button_play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( mp.isPlaying() ) {
					if(mp != null){
						mp.pause();
						button_play.setImageResource(R.drawable.button_play);
					}
				}
				else {
					if(mp != null) {
						mp.start();
						button_play.setImageResource(R.drawable.button_pause);
					}
				}		
			}
		});
		
		button_forward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get current song position				
				int currentPosition = mp.getCurrentPosition();
				// check if seekForward time is lesser than song duration
				if(currentPosition + seekForwardTime <= mp.getDuration()) {
					// forward song
					mp.seekTo(currentPosition + seekForwardTime);
				}
				else {
					// forward to end position
					mp.seekTo(mp.getDuration());
				}
			}
		});
		
		button_backward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get current song position				
				int currentPosition = mp.getCurrentPosition();
				// check if seekBackward time is greater than 0 sec
				if(currentPosition - seekBackwardTime >= 0) {
					// forward song
					mp.seekTo(currentPosition - seekBackwardTime);
				}
				else {
					// backward to starting position
					mp.seekTo(0);
				}	
			}
		});
		
		button_playlist.setOnClickListener(new View.OnClickListener() {
			@Override
            public void onClick(View v) {
                Intent button_playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
                startActivity(button_playlistIntent);
                //startActivityForResult(button_playlistIntent, 100);
            }
		});
	}
	
	public void playSong(int songIndex){
		// Play song
		try {
        	mp.reset();
			mp.setDataSource(songsList.get(songIndex).get("songPath"));
			mp.prepare();
			mp.start();
			// Displaying Song title
			Intent intent = getIntent();
	    	String trackName = intent.getStringExtra(TracksByAlbum.TAG_TRACK_NAME);
        	songTitleLabel.setText(trackName);
			
        	// Changing Button Image to pause image
			button_play.setImageResource(R.drawable.button_pause);
			
			// set Progress bar values
			//songProgressBar.setProgress(0);
			//songProgressBar.setMax(100);
			
			// Updating progress bar
			updateProgressBar();			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);        
    }
	
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			long totalDuration = mp.getDuration();
			long currentDuration = mp.getCurrentPosition();
			  
			// Displaying Total Duration time
			songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
			// Displaying time completed playing
			songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));
			   
			// Updating progress bar
			//int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
			//Log.d("Progress", "" + progress);
			//songProgressBar.setProgress(progress);
			   
			// Running this thread after 100 milliseconds
		    mHandler.postDelayed(this, 100);
		}
    };
    
    //@Override
	//public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {	
	//}
    
	 //When user starts moving the progress handler
	//@Override
	//public void onStartTrackingTouch(SeekBar seekBar) {
		// remove message Handler from updating progress bar
	//	mHandler.removeCallbacks(mUpdateTimeTask);
    //}
	
	//When user stops moving the progress handler
	//@Override
    //public void onStopTrackingTouch(SeekBar seekBar) {
	//	mHandler.removeCallbacks(mUpdateTimeTask);
	//	int totalDuration = mp.getDuration();
	//	int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
		
		// forward or backward to certain seconds
	//	mp.seekTo(currentPosition);
		
		// update timer progress again
	//	updateProgressBar();
    //}
	
	//@Override
	//public void onCompletion(MediaPlayer arg0) {
		// check for repeat is ON or OFF
	//	if(isRepeat) {
			// repeat is on play same song again
	//		playSong(currentSongIndex);
	//	} 
	//	else if(isShuffle) {
			// shuffle is on - play a random song
	//		Random rand = new Random();
	//		currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
	//		playSong(currentSongIndex);
	//	} 
	//	else {
			// no repeat or shuffle ON - play next song
	//		if(currentSongIndex < (songsList.size() - 1)){
	//			playSong(currentSongIndex + 1);
	//			currentSongIndex = currentSongIndex + 1;
	//		}
	//		else {
				// play first song
	//			playSong(0);
	//			currentSongIndex = 0;
	//		}
	//	}
	//}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    
	    if (mp != null) {
	        if(mp.isPlaying()) {
	            mp.stop();
	        }
	        mp.release();
	        mp = null;
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