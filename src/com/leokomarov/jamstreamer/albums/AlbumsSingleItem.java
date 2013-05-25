package com.leokomarov.jamstreamer.albums;

import com.leokomarov.jamstreamer.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**Displays values passed from AndroidJSONParsingActivity in a text view. 
 **
 */
public class AlbumsSingleItem extends Activity {
	
	// JSON node keys
	private static final String TAG_NAME = "name";
	private static final String TAG_ARTISTID = "id";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_list_item);
        
        // getting intent data
        Intent in = getIntent();
        
        // Get JSON values from previous intent
        String name = in.getStringExtra(TAG_NAME);
        String id = in.getStringExtra(TAG_ARTISTID);
        
        // Displaying all values on the screen
        TextView lblName = (TextView) findViewById(R.id.name_label);
        TextView lblID = (TextView) findViewById(R.id.id_label);
        
        lblName.setText(name);
        lblID.setText(id);
    }
}