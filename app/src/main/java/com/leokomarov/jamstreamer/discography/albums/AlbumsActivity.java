package com.leokomarov.jamstreamer.discography.albums;

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
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.ActionBarListActivity;
import com.leokomarov.jamstreamer.common.CustomListAdapter;
import com.leokomarov.jamstreamer.common.ListInteractor;
import com.leokomarov.jamstreamer.playlist.PlaylistActivity;

public class AlbumsActivity extends ActionBarListActivity implements CustomListAdapter.CallbackInterface {

	private ListView albumsLV;
	protected CustomListAdapter listAdapter;
    private AlbumsPresenter presenter;
	protected static ActionMode mActionMode;

	private ImageButton button_playlist;

    public void setPlaylistButtonClickable(boolean clickable){
        button_playlist.setClickable(clickable);
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getSupportActionBar();//.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.original_empty_list);

        presenter = new AlbumsPresenter(this, this, new ListInteractor());

		Intent intent = getIntent();
        presenter.populateList(intent);
	}

    public void setUpListview(){
        albumsLV = getListView();
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.albums_by_name_header, albumsLV, false);
        albumsLV.addHeaderView(header, null, false);

        listAdapter = new AlbumsAdapter(this, presenter);
        setListAdapter(listAdapter);
        registerForContextMenu(albumsLV);

        button_playlist = (ImageButton) findViewById(R.id.albums_by_name_btnPlaylist);
        button_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent button_playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
                startActivityForResult(button_playlistIntent, 1);
            }
        });

        albumsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                presenter.listviewOnClick(position);
            }
        });
    }
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        getMenuInflater().inflate(R.menu.albums_floating_menu , menu);
    }
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
        return presenter.onContextItemSelected(item);
    }

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
		@Override 
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.albums_contextual_menu, menu);
	        return true;
	    }
	    
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            String selectAllTitle = "Select all";
            if (! listAdapter.selectAll){ //if selectAll is false, we want the button to say "Select none"
                selectAllTitle = "Select none";
            }
            menu.findItem(R.id.albumsSelectAllTracks).setTitle(selectAllTitle);
            return true;
		}

		@Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        	int itemId = item.getItemId();
            int numberOfAlbums = albumsLV.getCount();
            
            if (itemId == R.id.tracksSelectAllTracks) {
                for (int i = 1; i < numberOfAlbums; i++) {
                    View view = albumsLV.getChildAt(i);
                    int indexPosition = i - 1;
                    listAdapter.tickCheckbox(indexPosition, listAdapter.selectAll);

                    if (view != null) {
                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.tracks_by_name_checkBox);

                        //if the checkbox isn't ticked, tick it
                        //or vice versa
                        if (checkbox.isChecked() == (! listAdapter.selectAll)){
                            checkbox.setChecked(listAdapter.selectAll);
                        }
                    }

                }
                listAdapter.selectAllPressed = true;
                listAdapter.selectAll = ! listAdapter.selectAll;
                callActionBar(listAdapter.tickedCheckboxCounter);
                return true;
            } else if (itemId == R.id.addAlbumToPlaylist) {
				setPlaylistButtonClickable(false);
                presenter.addAlbumToPlaylist(numberOfAlbums);

				mActionMode.finish();
				return true;
			} else {
				return false;
			}
        }

	    @Override
        public void onDestroyActionMode(ActionMode mode) {
	    	if (mActionMode != null){
	    		mActionMode = null;
	    		//mode = null;
	    	}
        }
	};

    //Called by the presenter to start new activities
    public void startNewActivity(Intent intent, int requestCode){
        listAdapter.clearCheckboxes();
        startActivityForResult(intent, requestCode);
    }
    
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Todo: change to customListAdapter
        //listAdapter.clearCheckboxes(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}