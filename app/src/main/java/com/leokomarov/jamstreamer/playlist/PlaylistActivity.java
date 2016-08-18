package com.leokomarov.jamstreamer.playlist;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.utils.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PlaylistActivity extends SherlockListActivity implements PlaylistAdapter.CallbackInterface {
	private ListView playlistLV;
	private ArrayAdapter<PlaylistTrackModel> playlistListAdapter;

    private PlaylistPresenter presenter;
    private ArrayList<HashMap<String, String>> trackList = new ArrayList<>();

    //Used with the action bar
	protected static ActionMode mActionMode;
	protected static boolean selectAll;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);     
        setContentView(R.layout.original_empty_list);

        presenter = new PlaylistPresenter(this, this, savedInstanceState, new PlaylistInteractor());

        playlistLV = getListView();
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.playlist_header, playlistLV, false);
        playlistLV.addHeaderView(header, null, false);

        PlaylistPresenter.selectAllPressed = false;
        selectAll = false;
        
        utils.clearCheckboxes(2);

        presenter.setPlaylistTrackModel(null);
        playlistListAdapter = new PlaylistAdapter(this, this, presenter.getPlaylistTrackModel());
    	setListAdapter(playlistListAdapter);
    	registerForContextMenu(playlistLV);
    	
    	playlistLV.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int indexPosition = position - 1;
                presenter.startAudioPlayer(indexPosition);
                finish();
            }
        });

        findViewById(R.id.playlist_checkBox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaylistPresenter.selectAllPressed = false;
            }
        });
    }

    public void startNewActivity(Intent intent, int requestCode){
        startActivityForResult(intent, requestCode);
    }

    //
    //Context menu
    //

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        getMenuInflater().inflate(R.menu.playlist_floating_menu , menu);
    }

	//@Override
	public boolean onContextItemSelected(MenuItem item) {
        return presenter.onContextItemSelected(item);
    }

    //
    //Action bar
    //

    ///*
	public void callActionBar(){
		if (mActionMode == null) {
			mActionMode = startActionMode(mActionModeCallback);
            System.out.println("");
            System.out.println("####################");
            System.out.println("Started action bar");
            System.out.println("");
		}
	}

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){
		@Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            System.out.println("");
            System.out.println("####################");
            System.out.println("Created action mode");
            System.out.println("");
	        return true;
	    }

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			menu.clear();
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

              		if (selectAll && ! PlaylistAdapter.PlaylistCheckboxList.get(indexPosition, false) ){
              			PlaylistAdapter.PlaylistCheckboxList.put(indexPosition, true);
              			PlaylistAdapter.PlaylistCheckboxCount++;
					}
              		else if (! selectAll && PlaylistAdapter.PlaylistCheckboxList.get(indexPosition, false) ){
              			PlaylistAdapter.PlaylistCheckboxList.put(indexPosition, false);
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
				SparseBooleanArray checkboxList = PlaylistAdapter.PlaylistCheckboxList;
				ArrayList<Integer> tracksToDelete = new ArrayList<>();

				for (int i = 0; i < playlistLVLength; i++){
					if (checkboxList.get(i, false)) {
						tracksToDelete.add(i);
					}
				}
				Collections.sort(tracksToDelete, Collections.reverseOrder());
				for (int i : tracksToDelete){
				    trackList.remove(i);
				}

                presenter.saveTracklist(trackList);

				if (trackList != null && ! trackList.isEmpty()){
					presenter.shuffleTracklist();
				}
				presenter.clearPlaylistTrackModel();
				presenter.setPlaylistTrackModel(trackList);

                String textInToast = "";
				if (tracksToDelete.size() == 1){
					textInToast = "1 track removed from the playlist";
				} else if(tracksToDelete.size() >= 2){
					textInToast = tracksToDelete.size() + " tracks removed from the playlist";
				}
                Toast.makeText(getApplicationContext(),textInToast, Toast.LENGTH_LONG).show();

				playlistListAdapter.notifyDataSetChanged();
				PlaylistAdapter.PlaylistCheckboxList.clear();
			   	PlaylistAdapter.PlaylistCheckboxCount = 0;
				mActionMode.finish();
				mActionMode = null;
				mode.finish();
				//mode = null;
				return true;
			} else if (itemId == R.id.deletePlaylist) {
                PlaylistPresenter.selectAllPressed = false;
				trackList.clear();
				presenter.saveTracklist(trackList);

				if (trackList != null && ! trackList.isEmpty()){
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        utils.clearCheckboxes(requestCode);
	}
	
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	trackList = presenter.restoreTracklist();
    	savedInstanceState.putSerializable(getString(R.string.TAG_TRACKLIST), trackList);
    }
    
	public boolean onOptionsItemSelected(MenuItem item) { 
	        int itemId = item.getItemId();
			if (itemId == android.R.id.home) {
				onBackPressed();
				return true;
			}
	    return super.onOptionsItemSelected((android.view.MenuItem) item);
	}

}