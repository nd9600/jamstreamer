package com.leokomarov.jamstreamer.audio_player;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;
import com.leokomarov.jamstreamer.utils.ComplexPreferences;

import java.util.HashMap;
import java.util.Locale;

public class AudioPlayer extends AppCompatActivity {
	protected static ImageButton button_play;
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

    private static ComplexPreferences trackPreferences;

    //called when the activity is re-launched while at the top of the activity stack instead of a new instance of the activity being started
    //since all intents creating it have the FLAG_ACTIVITY_SINGLE_TOP flag
    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        //changes the intent returned by getIntent()
        setIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        afterCreation(getIntent());
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        trackPreferences = ComplexPreferences.getComplexPreferences(this,
                getString(R.string.trackPreferences), MODE_PRIVATE);
        afterCreation(getIntent());
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

    public static void setViewsClickable(boolean clickable){
        button_play.setClickable(clickable);
        button_next.setClickable(clickable);
        button_previous.setClickable(clickable);
        button_repeat.setClickable(clickable);
        button_shuffle.setClickable(clickable);
        songProgressBar.setClickable(clickable);
    }

    public static void setMetadataAndAlbumArt(String trackAndArtist, String albumName, String trackDuration){
        songTitleLabel.setText(trackAndArtist);
        albumLabel.setText(albumName);
        songTotalDurationLabel.setText(trackDuration);
        setAlbumArt();
    }

    public static void setPlayButtonImage(boolean play){
        button_play.setImageResource(play ? R.drawable.button_play : R.drawable.button_pause);
    }

    public static void setAlbumArt(){
        albumArt.setImageBitmap(AudioPlayerService.albumImage);
    }

    public static void startProgressBar(int max){
        AudioPlayer.songProgressBar.setProgress(0);
        AudioPlayer.songProgressBar.setMax(max);
        AudioPlayer.updateProgressBar();
    }

    //Updates the progress bar in 100ms
    public static void updateProgressBar() {
        mHandler.postDelayed(mUpdateTime, 100);
    }

    //the method that actually updates the progress bar
    private static Runnable mUpdateTime = new Runnable() {
        public void run() {

            //if the player exists and is prepared
            if ((AudioPlayerService.mediaPlayer != null) && AudioPlayerService.prepared){
                int currentSeconds = AudioPlayerService.mediaPlayer.getCurrentPosition() / 1000;

                //set the bar's max duration
                if (songProgressBar.getMax() != AudioPlayerService.mediaPlayer.getDuration() / 1000){
                    songProgressBar.setMax(AudioPlayerService.mediaPlayer.getDuration() / 1000);
                }

                //if the song has started and is playing
                //set the current time and distance along the bar
                //then update the bad again
                if (currentSeconds >= 0){
                    if (AudioPlayerService.mediaPlayer.isPlaying()){
                        String currentDuration = String.format(Locale.US, "%d:%02d", currentSeconds / 60, currentSeconds % 60);
                        songCurrentDurationLabel.setText(currentDuration);
                        songProgressBar.setProgress(currentSeconds);
                        mHandler.postDelayed(this, 1000);
                    }
                }
            }
        }
    };

    //called after initial creation and whenever the activity is resumed
    //ie when it's in focus
	public void afterCreation(Intent intent){
		setContentView(R.layout.audio_player);	
		getSupportActionBar();//.setDisplayHomeAsUpEnabled(true);

        //sets the media stream that the volume buttons control
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		button_play = (ImageButton) findViewById(R.id.btnPlay);
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
    	button_next.setClickable(false);
    	button_previous.setClickable(false);
    	button_repeat.setClickable(false);
    	button_shuffle.setClickable(false);
    	songProgressBar.setClickable(false);    	

        //set the initial button images

    	if (AudioPlayerService.mediaPlayer != null && AudioPlayerService.mediaPlayer.isPlaying()){
			button_play.setImageResource(R.drawable.button_pause);
		}
    	
    	if (AudioPlayerService.repeatBoolean){
    		button_repeat.setImageResource(R.drawable.img_repeat_focused);
    	}

    	if (AudioPlayerService.shuffleBoolean){
    		button_shuffle.setImageResource(R.drawable.img_shuffle_focused);
    	}

        //set the initial duration and progress bar views
    	
    	if (AudioPlayerService.mediaPlayer != null){
    		int currentSeconds = AudioPlayerService.mediaPlayer.getCurrentPosition() / 1000;
            int songLength = AudioPlayerService.mediaPlayer.getDuration() / 1000;
    		String currentDuration = String.format(Locale.US, "%d:%02d", currentSeconds / 60, currentSeconds % 60);
    		
    		if (songProgressBar.getMax() != songLength){
				songProgressBar.setMax(songLength);
			}
    		
    		if (songProgressBar.getProgress() != currentSeconds){
    			songProgressBar.setProgress(currentSeconds);
    		}
            
    		if (! songCurrentDurationLabel.getText().toString().equals(currentDuration)){
    			songCurrentDurationLabel.setText(currentDuration);
    			songProgressBar.setProgress(currentSeconds);
    		}
    	}

        //if we didn't come from the notification,
        //start the audio service
        Boolean fromNotification = intent.getBooleanExtra("fromNotification", false);
        if (! fromNotification){
            button_play.setImageResource(R.drawable.button_pause);
            Intent audioServiceIntent = new Intent(getApplicationContext(), AudioPlayerService.class);
            audioServiceIntent.setAction(AudioPlayerService.ACTION_MAKE_NOTIFICATION);
            AudioPlayerService.playFirstSong = true;
            startService(audioServiceIntent);
        }

        //else set all the activity's views
        else {
            SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
            String nameOfIndexPosition = (AudioPlayerService.shuffleBoolean ? "shuffledIndexPosition" : "indexPosition");
            int indexPosition = indexPositionPreference.getInt(nameOfIndexPosition, -1);

            Log.v("audioPlayer", "setting all the views");

            if (AudioPlayerService.shuffleBoolean){
                indexPosition = AudioPlayerService.shuffleList[indexPosition];
            }

            HashMap<String, String> trackMap = AudioPlayerService.tracklist.get(indexPosition);

            String trackName = trackMap.get("trackName");
            String artistName = trackMap.get("trackArtist");
            String trackDuration = trackMap.get("trackDuration");
            String albumName = trackMap.get("trackAlbum");

            songTitleLabel.setText(String.format("%s - %s", trackName, artistName));
            albumLabel.setText(albumName);
            songTotalDurationLabel.setText(trackDuration);

            if (AudioPlayerService.albumImage != null){
                albumArt.setImageBitmap(AudioPlayerService.albumImage);
            }
        }

        //set all the button onClick listeners
		
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
				AudioPlayerService.playOrPause();

                if (AudioPlayerService.mediaPlayer != null){
                    mHandler.postDelayed(mUpdateTime, 100);
                }
			}
		});

        button_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioPlayerService.gotoPrevious();
            }
        });

        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioPlayerService.gotoNext();
            }
        });
		
		button_repeat.setOnClickListener(new View.OnClickListener() {
			@Override
            public void onClick(View v) {
				if(AudioPlayerService.repeatBoolean){
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
                if(AudioPlayerService.shuffleBoolean){
                	AudioPlayerService.shuffleBoolean = false;
                	button_shuffle.setImageResource(R.drawable.img_shuffle_default);
                }
                else {
					AudioPlayerService.shuffleBoolean = true;
					button_shuffle.setImageResource(R.drawable.img_shuffle_focused);					
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
		        AudioPlayerService.mediaPlayer.seekTo(progress);
		        updateProgressBar();
		    }
		});
	}
}