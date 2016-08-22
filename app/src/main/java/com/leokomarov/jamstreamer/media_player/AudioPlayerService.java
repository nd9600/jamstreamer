package com.leokomarov.jamstreamer.media_player;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.playlist.PlaylistList;
import com.leokomarov.jamstreamer.utils.ComplexPreferences;

import java.util.ArrayList;
import java.util.HashMap;

public class AudioPlayerService extends Service implements OnErrorListener, OnPreparedListener, OnCompletionListener{
	protected static MediaPlayer mp;
	protected static AudioManager audioManager;
	protected static WifiLock wifiLock;
	protected static boolean prepared;
	private static int lastKnownAudioFocusState;
	private static boolean wasPlayingWhenTransientLoss;
	private static int originalVolume;
	protected static boolean repeatBoolean;
	public static boolean shuffleBoolean;
	private String artistAndAlbumStore = "";
	
	private ArrayList<HashMap<String, String>> getTrackListFromPreferences(){
    	ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
	    		getString(R.string.trackPreferences), MODE_PRIVATE);
	    PlaylistList trackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
	    if (trackPreferencesObject != null){
    		return trackPreferencesObject.trackList;
    	}
    	else {
    		return null;
    	}
    }
	
	protected void playSong(int indexPosition){
		ArrayList<HashMap<String, String>> trackList;
		if (AudioPlayerService.shuffleBoolean){
			ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
				getString(R.string.trackPreferences), MODE_PRIVATE);
			PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("shuffledTracks", PlaylistList.class);
			trackList = shuffledTrackPreferencesObject.trackList;
		}
		else {
			trackList = getTrackListFromPreferences();
		}
				
		if (trackList != null && trackList.size() != 0){
			int trackID = Integer.parseInt(trackList.get(indexPosition).get("trackID"));
	    	
	    	SharedPreferences currentTrackPreference = getSharedPreferences(getString(R.string.currentTrackPreferences), 0);
	    	SharedPreferences.Editor currentTrackEditor = currentTrackPreference.edit();
	    	currentTrackEditor.putInt("currentTrack", trackID);
	        currentTrackEditor.apply();
	        
	    	String unformattedURL = getResources().getString(R.string.trackByIDURL);
	    	String url = String.format(unformattedURL, trackID).replace("&amp;", "&");
	    	Log.v("service-playSong","url = " + url);
	    	String unformattedTrackInfoURL = getResources().getString(R.string.trackInformation);
	    	String trackInfoURL = String.format(unformattedTrackInfoURL, trackID).replace("&amp;", "&");
	    	
	    	String trackName = trackList.get(indexPosition).get("trackName");
	        String artistName = trackList.get(indexPosition).get("trackArtist");
	        String trackDuration = trackList.get(indexPosition).get("trackDuration");
	        String albumName = trackList.get(indexPosition).get("trackAlbum");
	        String artistAndAlbum = artistName + " - "  + albumName;
	        
	    	mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
	    	try {
	    		AudioPlayer.songTitleLabel.setText(String.format("%s - %s", trackName, artistName));
	            AudioPlayer.albumLabel.setText(albumName);
	            AudioPlayer.songCurrentDurationLabel.setText("0:00");
	            AudioPlayer.songTotalDurationLabel.setText(trackDuration);

	    		if (AudioParser.albumImageStore != null){
	    			AudioPlayer.albumArt.setImageBitmap(AudioParser.albumImageStore);
	    		}
	    		BitmapDrawable albumImageDrawable = ((BitmapDrawable)AudioPlayer.albumArt.getDrawable());
	    		
	    		if (albumImageDrawable == null | ! artistAndAlbumStore.equals(artistAndAlbum)){
	    			AudioParser jParser = new AudioParser();
	    			jParser.execute(trackInfoURL, artistName + " - "  + albumName);
	    			artistAndAlbumStore = artistName + " - "  + albumName;
	    		}
	    		
	    		prepared = false;
	    		Log.v("service-playSong","Preparing");
	    		mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
	    		wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "myWifiLock");
	    		wifiLock.acquire();
	    		mp.setDataSource(url);
	    		mp.setOnCompletionListener(this);
	        	mp.setOnPreparedListener(this);
	        	mp.setOnErrorListener(this);
	        	AudioPlayer.button_play.setImageResource(R.drawable.button_pause);
				
	    		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);  
	        	builder.setSmallIcon(R.drawable.img_ic_launcher);
	            builder.setContentTitle(trackName);  
	            builder.setContentText(artistName + " - " + albumName);
                
	            Intent notificationIntent = new Intent(this, AudioPlayer.class);  
	            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	            notificationIntent.putExtra("fromNotification", true);
	            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);  
	            builder.setContentIntent(contentIntent);
	        	startForeground(46798, builder.getNotification());
	    		mp.prepareAsync();
	    	} catch(NullPointerException e){
	        	android.util.Log.e("AudioPlayerService","NullPointerException :" + e.getMessage());
	        	android.util.Log.v("AudioPlayerService","trackList.get(indexPosition) :" + trackList.get(indexPosition));
	    	} catch (Exception e) {
                Log.e("AudioPlayerService", "Exception: " + e.getMessage());
	    	}
		}
	
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    }
    
    @Override
    public int onStartCommand(Intent AudioPlayerServiceIntent,int flags, int startId) {  
    	ArrayList<HashMap<String, String>> trackList;
    	SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
    	int indexPosition;
		if (AudioPlayerService.shuffleBoolean){
			ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
				getString(R.string.trackPreferences), MODE_PRIVATE);
			PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("shuffledTracks", PlaylistList.class);
			trackList = shuffledTrackPreferencesObject.trackList;
			indexPosition = indexPositionPreference.getInt("shuffledIndexPosition", 0);
		}
		else {
			trackList = getTrackListFromPreferences();
			indexPosition = indexPositionPreference.getInt("indexPosition", 0);
		}

        assert trackList != null;
        int trackID = Integer.parseInt(trackList.get(indexPosition).get("trackID") );
        SharedPreferences currentTrackPreference = getSharedPreferences(getString(R.string.currentTrackPreferences), 0);
        int currentTrackID = currentTrackPreference.getInt("currentTrack", 0);  
  
    	if (mp == null){
        	mp = new MediaPlayer();
        	Log.v("service-onStartCommand", "Playing new song");
        	playSong(indexPosition);        	
        }
    	else if (trackID == currentTrackID){
       			String trackName = trackList.get(indexPosition).get("trackName");
       	        String trackDuration = trackList.get(indexPosition).get("trackDuration");
       	        String artistName = trackList.get(indexPosition).get("trackArtist");
       	        String albumName = trackList.get(indexPosition).get("trackAlbum");
       	        AudioPlayer.songTitleLabel.setText(String.format("%s - %s", trackName, artistName));
       			AudioPlayer.albumLabel.setText(albumName);
       			AudioPlayer.songTotalDurationLabel.setText(trackDuration);
       			AudioPlayer.albumArt.setImageBitmap(AudioParser.albumImageStore);			
        } else {
        	if (mp.isPlaying()){
        		mp.stop();
        	}
        	stopForeground(true);
        	mp.reset();
        	Log.v("service-onStartCommand", "Playing new song");
        	playSong(indexPosition);    	
        }
        
        return super.onStartCommand(AudioPlayerServiceIntent,flags,startId);
    }
 
  
    @Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
    	android.util.Log.v("onError", "Error: " + what + " " + extra);
		return true;
	}
    
    @Override
    public void onPrepared(MediaPlayer mp) {
    	Log.v("service-onPrepared", "Prepared");
    	prepared = true;
    	
		int audioFocusResult = audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		if (mp != null && audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			
			try { 
				mp.start();
			} catch (IllegalArgumentException e) {
                Log.e("AudioPlayerService", "IllegalArgumentException e: " + e.getMessage());
            }
			
	        AudioPlayer.songProgressBar.setProgress(0);
	        AudioPlayer.songProgressBar.setMax(mp.getDuration() / 1000);
	        AudioPlayer.updateProgressBar();
		}
        AudioPlayer.button_play.setClickable(true);
        AudioPlayer.button_next.setClickable(true);
        AudioPlayer.button_previous.setClickable(true);
        AudioPlayer.button_repeat.setClickable(true);
        AudioPlayer.button_shuffle.setClickable(true);
        AudioPlayer.songProgressBar.setClickable(true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
    	if ( mp.isPlaying() ){
    		mp.stop();
    	}
    	
    	if (repeatBoolean){
			mp.seekTo(0);
		}
    	else {
    		prepared = false;
            audioManager.abandonAudioFocus(onAudioFocusChangeListener);
            if (wifiLock.isHeld()){
            	wifiLock.release();
            }
            stopForeground(true);
            AudioPlayer.button_play.setImageResource(R.drawable.button_play);

            ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
    		    getString(R.string.trackPreferences), MODE_PRIVATE);
            SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
            SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
    		
    		if (! shuffleBoolean){
    			ArrayList<HashMap<String, String>> trackList = getTrackListFromPreferences();
    			int indexPosition = indexPositionPreference.getInt("indexPosition", -1);

                assert trackList != null;
                if (indexPosition + 1 <= trackList.size() - 1){
    				AudioPlayer.button_play.setClickable(false);
    		    	AudioPlayer.button_next.setClickable(false);
    		        AudioPlayer.button_previous.setClickable(false);
    		        AudioPlayer.button_repeat.setClickable(false);
    		        AudioPlayer.button_shuffle.setClickable(false);
    		        AudioPlayer.songProgressBar.setClickable(false);
    		    	AudioPlayer.button_play.setImageResource(R.drawable.button_play);
    				mp.reset();
    				indexPosition++;
    				indexPositionEditor.putInt("indexPosition", indexPosition);
    				indexPositionEditor.apply();
    				playSong(indexPosition);
    			}
    		}
    		else {
    			PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("shuffledTracks", PlaylistList.class);
    			ArrayList<HashMap<String, String>> shuffledTracklist = shuffledTrackPreferencesObject.trackList;
    			int shuffledIndexPosition = indexPositionPreference.getInt("shuffledIndexPosition", -1);
    			
    			if (shuffledIndexPosition + 1 <= shuffledTracklist.size() - 1){
    				AudioPlayer.button_play.setClickable(false);
    		    	AudioPlayer.button_next.setClickable(false);
    		        AudioPlayer.button_previous.setClickable(false);
    		        AudioPlayer.button_repeat.setClickable(false);
    		        AudioPlayer.button_shuffle.setClickable(false);
    		        AudioPlayer.songProgressBar.setClickable(false);
    		    	AudioPlayer.button_play.setImageResource(R.drawable.button_play);
    				mp.reset();
    				shuffledIndexPosition++;
    				indexPositionEditor.putInt("shuffledIndexPosition", shuffledIndexPosition);
    				indexPositionEditor.apply();
    				playSong(shuffledIndexPosition);
    			}
    		}
    	}
    }    
    
    protected static OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {    
    	@Override
    	public void onAudioFocusChange(int focusChange) { 		
    		if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				if (lastKnownAudioFocusState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT && wasPlayingWhenTransientLoss) {
	    			originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	    			if (prepared){
	    				try { 
	    					mp.start();
	    				} catch (IllegalArgumentException e) {
                            Log.e("AudioPlayerService", "IllegalArgumentException: " + e.getMessage());
	    				}
	    			}
				}
				else if (lastKnownAudioFocusState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
	    		}     
			}
    		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS){
    			audioManager.abandonAudioFocus(onAudioFocusChangeListener);
    			
    			if (mp != null && mp.isPlaying()){
    				mp.pause();
    			}
    		}
    		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
    			wasPlayingWhenTransientLoss = mp.isPlaying();
    			if (mp != null && wasPlayingWhenTransientLoss){
    				mp.pause();
    			}
    		}
    		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){
    			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);
    		}
    		lastKnownAudioFocusState = focusChange;
    	}	
    };
    
    @Override
    public IBinder onBind(Intent AudioPlayerServiceIntent) {
        return null;
    }
    

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mp != null) {
	        if( mp.isPlaying() ) {
	        	mp.stop();
	        	stopForeground(true);
	        }
	        mp = null;
	    }
        if (wifiLock.isHeld()) {
    		wifiLock.release();
    	}        
    }
}