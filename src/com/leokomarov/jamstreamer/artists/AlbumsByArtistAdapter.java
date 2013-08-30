package com.leokomarov.jamstreamer.artists;

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

class AlbumsByArtistModel {

	  private HashMap<String, String> albumMap;
	  private boolean selected;

	  public AlbumsByArtistModel(HashMap<String, String> albumMap) {
		  this.albumMap = albumMap;
		  selected = false;
	  }
	  	  
	  public String getAlbumName(){
		  return albumMap.get("name");
	  }
	   
	  public boolean isSelected() {
		  return selected;
	  }

	  public void setSelected(boolean selected) {
	    this.selected = selected;
	  }

} 

public class AlbumsByArtistAdapter extends ArrayAdapter<AlbumsByArtistModel> {
	private final List<AlbumsByArtistModel> list;
	private final Activity context;
	protected static SparseBooleanArray AlbumsByArtistCheckboxList = new SparseBooleanArray();
	protected static int AlbumsByArtistCheckboxCount = 0;
	
	protected interface CallbackInterface {
        public void setListItemChecked(int position, boolean checked);
    }
	
	private CallbackInterface mCallback;
	
	protected AlbumsByArtistAdapter(CallbackInterface callback, Activity context, List<AlbumsByArtistModel> list) {
		super(context, R.layout.artists_4albums_by_list_item, list);
		this.context = context;
		this.list = list;
		mCallback = callback;
	}

	static class ViewHolder {
		protected TextView albumName;
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
			view = inflator.inflate(R.layout.artists_4albums_by_list_item, null);
			final ViewHolder viewHolder = new ViewHolder();
			
			viewHolder.albumName = (TextView) view.findViewById(R.id.artists4_albumName);
			viewHolder.checkbox = (CheckBox) view.findViewById(R.id.artists4_checkBox);
      
			viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					AlbumsByArtistModel element = (AlbumsByArtistModel) viewHolder.checkbox.getTag();
					element.setSelected(buttonView.isChecked());
					if (element.isSelected()){
						if (AlbumsByArtistCheckboxList.get(position, false) == false){
							AlbumsByArtistCheckboxList.put(position, true);
							++AlbumsByArtistCheckboxCount;
							mCallback.setListItemChecked(position, true);
						}
					}
					else {
						if (AlbumsByArtistCheckboxList.get(position, false) == true){
							AlbumsByArtistCheckboxList.put(position, false);
							if (AlbumsByArtistCheckboxCount >= 1){
								--AlbumsByArtistCheckboxCount;
								mCallback.setListItemChecked(position, false);
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
		String albumName = list.get(position).getAlbumName();
		holder.albumName.setText(albumName);
		holder.checkbox.setChecked(list.get(position).isSelected());
		return view;
	}
}