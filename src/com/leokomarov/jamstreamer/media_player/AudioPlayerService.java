package com.leokomarov.jamstreamer.media_player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.leokomarov.jamstreamer.ComplexPreferences;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.playlist.PlaylistList;

public class AudioPlayerService extends Service implements MediaPlayer.OnErrorListener,
	MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener{
	
	private String DEBUG = "AudioPlayerService";
	public static MediaPlayer mp;
	
	private ArrayList<HashMap<String, String>> getTrackListFromPreferences(){
    	ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
	    		getString(R.string.trackPreferencesFile), MODE_PRIVATE);
	    PlaylistList trackPreferencesObject = trackPreferences.getObject("tracks", PlaylistList.class);
	    return trackPreferencesObject.trackList;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();      
        Log.v(DEBUG, "Created AudioPlayerService");
        
    }
    
    @Override
    public int onStartCommand(Intent AudioPlayerServiceIntent,int flags, int startId) {
        Log.v(DEBUG, "In onStart");
    	
        ArrayList<HashMap<String, String>> trackList = getTrackListFromPreferences();
        SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
        int indexPosition = indexPositionPreference.getInt("indexPosition", 0);
        int trackID = Integer.parseInt(trackList.get(indexPosition).get("id") );
        SharedPreferences currentTrackPreference = getSharedPreferences(getString(R.string.currentTrackPreferences), 0);
        int newID = currentTrackPreference.getInt("currentTrack", 0);
        
        Log.v(DEBUG,"trackID in onStartCommand in AudioPlayerService is " + trackID);
        
    	if (mp == null){
        	Log.v(DEBUG, "mp is null");
        	mp = new MediaPlayer();
        	playSong(indexPosition);
        	}
        
        if( mp.isPlaying() ) {
        	Log.v(DEBUG, "mp is playing");
        	if ( trackID == newID){
        		Log.v(DEBUG, "Restarting song");
        		mp.seekTo(0);
        	}
        	else {
        		mp.stop();
        		mp.reset();
        		playSong(indexPosition);
        	}
                	
        }
        
        Log.v(DEBUG, "Outside ACTION_PLAY");
        return super.onStartCommand(AudioPlayerServiceIntent,flags,startId);
    }
    
    private void playSong(int indexPosition){
    	
    	ArrayList<HashMap<String, String>> trackList = getTrackListFromPreferences();
    	String trackID = trackList.get(indexPosition).get("id");
    	String unformattedURL = getResources().getString(R.string.trackByIDURL);
    	String url = String.format(unformattedURL, trackID).replace("&amp;", "&");
    	
    	String unformattedTrackInfoURL = getResources().getString(R.string.trackInformation);
    	String trackInfoURL = String.format(unformattedTrackInfoURL, trackID).replace("&amp;", "&");
    	
    	AudioParser jParser = new AudioParser();
		jParser.execute(trackInfoURL);
    	
    	SharedPreferences currentTrackPreference = getSharedPreferences(getString(R.string.currentTrackPreferences), 0);
    	SharedPreferences.Editor currentTrackEditor = currentTrackPreference.edit();
    	currentTrackEditor.putInt("currentTrack", Integer.parseInt(trackID) );
        currentTrackEditor.commit();
          	
    	mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
    	try {
    		mp.setDataSource(url);
    		mp.setOnCompletionListener(this);
        	mp.setOnPreparedListener(this);
        	mp.setOnErrorListener(this);
    		mp.prepare();
    		Log.v(DEBUG,"Preparing mp");
    		AudioPlayer.button_play.setImageResource(R.drawable.button_pause);
    	} catch(FileNotFoundException e){
    		Log.e(DEBUG, "FileNotFoundException: " + e.getMessage(), e);
    	} catch (IllegalArgumentException e) {
    		Log.e(DEBUG, "IllegalArgumentException: " + e.getMessage(), e);
    	} catch (IllegalStateException e) {
    		Log.e(DEBUG, "IllegalStateException in AudioPlayerService: " + e.getMessage(), e);
    	} catch(RuntimeException e){
    		Log.e(DEBUG, "RuntimeException: " + e.getMessage(), e);
    	} catch (IOException e) {
    		Log.e(DEBUG, "IOException: " + e.getMessage(), e);
    	} catch (Exception e) {
    		Log.e(DEBUG, "Exception: " + e.getMessage(), e);
    	}
    	
    }
    
    @Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e(DEBUG,"Error preparing mp: " + what + extra);
		return true;
	}
    
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.v(DEBUG, "Prepared mp");
        mp.start();
        AudioPlayer.button_play.setClickable(true);
        AudioPlayer.button_forward.setClickable(true);
        AudioPlayer.button_backward.setClickable(true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
    	AudioPlayer.button_play.setClickable(false);
    	AudioPlayer.button_forward.setClickable(false);
    	AudioPlayer.button_backward.setClickable(false);
    	AudioPlayer.button_play.setImageResource(R.drawable.button_play);
    	if ( mp.isPlaying() ){
    		mp.stop();
    	}
        mp.reset();
    	Log.v(DEBUG, "In onCompletion method");
    	
    	SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
    	int indexPosition = indexPositionPreference.getInt("indexPosition", 0);
    	
        ArrayList<HashMap<String, String>> trackList = getTrackListFromPreferences();
        if (! trackList.isEmpty() ){
        	if (trackList.get(trackList.size()-1) != trackList.get(indexPosition)){
        		trackList.remove(indexPosition);
        		PlaylistList trackListObject = new PlaylistList();
        		trackListObject.setTrackList(trackList);    
        		ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this, 
        			getString(R.string.trackPreferencesFile), MODE_PRIVATE);;
        		trackPreferences.putObject("tracks", trackListObject);
        		trackPreferences.commit();
        		if (! trackList.isEmpty() ){
        			playSong(indexPosition);
        		}
        	}
        			
        }
    }    
    
    @Override
    public IBinder onBind(Intent AudioPlayerServiceIntent) {
        Log.v(DEBUG, "In onBind with AudioPlayerServiceIntent: " + AudioPlayerServiceIntent.getAction());
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(DEBUG, "Destroying mp");
        if(mp != null) {
            mp.stop();
        }
        
    }
}