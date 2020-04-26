package com.mkr.file_explorer;

import android.content.Context;
import android.graphics.Typeface;

public class Utils {

	public static Utils mUtils;
	private Typeface mRobotoSlabRegular;
	
	private Utils() { }
	
	public static Utils getInstance() {
		if(mUtils == null) {
			mUtils = new Utils();
		}
		return mUtils;
	}
	
	public void init(Context context) {
		mRobotoSlabRegular = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoSlab/Roboto-Regular.ttf");
	}
	
	public Typeface getDisplayTypeface() {
		return mRobotoSlabRegular;
	}
}
