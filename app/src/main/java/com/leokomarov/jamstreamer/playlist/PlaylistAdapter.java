package com.leokomarov.jamstreamer.playlist;

import android.annotation.SuppressLint;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.leokomarov.jamstreamer.R;

import java.util.List;

public class PlaylistAdapter extends ArrayAdapter<PlaylistTrackModel> {
    protected interface CallbackInterface {
        void callActionBar();
    }

    //playlistTrackData is the data used to generate the listview,
    //made up of individual tracks stored as PlaylistTrackModels
	private final List<PlaylistTrackModel> playlistTrackData;

    //playlistActivity is the activity that contains the LV
	private final PlaylistActivity playlistActivity;

    //mCallback is the instance of the interface with the callActionBar() method
    private CallbackInterface mCallback;

    //listOfCheckboxes says if a checkbox at position _i_ has been checked
	public static SparseBooleanArray listOfCheckboxes = new SparseBooleanArray();
    //tickedCheckboxCounter is the number of checked checkboxes
	public static int tickedCheckboxCounter = 0;
	
	protected PlaylistAdapter(CallbackInterface callback, PlaylistActivity playlistActivity, List<PlaylistTrackModel> playlistTrackData) {
		super(playlistActivity, R.layout.playlist_by_list_item, playlistTrackData);
		this.playlistActivity = playlistActivity;
		this.playlistTrackData = playlistTrackData;
		this.mCallback = callback;
	}

    //ViewHolder holds the track textviews and checkbox
	static class ViewHolder {
        protected CheckBox checkbox;
		protected TextView trackNameAndDuration;
		protected TextView trackArtistAndAlbum;
	}

    //returns the number of rows to display
	public int getCount() {
		return playlistTrackData.size();
	}

    //if the checkbox isn't ticked in the list,
    //put it in the list as ticked
    //and increment the counter
    //or vice versa
    public static void tickCheckbox(int position, boolean tickIt){
        if (listOfCheckboxes.get(position, false) == (! tickIt)){
            listOfCheckboxes.put(position, tickIt);
        }
        tickedCheckboxCounter += tickIt ? 1 : -1;
    }

    //called on every scroll, returns the individual view for each track
    //so views are reused if possible - convertView holds the reusedView if it exists
	@SuppressLint("InflateParams")
    @Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;

        //If the reused view exists, return it, don't make a new one
        //and stores the track data in the checkbox's tag field
		if (convertView != null) {
            view = convertView;
            ((ViewHolder) view.getTag()).checkbox.setTag(playlistTrackData.get(position));

        //if it doesn't exist, inflate a new one
        } else {
			LayoutInflater inflater = playlistActivity.getLayoutInflater();
			view = inflater.inflate(R.layout.playlist_by_list_item, null);

            //creates a new viewHolder for that track and put the textviews and checkbox inside it
			final ViewHolder viewHolder = new ViewHolder();
            viewHolder.checkbox = (CheckBox) view.findViewById(R.id.playlist_checkBox);
			viewHolder.trackNameAndDuration = (TextView) view.findViewById(R.id.playlist_trackNameAndDuration);
			viewHolder.trackArtistAndAlbum = (TextView) view.findViewById(R.id.playlist_trackArtistAndAlbum);

            //set the tag field of the view for this track to be the viewHolder
            //and set the checkbox's tag field to be the track data
            view.setTag(viewHolder);
            viewHolder.checkbox.setTag(playlistTrackData.get(position));

            //creates the click listener for the checkbox
			viewHolder.checkbox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

                    System.out.println("checkbox clicked: " + viewHolder.trackNameAndDuration.getText());

                    //calls the action bar and sets selectAllPressed to false
					mCallback.callActionBar();
                    PlaylistPresenter.selectAllPressed = false;

                    //gets the track data from the checkbox's tag field
                    //and sets whether it's selected
					PlaylistTrackModel element = (PlaylistTrackModel) viewHolder.checkbox.getTag();
					//element.setSelected(buttonView.isChecked());
                    element.setSelected(viewHolder.checkbox.isChecked());

                    //if the checkbox is ticked, and if it isn't ticked in the list,
                    //put it in the list at ticked
                    //and increment the counter
                    tickCheckbox(position, viewHolder.checkbox.isChecked());
                    /*
					if (viewHolder.checkbox.isChecked()){
						if (! listOfCheckboxes.get(position, false)){
							listOfCheckboxes.put(position, true);
							tickedCheckboxCounter++;
						}
					}
                    //if it isn't ticked, and if it isn't unticked in the list,
                    //put it in the list as unticked
                    //and decrement the counter if necessary
					else {
						if (listOfCheckboxes.get(position, false)){
							listOfCheckboxes.put(position, false);
							if (tickedCheckboxCounter >= 1){
								tickedCheckboxCounter--;
							}
						}
					}
					*/

                    //if no checkboxes are ticked, close the action bar
                    //if they are, set the title to be how many are ticked
					if (tickedCheckboxCounter == 0){
	                	PlaylistActivity.mActionMode.finish();
	                }
					else {
						PlaylistActivity.mActionMode.setTitle(tickedCheckboxCounter + " selected");
	                }
				}
			});
		}

        //get the viewHolder for this view
		ViewHolder holder = (ViewHolder) view.getTag();

        /*
        //if the select all button is pressed, and the checkbox isn't ticked, tick it
        //else if select all isn't pressed and it is ticked, untick it
		if (PlaylistPresenter.selectAllPressed){
			if (PlaylistActivity.selectAll && ! holder.checkbox.isChecked()){
				holder.checkbox.setChecked(true);
			}
			else if (! PlaylistActivity.selectAll && holder.checkbox.isChecked()){
				holder.checkbox.setChecked(false);
			}
		}
		*/

        //get the name, duration, artist and album info from the track data
        //and update the viewHolder's views
		String trackNameAndDuration = playlistTrackData.get(position).getTrackNameAndDuration();
		String trackArtistAndAlbum = playlistTrackData.get(position).getTrackArtistAndAlbum();
		holder.trackNameAndDuration.setText(trackNameAndDuration);
		holder.trackArtistAndAlbum.setText(trackArtistAndAlbum);
		holder.checkbox.setChecked(playlistTrackData.get(position).isSelected());

		return view;
	}

}