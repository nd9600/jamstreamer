package com.leokomarov.jamstreamer.searches.artists;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bluelinelabs.conductor.RouterTransaction;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.controllers.ButterKnifeController;
import com.leokomarov.jamstreamer.playlist.PlaylistController;
import com.leokomarov.jamstreamer.searches.albums.AlbumsController;
import com.leokomarov.jamstreamer.util.BundleBuilder;
import com.leokomarov.jamstreamer.util.GeneralUtils;
import com.leokomarov.jamstreamer.util.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class ArtistsController extends ButterKnifeController implements JSONParser.CallbackInterface {

    private String artistName;

    private RecyclerView.Adapter listAdapter;
    private ArrayList<String[]> artistList;

    @BindView(R.id.results_list_header_text)
    TextView results_list_header_textview;

    @BindView(R.id.main_recycler_view)
    RecyclerView recyclerView;

    @OnClick(R.id.results_list_header_btn_playlist)
    void playlistButtonClicked(){
        getRouter().pushController(RouterTransaction.with(new PlaylistController()));
    }

    public ArtistsController(Bundle args) {
        super(args);
    }

    public ArtistsController(String artistName){
        this(new BundleBuilder(new Bundle())
                .putString("artistName", artistName)
                .build());
        this.artistName = artistName;
    }

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_list, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        results_list_header_textview.setText(getApplicationContext().getString(R.string.mainArtists));

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        artistList = new ArrayList<>();
        listAdapter = new ArtistsAdapter(this, LayoutInflater.from(view.getContext()), artistList);
        recyclerView.setAdapter(listAdapter);

        String unformattedURL = getResources().getString(R.string.artistsByNameJSONURL);
        String url = String.format(unformattedURL, artistName).replace("&amp;", "&").replace(" ", "+");

        JSONParser jParser = new JSONParser(this);
        jParser.execute(url);
    }

    @Override
    public void onRequestCompleted(JSONObject json) {
        try {
            JSONArray results = json.getJSONArray(getApplicationContext().getString(R.string.TAG_RESULTS));

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
            listAdapter.notifyDataSetChanged();
        }
    }

    public void onRowClick(String artistID) {
        Log.v("onRowClick", "clicked: " + artistID);
        GeneralUtils.putHierarchy(getApplicationContext(), "artists");
        getRouter().pushController(RouterTransaction.with(new AlbumsController(artistID)));
    }
}
