package com.mkr.file_explorer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

/**
 * this class loads the file extensions and their respective drawables. 
 * file is stored in assests. 
 * 
 * @author Manthena Murali
 *
 */
public class FileExtensions {

	public static final String EXTENSION_APK = "apk";
	public static final String ZIP_EXTENSION = ".zip";
	
	public static final int FILE_ALL_TYPES = 0;
	public static final int FILE_TYPE_IMAGE = 1;
	public static final int FILE_TYPE_AUDIO = 2;
	public static final int FILE_TYPE_VIDEO = 3;
	public static final int FILE_TYPE_APK = 4;

	private Context mContext;
	
	/**
	 * Map holds extensions and their corresponding drawables. <br>
	 * key - File extensions <br>
	 * Value - drawable resource ID
	 */
	public static Map<String, Integer> mExtensionDrawables;
	/**
	 * map for which thumbnails can be created key is extension , value is file type
	 */
	public static Map<String, Integer> mThumbnailsList;
	
	public FileExtensions(Context context) {
		mContext = context;
		
		mThumbnailsList = new HashMap<String, Integer>();
		mExtensionDrawables = new HashMap<String, Integer>();
		
		/*
		 * load list for which thumbnails are to be created
		 */
		prepareThumnailList();

		/*
		 * start the async task
		 */
		PrepareExtensions extensions = new PrepareExtensions();
		extensions.execute();
	}
	
	
	private void prepareThumnailList() {
		mThumbnailsList.put("apk", FILE_TYPE_APK);
		mThumbnailsList.put("png", FILE_TYPE_IMAGE);
		mThumbnailsList.put("jpg", FILE_TYPE_IMAGE);
		mThumbnailsList.put("mp4", FILE_TYPE_VIDEO);
	}
	
	/**
	 * Parse the extensiondrawables.xml from assets and store the extensions(as key) and drawable resource ids(as value)
	 * in a map. While displaying the items in a folder(in ItemsAdapter.java) if the file extension is found in the map 
	 * then display the file icon based on the resource id. 
	 * 	  
	 * @author Manthena Murali
	 *
	 */
	public class PrepareExtensions extends AsyncTask<Void, Void, Void> {

		private final String EXTENSION = "extension";
		private final String DRAWABLE  = "drawable";

		private Resources mRes;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mRes = mContext.getResources();
		}

		@Override
		protected Void doInBackground(Void... params) {
			InputStream inputStream = null;
			try {
				inputStream = mContext.getAssets().open("extensiondrawables.xml");
				XmlPullParserFactory xmlfactory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = xmlfactory.newPullParser();
				parser.setInput(inputStream, null);
				int eventType = parser.getEventType();
				boolean parsingDone = false;
				do {
					if(eventType == XmlPullParser.START_TAG) {
						String extension = parser.getAttributeValue(null, EXTENSION);
						String drawable = parser.getAttributeValue(null, DRAWABLE);
						if(drawable != null && extension != null) {
							int resID = mRes.getIdentifier(drawable, DRAWABLE, mContext.getPackageName());
							mExtensionDrawables.put(extension, resID);
						}
					}
					eventType = parser.next();
				} while (!parsingDone && eventType != XmlPullParser.END_DOCUMENT);

			}catch (Exception e) {
				//e.printStackTrace();
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						//e.printStackTrace();
					}
				}
			}
			return null;
		}
	}
}
