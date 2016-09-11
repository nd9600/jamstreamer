package com.leokomarov.jamstreamer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.controllers.ArtistsController;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final ArtistsController artistsController;

    private final ArrayList<HashMap<String, String>> artistList;

    public ArtistsAdapter(ArtistsController artistsController, LayoutInflater inflater, ArrayList<HashMap<String, String>> artistList) {
        this.artistsController = artistsController;
        this.inflater = inflater;
        this.artistList = artistList;
    }

    @Override
    public ArtistsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.artists_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ArtistsAdapter.ViewHolder holder, int position) {
        String artistName = artistList.get(position).get(artistsController.getApplicationContext().getString(R.string.TAG_ARTIST_NAME));
        String artistID = artistList.get(position).get(artistsController.getApplicationContext().getString(R.string.TAG_ARTIST_ID));
        holder.bind(artistName, artistID);
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.artists_list_artists_names)
        TextView textView1;

        @BindView(R.id.artists_list_artists_ids)
        TextView textView2;

        private String artistID;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(String artistName, String artistID) {
            textView1.setText(artistName);
            textView2.setVisibility(View.INVISIBLE);
            this.artistID = artistID;
        }

        @OnClick(R.id.row_root)
        void onRowClick() {
            artistsController.onRowClick(artistID);
        }

    }
}
