package com.leokomarov.jamstreamer.home;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.bluelinelabs.conductor.RouterTransaction;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.controllers.ButterKnifeController;
import com.leokomarov.jamstreamer.searches.albums.AlbumsController;
import com.leokomarov.jamstreamer.searches.artists.ArtistsController;
import com.leokomarov.jamstreamer.searches.tracks.TracksController;
import com.leokomarov.jamstreamer.util.GeneralUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class SearchController extends ButterKnifeController {

    @BindView(R.id.search_field)
    EditText searchField;

    @BindView(R.id.search_spinner)
    Spinner searchSpinner;

    String option;
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_search, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view){
        //ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.search_spinner_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        //spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        //searchSpinner.setAdapter(spinnerAdapter);
        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                option = (String) parent.getItemAtPosition(position);
                Log.v("selected", "option: " + option);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //the default option
        option = "Artists";
    }

    @OnClick(R.id.search_button) void onSearchClick(){
        String searchTerm = searchField.getText().toString();
        if (searchTerm.length() >= 2){
            switch (option) {
                case "Artists":
                    GeneralUtils.putHierarchy(getApplicationContext(), "artists");
                    getRouter().pushController(RouterTransaction.with(new ArtistsController(searchTerm)));
                    break;
                case "Albums":
                    GeneralUtils.putHierarchy(getApplicationContext(), "albums");
                    getRouter().pushController(RouterTransaction.with(new AlbumsController(searchTerm)));
                    break;
                case "Tracks":
                    GeneralUtils.putHierarchy(getApplicationContext(), "tracks");
                    getRouter().pushController(RouterTransaction.with(new TracksController(searchTerm)));
                    break;
            }

        } else {
            Toast.makeText(getActivity().getApplicationContext(), "You must enter two or more characters", Toast.LENGTH_SHORT).show();
        }
    }
}
