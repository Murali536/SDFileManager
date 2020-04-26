package com.mkr.file_explorer;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public class ThemeUtils {

	private static ThemeUtils mThemeUtils;
	public static final String PREF_SELECTED_THEME = "selected_theme";

	private static Context mCon;
	
	private static int primaryColor;
	private static int darkPrimaryColor;
	
	private ThemeUtils() { }
	
	public static ThemeUtils getInstance(final Context con) {
		if(mThemeUtils == null) {
			mThemeUtils = new ThemeUtils();
			mCon = con;
		}
		return mThemeUtils;
	}
	
	public void populateColors(int theme) {
		int selectedTheme;
		if(theme == -1) {
			selectedTheme = PreferenceManager.getDefaultSharedPreferences(mCon).getInt(ThemeUtils.PREF_SELECTED_THEME, 0);
		} else {
			selectedTheme = theme;
		}
		Resources res = mCon.getResources();
		switch (selectedTheme) {
		case 0:
			primaryColor = res.getColor(R.color.actionbar_bg_0);	
			darkPrimaryColor = res.getColor(R.color.actionbar_bg_dark_0);
			break;
		case 1:
			primaryColor = res.getColor(R.color.actionbar_bg_1);
			darkPrimaryColor = res.getColor(R.color.actionbar_bg_dark_1);
			break;
		case 2:
			primaryColor = res.getColor(R.color.actionbar_bg_2);
			darkPrimaryColor = res.getColor(R.color.actionbar_bg_dark_2);
			break;
		case 3:
			primaryColor = res.getColor(R.color.actionbar_bg_3);
			darkPrimaryColor = res.getColor(R.color.actionbar_bg_dark_3);
			break;
		case 4:
			primaryColor = res.getColor(R.color.actionbar_bg_4);
			darkPrimaryColor = res.getColor(R.color.actionbar_bg_dark_4);
			break;
		case 5:
			primaryColor = res.getColor(R.color.actionbar_bg_5);
			darkPrimaryColor = res.getColor(R.color.actionbar_bg_dark_5);
			break;
		default:
			primaryColor = res.getColor(R.color.actionbar_bg_0);
			darkPrimaryColor = res.getColor(R.color.actionbar_bg_dark_0);
			break;
		}
	}
	
	public static int getPrimaryColor() {
		return primaryColor;
	}
	
	public static int getDarkPrimaryColor() {
		return darkPrimaryColor;
	}
}
