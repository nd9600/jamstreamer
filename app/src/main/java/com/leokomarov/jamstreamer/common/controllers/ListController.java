package com.leokomarov.jamstreamer.common.controllers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.leokomarov.jamstreamer.ActionBarProvider;
import com.leokomarov.jamstreamer.common.CustomListAdapter;

public abstract class ListController extends ButterKnifeController implements CustomListAdapter.CallbackInterface {

    public ListController(){
        super();
    }

    public ListController(Bundle args){
        super(args);
    }

    @Override
    protected abstract View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container);

    public abstract void onRowClick(int position);

    protected ActionBar getActionBar() {
        ActionBarProvider actionBarProvider = ((ActionBarProvider) getActivity());
        return actionBarProvider != null ? actionBarProvider.getSupportActionBar() : null;
    }
}
