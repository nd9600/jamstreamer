package com.leokomarov.jamstreamer.searches.artists;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leokomarov.jamstreamer.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final ArtistsController artistsController;

    private final  ArrayList<String[]> artistList;

    public ArtistsAdapter(ArtistsController artistsController, LayoutInflater inflater, ArrayList<String[]> artistList) {
        this.artistsController = artistsController;
        this.inflater = inflater;
        this.artistList = artistList;
    }

    @Override
    public ArtistsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.row_list_artists, parent, false));
    }

    @Override
    public void onBindViewHolder(ArtistsAdapter.ViewHolder holder, int position) {
        String[] artistArray = artistList.get(position);
        String artistID = artistArray[0];
        String artistName = artistArray[1];
        holder.bind(artistName, artistID);
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.artists_list_artists_names)
        TextView textView1;

        private String artistID;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(String artistName, String artistID) {
            textView1.setText(artistName);
            this.artistID = artistID;
        }

        @OnClick(R.id.row_root)
        void onRowClick() {
            artistsController.onRowClick(artistID);
        }

    }
}
