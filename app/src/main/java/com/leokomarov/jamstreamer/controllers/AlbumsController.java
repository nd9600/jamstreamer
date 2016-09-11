package com.leokomarov.jamstreamer.controllers;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.controllers.base.ButterKnifeController;

public class AlbumsController extends ButterKnifeController {
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_albums, container, false);
    }
}
