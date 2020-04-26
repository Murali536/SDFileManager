package com.mkr.file_explorer;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;

/**
 * this is the settings activity
 * @author Manthena Murali
 */

public class Settings extends AppCompatActivity {

	//prefs
	public static final String PREF_DEF_HOME_FOLDER 		= "def_home_folder";
	public static final String PREF_DISPLAY_HIDDEN_FILES 	= "display_hidden";
	public static final String PREF_DISPLAY_MODIFIED_DATE 	= "display_modified_date";
	public static final String PREF_DISPLAY_THUMBNAILS 		= "display_thumbs";
	public static final String PREF_SORT_ITEMS_BY 			= "sort_items";
	public static final String PREF_ITEM_TEXT_SIZE 			= "text_size";
	public static final String PREF_VIEW_TYPE    			= "view_type";
	public static final String PREF_FIRST_LAUNCH   			= "first_launch";

	//item sorting constants
	public static final int SORT_BY_NAME 			= 0;
	public static final int SORT_BY_SIZE 			= 1;
	public static final int SORT_BY_MODIFIED_DATE   = 2;
	public static final int SORT_BY_TYPE   			= 3;
	
	//text sizes
	public static final int TEXT_SIZE_SMALL 		= 0;
	public static final int TEXT_SIZE_MEDIUM  		= 1;
	public static final int TEXT_SIZE_LARGE			= 2;
	
	//view type
	public static final int VIEW_TYPE_LIST 			= 0;
	public static final int VIEW_TYPE_GRID  		= 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings_toolbar_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        
        final ActionBar actionBar = getSupportActionBar();
		if(actionBar != null) {
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
			actionBar.setTitle(getString(R.string.settings));
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		
		getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		applyTheme();
	}
	
	public void applyTheme() {
		final ActionBar actionBar = getSupportActionBar();
		if(actionBar != null) {
			actionBar.setBackgroundDrawable(new ColorDrawable(ThemeUtils.getPrimaryColor()));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				getWindow().setStatusBarColor(ThemeUtils.getDarkPrimaryColor());
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return true;
	}
}
