package com.mkr.file_explorer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements android.view.View.OnClickListener {

	private Context mContext;
	private int mHelpTutorialPage = 0;
	
	private ImageView mThemeImageViews[];
			
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.prefs);
		
		mContext = getActivity();
		
		findPreference("about_msg").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				displayAboutDialog();
				return false;
			}
		});
		
		findPreference("help_me").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				displayHelpDialog();
				return true;
			}
		});
		
		findPreference("reset_home").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				PreferenceManager.getDefaultSharedPreferences(mContext).edit().remove(Settings.PREF_DEF_HOME_FOLDER).commit();
				Toast.makeText(mContext, "Default home path set", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		findPreference("selected_theme").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				displayThemesDialog();
				return false;
			}
		});
	}
	
	private void displayThemesDialog() {
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("Select theme");
		
		int alreadySelectedTheme = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(ThemeUtils.PREF_SELECTED_THEME, 0);
				
		final TableLayout tableView = (TableLayout) View.inflate(mContext, R.layout.themes_layout, null);
		
		final Resources res = mContext.getResources();
		int primaryColors[] = {
				res.getColor(R.color.actionbar_bg_0),
				res.getColor(R.color.actionbar_bg_1), 
				res.getColor(R.color.actionbar_bg_2),
				res.getColor(R.color.actionbar_bg_3),
				res.getColor(R.color.actionbar_bg_4), 
				res.getColor(R.color.actionbar_bg_5)};
		
		mThemeImageViews = new ImageView[] {
				(ImageView)((LinearLayout)((TableRow)tableView.findViewById(R.id.tbr_1)).findViewById(R.id.tbr_ll_1)).findViewById(R.id.theme_1),
				(ImageView)((LinearLayout)((TableRow)tableView.findViewById(R.id.tbr_1)).findViewById(R.id.tbr_ll_2)).findViewById(R.id.theme_2),
				(ImageView)((LinearLayout)((TableRow)tableView.findViewById(R.id.tbr_1)).findViewById(R.id.tbr_ll_3)).findViewById(R.id.theme_3),
				(ImageView)((LinearLayout)((TableRow)tableView.findViewById(R.id.tbr_2)).findViewById(R.id.tbr_ll_4)).findViewById(R.id.theme_4),
				(ImageView)((LinearLayout)((TableRow)tableView.findViewById(R.id.tbr_2)).findViewById(R.id.tbr_ll_5)).findViewById(R.id.theme_5),
				(ImageView)((LinearLayout)((TableRow)tableView.findViewById(R.id.tbr_2)).findViewById(R.id.tbr_ll_6)).findViewById(R.id.theme_6),
		};
		
		for (int i = 0; i < mThemeImageViews.length; i++) {
		    final LayerDrawable bgDrawable = (LayerDrawable) mThemeImageViews[i].getBackground();
			final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
			shape.setColor(primaryColors[i]);
			
			if(i == alreadySelectedTheme) {
				mThemeImageViews[i].setImageResource(R.drawable.ic_check_white_24dp);
			} else {
				mThemeImageViews[i].setImageResource(0);
			}
			
			mThemeImageViews[i].setContentDescription(""+i);
			mThemeImageViews[i].setOnClickListener(this);
		}
		
		builder.setView(tableView);
		
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		
		builder.create().show();
	}
	
	private void displayHelpDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.help_tutorial);

		final ScrollView scrollView = new ScrollView(mContext);

		final int maxPages = 8;
		mHelpTutorialPage = 0;

		final TextView propertiesMsg = new TextView(mContext); 
		propertiesMsg.setMinLines(9);
		propertiesMsg.setGravity(Gravity.CENTER_VERTICAL);
		propertiesMsg.setPadding(15, 15, 0, 0);
		propertiesMsg.setTextSize(18);

		scrollView.addView(propertiesMsg);
		builder.setView(scrollView);

		builder.setPositiveButton(getString(R.string.next), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		builder.setNegativeButton(getString(R.string.prev), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		
		builder.setNeutralButton(getString(R.string.close), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mHelpTutorialPage  = -1;
			}
		});

		final AlertDialog mHeplpTutorialAlertDialog = builder.create();
		mHeplpTutorialAlertDialog.show();

		//propertiesMsg.setText(helpTutorialPages(mHeplpTutorialAlertDialog, propertiesMsg),  BufferType.SPANNABLE);
		helpTutorialPages(mHeplpTutorialAlertDialog, propertiesMsg); 
		
		View nextButton = mHeplpTutorialAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				mHelpTutorialPage ++;
				
				//propertiesMsg.setText(helpTutorialPages(mHeplpTutorialAlertDialog, propertiesMsg),  BufferType.SPANNABLE);
				helpTutorialPages(mHeplpTutorialAlertDialog, propertiesMsg);
				
				if(mHelpTutorialPage == maxPages - 1) {
					mHeplpTutorialAlertDialog.getButton(Dialog.BUTTON_POSITIVE).setClickable(false);
					mHeplpTutorialAlertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
				} else {
					mHeplpTutorialAlertDialog.getButton(Dialog.BUTTON_POSITIVE).setClickable(true);
					mHeplpTutorialAlertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
				}

				if(mHelpTutorialPage > 1) {
					mHeplpTutorialAlertDialog.getButton(Dialog.BUTTON_NEGATIVE).setClickable(true);
					mHeplpTutorialAlertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(true);
				}
			}
		});
		
		View prevButton = mHeplpTutorialAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		prevButton.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				mHelpTutorialPage --;
				
				//propertiesMsg.setText(helpTutorialPages(mHeplpTutorialAlertDialog, propertiesMsg),  BufferType.SPANNABLE);
				helpTutorialPages(mHeplpTutorialAlertDialog, propertiesMsg);
				
				if(mHelpTutorialPage == 0) {
					mHeplpTutorialAlertDialog.getButton(Dialog.BUTTON_NEGATIVE).setClickable(false);
					mHeplpTutorialAlertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
				} else {
					mHeplpTutorialAlertDialog.getButton(Dialog.BUTTON_NEGATIVE).setClickable(true);
					mHeplpTutorialAlertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(true);
				}

				if(mHelpTutorialPage == maxPages - 2) {
					mHeplpTutorialAlertDialog.getButton(Dialog.BUTTON_POSITIVE).setClickable(true);
					mHeplpTutorialAlertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
				}
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	private SpannableStringBuilder helpTutorialPages(final AlertDialog mHeplpTutorialAlertDialog, final TextView propertiesMsg) {
		SpannableStringBuilder msgToDisplay = null;
		String title = "Help";
		switch (mHelpTutorialPage) {
		case 0:
			title  = "Options";
			msgToDisplay = new SpannableStringBuilder("Long press on any file or folder to access options like cut, copy, delete, rename etc.."+
					"\n\t After selecting copy/cut, a paste icon will be displayed in the tool bar, navigate to any folder and tap it"+
					" to paste the copied files/folders. \n");
			break;
		case 1:
			title  = "Folder Navigation";
			msgToDisplay = new SpannableStringBuilder("Tap on any folder to view its sub folders and files. \n To navigate back, press the device back key or "
					+ " select a folder in the navigation bar, which is above the items list or select the \"Back\" option in the tool bar.");
			break;
		case 2:
			final Bitmap multi = BitmapFactory.decodeResource( getResources(), R.drawable.multi);
			title  = "Multi-select";
			msgToDisplay = new SpannableStringBuilder( "To select multiple files at once, press \"  \" icon in the tool bar." +
					"\n\t To select all the files, tap on \"Select All \" option. To cancel the multi-select, press device back key or tap on \"cancel\" option in the tool bar.");
			msgToDisplay.setSpan(new ImageSpan( multi ), 42, 43, Spannable.SPAN_INCLUSIVE_INCLUSIVE );
			break;
		case 3:
			final Bitmap smiley = BitmapFactory.decodeResource( getResources(), R.drawable.expand );
			title  = "Launch side panel";
			msgToDisplay = new SpannableStringBuilder( "Press \" \" icon in the tool bar(top-left of the screen) or swype from left-edge of the screen "+
					"to right to launch the side panel. " +
					"\n\t Access applications manager, settings and sdcard tools from the side panel. Press device back key to close the side panel.");
			msgToDisplay.setSpan(new ImageSpan( smiley ), 7, 8, Spannable.SPAN_INCLUSIVE_INCLUSIVE );
			break;
		case 4:
			title  = "Application Manager";
			msgToDisplay = new SpannableStringBuilder("Launch, Uninstall and Backup applications through Application manager. All the backuped "+
						"app are saved in the \"Backup\" folder of your SDCard.");
			break;
		case 5:
			title  = "SD Card Usage";
			msgToDisplay = new SpannableStringBuilder("Check your sd card details like total sd card size, used space and available space. You can also view the space comsumed by "
					+ "Pictures, audio, video and other files in your sdcard. ");
			break;
		case 6:
			title  = "Settings";
			msgToDisplay = new SpannableStringBuilder("Change application settings like text size, sort files/folders, display thumbnails, display hidden folders etc...");
			break;
		case 7:
			final Bitmap fav = BitmapFactory.decodeResource( getResources(), R.drawable.favourites);
			title  = "Favourites";
			msgToDisplay = new SpannableStringBuilder("To create shortcuts to files/folder, long press a item and select \"Mark as Favourite\" options"
					+"\n To view all the favourite items select \" \" in the tool bar. To delete a favourite item, long press on any item display in the list. ");
			msgToDisplay.setSpan(new ImageSpan( fav ), 135, 136, Spannable.SPAN_INCLUSIVE_INCLUSIVE );
			break;
		default:
			break;
		}
		
		mHeplpTutorialAlertDialog.setTitle(title);
		propertiesMsg.setText(msgToDisplay, BufferType.SPANNABLE);
		return msgToDisplay;
	}
	
	/**
	 * display the about dialog in settings
	 */
	private void displayAboutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(getString(R.string.app_name));
		
		View view = View.inflate(mContext, R.layout.about, null);
		builder.setView(view);
		
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		builder.create().show();
	}

	@Override
	public void onClick(View v) {
		int selectedTheme = Integer.parseInt(v.getContentDescription().toString());
		ThemeUtils.getInstance(mContext).populateColors(selectedTheme);
		
		for (int i = 0; i < mThemeImageViews.length; i++) {
			if(i == selectedTheme) {
				mThemeImageViews[i].setImageResource(R.drawable.ic_check_white_24dp);
			} else {
				mThemeImageViews[i].setImageResource(0);
			}
		}
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(ThemeUtils.PREF_SELECTED_THEME, selectedTheme).commit();
		
		final Settings settings = (Settings) getActivity();
		if(settings != null) {
			settings.applyTheme();
		}
	}
	
}
