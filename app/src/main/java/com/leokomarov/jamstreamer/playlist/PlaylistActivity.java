package com.leokomarov.jamstreamer.playlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.ActionBarListActivity;
import com.leokomarov.jamstreamer.common.TrackModel;
import com.leokomarov.jamstreamer.utils.ComplexPreferences;
import com.leokomarov.jamstreamer.utils.generalUtils;
import com.leokomarov.jamstreamer.utils.tracklistUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistActivity extends ActionBarListActivity implements PlaylistAdapter.CallbackInterface {

    //playListLV is the overall listview for the activity
	private ListView playlistLV;

    //playlistListAdapter links the LV with the data
	private ArrayAdapter<TrackModel> playlistListAdapter;

    //presenter holds the logic
    private PlaylistPresenter presenter;

    //mActionMode is the action bar
    //selectAll is changed when the selectAll/none button is pressed
	public static ActionMode mActionMode;
	protected static boolean selectAll;

    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean restoreTracklistFromMemory = true;

        ArrayList<HashMap<String, String>> tracklist = new ArrayList<>();
        if (savedInstanceState != null){
            tracklist = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable(getString(R.string.TAG_TRACKLIST));
            restoreTracklistFromMemory = false;
        }

        setContentView(R.layout.original_empty_list);

        //Initialises the presenter
        presenter = new PlaylistPresenter(this, this, new PlaylistInteractor());

        //Initialises the LV and sets the playlist data using the tracklist stored in memory
        //either from the savedInstanceState or trackPreferences
        playlistLV = getListView();
        presenter.setPlaylistTrackData(restoreTracklistFromMemory, tracklist);

        //Creates the list adapter to link the LV and data
        playlistListAdapter = new PlaylistAdapter(this, this, presenter);
        setListAdapter(playlistListAdapter);

        //Registers that the floating menu will be opened on a long press
        registerForContextMenu(playlistLV);

        //Inflates the header and adds it to the LV
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.playlist_header, playlistLV, false);
        playlistLV.addHeaderView(header, null, false);

        //Initial checkbox setup - the action mode's title is initially "Select all"
        PlaylistPresenter.selectAllPressed = false;
        selectAll = true;
        generalUtils.clearCheckboxes(2);

        //Creates the click listeners for the tracks
    	playlistLV.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int indexPosition = position - 1;
                presenter.startAudioPlayer(indexPosition);
                finish();
            }
        });
    }

    //Called by the presenter to start new activities
    public void startNewActivity(Intent intent, int requestCode){
        startActivityForResult(intent, requestCode);
    }

    //
    //Floating menu
    //

    //Inflates the floating menu when the it's created,
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        getMenuInflater().inflate(R.menu.playlist_floating_menu , menu);
    }

    //Called the presenter's function when the tracks are long-pressed
	@Override
	public boolean onContextItemSelected(MenuItem item) {
        return presenter.onContextItemSelected(item);
    }

    //
    //Action bar
    //

    ///*
    //Creates the contextual action bar
	public void callActionBar(){
        Log.v("callActionBar", "");
        if (getSupportActionBar() == null){
            Log.v("callActionBar", "Started");
			mActionMode = startSupportActionMode(mActionModeCallback);
		} else {
            Log.v("callActionBar", "Invalidated");
            //getSupportActionBar().show();
            mActionMode.invalidate();
        }
	}

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){

        //called on initial creation
		@Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.v("onCreateActionMode", "");
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.playlist_contextual_menu, menu);
	        return true;
	    }

        //called on initial creation and whenever the actionMode is invalidated
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            Log.v("onPrepareActionMode", "");

            String selectAllTitle = "Select all";
            if (! selectAll){ //if selectAll is true, we want the button to say "Select none"
                selectAllTitle = "Select none";
            }
            menu.findItem(R.id.playlistSelectAllTracks).setTitle(selectAllTitle);
			return true;
		}

        //called when a button is clicked
	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            int itemId = item.getItemId();
            int numberOfTracks = playlistLV.getCount();

            //if the selectAll button is pressed
            if (itemId == R.id.playlistSelectAllTracks) {

              	for (int i = 1; i < numberOfTracks; i++) {
              		View view = playlistLV.getChildAt(i);
              		int indexPosition = i - 1;
                    PlaylistAdapter.tickCheckbox(indexPosition, selectAll);

              		if (view != null) {
                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.playlist_checkBox);

                        //if the select all button is pressed
                        //and the checkbox isn't ticked, tick it
                        if (selectAll && checkbox.isChecked() == (! selectAll)){
                            checkbox.setChecked(selectAll);
                        }
              		}
              	}

                //set the pressed boolean
                PlaylistPresenter.selectAllPressed = true;

                //since we want the button to change,
                //set selectAll to the opposite value
                selectAll = ! selectAll;

                //if all checkboxes have been unticked, close the action bar
                //else open the action bar and set the title to however many are unticked
              	if (PlaylistAdapter.tickedCheckboxCounter == 0){
              		mode.finish();
                }
				else {
					//callActionBar();
					mActionMode.setTitle(PlaylistAdapter.tickedCheckboxCounter + " selected");
                }
                playlistListAdapter.notifyDataSetChanged();
               	return true;

            //if the button to remove those specific tracks from the playlist is pressed
            } else if (itemId == R.id.removePlaylistItem) {
                Log.v("onActionItemClicked", "removing from the playlist");

                //this method removes the ticked tracks from the tracklists
                //and from the LV's data
                //then the list adapter is told about the change
                int numberOfTracksDeleted = presenter.removeTracksFromPlaylist(numberOfTracks);
                playlistListAdapter.notifyDataSetChanged();

                //make the toast telling the user how many tracks were removed
                String textInToast = "";
                if (numberOfTracksDeleted == 1){
                    textInToast = "1 track removed from the playlist";
                } else if(numberOfTracksDeleted >= 2){
                    textInToast = numberOfTracksDeleted + " tracks removed from the playlist";
                }
                Log.v("onActionItemClicked", numberOfTracksDeleted + " tracks removed from the playlist");
                Toast.makeText(getApplicationContext(), textInToast, Toast.LENGTH_LONG).show();

                //clear the checkboxes and close the action bar
                for (int i = 1; i < numberOfTracks; i++) {
                    View view = playlistLV.getChildAt(i);
                    PlaylistAdapter.tickCheckbox(i, false);
                    if (view != null) {
                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.playlist_checkBox);
                        checkbox.setChecked(false);
                        Log.v("onActionItemClicked", "unticking checkbox #" + i);
                    }
                }

				mode.finish();

				return true;

            //if the "delete entire playlist" button is pressed
			} else if (itemId == R.id.deletePlaylist) {
                //clear and save the tracklist and shuffled tracklist
                PlaylistPresenter.selectAllPressed = false;
				presenter.deletePlaylist();
                playlistListAdapter.notifyDataSetChanged();

                //clear the checkboxes and close the action bar
                for (int i = 1; i < numberOfTracks; i++) {
                    View view = playlistLV.getChildAt(i);
                    if (view != null) {
                        //CheckBox checkbox = (CheckBox) view.findViewById(R.id.playlist_checkBox);
                        //checkbox.setChecked(false);
                        ((PlaylistAdapter.ViewHolder) view.getTag()).checkbox.setChecked(false);
                    }
                }
				generalUtils.clearCheckboxes(2);

				mode.finish();
				return true;
			} else {
				return false;
			}
	    }

        //called when the action mode is closed
	    @Override
        public void onDestroyActionMode(ActionMode mode) {
            System.out.println(" ");
            System.out.println("onDestroyActionMode");
            PlaylistPresenter.selectAllPressed = false;
            /*
	    	if (mActionMode != null){
	    		mActionMode = null;
	    	}
	    	*/
        }
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        generalUtils.clearCheckboxes(requestCode);
	}
	
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
        ComplexPreferences trackPreferences = ComplexPreferences.getComplexPreferences(this,
                getString(R.string.trackPreferences), Context.MODE_PRIVATE);
        ArrayList<HashMap<String, String>> tracklist = tracklistUtils.restoreTracklist(trackPreferences);
        savedInstanceState.putSerializable(getString(R.string.TAG_TRACKLIST), tracklist);
    }
    
	public boolean onOptionsItemSelected(MenuItem item) {
	        int itemId = item.getItemId();
			if (itemId == android.R.id.home) {
				onBackPressed();
				return true;
			}
	    return super.onOptionsItemSelected(item);
	}

}