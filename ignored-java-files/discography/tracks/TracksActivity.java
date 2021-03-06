package com.leokomarov.jamstreamer.ignored.discography.tracks;

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
import android.widget.Toast;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.ActionBarListActivity;
import com.leokomarov.jamstreamer.common.CustomListAdapter;
import com.leokomarov.jamstreamer.common.ListInteractor;
import com.leokomarov.jamstreamer.ignored.playlist.PlaylistActivity;

public class TracksActivity extends ActionBarListActivity implements CustomListAdapter.CallbackInterface {

    private ListView tracksLV;
    protected CustomListAdapter listAdapter;
    private TracksPresenter presenter;
    protected static ActionMode mActionMode;

    private ImageButton button_playlist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.original_empty_list);

        presenter = new TracksPresenter(this, this, new ListInteractor());

        Intent intent = getIntent();
        presenter.populateList(intent);
    }

    public void setUpListview() {
        tracksLV = getListView();
        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.tracks_list_header, tracksLV, false);
        tracksLV.addHeaderView(header, null, false);

        listAdapter = new TracksAdapter(this, presenter);
        listAdapter.selectAllPressed = false;
        listAdapter.selectAll = true;
        listAdapter.clearCheckboxes();

        setListAdapter(listAdapter);
        registerForContextMenu(tracksLV);

        tracksLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                presenter.listviewOnClick(position);
            }
        });

        button_playlist = (ImageButton) findViewById(R.id.tracks_by_name_btnPlaylist);
        button_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent button_playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
                startNewActivity(button_playlistIntent, 1);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        getMenuInflater().inflate(R.menu.tracks_floating_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return presenter.onContextItemSelected(item);
    }

    public void callActionBar(int tickedCheckboxCounter) {
        if (tickedCheckboxCounter == 0) {
            if (mActionMode != null) {
                mActionMode.finish();
            }
            return;
        }

        if (getSupportActionBar() == null) {
            mActionMode = startSupportActionMode(mActionModeCallback);
        } else {
            mActionMode.invalidate();
        }
        mActionMode.setTitle(tickedCheckboxCounter + " selected");
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.tracks_contextual_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            String selectAllTitle = "Select all";
            if (!listAdapter.selectAll) { //if selectAll is false, we want the button to say "Select none"
                selectAllTitle = "Select none";
            }
            menu.findItem(R.id.tracks_context_menu_SelectAllTracks).setTitle(selectAllTitle);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int itemId = item.getItemId();
            int numberOfTracks = tracksLV.getCount();

            if (itemId == R.id.tracks_context_menu_SelectAllTracks) {
                for (int i = 1; i < numberOfTracks; i++) {
                    View view = tracksLV.getChildAt(i);
                    int indexPosition = i - 1;
                    listAdapter.tickCheckbox(indexPosition, listAdapter.selectAll);

                    if (view != null) {
                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.tracks_by_name_checkBox);

                        //if the checkbox isn't ticked, tick it
                        //or vice versa
                        if (checkbox.isChecked() == (!listAdapter.selectAll)) {
                            checkbox.setChecked(listAdapter.selectAll);
                        }
                    }

                }
                listAdapter.selectAllPressed = true;
                listAdapter.selectAll = !listAdapter.selectAll;
                callActionBar(listAdapter.tickedCheckboxCounter);
                return true;
            } else if (itemId == R.id.tracks_context_menu_addTrackToPlaylist) {
                int numberOfTracksAdded = presenter.addTrackToPlaylist(numberOfTracks);

                if (numberOfTracksAdded == 1) {
                    Toast.makeText(getApplicationContext(), "1 track added to the playlist", Toast.LENGTH_LONG).show();
                } else if (numberOfTracksAdded >= 2) {
                    Toast.makeText(getApplicationContext(), numberOfTracksAdded + " tracks added to the playlist", Toast.LENGTH_LONG).show();
                }
                mActionMode.finish();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            listAdapter.selectAllPressed = false;
        }
    };

    //Called by the presenter to start new activities
    public void startNewActivity(Intent intent, int requestCode) {
        listAdapter.clearCheckboxes();
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        listAdapter.clearCheckboxes();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
