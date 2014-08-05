package com.iliakplv.notes.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;

public final class Utils {
	private Utils() {
		throw new AssertionError("Instance creation not allowed!");
	}

	public static String getVersionName() {
		try {
			final Context context = NotesApplication.getContext();
			return context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			AppLog.wtf("About", "Can't get app version", e);
			return "1";
		}
	}

	public static String getDeviceInformation() {
		return "Version: " + getVersionName() +
				"\n" + "Translation: " + NotesApplication.getContext().getString(R.string.translation) +
				"\n" + "Device: " + Build.MANUFACTURER + " " + Build.MODEL +
				"\n" + "API level: " + Build.VERSION.SDK_INT;
	}
}
