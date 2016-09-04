package com.leokomarov.jamstreamer.common;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public abstract class CustomListAdapter extends ArrayAdapter<TrackModel> {

    public interface CallbackInterface {
        void callActionBar(int tickedCheckboxCounter);
    }

    //mCallback is the instance of the interface with the callActionBar() method
    private CallbackInterface mCallback;

    //listActivity is the activity that contains the LV
    private final ActionBarListActivity listActivity;

    //listLayoutID is the ID of the layout used for the LV
    private final int listLayoutID;

    //selectAll is changed when the selectAll/none button is pressed
    public boolean selectAll;
    public boolean selectAllPressed;

    //trackData is the data used to generate the listview,
    //made up of individual tracks stored as trackModels
    private final List<TrackModel> trackData;

    private final int checkboxID;
    private final int textView1ID;
    private final int textView2ID;

    //listOfCheckboxes says if a checkbox at position _i_ has been checked
    public SparseBooleanArray listOfCheckboxes = new SparseBooleanArray();
    //tickedCheckboxCounter is the number of checked checkboxes
    public int tickedCheckboxCounter = 0;

    protected CustomListAdapter(CallbackInterface callback, ActionBarListActivity listActivity, List<TrackModel> trackData, int listLayoutID, int checkboxID, int textView1ID, int textView2ID) {
        super(listActivity, listLayoutID, trackData);
        this.mCallback = callback;
        this.listActivity = listActivity;
        this.trackData = trackData;
        this.listLayoutID = listLayoutID;
        this.checkboxID = checkboxID;
        this.textView1ID = textView1ID;
        this.textView2ID = textView2ID;
    }

    //returns the number of rows to display
    public int getCount() {
        return trackData.size();
    }

    //if the checkbox isn't ticked in the list,
    //put it in the list as ticked
    //and increment the counter
    //or vice versa
    public void tickCheckbox(int position, boolean tickIt){
        if (listOfCheckboxes.get(position, false) == (! tickIt)){
            Log.v("tickCheckbox", "Setting #" + position + " to " + tickIt);
            listOfCheckboxes.put(position, tickIt);
            tickedCheckboxCounter += tickIt ? 1 : -1;
        }
    }

    //Clears the checkbox list and counter for the adapter
    public void clearCheckboxes(){
        Log.v("clearCheckboxes", "");
        Log.v("clearCheckboxes", "Clearing checkboxes");
        listOfCheckboxes.clear();
        tickedCheckboxCounter = 0;
        mCallback.callActionBar(0);
        notifyDataSetChanged();
    }

    //ViewHolder holds the track textviews and checkbox
    public static class ViewHolder {
        public CheckBox checkbox;
        public TextView textView1;
        public TextView textView2;
    }

    //set the viewHolder's views
    public void setViewHolder(View view, ViewHolder viewHolder, int checkboxID, int textView1ID, int textView2ID){
        viewHolder.checkbox = (CheckBox) view.findViewById(checkboxID);
        viewHolder.textView1 = (TextView) view.findViewById(textView1ID);
        viewHolder.textView2 = (TextView) view.findViewById(textView2ID);
    }

    //update the viewHolder's views
    public abstract void updateViewHolder(ViewHolder viewHolder, int position);

    //called on every scroll, returns the individual view for each track
    //so views are reused if possible - convertView holds the reusedView if it exists
    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View view;

        //If the reused view exists, return it, don't make a new one
        //and stores the track data in the checkbox's tag field
        if (convertView != null) {
            view = convertView;
            ((ViewHolder) view.getTag()).checkbox.setTag(trackData.get(position));

            //if it doesn't exist, inflate a new one
        } else {
            LayoutInflater inflater = listActivity.getLayoutInflater();
            view = inflater.inflate(listLayoutID, null);

            //creates a new viewHolder for that track and put the textviews and checkbox inside it
            final ViewHolder viewHolder = new ViewHolder();
            setViewHolder(view, viewHolder, checkboxID, textView1ID, textView2ID);

            //set the tag field of the view for this track to be the viewHolder
            //and set the checkbox's tag field to be the track data
            view.setTag(viewHolder);
            viewHolder.checkbox.setTag(trackData.get(position));

            //creates the click listener for the checkbox
            viewHolder.checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //sets selectAllPressed to false
                    //and change the checkbox list and counter

                    Log.v("onClick", "");
                    Log.v("onClick", String.format("Track: ", position, viewHolder.textView1.getText()));
                    Log.v("onClick", String.format("Setting %s to %s", position, viewHolder.checkbox.isChecked()));

                    selectAllPressed = false;
                    tickCheckbox(position, viewHolder.checkbox.isChecked());

                    //calls the action bar, and
                    //if no checkboxes are ticked, close the action bar
                    //if they are, set the title to be how many are ticked
                    mCallback.callActionBar(tickedCheckboxCounter);
                }
            });
        }

        //breaks the select all button when there isn't any scrolling
        /*
        //if the select all button is pressed, and the checkbox isn't ticked, tick it
        //else if select all isn't pressed and it is ticked, untick it
		if (PlaylistPresenter.selectAllPressed){
            System.out.println("selectAllPressed");
            if (PlaylistActivity.selectAll && ! holder.checkbox.isChecked()){
                System.out.println("setting checkbox to checked");
				holder.checkbox.setChecked(true);
			}
			else if (! PlaylistActivity.selectAll && holder.checkbox.isChecked()){
                System.out.println("setting checkbox to unchecked");
				holder.checkbox.setChecked(false);
			}
		}
		*/

        //get the viewHolder for this view and update it's views with information from the listData
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        updateViewHolder(viewHolder, position);

        return view;
    }
}
