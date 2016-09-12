package com.leokomarov.jamstreamer.playlist;

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

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.controllers.base.ListController;

import butterknife.BindView;

public class PlaylistController extends ListController {

    @BindView(R.id.results_list_header_text)
    TextView results_list_header_textview;

    @BindView(R.id.results_list_header_btn_playlist)
    ImageButton playlistButton;

    @BindView(R.id.main_recycler_view)
    RecyclerView recyclerView;

    protected static PlaylistPresenter presenter;
    public static ActionMode mActionMode;

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_list, container, false);
    }

    @Override
    public void onRowClick(int position) {
        Log.v("playlist", "clicked: " + position);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        results_list_header_textview.setText(getApplicationContext().getString(R.string.playlist));
        playlistButton.setVisibility(View.INVISIBLE);
        presenter = new PlaylistPresenter(this, LayoutInflater.from(view.getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(presenter.listAdapter);
    }

    //
    //Action bar
    //

    //Creates the contextual action bar
    public void callActionBar(int tickedCheckboxCounter){
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

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){

        //called on initial creation
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.playlist_contextual_menu, menu);
            return true;
        }

        //called on initial creation and whenever the actionMode is invalidated
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            String selectAllTitle = "Select all";
            if (! presenter.listAdapter.selectAll){ //if selectAll is false, we want the button to say "Select none"
                selectAllTitle = "Select none";
            }
            menu.findItem(R.id.playlist_context_menu_SelectAllTracks).setTitle(selectAllTitle);
            return true;
        }

        //called when a button is clicked
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int itemId = item.getItemId();
            int numberOfTracks = presenter.listAdapter.getItemCount();

            //if the selectAll button is pressed
            if (itemId == R.id.playlist_context_menu_SelectAllTracks) {

                //set the pressed boolean
                presenter.listAdapter.selectAllPressed = true;

                for (int i = 0; i < numberOfTracks; i++) {
                    View view = recyclerView.getChildAt(i);
                    int indexPosition = i - 1;
                    presenter.listAdapter.tickCheckbox(indexPosition, presenter.listAdapter.selectAll);

                    if (view != null) {
                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.row_checkbox);

                        //if the checkbox isn't ticked, tick it
                        //or vice versa
                        if (checkbox.isChecked() == (! presenter.listAdapter.selectAll)){
                            checkbox.setChecked(presenter.listAdapter.selectAll);
                        }
                    }
                }

                //since we want the button to change,
                //set selectAll to the opposite value
                presenter.listAdapter.selectAll = ! presenter.listAdapter.selectAll;

                //if all checkboxes have been unticked, close the action bar
                //else open the action bar and set the title to however many are unticked
                callActionBar(presenter.listAdapter.tickedCheckboxCounter);
                return true;

                //if the button to remove those specific tracks from the playlist is pressed
            } else if (itemId == R.id.playlist_context_menu_removePlaylistItem) {

                //removes the ticked tracks from the tracklists
                //and from the LV's data
                //then the list adapter is told about the change
                //and updates its map of hashcodes - indexPositions
                int numberOfTracksDeleted = presenter.removeTracksFromPlaylist(numberOfTracks);
                presenter.listAdapter.notifyDataSetChanged();

                //make the toast telling the user how many tracks were removed
                String textInToast = "";
                if (numberOfTracksDeleted == 1){
                    textInToast = "1 track removed from the playlist";
                } else if(numberOfTracksDeleted >= 2){
                    textInToast = numberOfTracksDeleted + " tracks removed from the playlist";
                }
                Toast.makeText(getApplicationContext(), textInToast, Toast.LENGTH_LONG).show();

                //clear the checkboxes and close the action bar
                for (int i = 0; i < numberOfTracks; i++) {
                    View view = recyclerView.getChildAt(i);
                    presenter.listAdapter.tickCheckbox(i, false);
                    if (view != null) {
                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.row_checkbox);
                        checkbox.setChecked(false);
                    }
                }
                presenter.listAdapter.clearCheckboxes();

                mode.finish();
                return true;

                //if the "delete entire playlist" button is pressed
            } else if (itemId == R.id.playlist_context_menu_deletePlaylist) {
                //clear and save the tracklist and shuffled tracklist
                presenter.listAdapter.selectAllPressed = false;
                presenter.deletePlaylist();
                presenter.listAdapter.notifyDataSetChanged();

                //clear the checkboxes and close the action bar
                for (int i = 0; i < numberOfTracks; i++) {
                    View view = recyclerView.getChildAt(i);
                    if (view != null) {
                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.row_checkbox);
                        checkbox.setChecked(false);
                    }
                }
                presenter.listAdapter.clearCheckboxes();
                Toast.makeText(getApplicationContext(), "Playlist deleted", Toast.LENGTH_LONG).show();

                mode.finish();
                return true;
            } else {
                return false;
            }
        }

        //called when the action mode is closed
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            presenter.listAdapter.selectAllPressed = false;
        }
    };
}
