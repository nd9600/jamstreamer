package com.leokomarov.jamstreamer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.leokomarov.jamstreamer.audio_player.AudioPlayerService;
import com.leokomarov.jamstreamer.discography.tracks.TracksActivity;
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;
import com.leokomarov.jamstreamer.searches.AlbumsSearch;
import com.leokomarov.jamstreamer.searches.ArtistsSearch;
import com.leokomarov.jamstreamer.searches.TracksSearch;
import com.leokomarov.jamstreamer.utils.GeneralUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainMenu extends AppCompatActivity {
    @BindView(R.id.mainMenu_artists) TextView textView_artists;
    @BindView(R.id.mainMenu_albums) TextView textView_albums;
    @BindView(R.id.mainMenu_tracks) TextView textView_tracks;
    @BindView(R.id.mainMenu_topTracksThisWeek) TextView textView_topTracksThisWeek;
    @BindView(R.id.mainMenu_btnPlaylist) ImageButton button_playlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        ButterKnife.bind(this);
        getSupportActionBar();

        //if the app hasn't been ran before, show the toasts
        //ie if sharedPreferences doesn't contain "firstrun" or "firstrun" == true
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!sharedPreferences.contains("firstrun")
                || sharedPreferences.getBoolean("firstrun", true)) {
            Toast.makeText(getApplicationContext(), "Long-press on an album or track for options", Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), "I'd appreciate it if you rate this app. Thanks!", Toast.LENGTH_LONG).show();
            SharedPreferences.Editor firstrunEditor = sharedPreferences.edit();
            firstrunEditor.putBoolean("firstrun", false);
            firstrunEditor.apply();
        }

        button_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
                startActivityForResult(playlistIntent, 1);
            }
        });

        //If the artists &c button is clicked
        //put the appropriate string in the hierarchy, and start the activity
        textView_artists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeneralUtils.putHierarchy(MainMenu.this, "artists");
                Intent artistIntent = new Intent(getApplicationContext(), ArtistsSearch.class);
                startActivity(artistIntent);
            }
        });

        textView_albums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeneralUtils.putHierarchy(MainMenu.this, "albums");
                Intent albumIntent = new Intent(getApplicationContext(), AlbumsSearch.class);
                startActivity(albumIntent);
            }
        });

        textView_tracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeneralUtils.putHierarchy(MainMenu.this, "tracks");
                Intent trackIntent = new Intent(getApplicationContext(), TracksSearch.class);
                startActivity(trackIntent);
            }
        });

        textView_topTracksThisWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeneralUtils.putHierarchy(MainMenu.this, "topTracksPerWeek");
                Intent topTracksWeekIntent = new Intent(getApplicationContext(), TracksActivity.class);
                startActivityForResult(topTracksWeekIntent, 2);
            }
        });
    }

    //Resets the checkboxes once you've left the playlist or top tracks activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Todo: possibly remove this
        //GeneralUtils.clearCheckboxes(null);
    }

    //Brings up the exit dialog when you press the back button
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stopService(new Intent(MainMenu.this, AudioPlayerService.class));
                        MainMenu.this.finish();
                        GeneralUtils.closeNotification(MainMenu.this);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    //When you press the app home button, bring up the exit dialog
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}