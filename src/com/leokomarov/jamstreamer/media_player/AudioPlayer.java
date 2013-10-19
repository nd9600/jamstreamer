package com.leokomarov.jamstreamer.media_player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.leokomarov.jamstreamer.ComplexPreferences;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;
import com.leokomarov.jamstreamer.playlist.PlaylistList;

public class AudioPlayer extends SherlockActivity {
	protected static ImageButton button_play;
	protected static ImageButton button_forward;
	protected static ImageButton button_backward;
	protected static ImageButton button_next;
	protected static ImageButton button_previous;
	private ImageButton button_playlist;
	protected static ImageButton button_repeat;
	public static ImageButton button_shuffle;
	protected static SeekBar songProgressBar;
	protected static TextView songTitleLabel;
	protected static TextView albumLabel;
	protected static ImageView albumArt;
	protected static TextView songCurrentDurationLabel;
    protected static TextView songTotalDurationLabel;
	
	private static Handler mHandler = new Handler();
    
	private int seekForwardTime = 5000;
	private int seekBackwardTime = 5000;
	
	private ArrayList<HashMap<String, String>> getTrackListFromPreferences(){
    	ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
	    		getString(R.string.trackPreferencesFile), MODE_PRIVATE);
	    PlaylistList trackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
	    if (trackPreferencesObject != null){
    		return trackPreferencesObject.trackList;
    	}
    	else {
    		return null;
    	}
    }
		
	public void afterCreation(Intent intent){
		setContentView(R.layout.audio_player);	
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		button_play = (ImageButton) findViewById(R.id.btnPlay);
		button_forward = (ImageButton) findViewById(R.id.btnForward);
		button_backward = (ImageButton) findViewById(R.id.btnBackward);
		button_next = (ImageButton) findViewById(R.id.btnNext);
		button_previous = (ImageButton) findViewById(R.id.btnPrevious);
		button_playlist = (ImageButton) findViewById(R.id.btnPlaylist);
		button_repeat = (ImageButton) findViewById(R.id.btnRepeat);
		button_shuffle = (ImageButton) findViewById(R.id.btnShuffle);
		songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		albumLabel = (TextView) findViewById(R.id.albumTitle);
		albumArt = (ImageView)findViewById(R.id.albumArtImageview);
		songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
		
		button_play.setImageResource(R.drawable.button_play);
		button_play.setClickable(false);
    	button_forward.setClickable(false);
    	button_backward.setClickable(false);
    	button_next.setClickable(false);
    	button_previous.setClickable(false);
    	button_repeat.setClickable(false);
    	button_shuffle.setClickable(false);
    	songProgressBar.setClickable(false);
    	
    	if (AudioPlayerService.mp != null && AudioPlayerService.mp.isPlaying()){
			AudioPlayer.button_play.setImageResource(R.drawable.button_pause);
		}
    	
    	if (AudioPlayerService.repeatBoolean == true){
    		button_repeat.setImageResource(R.drawable.img_repeat_focused);
    	}

    	if (AudioPlayerService.shuffleBoolean == true){
    		button_shuffle.setImageResource(R.drawable.img_shuffle_focused);
    	}
		
		button_playlist.setOnClickListener(new View.OnClickListener() {
			@Override
            public void onClick(View v) {
                Intent button_playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
                startActivity(button_playlistIntent);
            }
		});
		
		button_play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( AudioPlayerService.mp.isPlaying() ) {
						AudioPlayerService.mp.pause();
						button_play.setImageResource(R.drawable.button_play);
				}
				else if(AudioPlayerService.mp != null) {
					int audioFocusResult = AudioPlayerService.audioManager.requestAudioFocus(AudioPlayerService.onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
					if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
						AudioPlayerService.mp.start();
				    	button_play.setImageResource(R.drawable.button_pause);
					}
					mHandler.postDelayed(mUpdateTime, 100);
				}		
			}
		});
		
		button_repeat.setOnClickListener(new View.OnClickListener() {
			@Override
            public void onClick(View v) {
				if(AudioPlayerService.repeatBoolean == true){
                	AudioPlayerService.repeatBoolean = false;
                	button_repeat.setImageResource(R.drawable.img_repeat_default);
                }
				else {
					AudioPlayerService.repeatBoolean = true;
					button_repeat.setImageResource(R.drawable.img_repeat_focused);
				}
            }
		});
		
		button_shuffle.setOnClickListener(new View.OnClickListener() {
			@Override
            public void onClick(View v) {
                if(AudioPlayerService.shuffleBoolean == true){
                	AudioPlayerService.shuffleBoolean = false;
                	button_shuffle.setImageResource(R.drawable.img_shuffle_default);
                }
                else {
					AudioPlayerService.shuffleBoolean = true;
					button_shuffle.setImageResource(R.drawable.img_shuffle_focused);					
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
				Intent audioServiceIntent = new Intent(getApplicationContext(),AudioPlayerService.class);
       			if (AudioPlayerService.repeatBoolean == true){
       				AudioPlayerService.mp.seekTo(0);
       			}
       			else {
       	            ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(AudioPlayer.this,
       	    		    getString(R.string.trackPreferencesFile), MODE_PRIVATE);
       	            SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
       	            SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
       	    		
       	    		if (AudioPlayerService.shuffleBoolean == false){
       	    			ArrayList<HashMap<String, String>> trackList = getTrackListFromPreferences();
       	    			int indexPosition = indexPositionPreference.getInt("indexPosition", -1);
       	    			
       	    			if (indexPosition + 1 <= trackList.size() - 1){
       	    				indexPosition++;
       	    				indexPositionEditor.putInt("indexPosition", indexPosition);
       	    				indexPositionEditor.commit();
       	    				startService(audioServiceIntent);
       	    			}
       	    		}
       	    		else if (AudioPlayerService.shuffleBoolean == true){
       	    			PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("shuffledTracks", PlaylistList.class);
       	    			ArrayList<HashMap<String, String>> shuffledTracklist = shuffledTrackPreferencesObject.trackList;
       	    			int shuffledIndexPosition = indexPositionPreference.getInt("shuffledIndexPosition", -1);
       	    			if (shuffledIndexPosition + 1 <= shuffledTracklist.size() - 1){
       	    				shuffledIndexPosition++;
       	    				indexPositionEditor.putInt("shuffledIndexPosition", shuffledIndexPosition);
       	    				indexPositionEditor.commit();
       	    				startService(audioServiceIntent);
       	    			}
       	    		}
       	    	}
       			
			}
		});
		
		button_previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent audioServiceIntent = new Intent(getApplicationContext(),AudioPlayerService.class);
       	        SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
       	        SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
       	            
       	    	if (AudioPlayerService.shuffleBoolean == false){
       	    		int indexPosition = indexPositionPreference.getInt("indexPosition", 1);
       	    		
       	    		if(AudioPlayerService.mp.getCurrentPosition() >= 3000){
       	    			AudioPlayerService.mp.seekTo(0);
       	    		}
       	    		else if (indexPosition != 0){
      	    			indexPosition--;
       	    			indexPositionEditor.putInt("indexPosition", indexPosition);
       	    			indexPositionEditor.commit();
       	    			startService(audioServiceIntent);
       	    		}
       	    	}
       	    	else if (AudioPlayerService.shuffleBoolean == true){
       	    		int shuffledIndexPosition = indexPositionPreference.getInt("shuffledIndexPosition", 1);
       	    		
       	    		if(AudioPlayerService.mp.getCurrentPosition() >= 3000){
       	    			AudioPlayerService.mp.seekTo(0);
       	    		}
       	    		else if (shuffledIndexPosition != 0){
       	    			shuffledIndexPosition--;
       	    			indexPositionEditor.putInt("shuffledIndexPosition", shuffledIndexPosition);
       	    			indexPositionEditor.commit();
       	    			startService(audioServiceIntent);
       	    		}
       	    	}        			
			}
		
		});
		
		songProgressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
			}

			@Override
		    public void onStartTrackingTouch(SeekBar seekBar) {
		        mHandler.removeCallbacks(mUpdateTime);
		    }

			@Override
		    public void onStopTrackingTouch(SeekBar seekBar) {
		        mHandler.removeCallbacks(mUpdateTime);
		        int progress = songProgressBar.getProgress() * 1000;
		        AudioPlayerService.mp.seekTo(progress);
		        updateProgressBar();
		    }
		});
		
		Boolean fromNotication = intent.getBooleanExtra("fromNotification", false);
		if (! fromNotication){
			Intent audioService = new Intent(getApplicationContext(), AudioPlayerService.class);
			startService(audioService);
			button_play.setImageResource(R.drawable.button_pause);
		}
		else if(fromNotication){
			ArrayList<HashMap<String, String>> trackList = new ArrayList<HashMap<String, String>>();
	    	SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
	    	int indexPosition;
			if (AudioPlayerService.shuffleBoolean == true){
				ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
					getString(R.string.trackPreferencesFile), MODE_PRIVATE);
				PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("shuffledTracks", PlaylistList.class);
				trackList = shuffledTrackPreferencesObject.trackList;
				indexPosition = indexPositionPreference.getInt("shuffledIndexPosition", 0);
			}
			else {
				trackList = getTrackListFromPreferences();
				indexPosition = indexPositionPreference.getInt("indexPosition", 0);
			}
			
			String trackName = trackList.get(indexPosition).get("trackName");
	        String artistName = trackList.get(indexPosition).get("trackArtist");
	        String trackDuration = trackList.get(indexPosition).get("trackDuration");
	        String albumName = trackList.get(indexPosition).get("trackAlbum");
	        int currentSeconds = AudioPlayerService.mp.getCurrentPosition() / 1000;
	        
	        songTitleLabel.setText(trackName + " - " + artistName);
			albumLabel.setText(albumName);
			songTotalDurationLabel.setText(trackDuration);
			songProgressBar.setProgress(currentSeconds);

			if (songProgressBar.getMax() != AudioPlayerService.mp.getDuration() / 1000){
				songProgressBar.setMax(AudioPlayerService.mp.getDuration() / 1000); 
			}
			if (AudioParser.albumImageStore != null){
				albumArt.setImageBitmap(AudioParser.albumImageStore);
			}
		}
	}
	
	@Override
	public void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		setIntent(intent);
		afterCreation(intent);
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		afterCreation(getIntent());
	}

	private static Runnable mUpdateTime = new Runnable() {
        public void run() {
            int currentSeconds = AudioPlayerService.mp.getCurrentPosition() / 1000;
            
            if (currentSeconds >= 0 && AudioPlayerService.mp != null ){
            	if (AudioPlayerService.mp.isPlaying()){
            		String currentDuration = String.format(Locale.US, "%d:%02d", currentSeconds / 60, currentSeconds % 60);
            		songCurrentDurationLabel.setText(currentDuration);
            		songProgressBar.setProgress(currentSeconds);
            		mHandler.postDelayed(this, 1000);
            	}
            }            
        }
     };
	
	public static void updateProgressBar() {
        mHandler.postDelayed(mUpdateTime, 100);
    }	
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	}
        			
	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 
	        int itemId = item.getItemId();
			if (itemId == android.R.id.home) {
				onBackPressed();
				return true;
			}
	    return super.onOptionsItemSelected(item);
	}
}