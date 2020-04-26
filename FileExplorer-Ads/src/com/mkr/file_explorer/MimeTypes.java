package com.mkr.file_explorer;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.webkit.MimeTypeMap;

public class MimeTypes {

	public String getMimeType(String filename) {
		
		String extension = "."+ItemOperations.getFileExtension(filename);
		if (extension != null && extension.length() > 0) {
			String webkitMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));
			if (webkitMimeType != null) {
				return webkitMimeType;
			}
		}		
		return null;
	}
	
	public String getSpecificFormats(Context c, String ext)
	{
		String format = null;
		XmlResourceParser in = c.getResources().getXml(R.xml.mimetypes);
		XmlPullParser xpp = in;
		try {
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tag = xpp.getName();
				if (eventType == XmlPullParser.START_TAG) {
					if ("type".equals(tag)) {
						if(ext.equalsIgnoreCase(xpp.getAttributeValue(null, "extension"))){
							format = xpp.getAttributeValue(null, "format");
							break;
						}
					}
				} 
				eventType = xpp.next();
			}
		}catch(XmlPullParserException e) {
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}
		return format;
	}
}