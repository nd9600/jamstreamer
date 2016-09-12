package com.leokomarov.jamstreamer.albums;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Override
    public void callActionBar(int tickedCheckboxCounter) {
        Log.v("callActionBar", "tickedCheckboxCounter: " + tickedCheckboxCounter);
    }

    public void setPlaylistButtonClickable(boolean clickable) {
        button_playlist.setClickable(clickable);
    }
}
