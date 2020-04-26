package com.mkr.file_explorer;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * this class is used to get all the file/folder details like file size, 
 * modified date etc..
 * 
 * @author Manthena Murali
 *
 */
public class ItemDetails {

	/**
	 * single ton object
	 */
	private static ItemDetails mItemDetails;

	private Context mContext;
	
	private static DateFormat dateFormat;
	private static Date date;

	public static ItemDetails getInstance() {
		if(mItemDetails == null) {
			mItemDetails = new ItemDetails();
			//dateFormat = new SimpleDateFormat("MMM dd, yy HH:mm");
			dateFormat = new SimpleDateFormat("MMM dd, yyyy");
			date  = new Date();
		}
		return mItemDetails;
	}

	public void setContext(Context context) {
		mContext = context;
	}

	/**
	 * get all the file details in a string format
	 * 
	 * @param file
	 * @return
	 */
	public String fileDetails(File file) {
		StringBuffer sb = new StringBuffer();
		if(file.isDirectory()) {
			sb.append(" Name : " + file.getName() + "\n");
			sb.append(" Path : "+ file.getParent() + "\n");
			sb.append("\n");

			File[] fArr = file.listFiles();
			if(fArr != null) {
				int totalCount = fArr.length;
				int foldersInside = 0;
				for (File temp : fArr) {
					if(!file.isHidden()) {
						if(temp.isDirectory()) {
							foldersInside ++;
						}	
					}
				}

				sb.append(" Contains : " + foldersInside + " folder(s), "+(totalCount - foldersInside) + " file(s)"+ "\n");
				sb.append(" Modified : " + getModifiedDate(file)+"\n");
				sb.append(" Can Read : " + file.canRead()+ "\n");
				sb.append(" Can Write : " + file.canWrite());
			}
		} else {
			sb.append(" Name : " + file.getName() + "\n");
			sb.append(" Path : "+ file.getParent() + "\n");
			sb.append(" Size : "+ readableFileSize(file.length()) + " ("+file.length() +" bytes)" + "\n");
			sb.append("\n");
			sb.append(" Modified : " + getModifiedDate(file)+"\n");
			sb.append(" Can Read : " + file.canRead()+ "\n");
			sb.append(" Can Write : " + file.canWrite());
		}

		return sb.toString();
	}

	public void fileDetails(File file, final TableLayout tableLayout) {
		((TextView)tableLayout.findViewById(R.id.properties_row_item_name)).setText(file.getName());
		((TextView)tableLayout.findViewById(R.id.properties_row_item_path)).setText(file.getParent());
		((TextView)tableLayout.findViewById(R.id.properties_row_item_modified)).setText(getModifiedDate(file));
		((TextView)tableLayout.findViewById(R.id.properties_row_item_can_read)).setText(""+file.canRead());
		((TextView)tableLayout.findViewById(R.id.properties_row_item_can_write)).setText(""+file.canWrite());

		if(file.isDirectory()) {
			((TextView)tableLayout.findViewById(R.id.properties_row_item_size)).setText("  -");

			File[] fArr = file.listFiles();
			if(fArr != null) {
				int totalCount = fArr.length;
				int foldersInside = 0;
				for (File temp : fArr) {
					if(!file.isHidden()) {
						if(temp.isDirectory()) {
							foldersInside ++;
						}	
					}
				}

				((TextView)tableLayout.findViewById(R.id.properties_row_item_contains)).setText(foldersInside + " folder(s), "+(totalCount - foldersInside) + " file(s)");
			}
		} else {
			((TextView)tableLayout.findViewById(R.id.properties_row_item_size)).setText(readableFileSize(file.length()) + " ("+file.length() +" bytes)");
			((TextView)tableLayout.findViewById(R.id.properties_row_item_contains)).setText("  -");
		}
	}
	
	/**
	 * Construct the file details in a string format for multiple files
	 * 
	 * @param selectedFilePaths file paths that are multi selected
	 * @return details in string format
	 */
	public String multipleFileDetails(ArrayList<String> selectedFilePaths) {
		StringBuffer sb = new StringBuffer();
		File file = new File(selectedFilePaths.get(0));
		sb.append("\n");
		sb.append(" Contains : " + selectedFilePaths.size() + " Items\n");
		if(file != null && file.exists()) {
			sb.append(" Path : "+ file.getParent() + "\n");
		}
		return sb.toString();
	}
	
	
	public void multipleFileDetails(ArrayList<String> selectedFilePaths, final TableLayout tableLayout) {
		
		File file = new File(selectedFilePaths.get(0));
		
		((TableRow)tableLayout.findViewById(R.id.tableRow1)).setVisibility(View.GONE);
		((TableRow)tableLayout.findViewById(R.id.tableRow3)).setVisibility(View.GONE);
		((TableRow)tableLayout.findViewById(R.id.tableRow4)).setVisibility(View.GONE);
		((TableRow)tableLayout.findViewById(R.id.tableRow6)).setVisibility(View.GONE);
		((TableRow)tableLayout.findViewById(R.id.tableRow7)).setVisibility(View.GONE);
		((TableRow)tableLayout.findViewById(R.id.tableRow8)).setVisibility(View.GONE);
		
		((TextView)tableLayout.findViewById(R.id.properties_row_item_path)).setText(file.getParent());
		((TextView)tableLayout.findViewById(R.id.properties_row_item_contains)).setText(""+selectedFilePaths.size());
	}
	
	/**
	 * return the file size in a readable format like KB, MB etc..
	 * @param fileSize
	 * @return
	 */
	public String readableFileSize(long fileSize) {
		return Formatter.formatFileSize(mContext, fileSize);
	}

	/**
	 * Get the modified date for the file
	 * 
	 * @param file
	 * @return modified date
	 */
	public String getModifiedDate(final File file) {
		date.setTime(file.lastModified());		
		return dateFormat.format(date);
	}
}
