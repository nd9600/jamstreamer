package com.leokomarov.jamstreamer.media_player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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

import com.leokomarov.jamstreamer.ComplexPreferences;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.playlist.PlaylistList;

public class AudioPlayerService extends Service implements OnErrorListener, OnPreparedListener, OnCompletionListener{
	protected static MediaPlayer mp;
	protected static AudioManager audioManager;
	protected static WifiLock wifiLock;
	private static int lastKnownAudioFocusState;
	private static boolean wasPlayingWhenTransientLoss;
	private static int originalVolume;
	protected static boolean repeatBoolean;
	public static boolean shuffleBoolean;
	private String artistAndAlbumStore = "";

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
	
	protected void playSong(int indexPosition){
		ArrayList<HashMap<String, String>> trackList = new ArrayList<HashMap<String, String>>();
		if (AudioPlayerService.shuffleBoolean == true){
			ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
				getString(R.string.trackPreferencesFile), MODE_PRIVATE);
			PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("shuffledTracks", PlaylistList.class);
			trackList = shuffledTrackPreferencesObject.trackList;
		}
		else {
			trackList = getTrackListFromPreferences();
		}
    	int trackID = Integer.parseInt(trackList.get(indexPosition).get("trackID"));
    	
    	SharedPreferences currentTrackPreference = getSharedPreferences(getString(R.string.currentTrackPreferences), 0);
    	SharedPreferences.Editor currentTrackEditor = currentTrackPreference.edit();
    	currentTrackEditor.putInt("currentTrack", trackID);
        currentTrackEditor.commit();
        
    	String unformattedURL = getResources().getString(R.string.trackByIDURL);
    	String url = String.format(unformattedURL, trackID).replace("&amp;", "&");
    	String unformattedTrackInfoURL = getResources().getString(R.string.trackInformation);
    	String trackInfoURL = String.format(unformattedTrackInfoURL, trackID).replace("&amp;", "&");
    	
    	String trackName = trackList.get(indexPosition).get("trackName");
        String artistName = trackList.get(indexPosition).get("trackArtist");
        String trackDuration = trackList.get(indexPosition).get("trackDuration");
        String albumName = trackList.get(indexPosition).get("trackAlbum");
        String artistAndAlbum = artistName + " - "  + albumName;
        
        AudioPlayer.songTitleLabel.setText(trackName + " - " + artistName);
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
          	
    	mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
    	try {
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
            builder.setContentTitle("JamStreamer");  
            builder.setContentText("" + trackName + " - " + artistName);  
            Intent notificationIntent = new Intent(this, AudioPlayer.class);  
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            notificationIntent.putExtra("fromNotification", true);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);  
            builder.setContentIntent(contentIntent);
        	startForeground(46798, builder.getNotification());
    		mp.prepareAsync();
    	} catch(FileNotFoundException e){
    	} catch (IllegalArgumentException e) {
    	} catch (IllegalStateException e) {
    	} catch(RuntimeException e){
    	} catch (IOException e) {
    	} catch (Exception e) {
    	} 	
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    }
    
    @Override
    public int onStartCommand(Intent AudioPlayerServiceIntent,int flags, int startId) {  
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
		
        int trackID = Integer.parseInt(trackList.get(indexPosition).get("trackID") );
        SharedPreferences currentTrackPreference = getSharedPreferences(getString(R.string.currentTrackPreferences), 0);
        int currentTrackID = currentTrackPreference.getInt("currentTrack", 0);  
  
    	if (mp == null){
        	mp = new MediaPlayer();
        	playSong(indexPosition);        	
        }
    	else {
        	if (trackID == currentTrackID ){
       			String trackName = trackList.get(indexPosition).get("trackName");
       	        String trackDuration = trackList.get(indexPosition).get("trackDuration");
       	        String artistName = trackList.get(indexPosition).get("trackArtist");
       	        String albumName = trackList.get(indexPosition).get("trackAlbum");
       	        AudioPlayer.songTitleLabel.setText(trackName + " - " + artistName);
       			AudioPlayer.albumLabel.setText(albumName);
       			AudioPlayer.songTotalDurationLabel.setText(trackDuration);
       			AudioPlayer.albumArt.setImageBitmap(AudioParser.albumImageStore);

       			mp.seekTo(0);       			
        	}
        	else {
        		mp.stop();
        		stopForeground(true);
        		mp.reset();
        		playSong(indexPosition);
        	}    	
        }
        
        return super.onStartCommand(AudioPlayerServiceIntent,flags,startId);
    }
 
  
    @Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		return true;
	}
    
    @Override
    public void onPrepared(MediaPlayer mp) {
		int audioFocusResult = audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			mp.start();
	        AudioPlayer.songProgressBar.setProgress(0);
	        AudioPlayer.songProgressBar.setMax(mp.getDuration() / 1000);
	        AudioPlayer.updateProgressBar();
		}
        AudioPlayer.button_play.setClickable(true);
        AudioPlayer.button_forward.setClickable(true);
        AudioPlayer.button_backward.setClickable(true);
        AudioPlayer.button_next.setClickable(true);
        AudioPlayer.button_previous.setClickable(true);
        AudioPlayer.button_repeat.setClickable(true);
        AudioPlayer.button_shuffle.setClickable(true);
        AudioPlayer.songProgressBar.setClickable(true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
    	AudioPlayer.button_play.setClickable(false);
    	AudioPlayer.button_forward.setClickable(false);
    	AudioPlayer.button_backward.setClickable(false);
    	AudioPlayer.button_next.setClickable(false);
        AudioPlayer.button_previous.setClickable(false);
        AudioPlayer.button_repeat.setClickable(false);
        AudioPlayer.button_shuffle.setClickable(false);
        AudioPlayer.songProgressBar.setClickable(false);
    	AudioPlayer.button_play.setImageResource(R.drawable.button_play);
    	stopForeground(true);
    	
    	if ( mp.isPlaying() ){
    		mp.stop();
    	}
    	
    	if (repeatBoolean == true){
			mp.seekTo(0);
		}
    	else {
        	mp.reset();
            audioManager.abandonAudioFocus(onAudioFocusChangeListener);
            wifiLock.release();

            ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
    		    getString(R.string.trackPreferencesFile), MODE_PRIVATE);
            SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
            SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();
    		
    		if (shuffleBoolean == false){
    			ArrayList<HashMap<String, String>> trackList = getTrackListFromPreferences();
    			int indexPosition = indexPositionPreference.getInt("indexPosition", -1);
    			
    			if (indexPosition + 1 <= trackList.size() - 1){
    				indexPosition++;
    				indexPositionEditor.putInt("indexPosition", indexPosition);
    				indexPositionEditor.commit();
    				playSong(indexPosition);
    			}
    		}
    		else if (shuffleBoolean == true){
    			PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("shuffledTracks", PlaylistList.class);
    			ArrayList<HashMap<String, String>> shuffledTracklist = shuffledTrackPreferencesObject.trackList;
    			int shuffledIndexPosition = indexPositionPreference.getInt("shuffledIndexPosition", -1);
    			if (shuffledIndexPosition + 1 <= shuffledTracklist.size() - 1){
    				shuffledIndexPosition++;
    				indexPositionEditor.putInt("shuffledIndexPosition", shuffledIndexPosition);
    				indexPositionEditor.commit();
    				playSong(shuffledIndexPosition);
    			}
    		}
    	}
    }    
    
    protected static OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {    
    	@Override
    	public void onAudioFocusChange(int focusChange) {
    		lastKnownAudioFocusState = focusChange;
    		if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				if (lastKnownAudioFocusState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT && wasPlayingWhenTransientLoss) {
	    			originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	    			mp.start();
				}
				else if (lastKnownAudioFocusState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
	    		}     
			}
    		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS){
    			audioManager.abandonAudioFocus(onAudioFocusChangeListener);
    			if (mp.isPlaying()){
    				mp.pause();
    			}
    		}
    		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
    			wasPlayingWhenTransientLoss = mp.isPlaying();
    			if (wasPlayingWhenTransientLoss){
    				mp.pause();
    			}
    		}
    		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){
    			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);
    		}
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