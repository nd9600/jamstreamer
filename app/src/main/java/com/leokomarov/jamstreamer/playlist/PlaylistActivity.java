package com.leokomarov.jamstreamer.playlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.utils.ActionBarListActivity;
import com.leokomarov.jamstreamer.utils.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistActivity extends ActionBarListActivity implements PlaylistAdapter.CallbackInterface {

    //playListLV is the overall listview for the activity
	private ListView playlistLV;

    //playlistListAdapter links the LV with the data
	private ArrayAdapter<PlaylistTrackModel> playlistListAdapter;

    //presenter holds the logic
    private PlaylistPresenter presenter;
    private ArrayList<HashMap<String, String>> tracklist = new ArrayList<>();

    //Used with the action bar
	protected static ActionMode mActionMode;
	protected static boolean selectAll;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.original_empty_list);

        //Initialises the presenter and restore the tracklist from memory
        presenter = new PlaylistPresenter(this, this, savedInstanceState, new PlaylistInteractor());
        tracklist = presenter.restoreTracklist();

        //Initialises the LV and sets the playlist data using the tracklist stored in memory
        playlistLV = getListView();
        presenter.setPlaylistTrackData(null);

        //Creates the list adapter to link the LV and data
        playlistListAdapter = new PlaylistAdapter(this, this, presenter.getPlaylistTrackData());
        setListAdapter(playlistListAdapter);

        //Registers that the floating menu will be opened on a long press
        registerForContextMenu(playlistLV);

        //Inflates the header and adds it to the LV
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.playlist_header, playlistLV, false);
        playlistLV.addHeaderView(header, null, false);

        //Initial checkbox setup
        PlaylistPresenter.selectAllPressed = false;
        selectAll = false;
        utils.clearCheckboxes(2);

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
	//@Override
	public boolean onContextItemSelected(MenuItem item) {
        return presenter.onContextItemSelected(item);
    }

    //
    //Action bar
    //

    ///*
    //Creates the contextual action bar
	public void callActionBar(){
		if (mActionMode == null) {
            System.out.println("");
            System.out.println("####################");
            System.out.println("Started action bar");
            System.out.println("");
			//mActionMode = startActionMode(mActionModeCallback);
		}
	}

    /*
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){
		@Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            System.out.println("");
            System.out.println("####################");
            System.out.println("Created action mode");
            System.out.println("");
            MenuInflater inflater = getSupportMenuInflater();
            inflater.inflate(R.menu.playlist_contextual_menu, menu);

            System.out.println("");
            System.out.println("####################");
            System.out.println("Opened action bar");
            System.out.println("");

            if (! selectAll){
                System.out.println("");
                System.out.println("####################");
                System.out.println("Set button to \"Select all\"");
                System.out.println("");
                menu.findItem(R.id.playlistSelectAllTracks).setTitle("Select all");
            }
            else { //selectAll will always be true here
                System.out.println("");
                System.out.println("####################");
                System.out.println("Set button to \"Select none\"");
                System.out.println("");
                menu.findItem(R.id.playlistSelectAllTracks).setTitle("Select none");
            }
	        return true;
	    }

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return true;
		}

	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            System.out.println("");
            System.out.println("####################");
            System.out.println("Clicked button in action bar");
            System.out.println("");

            int itemId = item.getItemId();
            if (itemId == R.id.playlistSelectAllTracks) {
            	PlaylistPresenter.selectAllPressed = true;
            	selectAll = ! selectAll;
            	mActionMode.invalidate();

              	for (int i = 1; i < playlistLV.getCount(); i++) {
              		View view = playlistLV.getChildAt(i);
              		int indexPosition = i - 1;

              		if (view != null) {
              			CheckBox checkbox = (CheckBox) view.findViewById(R.id.playlist_checkBox);

              			if (selectAll && ! checkbox.isChecked()){
              				checkbox.setChecked(true);
              			}
              			else if (! selectAll && checkbox.isChecked()){
              				checkbox.setChecked(false);
              			}
              		}

              		if (selectAll && ! PlaylistAdapter.playlistCheckboxList.get(indexPosition, false) ){
              			PlaylistAdapter.playlistCheckboxList.put(indexPosition, true);
              			PlaylistAdapter.PlaylistCheckboxCount++;
					}
              		else if (! selectAll && PlaylistAdapter.playlistCheckboxList.get(indexPosition, false) ){
              			PlaylistAdapter.playlistCheckboxList.put(indexPosition, false);
              			PlaylistAdapter.PlaylistCheckboxCount--;
					}
              	}

              	if (PlaylistAdapter.PlaylistCheckboxCount == 0){
              		if (mActionMode != null){
              			mActionMode.finish();
              		}
                }
				else {
					callActionBar();
					mActionMode.setTitle(PlaylistAdapter.PlaylistCheckboxCount + " selected");
                }

               	return true;
            } else if (itemId == R.id.removePlaylistItem) {
                PlaylistPresenter.selectAllPressed = false;
            	int playlistLVLength = playlistLV.getCount();
				SparseBooleanArray checkboxList = PlaylistAdapter.playlistCheckboxList;
				ArrayList<Integer> tracksToDelete = new ArrayList<>();

				for (int i = 0; i < playlistLVLength; i++){
					if (checkboxList.get(i, false)) {
						tracksToDelete.add(i);
					}
				}
				Collections.sort(tracksToDelete, Collections.reverseOrder());
				for (int i : tracksToDelete){
				    tracklist.remove(i);
				}

                presenter.saveTracklist(tracklist);

				if (tracklist != null && ! tracklist.isEmpty()){
					presenter.shuffleTracklist();
				}
				presenter.clearPlaylistTrackModel();
				presenter.setPlaylistTrackData(tracklist);

                String textInToast = "";
				if (tracksToDelete.size() == 1){
					textInToast = "1 track removed from the playlist";
				} else if(tracksToDelete.size() >= 2){
					textInToast = tracksToDelete.size() + " tracks removed from the playlist";
				}
                Toast.makeText(getApplicationContext(),textInToast, Toast.LENGTH_LONG).show();

				playlistListAdapter.notifyDataSetChanged();
				PlaylistAdapter.playlistCheckboxList.clear();
			   	PlaylistAdapter.PlaylistCheckboxCount = 0;
				mActionMode.finish();
				mActionMode = null;
				mode.finish();
				//mode = null;
				return true;
			} else if (itemId == R.id.deletePlaylist) {
                PlaylistPresenter.selectAllPressed = false;
				tracklist.clear();
				presenter.saveTracklist(tracklist);

				if (tracklist != null && ! tracklist.isEmpty()){
					presenter.shuffleTracklist();
				}
				presenter.clearPlaylistTrackModel();

				utils.clearCheckboxes(2);
				playlistListAdapter.notifyDataSetChanged();
				mActionMode.finish();
				mActionMode = null;
				mode.finish();
				//mode = null;
				return true;
			} else {
				return false;
			}
	    }

	    @Override
        public void onDestroyActionMode(ActionMode mode) {
            PlaylistPresenter.selectAllPressed = false;
	    	if (mActionMode != null){
	    		mActionMode = null;
	    		//mode = null;
	    	}
        }
	};
	*/
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        utils.clearCheckboxes(requestCode);
	}
	
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	tracklist = presenter.restoreTracklist();
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