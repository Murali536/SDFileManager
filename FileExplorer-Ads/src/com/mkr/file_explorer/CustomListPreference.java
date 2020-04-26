package com.mkr.file_explorer;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.View;

public class CustomListPreference extends ListPreference {

	private AlertDialog.Builder mBuilder;
	private Context context;

	public CustomListPreference(Context context) {
		super(context);
		this.context = context;
	}

	public CustomListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	@Override
	protected void showDialog(Bundle state) {
		mBuilder = new AlertDialog.Builder(context);
		mBuilder.setTitle(getTitle());
		mBuilder.setIcon(getDialogIcon());
		
		mBuilder.setNegativeButton(getNegativeButtonText(), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		int index = (( ListPreference) this).findIndexOfValue(getValue());
		
		mBuilder.setSingleChoiceItems(getEntries(), index, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				dialog.dismiss();

				if (which >= 0 && getEntryValues() != null) {
					String value = getEntryValues()[which].toString();
					if (callChangeListener(value))
						setValue(value);
				}
				
			}
		});

		final View contentView = onCreateDialogView();
		if (contentView != null) {
			onBindDialogView(contentView);
			mBuilder.setView(contentView);
		}
		else
			mBuilder.setMessage(getDialogMessage());

		mBuilder.show();
	}
}
