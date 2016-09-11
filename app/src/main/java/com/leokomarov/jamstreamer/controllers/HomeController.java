package com.leokomarov.jamstreamer.controllers;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.controllers.base.ButterKnifeController;

import butterknife.BindView;

public class HomeController extends ButterKnifeController {

    @BindView(R.id.main_recycler_view)
    RecyclerView recyclerView;

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_home, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        //recyclerView.setAdapter(new HomeAdapter(LayoutInflater.from(view.getContext()), HomeDemoModel.values()));
        recyclerView.setAdapter();
    }

}