package com.mkr.file_explorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * this class displays the search results
 * For now only files are displayed, no folders
 * 
 * @author Manthena Murali
 *
 */
public class SearchActivity extends AppCompatActivity {

	/**
	 * list view that displays all the search results
	 */
	private ListView mSearchListView;
	/**
	 * the search string entered by the user
	 */
	private String mSearchString;
	
	private MimeTypes mMimeTypes;
	private int mWhatToDisplay;

	private LayerDrawable mCircleDrawable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.search_activity_layout);

		Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        
        final ActionBar actionBar = getSupportActionBar();
		if(actionBar != null) {
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
			actionBar.setTitle(getString(R.string.search));
			actionBar.setDisplayHomeAsUpEnabled(true);
			
			actionBar.setBackgroundDrawable(new ColorDrawable(ThemeUtils.getPrimaryColor()));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				getWindow().setStatusBarColor(ThemeUtils.getDarkPrimaryColor());
			}
		}
		
		mMimeTypes = new MimeTypes();
		
		mSearchListView = new ListView(SearchActivity.this);
		mSearchListView.setCacheColorHint(Color.TRANSPARENT);
		mSearchListView.setFastScrollEnabled(true);
		mSearchListView.setDivider(null);
		mSearchListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				//directly open the file based on its type
				ViewHolderList holderList = (ViewHolderList) arg1.getTag();
				TextView textView = holderList.itemName;
				String path = textView.getContentDescription().toString();
				File file = new File(path);
				fileSupportedApplications(file);
			}
		});
		
		mSearchListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				longPressOptions(arg1);
				return true;
			}
		});

		mWhatToDisplay = FileExtensions.FILE_ALL_TYPES;
		Intent intent = getIntent();
		if(intent != null) {
			Bundle b = intent.getExtras();
			if(b != null) {
				//what kind of types ie.. all or audio, video etc..
				mWhatToDisplay = b.getInt("search_key");
				//text to be searched
				mSearchString = b.getString("search_string");
			}
		}

		mCircleDrawable = (LayerDrawable) getResources().getDrawable(R.drawable.circle_bg);
		final GradientDrawable shape = (GradientDrawable) mCircleDrawable.findDrawableByLayerId(R.id.shape_id);
		shape.setColor(ThemeUtils.getPrimaryColor());
		
		SearchAsync searchTask = new SearchAsync();
		searchTask.execute(mWhatToDisplay);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return true;
	}
	
	private void longPressOptions(View longPressedView) {

		AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
		builder.setTitle(getString(R.string.options));

		ViewHolderList holderList = (ViewHolderList) longPressedView.getTag();
		TextView textView = holderList.itemName;
		String path = textView.getContentDescription().toString();
		final File file = new File(path);
		
		builder.setItems(R.array.search_list_options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					fileSupportedApplications(file);
					break;
				case 1:
					// send the files parent path to be displayed in the main activity
					Intent resultIntent = new Intent();
					resultIntent.putExtra("open_path", file.getParent());
					setResult(RESULT_OK, resultIntent);
					finish();
					break;
				case 2:
					sendFile(file);
					break;
				case 3:
					try {
						if(file.exists()) {
							file.delete();
							
							Toast.makeText(SearchActivity.this, "File has been deleted.", Toast.LENGTH_SHORT).show();
							new MediaScannerNotifier(SearchActivity.this, file.getAbsolutePath(), null);
							
							SearchAsync searchTask = new SearchAsync();
							searchTask.execute(mWhatToDisplay);
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case 4:
					mHandler.sendMessage(Message.obtain(mHandler, 0, file));
					break;
				default:
					break;
				}
				dialog.dismiss();
			}
		});
		
		/*builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});*/
		
		builder.create().show();
	}
	
	/**
	 * this will display a progress dialog before displaying a dialog, generally used when
	 * operations take long time which can lead to ANR
	 * 
	 * @param id
	 */
	/*private void beforeDisplayingDialog(final File file) {
		final ProgressDialog progress = ProgressDialog.show(SearchActivity.this, "","Loading info..."); 
		new Thread(new Runnable() { 
			public void run() { 
				ItemDetails itemDetails = ItemDetails.getInstance();
				String str = itemDetails.fileDetails(file);
				
				Message msg = Message.obtain();
				msg.obj = str;
				mHandler.sendMessage(msg);
				
				progress.dismiss();
			}
		}).start();
	}*/
	
	Handler mHandler = new Handler () {
		@Override
		public void handleMessage(Message msg) {
			//displayPropertiesDialog((String)msg.obj);
			
			File file = (File) msg.obj;
			ItemDetails itemDetails = ItemDetails.getInstance();
			TableLayout tableLayout = (TableLayout) View.inflate(SearchActivity.this, R.layout.properties_dialog_layout, null);
			itemDetails.fileDetails(file, tableLayout);
			
			displayPropertiesDialog(tableLayout);
		}
	};
	/**
	 * display the file/folder properties
	 * @param tableLayout
	 */
	private void displayPropertiesDialog(TableLayout tableLayout) {
		AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
		builder.setTitle(R.string.properties);

		ScrollView scrollView = new ScrollView(SearchActivity.this);
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
			Toast.makeText(SearchActivity.this, "No Supported Applications Found", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * when send options is selected display a list of apps that can be used
	 * to transfer the file. this options is only available for files
	 */
	private void sendFile(File file) {

		String filename = file.getName();
		MimeTypes mt = new MimeTypes(); 

		Intent i = new Intent();
		i.setAction(Intent.ACTION_SEND);
		i.setType(mt.getMimeType(filename));
		i.putExtra(Intent.EXTRA_SUBJECT, filename);
		i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		i = Intent.createChooser(i, "send "+filename+" using");

		try {
			startActivity(i);
		} catch (Exception e) {
			Toast.makeText(SearchActivity.this, "Unable to send", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * search async task
	 * 
	 * @author Manthena Murali
	 *
	 */
	private class SearchAsync extends AsyncTask<Integer, Void, Void> {

		private ProgressDialog pd;
		private ContentResolver resolver;
		private List<String> mSearchResults;
		private MergeCursor mergeCusrsor; 

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pd = ProgressDialog.show(SearchActivity.this, getString(R.string.searching_dialog_title), 
					getString(R.string.searching_dialog_body));
			resolver = SearchActivity.this.getContentResolver();
			mSearchResults = new ArrayList<String>();
		}

		@Override
		protected Void doInBackground(Integer... params) {

			int serachItems = params[0];
			Cursor c = null;
			int totalCount = 0;

			switch (serachItems) {
			case FileExtensions.FILE_ALL_TYPES:
				Cursor cursor[] = new Cursor[3];
				cursor[0] = getAudioCursor();
				cursor[1] = getVideoCursor();
				cursor[2] = getImagesCursor();
				
				mergeCusrsor = new MergeCursor(cursor);
				c = mergeCusrsor;
				break;
			case FileExtensions.FILE_TYPE_AUDIO:
				c = getAudioCursor();
				break;
			case FileExtensions.FILE_TYPE_VIDEO:
				c = getVideoCursor();
				break;
			case FileExtensions.FILE_TYPE_IMAGE:
				c = getImagesCursor();
				break;
			}


			if(c != null) {
				totalCount = c.getCount();
				c.moveToFirst();
				for(int j=0; j<totalCount; j++){
					int index = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
					mSearchResults.add(c.getString(index));
					index = j;
					c.moveToNext();
				}
				c.close();
			}

			return null;
		}


		public Cursor getAudioCursor() {
			return resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, 
					MediaStore.Audio.AudioColumns.DISPLAY_NAME + " LIKE '%" + mSearchString.toString() + "%'  ", null, null);
		}

		public Cursor getVideoCursor() {
			return resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, 
					MediaStore.Video.VideoColumns.DISPLAY_NAME + " LIKE '%" + mSearchString.toString() + "%' ", null, null);
		}

		public Cursor getImagesCursor() {
			return resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, 
					MediaStore.Images.ImageColumns.DISPLAY_NAME + " LIKE '%" + mSearchString.toString() + "%' ", null, null);
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			TextView titleView = (TextView) findViewById(R.id.search_titlebar);
			titleView.setText(getString(R.string.search_title) + "( " + mSearchResults.size() + " )");
			
			LinearLayout listParent = (LinearLayout) findViewById(R.id.search_list_parent);
			listParent.removeAllViews();
			
			if(mSearchResults != null && mSearchResults.size() == 0) {
				listParent.addView(addEmtyTextView(getString(R.string.no_search_results)));
			} else {
				listParent.addView(mSearchListView);
				SearchAdapter searchAdapter = new SearchAdapter(mSearchResults);
				mSearchListView.setAdapter(searchAdapter);
			}

			if(pd != null && pd.isShowing()) {
				pd.dismiss();
			}
		}
		
		/**
		 * this is a empty text view to add and message. used to display empty folder 
		 * text when a empty folder is opened
		 * @param msg
		 */
		private View addEmtyTextView(String msg) {
			TextView text = new TextView(SearchActivity.this);
			text.setWidth(getResources().getDisplayMetrics().widthPixels);
			text.setHeight(getResources().getDisplayMetrics().heightPixels - 100);
			text.setGravity(Gravity.CENTER);
			text.setPadding(0, 0, 0, 150);
			text.setTextColor(Color.BLACK);
			text.setTextSize(22);
			text.setText(msg);
			
			return text;
		}
	}

	private class SearchAdapter extends BaseAdapter {

		private List<String> mSearchResults;
		private LayoutInflater mInflater;

		public SearchAdapter(List<String> results) {
			mSearchResults = results;
			mInflater = LayoutInflater.from(SearchActivity.this);
		}

		@Override
		public int getCount() {
			return mSearchResults.size();
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
			ViewHolderList holderList;
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.explorer_list_item, null);

				holderList = new ViewHolderList();
				holderList.itemIcon = (ImageView)convertView.findViewById(R.id.item_icon);
				holderList.itemName = (TextView)convertView.findViewById(R.id.item_name);
				holderList.itemPath = (TextView)convertView.findViewById(R.id.search_path);
				holderList.itemPath.setVisibility(View.VISIBLE);

				//disable the checkbox
				convertView.findViewById(R.id.item_checkbox).setVisibility(View.GONE);

				holderList.itemName.setTypeface(Utils.getInstance().getDisplayTypeface());
				holderList.itemPath.setTypeface(Utils.getInstance().getDisplayTypeface());
				
				convertView.setTag(holderList);
			} else {
				holderList = (ViewHolderList) convertView.getTag();
			} 

			holderList.itemIcon.setBackground(mCircleDrawable);
			
			File file = new File( mSearchResults.get(position));
			String extension = ItemOperations.getFileExtension(file.getName());
			displaypredefinedDrawables(extension, holderList);
			
			holderList.itemName.setText(file.getName());
			holderList.itemName.setContentDescription(mSearchResults.get(position));
			
			holderList.itemPath.setText(file.getPath());
			
			return convertView;
		}
		
		private void displaypredefinedDrawables(String extension, ViewHolderList holderList) {
			try {
				if(extension == null) {
					holderList.itemIcon.setImageResource(R.drawable.ic_doc_generic_am);
				} else {
					//get the resource id from the already loaded list. if not found in the list just load the default file
					//icon
					Integer resId = FileExtensions.mExtensionDrawables.get(extension.toLowerCase());
					if(resId != null) {
						holderList.itemIcon.setImageResource(resId);
					} else {
						holderList.itemIcon.setImageResource(R.drawable.ic_doc_generic_am);
					}
				}
			}catch(Exception e) {
				if(holderList.itemIcon != null) {
					holderList.itemIcon.setImageResource(R.drawable.ic_doc_generic_am);
				}
			}
		}
	}

	static class ViewHolderList {
		ImageView itemIcon;
		TextView itemName;
		TextView itemPath;
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
