package com.deadbeat.bluetoothalertfree;

import android.app.Activity;
import android.app.Service;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class AppDetector {
	protected boolean isAppInstalledFromService(Service parent, String uri) {
		 PackageManager pm = parent.getPackageManager();
		 boolean installed = false;
		 try {
			 pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			 installed = true;
		 } catch (PackageManager.NameNotFoundException e) {
			 installed = false;
		 }
		 return installed;
	}
	
	protected boolean isAppInstalledFromActivity(Activity parent, String uri) {
		 PackageManager pm = parent.getPackageManager();
		 boolean installed = false;
		 try {
			 pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			 installed = true;
		 } catch (PackageManager.NameNotFoundException e) {
			 installed = false;
		 }
	 	return installed;
	}
	
	protected String getVersion(Activity parent, String uri) {
		String version = null;
		PackageManager pm = parent.getPackageManager();
		try {
			PackageInfo pInfo = pm.getPackageInfo(uri, PackageManager.GET_META_DATA);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			version = "0.0";
		}
		return version;
	}
}
