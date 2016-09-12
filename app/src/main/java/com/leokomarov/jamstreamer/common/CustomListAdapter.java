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
import com.leokomarov.jamstreamer.controllers.base.ListController;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class CustomListAdapter extends RecyclerView.Adapter<CustomListAdapter.ViewHolder> {

    public interface CallbackInterface {
        void callActionBar(int tickedCheckboxCounter);
    }

    //mCallback is the instance of the interface with the callActionBar() method
    private CallbackInterface mCallback;

    //listController is the activity that contains the LV
    protected final ListController listController;

    protected final LayoutInflater inflater;

    //listLayoutID is the ID of the layout used for the LV
    //private final int listLayoutID;

    //selectAll is changed when the selectAll/none button is pressed
    public boolean selectAll;
    public boolean selectAllPressed;

    //listData is the data used to generate the listview,
    //made up of individual tracks stored as trackModels
    protected List<TrackModel> listData;

    /*
    private final int checkboxID;
    private final int textView1ID;
    private final int textView2ID;
    */

    //listOfCheckboxes says if a checkbox at position _i_ has been checked
    public SparseBooleanArray listOfCheckboxes = new SparseBooleanArray();
    //tickedCheckboxCounter is the number of checked checkboxes
    public int tickedCheckboxCounter = 0;

    //maps the hashcodes of track models to their indexes
    protected HashMap<Integer, Integer> hashcodeToPosition = new HashMap<>();

    protected CustomListAdapter(CallbackInterface callback, ListController listController, List<TrackModel> listData, LayoutInflater inflater) {
        this.mCallback = callback;
        this.listController = listController;
        this.listData = listData;
        this.inflater = inflater;
        /*
        this.listLayoutID = listLayoutID;
        this.checkboxID = checkboxID;
        this.textView1ID = textView1ID;
        this.textView2ID = textView2ID;
        */

        for (int i = 0; i < this.listData.size(); i++){
            Log.v("updateListData-1", String.format("%s: %s", i, this.listData.get(i)));
        }

    }

    public void updateListData(List<TrackModel> listData){

        int previousNumberOfItems = this.listData.size();

        for (int i = 0; i < this.listData.size(); i++){
            Log.v("updateListData-1", String.format("%s: %s", i, this.listData.get(i)));
        }

        for (int i = 0; i < listData.size(); i++){
            Log.v("updateListData-2", String.format("%s: %s", i, listData.get(i)));
        }

        this.listData = listData;
        Log.v("updateListData", "" + previousNumberOfItems + ", " + listData.size());
        notifyDataSetChanged();
        //notifyItemRangeInserted(previousNumberOfItems, listData.size());
        //updateHashcodeMap();
    }

    //returns the number of rows to display
    public int getItemCount() {
        return listData.size();
    }

    public void updateHashcodeMap(){
        hashcodeToPosition.clear();
        for (int i = 0; i < listData.size(); i++){
            Log.v("updateHashcodeMap", String.format("%s: %s", i, listData.get(i)));
            Log.v("updateHashcodeMap", String.format("%s: %s", i, listData.get(i).getMap()));
            Log.v("updateHashcodeMap", String.format("%s: %s", i, listData.get(i).getMap().hashCode()));
            hashcodeToPosition.put(listData.get(i).getMap().hashCode(), i);
        }
    }

    //if the checkbox isn't ticked in the list,
    //put it in the list as ticked
    //and increment the counter
    //or vice versa
    public void tickCheckbox(int position, boolean tickIt){
        if (listOfCheckboxes.get(position, false) == (! tickIt)){
            listOfCheckboxes.put(position, tickIt);
            tickedCheckboxCounter += tickIt ? 1 : -1;
        }
    }

    //Clears the checkbox list and counter for the adapter
    public void clearCheckboxes(){
        listOfCheckboxes.clear();
        tickedCheckboxCounter = 0;
        mCallback.callActionBar(0);
        //notifyDataSetChanged();
    }

    @Override
    public CustomListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.list_results_row, parent, false));
    }

    public void onBindViewHolder(CustomListAdapter.ViewHolder holder, int position) {
        holder.bind(position);
    }

    //update the viewHolder's views
    public abstract void updateViewHolder(ViewHolder viewHolder, int position);

    //ViewHolder holds the track textviews and checkbox
    public class ViewHolder extends RecyclerView.ViewHolder {

        //set the viewHolder's views
        @BindView(R.id.row_checkbox)
        public CheckBox checkbox;

        @BindView(R.id.row_textview_1)
        public TextView textView1;

        @BindView(R.id.row_textview_2)
        public TextView textView2;

        int position;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(int position) {
            this.position = position;
            checkbox.setTag(listData.get(position));
            updateViewHolder(this, position);
            //creates the click listener for the checkbox
        }

        @OnClick(R.id.row_checkbox)
        public void onCheckboxClick() {
            //sets selectAllPressed to false
            //and change the checkbox list and counter
            //using the view data - indexPosition in data map

            Log.v("onCheckboxClick", "position:" + position);

            /*
            TrackModel trackModel = ((TrackModel) checkbox.getTag());
            Log.v("onCheckboxClick", "trackModel: " + trackModel);

            int trackModelHashcode = trackModel.getMap().hashCode();
            Log.v("onCheckboxClick", "trackModelHashcode: " + trackModelHashcode);

            for (int i = 0; i < hashcodeToPosition.size(); i++){
                Log.v("onCheckboxClick", String.format("%s: %s", i, trackModel.getMap().hashCode()));
            }

            int indexPosition = hashcodeToPosition.get(trackModelHashcode);

            Log.v("onCheckboxClick", "indexPosition:" + indexPosition);
            Log.v("onCheckboxClick", "position: " + position);
            */

            selectAllPressed = false;
            tickCheckbox(position, checkbox.isChecked());

            //calls the action bar, and
            //if no checkboxes are ticked, close the action bar
            //if they are, set the title to be how many are ticked
            mCallback.callActionBar(tickedCheckboxCounter);
        }

        @OnClick(R.id.row_root)
        void onRowClick() {
            listController.onRowClick(position);
        }

    }

    /*
    //called on every scroll, returns the individual view for each track
    //so views are reused if possible - convertView holds the reusedView if it exists
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View view;

        //If the reused view exists, return it, don't make a new one
        //and store the track data in the checkbox's tag field
        if (convertView != null) {
            view = convertView;

            //get the viewHolder for this view and update its views with information from the listData
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            viewHolder.checkbox.setTag(listData.get(position));
            updateViewHolder(viewHolder, position);

            //if it doesn't exist, inflate a new one
        } else {
            view = inflater.inflate(listLayoutID, null);

            //creates a new viewHolder for that track and put the textviews and checkbox inside it
            final ViewHolder viewHolder = new ViewHolder();
            setViewHolder(view, viewHolder, checkboxID, textView1ID, textView2ID);

            //set the checkbox's tag field to be the track data
            viewHolder.checkbox.setTag(listData.get(position));

            //set the tag field of the view for this track to be the viewHolder
            view.setTag(viewHolder);

            //use the viewHolder for this view and update its views with information from the listData
            updateViewHolder(viewHolder, position);
        }

        //breaks the select all button when there isn't any scrolling
        /*
        //if the select all button is pressed, and the checkbox isn't ticked, tick it
        //else if select all isn't pressed and it is ticked, untick it
		if (selectAllPressed){
            System.out.println("selectAllPressed");
            if (selectAll && ! holder.checkbox.isChecked()){
                System.out.println("setting checkbox to checked");
				holder.checkbox.setChecked(true);
			}
			else if (! selectAll && holder.checkbox.isChecked()){
                System.out.println("setting checkbox to unchecked");
				holder.checkbox.setChecked(false);
			}
		}
		*/

    /*
        return view;
    }
    */
}
