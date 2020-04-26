package com.mkr.file_explorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter which displayed all the files and folders
 * 
 * @author Manthena Murali
 */
public class ItemsAdapter extends BaseAdapter{

	private LayoutInflater mInflater;
	private PackageManager mPackageManager;
	
	private ItemDetails mItemDetails;
	private ItemOperations mItemOperations;

	private Resources mRes;
	
	/**
	 * current folder items that are to be displayed in the list
	 */
	private List<File> mItemsToDisplay;
	
	/**
	 * List holds all the files and folders that are selected for multi operation. 
	 */
	public static ArrayList<String> mMultipleSelectFiles;
	
	/**
	 * this flag is used to lazy load the thumbnails. Once the user has stopped scrolling the list
	 * only then the thumbnails are loaded
	 */
	private boolean mScrollingStarted = false;
	
	/**
	 * default options for creating thumbnails
	 */
	private BitmapFactory.Options mBitmapFactoryOptions;
	
	/**
	 * text size that will be used only for grid layout
	 */
	private int mGridTextSize;
	
	private LayerDrawable mCircleDrawable;
	
	public ItemsAdapter(Context context, ItemOperations itemOperations) {
		
		mRes = context.getResources();
		
		//DPI = Math.max(1, mRes.getDisplayMetrics().density);
		
		//load all the file extensions and their drawables
		new FileExtensions(context);
		
		float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		mGridTextSize = (int) (context.getResources().getDimension(R.dimen.item_name_size_very_small)/scaledDensity);
		
		mInflater = LayoutInflater.from(context);
		mPackageManager = context.getPackageManager();
		
		mItemDetails = ItemDetails.getInstance();
		mItemDetails.setContext(context);
		
		mItemOperations = itemOperations;
		
		mMultipleSelectFiles = new ArrayList<String>();

		/*
		 * load the default bitmap factory settings
		 */
		mBitmapFactoryOptions = new BitmapFactory.Options();
		mBitmapFactoryOptions.inJustDecodeBounds = false;
		mBitmapFactoryOptions.outWidth = 0;
		mBitmapFactoryOptions.outHeight = 0;
		mBitmapFactoryOptions.inSampleSize = 4;
		
		//load the items for the very first time with home folder
		updateItems(PreferenceManager.getDefaultSharedPreferences(context).getString(Settings.PREF_DEF_HOME_FOLDER, 
				Environment.getExternalStorageDirectory().getPath()+"/"));
		createCircleBG();
	}

	public void createCircleBG() {
		mCircleDrawable = (LayerDrawable) mRes.getDrawable(R.drawable.circle_bg);
		final GradientDrawable shape = (GradientDrawable) mCircleDrawable.findDrawableByLayerId(R.id.shape_id);
		shape.setColor(ThemeUtils.getPrimaryColor());
	}
	
	public int getCount() {
		if(mItemsToDisplay != null && mItemsToDisplay.size() > 0) {
			return mItemsToDisplay.size(); 
		}
		return 0;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolderList holderList;
		if(convertView == null) {
			if(FileExpActivity.mViewTypeOption == Settings.VIEW_TYPE_LIST) {
				convertView = mInflater.inflate(R.layout.explorer_list_item, null);
			} else {
				convertView = mInflater.inflate(R.layout.explorer_grid_item, null);
			}

			holderList = new ViewHolderList();
			holderList.itemIcon = (ImageView)convertView.findViewById(R.id.item_icon);
			holderList.itemName = (TextView)convertView.findViewById(R.id.item_name);
			holderList.itemSize = (TextView) convertView.findViewById(R.id.item_size);
			holderList.checkbox = (CheckBox)convertView.findViewById(R.id.item_checkbox);
			holderList.itemModifiedDate = (TextView) convertView.findViewById(R.id.item_modified_Date);

			holderList.itemName.setTypeface(Utils.getInstance().getDisplayTypeface());
			holderList.itemSize.setTypeface(Utils.getInstance().getDisplayTypeface());
			holderList.itemModifiedDate.setTypeface(Utils.getInstance().getDisplayTypeface());
			
			if(!FileExpActivity.mIsMultipleOptionSelected) {
				//holderList.itemName.setCheckMarkDrawable(new ColorDrawable(Color.TRANSPARENT));
				holderList.checkbox.setVisibility(View.GONE);
			}

			convertView.setTag(holderList);
		} else {
			holderList = (ViewHolderList) convertView.getTag();
		} 

		File file = mItemsToDisplay.get(position);

		holderList.itemName.setText(file.getName());
		//file path is set as content description, to use for other operations in the activity
		holderList.itemName.setContentDescription(file.getPath());
		
		if(FileExpActivity.mDisplayModifiedDate) {
			if(holderList.itemModifiedDate.getVisibility() != View.VISIBLE) {
				holderList.itemModifiedDate.setVisibility(View.VISIBLE);
			}
			//display modified date for items based on the user settings
			holderList.itemModifiedDate.setText(mItemDetails.getModifiedDate(file));
		} else {
			holderList.itemModifiedDate.setVisibility(View.INVISIBLE);
		}
		
		if(FileExpActivity.mViewTypeOption == Settings.VIEW_TYPE_LIST) {
			//set text size based on the user preference
			holderList.itemName.setTextSize(FileExpActivity.mDisplayItemTextSize);
		} else {
			//for grid alone set separate text size
			holderList.itemName.setTextSize(mGridTextSize);
		}

		if(file.isDirectory()) {
			holderList.isDirectory = true;
			holderList.itemIcon.setImageResource(R.drawable.ic_doc_folder);
			holderList.itemIcon.setBackground(mCircleDrawable);
			holderList.itemSize.setText(" ");
			if(FileExpActivity.mViewTypeOption == Settings.VIEW_TYPE_LIST) {
				//holderList.itemName.setPadding((int)(53 * DPI), 0, (int)(10 * DPI), 0);
			}
		} else {
			if(FileExpActivity.mViewTypeOption == Settings.VIEW_TYPE_LIST) {
				//holderList.itemName.setPadding((int)(53 * DPI), 0, (int)(10 * DPI), (int)(14 * DPI));
			}
			holderList.isDirectory = false;
			holderList.itemSize.setText(mItemDetails.readableFileSize(file.length()));
			String extension = ItemOperations.getFileExtension(file.getName());
			holderList.extension = extension;
			if(extension != null && extension.length() > 0) {
				Integer extensionType = FileExtensions.mThumbnailsList.get(extension);
				if(extensionType != null) {
					holderList.extensionType = extensionType;
				}
			}
			try {
				//if still in scrolling dont update the drawables, only update once scolling is stopped
				if(FileExpActivity.mDisplayThumbs && extension != null &&
						!mScrollingStarted && FileExtensions.mThumbnailsList.containsKey(extension)) {
					updateItemsForThumbnails(file, holderList, extension);
				} else {
					if(FileExpActivity.mThumbnailCache.containsKey(file.getAbsolutePath())) {
						final Bitmap bmp = FileExpActivity.mThumbnailCache.get((file.getAbsolutePath()));
						if(bmp != null) {
							holderList.itemIcon.setImageDrawable(null);
							holderList.itemIcon.setBackground(null);
							holderList.itemIcon.setBackground(new BitmapDrawable(mRes, bmp));
						} else {
							displaypredefinedDrawables(extension, holderList);
						}
					} else {
						displaypredefinedDrawables(extension, holderList);
					}
				}

			} catch (Exception e) {
				//e.printStackTrace();
				//safe check load the default icon
				holderList.itemIcon.setImageResource(R.drawable.ic_doc_generic_am);
			}
		}
		
		//if a file/folder is marked as multiple change the text color
		if(FileExpActivity.mIsMultipleOptionSelected) {
			holderList.checkbox.setVisibility(View.VISIBLE);
			if(mMultipleSelectFiles != null && mMultipleSelectFiles.contains(file.getPath())) {
				final int textColor = ThemeUtils.getPrimaryColor();
				//holderList.itemName.setCheckMarkDrawable(android.R.drawable.checkbox_on_background);
				holderList.checkbox.setChecked(true);
				holderList.itemName.setTextColor(textColor);
				holderList.itemSize.setTextColor(textColor);
				holderList.itemModifiedDate.setTextColor(textColor);
			} else {
				final int textColor = mRes.getColor(R.color.file_folder_name_text_color);
				//holderList.itemName.setCheckMarkDrawable(android.R.drawable.checkbox_off_background);
				holderList.checkbox.setChecked(false);
				holderList.itemName.setTextColor(textColor);
				holderList.itemSize.setTextColor(textColor);
				holderList.itemModifiedDate.setTextColor(textColor);
			}
		}
		
		return convertView;
	}

	private void displaypredefinedDrawables(String extension, ViewHolderList holderList) {
		holderList.itemIcon.setBackground(mCircleDrawable);
		if(extension == null) {
			holderList.itemIcon.setImageResource(R.drawable.ic_doc_generic_am);
		} else {
			//get the resource id from the already loaded list. if not found in the list just load the default file
			//icon
			Integer resId = FileExtensions.mExtensionDrawables.get(extension);
			if(resId != null) {
				holderList.itemIcon.setImageResource(resId);
			} else {
				holderList.itemIcon.setImageResource(R.drawable.ic_doc_generic_am);
			}
		}
	}
	
	/**
	 * load thumbnails 
	 * 
	 * @param file file for which thumbnail has to be loaded
	 * @param holderList
	 * @param extension file extension
	 */
	private void updateItemsForThumbnails(File file, ViewHolderList holderList, String extension) {
		if(FileExpActivity.mThumbnailCache.containsKey(file.getAbsolutePath())) {
			final Bitmap bmp = FileExpActivity.mThumbnailCache.get((file.getAbsolutePath()));
			if(bmp != null) {
				holderList.itemIcon.setImageDrawable(null);
				holderList.itemIcon.setBackground(null);
				holderList.itemIcon.setBackground(new BitmapDrawable(mRes, bmp));
			} else {
				holderList.itemIcon.setImageResource(R.drawable.ic_doc_generic_am);
				holderList.itemIcon.setBackground(mCircleDrawable);
			}
		} else { 
			if(FileExtensions.EXTENSION_APK.equalsIgnoreCase(extension)) {
				Drawable d = getAPKDrawable(file.getPath());
				if(d != null) {
					holderList.itemIcon.setImageDrawable(null);
					holderList.itemIcon.setBackground(d);
				} else {
					holderList.itemIcon.setImageResource(R.drawable.ic_doc_generic_am);
					holderList.itemIcon.setBackground(mCircleDrawable);
				}
			}/*else if("jpg".equals(extension) || "png".equals(extension)) {
				//for this don't set any image because this will be loaded after scrolling is done
			} */
		}
	}
	
	public Bitmap getThumbnailsForVideo(String path) {
		try {
			return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
		} catch(Exception e) {
			return null;
		}
	}
	
	
	/**
	 * display all items in the folder
	 * 
	 * @param currentPath the folder path in which the files and folders has to be displayed
	 * @return true if items has been displayed
	 */
	public boolean updateItems(String currentPath) {
		if(mItemsToDisplay != null) {
			mItemsToDisplay.clear();
		}

		mItemsToDisplay = mItemOperations.loadFiles(currentPath);
		if(mItemsToDisplay!= null && mItemsToDisplay.size() > 0) {
			notifyDataSetChanged();
			return true;
		}
		return false;
	}

	/**
	 * set true if the current folder was scrolled atleast once
	 * @param isScrolled
	 */
	public void scrollStarted(boolean isScrolled) {
		mScrollingStarted = isScrolled;
	}
	
	/**
	 * the current displaying items in the list
	 * @return
	 */
	public List<File> getCurrentDisplayingItems() {
		return mItemsToDisplay;
	}

	/**
	 * Get the application icon
	 * 
	 * @param path
	 * @return application launcher icon
	 */
	public Drawable getAPKDrawable(String path) {
  		 PackageInfo pInfo = mPackageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
  		 if (pInfo!=null) {
	   		 ApplicationInfo aInfo = pInfo.applicationInfo;
	   		 if(aInfo != null && android.os.Build.VERSION.SDK_INT >= 8){
	   			 aInfo.sourceDir = path;
	   			 aInfo.publicSourceDir = path;
	   		 }
	   		 return aInfo.loadIcon(mPackageManager);
  		 }
  		 return null;
	}
	
	/**
	 * get the thumbnail for file
	 *  
	 * @param filePath file path for which thumbnail has to be created
	 * @return thumbnail in bitmap format
	 */
	public Bitmap getThumbnails(String filePath) {
		try {
			return resizeBitmap(BitmapFactory.decodeFile(filePath, mBitmapFactoryOptions), 64, 64);
		}catch(Exception e) {
			//e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * resize the bitmap 
	 * 
	 * @param drawable
	 * @param desireWidth
	 * @param desireHeight
	 * 
	 * @return resized bitmap
	 */
	private Bitmap resizeBitmap(Bitmap drawable, int desireWidth, int desireHeight) {
		int width = drawable.getWidth();
		int height = drawable.getHeight();

		if (0 < width && 0 < height && desireWidth < width
				|| desireHeight < height) {
			// Calculate scale
			float scale;
			if (width < height) {
				scale = (float) desireHeight / (float) height;
				if (desireWidth < width * scale) {
					scale = (float) desireWidth / (float) width;
				}
			} else {
				scale = (float) desireWidth / (float) width;
			}

			// Draw resized image
			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			Bitmap bitmap = Bitmap.createBitmap(drawable, 0, 0, width, height,
					matrix, true);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawBitmap(bitmap, 0, 0, null);

			drawable = bitmap;
		}

		return drawable;
	}
	
	static class ViewHolderList {
		ImageView itemIcon;
		TextView itemName;
		TextView itemSize;
		CheckBox checkbox;
		TextView itemModifiedDate;
		boolean isDirectory;
		String extension;
		int extensionType = -1;
	}
}
