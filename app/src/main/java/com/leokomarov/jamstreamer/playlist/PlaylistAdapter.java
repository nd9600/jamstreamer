package com.leokomarov.jamstreamer.playlist;

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

import java.util.List;

public class PlaylistAdapter extends ArrayAdapter<PlaylistTrackModel> {
	private final List<PlaylistTrackModel> list;
	private final Activity context;
	public static SparseBooleanArray PlaylistCheckboxList = new SparseBooleanArray();
	public static int PlaylistCheckboxCount = 0;
	
	protected interface CallbackInterface {
        void callActionBar();
    }
	
	private CallbackInterface mCallback;
	
	protected PlaylistAdapter(CallbackInterface callback, Activity context, List<PlaylistTrackModel> list) {
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
		View view;

		if (convertView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			view = inflater.inflate(R.layout.playlist_by_list_item, null);
			final ViewHolder viewHolder = new ViewHolder();
			
			viewHolder.trackNameAndDuration = (TextView) view.findViewById(R.id.playlist_trackNameAndDuration);
			viewHolder.trackArtistAndAlbum = (TextView) view.findViewById(R.id.playlist_trackArtistAndAlbum);
			viewHolder.checkbox = (CheckBox) view.findViewById(R.id.playlist_checkBox);
      
			viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mCallback.callActionBar();
					
					PlaylistTrackModel element = (PlaylistTrackModel) viewHolder.checkbox.getTag();
					element.setSelected(buttonView.isChecked());
					if (element.isSelected()){
						if (! PlaylistCheckboxList.get(position, false)){
							PlaylistCheckboxList.put(position, true);
							PlaylistCheckboxCount++;
						}
					}
					else {
						if (! PlaylistCheckboxList.get(position, false)){
							PlaylistCheckboxList.put(position, false);
							if (PlaylistCheckboxCount >= 1){
								PlaylistCheckboxCount--;
							}
						}
					}
					
					if (PlaylistCheckboxCount == 0){
	                	PlaylistActivity.mActionMode.finish();
	                }
					else {
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
		
		if (PlaylistPresenter.selectAllPressed){
			if (PlaylistActivity.selectAll && ! holder.checkbox.isChecked()){
				holder.checkbox.setChecked(true);
			}
			else if (! PlaylistActivity.selectAll && holder.checkbox.isChecked()){
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