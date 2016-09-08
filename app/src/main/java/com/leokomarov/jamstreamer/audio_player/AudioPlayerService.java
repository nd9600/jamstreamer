package com.leokomarov.jamstreamer.audio_player;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.playlist.PlaylistList;
import com.leokomarov.jamstreamer.utils.ComplexPreferences;
import com.leokomarov.jamstreamer.utils.GeneralUtils;
import com.leokomarov.jamstreamer.utils.TracklistUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class AudioPlayerService extends Service implements OnErrorListener, OnPreparedListener, OnCompletionListener {
    protected static MediaPlayer mediaPlayer;
    protected static boolean prepared;
    private static WifiLock wifiLock;
    private static ComplexPreferences trackPreferences;
    private static AudioPlayerService context;
    private static ArrayList<HashMap<String, String>> tracklist;

    protected static AudioManager audioManager;
    private static int lastKnownAudioFocusState;
    private static boolean wasPlayingWhenTransientLoss;
    private static int originalVolume;

    private static String currentTrackName = "Track name";
    private static String previousArtistAndAlbum = "";
    private static String currentArtistAndAlbum = "Artist - album";
    private static String currentTrackInfoURL = "";

    public static boolean tracklistHasChanged;
    public static boolean shuffleBoolean;
    protected static boolean repeatBoolean;

    //used to stop playback when you detach headphones
    private static IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private static HeadsetIntentReceiver headsetReceiver = new HeadsetIntentReceiver();

    //used with notification and lockscreen controls
    private static MediaSessionCompat mSession;
    private static MediaControllerCompat mController;
    private static NotificationCompat.Builder builder;
    private static NotificationCompat.Action notificationAction;
    protected static Bitmap albumImage;

    public static final String ACTION_MAKE_NOTIFICATION = "action_make_notification";
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    //used to stop the song playing twice
    public static boolean playFirstSong;

    @Override
    public void onCreate(){
        super.onCreate();

        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        trackPreferences = ComplexPreferences.getComplexPreferences(this,
                getString(R.string.trackPreferences), MODE_PRIVATE);
        context = this;

        updateTracklist();
        tracklistHasChanged = false;
    }

    private static void updateTracklist(){
        if (!shuffleBoolean) {
            tracklist = TracklistUtils.restoreTracklist(trackPreferences);
        } else {
            PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("shuffledTracks", PlaylistList.class);
            tracklist = shuffledTrackPreferencesObject.tracklist;
        }
    }

    private static void updateAlbumArt(){
        //if there isn't any album art, or the ArtistAndAlbum has changed
        //update the album art and previousArtistAndAlbum
        if ((! previousArtistAndAlbum.equals(currentArtistAndAlbum)) || albumImage == null) {
            Log.v("updateAlbumArt", "getting new album art");
            Log.v("updateAlbumArt", "text different: " + (!previousArtistAndAlbum.equals(currentArtistAndAlbum)));
            Log.v("updateAlbumArt", "imageStore null: " + (albumImage == null));

            AudioParser audioParser = new AudioParser();
            audioParser.execute(currentTrackInfoURL, currentArtistAndAlbum);
            previousArtistAndAlbum = currentArtistAndAlbum;
        } else {
            notificationListener(null);
        }
    }

    private void initMediaSessions() {
        mSession = new MediaSessionCompat(this, "jamstreamerMediaSession");
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        try {
            mController = new MediaControllerCompat(getApplicationContext(), mSession.getSessionToken());
        } catch (RemoteException e) {
            Log.e("AudioPlayerService", "RemoteException: " + e.getMessage());
            e.printStackTrace();
        }

        mSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();

                Log.v("AudioPlayerService", "onPlay");
                playOrPause();
            }

            @Override
            public void onPause() {
                super.onPause();

                Log.v("AudioPlayerService", "onPause");
                playOrPause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();

                Log.v("AudioPlayerService", "onSkipToNext");
                gotoNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();

                Log.v("AudioPlayerService", "onSkipToPrevious");
                gotoPrevious();
            }

            @Override
            public void onStop() {
                super.onStop();

                Log.v("AudioPlayerService", "onStop");
                prepared = false;
                audioManager.abandonAudioFocus(onAudioFocusChangeListener);
                unregisterReceiver(headsetReceiver);
                mSession.release();
                if (wifiLock.isHeld()) {
                    wifiLock.release();
                }
                GeneralUtils.closeNotification(AudioPlayerService.this);
                AudioPlayer.setPlayButtonImage(true);
                Intent audioPlayerServiceIntent = new Intent(getApplicationContext(), AudioPlayerService.class);

                stopService(audioPlayerServiceIntent);
            }
        });
    }

    protected void handleIntent(Intent intent) {
        if (intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ACTION_MAKE_NOTIFICATION)){
            playInitialSong();
        }
        else if (action.equalsIgnoreCase(ACTION_PLAY)) {
            mController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            mController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mController.getTransportControls().skipToPrevious();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            mController.getTransportControls().skipToNext();
        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            mController.getTransportControls().stop();
        }
    }

    public static void notificationListener(Bitmap albumImage){

        if (albumImage != null){
            AudioPlayerService.albumImage = albumImage;
        }
        builder.setLargeIcon(AudioPlayerService.albumImage);

        //builder.setLargeIcon(((BitmapDrawable) AudioPlayer.albumArt.getDrawable()).getBitmap());
        builder.addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
        builder.addAction(notificationAction);
        builder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));
        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle().setMediaSession(mSession.getSessionToken());
        style.setShowActionsInCompactView(0, 1, 2);
        builder.setStyle(style);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(GeneralUtils.notificationID, builder.build());
    }

    private static NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(context, AudioPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(context, 1, intent, 0);
        NotificationCompat.Action.Builder builder = new NotificationCompat.Action.Builder(icon, title, pendingIntent);

        return builder.build();
    }


    //if pre 4.1
    // make simple notification
    // notify
    //if >4.1
    // make simple notification
    // get album art
    // wait
    // in listener called from AudioParser, make advanced notification
    // in listener, notify
    private static void buildNotification(boolean update, NotificationCompat.Action action) {
        //this defines the activity that's opened when
        //the notification is pressed
        Intent clickIntent = new Intent(context, AudioPlayer.class);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        clickIntent.putExtra("fromNotification", true);
        PendingIntent pendingClickIntent = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(context, AudioPlayerService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pendingStopIntent = PendingIntent.getService(context, 1, stopIntent, 0);

        builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.img_ic_launcher);
        builder.setContentTitle(currentTrackName);
        builder.setContentText(currentArtistAndAlbum);
        builder.setContentIntent(pendingClickIntent);
        builder.setDeleteIntent(pendingStopIntent);
        //builder.setOngoing(true);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (Build.VERSION.SDK_INT >= 16){
            //for notification controls, ignored pre-4.1
            //sets the album art if it's been stored in AudioParser
            notificationAction = action;
            if (update) {
                updateAlbumArt();
            }  else {
                notificationListener(null);
            }
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(GeneralUtils.notificationID, builder.build());
        }

    }

    //called every time the service is started with startService()
    @Override
    public int onStartCommand(Intent audioPlayerServiceIntent, int flags, int startId) {
        if (mSession == null) {
            initMediaSessions();
        }
        handleIntent(audioPlayerServiceIntent);

        return super.onStartCommand(audioPlayerServiceIntent, flags, startId);
    }

    private static void playInitialSong(){

        if (playFirstSong) {
            playFirstSong = false;

            SharedPreferences indexPositionPreference = context.getSharedPreferences(context.getString(R.string.indexPositionPreferences), 0);
            int indexPosition;

            //gets the indexPosition
            if (! shuffleBoolean) {
                indexPosition = indexPositionPreference.getInt("indexPosition", 0);
            } else {
                indexPosition = indexPositionPreference.getInt("shuffledIndexPosition", 0);
            }

            playSong(indexPosition);
        }

    }

    protected static void playSong(int indexPosition) {
        if (tracklistHasChanged) {
            Log.v("service-playSong", "tracklistHasChanged");
            updateTracklist();
            tracklistHasChanged = false;
        }

        //gets the trackIDs
        SharedPreferences currentTrackPreference = context.getSharedPreferences(context.getString(R.string.currentTrackPreferences), 0);
        int previousTrackID = currentTrackPreference.getInt("currentTrack", 0);
        int newTrackID = Integer.parseInt(tracklist.get(indexPosition).get("trackID"));

        //if the player doesn't exist, or the tracks that was playing is
        //different from the new one, play the new track
        if ((mediaPlayer != null) && (newTrackID == previousTrackID)) {
            //if the player exists, and the track that was playing is the same as the current one,
            //set the player's views

            Log.v("service-playSong", "setting player view");

            String trackName = tracklist.get(indexPosition).get("trackName");
            String trackDuration = tracklist.get(indexPosition).get("trackDuration");
            String artistName = tracklist.get(indexPosition).get("trackArtist");
            String albumName = tracklist.get(indexPosition).get("trackAlbum");
            String trackAndArtist = String.format("%s - %s", trackName, artistName);
            AudioPlayer.setMetadataAndAlbumArt(trackAndArtist, albumName, trackDuration);
            buildNotification(false, generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));

            return;
        }

        Log.v("service-playSong", "Playing new song");

        if (tracklist.size() != 0) {

            //create the player if it doesn't exist
            //if it does, reset it
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                mediaPlayer.reset();
            }

            //saves the current trackID in memory
            SharedPreferences.Editor currentTrackEditor = currentTrackPreference.edit();
            currentTrackEditor.putInt("currentTrack", newTrackID);
            currentTrackEditor.apply();

            //gets the mp3 and info urls for this track
            //info url used for album art
            String unformattedURL = context.getString(R.string.trackByIDURL);
            String mp3url = String.format(unformattedURL, newTrackID).replace("&amp;", "&");
            Log.v("service-playSong", "mp3url = " + mp3url);
            String unformattedTrackInfoURL = context.getString(R.string.trackInformationURL);
            currentTrackInfoURL = String.format(unformattedTrackInfoURL, newTrackID).replace("&amp;", "&");

            //gets the track info from the tracklist
            String trackName = tracklist.get(indexPosition).get("trackName");
            String artistName = tracklist.get(indexPosition).get("trackArtist");
            String trackDuration = tracklist.get(indexPosition).get("trackDuration");
            String albumName = tracklist.get(indexPosition).get("trackAlbum");
            String artistAndAlbum = String.format("%s - %s", artistName, albumName);
            String trackAndArtist = String.format("%s - %s", trackName, artistName);

            currentTrackName = trackName;
            currentArtistAndAlbum = artistAndAlbum;

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                //sets the initial text in the player;
                AudioPlayer.setMetadataAndAlbumArt(trackAndArtist, albumName, trackDuration);

                //set wake and wifilocks and the player data source
                Log.v("service-playSong", "Preparing");
                prepared = false;
                mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
                wifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "jamstreamerWifilock");
                wifiLock.acquire();
                mediaPlayer.setDataSource(mp3url);

                mediaPlayer.setOnCompletionListener(context);
                mediaPlayer.setOnPreparedListener(context);
                mediaPlayer.setOnErrorListener(context);

                mediaPlayer.prepareAsync();
                buildNotification(true, generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
            } catch (NullPointerException e) {
                android.util.Log.e("AudioPlayerService", "NullPointerException: " + e.getMessage());
            } catch (Exception e) {
                Log.e("AudioPlayerService", "Exception: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        android.util.Log.e("service-error", "Error: " + what + " " + extra);
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.v("service-onPrepared", "Prepared");
        prepared = true;

        int audioFocusResult = audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (mp != null && audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            Log.v("service-onPrepared", "gained audio focus");

            try {
                Log.v("service-onPrepared", "mp started");
                registerReceiver(headsetReceiver, intentFilter);
                mSession.setActive(true);
                mp.start();
            } catch (IllegalArgumentException e) {
                Log.e("AudioPlayerService", "IllegalArgumentException e: " + e.getMessage());
            }

            AudioPlayer.startProgressBar(mp.getDuration() / 1000);
        }

        AudioPlayer.setViewsClickable(true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        Log.v("onCompletion", "onCompletion");

        if (mp.isPlaying()) {
            mp.stop();
        }

        if (! repeatBoolean){
            prepared = false;
            audioManager.abandonAudioFocus(onAudioFocusChangeListener);
            unregisterReceiver(headsetReceiver);
            mSession.release();
            if (wifiLock.isHeld()) {
                wifiLock.release();
            }
            GeneralUtils.closeNotification(this);
            AudioPlayer.setPlayButtonImage(true);
        }
        gotoNext();
    }

    //pause audio if it's playing
    //if it isn't, and audio focus is granted, play
    //then update the progress bar
    public static void playOrPause(){
        if ( mediaPlayer.isPlaying() ) {
            mediaPlayer.pause();
            AudioPlayer.setPlayButtonImage(true);
            buildNotification(false, generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
        }
        else if(mediaPlayer != null) {
            int audioFocusResult = audioManager.requestAudioFocus(AudioPlayerService.onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mediaPlayer.start();
                AudioPlayer.setPlayButtonImage(false);
                buildNotification(false, generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
            }
            AudioPlayer.updateProgressBar();
        }
    }

    public static void gotoPrevious(){
        if(mediaPlayer.getCurrentPosition() >= 3000){
            mediaPlayer.seekTo(0);
        } else {
            SharedPreferences indexPositionPreference = context.getSharedPreferences(context.getString(R.string.indexPositionPreferences), 0);
            SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();

            //start the previous song
            String nameOfIndexPosition = (shuffleBoolean ? "shuffledIndexPosition" : "indexPosition");
            int indexPosition = indexPositionPreference.getInt(nameOfIndexPosition, 1);
            if (indexPosition != 0){
                indexPosition--;
                indexPositionEditor.putInt(nameOfIndexPosition, indexPosition);
                indexPositionEditor.apply();

                playSong(indexPosition);
            }
        }
    }

    public static void gotoNext(){
        //if on repeat, seek to the start
        if (repeatBoolean){
            mediaPlayer.seekTo(0);
        }
        else {
            //else get the shuffled/tracklist
            ArrayList<HashMap<String, String>> tracklist = (shuffleBoolean ? TracklistUtils.restoreTracklist(trackPreferences) : trackPreferences.getObject("shuffledTracks", PlaylistList.class).tracklist);

            //and shuffled/indexPosition
            SharedPreferences indexPositionPreference = context.getSharedPreferences(context.getString(R.string.indexPositionPreferences), 0);
            SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();

            String nameOfIndexPosition = (shuffleBoolean ? "shuffledIndexPosition" : "indexPosition");
            int indexPosition = indexPositionPreference.getInt(nameOfIndexPosition, -1);

            //if the next indexPosition is within the tracklist, play the next song
            if ((indexPosition + 1) <= (tracklist.size() - 1)){
                indexPosition++;
                indexPositionEditor.putInt(nameOfIndexPosition, indexPosition);
                indexPositionEditor.apply();
                playSong(indexPosition);
            }
        }

    }

    protected static OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

            Log.v("audioFocus", "onAudioFocusChange: " + focusChange);

            if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

                Log.v("audioFocus", "gained");

                if (lastKnownAudioFocusState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT && wasPlayingWhenTransientLoss) {
                    originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (prepared) {
                        try {
                            Log.v("audioFocus", "started mp");
                            mediaPlayer.start();
                        } catch (IllegalArgumentException e) {
                            Log.e("AudioPlayerService", "IllegalArgumentException: " + e.getMessage());
                        }
                    }
                } else if (lastKnownAudioFocusState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                Log.v("audioFocus", "lost");

                audioManager.abandonAudioFocus(onAudioFocusChangeListener);

                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    Log.v("audioFocus", "paused mp");

                    mediaPlayer.pause();
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {

                Log.v("audioFocus", "transient loss");

                wasPlayingWhenTransientLoss = mediaPlayer.isPlaying();
                if (mediaPlayer != null && wasPlayingWhenTransientLoss) {
                    Log.v("audioFocus", "paused mp");
                    mediaPlayer.pause();
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                //15 volume levels, 5 is a third of max
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);
            }
            lastKnownAudioFocusState = focusChange;
        }
    };

    @Override
    public IBinder onBind(Intent audioPlayerServiceIntent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                GeneralUtils.closeNotification(this);
            }
            mediaPlayer = null;
        }
        if (wifiLock.isHeld()) {
            wifiLock.release();
        }
    }
}

class HeadsetIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //stops playback when you detach headphones
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            AudioPlayerService.playOrPause();
        }
    }
}