package com.leokomarov.jamstreamer.controllers;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.bluelinelabs.conductor.RouterTransaction;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.artists.ArtistsController;
import com.leokomarov.jamstreamer.controllers.base.ButterKnifeController;

import butterknife.BindView;
import butterknife.OnClick;

public class SearchController extends ButterKnifeController {

    @BindView(R.id.search_field)
    EditText searchField;

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_search, container, false);
    }

    @OnClick(R.id.search_button) void onSearchClick(){
        String artistName = searchField.getText().toString();
        if (artistName.length() >= 2){
            getRouter().pushController(RouterTransaction.with(new ArtistsController(artistName)));
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "You must enter two or more characters", Toast.LENGTH_SHORT).show();
        }
    }
}
