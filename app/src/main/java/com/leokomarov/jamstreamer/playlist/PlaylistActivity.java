package com.leokomarov.jamstreamer.playlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.ActionBarListActivity;
import com.leokomarov.jamstreamer.common.CustomListAdapter;
import com.leokomarov.jamstreamer.common.ListInteractor;
import com.leokomarov.jamstreamer.utils.ComplexPreferences;
import com.leokomarov.jamstreamer.utils.tracklistUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistActivity extends ActionBarListActivity implements CustomListAdapter.CallbackInterface {

    //playListLV is the overall listview for the activity
	private ListView playlistLV;

    //listAdapter links the LV with the data
	protected CustomListAdapter listAdapter;

    //presenter holds the logic
    private PlaylistPresenter presenter;

    //mActionMode is the action bar
	public static ActionMode mActionMode;

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
        presenter = new PlaylistPresenter(this, this, new ListInteractor());

        //Initialises the LV and sets the playlist data using the tracklist stored in memory
        //either from the savedInstanceState or trackPreferences
        playlistLV = getListView();
        presenter.setListData(restoreTracklistFromMemory, tracklist);

        //Creates the list adapter to link the LV and data
        listAdapter = new PlaylistAdapter(this, presenter);
        setListAdapter(listAdapter);

        //Registers that the floating menu will be opened on a long press
        registerForContextMenu(playlistLV);

        //Inflates the header and adds it to the LV
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.playlist_header, playlistLV, false);
        playlistLV.addHeaderView(header, null, false);

        //Initial checkbox setup - the action mode's title is initially "Select all"
        listAdapter.selectAllPressed = false;
        listAdapter.selectAll = true;
        listAdapter.clearCheckboxes();

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
    public void startNewActivity(Intent intent){
        listAdapter.clearCheckboxes();
        startActivityForResult(intent, 1);
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
	public void callActionBar(int tickedCheckboxCounter){
        if (tickedCheckboxCounter == 0) {
            if (mActionMode != null) {
                mActionMode.finish();
            }

            return;
        }

        if (getSupportActionBar() == null){
			mActionMode = startSupportActionMode(mActionModeCallback);
		} else {
            mActionMode.invalidate();
        }
        mActionMode.setTitle(tickedCheckboxCounter + " selected");
	}

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){

        //called on initial creation
		@Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.playlist_contextual_menu, menu);
	        return true;
	    }

        //called on initial creation and whenever the actionMode is invalidated
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            String selectAllTitle = "Select all";
            if (! listAdapter.selectAll){ //if selectAll is false, we want the button to say "Select none"
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

                //set the pressed boolean
                listAdapter.selectAllPressed = true;

              	for (int i = 1; i < numberOfTracks; i++) {
              		View view = playlistLV.getChildAt(i);
              		int indexPosition = i - 1;
                    listAdapter.tickCheckbox(indexPosition, listAdapter.selectAll);

              		if (view != null) {
                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.playlist_checkBox);

                        //if the checkbox isn't ticked, tick it
                        //or vice versa
                        if (checkbox.isChecked() == (! listAdapter.selectAll)){
                            checkbox.setChecked(listAdapter.selectAll);
                        }
              		}
              	}

                //since we want the button to change,
                //set selectAll to the opposite value
                listAdapter.selectAll = ! listAdapter.selectAll;

                //if all checkboxes have been unticked, close the action bar
                //else open the action bar and set the title to however many are unticked
              	callActionBar(listAdapter.tickedCheckboxCounter);
               	return true;

            //if the button to remove those specific tracks from the playlist is pressed
            } else if (itemId == R.id.removePlaylistItem) {

                //removes the ticked tracks from the tracklists
                //and from the LV's data
                //then the list adapter is told about the change
                int numberOfTracksDeleted = presenter.removeTracksFromPlaylist(numberOfTracks);
                listAdapter.notifyDataSetChanged();

                //make the toast telling the user how many tracks were removed
                String textInToast = "";
                if (numberOfTracksDeleted == 1){
                    textInToast = "1 track removed from the playlist";
                } else if(numberOfTracksDeleted >= 2){
                    textInToast = numberOfTracksDeleted + " tracks removed from the playlist";
                }
                Toast.makeText(getApplicationContext(), textInToast, Toast.LENGTH_LONG).show();

                //clear the checkboxes and close the action bar
                for (int i = 1; i < numberOfTracks; i++) {
                    View view = playlistLV.getChildAt(i);
                    listAdapter.tickCheckbox(i, false);
                    if (view != null) {
                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.playlist_checkBox);
                        checkbox.setChecked(false);
                    }
                }

				mode.finish();

				return true;

            //if the "delete entire playlist" button is pressed
			} else if (itemId == R.id.deletePlaylist) {
                //clear and save the tracklist and shuffled tracklist
                listAdapter.selectAllPressed = false;
				presenter.deletePlaylist();
                listAdapter.notifyDataSetChanged();

                //clear the checkboxes and close the action bar
                for (int i = 1; i < numberOfTracks; i++) {
                    View view = playlistLV.getChildAt(i);
                    if (view != null) {
                        //CheckBox checkbox = (CheckBox) view.findViewById(R.id.playlist_checkBox);
                        //checkbox.setChecked(false);
                        ((PlaylistAdapter.ViewHolder) view.getTag()).checkbox.setChecked(false);
                    }
                }
                listAdapter.clearCheckboxes();

				mode.finish();
				return true;
			} else {
				return false;
			}
	    }

        //called when the action mode is closed
	    @Override
        public void onDestroyActionMode(ActionMode mode) {
            listAdapter.selectAllPressed = false;
        }
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        listAdapter.clearCheckboxes();
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