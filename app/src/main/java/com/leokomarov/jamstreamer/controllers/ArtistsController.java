package com.leokomarov.jamstreamer.controllers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.adapters.ArtistsAdapter;
import com.leokomarov.jamstreamer.common.ActionBarListActivity;
import com.leokomarov.jamstreamer.util.BundleBuilder;
import com.leokomarov.jamstreamer.util.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class ArtistsController extends ActionBarListActivity implements JSONParser.CallbackInterface {

    String artistName;
    JSONArray results = null;

    RecyclerView.Adapter adapter;
    ArrayList<String[]> artistList;

    @BindView(R.id.main_recycler_view)
    RecyclerView recyclerView;

    public ArtistsController(Bundle args) {
        super(args);
    }

    public ArtistsController(String artistName){
        this(new BundleBuilder(new Bundle())
                .putString("artist_name", artistName)
                .build());
        this.artistName = artistName;
    }

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_artists, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        artistList = new ArrayList<>();
        adapter = new ArtistsAdapter(this, LayoutInflater.from(view.getContext()), artistList);
        recyclerView.setAdapter(adapter);

        String unformattedURL = getResources().getString(R.string.artistsByNameJSONURL);
        String url = String.format(unformattedURL, artistName).replace("&amp;", "&").replace(" ", "+");

        JSONParser jParser = new JSONParser(this);
        jParser.execute(url);
    }

    @OnClick(R.id.artists_btn_playlist) void playlistButtonClicked(){
        //Intent button_playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
        //startActivityForResult(button_playlistIntent, 1);
    }

    @Override
    public void onRequestCompleted(JSONObject json) {
        try {
            results = json.getJSONArray(getApplicationContext().getString(R.string.TAG_RESULTS));

            ArrayList<String[]> artistsList = new ArrayList<>();

            for(int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);

                String id = result.getString(getApplicationContext().getString(R.string.TAG_ARTIST_ID));
                String name = result.getString(getApplicationContext().getString(R.string.TAG_ARTIST_NAME));

                String[] artistArray = {id, name};

                artistList.add(artistArray);
            }
        } catch (Exception e) {
            Log.e("ArtistsController", "Exception: " +  e);
        }

        if (json == null || json.isNull("results")) {
            Toast.makeText(getApplicationContext(), "Please retry, there has been an error downloading the artist list", Toast.LENGTH_SHORT).show();
        }
        else if (json.has("results") && artistList.isEmpty()){
            Toast.makeText(getApplicationContext(), "There are no artists matching this search", Toast.LENGTH_SHORT).show();
        }
        else {
            adapter.notifyDataSetChanged();
        }
    }

    public void onRowClick(String artistID) {
        Log.v("onRowClick", "clicked: " + artistID);
        //GeneralUtils.putHierarchy(getApplicationContext(), "artists");

        //Intent in = new Intent(getApplicationContext(), AlbumsActivity.class);
        //in.putExtra(getApplicationContext().getString(R.string.TAG_ARTIST_ID), artistID);
        //startActivityForResult(in, 2);
    }
}
