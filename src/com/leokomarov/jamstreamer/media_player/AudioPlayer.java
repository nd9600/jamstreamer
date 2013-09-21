package com.leokomarov.jamstreamer.media_player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

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
	private ArrayList<HashMap<String, String>> unshuffledTrackList = new ArrayList<HashMap<String, String>>();
	
	protected static ImageButton button_play;
	protected static ImageButton button_forward;
	protected static ImageButton button_backward;
	protected static ImageButton button_next;
	protected static ImageButton button_previous;
	private ImageButton button_playlist;
	private ImageButton button_repeat;
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
	
	public void afterIntent(){
        setContentView(R.layout.audio_player);	
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        unshuffledTrackList = getTrackListFromPreferences();
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
		
		Intent audioService = new Intent(getApplicationContext(), AudioPlayerService.class);
		startService(audioService);
		
		button_play.setImageResource(R.drawable.button_pause);
	    
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
					AudioPlayerService.mp.start();
			    	button_play.setImageResource(R.drawable.button_pause);
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
					
					ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(AudioPlayer.this,
						getString(R.string.trackPreferencesFile), MODE_PRIVATE);
					PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("shuffledTracks", PlaylistList.class);

					if (shuffledTrackPreferencesObject == null || ! unshuffledTrackList.equals(getTrackListFromPreferences()) ){
						if (! unshuffledTrackList.equals(getTrackListFromPreferences()) ){
							unshuffledTrackList = getTrackListFromPreferences();
						}
						ArrayList<HashMap<String, String>> shuffledTrackList = getTrackListFromPreferences();
						Random randomNumber = new Random();
						for (int i = 0; i < shuffledTrackList.size(); i++) {
							int changeTo = i + randomNumber.nextInt(shuffledTrackList.size() - i);
							shuffledTrackList.set(i, shuffledTrackList.get(changeTo));
							shuffledTrackList.set(changeTo, shuffledTrackList.get(i));
						}
            	    
						PlaylistList trackListObject = new PlaylistList();
						trackListObject.setTrackList(shuffledTrackList);  
						trackPreferences.putObject("shuffledTracks", trackListObject);
						trackPreferences.commit();
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
				ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(AudioPlayer.this,
					getString(R.string.trackPreferencesFile), MODE_PRIVATE);
			    SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
			    SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
					
				if (AudioPlayerService.repeatBoolean == true || AudioPlayerService.shuffleBoolean == false){
					ArrayList<HashMap<String, String>> trackList = getTrackListFromPreferences();
					if (! trackList.isEmpty()){
						int indexPosition = indexPositionPreference.getInt("indexPosition", -1);
					
						if (AudioPlayerService.repeatBoolean == true){
							Intent audioServiceIntent = new Intent(getApplicationContext(),AudioPlayerService.class);
			       			startService(audioServiceIntent);
						}
						else if (indexPosition + 1 <= trackList.size() - 1){
							indexPosition++;
							indexPositionEditor.putInt("indexPosition", indexPosition);
							indexPositionEditor.commit();
				        	
							Intent audioServiceIntent = new Intent(getApplicationContext(),AudioPlayerService.class);
			       			startService(audioServiceIntent);
						}
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
			        	
						Intent audioServiceIntent = new Intent(getApplicationContext(),AudioPlayerService.class);
		        		startService(audioServiceIntent);
					}
				}
			}
		
		});
		
		button_previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
				int indexPosition = indexPositionPreference.getInt("indexPosition", 0);
				
        		if (indexPosition - 1 <= 0){
        			indexPosition--;
    		        SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
    	        	indexPositionEditor.putInt("indexPosition", indexPosition);
    	        	indexPositionEditor.commit();
    	        	
        			Intent audioServiceIntent = new Intent(getApplicationContext(),AudioPlayerService.class);
        			startService(audioServiceIntent);
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
	}
	
	@Override
	public void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		setIntent(intent);
		
		Boolean fromNotication = intent.getBooleanExtra("fromNotification", false);
		if (! fromNotication){
			afterIntent();
		}
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Boolean fromNotication = getIntent().getBooleanExtra("fromNotification", false);
		if (! fromNotication){
			afterIntent();
		}
	}

	private static Runnable mUpdateTime = new Runnable() {
        public void run() {
            int currentSeconds = AudioPlayerService.mp.getCurrentPosition() / 1000;
            String currentDuration = String.format(Locale.US, "%d:%02d", currentSeconds / 60, currentSeconds % 60);
            
            songCurrentDurationLabel.setText(currentDuration);
            songProgressBar.setProgress(currentSeconds);
            mHandler.postDelayed(this, 1000);
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