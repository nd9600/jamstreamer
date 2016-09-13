package com.leokomarov.jamstreamer.searches.albums;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bluelinelabs.conductor.RouterTransaction;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.controllers.ListController;
import com.leokomarov.jamstreamer.playlist.PlaylistController;
import com.leokomarov.jamstreamer.util.BundleBuilder;
import com.leokomarov.jamstreamer.util.GeneralUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class AlbumsController extends ListController {

    private String searchTerm;

    @BindView(R.id.results_list_header_text)
    TextView results_list_header_textview;

    @BindView(R.id.results_list_header_btn_playlist)
    ImageButton button_playlist;

    @BindView(R.id.main_recycler_view)
    RecyclerView recyclerView;

    @OnClick(R.id.results_list_header_btn_playlist)
    void playlistButtonClicked(){
        getRouter().pushController(RouterTransaction.with(new PlaylistController()));
    }

    protected static AlbumsPresenter presenter;
    protected static ActionMode mActionMode;

    public AlbumsController(Bundle args) {
        super(args);
    }

    public AlbumsController(String searchTerm){
        this(new BundleBuilder(new Bundle())
                .putString("searchTerm", searchTerm)
                .build());
        this.searchTerm = searchTerm;
    }

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_list, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        results_list_header_textview.setText(getApplicationContext().getString(R.string.mainAlbums));
        presenter = new AlbumsPresenter(view.getContext(), this, LayoutInflater.from(view.getContext()));

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(presenter.listAdapter);

        presenter.populateList(searchTerm);
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

    //Creates the contextual action bar
    public void callActionBar(int tickedCheckboxCounter) {
        Log.v("callActionBar", "tickedCheckboxCounter: " + tickedCheckboxCounter);
        if (tickedCheckboxCounter == 0) {
            if (mActionMode != null) {
                mActionMode.finish();
            }

            return;
        }

        if (getActionBar() == null) {
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
        } else {
            mActionMode.invalidate();
        }
        mActionMode.setTitle(tickedCheckboxCounter + " selected");
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.albums_contextual_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            String selectAllTitle = "Select all";
            if (! presenter.listAdapter.selectAll) { //if selectAll is false, we want the button to say "Select none"
                selectAllTitle = "Select none";
            }
            menu.findItem(R.id.albums_context_menu_SelectAllTracks).setTitle(selectAllTitle);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int itemId = item.getItemId();
            int numberOfAlbums = presenter.listAdapter.getItemCount();

            if (itemId == R.id.albums_context_menu_SelectAllTracks) {
                for (int i = 0; i < numberOfAlbums; i++) {
                    View view = recyclerView.getChildAt(i);

                    int indexPosition = i - 1;
                    presenter.listAdapter.tickCheckbox(indexPosition, presenter.listAdapter.selectAll);

                    if (view != null) {
                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.row_checkbox);

                        //if the checkbox isn't ticked, tick it
                        //or vice versa
                        if (checkbox.isChecked() == (! presenter.listAdapter.selectAll)) {
                            checkbox.setChecked(presenter.listAdapter.selectAll);
                        }
                    }

                }
                presenter.listAdapter.selectAll = ! presenter.listAdapter.selectAll;
                callActionBar(presenter.listAdapter.tickedCheckboxCounter);
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
            }
        }
    };
}
