package com.leokomarov.jamstreamer.playlist;

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

class PlaylistModel {
	  private HashMap<String, String> trackMap;
	  private boolean selected;

	  public PlaylistModel(HashMap<String, String> trackMap) {
		  this.trackMap = trackMap;
		  selected = false;
	  }
	    
	  public String getTrackNameAndDuration(){
		  return trackMap.get("trackName") + " - " + trackMap.get("trackDuration");
	  }
	  
	  public String getTrackArtistAndAlbum(){
		  return trackMap.get("trackArtist") + " - " + trackMap.get("trackAlbum");
	  }

	  public boolean isSelected() {
		  return selected;
	  }
	  
	  public void setSelected(boolean selected) {
		    this.selected = selected;
	  }

} 

public class PlaylistAdapter extends ArrayAdapter<PlaylistModel> {
	private final List<PlaylistModel> list;
	private final Activity context;
	public static SparseBooleanArray PlaylistCheckboxList = new SparseBooleanArray();
	public static int PlaylistCheckboxCount = 0;
	
	protected interface CallbackInterface {
        public void callActionBar();
    }
	
	private CallbackInterface mCallback;
	
	protected PlaylistAdapter(CallbackInterface callback, Activity context, List<PlaylistModel> list) {
		super(context, R.layout.playlist_by_list_item, list);
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
		View view = null;
		if (convertView == null) {
			LayoutInflater inflator = context.getLayoutInflater();
			view = inflator.inflate(R.layout.playlist_by_list_item, null);
			final ViewHolder viewHolder = new ViewHolder();
			
			viewHolder.trackNameAndDuration = (TextView) view.findViewById(R.id.playlist_trackNameAndDuration);
			viewHolder.trackArtistAndAlbum = (TextView) view.findViewById(R.id.playlist_trackArtistAndAlbum);
			viewHolder.checkbox = (CheckBox) view.findViewById(R.id.playlist_checkBox);
      
			viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mCallback.callActionBar();
					
					PlaylistModel element = (PlaylistModel) viewHolder.checkbox.getTag();
					element.setSelected(buttonView.isChecked());
					if (element.isSelected()){
						if (PlaylistCheckboxList.get(position, false) == false){
							PlaylistCheckboxList.put(position, true);
							PlaylistCheckboxCount++;
						}
					}
					else {
						if (PlaylistCheckboxList.get(position, false) == true){
							PlaylistCheckboxList.put(position, false);
							if (PlaylistCheckboxCount >= 1){
								PlaylistCheckboxCount--;
							}
						}
					}
					
					if (PlaylistCheckboxCount == 0){
	                	PlaylistActivity.mActionMode.finish();
	                }
					else if (PlaylistCheckboxCount != 0){
						PlaylistActivity.mActionMode.setTitle(PlaylistCheckboxCount + " selected");
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
		String trackNameAndDuration = list.get(position).getTrackNameAndDuration();
		String trackArtistAndAlbum = list.get(position).getTrackArtistAndAlbum();
		holder.trackNameAndDuration.setText(trackNameAndDuration);
		holder.trackArtistAndAlbum.setText(trackArtistAndAlbum);
		holder.checkbox.setChecked(list.get(position).isSelected());
		return view;
	}
}