package com.leokomarov.jamstreamer.albums;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.ListInteractor;
import com.leokomarov.jamstreamer.controllers.base.ListController;
import com.leokomarov.jamstreamer.util.BundleBuilder;
import com.leokomarov.jamstreamer.util.GeneralUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class AlbumsController extends ListController {

    private String artistID;

    @BindView(R.id.results_list_header_text)
    TextView results_list_header_textview;

    @BindView(R.id.results_list_header_btn_playlist)
    ImageButton button_playlist;

    @BindView(R.id.main_recycler_view)
    RecyclerView recyclerView;

    @OnClick(R.id.results_list_header_btn_playlist)
    void playlistButtonClicked(){
        Log.v("playlistButtonClicked", "playlistButtonClicked");
        //Intent button_playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
        //startActivityForResult(button_playlistIntent, 1);
    }

    private AlbumsPresenter presenter;
    protected static ActionMode mActionMode;

    public AlbumsController(Bundle args) {
        super(args);
    }

    public AlbumsController(String artistID){
        this(new BundleBuilder(new Bundle())
                .putString("artistID", artistID)
                .build());
        this.artistID = artistID;
    }

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_list, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        results_list_header_textview.setText(getApplicationContext().getString(R.string.mainAlbums));
        presenter = new AlbumsPresenter(view.getContext(), this, new ListInteractor(), LayoutInflater.from(view.getContext()));

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(presenter.listAdapter);

        presenter.populateList(artistID);
    }

    @Override
    public void onRowClick(int position) {
        GeneralUtils.putHierarchy(getApplicationContext(), "albums");
        String albumID = presenter.albumList.get(position).get("albumID");
        Log.v("onRowClick", albumID);
        //getRouter().pushController(RouterTransaction.with(new AlbumsController(artistID)));
    }

    public void setPlaylistButtonClickable(boolean clickable) {
        button_playlist.setClickable(clickable);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        getMenuInflater().inflate(R.menu.albums_floating_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        return presenter.onContextItemSelected(item);
    }

    //Creates the contextual action bar
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
            inflater.inflate(R.menu.albums_contextual_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            String selectAllTitle = "Select all";
            if (!listAdapter.selectAll) { //if selectAll is false, we want the button to say "Select none"
                selectAllTitle = "Select none";
            }
            menu.findItem(R.id.albums_context_menu_SelectAllTracks).setTitle(selectAllTitle);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int itemId = item.getItemId();
            int numberOfAlbums = albumsLV.getCount();

            if (itemId == R.id.albums_context_menu_SelectAllTracks) {
                for (int i = 1; i < numberOfAlbums; i++) {
                    View view = albumsLV.getChildAt(i);
                    int indexPosition = i - 1;
                    listAdapter.tickCheckbox(indexPosition, listAdapter.selectAll);

                    if (view != null) {
                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.albums_checkbox);

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
            } else if (itemId == R.id.albums_context_menu_addAlbumToPlaylist) {
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
            if (mActionMode != null) {
                mActionMode = null;
                //mode = null;
            }
        }
    };
}
