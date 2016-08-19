package com.leokomarov.jamstreamer.discography;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.leokomarov.jamstreamer.R;



public class TracksByNameAdapter extends ArrayAdapter<TracksByNameModel> {
	private final List<TracksByNameModel> list;
	private final Activity context;
	public static SparseBooleanArray TracksByNameCheckboxList = new SparseBooleanArray();
	public static int TracksByNameCheckboxCount = 0;
	
	protected interface CallbackInterface {
        void callActionBar();
    }
	
	private CallbackInterface mCallback;
	
	protected TracksByNameAdapter(CallbackInterface callback, Activity context, List<TracksByNameModel> list) {
		super(context, R.layout.tracks_by_name, list);
		this.context = context;
		this.list = list;
		mCallback = callback;
	}

	static class ViewHolder {
		protected TextView trackNameAndDuration;
		protected TextView trackArtistAndAlbum;
		protected CheckBox checkbox;
	}
	
	public int getCount() {
		return list.size();
	}
	
	@Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 250;
    }

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;

		if (convertView == null) {
			LayoutInflater inflator = context.getLayoutInflater();
			view = inflator.inflate(R.layout.tracks_by_name, null);
			final ViewHolder viewHolder = new ViewHolder();
			
			viewHolder.trackNameAndDuration = (TextView) view.findViewById(R.id.tracks_by_name_trackNameAndDuration);
			viewHolder.trackArtistAndAlbum = (TextView) view.findViewById(R.id.tracks_by_name_trackArtistAndAlbum);
			viewHolder.checkbox = (CheckBox) view.findViewById(R.id.tracks_by_name_checkBox);
      
			viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mCallback.callActionBar();
					
					TracksByNameModel element = (TracksByNameModel) viewHolder.checkbox.getTag();
					element.setSelected(buttonView.isChecked());
					if (element.isSelected()){
						if (! TracksByNameCheckboxList.get(position, false)){
							TracksByNameCheckboxList.put(position, true);
							TracksByNameCheckboxCount++;
						}
					}
					else {
						if (! TracksByNameCheckboxList.get(position, false)){
							TracksByNameCheckboxList.put(position, false);
							if (TracksByNameCheckboxCount >= 1){
								TracksByNameCheckboxCount--;
							}
						}
					}
					
					if (TracksByNameCheckboxCount == 0){
						TracksByName.mActionMode.finish();
	                }
	                else if (TracksByNameCheckboxCount != 0){
	                	TracksByName.mActionMode.setTitle(TracksByNameCheckboxCount + " selected");
	                }
					
				}
			});
      
			view.setTag(viewHolder);
			viewHolder.checkbox.setTag(list.get(position));
		} else {
			view = convertView;
			((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
		}
	  	
		ViewHolder holder = (ViewHolder) view.getTag();
		
		if (TracksByName.selectAllPressed){
			if (TracksByName.selectAll && ! holder.checkbox.isChecked()){
				holder.checkbox.setChecked(true);
			}
			else if (! TracksByName.selectAll && holder.checkbox.isChecked()){
				holder.checkbox.setChecked(false);
			}
		}
		
		String trackNameAndDuration = list.get(position).getTrackNameAndDuration();
		String trackArtistAndAlbum = list.get(position).getTrackArtistAndAlbum();
		holder.trackNameAndDuration.setText(trackNameAndDuration);
		holder.trackArtistAndAlbum.setText(trackArtistAndAlbum);
		holder.checkbox.setChecked(list.get(position).isSelected());
		return view;
	}
}