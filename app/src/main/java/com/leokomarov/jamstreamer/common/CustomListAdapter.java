package com.leokomarov.jamstreamer.common;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.controllers.ListController;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class CustomListAdapter extends RecyclerView.Adapter<CustomListAdapter.ViewHolder> {

    public interface CallbackInterface {
        void callActionBar(int tickedCheckboxCounter);
    }

    //mCallback is the instance of the interface with the callActionBar() method
    private static CallbackInterface mCallback;

    //listController is the activity that contains the RV
    protected static ListController listController;

    protected final LayoutInflater inflater;

    //selectAll is changed when the selectAll/none button is pressed
    public boolean selectAll;

    //listData is the data used to generate the recyclerview,
    //made up of individual tracks stored as trackModels
    protected List<TrackModel> listData;

    //listOfCheckboxes says if a checkbox at position _i_ has been checked
    public SparseBooleanArray listOfCheckboxes = new SparseBooleanArray();
    //tickedCheckboxCounter is the number of checked checkboxes
    public int tickedCheckboxCounter = 0;

    protected CustomListAdapter(CallbackInterface callback, ListController listController, List<TrackModel> listData, LayoutInflater inflater) {
        mCallback = callback;
        CustomListAdapter.listController = listController;
        this.listData = listData;
        this.inflater = inflater;
    }

    //returns the number of rows to display
    @Override
    public int getItemCount() {
        return this.listData.size();
    }

    //if the checkbox isn't ticked in the list,
    //put it in the list as ticked
    //and increment the counter
    //or vice versa
    public void tickCheckbox(int position, boolean tickIt){
        Log.v("tracks", "ticking position: " + position);
        if (listOfCheckboxes.get(position, false) == (! tickIt)){
            listOfCheckboxes.put(position, tickIt);
            tickedCheckboxCounter += tickIt ? 1 : -1;
            Log.v("tracks", "setting to: " + tickIt);
        }
    }

    //Clears the checkbox list and counter for the adapter
    public void clearCheckboxes(){
        listOfCheckboxes.clear();
        tickedCheckboxCounter = 0;
        mCallback.callActionBar(0);
    }

    public void selectAllItems(){
        int numberOfTracks = getItemCount();

        for (int i = 0; i < numberOfTracks; i++) {
            tickCheckbox(i, selectAll);
            View view = listController.recyclerView.getChildAt(i);

            if (view != null) {
                CheckBox checkbox = (CheckBox) view.findViewById(R.id.row_checkbox);

                //if the checkbox isn't ticked, tick it
                //or vice versa
                if (checkbox.isChecked() == (! selectAll)) {
                    checkbox.setChecked(selectAll);
                }
            }

        }
        selectAll = ! selectAll;
        mCallback.callActionBar(tickedCheckboxCounter);
    }

    @Override
    public CustomListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.row_list_results, parent, false));
    }

    public void onBindViewHolder(CustomListAdapter.ViewHolder holder, int position) {
        boolean checkboxTickedInList = listOfCheckboxes.get(position);
        if (holder.checkbox.isChecked() != (checkboxTickedInList)) {
                holder.checkbox.setChecked(checkboxTickedInList);
        }

        updateViewHolder(holder, position);
    }

    //update the viewHolder's views
    public abstract void updateViewHolder(ViewHolder viewHolder, int position);

    //ViewHolder holds the track textviews and checkbox
    protected class ViewHolder extends RecyclerView.ViewHolder {

        //set the viewHolder's views
        @BindView(R.id.row_checkbox)
        public CheckBox checkbox;

        @BindView(R.id.row_textview_1)
        public TextView textView1;

        @BindView(R.id.row_textview_2)
        public TextView textView2;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.row_checkbox)
        public void onCheckboxClick() {
            tickCheckbox(getAdapterPosition(), checkbox.isChecked());
            mCallback.callActionBar(tickedCheckboxCounter);
        }

        @OnClick(R.id.row_root)
        void onRowClick() {
            listController.onRowClick(getAdapterPosition());
        }
    }
}
