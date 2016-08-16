package com.leokomarov.jamstreamer.searches;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.AlbumsByName;
import com.leokomarov.jamstreamer.common.AlbumsByNameAdapter;

public class AlbumsSearch extends SherlockActivity {
	public static final String TAG_ALBUM_NAME = "name";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albums_search);        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        findViewById(R.id.albumsSearchButton).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(getApplicationContext(), AlbumsByName.class);
                EditText editText = (EditText) findViewById(R.id.albumsByNameField);
                String albumName = editText.getText().toString();
                if (albumName.length() >= 2){
                    intent.putExtra(TAG_ALBUM_NAME, albumName);
                    startActivityForResult(intent, 1);	
                }
                else {
                	Toast.makeText(getApplicationContext(), "You must enter two or more characters", Toast.LENGTH_SHORT).show();
                }
        	}
        });  
	}
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
	    	AlbumsByNameAdapter.AlbumsByNameCheckboxList.clear();
	    	AlbumsByNameAdapter.AlbumsByNameCheckboxCount = 0;
	    }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 
	        int itemId = item.getItemId();
			if (itemId == android.R.id.home) {
				onBackPressed();
				return true;
			}
	    return super.onOptionsItemSelected(item);
	}	
}