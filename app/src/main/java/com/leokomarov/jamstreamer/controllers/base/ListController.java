package com.leokomarov.jamstreamer.controllers.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.leokomarov.jamstreamer.common.CustomListAdapter;

public abstract class ListController extends ButterKnifeController implements CustomListAdapter.CallbackInterface {

    public ListController(Bundle args){
        super(args);
    }

    @Override
    protected abstract View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container);

    public abstract void onRowClick(int position);
}
