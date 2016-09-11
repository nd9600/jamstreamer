package com.leokomarov.jamstreamer.common;

import android.os.Bundle;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.leokomarov.jamstreamer.controllers.base.ButterKnifeController;

public abstract class ActionBarListActivity extends ButterKnifeController {

    private ListView mListView;

    public ActionBarListActivity(){
    }

    public ActionBarListActivity(Bundle args){
        super(args);
    }

    protected ListView getListView() {
        if (mListView == null) {
            mListView = (ListView) getActivity().findViewById(android.R.id.list);
        }
        return mListView;
    }

    protected void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
    }

    protected ListAdapter getListAdapter() {
        ListAdapter adapter = getListView().getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            return ((HeaderViewListAdapter)adapter).getWrappedAdapter();
        } else {
            return adapter;
        }
    }
}
