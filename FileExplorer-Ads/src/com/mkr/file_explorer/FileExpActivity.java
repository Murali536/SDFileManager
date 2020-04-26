package com.mkr.file_explorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mkr.applicationmanager.ApplicationManager;
import com.mkr.file_explorer.ItemsAdapter.ViewHolderList;
import com.mkr.file_explorer.utils.Favourites;
import com.mkr.file_explorer.utils.ZipUnZip;

public class FileExpActivity extends AppCompatActivity implements OnItemClickListener, OnClickListener, OnSharedPreferenceChangeListener{

	public final String TAG = "FileExplorer Activity";

	private Context mContext;
	private Resources mResources;
	private SharedPreferences mSharedPref;
	
	//item options constants
	public static final int MENU_COPY 				= 0;
	public static final int MENU_CUT 				= 1;
	public static final int MENU_PASTE 		   	 	= 2;
	public static final int MENU_RENAME 			= 3;
	public static final int MENU_DELETE 			= 4;
	public static final int MENU_PROPERTIES 		= 5;
	public static final int MENU_OPEN 				= 6;
	public static final int MENU_SEND 				= 7;
	public static final int MENU_CREATE_NEW 		= 8;
	public static final int MENU_CREATE_NEW_FILE 	= 9;
	public static final int MENU_CREATE_NEW_FOLDER 	= 10;
	public static final int MENU_TASK_MANAGER 		= 11;
	public static final int MENU_SETTINGS 			= 12;
	public static final int MENU_FAVOURITE   		= 13;
	public static final int MENU_ZIP 				= 14;
	public static final int MENU_UNZIP 				= 15;
	public static final int MENU_EXIT 				= 16;
	public static final int MENU_SEARCH 			= 17;
	public static final int MENU_SDCARD_INFO 		= 18;

	public static final int WHATS_NEW_DIAG 		    = 99;
	
	public static final String BUNDLE_CURRENT_PATH = "current_path";
	public static final String DELIMITER = ";";
	
	private ItemOperations mItemOperations;
	private ItemsAdapter mItemsAdapter;
	private ItemDetails mItemDetails;
	private MimeTypes mMimeTypes;
	private Favourites mFavourites;
	
	/**
	 * This flag indicates whether user has selected cut or copy operation. 
	 * used for paste option  
	 */
	private int mSelectedCutCopyOption = -1; 
	
	/**
	 * the current folder being displayed
	 */
	private String mCurrentDisplayPath;
	
	/**
	 * default home path set by the user
	 */
	private String mDefaultHomePath;
	
	/**
	 * when multiple files are to be zipped, user is prompted to enter a name 
	 */
	private String mZipFileName;

	/**
	 * list view to display the items in a folder
	 */
	private ListView mListView;
	
	/**
	 * Grid view to display the items in a folder
	 */
	private GridView mGridView;
	
	/**
	 * the paste view. this is visible only when either cut or copy operation is selected
	 */
	private TextView mPasteView;
	
	/**
	 * parent scroll view in which all the options are displayed to the user
	 */
	private HorizontalScrollView mHSVPath;
	
	/**
	 * displays list of items
	 */
	private LinearLayout mDisplayItemsLinearLayout;
	/**
	 * normal layout which displays options like home, back, up, multiple et..
	 */
	private View mOptionsLayout;
	/**
	 * layout displayed when multiple option is selected in normal options 
	 */
	private View mMultipleOptionsLayout;

	/**
	 * File/Folder which is long pressed, upon long press options like cut, copy, rename are
	 * displayed based on the item type. 
	 */
	private File mLongPressedFile;

	/**
	 *  indicated whether multiple option is selected.  
	 */
	public static boolean mIsMultipleOptionSelected = false;

	/**
	 * counter to know how many time back key is pressed
	 */
	private int mBackPressCounter = -1;

	private int mSelectedSearchOption;
	
	private ViewFlipper mViewFlipper;
	private ProgressDialog mProgressDialog;

	/**
	 *  flag to indicate if the hidden files are to be displayed. options selected by user in settings
	 */
	public static boolean mDisplayHiddenFiles;
	/**
	 * flag to indicate if the modified date for files has to be displayed. options selected by user in settings
	 */
	public static boolean mDisplayModifiedDate;
	/**
	 * flag to indicate if thumbnails are to be displayed for files. options selected by user in settings
	 */
	public static boolean mDisplayThumbs;
	/**
	 * what text size should to applied for file/folder name. options selected by user in settings
	 */
	public static int mDisplayItemTextSize;
	/**
	 * how the files/folders should be sorted by. options selected by user in settings
	 */
	public static int mSortItemsBy;

	/**
	 * if the items should be displayed as list or as grid
	 */
	public static int mViewTypeOption;

	/**
	 * cache to store already displayed thumbnails, so that if the file is opened again no need to fetch again
	 */
	public static Map<String, Bitmap> mThumbnailCache;
	
	/**
	 * use this flag to lazy the items when the folder is clicked for first time
	 */
	private boolean isFolderClicked = false;
	
	/**
	 * this will store the path and the first visible item in that path
	 */
	private Map<String, Integer> mFirstVisibleItemInPathMap;

	private TextView mMultiCopy, mMultiCut, mMultiDelete;
	
	/**
	 * this flag indicates that the user has come from third party app to select a file.
	 * When this flag is set to true
	 * 1) when a file is selected return the uri of that file
	 * 2) when long pressed disable all options and return uri
	 */
	private boolean mShouldPickFile = false;
	
	/**
	 * these are used for the side panle drawer layout
	 */
	private DrawerLayout mDrawerLayout;
	/**
	 * toggler to open or close the side panel
	 */
	private ActionBarDrawerToggle mDrawerToggle;
	
	/**
	 * Layout inflater
	 */
	private LayoutInflater mLayoutInflater;
	
	/**
	 * this linear layout holds the items in the navigation bar
	 */
	private LinearLayout mFolderPathLinearLayout;
	
	private AdView mAdView;

	private ActionBar mActionBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.explorer_ui);
		ThemeUtils.getInstance(FileExpActivity.this).populateColors(-1);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
		}
		mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setHomeButtonEnabled(true);
		
		Utils.getInstance().init(FileExpActivity.this);
		
		mItemOperations = new ItemOperations();
		mContext = FileExpActivity.this;
		mResources = getResources();
		mMimeTypes = new MimeTypes();
		mItemDetails = ItemDetails.getInstance();

		mFirstVisibleItemInPathMap = new HashMap<String, Integer>();
		
		mShouldPickFile = false;
		final Intent intent = getIntent();
		if(intent != null) {
			final String action = intent.getAction();
			if(action != null && action.equalsIgnoreCase("android.intent.action.GET_CONTENT")) {
				mShouldPickFile = true;
				displayToast(getString(R.string.pick_file));
			}
		}
		
		/*
		 * initialize the list view that will display all the files and folder
		 */
		mListView = new ListView(FileExpActivity.this);
		mListView.setCacheColorHint(Color.TRANSPARENT);
		mListView.setFastScrollEnabled(true);
		mListView.setOnItemClickListener(this);
		mListView.setDivider(null);
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if(mShouldPickFile) {
					ViewHolderList holderList = (ViewHolderList) arg1.getTag();
					TextView textView = holderList.itemName;
					String path = textView.getContentDescription().toString();
					File file = new File(path);
					finishWithResult(file);
				} else {
					longPressOptions(arg1);
				}
				return true;
			}
		});

		/**
		 * initialize a grid view
		 */
		mGridView = new GridView(mContext);
		mGridView.setNumColumns(2);
		mGridView.setVerticalSpacing(3);
		mGridView.setFastScrollEnabled(true);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				mProgressDialog = ProgressDialog.show(FileExpActivity.this, "", getString(R.string.loading_msg), true);
				Message msg = Message.obtain();
				msg.obj = arg1;
				itemClickHandler.sendMessage(msg);
			}
		});
		
		mGridView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if(mShouldPickFile) {
					ViewHolderList holderList = (ViewHolderList) arg1.getTag();
					TextView textView = holderList.itemName;
					String path = textView.getContentDescription().toString();
					File file = new File(path);
					finishWithResult(file);
				} else {
					longPressOptions(arg1);
				}
				return true;
			}
		});
		
		mThumbnailCache = new HashMap<String, Bitmap>();
		
		mHSVPath = (HorizontalScrollView) findViewById(R.id.path_scroll);
		mHSVPath.setHorizontalScrollBarEnabled(false);
		mHSVPath.setHorizontalFadingEdgeEnabled(false);
		
		mSharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		setTextSize();
		
		mSharedPref.registerOnSharedPreferenceChangeListener(this);
		
		mDisplayItemsLinearLayout = (LinearLayout) findViewById(R.id.explorer_display_items);
		mViewTypeOption = Integer.parseInt(mSharedPref.getString(Settings.PREF_VIEW_TYPE, ""+Settings.VIEW_TYPE_LIST));
		if(mViewTypeOption == Settings.VIEW_TYPE_LIST) {
			mDisplayItemsLinearLayout.addView(mListView);
		} else {
			mDisplayItemsLinearLayout.addView(mGridView);
		}

		mFolderPathLinearLayout = (LinearLayout) findViewById(R.id.path_scroll_linearLayout);
		mFolderPathLinearLayout.setFocusableInTouchMode(true);
		
		//load the default preferences
		mDisplayHiddenFiles = mSharedPref.getBoolean(Settings.PREF_DISPLAY_HIDDEN_FILES, false); 
		mDisplayModifiedDate = mSharedPref.getBoolean(Settings.PREF_DISPLAY_MODIFIED_DATE, true);
		mDisplayThumbs = mSharedPref.getBoolean(Settings.PREF_DISPLAY_THUMBNAILS, true);
		
		mSortItemsBy = Integer.parseInt(mSharedPref.getString(Settings.PREF_SORT_ITEMS_BY, ""+Settings.SORT_BY_NAME));

		//load the layouts
		inflateLayouts();
		createSidePane();
		mLayoutInflater = (LayoutInflater) getSystemService(Service.LAYOUT_INFLATER_SERVICE);

		mFavourites = new Favourites(mContext);

		if(savedInstanceState != null) {
			mCurrentDisplayPath = savedInstanceState.getString(BUNDLE_CURRENT_PATH);
		} else {
			mCurrentDisplayPath = mSharedPref.getString(Settings.PREF_DEF_HOME_FOLDER, getExternalStoragePath());
		}
		
		mDefaultHomePath = mSharedPref.getString(Settings.PREF_DEF_HOME_FOLDER, getExternalStoragePath());
		mItemsAdapter = new ItemsAdapter(mContext, mItemOperations);
		mListView.setAdapter(mItemsAdapter);
		mGridView.setAdapter(mItemsAdapter);
		displayItems(mCurrentDisplayPath);

		/**
		 * this listener loads the thumbnails for files once the user the list has stopped scrolling.
		 * since loading thumbnails taken more time and memory, instead of loading each and every time 
		 * load once the list has stopped. Also check the user settings for load thumbnails
		 */
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(mDisplayThumbs) {
					switch (scrollState) {
					case OnScrollListener.SCROLL_STATE_IDLE:
						mItemsAdapter.scrollStarted(false);
						updateItems(view);
						break;
					case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
						mItemsAdapter.scrollStarted(true);
						break;
					case OnScrollListener.SCROLL_STATE_FLING:
						mItemsAdapter.scrollStarted(true);
						break;
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(isFolderClicked) {
					if(mDisplayThumbs) {
						updateItems(view);
					}
					isFolderClicked = false;
				}
			}
		});
		
		
		mGridView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(mDisplayThumbs) {
					switch (scrollState) {
					case OnScrollListener.SCROLL_STATE_IDLE:
						mItemsAdapter.scrollStarted(false);
						updateItems(view);
						break;
					case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
						mItemsAdapter.scrollStarted(true);
						break;
					case OnScrollListener.SCROLL_STATE_FLING:
						mItemsAdapter.scrollStarted(true);
						break;
					}
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(isFolderClicked) {
					if(mDisplayThumbs) {
						updateItems(view);
					}
					isFolderClicked = false;
				}
			}
		});
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.string.drawer_open,
				R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
			}
		};
		
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();
		
		boolean shouldDispChangeLog = mSharedPref.getBoolean(Settings.PREF_FIRST_LAUNCH, true);
		if(shouldDispChangeLog) {
			mSharedPref.edit().putBoolean(Settings.PREF_FIRST_LAUNCH, false).commit();
			//showDialog(WHATS_NEW_DIAG);
			displayWatsNewDialog();
		}
		
		mAdView = (AdView)this.findViewById(R.id.adView);
	    final AdRequest adRequest = new AdRequest.Builder().build();
	    mAdView.loadAd(adRequest);
	    mAdView.setVisibility(View.GONE);
	    mAdView.setAdListener(new AdListener() {
	    	@Override
	    	public void onAdLoaded() {
	    		super.onAdLoaded();
	    		mAdView.setVisibility(View.VISIBLE);		
	    	}
		});
	    
	    applyTheme();
	}

	private void applyTheme() {
		mActionBar.setBackgroundDrawable(new ColorDrawable(ThemeUtils.getPrimaryColor()));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
	        getWindow().setStatusBarColor(ThemeUtils.getDarkPrimaryColor());
	        
	        ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(null, null, 
	        						ThemeUtils.getPrimaryColor());
	        setTaskDescription(td);
	    }
		findViewById(R.id.explorer_ui_parent_relative).setBackgroundColor(ThemeUtils.getPrimaryColor());
		
		//side panel header
		((LinearLayout) findViewById(R.id.side_pane_parent)).findViewById(R.id.side_pane_header)
						.setBackgroundColor(ThemeUtils.getPrimaryColor());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mAdView.resume();
		refreshList();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mAdView.pause();
	}
	
	/**
	 * load the item thumbnails after the list is drawn or list is idle
	 * @param view
	 */
	private void updateItems(AbsListView view) {
		try {
			int childCount = view.getChildCount();
			for(int i = 0; i < childCount; i++) {
				View v = view.getChildAt(i);
				if(v != null) {
					ViewHolderList holderList = (ViewHolderList) v.getTag();
					if(!holderList.isDirectory && FileExtensions.mThumbnailsList.containsKey(holderList.extension)) {
						String path = holderList.itemName.getContentDescription().toString();
						Bitmap bmp = mThumbnailCache.get(path);
						if(bmp == null) {
							if(holderList.extensionType == FileExtensions.FILE_TYPE_VIDEO) {
								bmp = mItemsAdapter.getThumbnailsForVideo(path);
							} else if(holderList.extensionType == FileExtensions.FILE_TYPE_APK) {
								final String extension = ItemOperations.getFileExtension(holderList.itemName.getText().toString());
								if(FileExtensions.EXTENSION_APK.equalsIgnoreCase(extension)) {
									Drawable d = mItemsAdapter.getAPKDrawable(path);
									if(d != null) {
										bmp = ((BitmapDrawable)d).getBitmap();
									}
								}
							} else {
								bmp = mItemsAdapter.getThumbnails(path);
							}
							mThumbnailCache.put(path, bmp);
							if(mThumbnailCache.size() > 100) {
								mThumbnailCache.remove(0);
							}
						}
						if(bmp != null) {
							holderList.itemIcon.setImageDrawable(null);
							holderList.itemIcon.setBackground(null);
							holderList.itemIcon.setBackground(new BitmapDrawable(mResources, bmp));
						} else {
							//holderList.itemIcon.setImageResource(R.drawable.file);
						}
						//mItemsAdapter.notifyDataSetChanged();
					} 
				}
			}
		}catch(Exception e) {
			//e.printStackTrace();
		}
	}
	
	/**
	 * inflate all the toolbars ie. multi-select and normal tool bar and register click listeners for them
	 */
	private void inflateLayouts() {

		LayoutInflater inflater = (LayoutInflater) getSystemService(Service.LAYOUT_INFLATER_SERVICE);

		mViewFlipper = (ViewFlipper)findViewById(R.id.viewflipper_top);
		mOptionsLayout = inflater.inflate(R.layout.explorer_tool_bar, null);
		mMultipleOptionsLayout = inflater.inflate(R.layout.multi_select_toolbar, null);

		/*ImageView im = (ImageView) mOptionsLayout.findViewById(R.id.explorer_toolbar_expand);
		DrawerArrowDrawable arrow = new DrawerArrowDrawable(this, Color.WHITE) {
			@Override
			public boolean isLayoutRtl() {
				return false;
			}
		};
		im.setImageDrawable(arrow);*/
		
		mOptionsLayout.setHorizontalScrollBarEnabled(false);
		mOptionsLayout.setHorizontalFadingEdgeEnabled(false);
		mOptionsLayout.setVerticalScrollBarEnabled(false);
		mOptionsLayout.setVerticalFadingEdgeEnabled(false);
		//mOptionsLayout.findViewById(R.id.explorer_toolbar_expand).setOnClickListener(this);
		mOptionsLayout.findViewById(R.id.explorer_toolbar_paste).setOnClickListener(this);
		mPasteView = (TextView) mOptionsLayout.findViewById(R.id.explorer_toolbar_paste);
		mOptionsLayout.findViewById(R.id.explorer_toolbar_home).setOnClickListener(this);
		//mOptionsLayout.findViewById(R.id.explorer_toolbar_up).setOnClickListener(this);
		mOptionsLayout.findViewById(R.id.explorer_toolbar_multi).setOnClickListener(this);
		mOptionsLayout.findViewById(R.id.explorer_toolbar_back).setOnClickListener(this);
		mOptionsLayout.findViewById(R.id.explorer_toolbar_new).setOnClickListener(this);
		mOptionsLayout.findViewById(R.id.explorer_toolbar_search).setOnClickListener(this);
		//mOptionsLayout.findViewById(R.id.explorer_toolbar_sort).setOnClickListener(this);
		mOptionsLayout.findViewById(R.id.explorer_toolbar_favourites).setOnClickListener(this);
		mOptionsLayout.findViewById(R.id.explorer_toolbar_refresh).setOnClickListener(this);
		mOptionsLayout.findViewById(R.id.explorer_toolbar_exit).setOnClickListener(this);

		mMultipleOptionsLayout.setHorizontalScrollBarEnabled(false);
		mMultipleOptionsLayout.setHorizontalFadingEdgeEnabled(false);
		mMultipleOptionsLayout.setVerticalScrollBarEnabled(false);
		mMultipleOptionsLayout.setVerticalFadingEdgeEnabled(false);
		mMultipleOptionsLayout.findViewById(R.id.explorer_toolbar_multi_cancel).setOnClickListener(this);
		mMultipleOptionsLayout.findViewById(R.id.explorer_toolbar_multi_copy).setOnClickListener(this);
		mMultipleOptionsLayout.findViewById(R.id.explorer_toolbar_multi_cut).setOnClickListener(this);
		mMultipleOptionsLayout.findViewById(R.id.explorer_toolbar_multi_delete).setOnClickListener(this);
		mMultipleOptionsLayout.findViewById(R.id.explorer_toolbar_multi_selectall).setOnClickListener(this);
		//mMultipleOptionsLayout.findViewById(R.id.explorer_toolbar_multi_unselectall).setOnClickListener(this);

		mMultiCopy = (TextView) mMultipleOptionsLayout.findViewById(R.id.explorer_toolbar_multi_copy);
		mMultiCut = (TextView) mMultipleOptionsLayout.findViewById(R.id.explorer_toolbar_multi_cut);
		mMultiDelete = (TextView) mMultipleOptionsLayout.findViewById(R.id.explorer_toolbar_multi_delete);
		
		mViewFlipper.addView(mOptionsLayout);
		mViewFlipper.addView(mMultipleOptionsLayout);

		Animation animationFlipIn  = AnimationUtils.loadAnimation(this, R.anim.flipin);
		Animation animationFlipOut = AnimationUtils.loadAnimation(this, R.anim.flipout);
		mViewFlipper.setInAnimation(animationFlipIn);
		mViewFlipper.setOutAnimation(animationFlipOut);
	}

	/**
	 * based on the current displaying tool bar options switch from mulit-select to normal
	 * using view flipper
	 * 
	 * @param isMultipleSelected true if multi-select tool bar is currently being displayed
	 */
	private void addToolBar(boolean isMultipleSelected) {		
		if(isMultipleSelected) {
			mViewFlipper.showNext();
		} else {
			mViewFlipper.showPrevious();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(BUNDLE_CURRENT_PATH, mCurrentDisplayPath);
	}
	
	private String getExternalStoragePath() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	/**
	 * when a file or folder is long pressed a dialog with options is displayed. The options vary based on the 
	 * file type, extensions(like unzip options is displayed for .zip files), if multi options is selected or not etcc..
	 * 
	 * @param longPressedView the list item which is long pressed
	 */
	private void longPressOptions(View longPressedView) {

		AlertDialog.Builder builder = new AlertDialog.Builder(FileExpActivity.this);
		builder.setTitle(getString(R.string.options));

		ViewHolderList holderList = (ViewHolderList) longPressedView.getTag();
		TextView textView = holderList.itemName;
		String path = textView.getContentDescription().toString();

		mLongPressedFile = new File(path);
		final List<String> itemsToDisplay = new ArrayList<String>();

		if(!mIsMultipleOptionSelected) {
			itemsToDisplay.add(getString(R.string.copy));
			itemsToDisplay.add(getString(R.string.delete));
			itemsToDisplay.add(getString(R.string.rename));
			itemsToDisplay.add(getString(R.string.cut));
		}

		if(mLongPressedFile.isFile() && !mIsMultipleOptionSelected) {
			itemsToDisplay.add(getString(R.string.send));
		} else {
			//check if multiple selection has any files, only then show this option
			if(mIsMultipleOptionSelected && ItemsAdapter.mMultipleSelectFiles.size() > 0) {
				boolean showShare = false;
				for(String filPath : ItemsAdapter.mMultipleSelectFiles) {
					final File file = new File(filPath);
					if(file != null && file.exists() && file.isFile()) {
						//yes atleast one file found, show share
						showShare = true;
						break;
					}
				}
				if(showShare) {
					itemsToDisplay.add(getString(R.string.send));
				}
			}
		}

		itemsToDisplay.add(getString(R.string.properties));
		if(mLongPressedFile != null && mLongPressedFile.isFile() && !mIsMultipleOptionSelected) {
			if("zip".equalsIgnoreCase(ItemOperations.getFileExtension(mLongPressedFile.getName()))) {
				itemsToDisplay.add(getString(R.string.unzip));
			} else {
				itemsToDisplay.add(getString(R.string.zip));
			}
		} else {
			itemsToDisplay.add(getString(R.string.zip));
		}

		if(!mIsMultipleOptionSelected) {
			itemsToDisplay.add(getString(R.string.mark_favourite));
		}

		if(mLongPressedFile.isDirectory() && !mIsMultipleOptionSelected) {
			itemsToDisplay.add(getString(R.string.set_as_home));
		}
		
		String[] opts = new String[itemsToDisplay.size()];
		opts = itemsToDisplay.toArray(opts); 
		
		/*ListView list = new ListView(FileExpActivity.this);
		list.setDivider(mResources.getDrawable(R.drawable.list_seperator));
		list.setCacheColorHint(Color.TRANSPARENT);
		list.setAdapter(new ArrayAdapter<String>(FileExpActivity.this, R.layout.options_list_item, opts));
		builder.setView(list);*/

		builder.setItems(opts, new DialogInterface.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				String type = itemsToDisplay.get(which);
				if(type.equals(getString(R.string.copy))) {
					mSelectedCutCopyOption = MENU_COPY;
					displayToast(getString(R.string.file_copy_selected));
					mPasteView.setVisibility(View.VISIBLE);
					onWindowFocusChanged(true);
				} else if(type.equals(getString(R.string.cut))) {
					mSelectedCutCopyOption = MENU_CUT;
					displayToast(getString(R.string.file_copy_selected));
					mPasteView.setVisibility(View.VISIBLE);
					onWindowFocusChanged(true);
				} else if(type.equals(getString(R.string.delete))) {
					showDialog(MENU_DELETE);
				} else if(type.equals(getString(R.string.rename))) {
					displayRenameDialog();
				} else if(type.equals(getString(R.string.zip))) {
					if(!mIsMultipleOptionSelected) {
						ZipUnZipTask zipTask = new ZipUnZipTask();
						zipTask.execute(MENU_ZIP);
					} else {
						displayZipDialog();
					}
				} else if(type.equals(getString(R.string.unzip))) {
					ZipUnZipTask unzipTask = new ZipUnZipTask();
					unzipTask.execute(MENU_UNZIP);
				} else if(type.equals(getString(R.string.properties))) {
					//beforeDisplayingDialog(MENU_PROPERTIES);
					mHandler.sendEmptyMessage(MENU_PROPERTIES);
				} else if(type.equals(getString(R.string.mark_favourite))) {
					mFavourites.saveFavouritePath(mLongPressedFile.getPath());
					displayToastShort(mLongPressedFile.getName() + " " + getString(R.string.favourite_created));
				} else if(type.equals(getString(R.string.send))) {
					if(mIsMultipleOptionSelected && ItemsAdapter.mMultipleSelectFiles.size() > 0) {
						sendMultipleFiles();
					} else {
						sendFile();
					}
				} else if(type.equals(getString(R.string.set_as_home))) {
					mDefaultHomePath = mLongPressedFile.getPath();
					mSharedPref.edit().putString(Settings.PREF_DEF_HOME_FOLDER, mLongPressedFile.getPath()).commit();
				}

				dialog.dismiss();
			}
		});
		
		final AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		mOptionsLayout.post(new Runnable() {
			public void run() {
				mOptionsLayout.scrollTo(0, 0);
			}
		});
		
		mHSVPath.post(new Runnable() {
			@Override
			public void run() {
				//always the latest folder path ie. end of folder path has to be displayed
				mHSVPath.smoothScrollTo(mFolderPathLinearLayout.getWidth(), 0);
			}
		});
	}
	
	/**
	 * this method displays the items in a folder. If no items are found empty folder msg is shown
	 * 
	 * @param pathToDisplay folder path in which items are to be displayed
	 */
	private void displayItems(String pathToDisplay) {
		isFolderClicked = true;
		mItemsAdapter.scrollStarted(true);
		mCurrentDisplayPath = pathToDisplay;
		//display the folder path just below the tool bar
		updateNavigationBar(pathToDisplay);
		//move the scroll bar of folder path to the end
		onWindowFocusChanged(true);

		boolean result = mItemsAdapter.updateItems(mCurrentDisplayPath);
		
		/*mListView.requestFocusFromTouch();
		if(mFirstVisibleItemInPathMap.containsKey(mCurrentDisplayPath)) {
			int selectedPosition = mFirstVisibleItemInPathMap.get(mCurrentDisplayPath);
			mListView.setSelection(selectedPosition);
		} else {
			mListView.setSelection(0);
		}
		mListView.clearFocus();*/
		
		if(mFirstVisibleItemInPathMap != null) {
			final boolean contains = mFirstVisibleItemInPathMap.containsKey(mCurrentDisplayPath);
			if(contains) {
				mListView.clearFocus();
				mListView.post(new Runnable() {
					@Override
					public void run() {
						if(contains) {
							mListView.setSelection(mFirstVisibleItemInPathMap.get(mCurrentDisplayPath));
						} else {
							mListView.setSelection(0);
						}
					}
				});
			} else {
				//for new folder, go to first position
				mListView.setSelection(0);
			}
		}
		
		//based on the result display either items or empty folder msg
		if(result) {
			if(mDisplayItemsLinearLayout.getChildAt(0) instanceof TextView) {
				//if previously empty view is shown remove it and display the lsitview
				mDisplayItemsLinearLayout.removeAllViews();
				if(mViewTypeOption == Settings.VIEW_TYPE_LIST) {
					mDisplayItemsLinearLayout.addView(mListView);
				} else {
					mDisplayItemsLinearLayout.addView(mGridView);
				}
			}
		} else {
			addEmtyTextView("Empty folder");
		}
	}
	
	/**
	 * this will update the navigation bar view.(below the tool bar and above the lsit view)
	 * 
	 * @param path
	 */
	private void updateNavigationBar(final String path) {
		
		mFolderPathLinearLayout.removeAllViews();
		
		//first add the root item in the navigation bar. that has to be all the time, so adding very first
		final View scrollParentLinearRoot = mLayoutInflater.inflate(R.layout.navigation_bar_item, null);
		((ImageView) scrollParentLinearRoot.findViewById(R.id.navigation_bar_arrow_image)).setVisibility(View.INVISIBLE);
		final TextView itemRoot = (TextView) scrollParentLinearRoot.findViewById(R.id.navigation_bar_tile);
		itemRoot.setText("/");
		itemRoot.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				displayItems("/");
			}
		});
		mFolderPathLinearLayout.addView(scrollParentLinearRoot);
		
		//now add rest of the folders into the view
		final String[] allFolders = path.split("/");
		final StringBuffer sb = new StringBuffer();
		
		for (String str : allFolders) {
			
			if(TextUtils.isEmpty(str)) {
				continue;
			}
			
			sb.append(str);
			
			final View scrollParentLinear = mLayoutInflater.inflate(R.layout.navigation_bar_item, null);
			final TextView item = (TextView)scrollParentLinear.findViewById(R.id.navigation_bar_tile);
			item.setText(str);
			item.setContentDescription(sb.toString());
			item.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					displayItems(v.getContentDescription().toString());
				}
			});

			sb.append("/");
			
			mFolderPathLinearLayout.addView(scrollParentLinear);
		}
	}
	
	/**
	 * used to display the toast messages
	 * @param msg msg that has to be displayed in the toast message
	 */
	private void displayToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	private void displayToastShort(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	Handler itemClickHandler = new Handler() {
		public void handleMessage(Message msg) {
			View arg1 = (View) msg.obj;
			
			mBackPressCounter = -1;
			mItemsAdapter.scrollStarted(false);
			ViewHolderList holderList = (ViewHolderList) arg1.getTag();
			TextView textView = holderList.itemName;
			String path = textView.getContentDescription().toString();
			File file = new File(path);

			mFirstVisibleItemInPathMap.put(mCurrentDisplayPath, mListView.getFirstVisiblePosition());
			
			if(!mIsMultipleOptionSelected) {
				if(file.isDirectory()) {
					displayItems(path);
				} else {
					if(mShouldPickFile) {
						finishWithResult(file);
					} else {
						if("zip".equalsIgnoreCase(ItemOperations.getFileExtension(file.getName()))) {
							openZipFileDialog(file);
						} else {
							fileSupportedApplications(file);
						}
					}
				}
			} else {
				if(ItemsAdapter.mMultipleSelectFiles.contains(path)) {
					//textView.setCheckMarkDrawable(android.R.drawable.checkbox_off_background);
					ItemsAdapter.mMultipleSelectFiles.remove(path);
				} else {
					//textView.setCheckMarkDrawable(android.R.drawable.checkbox_on_background);
					ItemsAdapter.mMultipleSelectFiles.add(path);
				}
				mItemsAdapter.notifyDataSetChanged();
				
				if(ItemsAdapter.mMultipleSelectFiles.size() == 0) {
					mMultiCopy.setClickable(false);
					mMultiCut.setClickable(false);
					mMultiDelete.setClickable(false);
					
					mMultiCopy.setTextColor(Color.GRAY);
					mMultiCut.setTextColor(Color.GRAY);
					mMultiDelete.setTextColor(Color.GRAY);
					
				} else {
					mMultiCopy.setClickable(true);
					mMultiCut.setClickable(true);
					mMultiDelete.setClickable(true);
					
					final int color = getResources().getColor(android.R.color.white);
					mMultiCopy.setTextColor(color);
					mMultiCut.setTextColor(color);
					mMultiDelete.setTextColor(color);
				}
			}
			
			List<File> listOfFiles = mItemsAdapter.getCurrentDisplayingItems();
			if(listOfFiles != null && (listOfFiles.size() == ItemsAdapter.mMultipleSelectFiles.size())) {
				((TextView) mMultipleOptionsLayout.findViewById(R.id.explorer_toolbar_multi_selectall))
					.setText(getString(R.string.unselectall));
			} else {
				((TextView) mMultipleOptionsLayout.findViewById(R.id.explorer_toolbar_multi_selectall))
					.setText(getString(R.string.selectall));
			}
			
			if(mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}
		
		};
	};
	
	/**
	 * Finish this Activity with a result code and URI of the selected file.
	 * 
	 * @param file The file selected.
	 */
	private void finishWithResult(final File file) {
		if (file != null) {
			final Uri uri = Uri.fromFile(file);
			setResult(RESULT_OK, new Intent().setData(uri));
			finish();
		} else {
			setResult(RESULT_CANCELED);	
			finish();
		}
	}
	
	/**
	 * this will display a dialog when a zip file is singl clicked
	 * @param file
	 */
	private void openZipFileDialog(final File file) {
		AlertDialog.Builder builder = new AlertDialog.Builder(FileExpActivity.this);
		builder.setTitle(R.string.unzip);
		builder.setMessage(getString(R.string.unzip_dialog));

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mLongPressedFile = file;
				ZipUnZipTask zipTask = new ZipUnZipTask();
				zipTask.execute(MENU_UNZIP);
			}
		});
		
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) { }
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	/**
	 * when a folder or file is clicked
	 */
	public void onItemClick(AdapterView<?> arg0, final View arg1, int arg2, long arg3) {
		if(!mIsMultipleOptionSelected) {
			mProgressDialog = ProgressDialog.show(FileExpActivity.this, "", getString(R.string.loading_msg), true);
		}
		Message msg = Message.obtain();
		msg.obj = arg1;
		itemClickHandler.sendMessage(msg);
	}

	/**
	 * this is a empty text view to add and message. used to display empty folder 
	 * text when a empty folder is opened
	 * @param msg
	 */
	private void addEmtyTextView(String msg) {
		TextView text = new TextView(mContext);
		text.setWidth(mResources.getDisplayMetrics().widthPixels);
		text.setHeight(mResources.getDisplayMetrics().heightPixels - 100);
		text.setGravity(Gravity.CENTER);
		text.setPadding(0, 0, 0, 150);
		text.setTextAppearance(FileExpActivity.this, R.style.app_text_style);
		text.setText(msg);
		text.setTypeface(Utils.getInstance().getDisplayTypeface());

		mDisplayItemsLinearLayout.removeAllViews();
		mDisplayItemsLinearLayout.addView(text);
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_SDCARD_INFO, 0, R.string.sd_card_analysis);
		menu.add(0, MENU_SETTINGS, 0, R.string.settings);
		return super.onCreateOptionsMenu(menu);
	}*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case android.R.id.home:
			mDrawerLayout.openDrawer(Gravity.START);
			break;
		case MENU_SETTINGS:
			i = new Intent(mContext, Settings.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
			break;
		case MENU_SDCARD_INFO:
			i = new Intent(mContext, SDCardInfoActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * this will display a progress dialog before displaying a dialog, generally used when
	 * operations take long time which can lead to ANR
	 * 
	 * @param id
	 */
	/*private void beforeDisplayingDialog(final int id) {
		final ProgressDialog progress = ProgressDialog.show(FileExpActivity.this, "","Loading info..."); 
		new Thread(new Runnable() { 
			public void run() { 
				Message msg = Message.obtain();
				msg.arg1 = id;
				if(id == MENU_PROPERTIES) {
					if(ItemsAdapter.mMultipleSelectFiles != null && ItemsAdapter.mMultipleSelectFiles.size() == 0) {
						msg.obj = mItemDetails.fileDetails(mLongPressedFile);
					} else {
						msg.obj = mItemDetails.multipleFileDetails(ItemsAdapter.mMultipleSelectFiles);
					}
				}
				progress.dismiss();
				mHandler.sendMessage(msg);
			}
		}).start();
	}*/

	/** 
	 * when a file is selected display the list of applications which can open the file
	 * 
	 * @param file file that is selected
	 */
	private void fileSupportedApplications(File file) {

		Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
		String type = mMimeTypes.getMimeType(file.getName().toLowerCase());
		intent.setDataAndType(Uri.fromFile(file), type);
		Intent.createChooser(intent, "Select");

		//if no applications are found display a toast
		try {
			startActivity(intent);
		} catch(Exception e) {
			displayToast("No Supported Applications Found");
		}
	}
	
	/**
	 * when send options is selected display a list of apps that can be used
	 * to transfer the file. this options is only available for files
	 */
	private void sendFile() {

		String filename = mLongPressedFile.getName();
		MimeTypes mt = new MimeTypes(); 

		Intent i = new Intent();
		i.setAction(Intent.ACTION_SEND);
		i.setType(mt.getMimeType(filename));
		i.putExtra(Intent.EXTRA_SUBJECT, filename);
		i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mLongPressedFile));
		i = Intent.createChooser(i, "send "+filename+" using");

		try {
			startActivity(i);
		} catch (Exception e) {
			displayToast("Unable to send");
		}
	}

	private void sendMultipleFiles() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND_MULTIPLE);
		intent.setType("*/*"); 

		ArrayList<Uri> files = new ArrayList<Uri>();
		for(String path : ItemsAdapter.mMultipleSelectFiles ) {
		    final File file = new File(path);
		    if(file != null && file.isFile()) {
		    	Uri uri = Uri.fromFile(file);
		    	files.add(uri);
		    }
		}

		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
		intent = Intent.createChooser(intent, "send files using...");
		startActivity(intent);
	}
	
	Handler mHandler = new Handler () {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == MENU_PROPERTIES) {
				
				TableLayout tableLayout = (TableLayout) View.inflate(FileExpActivity.this, R.layout.properties_dialog_layout, null);
				if(ItemsAdapter.mMultipleSelectFiles != null && ItemsAdapter.mMultipleSelectFiles.size() == 0) {
					mItemDetails.fileDetails(mLongPressedFile, tableLayout);
				} else {
					mItemDetails.multipleFileDetails(ItemsAdapter.mMultipleSelectFiles, tableLayout);
				}
				
				msg.obj = tableLayout;
				displayPropertiesDialog((TableLayout)msg.obj);
			} 
			
			if(msg.what == -1) {
				mBackPressCounter = -1;
			} 
		}
	};

	private void refreshList() {
		displayItems(mCurrentDisplayPath);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		/*case MENU_RENAME:
			final android.widget.EditText edit = new android.widget.EditText(mContext);
			edit.setMaxLines(1);
			edit.requestFocus();
			edit.setId(MENU_RENAME);
			return new AlertDialog.Builder(FileExpActivity.this)
			.setTitle(R.string.rename).setView(edit).setPositiveButton(
					R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							
						}
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

						}
					}).create();*/
		case MENU_CREATE_NEW:
			return new AlertDialog.Builder(FileExpActivity.this).setTitle(R.string.create_new)
					.setItems(mResources.getStringArray(R.array.menu_create_new_items), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(which == 0) {
								//create File
								createNewDialog(false);
							} else {
								//create Folder
								createNewDialog(true);
							}
						}
					}).create();
		case MENU_DELETE:
			return new AlertDialog.Builder(FileExpActivity.this).setTitle(R.string.delete)
					.setMessage(R.string.confirm_delete)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							DeleteOperationTask delTask = new DeleteOperationTask();
							delTask.execute();
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
					}).create();
		/*case WHATS_NEW_DIAG:
			return new AlertDialog.Builder(FileExpActivity.this).setTitle(R.string.welcome_msg)
					.setMessage(R.string.change_log)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							 mSharedPref.edit().putBoolean(Settings.PREF_FIRST_LAUNCH, false).commit();
						}
					}).create();*/
		/*case MENU_ZIP:
			final android.widget.EditText zipName = new android.widget.EditText(mContext);
			zipName.setId(MENU_ZIP);
			return new AlertDialog.Builder(FileExpActivity.this)
			.setMessage(R.string.enter_zip_name)
			.setTitle(R.string.zip).setView(zipName).setPositiveButton(
					R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mZipFileName = zipName.getText().toString();
							ZipUnZipTask zipTask = new ZipUnZipTask();
							zipTask.execute(MENU_ZIP);
						}
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

						}
					}).create();*/
		}
		return super.onCreateDialog(id);
	}

	/**
	 * dialog to display zip dialog when multiple files are selected
	 */
	private void displayZipDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(FileExpActivity.this);
		builder.setTitle(R.string.zip);
		
		final RelativeLayout relative = (RelativeLayout) View.inflate(mContext, R.layout.new_folder_dialog, null);
		final EditText edit = (EditText) relative.findViewById(R.id.dialog_edittext);

		builder.setView(relative);
		builder.setPositiveButton(getString(R.string.create), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Editable text = edit.getText();
				if(text != null && text.toString() != null && text.toString().length() > 0) {
					mZipFileName = edit.getText().toString();
					ZipUnZipTask zipTask = new ZipUnZipTask();
					zipTask.execute(MENU_ZIP);
				}
				dialog.dismiss();
			}
		});
		
		builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		builder.create().show();
	}
	
	/*
	 * dialog to display the rename dialog
	 */
	private void displayRenameDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(FileExpActivity.this);
		builder.setTitle(R.string.rename);
		
		final RelativeLayout relative = (RelativeLayout) View.inflate(mContext, R.layout.new_folder_dialog, null);
		final EditText edit = (EditText) relative.findViewById(R.id.dialog_edittext);
		builder.setView(relative);
		
		String str = mLongPressedFile.getName();
		if(str.contains("."))
			str = str.substring(0, str.indexOf("."));
		edit.setText(str);
		edit.setSelectAllOnFocus(true);
		edit.setSelection(0, str.length());
		edit.requestFocus();
		
		builder.setPositiveButton(getString(R.string.create), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Editable text = edit.getText();
				if(text != null && text.toString() != null && text.toString().length() > 0) {
					boolean result = mItemOperations.rename(mLongPressedFile.getParent(), mLongPressedFile.getName(), edit.getText().toString());
					if(!result) {
						displayToast(getString(R.string.rename_failed));
					} else {
						new MediaScannerNotifier(FileExpActivity.this, mLongPressedFile.getParent()+"/"+edit.getText().toString(), null);
					}
					refreshList();
				}
				dialog.dismiss();
			}
		});
		
		builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		builder.create().show();
	}
	
	/**
	 * display the file/folder properties
	 * @param msg
	 */
	private void displayPropertiesDialog(final TableLayout tableLayout) {
		AlertDialog.Builder builder = new AlertDialog.Builder(FileExpActivity.this);
		builder.setTitle(R.string.properties);

		ScrollView scrollView = new ScrollView(mContext);

		/*TextView propertiesMsg = new TextView(mContext); 
		propertiesMsg.setTextSize(mResources.getDimension(R.dimen.properties_dialog_msg));
		propertiesMsg.setText(msg);*/

		scrollView.addView(tableLayout);
		builder.setView(scrollView);

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/**
	 * displays the change logs for the version
	 */
	private void displayWatsNewDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(FileExpActivity.this);
		builder.setTitle(R.string.welcome_msg);

		ScrollView scrollView = new ScrollView(mContext);

		TextView propertiesMsg = new TextView(mContext); 
		propertiesMsg.setPadding(10, 0, 0, 0);
		propertiesMsg.setTextSize(15);
		propertiesMsg.setText(R.string.change_log);

		scrollView.addView(propertiesMsg);
		builder.setView(scrollView);

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				 mSharedPref.edit().putBoolean(Settings.PREF_FIRST_LAUNCH, false).commit();
			}
		});

		builder.create().show();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.explorer_toolbar_paste:
			CutCopyTask cutCopyTask = new CutCopyTask();
			cutCopyTask.execute();
			break;
		case R.id.explorer_toolbar_home:
			//display the home folder set by the user
			String homePath = mSharedPref.getString(Settings.PREF_DEF_HOME_FOLDER, getExternalStoragePath());
			
			if(mFirstVisibleItemInPathMap != null) {
				mFirstVisibleItemInPathMap.put(homePath, 0);
			}
			
			if(mItemOperations.isFolderExists(homePath)) {
				mCurrentDisplayPath = homePath;
			} else {
				mCurrentDisplayPath = getExternalStoragePath();
			}
			displayItems(mCurrentDisplayPath);
			break;
		/*case R.id.explorer_toolbar_up:
			String parentPath = ItemOperations.getParentPath(mCurrentDisplayPath);
			boolean isReadable = mItemOperations.isFolderReadable(parentPath);
			if(isReadable) {
				displayItems(parentPath);
			}
			break;*/
		case R.id.explorer_toolbar_multi:
			mIsMultipleOptionSelected = true;
			mItemsAdapter.notifyDataSetChanged();
			addToolBar(true);
			
			mMultiCopy.setClickable(false);
			mMultiCut.setClickable(false);
			mMultiDelete.setClickable(false);
			
			mMultiCopy.setTextColor(Color.GRAY);
			mMultiCut.setTextColor(Color.GRAY);
			mMultiDelete.setTextColor(Color.GRAY);
			
			break;
		case R.id.explorer_toolbar_back :
			handleBackPress();
			break;
		case R.id.explorer_toolbar_search:
			displaySearchDialog();
			break;
		case R.id.explorer_toolbar_new:
			showDialog(MENU_CREATE_NEW);
			break;
		case R.id.explorer_toolbar_favourites:
			displayFavoutiresList();
			break;
		case R.id.explorer_toolbar_refresh:
			refreshList();
			break;
		case R.id.explorer_toolbar_exit:
			finish();
			break;
		case R.id.explorer_toolbar_multi_cancel:
			changeFromMultiToNormal();
			break;
		case R.id.explorer_toolbar_multi_delete:
			showDialog(MENU_DELETE);
			break;
		case R.id.explorer_toolbar_multi_copy:
			mSelectedCutCopyOption = MENU_COPY;
			displayToast(getString(R.string.file_copy_selected));
			mIsMultipleOptionSelected = false;
			if(mViewTypeOption == Settings.VIEW_TYPE_LIST) {
				mListView.setAdapter(mItemsAdapter);
			} else {
				mGridView.setAdapter(mItemsAdapter);
			}
			addToolBar(false);
			mPasteView.setVisibility(View.VISIBLE);
			break;
		case R.id.explorer_toolbar_multi_cut:
			mSelectedCutCopyOption = MENU_CUT;
			displayToast(getString(R.string.file_copy_selected));
			mIsMultipleOptionSelected = false;
			if(mViewTypeOption == Settings.VIEW_TYPE_LIST) {
				mListView.setAdapter(mItemsAdapter);
			} else {
				mGridView.setAdapter(mItemsAdapter);
			}
			addToolBar(false);
			mPasteView.setVisibility(View.VISIBLE);
			break;	
		case R.id.explorer_toolbar_multi_selectall:
			List<File> listOfFiles = mItemsAdapter.getCurrentDisplayingItems();
			
			if(listOfFiles != null && (listOfFiles.size() == ItemsAdapter.mMultipleSelectFiles.size())) {
				//all are already selected, so unselect all
				
				((TextView) v).setText(getString(R.string.selectall));
				ItemsAdapter.mMultipleSelectFiles.clear();
				
				mMultiCopy.setClickable(false);
				mMultiCut.setClickable(false);
				mMultiDelete.setClickable(false);
				
				mMultiCopy.setTextColor(Color.GRAY);
				mMultiCut.setTextColor(Color.GRAY);
				mMultiDelete.setTextColor(Color.GRAY);
				
			} else {
				//select all
				
				((TextView) v).setText(getString(R.string.unselectall));
				ItemsAdapter.mMultipleSelectFiles.clear();
				//add all the files in this folder to multi selector list
				if(listOfFiles != null && listOfFiles.size() > 0) {
					for (File f : listOfFiles) {
						ItemsAdapter.mMultipleSelectFiles.add(f.getPath());
					}
					
					mMultiCopy.setClickable(true);
					mMultiCut.setClickable(true);
					mMultiDelete.setClickable(true);
					
					final int color = getResources().getColor(android.R.color.white);
					mMultiCopy.setTextColor(color);
					mMultiCut.setTextColor(color);
					mMultiDelete.setTextColor(color);
				}
			}
			mItemsAdapter.notifyDataSetChanged();
			
			break;
		/*case R.id.explorer_toolbar_multi_unselectall:
			ItemsAdapter.mMultipleSelectFiles.clear();
			mItemsAdapter.notifyDataSetChanged();
			
			mMultiCopy.setClickable(false);
			mMultiCut.setClickable(false);
			mMultiDelete.setClickable(false);
			
			mMultiCopy.setTextColor(Color.GRAY);
			mMultiCut.setTextColor(Color.GRAY);
			mMultiDelete.setTextColor(Color.GRAY);
			
			break;*/
		/*case R.id.explorer_toolbar_expand:
			//hideUnhideSidePane();
			mDrawerLayout.openDrawer(Gravity.START);
			break;*/
		}
	}
	
	private void createSidePane() {
		
		final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.side_pane_parent);
		
		final TextView sdcardInfo = (TextView) linearLayout.findViewById(R.id.side_pane_options_sdcard_info);
		final TextView appManager = (TextView) linearLayout.findViewById(R.id.side_pane_options_application_manager);
		final TextView settings = (TextView) linearLayout.findViewById(R.id.side_pane_option_settings);
		final TextView recommend = (TextView) linearLayout.findViewById(R.id.side_pane_option_recommend);
		final TextView rateMe = (TextView) linearLayout.findViewById(R.id.side_pane_option_rateme);
		
		sdcardInfo.setTypeface(Utils.getInstance().getDisplayTypeface());
		appManager.setTypeface(Utils.getInstance().getDisplayTypeface());
		settings.setTypeface(Utils.getInstance().getDisplayTypeface());
		recommend.setTypeface(Utils.getInstance().getDisplayTypeface());
		rateMe.setTypeface(Utils.getInstance().getDisplayTypeface());
		
		sdcardInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				Intent i = new Intent(mContext, SDCardInfoActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		});
		appManager.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				Intent i = new Intent(mContext, ApplicationManager.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		});
		settings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				Intent i = new Intent(mContext, Settings.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		});
		rateMe.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				
				int storeType = getResources().getInteger(R.integer.app_store_type);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				if(storeType == 0) {
					try { 
						intent.setData(Uri.parse("market://details?id=com.mkr.file_explorer"));
						startActivity(intent);
					} catch (Exception e) { //google play app is not installed
						intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.com.mkr.file_explorer"));
					}
				} else if(storeType == 1) {
					intent.setData(Uri.parse("http://www.amazon.com/gp/mas/dl/android?p=com.com.mkr.file_explorer"));
				}
				startActivity(intent);
			}
		});
		recommend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "manthena.android@gmail.com" });
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "SD File manager : New feature/Suggestion");
				FileExpActivity.this.startActivity(Intent.createChooser(emailIntent,"Send mail"));
			}
		});
	}
	
	
	private boolean displaySearchDialog() {
		mSelectedSearchOption = FileExtensions.FILE_ALL_TYPES;
		
		AlertDialog.Builder searchDialogBuilder = new AlertDialog.Builder(FileExpActivity.this);
		RelativeLayout relative = (RelativeLayout) View.inflate(FileExpActivity.this, R.layout.search_dialog, null);
		
		searchDialogBuilder.setTitle(R.string.search);
		
		Spinner optionsSpinner = (Spinner) relative.findViewById(R.id.search_dialog_options);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.search_options, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(R.layout.simple_spinner_item);
		optionsSpinner.setAdapter(adapter);
		optionsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				switch (arg2) {
				case 0:
					mSelectedSearchOption = FileExtensions.FILE_ALL_TYPES;
					break;
				case 1:
					mSelectedSearchOption = FileExtensions.FILE_TYPE_IMAGE;
					break;
				case 2:
					mSelectedSearchOption = FileExtensions.FILE_TYPE_AUDIO;
					break;
				case 3:
					mSelectedSearchOption = FileExtensions.FILE_TYPE_VIDEO;
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
			
		});
		
		searchDialogBuilder.setView(relative);
		final EditText searchText = (EditText) relative.findViewById(R.id.search_dialog_edittext);
		
		searchDialogBuilder.setPositiveButton(getString(R.string.search), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				dialog.dismiss();
				Intent intent = new Intent(FileExpActivity.this, SearchActivity.class);
				intent.putExtra("search_key", mSelectedSearchOption);
				intent.putExtra("search_string", searchText.getText().toString());
				startActivityForResult(intent, 1);
			}
		});
		
		searchDialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		final AlertDialog dialog = searchDialogBuilder.create();
		searchText.requestFocus();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		searchText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s != null && s.length() >= 1) {
					dialog.getButton(Dialog.BUTTON_POSITIVE).setClickable(true);
					dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
				} else {
					dialog.getButton(Dialog.BUTTON_POSITIVE).setClickable(false);
					dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override
			public void afterTextChanged(Editable s) { }
			
		});
		
		dialog.show();
		dialog.getButton(Dialog.BUTTON_POSITIVE).setClickable(false);
		dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
		
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data != null) {
			Bundle b = data.getExtras();
			if(b != null) {
				String path = b.getString("open_path");
				displayItems(path);
			}
		}
	}
	
	/**
	 * this will remove the multi-select options and display the normal options.
	 */
	private void changeFromMultiToNormal() {
		mIsMultipleOptionSelected = false;
		//clear any mulit-selected files
		ItemsAdapter.mMultipleSelectFiles.clear();
		if(mViewTypeOption == Settings.VIEW_TYPE_LIST) {
			mListView.setAdapter(mItemsAdapter);
		} else {
			mGridView.setAdapter(mItemsAdapter);
		}
		addToolBar(false);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(mIsMultipleOptionSelected) {
				changeFromMultiToNormal();
				return true;
			} if(mDrawerLayout.isDrawerOpen(Gravity.START)) {
				mDrawerLayout.closeDrawer(Gravity.START);
				return true;
			} else {
				if(mCurrentDisplayPath.equals(getExternalStoragePath())
						|| mCurrentDisplayPath.equals("/")) {
					if(mBackPressCounter == -1) {
						mBackPressCounter ++;
						displayToast(getString(R.string.press_back_to_exit));
						mHandler.sendMessageDelayed(Message.obtain(mHandler, -1), 3000);
						return true;
					} 
				} else {
					String parentPath = ItemOperations.getParentPath(mCurrentDisplayPath);
					boolean isReadable = mItemOperations.isFolderReadable(parentPath);
					if(isReadable) {
						displayItems(parentPath);
					}
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * called when the device back or back options in header is clicked.
	 * when at home folder. if the back is pressed display a toast "press again to exit"
	 * upon pressing again exit the application
	 */
	private void handleBackPress() {
		if(mCurrentDisplayPath.equals(mDefaultHomePath)
				|| mCurrentDisplayPath.equals("/")) {
			if(mBackPressCounter == -1) {
				mBackPressCounter ++;
				displayToast(getString(R.string.press_back_to_exit));
			} else if(mBackPressCounter == 0) {
				super.onBackPressed();
			}
		} else {
			String parentPath = ItemOperations.getParentPath(mCurrentDisplayPath);
			boolean isReadable = mItemOperations.isFolderReadable(parentPath);
			if(isReadable) {
				displayItems(parentPath);
			}
		}

	}

	/**
	 * this displayed the favorites list dialog. Long pressing the list item will delete the path
	 * as favorite
	 * 
	 */
	private void displayFavoutiresList() {

		String[] savedPaths = mFavourites.getFavouritePaths();
		//if no items are found display a toast
		if(savedPaths == null || savedPaths.length == 0) {
			displayToastShort(getString(R.string.no_favourite_path));
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(FileExpActivity.this);
			builder.setTitle(R.string.fav_paths);

			final ListView list = (ListView) View.inflate(mContext, R.layout.simple_listview, null);
			list.setCacheColorHint(Color.TRANSPARENT);
			list.setDivider(mResources.getDrawable(R.drawable.list_seperator));
			list.setAdapter(new FavouritesAdapter(mContext, savedPaths));
			builder.setView(list);

			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
			});

			final Dialog dialog = builder.create();
			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
					dialog.dismiss();
					FavHolder holder = (FavHolder) arg1.getTag();
					String path = holder.favPath.getText().toString() + "/" + holder.favName.getText().toString();
					File file = new File(path);
					if(file.exists()) {
						if(file.isDirectory()) {
							displayItems(path);
						} else {
							fileSupportedApplications(file);
						}
					} else {
						displayToast(getString(R.string.path_not_found));
					}
				}
			});

			list.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					FavHolder holder = (FavHolder) arg1.getTag();
					String path = holder.favPath.getText().toString() + "/" + holder.favName.getText().toString();
					mFavourites.deletePath(path);
					displayToastShort(holder.favName.getText().toString() + " " + getString(R.string.favourite_path_deleted));
					String[] savedPaths = mFavourites.getFavouritePaths();
					if(savedPaths == null || savedPaths.length == 0) {
						dialog.dismiss();
						displayToast(getString(R.string.no_favourite_path));
					} else {
						list.setAdapter(new FavouritesAdapter(mContext, savedPaths));
					}
					return true;
				}
			});

			dialog.show();
			displayToastShort(getString(R.string.delete_favourite_path));
		}
	}

	/**
	 * dialog allows to create a new file or folder
	 * @param isFolder
	 */
	private void createNewDialog(final boolean isFolder) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(FileExpActivity.this);

		final RelativeLayout relative = (RelativeLayout) View.inflate(mContext, R.layout.new_folder_dialog, null);
		final EditText edit = (EditText) relative.findViewById(R.id.dialog_edittext);
		
		if(isFolder) {
			builder.setTitle(R.string.newfolder);
			//builder.setMessage(R.string.create_new_folder);
			edit.setHint(R.string.create_new_folder);
		} else {
			builder.setTitle(R.string.newfile);
			builder.setMessage(R.string.create_new_file);
		}

		builder.setView(relative);

		builder.setPositiveButton(getString(R.string.create), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				//based on the result display a toast
				if(isFolder) {
					boolean result = mItemOperations.createNewFolder(mCurrentDisplayPath, edit.getText().toString());
					if(!result) {
						displayToast("Folder \"" + edit.getText().toString() + "\" already exists");
					} else {
						displayToast("Folder \"" + edit.getText().toString() + "\" created");
						refreshList();
						int folderIndex = mItemsAdapter.getCurrentDisplayingItems().indexOf(new File(mCurrentDisplayPath, edit.getText().toString()));
						if(mViewTypeOption == Settings.VIEW_TYPE_LIST) {
							mListView.setSelection(folderIndex);
						} else {
							mGridView.setSelection(folderIndex);
						}
					}
				} else {
					boolean result = mItemOperations.createNewFile(mCurrentDisplayPath, edit.getText().toString()+".txt");
					if(!result) {
						displayToast("File \"" + edit.getText().toString() + "\" already exists");
					} else {
						displayToast("File \"" + edit.getText().toString() + "\" created");
						refreshList();
						int folderIndex = mItemsAdapter.getCurrentDisplayingItems().indexOf(new File(mCurrentDisplayPath, edit.getText().toString()));
						if(mViewTypeOption == Settings.VIEW_TYPE_LIST) {
							mListView.setSelection(folderIndex);
						} else {
							mGridView.setSelection(folderIndex);
						}
					}
				}
				dialog.dismiss();
			}
		});
		
		builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		final AlertDialog dialog = builder.create();
		edit.requestFocus();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		//make the create button enabled only if a character is entered
		edit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s != null && s.length() >= 1) {
					dialog.getButton(Dialog.BUTTON_POSITIVE).setClickable(true);
					dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
				} else {
					dialog.getButton(Dialog.BUTTON_POSITIVE).setClickable(false);
					dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) { }
			@Override
			public void afterTextChanged(Editable s) { }
		});

		dialog.show();
		dialog.getButton(Dialog.BUTTON_POSITIVE).setClickable(false);
		dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals(Settings.PREF_DEF_HOME_FOLDER)) {
			mDefaultHomePath = sharedPreferences.getString(Settings.PREF_DEF_HOME_FOLDER, Environment.getExternalStorageDirectory().getPath());
		} else if(key.equals(Settings.PREF_DISPLAY_HIDDEN_FILES)) {
			mDisplayHiddenFiles = sharedPreferences.getBoolean(Settings.PREF_DISPLAY_HIDDEN_FILES, false);
		} else if(key.equals(Settings.PREF_DISPLAY_MODIFIED_DATE)) {
			mDisplayModifiedDate = sharedPreferences.getBoolean(Settings.PREF_DISPLAY_MODIFIED_DATE, true);
		} else if(key.equals(Settings.PREF_SORT_ITEMS_BY)) {
			mSortItemsBy = Integer.parseInt(sharedPreferences.getString(Settings.PREF_SORT_ITEMS_BY, ""+Settings.SORT_BY_NAME));
		} else if(key.equals(Settings.PREF_ITEM_TEXT_SIZE)) {
			setTextSize();
		} else if(key.equals(Settings.PREF_DISPLAY_THUMBNAILS)) {
			mDisplayThumbs = mSharedPref.getBoolean(Settings.PREF_DISPLAY_THUMBNAILS, true);
		} else if(key.equals(Settings.PREF_VIEW_TYPE)) {
			mViewTypeOption = Integer.parseInt(sharedPreferences.getString(Settings.PREF_VIEW_TYPE, ""+Settings.VIEW_TYPE_LIST));
			mDisplayItemsLinearLayout.removeAllViews();
			if(mViewTypeOption == Settings.VIEW_TYPE_LIST) {
				mDisplayItemsLinearLayout.addView(mListView);
			} else {
				mDisplayItemsLinearLayout.addView(mGridView);
			}
			mItemsAdapter.notifyDataSetChanged();
		} else if(key.equals(Settings.PREF_FIRST_LAUNCH)) {
			boolean val = sharedPreferences.getBoolean(Settings.PREF_FIRST_LAUNCH, false);
			if(val) {
				//showDialog(WHATS_NEW_DIAG);
				displayWatsNewDialog();
			}
		} else if(key.equals(ThemeUtils.PREF_SELECTED_THEME)) {
			applyTheme();
			mItemsAdapter.createCircleBG();
			refreshList();
		}
		
		//change_textsize_harcode is used only for this release, in this case dont update the list
		if(!"change_textsize_harcode".equals(key)) {
			//refresh lists once
			refreshList();
		}
	}

	/**
	 * set the text size based on the user settings
	 */
	private void setTextSize() {
		float scaledDensity = mResources.getDisplayMetrics().scaledDensity;
		int selectedItem = Integer.parseInt(mSharedPref.getString(Settings.PREF_ITEM_TEXT_SIZE, ""+Settings.TEXT_SIZE_SMALL));
		int pixels = 0;
		
		boolean update = false;
		if(!mSharedPref.contains("change_textsize_harcode")) {
			mSharedPref.edit().putBoolean("change_textsize_harcode", false).commit();
			mSharedPref.edit().putString(Settings.PREF_ITEM_TEXT_SIZE, ""+Settings.TEXT_SIZE_SMALL).commit();
			update = true;
		}
		
		if(update) {
			pixels = (int) mResources.getDimension(R.dimen.item_name_size_small);
		} else {
			switch (selectedItem) {
			case 0:
				pixels = (int) mResources.getDimension(R.dimen.item_name_size_small);
				break;
			case 1:
				pixels = (int) mResources.getDimension(R.dimen.item_name_size_medium);
				break;
			case 2:
				pixels = (int) mResources.getDimension(R.dimen.item_name_size_large);
				break;
			default :
				pixels = (int) mResources.getDimension(R.dimen.item_name_size_small);
				break;
			}
		}
		mDisplayItemTextSize = (int) (pixels/scaledDensity);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}
	
	private class ZipUnZipTask extends AsyncTask<Integer, Void, Void> {

		private ProgressDialog pd;
		@Override
		protected void onPreExecute() {
			pd = ProgressDialog.show(FileExpActivity.this, "", "Please wait...");
		}

		@Override
		protected Void doInBackground(Integer... params) {
			ZipUnZip zipAndUnZip = new ZipUnZip();
			if(params[0] == MENU_UNZIP) {
				pd.setTitle("Extracting ...");
				zipAndUnZip.fileUnzip(mLongPressedFile.getPath());
			} else if(params[0] == MENU_ZIP) {
				pd.setTitle("Creating zip ...");
				try {
					if(!mIsMultipleOptionSelected) {
						zipAndUnZip.createZIP(mLongPressedFile.getPath(), mLongPressedFile.getName());
					} else {
						if("null".equals(mZipFileName)) {
							mZipFileName = mLongPressedFile.getName();
						}
						String[] filePaths = new String[ItemsAdapter.mMultipleSelectFiles.size()];
						filePaths = ItemsAdapter.mMultipleSelectFiles.toArray(filePaths);
						zipAndUnZip.createZIP(filePaths, mLongPressedFile.getParent() + "/" + mZipFileName + FileExtensions.ZIP_EXTENSION);
					}
				}catch(Exception e) { 
					//e.printStackTrace(); 
				};
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			refreshList();
			if(pd != null && pd.isShowing()) {
				pd.dismiss();
			}
			if(mIsMultipleOptionSelected) {
				changeFromMultiToNormal();
			}	
		}
	}

	/**
	 * Async task to handle cut, copy operations
	 * @author Manthena Murali
	 *
	 */
	private class CutCopyTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog pd;
		private String mCurrentCopyingfile;
		private static final long FILE_COPY_BUFFER_SIZE = 1024 * 1024 * 5;

		@Override
		protected void onPreExecute() {
			pd = ProgressDialog.show(FileExpActivity.this, "Copying ... ", "Starting.. Please wait");
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				if(ItemsAdapter.mMultipleSelectFiles != null && ItemsAdapter.mMultipleSelectFiles.size() == 0) {
					if(mSelectedCutCopyOption == MENU_COPY) {
						File file = new File(mLongPressedFile.getPath());
						if(file != null && file.exists()) {
							if(file.isFile()) {
								copyFile(file, new File(mCurrentDisplayPath, file.getName()));
								new MediaScannerNotifier(FileExpActivity.this, mCurrentDisplayPath+"/"+file.getName(), null);
							} else if(file.isDirectory()) {
								copyFolder(file,  new File(mCurrentDisplayPath, file.getName()));
							}
						} 
					} else if(mSelectedCutCopyOption == MENU_CUT) {
						File file = new File(mLongPressedFile.getPath());
						/*boolean moveStatus =*/ file.renameTo(new File(mCurrentDisplayPath + "/" + file.getName()));
						new MediaScannerNotifier(FileExpActivity.this, mCurrentDisplayPath+"/"+file.getName(), null);
					}
				} else {
					if(mSelectedCutCopyOption == MENU_COPY) {
						for (String filPath : ItemsAdapter.mMultipleSelectFiles) {
							File file = new File(filPath);
							if(file != null && file.exists()) {
								if(file.isFile()) {
									copyFile(file, new File(mCurrentDisplayPath, file.getName()));
									new MediaScannerNotifier(FileExpActivity.this, mCurrentDisplayPath+"/"+file.getName(), null);
								} else if(file.isDirectory()) {
									copyFolder(file,  new File(mCurrentDisplayPath, file.getName()));
								}
							}
						}
					} else if(mSelectedCutCopyOption == MENU_CUT) {
						for (String filPath : ItemsAdapter.mMultipleSelectFiles) {
							File file = new File(filPath);
							if(file != null && file.exists()) {
								/*boolean moveStatus = */file.renameTo(new File(mCurrentDisplayPath + "/" + file.getName()));
								new MediaScannerNotifier(FileExpActivity.this, mCurrentDisplayPath+"/"+file.getName(), null);
							}
						}
					}
				}
			}catch(Exception e) {
				//ackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			pd.setMessage(mCurrentCopyingfile);
		}
		
		@Override
		protected void onPostExecute(Void result) {
			mPasteView.setVisibility(View.GONE);
			refreshList();
			ItemsAdapter.mMultipleSelectFiles.clear();
			pd.dismiss();
		}

		public void copyFolder(File srcDir, File destDir) {
			try {
				doCopyDirectory(srcDir, destDir);
			}catch (Exception e) {
				//e.printStackTrace();		
			}
		}

		public void copyFile(File srcDir, File destDir) {
			try {
				doCopyFile(srcDir, destDir);
			} catch(Exception e) {
				//e.printStackTrace();
			}
		}

		public void doCopyDirectory(File srcDir, File destDir) throws IOException {
			// recurse
			File[] srcFiles =  srcDir.listFiles();
			if (srcFiles == null) {  
				throw new IOException("Failed to list contents of " + srcDir);
			}
			if (destDir.exists()) {
				if (destDir.isDirectory() == false) {
					throw new IOException("Destination '" + destDir + "' exists but is not a directory");
				}
			} else {
				if (!destDir.mkdirs() && !destDir.isDirectory()) {
					throw new IOException("Destination '" + destDir + "' directory cannot be created");
				}
			}
			if (destDir.canWrite() == false) {
				throw new IOException("Destination '" + destDir + "' cannot be written to");
			}

			for (File srcFile : srcFiles) {
				File dstFile = new File(destDir, srcFile.getName());
				if (srcFile.isDirectory()) {
					doCopyDirectory(srcFile, dstFile);
				} else {
					doCopyFile(srcFile, dstFile);
				}
			}

			destDir.setLastModified(srcDir.lastModified());
		}

		private void doCopyFile(File srcFile, File destFile) throws IOException {
			/*if (destFile.exists() && destFile.isDirectory()) {
				throw new IOException("Destination '" + destFile + "' exists but is a directory");
			}*/

			FileInputStream fis = null;
			FileOutputStream fos = null;
			FileChannel input = null;
			FileChannel output = null;
			try {
				fis = new FileInputStream(srcFile);
				fos = new FileOutputStream(destFile);
				input  = fis.getChannel();
				output = fos.getChannel();
				long size = input.size();
				long pos = 0;
				long count = 0;
				mCurrentCopyingfile = srcFile.getName();
				publishProgress();
				while (pos < size) {
					count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
					pos += output.transferFrom(input, pos, count);
				}
			} catch(Exception e) {
				//e.printStackTrace();
			} finally {
				fis.close();
				fos.close();
				input.close();
				output.close();
			}

			if (srcFile.length() != destFile.length()) {
				throw new IOException("Failed to copy full contents from '" +
						srcFile + "' to '" + destFile + "'");
			}
			destFile.setLastModified(srcFile.lastModified());
		}
	}

	/**
	 * Async task to perform file/folder delete operations
	 * @author Manthena Murali
	 *
	 */
	private class DeleteOperationTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog pd;
		private String currentDeleteFileName;

		@Override
		protected void onPreExecute() {
			pd = ProgressDialog.show(FileExpActivity.this, getString(R.string.delete_title), "Starting.. Please wait");
			if(!mIsMultipleOptionSelected) {
				pd.setMessage(mLongPressedFile.getName());
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			if(mIsMultipleOptionSelected) {
				if(ItemsAdapter.mMultipleSelectFiles != null && ItemsAdapter.mMultipleSelectFiles.size() != 0) {
					File delFile;
					for(String delFilePath : ItemsAdapter.mMultipleSelectFiles)  {
						delFile = new File(delFilePath);
						if(delFile != null && delFile.exists()) {
							currentDeleteFileName = delFile.getName();
							publishProgress();
							delete(delFile);
							new MediaScannerNotifier(FileExpActivity.this, delFilePath, null);
						}
					}
				}
			} else {
				try {
					delete(mLongPressedFile);
					new MediaScannerNotifier(FileExpActivity.this, mLongPressedFile.getAbsolutePath(), null);
				} catch(Exception e) {
					//e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			pd.setMessage(currentDeleteFileName);
		}

		@Override
		protected void onPostExecute(Void result) {
			refreshList();
			pd.dismiss();
			if(mIsMultipleOptionSelected) {
				changeFromMultiToNormal();
			}
		}

		private void delete(File file) {
			if(file == null || !file.exists()) {
				return;
			}

			if(file.isFile()) {
				currentDeleteFileName = file.getName();
				publishProgress();
				file.delete();
			} else {
				try {
					deleteDirectory(file);
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
		}

		public void deleteDirectory(File directory) throws IOException {
			if (!directory.exists()) {
				return;
			}
			cleanDirectory(directory);
			if (!directory.delete()) {
				//handle
			}
		}

		private void cleanDirectory(File directory) throws IOException {
			if (!directory.exists() || !directory.isDirectory()) {
				return;
			}

			File[] files = directory.listFiles();
			if (files == null) { 
				return;
			}

			IOException exception = null;
			for (File file : files) {
				try {
					forceDelete(file);
				} catch (IOException ioe) {
					exception = ioe;
				}
			}

			if (null != exception) {
				throw exception;
			}
		}

		public void forceDelete(File file) throws IOException {
			if (file.isDirectory()) {
				deleteDirectory(file);
			} else {
				boolean filePresent = file.exists();
				currentDeleteFileName = file.getName();
				publishProgress();
				boolean res = file.delete();
				if (!res) {
					if (!filePresent){
						throw new FileNotFoundException("File does not exist: " + file);
					}
					String message =
							"Unable to delete file: " + file;
					throw new IOException(message);
				}
			}
		}
	}
	
	/**
	 * adapter to display the favorite paths
	 * @author Manthena Murali
	 *
	 */
	public class FavouritesAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private String[] favPath;

		public FavouritesAdapter(Context context, String[] paths) {
			mInflater = LayoutInflater.from(context);
			favPath = paths;
		} 

		@Override
		public int getCount() {
			return favPath.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FavHolder favHolder;
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.favourites_list, null);

				favHolder = new FavHolder();
				favHolder.favIcon = (ImageView)convertView.findViewById(R.id.fav_icon);
				favHolder.favName = (TextView) convertView.findViewById(R.id.fav_filename);
				favHolder.favPath = (TextView) convertView.findViewById(R.id.fav_filePath);

				convertView.setTag(favHolder);
			} else {
				favHolder = (FavHolder) convertView.getTag();
			} 

			File file = new File(favPath[position]);
			
			favHolder.favPath.setText(file.getParentFile().getPath());
			favHolder.favName.setText(file.getName());
			
			try {
				LayerDrawable circleDrawable = (LayerDrawable) getResources().getDrawable(R.drawable.circle_bg);
				final GradientDrawable shape = (GradientDrawable) circleDrawable.findDrawableByLayerId(R.id.shape_id);
				shape.setColor(ThemeUtils.getPrimaryColor());
				favHolder.favIcon.setBackground(circleDrawable);
			} catch(Exception e) {
				//something wrong
			}
			
			if(file.isDirectory()) {
				favHolder.favIcon.setImageResource(R.drawable.ic_doc_folder);
			} else {
				String extension = ItemOperations.getFileExtension(file.getName());
				displaypredefinedDrawables(extension, favHolder);
			}

			return convertView;
		}
		
		private void displaypredefinedDrawables(String extension, FavHolder holderList) {
			try {
				if(extension == null) {
					holderList.favIcon.setImageResource(R.drawable.ic_doc_generic_am);
				} else {
					Integer resId = FileExtensions.mExtensionDrawables.get(extension.toLowerCase());
					if(resId != null) {
						holderList.favIcon.setImageResource(resId);
					} else {
						holderList.favIcon.setImageResource(R.drawable.ic_doc_generic_am);
					}
				}
			}catch(Exception e) {
				if(holderList.favIcon != null) {
					holderList.favIcon.setImageResource(R.drawable.ic_doc_generic_am);
				}
			}
		}
	}

	static class FavHolder {
		ImageView favIcon;
		TextView favName;
		TextView favPath;
	}
	
	public class MediaScannerNotifier implements MediaScannerConnectionClient {


		private MediaScannerConnection mConnection;
		private String mPath;
		//private String mMimeType;
		public MediaScannerNotifier(Context context, String path, String mimeType) {
			mPath = path;
			//mMimeType = mimeType;
			mConnection = new MediaScannerConnection(context, this);
			mConnection.connect();
		}
		public void onMediaScannerConnected() {
			mConnection.scanFile(mPath, null);
		}
		public void onScanCompleted(String path, Uri uri) {
			try {
				if (uri != null) {                
				}
			} finally {
				mConnection.disconnect();
			}
		}
	
	}
}
