package com.leokomarov.jamstreamer.searches.tracks;

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
import android.widget.Toast;

import com.bluelinelabs.conductor.RouterTransaction;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.controllers.ListController;
import com.leokomarov.jamstreamer.playlist.PlaylistController;
import com.leokomarov.jamstreamer.util.BundleBuilder;

import butterknife.BindView;
import butterknife.OnClick;

public class TracksController extends ListController {

    private String searchTerm;

    @BindView(R.id.results_list_header_text)
    TextView results_list_header_textview;

    @BindView(R.id.results_list_header_btn_playlist)
    ImageButton button_playlist;

    @OnClick(R.id.results_list_header_btn_playlist)
    void playlistButtonClicked(){
        getRouter().pushController(RouterTransaction.with(new PlaylistController()));
    }

    protected static TracksPresenter presenter;
    protected static ActionMode mActionMode;

    public TracksController(Bundle args) {
        super(args);
    }

    public TracksController(String searchTerm){
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

        results_list_header_textview.setText(getApplicationContext().getString(R.string.mainTracks));
        presenter = new TracksPresenter(view.getContext(), this, LayoutInflater.from(view.getContext()));

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(presenter.listAdapter);

        presenter.populateList(searchTerm);
    }

    @Override
    public void onRowClick(int position) {
        presenter.recyclerViewOnClick(position);
        //getRouter().pushController(RouterTransaction.with(new AlbumsController(artistID)));
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
            inflater.inflate(R.menu.tracks_contextual_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            String selectAllTitle = "Select all";
            if (! presenter.listAdapter.selectAll) { //if selectAll is false, we want the button to say "Select none"
                selectAllTitle = "Select none";
            }
            menu.findItem(R.id.tracks_context_menu_SelectAllTracks).setTitle(selectAllTitle);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int itemId = item.getItemId();

            if (itemId == R.id.tracks_context_menu_SelectAllTracks) {
                presenter.listAdapter.selectAllItems();
                return true;
            } else if (itemId == R.id.tracks_context_menu_addTrackToPlaylist) {
                int numberOfTracks = presenter.listAdapter.getItemCount();
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
            if (mActionMode != null) {
                mActionMode = null;
            }
        }
    };
}

