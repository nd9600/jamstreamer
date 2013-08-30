package com.leokomarov.jamstreamer.artists;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.leokomarov.jamstreamer.R;

class TracksByAlbumModel {

	  private HashMap<String, String> trackMap;
	  private boolean selected;

	  public TracksByAlbumModel(HashMap<String, String> trackMap) {
		  this.trackMap = trackMap;
		  selected = false;
	  }
	  	  
	  public String getTrackName(){
		  return trackMap.get("name");
	  }
	  
	  public String getTrackDuration(){
		  return trackMap.get("duration");
	  }

	  public boolean isSelected() {
		  return selected;
	  }

	  public void setSelected(boolean selected) {
	    this.selected = selected;
	  }

} 

public class TracksByAlbumAdapter extends ArrayAdapter<TracksByAlbumModel> {
	private final List<TracksByAlbumModel> list;
	private final Activity context;
	protected static SparseBooleanArray TracksByAlbumCheckboxList = new SparseBooleanArray();
	protected static int TracksByAlbumCheckboxCount = 0;
	
	protected interface CallbackInterface {
        public void setListItemChecked(int position, boolean checked);
    }
	
	private CallbackInterface mCallback;
	
	protected TracksByAlbumAdapter(CallbackInterface callback, Activity context, List<TracksByAlbumModel> list) {
		super(context, R.layout.artists_6tracks_in_album_list_item, list);
		this.context = context;
		this.list = list;
		mCallback = callback;
	}

	static class ViewHolder {
		protected TextView trackName;
		protected TextView trackDuration;
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
		View view = null;
		if (convertView == null) {
			LayoutInflater inflator = context.getLayoutInflater();
			view = inflator.inflate(R.layout.artists_6tracks_in_album_list_item, null);
			final ViewHolder viewHolder = new ViewHolder();
			
			viewHolder.trackName = (TextView) view.findViewById(R.id.TracksByAlbum_trackName);
			viewHolder.trackDuration = (TextView) view.findViewById(R.id.TracksByAlbum_trackDuration);
			viewHolder.checkbox = (CheckBox) view.findViewById(R.id.TracksByAlbum_checkBox);
      
			viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					TracksByAlbumModel element = (TracksByAlbumModel) viewHolder.checkbox.getTag();
					element.setSelected(buttonView.isChecked());
					if (element.isSelected()){
						if (TracksByAlbumCheckboxList.get(position, false) == false){
							TracksByAlbumCheckboxList.put(position, true);
							++TracksByAlbumCheckboxCount;
							mCallback.setListItemChecked(position, true);
							Log.v("TracksByAlbumArrayAdapter","TracksByAlbumCheckboxCount is " + TracksByAlbumCheckboxCount);
						}
					}
					else {
						if (TracksByAlbumCheckboxList.get(position, false) == true){
							TracksByAlbumCheckboxList.put(position, false);
							if (TracksByAlbumCheckboxCount >= 1){
								--TracksByAlbumCheckboxCount;
								mCallback.setListItemChecked(position, false);
								Log.v("TracksByAlbumArrayAdapter","TracksByAlbumCheckboxCount is " + TracksByAlbumCheckboxCount);
							}
						}
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
		String trackName = list.get(position).getTrackName();
		String trackDuration = list.get(position).getTrackDuration();
		holder.trackName.setText(trackName);
		holder.trackDuration.setText(trackDuration);
		holder.checkbox.setChecked(list.get(position).isSelected());
		return view;
	}
}