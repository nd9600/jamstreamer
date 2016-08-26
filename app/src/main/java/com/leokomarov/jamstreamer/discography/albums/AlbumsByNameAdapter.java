package com.leokomarov.jamstreamer.discography.albums;

import android.annotation.SuppressLint;
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

public class AlbumsByNameAdapter extends ArrayAdapter<AlbumsByNameModel> {
	private final List<AlbumsByNameModel> list;
	private final Activity context;
	public static SparseBooleanArray listOfCheckboxes = new SparseBooleanArray();
	public static int tickedCheckboxCounter = 0;
	
	protected interface CallbackInterface {
        void callActionBar();
    }
	
	private CallbackInterface mCallback;
	
	protected AlbumsByNameAdapter(CallbackInterface callback, Activity context, List<AlbumsByNameModel> list) {
		super(context, R.layout.albums_by_name, list);
		this.context = context;
		this.list = list;
		mCallback = callback;
	}

	static class ViewHolder {
		protected TextView albumName;
		protected TextView albumArtist;
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

	@SuppressLint("InflateParams")
    @Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;

		if (convertView == null) {
			LayoutInflater inflator = context.getLayoutInflater();
			view = inflator.inflate(R.layout.albums_by_name, null);
			final ViewHolder viewHolder = new ViewHolder();
			
			viewHolder.albumName = (TextView) view.findViewById(R.id.albums_by_name_albumName);
			viewHolder.albumArtist = (TextView) view.findViewById(R.id.albums_by_name_albumArtist);
			viewHolder.checkbox = (CheckBox) view.findViewById(R.id.albums_by_name_checkBox);
      
			viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mCallback.callActionBar();
					
					AlbumsByNameModel element = (AlbumsByNameModel) viewHolder.checkbox.getTag();
					element.setSelected(buttonView.isChecked());
					if (element.isSelected()){
						if (! listOfCheckboxes.get(position, false)){
							listOfCheckboxes.put(position, true);
							tickedCheckboxCounter++;
						}
					}
					else {
						if (! listOfCheckboxes.get(position, false)){
							listOfCheckboxes.put(position, false);
							if (tickedCheckboxCounter >= 1){
								tickedCheckboxCounter--;
							}
						}
					}
					
					if (tickedCheckboxCounter == 0){
						AlbumsByName.mActionMode.finish();
	                }
					else if (tickedCheckboxCounter != 0){
						AlbumsByName.mActionMode.setTitle(tickedCheckboxCounter + " selected");
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
		
		if (AlbumsByName.selectAllPressed){
			if (AlbumsByName.selectAll && ! holder.checkbox.isChecked()){
				holder.checkbox.setChecked(true);
			}
			else if (! AlbumsByName.selectAll && holder.checkbox.isChecked()){
				holder.checkbox.setChecked(false);
			}
		}
		
		String albumName = list.get(position).getAlbumName();
		String albumArtist = list.get(position).getAlbumArtist();
		holder.albumName.setText(albumName);
		holder.albumArtist.setText(albumArtist);
		holder.checkbox.setChecked(list.get(position).isSelected());
		return view;
	}
}