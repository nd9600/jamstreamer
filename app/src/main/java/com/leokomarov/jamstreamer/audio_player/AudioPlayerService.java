package com.leokomarov.jamstreamer.audio_player;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.playlist.PlaylistList;
import com.leokomarov.jamstreamer.utils.ComplexPreferences;
import com.leokomarov.jamstreamer.utils.generalUtils;
import com.leokomarov.jamstreamer.utils.tracklistUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class AudioPlayerService extends Service implements OnErrorListener, OnPreparedListener, OnCompletionListener {

    public static boolean shuffleBoolean;
    protected static boolean repeatBoolean;

    protected static MediaPlayer mediaPlayer;
    protected static boolean prepared;
    private static WifiLock wifiLock;

    protected static AudioManager audioManager;
    private static int lastKnownAudioFocusState;
    private static boolean wasPlayingWhenTransientLoss;
    private static int originalVolume;

    private static String artistAndAlbumStore = "";

    //used to stop playback when you detach headphones
    private static IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private static HeadsetIntentReceiver headsetReceiver = new HeadsetIntentReceiver();

    //used with notification and lockscreen controls
    private MediaSessionCompat mSession;
    private MediaControllerCompat mController;

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    private void handleIntent(Intent intent) {

        Log.v("handleIntent", intent.getAction());

        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
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

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {

        Log.v("generateAction", intentAction);

        Intent intent = new Intent(getApplicationContext(), AudioPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        NotificationCompat.Action.Builder builder = new NotificationCompat.Action.Builder(icon, title, pendingIntent);

        return builder.build();
    }

    private void buildNotification(String trackName, String artistAndAlbum, NotificationCompat.Action action) {

        Log.v("buildNotification", trackName);

        //this defines the activity that's opened when
        //the notification is pressed
        Intent clickIntent = new Intent(this, AudioPlayer.class);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        clickIntent.putExtra("fromNotification", true);
        PendingIntent pendingClickIntent = PendingIntent.getActivity(this, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.img_ic_launcher);
        builder.setContentTitle(trackName);
        builder.setContentText(artistAndAlbum);
        builder.setContentIntent(pendingClickIntent);
        builder.setOngoing(true);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        //for notification controls, ignored pre-4.1
        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle().setMediaSession(mSession.getSessionToken());

        Intent stopIntent = new Intent(getApplicationContext(), AudioPlayerService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pendingStopIntent = PendingIntent.getService(getApplicationContext(), 1, stopIntent, 0);

        //builder.setLargeIcon(AudioParser.albumImageStore);
        builder.setLargeIcon(((BitmapDrawable) AudioPlayer.albumArt.getDrawable()).getBitmap());
        builder.setDeleteIntent(pendingStopIntent);
        builder.setStyle(style);
        builder.addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
        builder.addAction(action);
        builder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));
        style.setShowActionsInCompactView(0, 1, 2);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(generalUtils.notificationID, builder.build());
    }

    private void initMediaSessions(final String trackName, final String artistAndAlbum) {
        mSession = new MediaSessionCompat(this, "jamstreamerMediaSession");
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        try {
            Log.v("initMediaSessions", "making new mController");
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
                 AudioPlayer.pauseOrPlay();
                 buildNotification(trackName, artistAndAlbum, generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
             }

             @Override
             public void onPause() {
                 super.onPause();

                 Log.v("MediaPlayerService", "onPause");
                 AudioPlayer.pauseOrPlay();
                 buildNotification(trackName, artistAndAlbum, generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
             }

             @Override
             public void onSkipToNext() {
                 super.onSkipToNext();

                 Log.v("MediaPlayerService", "onSkipToNext");
                 AudioPlayer.gotoNext();
                 buildNotification(trackName, artistAndAlbum, generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
             }

             @Override
             public void onSkipToPrevious() {
                 super.onSkipToPrevious();

                 Log.v("MediaPlayerService", "onSkipToPrevious");
                 AudioPlayer.gotoPrevious();
                 buildNotification(trackName, artistAndAlbum, generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
             }

             @Override
             public void onStop() {
                 super.onStop();

                 Log.v("MediaPlayerService", "onStop");
                 //Stop media player here
                 generalUtils.closeNotification(AudioPlayerService.this);
                 Intent audioPlayerServiceIntent = new Intent(getApplicationContext(), AudioPlayerService.class);
                 stopService(audioPlayerServiceIntent);
             }
        });
    }

    /*
    //called on instantiation
    @Override
    public void onCreate() {
        super.onCreate();
    }
    */

    //called every time the service is started with startService()
    @Override
    public int onStartCommand(Intent audioPlayerServiceIntent, int flags, int startId) {

        Log.v("onStartCommand", audioPlayerServiceIntent.getAction());

        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
        int indexPosition;

        ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
                getString(R.string.trackPreferences), MODE_PRIVATE);
        ArrayList<HashMap<String, String>> trackList;

        //restores the tracklist
        if (! AudioPlayerService.shuffleBoolean) {
            trackList = tracklistUtils.restoreTracklist(trackPreferences);
            indexPosition = indexPositionPreference.getInt("indexPosition", 0);
        } else {
            PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("shuffledTracks", PlaylistList.class);
            trackList = shuffledTrackPreferencesObject.trackList;
            indexPosition = indexPositionPreference.getInt("shuffledIndexPosition", 0);
        }

        //gets the trackIDs
        int newtrackID = Integer.parseInt(trackList.get(indexPosition).get("trackID"));
        SharedPreferences currentTrackPreference = getSharedPreferences(getString(R.string.currentTrackPreferences), 0);
        int currentTrackID = currentTrackPreference.getInt("currentTrack", 0);

        //if the player doesn't exist, or the tracks that was playing is
        //different from the new one, play the new track
        if ((mediaPlayer == null) || (newtrackID != currentTrackID)) {
            Log.v("service-onStartCommand", "Playing new song");

            if (mSession == null) {
                String trackName = trackList.get(indexPosition).get("trackName");
                String artistName = trackList.get(indexPosition).get("trackArtist");
                String albumName = trackList.get(indexPosition).get("trackAlbum");
                String artistAndAlbum = String.format("%s - %s", artistName, albumName);
                initMediaSessions(trackName, artistAndAlbum);
            }
            handleIntent(audioPlayerServiceIntent);
            playSong(trackList, indexPosition);
        }

        //if it does, and the track that was playing is the same as the current one,
        //set the player's views
        else {
            String trackName = trackList.get(indexPosition).get("trackName");
            String trackDuration = trackList.get(indexPosition).get("trackDuration");
            String artistName = trackList.get(indexPosition).get("trackArtist");
            String albumName = trackList.get(indexPosition).get("trackAlbum");
            AudioPlayer.songTitleLabel.setText(String.format("%s - %s", trackName, artistName));
            AudioPlayer.albumLabel.setText(albumName);
            AudioPlayer.songTotalDurationLabel.setText(trackDuration);
            AudioPlayer.albumArt.setImageBitmap(AudioParser.albumImageStore);
        }

        return super.onStartCommand(audioPlayerServiceIntent, flags, startId);
    }

    protected void playSong(ArrayList<HashMap<String, String>> trackList, int indexPosition) {
        if (trackList != null && trackList.size() != 0) {

            //create the player if it doesn't exist
            //if it does, reset it and close the notification
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                /*
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }
                */
                generalUtils.closeNotification(this);
                mediaPlayer.reset();
            }

            int newtrackID = Integer.parseInt(trackList.get(indexPosition).get("trackID"));

            //saves the current trackID in memory
            SharedPreferences currentTrackPreference = getSharedPreferences(getString(R.string.currentTrackPreferences), 0);
            SharedPreferences.Editor currentTrackEditor = currentTrackPreference.edit();
            currentTrackEditor.putInt("currentTrack", newtrackID);
            currentTrackEditor.apply();

            //gets the mp3 and info urls for this track
            //info url used for album art
            String unformattedURL = getResources().getString(R.string.trackByIDURL);
            String mp3url = String.format(unformattedURL, newtrackID).replace("&amp;", "&");
            Log.v("service-playSong", "mp3url = " + mp3url);
            String unformattedTrackInfoURL = getResources().getString(R.string.trackInformationURL);
            String trackInfoURL = String.format(unformattedTrackInfoURL, newtrackID).replace("&amp;", "&");

            //gets the track info from the tracklist
            String trackName = trackList.get(indexPosition).get("trackName");
            String artistName = trackList.get(indexPosition).get("trackArtist");
            String trackDuration = trackList.get(indexPosition).get("trackDuration");
            String albumName = trackList.get(indexPosition).get("trackAlbum");
            String artistAndAlbum = artistName + " - " + albumName;

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                //sets the initial text in the player
                AudioPlayer.songTitleLabel.setText(String.format("%s - %s", trackName, artistName));
                AudioPlayer.albumLabel.setText(albumName);
                AudioPlayer.songCurrentDurationLabel.setText("0:00");
                AudioPlayer.songTotalDurationLabel.setText(trackDuration);
                AudioPlayer.button_play.setImageResource(R.drawable.button_pause);

                //sets the album art if it has been stored
                if (AudioParser.albumImageStore != null) {
                    AudioPlayer.albumArt.setImageBitmap(AudioParser.albumImageStore);
                }
                BitmapDrawable albumImageDrawable = ((BitmapDrawable) AudioPlayer.albumArt.getDrawable());

                //if there isn't any album art, or the artist and album has changed
                //update it
                if (albumImageDrawable == null | !artistAndAlbumStore.equals(artistAndAlbum)) {
                    AudioParser audioParser = new AudioParser();
                    audioParser.execute(trackInfoURL, artistName + " - " + albumName);
                    artistAndAlbumStore = artistName + " - " + albumName;
                }

                //set wake and wifilocks and the player data source
                Log.v("service-playSong", "Preparing");
                prepared = false;
                mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "jamstreamerWifilock");
                wifiLock.acquire();
                mediaPlayer.setDataSource(mp3url);

                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnErrorListener(this);

                mediaPlayer.prepareAsync();

                /*
                //make the notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setSmallIcon(R.drawable.img_ic_launcher);
                builder.setContentTitle(trackName);
                builder.setContentText(artistName + " - " + albumName);
                builder.setOngoing(true);
                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                //this defines the activity that's opened when
                //the notification is pressed
                Intent notificationIntent = new Intent(this, AudioPlayer.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                notificationIntent.putExtra("fromNotification", true);
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(generalUtils.notificationID, builder.build());
                */
            } catch (NullPointerException e) {
                android.util.Log.e("AudioPlayerService", "NullPointerException: " + e.getMessage());
                //android.util.Log.e("AudioPlayerService", "trackList.get(i): " + trackList.get(indexPosition));
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

        Log.v("onCompletion", "onCompletion");

        if (mp.isPlaying()) {
            mp.stop();
        }

        if (repeatBoolean) {
            mp.seekTo(0);
        } else {
            prepared = false;
            audioManager.abandonAudioFocus(onAudioFocusChangeListener);
            unregisterReceiver(headsetReceiver);
            mSession.release();
            if (wifiLock.isHeld()) {
                wifiLock.release();
            }
            generalUtils.closeNotification(this);
            AudioPlayer.button_play.setImageResource(R.drawable.button_play);

            ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
                    getString(R.string.trackPreferences), MODE_PRIVATE);
            SharedPreferences indexPositionPreference = getSharedPreferences(getString(R.string.indexPositionPreferences), 0);
            SharedPreferences.Editor indexPositionEditor = indexPositionPreference.edit();

            if (!shuffleBoolean) {
                ArrayList<HashMap<String, String>> trackList = tracklistUtils.restoreTracklist(trackPreferences);
                int indexPosition = indexPositionPreference.getInt("indexPosition", -1);

                assert trackList != null;
                if (indexPosition + 1 <= trackList.size() - 1) {
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
            } else {
                PlaylistList shuffledTrackPreferencesObject = trackPreferences.getObject("shuffledTracks", PlaylistList.class);
                ArrayList<HashMap<String, String>> shuffledTracklist = shuffledTrackPreferencesObject.trackList;
                int shuffledIndexPosition = indexPositionPreference.getInt("shuffledIndexPosition", -1);

                if (shuffledIndexPosition + 1 <= shuffledTracklist.size() - 1) {
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
                generalUtils.closeNotification(this);
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
            AudioPlayer.pauseOrPlay();
        }
    }
}