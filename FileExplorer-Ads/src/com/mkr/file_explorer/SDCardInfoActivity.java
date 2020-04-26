package com.mkr.file_explorer;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * this class displays the sd card details
 * 
 * @author Manthena Murali
 *
 */

public class SDCardInfoActivity extends AppCompatActivity{

	private LinearLayout mChartLinearLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sdcardinfo_layout);

		final Toolbar toolbar = (Toolbar) findViewById(R.id.sdcard_usage_toolBar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
		}
		
		final ActionBar actionBar = getSupportActionBar();
		if(actionBar != null) {
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
			actionBar.setTitle(getString(R.string.sd_card_usage));
			actionBar.setDisplayHomeAsUpEnabled(true);
			
			actionBar.setBackgroundDrawable(new ColorDrawable(ThemeUtils.getPrimaryColor()));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				getWindow().setStatusBarColor(ThemeUtils.getDarkPrimaryColor());
			}
		}
		
		mChartLinearLayout = (LinearLayout) findViewById(R.id.sdcard_info_chart);

		new SDcardInfoTask().execute();

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

	private class SDcardInfoTask extends AsyncTask<Void, Void, Void> {

		//total size of the storage
		private long totalSize;
		//free space of the storage
		private long freeSize;

		private StatFs stats;

		int colors[] = { getResources().getColor(R.color.color_audio), 
				getResources().getColor(R.color.color_video), 
				getResources().getColor(R.color.color_images), 
				getResources().getColor(R.color.color_other), 
				getResources().getColor(R.color.color_free)};

		int videoPerc, audioPerc, imagesPerc, otherPerc, freePerc;
		long videoSize, audioSize, imagesSize, otherSize;

		private ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			ContextThemeWrapper mWrapper = new ContextThemeWrapper(SDCardInfoActivity.this, android.R.style.Theme_Holo);
			pd = ProgressDialog.show(mWrapper, null, 
					"Please wait... Loading");
		}

		@SuppressWarnings("deprecation")
		@Override
		protected Void doInBackground(Void... params) {

			try {
				stats = new StatFs(Environment.getExternalStorageDirectory().getPath());

				totalSize = (long) stats.getBlockSize() * (long) stats.getBlockCount();
				freeSize = (long) stats.getAvailableBlocks() * (long) stats.getBlockSize();

				videoSize = getVideoSize();
				audioSize = getAudioSize();
				imagesSize = getImagesSize();
				otherSize = totalSize - freeSize - videoSize - audioSize - imagesSize;

				videoPerc = getPerc(videoSize, totalSize);
				audioPerc = getPerc(audioSize, totalSize);
				imagesPerc = getPerc(imagesSize, totalSize);
				otherPerc = getPerc(otherSize, totalSize);
				freePerc = 100 - videoPerc - audioPerc - imagesPerc	- otherPerc;

			}catch(Exception e) {
				
			}
			return null;
		}


		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			LinearLayout totalSpace = (LinearLayout) findViewById(R.id.include_total_space);
			LinearLayout usedSpace  = (LinearLayout) findViewById(R.id.include_used_space);
			LinearLayout freeSpace  = (LinearLayout) findViewById(R.id.include_free_space);

			((TextView)totalSpace.findViewById(R.id.sdcard_type_name)).setText(R.string.total_space);
			((TextView)totalSpace.findViewById(R.id.sdcard_type_size)).setText(Formatter.formatFileSize(SDCardInfoActivity.this, totalSize));

			((TextView)usedSpace.findViewById(R.id.sdcard_type_name)).setText(R.string.used_space);
			((TextView)usedSpace.findViewById(R.id.sdcard_type_size)).setText(Formatter.formatFileSize(SDCardInfoActivity.this, (totalSize - freeSize)));

			((TextView)freeSpace.findViewById(R.id.sdcard_type_name)).setText(R.string.free_space);
			((TextView)freeSpace.findViewById(R.id.sdcard_type_size)).setText(Formatter.formatFileSize(SDCardInfoActivity.this, freeSize));

			LayoutParams params = mChartLinearLayout.getLayoutParams();
			int screenWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
			params.width = screenWidth;
			mChartLinearLayout.setLayoutParams(params);
			
			for (int i = 0; i < 5; i++) {
				TextView text = new TextView(SDCardInfoActivity.this);
				text.setHeight((int)getResources().getDimension(R.dimen.sdcard_item_height));
				if (i == 0) {
					text.setWidth(Math.round(audioPerc * 5));
					text.setBackgroundColor(colors[0]);
				} else if (i == 1) {
					text.setWidth(Math.round(videoPerc * 5));
					text.setBackgroundColor(colors[1]);
				} else if (i == 2) {
					text.setWidth(Math.round(imagesPerc * 5));
					text.setBackgroundColor(colors[2]);
				} else if (i == 3) {
					text.setWidth(Math.round(otherPerc * 5));
					text.setBackgroundColor(colors[3]);
				} else {
					text.setWidth(Math.round(freePerc * 5));
					text.setBackgroundColor(colors[4]);
				}

				mChartLinearLayout.addView(text, i);
			}


			LinearLayout audio = (LinearLayout) findViewById(R.id.include_audio);
			LinearLayout video  = (LinearLayout) findViewById(R.id.include_video);
			LinearLayout images  = (LinearLayout) findViewById(R.id.include_images);
			LinearLayout used  = (LinearLayout) findViewById(R.id.include_used);
			LinearLayout free  = (LinearLayout) findViewById(R.id.include_free);

			((TextView)audio.findViewById(R.id.sdcard_info_item_color)).setBackgroundColor(colors[0]);
			((TextView)video.findViewById(R.id.sdcard_info_item_color)).setBackgroundColor(colors[1]);
			((TextView)images.findViewById(R.id.sdcard_info_item_color)).setBackgroundColor(colors[2]);
			((TextView)used.findViewById(R.id.sdcard_info_item_color)).setBackgroundColor(colors[3]);
			((TextView)free.findViewById(R.id.sdcard_info_item_color)).setBackgroundColor(colors[4]);

			((TextView)audio.findViewById(R.id.sdcard_info_item_type)).setText(R.string.audio);
			((TextView)video.findViewById(R.id.sdcard_info_item_type)).setText(R.string.video);
			((TextView)images.findViewById(R.id.sdcard_info_item_type)).setText(R.string.images);
			((TextView)used.findViewById(R.id.sdcard_info_item_type)).setText(R.string.other_files);
			((TextView)free.findViewById(R.id.sdcard_info_item_type)).setText(R.string.free_space);

			((TextView)audio.findViewById(R.id.sdcard_info_item_size)).setText(Formatter.formatFileSize(SDCardInfoActivity.this, audioSize));
			((TextView)video.findViewById(R.id.sdcard_info_item_size)).setText(Formatter.formatFileSize(SDCardInfoActivity.this, videoSize));
			((TextView)images.findViewById(R.id.sdcard_info_item_size)).setText(Formatter.formatFileSize(SDCardInfoActivity.this, imagesSize));
			((TextView)used.findViewById(R.id.sdcard_info_item_size)).setText(Formatter.formatFileSize(SDCardInfoActivity.this, otherSize));
			((TextView)free.findViewById(R.id.sdcard_info_item_size)).setText(Formatter.formatFileSize(SDCardInfoActivity.this, freeSize));

			if(pd != null && pd.isShowing()) {
				pd.dismiss();
			}

		}

		/**
		 * calculate the percentage 
		 * @param xval numerator value	 
		 * @param yval denominator value
		 * @return the percentage value
		 */
		public int getPerc(final long xval, final long yval) {
			return (int)(((double)xval/(double)yval) * 100);
		}

		/**
		 * total sum of size of all video files
		 * @return video files size
		 */
		public long getVideoSize() {
			Cursor video = getContentResolver().query(
					MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
					new String[] { "sum(_size)" }, null, null, null);
			if(video != null) {
				video.moveToFirst();
				return video.getLong(0);
			}
			
			return 0;
		}

		/**
		 * total sum of size of all audio files
		 * @return audio files size
		 */
		public long getAudioSize() {
			Cursor audio = getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[] { "sum(_size)" }, null, null, null);
			if(audio != null) {
				audio.moveToFirst();
				return audio.getLong(0);
			}
			
			return 0;
		}

		/**
		 * total sum of size of all image files
		 * @return image files size
		 */
		public long getImagesSize() {
			Cursor pictures = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					new String[] { "sum(_size)" }, null, null, null);
			if(pictures != null) {
				pictures.moveToFirst();
				return pictures.getLong(0);
			}

			return 0;
		}

	}

}
