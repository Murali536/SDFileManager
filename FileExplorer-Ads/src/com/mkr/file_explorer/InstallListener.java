package com.mkr.file_explorer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * this class listens whether the application has been upgraded
 * 
 * @author Manthena Murali
 *
 */
public class InstallListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(Intent.ACTION_PACKAGE_REPLACED.equalsIgnoreCase(action)) {
			/**
			 * if the app has been upgraded update the first launch shared pref
			 * which displays the change log dialog 
			 */
			if(intent.getDataString().contains(context.getPackageName())) {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
				preferences.edit().putBoolean(Settings.PREF_FIRST_LAUNCH, true).commit();
			}
		}
	}

}
