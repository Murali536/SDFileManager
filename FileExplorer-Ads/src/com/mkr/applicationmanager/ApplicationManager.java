package com.mkr.applicationmanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mkr.file_explorer.ItemDetails;
import com.mkr.file_explorer.R;
import com.mkr.file_explorer.ThemeUtils;

public class ApplicationManager extends AppCompatActivity {

	private AdView mAdView;
	private ApplicationsAdapter mAppsAdapter;
	private ArrayList<AppInfo> mAppsList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.app_manager_layout_ui);

		final Toolbar toolbar = (Toolbar) findViewById(R.id.app_mgr_toolBar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
		}
		
		final ActionBar actionBar = getSupportActionBar();
		if(actionBar != null) {
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
			actionBar.setTitle(getString(R.string.app_manager));
			actionBar.setDisplayHomeAsUpEnabled(true);
			
			actionBar.setBackgroundDrawable(new ColorDrawable(ThemeUtils.getPrimaryColor()));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				getWindow().setStatusBarColor(ThemeUtils.getDarkPrimaryColor());
			}
		}
		
		ListView mApplicationsListView = (ListView) findViewById(R.id.application_manager_listview);
		mApplicationsListView.setCacheColorHint(Color.TRANSPARENT);
		mApplicationsListView.setFastScrollEnabled(true);
		mApplicationsListView.setDivider(getResources().getDrawable(R.drawable.list_seperator));
		mApplicationsListView.setDividerHeight(1);
		mApplicationsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AppsListHolder holder = (AppsListHolder) view.getTag();
				launchApplication(holder.appName.getContentDescription().toString());
			}
		});
		mApplicationsListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				displayLongPressOptions(view);
				return true;
			}
		});
		
		mAppsList = new ArrayList<ApplicationManager.AppInfo>();
		mAppsAdapter = new ApplicationsAdapter(); 
		mApplicationsListView.setAdapter(mAppsAdapter);
		
		new InstalledAppsInfo().execute();
		
		mAdView = (AdView)this.findViewById(R.id.app_mgr_ads);
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
	    
		Toast.makeText(ApplicationManager.this, getString(R.string.application_manager_welcome_msg), Toast.LENGTH_SHORT).show();
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addDataScheme("package");
		registerReceiver(broadcastReceiver, intentFilter);
	}

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			new InstalledAppsInfo().execute();
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		mAdView.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mAdView.pause();
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
	
	private void displayLongPressOptions(final View view) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose an option");

		final AppsListHolder holder = (AppsListHolder) view.getTag();

		List<String> itemsToDisplay = new ArrayList<String>();
		itemsToDisplay.add(getString(R.string.launch));
		if(!holder.isSystemApp) {
			itemsToDisplay.add(getString(R.string.backup));
			itemsToDisplay.add(getString(R.string.uninstall));
		} 
		itemsToDisplay.add(getString(R.string.details));
		String[] items = new String[itemsToDisplay.size()];
		items = itemsToDisplay.toArray(items);
		
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					launchApplication(holder.appName.getContentDescription().toString());
					break;
				case 1:
					new BackupManager().execute(holder.appInfo.packageName);
					break;
				case 2:
					uninstallApplication(holder.appName.getContentDescription().toString());
					break;
				case 3:
					showInstalledAppDetails(holder.appInfo.packageName);
					break;
				default:
					dialog.dismiss();
					break;
				}
			}
		});
		
		builder.create().show();
		
		/*final ListView list = new ListView(ApplicationManager.this);
		list.setDivider(getResources().getDrawable(R.drawable.list_seperator));
		list.setCacheColorHint(Color.TRANSPARENT);
		String[] opts = new String[itemsToDisplay.size()];
		opts = itemsToDisplay.toArray(opts); 
		list.setAdapter(new ArrayAdapter<String>(ApplicationManager.this, R.layout.options_list_item, opts));
		builder.setView(list);

		final AlertDialog dialog = builder.create();
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String text = ((TextView)arg1).getText().toString();
				if(getString(R.string.launch).equalsIgnoreCase(text)) {
					launchApplication(holder.appName.getContentDescription().toString());
				} else if(getString(R.string.uninstall).equalsIgnoreCase(text)) {
					uninstallApplication(holder.appName.getContentDescription().toString());
				} else if(getString(R.string.details).equalsIgnoreCase(text)) {
					showInstalledAppDetails(holder.appInfo.packageName);
				} else if(getString(R.string.backup).equalsIgnoreCase(text)) {
					new BackupManager().execute(holder.appInfo.packageName);
				} else {
					//nothing
				}
				dialog.dismiss();
			}
		});*/
	}

	private void launchApplication(String packageName) {
		String className = null;
		final Intent mIntent = getPackageManager().getLaunchIntentForPackage(packageName); 
		if (mIntent != null) {
			if (mIntent.getComponent() != null) {
				className = mIntent.getComponent().getClassName();
			}
		}

		final Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(new ComponentName(packageName, className));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	private void uninstallApplication(String packageName) {
		Uri packageURI = Uri.parse("package:"+packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		startActivity(uninstallIntent);
	}

	public void showInstalledAppDetails(String packageName) {
	    final int apiLevel = Build.VERSION.SDK_INT;
	    final Intent intent = new Intent();
	    if (apiLevel >= 9) {
	        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
	        intent.setData(Uri.parse("package:" + packageName));
	    } else {
	        final String appPkgName = (apiLevel == 8 ? "pkg" : "com.android.settings.ApplicationPkgName");
	        intent.setAction(Intent.ACTION_VIEW);
	        intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
	        intent.putExtra(appPkgName, packageName);
	    }
	    startActivity(intent);
	}
	
	/*private void applicationDetails(AppsListHolder holder) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AppInfo appInfo = holder.appInfo;

		final ScrollView sc = (ScrollView) View.inflate(ApplicationManager.this, R.layout.app_manager_app_details, null);
		final TextView appDetails = (TextView) sc.findViewById(R.id.app_details_app_details);

		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		sb.append(" Package Name : " + appInfo.packageName + "\n");
		sb.append(" APK Size     : "+ appInfo.apkSize + "\n");
		sb.append(" App Version  : "+ appInfo.appVersion + "\n");
		appDetails.setText(sb.toString());
		
		builder.setIcon(appInfo.appIcon);
		builder.setTitle(appInfo.applicationName);
		builder.setView(sc);
		
		builder.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		
		builder.create().show();
	}*/
	
	class BackupManager extends AsyncTask<String, Void, Void> {
		
		private ProgressDialog pd;
		private boolean status;
		final String backupLocation = Environment.getExternalStorageDirectory().getPath() + "/Backup/";
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = ProgressDialog.show(ApplicationManager.this, null, getString(R.string.backup_pd_msg));
		}
		
		@Override
		protected Void doInBackground(String... params) {
			//Looper.prepare();
			backupApp(params[0]);
			return null;
		}
		
		private void backupApp(final String pkgname){
			try {
				final PackageManager pkgManager = getPackageManager();
				final ApplicationInfo info = pkgManager.getApplicationInfo(pkgname, 0);
				final String sourceDir = info.sourceDir;
				final String out_file = info.loadLabel(pkgManager).toString()+".apk";
				
				final BufferedInputStream mBuffIn;
				final BufferedOutputStream mBuffOut;

				int read = 0;
				final File mDir = new File(backupLocation);
				byte[] mData;

				final int BUFFER = 256;
				mData =  new byte[BUFFER];

				/*create dir if needed*/
				final File d = new File(backupLocation);
				if(!d.exists()) {
					d.mkdir();
					//then create this directory
					mDir.mkdir();
				} else {
					if(!mDir.exists()) {
						mDir.mkdir();
					}
				}

				try {
					mBuffIn = new BufferedInputStream(new FileInputStream(sourceDir));
					mBuffOut = new BufferedOutputStream(new FileOutputStream(backupLocation + out_file));

					while((read = mBuffIn.read(mData, 0, BUFFER)) != -1) {
						mBuffOut.write(mData, 0, read);
					}

					status = true;
					
					mBuffOut.flush();
					mBuffIn.close();
					mBuffOut.close();
				} catch (FileNotFoundException e) {
					status = false;
				} catch (Exception e) {
					status = false;
				}
			} catch (NameNotFoundException e) {
				status = false;
			}
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if(pd != null && pd.isShowing()) {
				pd.cancel();
			}
			
			if(status) {
				Toast.makeText(ApplicationManager.this, getString(R.string.backup_done) +" "+backupLocation, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(ApplicationManager.this, getString(R.string.backup_failed), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	class InstalledAppsInfo extends AsyncTask<Void, Void, Void> {

		private ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mAppsList = new ArrayList<AppInfo>();
			pd = ProgressDialog.show(ApplicationManager.this, null, getString(R.string.loading_msg));
		}

		@Override
		protected Void doInBackground(Void... params) {

			final ItemDetails itemDetails = ItemDetails.getInstance();
			final PackageManager pm = getPackageManager();
			final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			final List<ResolveInfo> packs = pm.queryIntentActivities( mainIntent, 0);

			for(ResolveInfo rInfo : packs) {
				final ActivityInfo aInfo = rInfo.activityInfo;
				final AppInfo newInfo = new AppInfo();
				newInfo.packageName = aInfo.packageName;
				newInfo.applicationName = aInfo.applicationInfo.loadLabel(pm).toString();
				newInfo.appIcon = rInfo.loadIcon(pm);

				try {
					final ApplicationInfo ai = pm.getApplicationInfo(aInfo.packageName, 0);
					if((ai.flags & ApplicationInfo.FLAG_SYSTEM)!=0){
						newInfo.isSystemApp = true;
					} else {
						newInfo.isSystemApp = false;
					}
				} catch (NameNotFoundException e1) {
					newInfo.isSystemApp = false;
				}

				if(!newInfo.isSystemApp && !"com.mkr.file_explorer".equalsIgnoreCase(newInfo.packageName)) {
					try {
						Method getPackageSizeInfo = pm.getClass().getMethod(
								"getPackageSizeInfo", String.class, IPackageStatsObserver.class);
						getPackageSizeInfo.invoke(pm, aInfo.packageName, new IPackageStatsObserver.Stub() {
							@Override
							public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
									throws RemoteException {
								newInfo.apkSize = itemDetails.readableFileSize(pStats.codeSize);
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
						newInfo.apkSize = null;
					}
				}

				if(!newInfo.isSystemApp && !"com.mkr.file_explorer".equalsIgnoreCase(newInfo.packageName)) {
					mAppsList.add(newInfo);
				}
				
				Collections.sort(mAppsList, new AppInfo.AgeComparator());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			mAppsAdapter.notifyDataSetChanged();
			if(pd != null && pd.isShowing()) {
				pd.cancel();
			}
		}
	}

	static class AppInfo implements Comparable<AppInfo> {
		private String applicationName;
		private String packageName;
		private Drawable appIcon;
		private String apkSize;
		private boolean isSystemApp;
		
		@Override
		public int compareTo(AppInfo arg0) {
			return applicationName.compareTo(arg0.applicationName);
		}
		
		static class AgeComparator implements Comparator<AppInfo> {
	    	public int compare(AppInfo p1, AppInfo p2) {
	    		
	    		String appName1 = p1.applicationName;
	    		String appName2 = p2.applicationName;

	    		if (appName1.charAt(0) == appName2.charAt(0))
	    			return 0;
	    		else if (appName1.charAt(0) > appName2.charAt(0))
	    			return 1;
	    		else
	    			return -1;
	    	}
	    }
	}
	
	class ApplicationsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mAppsList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			AppsListHolder appHolder;
			if(arg1 == null) {
				appHolder = new AppsListHolder();
				arg1 = View.inflate(ApplicationManager.this, R.layout.app_manager_list_item, null);

				appHolder.appIcon = (ImageView) arg1.findViewById(R.id.app_manager_app_icon);
				appHolder.appName = (CheckedTextView) arg1.findViewById(R.id.app_manager_app_name);
				appHolder.appName.setCheckMarkDrawable(new ColorDrawable(Color.TRANSPARENT));
				appHolder.appSize = (TextView) arg1.findViewById(R.id.app_manager_app_size);

				arg1.setTag(appHolder);
			} else {
				appHolder = (AppsListHolder) arg1.getTag();
			}

			AppInfo appInfo = mAppsList.get(arg0);
			appHolder.appIcon.setBackground(appInfo.appIcon);
			appHolder.appName.setText(appInfo.applicationName);
			appHolder.appName.setContentDescription(appInfo.packageName);
			appHolder.appSize.setText(appInfo.apkSize);
			appHolder.isSystemApp = appInfo.isSystemApp;
			appHolder.appInfo = appInfo;

			return arg1;
		}

	}

	static class AppsListHolder {
		ImageView appIcon;
		CheckedTextView appName;
		TextView appSize;
		boolean isSystemApp;
		AppInfo appInfo;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//Log.e("mkr","on destroy");
		unregisterReceiver(broadcastReceiver);
	}
}


