package com.leokomarov.jamstreamer.controllers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.ActionBarListActivity;
import com.leokomarov.jamstreamer.util.BundleBuilder;
import com.leokomarov.jamstreamer.util.GeneralUtils;
import com.leokomarov.jamstreamer.util.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;

public class ArtistsController extends ActionBarListActivity implements JSONParser.CallbackInterface {

    String artistName;
    JSONArray results = null;

    @BindView(R.id.artists1_btnPlaylist)
    ImageView playlistButton;

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
        return inflater.inflate(R.layout.original_empty_list, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        String unformattedURL = getResources().getString(R.string.artistsByNameJSONURL);
        String url = String.format(unformattedURL, artistName).replace("&amp;", "&").replace(" ", "+");

        JSONParser jParser = new JSONParser(this);
        jParser.execute(url);
    }

    /*
    @OnClick(R.id.artists1_btnPlaylist) void playlistButtonClicked(){
        //Intent button_playlistIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
        //startActivityForResult(button_playlistIntent, 1);
    }
    */

    @Override
    public void onRequestCompleted(JSONObject json) {
        ArrayList<HashMap<String, String>> artistList = new ArrayList<>();
        try {
            results = json.getJSONArray(getApplicationContext().getString(R.string.TAG_RESULTS));

            for(int i = 0; i < results.length(); i++) {
                JSONObject r = results.getJSONObject(i);

                String id = r.getString(getApplicationContext().getString(R.string.TAG_ARTIST_ID));
                String name = r.getString(getApplicationContext().getString(R.string.TAG_ARTIST_NAME));

                HashMap<String, String> map = new HashMap<>();

                map.put(getApplicationContext().getString(R.string.TAG_ARTIST_ID), id);
                map.put(getApplicationContext().getString(R.string.TAG_ARTIST_NAME), name);

                artistList.add(map);
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
            ListView lv = getListView();
            LayoutInflater inflater = getActivity().getLayoutInflater();

            ViewGroup header = (ViewGroup)inflater.inflate(R.layout.artists_list_header, lv, false);
            lv.addHeaderView(header, null, false);

            String[] stringArray = {getApplicationContext().getString(R.string.TAG_ARTIST_NAME), getApplicationContext().getString(R.string.TAG_ARTIST_ID)};
            int[] intArray = {R.id.artists_list_artists_names, R.id.artists_list_artists_ids};

            ListAdapter adapter = new SimpleAdapter(getApplicationContext(), artistList, R.layout.artists_list, stringArray , intArray);
            setListAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    GeneralUtils.putHierarchy(getApplicationContext(), "artists");
                    String artistID = ((TextView) view.findViewById(R.id.artists_list_artists_ids)).getText().toString();

                    //Intent in = new Intent(getApplicationContext(), AlbumsActivity.class);
                    //in.putExtra(getApplicationContext().getString(R.string.TAG_ARTIST_ID), artistID);
                    //startActivityForResult(in, 2);
                }
            });

        }
    }
}
